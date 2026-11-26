package dev.immersive_munitions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Map;

/**
 * Caliber labels in inventories only: casings, projectiles and molds use their plain model everywhere except the GUI,
 * where the labeled one from models/item/labeled/ is swapped in (both made by art/labels/Labels.java).
 */
@EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LabeledItemModels {
    private static final List<String> PREFIXES = List.of("empty_casing_", "projectile_", "mold_casing_");

    private static List<ResourceLocation> items() {
        return BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(ImmersiveMunitions.MODID))
                .filter(id -> PREFIXES.stream().anyMatch(id.getPath()::startsWith))
                .toList();
    }

    private static ModelResourceLocation labeled(ResourceLocation item) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/labeled/" + item.getPath()));
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        items().forEach(item -> event.register(labeled(item)));
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = models.get(ModelBakery.MISSING_MODEL_VARIANT);
        for (ResourceLocation item : items()) {
            ModelResourceLocation key = ModelResourceLocation.inventory(item);
            BakedModel plain = models.get(key);
            BakedModel label = models.get(labeled(item));
            if (plain == null || label == null || label == missing) continue;
            models.put(key, new GuiSwap(plain, label));
        }
    }

    private static final class GuiSwap extends BakedModelWrapper<BakedModel> {
        private final BakedModel gui;

        GuiSwap(BakedModel plain, BakedModel gui) {
            super(plain);
            this.gui = gui;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            return context == ItemDisplayContext.GUI
                    ? gui.applyTransform(context, pose, leftHand)
                    : super.applyTransform(context, pose, leftHand);
        }
    }

    private LabeledItemModels() {}
}
