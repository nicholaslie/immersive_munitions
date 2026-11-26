package dev.immersive_munitions.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import dev.immersive_munitions.registry.ModMultiblocks;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Reads and syncs Ammunition Assembler recipes:
 * <pre>
 * {
 *   "type": "immersive_munitions:ammo_assembly",
 *   "casing": {"item": "immersive_munitions:empty_casing_9mm"},
 *   "projectile": {"item": "immersive_munitions:projectile_9mm"},
 *   "gunpowder": {"tag": "c:gunpowders"},
 *   "ammo": "9mm",                  the TaCZ ammo id (bare: TaCZ's; or "immersive_armorer:20mm")
 *   "count": 1,                     optional
 *   "time": 60, "energy": 1200,     optional
 *   "hide_projectile": true         optional: don't draw the projectile on the casing (12g)
 * }
 * </pre>
 */
public class AmmoAssemblySerializer extends IERecipeSerializer<AmmoAssemblyRecipe> {
    // Ticks for a casing to cross the belt, and energy per round
    private static final int DEFAULT_TIME = 60;
    private static final int DEFAULT_ENERGY = 1200;

    public static final DualMapCodec<RegistryFriendlyByteBuf, AmmoAssemblyRecipe> CODECS = DualCompositeMapCodecs.composite(
            IngredientWithSize.CODECS.fieldOf("casing"), r -> r.casing,
            IngredientWithSize.CODECS.fieldOf("projectile"), r -> r.projectile,
            IngredientWithSize.CODECS.fieldOf("gunpowder"), r -> r.gunpowder,
            DualCodecs.STRING.fieldOf("ammo"), r -> r.ammoId,
            DualCodecs.INT.optionalFieldOf("count", 1), r -> r.count,
            DualCodecs.INT.optionalFieldOf("time", DEFAULT_TIME), MultiblockRecipe::getBaseTime,
            DualCodecs.INT.optionalFieldOf("energy", DEFAULT_ENERGY), MultiblockRecipe::getBaseEnergy,
            DualCodecs.BOOL.optionalFieldOf("hide_projectile", false), r -> r.hideProjectile,
            AmmoAssemblyRecipe::new
    );

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, AmmoAssemblyRecipe> codecs() {
        return CODECS;
    }

    @Override
    public ItemStack getIcon() {
        return ModMultiblocks.AMMO_ASSEMBLER.iconStack();
    }
}
