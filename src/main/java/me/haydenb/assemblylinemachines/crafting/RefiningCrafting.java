package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RefiningCrafting implements Recipe<Container>{

	
	public static final RecipeType<RefiningCrafting> REFINING_RECIPE = new TypeRefiningCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	public final Block attachmentBlock;
	public final Pair<FluidStack, Float> fluidInput;
	public final Pair<Ingredient, Float> itemInput;
	public final int time;
	public final Pair<FluidStack, Float> fluidOutputA;
	public final Pair<FluidStack, Float> fluidOutputB;
	public final Pair<ItemStack, Float> itemOutput;
	
	private final ResourceLocation id;
	
	public RefiningCrafting(ResourceLocation id, Block attachmentBlock, Pair<FluidStack, Float> fluidInput, Pair<Ingredient, Float> itemInput, 
			int time, Pair<FluidStack, Float> fluidOutputA, Pair<FluidStack, Float> fluidOutputB, Pair<ItemStack, Float> itemOutput) {
		
		this.id = id;
		this.attachmentBlock = attachmentBlock;
		this.fluidInput = fluidInput;
		this.itemInput = itemInput;
		this.time = time;
		this.fluidOutputA = fluidOutputA;
		this.fluidOutputB = fluidOutputB;
		this.itemOutput = itemOutput;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		return true;
		
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
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
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeType<?> getType() {
		return REFINING_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RefiningCrafting>{

		@Override
		public RefiningCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "attachment")));
				Pair<FluidStack, Float> fsInput = null;
				if(GsonHelper.isValidNode(json, "input_fluid")) {
					fsInput = getFluidStackWithOdds(GsonHelper.getAsJsonObject(json, "input_fluid"));
				}
				
				Pair<Ingredient, Float> ingInput = null;
				if(GsonHelper.isValidNode(json, "input_item")) {
					ingInput = getIngredientWithOdds(GsonHelper.getAsJsonObject(json, "input_item"));
				}
				
				if(fsInput == null && ingInput == null) {
					throw new IllegalArgumentException("Either input_fluid or input_item must be present.");
				}
				
				int time = GsonHelper.getAsInt(json, "proc_time");
				
				Pair<FluidStack, Float> fsouta = null;
				Pair<FluidStack, Float> fsoutb = null;
				if(GsonHelper.isValidNode(json, "output_fluid")) {
					fsouta = getFluidStackWithOdds(GsonHelper.getAsJsonObject(json, "output_fluid"));
				}else if(GsonHelper.isValidNode(json, "output_fluid_a")) {
					fsouta = getFluidStackWithOdds(GsonHelper.getAsJsonObject(json, "output_fluid_a"));
				}
				
				if(GsonHelper.isValidNode(json, "output_fluid_b")) {
					
					
					if(fsouta == null) {
						throw new IllegalArgumentException("output_fluid_b cannot be present while output_fluid is not.");
					}
					fsoutb = getFluidStackWithOdds(GsonHelper.getAsJsonObject(json, "output_fluid_b"));
				}
				
				Pair<ItemStack, Float> isout = null;
				if(GsonHelper.isValidNode(json, "output_item")) {
					isout = getItemStackWithOdds(GsonHelper.getAsJsonObject(json, "output_item"));
				}
				
				if(fsouta == null && isout == null) {
					throw new IllegalArgumentException("Either output_fluid or output_item must be present.");
				}
				
				if(fsInput == null) {
					fsInput = Pair.of(FluidStack.EMPTY, 0f);
				}
				
				if(ingInput == null) {
					ingInput = Pair.of(Ingredient.EMPTY, 0f);
				}
				
				if(fsouta == null) {
					fsouta = Pair.of(FluidStack.EMPTY, 0f);
				}
				
				if(fsoutb == null) {
					fsoutb = Pair.of(FluidStack.EMPTY, 0f);
				}
				
				if(isout == null) {
					isout = Pair.of(ItemStack.EMPTY, 0f);
				}
				return new RefiningCrafting(recipeId, block, fsInput, ingInput, time, fsouta, fsoutb, isout);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Refining Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		private Pair<FluidStack, Float> getFluidStackWithOdds(JsonObject json) throws Exception{
			
			FluidStack fs = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid"))), GsonHelper.getAsInt(json, "amount"));
			Float odds = 0f;
			if(GsonHelper.isValidNode(json, "upgrade_preserve_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_preserve_chance");
			}else if(GsonHelper.isValidNode(json, "upgrade_multiply_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(fs, odds);
		}
		
		private Pair<Ingredient, Float> getIngredientWithOdds(JsonObject json) throws Exception{
			Ingredient ing = Ingredient.fromJson(json);
			Float odds = 0f;
			if(GsonHelper.isValidNode(json, "upgrade_preserve_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_preserve_chance");
			}else if(GsonHelper.isValidNode(json, "upgrade_multiply_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(ing, odds);
		}
		
		private Pair<ItemStack, Float> getItemStackWithOdds(JsonObject json) throws Exception{
			ItemStack ing = ShapedRecipe.itemStackFromJson(json);
			Float odds = 0f;
			if(GsonHelper.isValidNode(json, "upgrade_preserve_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_preserve_chance");
			}else if(GsonHelper.isValidNode(json, "upgrade_multiply_chance")) {
				odds = GsonHelper.getAsFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(ing, odds);
		}
		
		@Override
		public RefiningCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			
			return new RefiningCrafting(recipeId, ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation()), Pair.of(buffer.readFluidStack(), buffer.readFloat()), 
					Pair.of(Ingredient.fromNetwork(buffer), buffer.readFloat()), buffer.readInt(), Pair.of(buffer.readFluidStack(), buffer.readFloat()), Pair.of(buffer.readFluidStack(), 
							buffer.readFloat()),Pair.of(buffer.readItem(), buffer.readFloat()));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, RefiningCrafting recipe) {
			buffer.writeResourceLocation(recipe.attachmentBlock.getRegistryName());
			buffer.writeFluidStack(recipe.fluidInput.getFirst());
			buffer.writeFloat(recipe.fluidInput.getSecond());
			recipe.itemInput.getFirst().toNetwork(buffer);
			buffer.writeFloat(recipe.itemInput.getSecond());
			buffer.writeInt(recipe.time);
			buffer.writeFluidStack(recipe.fluidOutputA.getFirst());
			buffer.writeFloat(recipe.fluidOutputA.getSecond());
			buffer.writeFluidStack(recipe.fluidOutputB.getFirst());
			buffer.writeFloat(recipe.fluidOutputB.getSecond());
			buffer.writeItem(recipe.itemOutput.getFirst());
			buffer.writeFloat(recipe.itemOutput.getSecond());
		}
		
	}
	
	public static class TypeRefiningCrafting implements RecipeType<RefiningCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:refining";
		}
	}
	
}
