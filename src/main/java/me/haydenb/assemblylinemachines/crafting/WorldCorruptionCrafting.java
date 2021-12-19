package me.haydenb.assemblylinemachines.crafting;

import java.util.*;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.block.fluidutility.BlockCorruptingBasin.TECorruptingBasin;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class WorldCorruptionCrafting implements Recipe<Container>, IRecipeCategoryBuilder {

	public static final RecipeType<WorldCorruptionCrafting> WORLD_CORRUPTION_RECIPE = new TypeWorldCorruptionCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Ingredient input;
	private final Fluid inputFluid;
	
	private final Block output;
	private final Fluid outputFluid;
	
	private final ResourceLocation id;
	
	public WorldCorruptionCrafting(ResourceLocation id, Ingredient input, Fluid inputFluid, ItemStack output, Fluid outputFluid) {
		this.id = id;
		this.input = input;
		this.output = output.isEmpty() ? Blocks.AIR : ((BlockItem) output.getItem()).getBlock();
		this.inputFluid = inputFluid;
		this.outputFluid = outputFluid;
	}
	
	@Override
	public boolean matches(Container container, Level level) {
		if(container == null) return true;
		if(container instanceof TECorruptingBasin) {
			return input.test(container.getItem(2));
		}
		return false;
	}

	@Override
	public ItemStack assemble(Container p_44001_) {
		return output.asItem().getDefaultInstance().copy();
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output.asItem().getDefaultInstance();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return WORLD_CORRUPTION_RECIPE;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		List<Ingredient> list = new ArrayList<>();
		if(!input.isEmpty()) {
			list.add(input);
		}
		list.add(Ingredient.of(Registry.getItem("entropy_reactor_block"), Registry.getItem("entropy_reactor_core"), Registry.getItem("corrupting_basin")));
		return list;
	}
	
	@Override
	public List<FluidStack> getJEIFluidInputs() {
		return !inputFluid.equals(Fluids.EMPTY) ? List.of(new FluidStack(inputFluid, 1000)) : null;
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return !output.equals(Blocks.AIR) ? List.of(output.asItem().getDefaultInstance()) : null;
	}
	
	@Override
	public List<FluidStack> getJEIFluidOutputs() {
		return !outputFluid.equals(Fluids.EMPTY) ? List.of(new FluidStack(outputFluid, 1000)) : null;
	}
	
	@Override
	public void setupSlots(IRecipeLayout layout, IGuiHelper helper, Optional<IIngredientRenderer<FluidStack>> renderer) {
		if(!input.isEmpty()) {
			layout.getItemStacks().init(0, false, 21, 4);
			layout.getItemStacks().init(1, false, 0, 4);
			layout.getItemStacks().init(2, false, 68, 4);
		}else if(!inputFluid.equals(Fluids.EMPTY)){
			layout.getItemStacks().init(0, false, 0, 4);
			layout.getFluidStacks().init(0, false, renderer.get(), 21, 4, 18, 18, 1, 1);
			layout.getFluidStacks().init(1, false, renderer.get(), 68, 4, 18, 18, 1, 1);
		}else {
			throw new IllegalArgumentException("Item Input and Fluid Input are both non-present!");
		}
	}
	
	
	public Optional<Block> testBlock(Block test){
		if(input.isEmpty()) return Optional.empty();
		return input.test(test.asItem().getDefaultInstance()) ? Optional.of(output) : Optional.empty();
	}
	
	public Optional<Fluid> testFluid(Fluid test){
		if(inputFluid.equals(Fluids.EMPTY)) return Optional.empty();
		return inputFluid.equals(test) ? Optional.of(outputFluid) : Optional.empty();
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<WorldCorruptionCrafting>{

		@Override
		public WorldCorruptionCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient input = GsonHelper.isValidNode(json, "input") ? Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")) : Ingredient.EMPTY;
			Fluid inputFluid = GsonHelper.isValidNode(json, "fluidInput") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluidInput"))) : Fluids.EMPTY;
			ItemStack output = GsonHelper.isValidNode(json, "output") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output")) : ItemStack.EMPTY;
			Fluid outputFluid = GsonHelper.isValidNode(json, "fluidOutput") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluidOutput"))) : Fluids.EMPTY;
			
			if(input.isEmpty() && inputFluid.equals(Fluids.EMPTY)) {
				throw new IllegalArgumentException("One of input or inputFluid must be set.");
			}else if(output.isEmpty() && outputFluid.equals(Fluids.EMPTY)) {
				throw new IllegalArgumentException("One of output or outputFluid must be set.");
			}else if(!input.isEmpty() && output.isEmpty()) {
				throw new IllegalArgumentException("If input is set, output must be set as well.");
			}else if(!inputFluid.equals(Fluids.EMPTY) && outputFluid.equals(Fluids.EMPTY)) {
				throw new IllegalArgumentException("If fluidInput is set, fluidOutput must be set as well.");
			}else if((!input.isEmpty() && !inputFluid.equals(Fluids.EMPTY)) || (!output.isEmpty() && !outputFluid.equals(Fluids.EMPTY))) {
				throw new IllegalArgumentException("Recipe may only contain a fluid or an item, not both.");
			}
			
			return new WorldCorruptionCrafting(recipeId, input, inputFluid, output, outputFluid);
		}

		@Override
		public WorldCorruptionCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			Fluid fluidInput = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			ItemStack output = buffer.readItem();
			Fluid fluidOutput = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			return new WorldCorruptionCrafting(recipeId, input, fluidInput, output, fluidOutput);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, WorldCorruptionCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeResourceLocation(recipe.inputFluid.getRegistryName());
			buffer.writeItem(recipe.output.asItem().getDefaultInstance());
			buffer.writeResourceLocation(recipe.outputFluid.getRegistryName());
		}
		
	}
	public static class TypeWorldCorruptionCrafting implements RecipeType<WorldCorruptionCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:world_corruption";
		}
	}

}
