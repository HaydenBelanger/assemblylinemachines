package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockLumberMill.TELumberMill;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LumberCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<LumberCrafting> LUMBER_RECIPE = new TypeLumberCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Ingredient input;
	private final ItemStack outputa;
	private final ItemStack outputb;
	private final float opbchance;
	private final int time;
	private final ResourceLocation id;
	
	public LumberCrafting(ResourceLocation id, Ingredient input, ItemStack outputa, ItemStack outputb, float opbchance, int time) {
		this.id = id;
		this.input = input;
		this.outputa = outputa;
		this.outputb = outputb;
		this.opbchance = opbchance;
		this.time = time;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TELumberMill) {
				if(input.test(inv.getStackInSlot(2))) {
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
	
	public ItemStack getSecondaryOutput() {
		return outputb;
	}
	
	public float getOutputChance() {
		return opbchance;
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
		nnl.add(Ingredient.fromItems(Registry.getItem("lumber_mill")));
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
		return LUMBER_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<LumberCrafting>{

		@Override
		public LumberCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				ItemStack outputb = ItemStack.EMPTY;
				float opbchance = 0f;
				if(JSONUtils.hasField(json, "secondaryoutput")) {
					outputb = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "secondaryoutput"));
					opbchance = JSONUtils.getFloat(json, "opbchance");
				}
				final int time = JSONUtils.getInt(json, "time");
				
				return new LumberCrafting(recipeId, input, output, outputb, opbchance, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Lumber Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public LumberCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient input = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final ItemStack opb = buffer.readItemStack();
			final float opbc = buffer.readFloat();
			final int time = buffer.readInt();
			
			return new LumberCrafting(recipeId, input, output, opb, opbc, time);
		}

		@Override
		public void write(PacketBuffer buffer, LumberCrafting recipe) {
			recipe.input.write(buffer);
			buffer.writeItemStack(recipe.outputa);
			buffer.writeItemStack(recipe.outputb);
			buffer.writeFloat(recipe.opbchance);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeLumberCrafting implements IRecipeType<LumberCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:lumber";
		}
	}

	
}
