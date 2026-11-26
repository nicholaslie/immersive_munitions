package dev.immersive_munitions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IAmmo;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.compat.ArmorerCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.ClientExtensionsManager;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Map;

/**
 * With our icon pack on, TaCZ rounds in the world (dropped, held, on conveyors) are drawn as flat items with our art
 * instead of TaCZ's 3D model. Inventory icons, and everything with the pack off, stay TaCZ's.
 */
@EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class FlatRoundRenderer extends BlockEntityWithoutLevelRenderer {
    private static final List<String> ROUNDS = List.of(
            "12g", "22wmr", "300blk", "308", "30_06", "338", "357mag", "40mm", "45_70", "45acp",
            "46x30", "500mag", "50ae", "50bmg", "545x39", "556x45", "57x28", "58x42", "68x51fury",
            "6x43", "762x25", "762x39", "762x54", "792x57", "9mm", "rpg_rocket");
    // the Immersive Armorer gun pack's ammo, in its own namespace (the models share the tacz_round folder; no ids clash)
    private static final List<String> ARMORER_ROUNDS = List.of(
            "454_casul", "ap_ammo", "20mm", "cut_steel_rod", "burst_capacitor", "stream_capacitor", "high_power_capacitor", "chemical");

    private final IClientItemExtensions tacz;

    private FlatRoundRenderer(IClientItemExtensions tacz) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.tacz = tacz;
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        ROUNDS.forEach(id -> event.register(model(id)));
        ARMORER_ROUNDS.forEach(id -> event.register(model(id)));
    }

    // TaCZ registers its ammo item's renderer as a client extension; swap in a wrapper so we can step in for world
    // rendering. NeoForge keeps the extensions in a package-private map, hence the reflection
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item ammo = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tacz", "ammo"));
            IClientItemExtensions original = IClientItemExtensions.of(ammo);
            if (original == IClientItemExtensions.DEFAULT) return;

            FlatRoundRenderer renderer = new FlatRoundRenderer(original);
            IClientItemExtensions wrapped = new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return renderer;
                }
            };
            Map<Item, IClientItemExtensions> extensions =
                    ObfuscationReflectionHelper.getPrivateValue(ClientExtensionsManager.class, null, "ITEM_EXTENSIONS");
            if (extensions != null) extensions.put(ammo, wrapped);
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
        boolean ours = switch (id.getNamespace()) {
            case "tacz" -> ROUNDS.contains(id.getPath());
            case ArmorerCompat.NAMESPACE -> ARMORER_ROUNDS.contains(id.getPath());
            default -> false;
        };
        if (!ours) return null;

        BakedModel model = mc.getModelManager().getModel(model(id.getPath()));
        return model == mc.getModelManager().getMissingModel() ? null : model;
    }

    private static ModelResourceLocation model(String round) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "item/tacz_round/" + round));
    }
}
