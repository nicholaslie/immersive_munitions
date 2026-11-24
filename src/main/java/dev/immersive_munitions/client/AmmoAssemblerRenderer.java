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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
            ROUND = part("round"), PRESS = part("press");

    private static final float BELT_START = -1, BELT_LENGTH = 3, BELT_Z = 1.5f, BELT_TOP = 0.125f;
    // Casings and rounds are drawn as their (unlabeled) item sprites at this size
    private static final float ITEM_SCALE = 0.3125f;
    // A head strokes while a casing is within this much of a cycle of its station (about 3 ticks either side)
    private static final float WINDOW = 0.05f;
    private static final float POWDER_STROKE = 0.06f, FEED_STROKE = 0.06f, PRESS_STROKE = 0.07f;
    // The handwheel turns a quarter turn per casing filled, about its own centre
    private static final float WHEEL_X = -0.5f, WHEEL_Y = 1.03f;

    private static ResourceLocation part(String group) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "block/ammo_assembler_" + group);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation model : List.of(POWDER, WHEEL, JAWS, ROUND, PRESS)) event.register(model);
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

        VertexConsumer solid = buffers.getBuffer(RenderType.solid());
        float fill = stroke(progress, AmmoAssemblerLogic.FILL_POINT);
        float feed = stroke(progress, AmmoAssemblerLogic.PLACE_POINT);
        float press = stroke(progress, AmmoAssemblerLogic.PRESS_POINT);

        drawPart(POWDER, 0, -POWDER_STROKE * fill, pose, solid, rotation, light, packedOverlay);
        drawPart(JAWS, 0, -FEED_STROKE * feed, pose, solid, rotation, light, packedOverlay);
        if (state.hasProjectiles()) drawPart(ROUND, 0, -FEED_STROKE * feed, pose, solid, rotation, light, packedOverlay);
        drawPart(PRESS, 0, -PRESS_STROKE * press, pose, solid, rotation, light, packedOverlay);

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
        drawPart(WHEEL, 0, 0, pose, solid, rotation, light, packedOverlay);
        pose.popPose();

        // Casings: the casing until the feeder places its projectile, then the finished round
        for (int i = 0; i < casings.size(); i++) {
            AssemblyProcess casing = casings.get(i);
            float p = progress.get(i);
            ItemStack stack = displayStack(casing, level, p);
            if (stack.isEmpty()) continue;
            drawItem(stack, BELT_START + BELT_LENGTH * p, pose, buffers, level, light, packedOverlay);
        }
        pose.popPose();
    }

    // 0..1: how far down a head is, from the nearest casing to its station
    private static float stroke(List<Float> progress, float station) {
        float nearest = 1;
        for (float p : progress) nearest = Math.min(nearest, Math.abs(p - station));
        return nearest < WINDOW ? Mth.cos(Mth.HALF_PI * nearest / WINDOW) : 0;
    }

    private static ItemStack displayStack(AssemblyProcess casing, Level level, float p) {
        if (p >= AmmoAssemblerLogic.PLACE_POINT) {
            AmmoAssemblyRecipe recipe = casing.getRecipe(level);
            if (recipe != null && !recipe.getItemOutputs().isEmpty()) return recipe.getItemOutputs().get(0);
        }
        return casing.inputItems.isEmpty() ? ItemStack.EMPTY : casing.inputItems.get(0);
    }

    private static void drawPart(ResourceLocation name, float dx, float dy, PoseStack pose, VertexConsumer out,
                                 Rotation rotation, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(name);
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

    // A flat item standing on the belt, facing the front of the machine, its lowest opaque pixel on the belt
    private static void drawItem(ItemStack stack, float x, PoseStack pose, MultiBufferSource buffers, Level level,
                                 int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = FlatRoundRenderer.flatModel(stack);
        if (model == null) model = mc.getItemRenderer().getModel(stack, level, null, 0);
        float bottom = bottomOf(model.getParticleIcon(ModelData.EMPTY));
        pose.pushPose();
        pose.translate(x, BELT_TOP - ITEM_SCALE * (0.5f - bottom), BELT_Z);
        pose.mulPose(Axis.YP.rotationDegrees(180));    // the fixed item transform turns it to face -z; face the front
        pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, level, 0);
        pose.popPose();
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
