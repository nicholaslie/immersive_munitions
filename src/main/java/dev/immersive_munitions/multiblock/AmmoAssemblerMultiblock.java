package dev.immersive_munitions.multiblock;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.registry.ModMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * The blueprint: data/immersive_munitions/structures/multiblocks/ammo_assembler.nbt.
 * 3 long (x, the belt runs toward +x), 3 tall, 2 deep (z=1 is the belt side). Hammer the middle conveyor.
 */
public class AmmoAssemblerMultiblock extends IETemplateMultiblock {
    // Must be declared before INSTANCE: static fields initialize top to bottom
    private static final BlockPos MIDDLE_CONVEYOR = new BlockPos(1, 1, 1);

    public static final AmmoAssemblerMultiblock INSTANCE = new AmmoAssemblerMultiblock();

    private AmmoAssemblerMultiblock() {
        super(ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "multiblocks/ammo_assembler"),
                MIDDLE_CONVEYOR, MIDDLE_CONVEYOR, new BlockPos(3, 3, 2),
                ModMultiblocks.AMMO_ASSEMBLER);
    }

    @Override
    public float getManualScale() {
        return 12;
    }
}
