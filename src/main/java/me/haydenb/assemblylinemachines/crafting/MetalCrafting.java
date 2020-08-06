package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockMetalShaper.TEMetalShaper;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MetalCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<MetalCrafting> METAL_RECIPE = new TypeMetalCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Ingredient input;
	private final ItemStack outputa;
	private final int time;
	private final ResourceLocation id;
	
	public MetalCrafting(ResourceLocation id, Ingredient input, ItemStack outputa, int time) {
		this.id = id;
		this.input = input;
		this.outputa = outputa;
		this.time = time;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEMetalShaper) {
				if(input.test(inv.getStackInSlot(1))) {
					return true;
				}
			}
			
			return false;
		}else {
			return true;
		}
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		return this.outputa.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return outputa;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(input);
		return nnl;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(input);
		nnl.add(Ingredient.fromItems(Registry.getItem("metal_shaper")));
		return nnl;
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
	public IRecipeType<?> getType() {
		return METAL_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<MetalCrafting>{

		@Override
		public MetalCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				
				final int time = JSONUtils.getInt(json, "time");
				
				return new MetalCrafting(recipeId, input, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Metal Shaper Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public MetalCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient input = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int time = buffer.readInt();
			
			return new MetalCrafting(recipeId, input, output, time);
		}

		@Override
		public void write(PacketBuffer buffer, MetalCrafting recipe) {
			recipe.input.write(buffer);
			buffer.writeItemStack(recipe.outputa);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeMetalCrafting implements IRecipeType<MetalCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:metal";
		}
	}

	
}
