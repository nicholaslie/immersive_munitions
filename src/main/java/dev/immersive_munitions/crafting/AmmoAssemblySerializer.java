package dev.immersive_munitions.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import dev.immersive_munitions.registry.ModMultiblocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

/**
 * Reads and syncs Ammunition Assembler recipes:
 * <pre>
 * {
 *   "type": "immersive_munitions:ammo_assembly",
 *   "casing": {"item": "immersive_munitions:empty_casing_9mm"},
 *   "projectile": {"item": "immersive_munitions:projectile_9mm"},
 *   "gunpowder": {"tag": "forge:gunpowder"},
 *   "result": {"ammo": "9mm", "count": 1},
 *   "time": 60, "energy": 1200,     optional
 *   "hide_projectile": true         optional: don't draw the projectile on the casing (12g)
 * }
 * </pre>
 */
public class AmmoAssemblySerializer extends IERecipeSerializer<AmmoAssemblyRecipe> {
    // Ticks for a casing to cross the belt, and energy per round
    private static final int DEFAULT_TIME = 60;
    private static final int DEFAULT_ENERGY = 1200;

    @Override
    public ItemStack getIcon() {
        return ModMultiblocks.AMMO_ASSEMBLER.iconStack();
    }

    @Override
    public AmmoAssemblyRecipe readFromJson(ResourceLocation id, JsonObject json, IContext context) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");
        return new AmmoAssemblyRecipe(id,
                IngredientWithSize.deserialize(json.get("casing")),
                IngredientWithSize.deserialize(json.get("projectile")),
                IngredientWithSize.deserialize(json.get("gunpowder")),
                GsonHelper.getAsString(result, "ammo"),
                GsonHelper.getAsInt(result, "count", 1),
                GsonHelper.getAsInt(json, "time", DEFAULT_TIME),
                GsonHelper.getAsInt(json, "energy", DEFAULT_ENERGY),
                GsonHelper.getAsBoolean(json, "hide_projectile", false));
    }

    @Override
    public AmmoAssemblyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        return new AmmoAssemblyRecipe(id,
                IngredientWithSize.read(buf),
                IngredientWithSize.read(buf),
                IngredientWithSize.read(buf),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AmmoAssemblyRecipe recipe) {
        recipe.casing.write(buf);
        recipe.projectile.write(buf);
        recipe.gunpowder.write(buf);
        buf.writeUtf(recipe.ammoId);
        buf.writeVarInt(recipe.count);
        buf.writeVarInt(recipe.getTotalProcessTime());
        buf.writeVarInt(recipe.getTotalProcessEnergy());
        buf.writeBoolean(recipe.hideProjectile);
    }
}
