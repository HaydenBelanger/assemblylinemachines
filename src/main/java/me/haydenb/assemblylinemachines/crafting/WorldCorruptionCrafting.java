package me.haydenb.assemblylinemachines.crafting;

import java.util.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.machines.BlockCorruptingBasin.TECorruptingBasin;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

@SuppressWarnings("removal")
public class WorldCorruptionCrafting implements Recipe<Container>, IRecipeCategoryBuilder {

	public static final RecipeType<WorldCorruptionCrafting> WORLD_CORRUPTION_RECIPE = new RecipeType<WorldCorruptionCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:world_corruption";
		}
	};
	
	public static final WorldCorruptionSerializer SERIALIZER = new WorldCorruptionSerializer();
	
	private final Lazy<Ingredient> input;
	private final Fluid inputFluid;
	
	private final Block output;
	private final List<Pair<Float, Block>> additionalOutputs = new ArrayList<>();
	private final Fluid outputFluid;
	
	private final ResourceLocation id;
	
	public WorldCorruptionCrafting(ResourceLocation id, Lazy<Ingredient> input, Fluid inputFluid, ItemStack output, Fluid outputFluid, List<Pair<Float, ItemStack>> additionalOutputs) {
		this.id = id;
		this.input = input;
		this.output = output.isEmpty() ? Blocks.AIR : ((BlockItem) output.getItem()).getBlock();
		this.inputFluid = inputFluid;
		this.outputFluid = outputFluid;
		for(Pair<Float, ItemStack> pair : additionalOutputs) {
			this.additionalOutputs.add(Pair.of(pair.getFirst(), ((BlockItem) pair.getSecond().getItem()).getBlock()));
		}
		
		this.additionalOutputs.sort(new Comparator<>() {
			@Override
			public int compare(Pair<Float, Block> o1, Pair<Float, Block> o2) {
				return Math.round((o1.getFirst() * 100f) / (o2.getFirst() * 100f));
			}
		});
	}
	
	@Override
	public boolean matches(Container container, Level level) {
		if(container == null) return true;
		if(container instanceof TECorruptingBasin) {
			return input.get().test(container.getItem(2));
		}
		return false;
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return false;
	}
	
	@Override
	public ItemStack assemble(Container p_44001_) {
		return output.asItem().getDefaultInstance().copy();
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
		return !input.get().isEmpty() ? List.of(input.get()) : null;
	}
	
	@Override
	public List<FluidStack> getJEIFluidInputs() {
		return !inputFluid.equals(Fluids.EMPTY) ? List.of(new FluidStack(inputFluid, 1000)) : null;
	}
	
	@Override
	public List<List<ItemStack>> getJEIItemOutputLists() {
		if(!output.equals(Blocks.AIR)) {
			List<ItemStack> stacks = new ArrayList<>();
			stacks.add(output.asItem().getDefaultInstance());
			for(Pair<Float, Block> pair : this.additionalOutputs) {
				stacks.add(pair.getSecond().asItem().getDefaultInstance());
			}
			return List.of(stacks);
		}
		return null;
	}
	
	@Override
	public List<FluidStack> getJEIFluidOutputs() {
		return !outputFluid.equals(Fluids.EMPTY) ? List.of(new FluidStack(outputFluid, 1000)) : null;
	}
	
	@Override
	public void setupSlots(IRecipeLayout layout, IGuiHelper helper, RecipeCategoryBuilder category) {
		if(!input.get().isEmpty()) {
			layout.getItemStacks().init(0, false, 0, 4);
			layout.getItemStacks().init(1, false, category.getBasicRenderer(ItemStack.class, this), 47, 4, 18, 18, 1, 1);
		}else if(!inputFluid.equals(Fluids.EMPTY)){
			layout.getFluidStacks().init(0, false, 1, 5, 16, 16, 1, false, null);
			layout.getFluidStacks().init(1, false, 48, 5, 16, 16, 1, false, null);
		}else {
			throw new IllegalArgumentException("Item Input and Fluid Input are both non-present!");
		}
	}
	
	
	public Optional<Block> testBlock(Random rand, Block test){
		if(input.get().isEmpty() || !input.get().test(test.asItem().getDefaultInstance())) return Optional.empty();
		return Optional.of(getRandom(rand));
	}
	
	public Block getRandom(Random rand) {
		for(Pair<Float, Block> pair : additionalOutputs) {
			if(rand.nextFloat() <= pair.getFirst()) {
				return pair.getSecond();
			}
		}
		return output;
	}
	
	public Optional<Fluid> testFluid(Fluid test){
		if(inputFluid.equals(Fluids.EMPTY)) return Optional.empty();
		return inputFluid.equals(test) ? Optional.of(outputFluid) : Optional.empty();
	}
	
	public Optional<Float> getChanceOfSubdrop(Item stack) {
		for(Pair<Float, Block> pair : this.additionalOutputs) {
			if(pair.getSecond().asItem().equals(stack)) {
				return Optional.of(pair.getFirst());
			}
		}
		
		return Optional.empty();
	}
	
	public boolean hasOptionalResults() {
		return this.additionalOutputs.size() != 0;
	}
	
	public static class WorldCorruptionSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<WorldCorruptionCrafting>{

		@Override
		public WorldCorruptionCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			
			Fluid inputFluid = GsonHelper.isValidNode(json, "fluidInput") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluidInput"))) : Fluids.EMPTY;
			ItemStack output = GsonHelper.isValidNode(json, "output") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output")) : ItemStack.EMPTY;
			Fluid outputFluid = GsonHelper.isValidNode(json, "fluidOutput") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluidOutput"))) : Fluids.EMPTY;
			
			
			
			List<Pair<Float, ItemStack>> additionalOutputs;
			if(GsonHelper.isValidNode(json, "additionalOutputs")) {
				additionalOutputs = new ArrayList<>();
				Iterator<JsonElement> jsonAddtOutputs = GsonHelper.getAsJsonArray(json, "additionalOutputs").iterator();
				while(jsonAddtOutputs.hasNext()) {
					JsonObject subJson = jsonAddtOutputs.next().getAsJsonObject();
					additionalOutputs.add(Pair.of(GsonHelper.getAsFloat(subJson, "chance"), ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(subJson, "output"))));
				}
			}else {
				additionalOutputs = List.of();
			}
			
			Lazy<Ingredient> input = Lazy.of(() -> {
				if(GsonHelper.isValidNode(json, "input")) {
					if(!inputFluid.equals(Fluids.EMPTY)) throw new IllegalArgumentException(recipeId + " cannot have input and fluidInput both set.");
					if(output.isEmpty()) throw new IllegalArgumentException(recipeId + " must have output when input is set.");
					return Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				}else {
					if(inputFluid.equals(Fluids.EMPTY)) throw new IllegalArgumentException(recipeId + " does not have input or fluidInput set.");
					if(!output.isEmpty()) throw new IllegalArgumentException(recipeId + " cannot have output while input is unset.");
					if(additionalOutputs.size() != 0) throw new IllegalArgumentException(recipeId + " cannot have additionalOutputs when input is unset.");
					return Ingredient.EMPTY;
				}
			});
			
			if(output.isEmpty() && outputFluid.equals(Fluids.EMPTY)) throw new IllegalArgumentException("One of output or outputFluid must be set.");
			if(!inputFluid.equals(Fluids.EMPTY) && outputFluid.equals(Fluids.EMPTY)) throw new IllegalArgumentException("If fluidInput is set, fluidOutput must be set as well.");
			
			return new WorldCorruptionCrafting(recipeId, input, inputFluid, output, outputFluid, additionalOutputs);
		}

		@Override
		public WorldCorruptionCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			Fluid fluidInput = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			ItemStack output = buffer.readItem();
			Fluid fluidOutput = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			List<Pair<Float, ItemStack>> additionalOutputs = new ArrayList<>();
			int size = buffer.readInt();
			for(int i = 0; i < size; i++) {
				float a = buffer.readFloat();
				ItemStack b = buffer.readItem();
				additionalOutputs.add(Pair.of(a, b));
			}
			return new WorldCorruptionCrafting(recipeId, Lazy.of(() -> input), fluidInput, output, fluidOutput, additionalOutputs);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, WorldCorruptionCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeResourceLocation(recipe.inputFluid.getRegistryName());
			buffer.writeItem(recipe.output.asItem().getDefaultInstance());
			buffer.writeResourceLocation(recipe.outputFluid.getRegistryName());
			buffer.writeInt(recipe.additionalOutputs.size());
			for(Pair<Float, Block> pair : recipe.additionalOutputs) {
				buffer.writeFloat(pair.getFirst());
				buffer.writeItem(pair.getSecond().asItem().getDefaultInstance());
			}
		}
		
	}
}
