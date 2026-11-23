package dev.immersive_munitions.client;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/** Lists the TaCZ ammo icon pack (shipped inside the jar) in the Resource Packs screen. Off by default. */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BundledAmmoResourcePack {
    public static final String PACK_ID = "builtin/immersive_munitions_ammo";
    private static final String PACK_PATH = "resourcepacks/immersive_munitions_ammo";

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        var modFile = ModList.get().getModFileById(ImmersiveMunitions.MODID);
        if (modFile == null) return;

        var packRoot = modFile.getFile().findResource(PACK_PATH);
        Pack pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal("Immersive Munitions — TaCZ Ammunition Icons"),
                false,
                id -> new PathPackResources(id, packRoot, true),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN);

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }

    private BundledAmmoResourcePack() {}
}
