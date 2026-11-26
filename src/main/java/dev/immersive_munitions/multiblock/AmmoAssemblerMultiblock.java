package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.registry.ModMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * The Ammunition Assembler's structure (data/immersive_munitions/structures/multiblocks/ammo_assembler.nbt): 3 long,
 * 3 tall, 2 deep, formed by hammering the redstone engineering block (front middle, bottom layer). Not mirrorable.
 */
public class AmmoAssemblerMultiblock extends IETemplateMultiblock {
    // The master sits in the back middle like IE's bottling machine. Declared before INSTANCE, which uses them
    private static final BlockPos MASTER = new BlockPos(1, 1, 0);
    private static final BlockPos TRIGGER = new BlockPos(1, 0, 1);   // the redstone engineering block: easy to spot

    public static final AmmoAssemblerMultiblock INSTANCE = new AmmoAssemblerMultiblock();

    private AmmoAssemblerMultiblock() {
        super(ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "multiblocks/ammo_assembler"),
                MASTER, TRIGGER, new BlockPos(3, 3, 2),
                ModMultiblocks.AMMO_ASSEMBLER);
    }

    @Override
    public float getManualScale() {
        return 12;
    }
}
