package dev.immersive_munitions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IAmmo;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * With our icon pack on, TaCZ rounds in the world (dropped, held, on conveyors) are drawn as flat items with our art
 * instead of TaCZ's 3D model. Inventory icons, and everything with the pack off, stay TaCZ's.
 */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FlatRoundRenderer extends BlockEntityWithoutLevelRenderer {
    private static final List<String> ROUNDS = List.of(
            "12g", "22wmr", "300blk", "308", "30_06", "338", "357mag", "40mm", "45_70", "45acp",
            "46x30", "500mag", "50ae", "50bmg", "545x39", "556x45", "57x28", "58x42", "68x51fury",
            "6x43", "762x25", "762x39", "762x54", "792x57", "9mm", "rpg_rocket");

    private final IClientItemExtensions tacz;

    private FlatRoundRenderer(IClientItemExtensions tacz) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.tacz = tacz;
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        ROUNDS.forEach(id -> event.register(model(id)));
    }

    // TaCZ's ammo item gets its renderer from initializeClient; wrap it so we can step in for world rendering
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item ammo = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("tacz", "ammo"));
            if (ammo == null || !(ammo.getRenderPropertiesInternal() instanceof IClientItemExtensions original)) return;

            FlatRoundRenderer renderer = new FlatRoundRenderer(original);
            IClientItemExtensions wrapped = new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
                }
            };
            ObfuscationReflectionHelper.setPrivateValue(Item.class, ammo, wrapped, "renderProperties");
        });
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        BakedModel flat = context == ItemDisplayContext.GUI ? null : flatModel(stack);
        if (flat == null) {
            tacz.getCustomRenderer().renderByItem(stack, context, pose, buffers, light, overlay);
            return;
        }

        pose.pushPose();
        // we're inside TaCZ's item model, shifted to the corner; move back to the centre
        pose.translate(0.5, 0.5, 0.5);
        if (context.firstPerson()) pose.scale(2, 2, 2);   // TaCZ's model halves first-person items
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().getItemRenderer().render(stack, context, leftHand, pose, buffers, light, overlay, flat);
        pose.popPose();
    }

    /** Our flat model for a TaCZ round, or null when the icon pack is off or the round isn't one of ours. */
    public static BakedModel flatModel(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.getResourcePackRepository().getSelectedIds().contains(BundledAmmoResourcePack.PACK_ID)) return null;
        if (!(stack.getItem() instanceof IAmmo ammo)) return null;

        ResourceLocation id = ammo.getAmmoId(stack);
        if (!"tacz".equals(id.getNamespace()) || !ROUNDS.contains(id.getPath())) return null;

        BakedModel model = mc.getModelManager().getModel(model(id.getPath()));
        return model == mc.getModelManager().getMissingModel() ? null : model;
    }

    private static ResourceLocation model(String round) {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "item/tacz_round/" + round);
    }
}
