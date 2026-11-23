package dev.immersive_munitions.client;

import dev.immersive_munitions.ImmersiveMunitions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/** Registers the optional TaCZ ammunition icon pack bundled inside this mod. */
@Mod.EventBusSubscriber(modid = ImmersiveMunitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BundledAmmoResourcePack {
    private static final String PACK_PATH = "resourcepacks/immersive_munitions_ammo";

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        var modFile = ModList.get().getModFileById(ImmersiveMunitions.MODID);
        if (modFile == null) return;

        var resourcePath = modFile.getFile().findResource(PACK_PATH);
        Pack pack = Pack.readMetaAndCreate(
                "builtin/immersive_munitions_ammo",
                Component.literal("Immersive Munitions — TaCZ Ammunition Icons"),
                false,
                path -> new PathPackResources(path, resourcePath, true),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN
        );
        if (pack != null) {
            event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
        }
    }

    private BundledAmmoResourcePack() {}
}
