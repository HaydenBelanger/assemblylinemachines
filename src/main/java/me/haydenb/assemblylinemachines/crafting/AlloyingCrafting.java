package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAlloySmelter.TEAlloySmelter;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class AlloyingCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<AlloyingCrafting> ALLOYING_RECIPE = new TypeAlloyingCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	
	private final Ingredient parta;
	private final Ingredient partb;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public AlloyingCrafting(ResourceLocation id, Ingredient parta, Ingredient partb, ItemStack output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEAlloySmelter) {
				if((parta.test(inv.getStackInSlot(1)) && partb.test(inv.getStackInSlot(2))) || (partb.test(inv.getStackInSlot(1)) && parta.test(inv.getStackInSlot(2)))) {
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
		return this.output.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return output;
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
		return ALLOYING_RECIPE;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta);
		nnl.add(partb);
		
		return nnl;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted(){
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta);
		nnl.add(partb);
		nnl.add(Ingredient.fromItems(Registry.getItem("alloy_smelter")));
		
		return nnl;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AlloyingCrafting>{

		@Override
		public AlloyingCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.deserialize(JSONUtils.getJsonObject(json, "part_a"));
				final Ingredient ingredientb = Ingredient.deserialize(JSONUtils.getJsonObject(json, "part_b"));
				
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				final int time = JSONUtils.getInt(json, "time");
				
				return new AlloyingCrafting(recipeId, ingredienta, ingredientb, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Alloying recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public AlloyingCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient inputa = Ingredient.read(buffer);
			final Ingredient inputb = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int time = buffer.readInt();
			
			return new AlloyingCrafting(recipeId, inputa, inputb, output, time);
		}

		@Override
		public void write(PacketBuffer buffer, AlloyingCrafting recipe) {
			recipe.parta.write(buffer);
			recipe.partb.write(buffer);
			buffer.writeItemStack(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeAlloyingCrafting implements IRecipeType<AlloyingCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:alloying";
		}
	}

	
}
