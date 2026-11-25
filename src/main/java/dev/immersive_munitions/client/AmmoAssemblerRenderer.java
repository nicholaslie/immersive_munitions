package dev.immersive_munitions.client;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic;
import dev.immersive_munitions.multiblock.AmmoAssemblerLogic.State;
import dev.immersive_munitions.multiblock.AssemblyProcess;
import dev.immersive_munitions.registry.ModMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws the Ammunition Assembler's moving parts: casings on the belt, the three station heads, the handwheel, the
 * power lamp and the two front screens. The static machine is the block model; the moving parts are the
 * ammo_assembler_[group] models exported by art/assembler/texture.py.
 * <p>
 * Coordinates are in the model's frame (block units from the master block's corner): the belt runs along x from -1
 * to 2 at z = 1.5, its top at y = 0.125, with the stations at x = -0.5, 0.5 and 1.5.
 */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AmmoAssemblerRenderer implements BlockEntityRenderer<MultiblockBlockEntityMaster<State>> {
    // Moving parts; the lamp's lit art is one tile right on the 128px sheet
    private static final ResourceLocation POWDER = part("powder"), WHEEL = part("wheel"), JAWS = part("jaws"),
            PRESS = part("press"), LAMP = part("lamp");
    private static final float LAMP_TILE_SHIFT = 16 / 128f;

    // Front screens: centred at x = -0.5 and 0.5, icon on the left, count on the right
    private static final float SCREEN_POWDER_X = -0.5f, SCREEN_FEEDER_X = 0.5f, SCREEN_Y = 1.63f, SCREEN_Z = 2.015f;
    private static final float SCREEN_ICON_DX = -0.12f, SCREEN_ICON_SIZE = 0.2f, SCREEN_COUNT_DX = 0.1f;
    private static final float SCREEN_TEXT_SCALE = 0.012f;
    private static final int SCREEN_TEXT = 0xFF5CFF7A, SCREEN_EMPTY = 0xFFFF4A3A;

    // Belt and station heads: a head strokes while a casing is within WINDOW of its station
    private static final float BELT_START = -1, BELT_LENGTH = 3, BELT_Z = 1.5f, BELT_TOP = 0.125f;
    private static final float ITEM_SCALE = 0.3125f;
    private static final float WINDOW = 0.05f;
    private static final float POWDER_STROKE = 0.06f, FEED_STROKE = 0.06f, PRESS_STROKE = 0.07f;
    private static final float WHEEL_X = -0.5f, WHEEL_Y = 1.03f;

    // Projectiles: held in the feeder's jaws, then seated in the casing's mouth just in front of it. Drawn smaller
    // than casings because their sprites fill more of the icon
    private static final float FEEDER_X = 0.5f, FEEDER_BOTTOM = 0.45f, FEEDER_SCALE = 0.2f;
    private static final float SEAT_FRONT = 0.006f;

    private static ResourceLocation part(String group) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "block/ammo_assembler_" + group);
    }

    // ---- Registration ----

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation model : List.of(POWDER, WHEEL, JAWS, PRESS, LAMP)) event.register(model);
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (net.minecraft.world.level.block.entity.BlockEntityType<MultiblockBlockEntityMaster<State>>)
                        ModMultiblocks.AMMO_ASSEMBLER.masterBE().get(),
                ctx -> new AmmoAssemblerRenderer());
    }

    // ---- Frame ----

    @Override
    public void render(MultiblockBlockEntityMaster<State> be, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;
        IMultiblockBEHelperMaster<State> helper = be.getHelper();
        State state = helper.getState();
        Direction facing = be.getBlockState().getValue(IEProperties.FACING_HORIZONTAL);
        // light from the open belt block, not the master, which sits in the solid back row
        BlockPos belt = helper.getContext().getLevel().toAbsolute(new BlockPos(1, 1, 1));
        int light = LevelRenderer.getLightColor(level, belt);

        // each casing's position along the belt, 0..1
        List<AssemblyProcess> casings = new ArrayList<>();
        List<Float> progress = new ArrayList<>();
        for (MultiblockProcess<AmmoAssemblyRecipe, ?> process : state.processor.getQueue()) {
            if (!(process instanceof AssemblyProcess casing)) continue;
            int max = casing.getMaxTicks(level);
            if (max <= 0) continue;
            float tick = casing.processTick + (casing.isMoving() ? partialTicks : 0);
            casings.add(casing);
            progress.add(Mth.clamp(tick / max, 0, 1));
        }

        // turn to match the static model's blockstate rotation
        pose.pushPose();
        int yRot = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yRot));
        pose.translate(-0.5, -0.5, -0.5);
        Rotation rotation = switch (yRot) {
            case 90 -> Rotation.CLOCKWISE_90;
            case 180 -> Rotation.CLOCKWISE_180;
            case 270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };

        // station heads, the hopper's projectile in the feeder's jaws, and the power lamp
        float fill = stroke(progress, AmmoAssemblerLogic.FILL_POINT);
        float feed = stroke(progress, AmmoAssemblerLogic.PLACE_POINT);
        float press = stroke(progress, AmmoAssemblerLogic.PRESS_POINT);

        drawPart(POWDER, 0, -POWDER_STROKE * fill, pose, buffers, rotation, light, packedOverlay);
        drawPart(JAWS, 0, -FEED_STROKE * feed, pose, buffers, rotation, light, packedOverlay);
        ItemStack projectile = state.shownProjectiles();
        if (!projectile.isEmpty())
            drawItem(projectile, FEEDER_X, FEEDER_BOTTOM - FEED_STROKE * feed, FEEDER_SCALE, pose, buffers, level,
                    light, packedOverlay);
        drawPart(PRESS, 0, -PRESS_STROKE * press, pose, buffers, rotation, light, packedOverlay);
        if (state.isPowered()) drawLitLamp(pose, buffers, packedOverlay);
        else drawPart(LAMP, 0, 0, pose, buffers, rotation, light, packedOverlay);

        // front screens: what each hopper holds
        drawScreen(state.shownGunpowder(), SCREEN_POWDER_X, pose, buffers, level, packedOverlay);
        drawScreen(state.shownProjectiles(), SCREEN_FEEDER_X, pose, buffers, level, packedOverlay);

        // handwheel: a quarter turn per casing filled, easing through the one under it (a casing counts as filled
        // halfway through, so that quarter comes back off until it finishes)
        float turns = state.clientFills();
        for (int i = 0; i < casings.size(); i++) {
            float p = progress.get(i), start = AmmoAssemblerLogic.FILL_POINT - WINDOW;
            if (p <= start || p >= AmmoAssemblerLogic.FILL_POINT + WINDOW) continue;
            turns += (p - start) / (2 * WINDOW) - (casings.get(i).isFilled() ? 1 : 0);
        }
        float angle = turns * Mth.HALF_PI;
        pose.pushPose();
        pose.translate(WHEEL_X, WHEEL_Y, 0);
        pose.mulPose(Axis.ZP.rotation(angle));
        pose.translate(-WHEEL_X, -WHEEL_Y, 0);
        drawPart(WHEEL, 0, 0, pose, buffers, rotation, light, packedOverlay);
        pose.popPose();

        // casings: bare, then with the projectile seated (after the feeder), then the finished round (after the press)
        for (int i = 0; i < casings.size(); i++) {
            AssemblyProcess casing = casings.get(i);
            float p = progress.get(i), x = BELT_START + BELT_LENGTH * p;
            AmmoAssemblyRecipe recipe = casing.getRecipe(level);
            if (p >= AmmoAssemblerLogic.PRESS_POINT && recipe != null && !recipe.getItemOutputs().isEmpty()) {
                drawItem(recipe.getItemOutputs().get(0), x, BELT_TOP, ITEM_SCALE, pose, buffers, level, light, packedOverlay);
                continue;
            }
            if (casing.inputItems.isEmpty()) continue;
            ItemStack shell = casing.inputItems.get(0);
            drawItem(shell, x, BELT_TOP, ITEM_SCALE, pose, buffers, level, light, packedOverlay);
            if (p < AmmoAssemblerLogic.PLACE_POINT || recipe == null || recipe.hideProjectile) continue;
            ItemStack[] projectiles = recipe.projectile.getMatchingStacks();
            if (projectiles.length == 0) continue;
            TextureAtlasSprite shellSprite = spriteOf(shell, level);
            float mouth = BELT_TOP + ITEM_SCALE * (bottomOf(shellSprite) - mouthOf(shellSprite));
            pose.pushPose();
            pose.translate(0, 0, SEAT_FRONT);
            drawItem(projectiles[0], x, mouth, FEEDER_SCALE, pose, buffers, level, light, packedOverlay);
            pose.popPose();
        }
        pose.popPose();
    }

    // ---- Parts ----

    // How far down a head is, 0..1, from the nearest casing to its station
    private static float stroke(List<Float> progress, float station) {
        float nearest = 1;
        for (float p : progress) nearest = Math.min(nearest, Math.abs(p - station));
        return nearest < WINDOW ? Mth.cos(Mth.HALF_PI * nearest / WINDOW) : 0;
    }

    // One front screen at full brightness: the item and its count, or EMPTY
    private static void drawScreen(ItemStack stack, float cx, PoseStack pose, MultiBufferSource buffers, Level level,
                                   int overlay) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        if (stack.isEmpty()) {
            drawText(font, "EMPTY", cx, SCREEN_Y, SCREEN_EMPTY, pose, buffers);
            return;
        }
        pose.pushPose();
        pose.translate(cx + SCREEN_ICON_DX, SCREEN_Y, SCREEN_Z + SCREEN_ICON_SIZE * 0.05f);
        pose.mulPose(Axis.YP.rotationDegrees(180));    // face the front
        pose.scale(SCREEN_ICON_SIZE, SCREEN_ICON_SIZE, SCREEN_ICON_SIZE * 0.1f);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, overlay, pose,
                buffers, level, 0);
        pose.popPose();
        drawText(font, String.valueOf(stack.getCount()), cx + SCREEN_COUNT_DX, SCREEN_Y, SCREEN_TEXT, pose, buffers);
    }

    // Text centred on (x, y) on the screens' face, reading from the front
    private static void drawText(Font font, String text, float x, float y, int color, PoseStack pose,
                                 MultiBufferSource buffers) {
        pose.pushPose();
        pose.translate(x, y, SCREEN_Z + 0.002f);
        pose.scale(SCREEN_TEXT_SCALE, -SCREEN_TEXT_SCALE, SCREEN_TEXT_SCALE);
        font.drawInBatch(text, -font.width(text) / 2f, -font.lineHeight / 2f + 1, color, false, pose.last().pose(),
                buffers, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    // The lit lamp: its quads moved onto the lit tile, drawn at full brightness so it glows
    private static void drawLitLamp(PoseStack pose, MultiBufferSource buffers, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(LAMP);
        VertexConsumer out = buffers.getBuffer(RenderType.solid());
        int[] lights = {LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT, LightTexture.FULL_BRIGHT};
        float[] shade = {1, 1, 1, 1};
        for (BakedQuad quad : model.getQuads(null, null, RandomSource.create(42), ModelData.EMPTY, null)) {
            TextureAtlasSprite sprite = quad.getSprite();
            float shift = (sprite.getU1() - sprite.getU0()) * LAMP_TILE_SHIFT;
            int[] vertices = quad.getVertices().clone();
            for (int v = 0; v < 4; v++) {
                int u = v * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
                vertices[u] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[u]) + shift);
            }
            BakedQuad lit = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite, false);
            out.putBulkData(pose.last(), lit, shade, 1, 1, 1, lights, overlay, false);
        }
    }

    private static void drawPart(ResourceLocation name, float dx, float dy, PoseStack pose, MultiBufferSource buffers,
                                 Rotation rotation, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(name);
        // fetch the buffer each time: drawing an item in between ends it, and reusing it corrupts the frame
        VertexConsumer out = buffers.getBuffer(RenderType.solid());
        pose.pushPose();
        pose.translate(dx, dy, 0);
        RandomSource random = RandomSource.create(42);
        int[] lights = {light, light, light, light};
        for (BakedQuad quad : model.getQuads(null, null, random, ModelData.EMPTY, null)) {
            // shade by face direction, like the block model
            float shade = quad.isShade() ? shadeOf(rotation.rotate(quad.getDirection())) : 1;
            out.putBulkData(pose.last(), quad, new float[]{shade, shade, shade, shade}, 1, 1, 1, lights, overlay, false);
        }
        pose.popPose();
    }

    private static float shadeOf(Direction d) {
        return switch (d) {
            case UP -> 1.0f;
            case DOWN -> 0.5f;
            case NORTH, SOUTH -> 0.8f;
            default -> 0.6f;
        };
    }

    // ---- Items and sprites ----

    // A flat item facing the front of the machine, its lowest opaque pixel at height y
    private static void drawItem(ItemStack stack, float x, float y, float scale, PoseStack pose, MultiBufferSource buffers,
                                 Level level, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        float bottom = bottomOf(spriteOf(stack, level));
        pose.pushPose();
        pose.translate(x, y - scale * (0.5f - bottom), BELT_Z);
        pose.mulPose(Axis.YP.rotationDegrees(180));    // face the front
        pose.scale(scale, scale, scale);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, level, 0);
        pose.popPose();
    }

    private static TextureAtlasSprite spriteOf(ItemStack stack, Level level) {
        BakedModel model = FlatRoundRenderer.flatModel(stack);
        if (model == null) model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        return model.getParticleIcon(ModelData.EMPTY);
    }

    private static final Map<TextureAtlasSprite, Float> MOUTHS = new WeakHashMap<>();

    // Where a casing's mouth ends, as a fraction of the sprite's height from the top: the bottom of the dark band at
    // the top of its centre column
    private static float mouthOf(TextureAtlasSprite sprite) {
        return MOUTHS.computeIfAbsent(sprite, s -> {
            SpriteContents c = s.contents();
            int w = c.width(), h = c.height(), left = w, right = -1;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if (!c.isTransparent(0, x, y)) {
                        left = Math.min(left, x);
                        right = Math.max(right, x);
                    }
            if (right < 0) return 0f;
            int cx = (left + right) / 2, top = -1, brightest = 0;
            for (int y = 0; y < h; y++) {
                if (c.isTransparent(0, cx, y)) continue;
                if (top < 0) top = y;
                brightest = Math.max(brightest, luminance(c.getOriginalImage().getPixelRGBA(cx, y)));
            }
            if (top < 0) return 0f;
            int y = top;
            while (y < h && !c.isTransparent(0, cx, y)
                    && luminance(c.getOriginalImage().getPixelRGBA(cx, y)) < 0.6f * brightest) y++;
            return y / (float) h;
        });
    }

    private static int luminance(int abgr) {
        return ((abgr & 255) + (abgr >> 8 & 255) + (abgr >> 16 & 255)) / 3;
    }

    private static final Map<TextureAtlasSprite, Float> BOTTOMS = new WeakHashMap<>();

    // Where a sprite's lowest opaque pixel ends, as a fraction of its height from the top
    private static float bottomOf(TextureAtlasSprite sprite) {
        return BOTTOMS.computeIfAbsent(sprite, s -> {
            int w = s.contents().width(), h = s.contents().height();
            for (int y = h - 1; y >= 0; y--)
                for (int x = 0; x < w; x++)
                    if (!s.contents().isTransparent(0, x, y)) return (y + 1) / (float) h;
            return 1f;
        });
    }

    @Override
    public boolean shouldRenderOffScreen(MultiblockBlockEntityMaster<State> be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
