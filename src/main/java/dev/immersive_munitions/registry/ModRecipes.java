package dev.immersive_munitions.registry;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import dev.immersive_munitions.ImmersiveMunitions;
import dev.immersive_munitions.crafting.AmmoAssemblyRecipe;
import dev.immersive_munitions.crafting.AmmoAssemblySerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The Ammunition Assembler's recipe type and serializer. */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ImmersiveMunitions.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ImmersiveMunitions.MODID);

    private static final RegistryObject<RecipeType<AmmoAssemblyRecipe>> AMMO_ASSEMBLY_TYPE = TYPES.register("ammo_assembly",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(ImmersiveMunitions.MODID, "ammo_assembly")));

    // IE's recipe and cache classes want the type paired with its class
    public static final TypeWithClass<AmmoAssemblyRecipe> AMMO_ASSEMBLY = new TypeWithClass<>(AMMO_ASSEMBLY_TYPE, AmmoAssemblyRecipe.class);

    public static final RegistryObject<AmmoAssemblySerializer> AMMO_ASSEMBLY_SERIALIZER = SERIALIZERS.register("ammo_assembly", AmmoAssemblySerializer::new);

    private ModRecipes() {}
}
