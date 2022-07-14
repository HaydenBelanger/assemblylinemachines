package me.haydenb.assemblylinemachines.crafting;

import java.util.*;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.FluidGeneratorTypes;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.TEFluidGenerator;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GeneratorFluidCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	public static final RecipeType<GeneratorFluidCrafting> GENFLUID_RECIPE = new RecipeType<GeneratorFluidCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:generator_fluid";
		}
	};
	
	public static final GeneratorFluidSerializer SERIALIZER = new GeneratorFluidSerializer();
	
	public final GeneratorFluidTypes fluidType;
	public final Fluid fluid;
	public final int powerPerUnit;
	public final float coolantStrength;
	
	
	
	private final ResourceLocation id;
	
	public GeneratorFluidCrafting(ResourceLocation id, GeneratorFluidTypes fluidType, Fluid fluid, int powerPerUnit, float coolantStrength) {
		this.fluidType = fluidType;
		this.id = id;
		this.fluid = fluid;
		this.powerPerUnit = powerPerUnit;
		this.coolantStrength = coolantStrength;
	}
	
	@Override
	public boolean matches(Container inv, Level level) {
		if(inv != null && fluidType != GeneratorFluidTypes.COOLANT && inv instanceof TEFluidGenerator generator) return this.fluidType.equivalentGenerator.equals(generator.type.get());
		return false;
	}

	@Override
	public ItemStack assemble(Container p_44001_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return GENFLUID_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(fluidType.jeiIngredient.get(), new FluidStack(fluid, 1000));
	}
	
	public static class GeneratorFluidSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GeneratorFluidCrafting>{

		@Override
		public GeneratorFluidCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
			if(f == null) throw new IllegalArgumentException("Could not find this fluid.");
			GeneratorFluidTypes type = GeneratorFluidTypes.valueOf(GsonHelper.getAsString(json, "generator"));
			int powerPerUnit = type != GeneratorFluidTypes.COOLANT ? GsonHelper.getAsInt(json, "fe") : 0;
			float coolantStrength = type == GeneratorFluidTypes.COOLANT ? GsonHelper.getAsFloat(json, "coolantstrength") : 0f;
			return new GeneratorFluidCrafting(recipeId, type, f, powerPerUnit, coolantStrength);
		}

		@Override
		public GeneratorFluidCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Fluid f = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			GeneratorFluidTypes type = buffer.readEnum(GeneratorFluidTypes.class);
			int powerPerUnit = buffer.readInt();
			float coolantStrength = buffer.readFloat();
			return new GeneratorFluidCrafting(recipeId, type, f, powerPerUnit, coolantStrength);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, GeneratorFluidCrafting recipe) {
			buffer.writeResourceLocation(recipe.fluid.getRegistryName());
			buffer.writeEnum(recipe.fluidType);
			buffer.writeInt(recipe.powerPerUnit);
			buffer.writeFloat(recipe.coolantStrength);
			
		}
		
	}
	
	public static enum GeneratorFluidTypes{
		GEOTHERMAL(FluidGeneratorTypes.GEOTHERMAL, Lazy.of(() -> Ingredient.of(Registry.getBlock("geothermal_generator")))), COMBUSTION(FluidGeneratorTypes.COMBUSTION, Lazy.of(() -> Ingredient.of(Registry.getBlock("combustion_generator")))),
		COOLANT(null, Lazy.of(() ->{
			ArrayList<ItemStack> ingredients = new ArrayList<>();
			for(GeneratorFluidTypes type : GeneratorFluidTypes.values()) {
				if(!type.toString().equalsIgnoreCase("COOLANT")) ingredients.addAll(Arrays.asList(type.jeiIngredient.get().getItems()));
			}
			return Ingredient.of(ingredients.stream());
		}));
		
		public final FluidGeneratorTypes equivalentGenerator;
		private final Lazy<Ingredient> jeiIngredient;
		
		GeneratorFluidTypes(FluidGeneratorTypes equivalentGenerator, Lazy<Ingredient> jeiIngredient){
			this.equivalentGenerator = equivalentGenerator;
			this.jeiIngredient = jeiIngredient;
		}
	}

}
