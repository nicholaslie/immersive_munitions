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
 * Draws the Ammunition Assembler's moving parts: casings riding the belt, and the three station heads stroking as
 * each casing passes under them. The static machine is the block model; the moving parts are the
 * ammo_assembler_[group] models exported by art/assembler/texture.py (see DYNAMIC there).
 *
 * Coordinates below are in the model's frame (block units, origin at the master block's corner): the belt runs
 * along x from -1 (input) to 2 at z = 1.5, its top at y = 0.125, and the stations sit at x = -0.5, 0.5 and 1.5.
 */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AmmoAssemblerRenderer implements BlockEntityRenderer<MultiblockBlockEntityMaster<State>> {
    private static final ResourceLocation POWDER = part("powder"), WHEEL = part("wheel"), JAWS = part("jaws"),
            PRESS = part("press"), LAMP = part("lamp");
    // The lamp's lit art is the next tile right on the machine's 128px sheet of 16px tiles
    private static final float LAMP_TILE_SHIFT = 16 / 128f;
    // Front screens (model.py front_screens): 0.64 x 0.46, centred at x = -0.5 and 0.5, y = 1.63, face at z = 1.99.
    // The icon sits left of centre and the count right; text is about 0.1 tall, in display green (EMPTY in red)
    private static final float SCREEN_POWDER_X = -0.5f, SCREEN_FEEDER_X = 0.5f, SCREEN_Y = 1.63f, SCREEN_Z = 1.99f;
    private static final float SCREEN_ICON_DX = -0.12f, SCREEN_ICON_SIZE = 0.2f, SCREEN_COUNT_DX = 0.1f;
    private static final float SCREEN_TEXT_SCALE = 0.012f;
    private static final int SCREEN_TEXT = 0xFF5CFF7A, SCREEN_EMPTY = 0xFFFF4A3A;

    private static final float BELT_START = -1, BELT_LENGTH = 3, BELT_Z = 1.5f, BELT_TOP = 0.125f;
    // Casings and rounds are drawn as their (unlabeled) item sprites at this size
    private static final float ITEM_SCALE = 0.3125f;
    // A head strokes while a casing is within this much of a cycle of its station (about 3 ticks either side)
    private static final float WINDOW = 0.05f;
    private static final float POWDER_STROKE = 0.06f, FEED_STROKE = 0.06f, PRESS_STROKE = 0.07f;
    // The handwheel turns a quarter turn per casing filled, about its own centre
    private static final float WHEEL_X = -0.5f, WHEEL_Y = 1.03f;
    // The projectile in the feeder's jaws: centred between them, its tip end low in the jaws (bottom of the jaws is 0.47)
    // Smaller than ITEM_SCALE: projectile sprites fill more of their icon than casings do
    private static final float FEEDER_X = 0.5f, FEEDER_BOTTOM = 0.45f, FEEDER_SCALE = 0.2f;
    // After the feeder the projectile stands in the casing's mouth, this far toward the machine's front: its front
    // face just ahead of the casing's (half thicknesses: casing 0.0098, projectile 0.0063), its back inside the casing
    private static final float SEAT_FRONT = 0.006f;

    private static ResourceLocation part(String group) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "block/ammo_assembler_" + group);
    }

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

    @Override
    public void render(MultiblockBlockEntityMaster<State> be, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;
        IMultiblockBEHelperMaster<State> helper = be.getHelper();
        State state = helper.getState();
        Direction facing = be.getBlockState().getValue(IEProperties.FACING_HORIZONTAL);
        // The master sits in the solid back row; light the moving parts from the open belt block instead
        BlockPos belt = helper.getContext().getLevel().toAbsolute(new BlockPos(1, 1, 1));
        int light = LevelRenderer.getLightColor(level, belt);

        // Where each casing is along the belt, 0..1
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

        pose.pushPose();
        // Same rotation as the blockstate gives the static model
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

        float fill = stroke(progress, AmmoAssemblerLogic.FILL_POINT);
        float feed = stroke(progress, AmmoAssemblerLogic.PLACE_POINT);
        float press = stroke(progress, AmmoAssemblerLogic.PRESS_POINT);

        drawPart(POWDER, 0, -POWDER_STROKE * fill, pose, buffers, rotation, light, packedOverlay);
        drawPart(JAWS, 0, -FEED_STROKE * feed, pose, buffers, rotation, light, packedOverlay);
        // The jaws hold one of whatever is in the projectile hopper, drawn like the casings on the belt
        ItemStack projectile = state.shownProjectiles();
        if (!projectile.isEmpty())
            drawItem(projectile, FEEDER_X, FEEDER_BOTTOM - FEED_STROKE * feed, FEEDER_SCALE, pose, buffers, level,
                    light, packedOverlay);
        drawPart(PRESS, 0, -PRESS_STROKE * press, pose, buffers, rotation, light, packedOverlay);
        if (state.isActive()) drawLitLamp(pose, buffers, packedOverlay);
        else drawPart(LAMP, 0, 0, pose, buffers, rotation, light, packedOverlay);

        // Front screens: what each roof hopper holds, gunpowder over the powder station, projectiles over the feeder
        drawScreen(state.shownGunpowder(), SCREEN_POWDER_X, pose, buffers, level, packedOverlay);
        drawScreen(state.shownProjectiles(), SCREEN_FEEDER_X, pose, buffers, level, packedOverlay);

        // Handwheel: a quarter turn for every casing filled, easing through the one passing under it now
        // (a casing counts as filled halfway through its turn, so take that quarter back off while it finishes)
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

        // Casings: the bare casing, then from the feeder on with its projectile standing in the mouth, then from the
        // press on the finished round
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
            if (p < AmmoAssemblerLogic.PLACE_POINT || recipe == null) continue;
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

    // 0..1: how far down a head is, from the nearest casing to its station
    private static float stroke(List<Float> progress, float station) {
        float nearest = 1;
        for (float p : progress) nearest = Math.min(nearest, Math.abs(p - station));
        return nearest < WINDOW ? Mth.cos(Mth.HALF_PI * nearest / WINDOW) : 0;
    }

    // One front screen: the hopper's item as an icon with its count beside it, or EMPTY; lit like a display
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
        pose.mulPose(Axis.YP.rotationDegrees(180));    // the fixed item transform turns it to face -z; face the front
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

    // The lamp, lit while the machine works: its quads moved onto the lit tile, at full brightness and unshaded, so it glows
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
        // Take the solid buffer fresh each time: RenderType.solid() isn't one of the buffer source's fixed buffers,
        // so drawing an item in between ends it, and writing to the old one corrupts the frame
        VertexConsumer out = buffers.getBuffer(RenderType.solid());
        pose.pushPose();
        pose.translate(dx, dy, 0);
        RandomSource random = RandomSource.create(42);
        int[] lights = {light, light, light, light};
        for (BakedQuad quad : model.getQuads(null, null, random, ModelData.EMPTY, null)) {
            // The block model is shaded by face direction in the world; do the same so the parts match it
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

    // A flat item over the belt, facing the front of the machine, its lowest opaque pixel at height y
    private static void drawItem(ItemStack stack, float x, float y, float scale, PoseStack pose, MultiBufferSource buffers,
                                 Level level, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        float bottom = bottomOf(spriteOf(stack, level));
        pose.pushPose();
        pose.translate(x, y - scale * (0.5f - bottom), BELT_Z);
        pose.mulPose(Axis.YP.rotationDegrees(180));    // the fixed item transform turns it to face -z; face the front
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

    // A casing's open mouth is the dark band at the top of its sprite. Fraction of the sprite's height (from the
    // top) down to the bottom of that band, along the casing's centre column, 0..1
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

    // Fraction of the sprite's height (from the top) below its lowest opaque pixel, 0..1
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
