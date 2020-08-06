package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RefiningCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<RefiningCrafting> REFINING_RECIPE = new TypeRefiningCrafting();
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
	public boolean matches(IInventory inv, World worldIn) {
		return true;
		
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public IRecipeType<?> getType() {
		return REFINING_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RefiningCrafting>{

		@Override
		public RefiningCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(json, "attachment")));
				Pair<FluidStack, Float> fsInput = null;
				if(JSONUtils.hasField(json, "input_fluid")) {
					fsInput = getFluidStackWithOdds(JSONUtils.getJsonObject(json, "input_fluid"));
				}
				
				Pair<Ingredient, Float> ingInput = null;
				if(JSONUtils.hasField(json, "input_item")) {
					ingInput = getIngredientWithOdds(JSONUtils.getJsonObject(json, "input_item"));
				}
				
				if(fsInput == null && ingInput == null) {
					throw new IllegalArgumentException("Either input_fluid or input_item must be present.");
				}
				
				int time = JSONUtils.getInt(json, "proc_time");
				
				Pair<FluidStack, Float> fsouta = null;
				Pair<FluidStack, Float> fsoutb = null;
				if(JSONUtils.hasField(json, "output_fluid")) {
					fsouta = getFluidStackWithOdds(JSONUtils.getJsonObject(json, "output_fluid"));
				}else if(JSONUtils.hasField(json, "output_fluid_a")) {
					fsouta = getFluidStackWithOdds(JSONUtils.getJsonObject(json, "output_fluid_a"));
				}
				
				if(JSONUtils.hasField(json, "output_fluid_b")) {
					
					
					if(fsouta == null) {
						throw new IllegalArgumentException("output_fluid_b cannot be present while output_fluid is not.");
					}
					fsoutb = getFluidStackWithOdds(JSONUtils.getJsonObject(json, "output_fluid_b"));
				}
				
				Pair<ItemStack, Float> isout = null;
				if(JSONUtils.hasField(json, "output_item")) {
					isout = getItemStackWithOdds(JSONUtils.getJsonObject(json, "output_item"));
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
			
			FluidStack fs = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(JSONUtils.getString(json, "fluid"))), JSONUtils.getInt(json, "amount"));
			Float odds = 0f;
			if(JSONUtils.hasField(json, "upgrade_preserve_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_preserve_chance");
			}else if(JSONUtils.hasField(json, "upgrade_multiply_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(fs, odds);
		}
		
		private Pair<Ingredient, Float> getIngredientWithOdds(JsonObject json) throws Exception{
			Ingredient ing = Ingredient.deserialize(json);
			Float odds = 0f;
			if(JSONUtils.hasField(json, "upgrade_preserve_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_preserve_chance");
			}else if(JSONUtils.hasField(json, "upgrade_multiply_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(ing, odds);
		}
		
		private Pair<ItemStack, Float> getItemStackWithOdds(JsonObject json) throws Exception{
			ItemStack ing = ShapedRecipe.deserializeItem(json);
			Float odds = 0f;
			if(JSONUtils.hasField(json, "upgrade_preserve_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_preserve_chance");
			}else if(JSONUtils.hasField(json, "upgrade_multiply_chance")) {
				odds = JSONUtils.getFloat(json, "upgrade_multiply_chance");
			}
			
			return Pair.of(ing, odds);
		}
		
		@Override
		public RefiningCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			
			return new RefiningCrafting(recipeId, ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation()), Pair.of(buffer.readFluidStack(), buffer.readFloat()), 
					Pair.of(Ingredient.read(buffer), buffer.readFloat()), buffer.readInt(), Pair.of(buffer.readFluidStack(), buffer.readFloat()), Pair.of(buffer.readFluidStack(), 
							buffer.readFloat()),Pair.of(buffer.readItemStack(), buffer.readFloat()));
		}

		@Override
		public void write(PacketBuffer buffer, RefiningCrafting recipe) {
			buffer.writeResourceLocation(recipe.attachmentBlock.getRegistryName());
			buffer.writeFluidStack(recipe.fluidInput.getFirst());
			buffer.writeFloat(recipe.fluidInput.getSecond());
			recipe.itemInput.getFirst().write(buffer);
			buffer.writeFloat(recipe.itemInput.getSecond());
			buffer.writeInt(recipe.time);
			buffer.writeFluidStack(recipe.fluidOutputA.getFirst());
			buffer.writeFloat(recipe.fluidOutputA.getSecond());
			buffer.writeFluidStack(recipe.fluidOutputB.getFirst());
			buffer.writeFloat(recipe.fluidOutputB.getSecond());
			buffer.writeItemStack(recipe.itemOutput.getFirst());
			buffer.writeFloat(recipe.itemOutput.getSecond());
		}
		
	}
	
	public static class TypeRefiningCrafting implements IRecipeType<RefiningCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:refining";
		}
	}
	
}
