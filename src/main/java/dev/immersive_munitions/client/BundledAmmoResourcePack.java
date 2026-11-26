package dev.immersive_munitions.client;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Lists the TaCZ ammo icon pack (shipped inside the jar) in the Resource Packs screen. Off by default. */
@EventBusSubscriber(modid = ImmersiveMunitions.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class BundledAmmoResourcePack {
    private static final ResourceLocation PACK = ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "resourcepacks/immersive_munitions_ammo");
    // the id NeoForge gives a pack found this way
    public static final String PACK_ID = "mod/" + PACK;

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(PACK, PackType.CLIENT_RESOURCES, Component.literal("Immersive Munitions — TaCZ Ammunition Icons"),
                PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    private BundledAmmoResourcePack() {}
}
