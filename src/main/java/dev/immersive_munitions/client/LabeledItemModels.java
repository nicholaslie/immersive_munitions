package dev.immersive_munitions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

/**
 * Caliber labels in inventories only: casings, projectiles and molds use their plain model everywhere except the GUI,
 * where the labeled one from models/item/labeled/ is swapped in (both made by art/labels/Labels.java).
 */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LabeledItemModels {
    private static final List<String> PREFIXES = List.of("empty_casing_", "projectile_", "mold_casing_");

    private static List<ResourceLocation> items() {
        return ForgeRegistries.ITEMS.getKeys().stream()
                .filter(id -> id.getNamespace().equals(ImmersiveMunitions.MODID))
                .filter(id -> PREFIXES.stream().anyMatch(id.getPath()::startsWith))
                .toList();
    }

    private static ResourceLocation labeled(ResourceLocation item) {
        return ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/labeled/" + item.getPath());
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        items().forEach(item -> event.register(labeled(item)));
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = models.get(ModelResourceLocation.vanilla("builtin/missing", "missing"));
        for (ResourceLocation item : items()) {
            ModelResourceLocation key = new ModelResourceLocation(item, "inventory");
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
