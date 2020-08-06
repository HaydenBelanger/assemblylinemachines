package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier.TEElectricPurifier;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PurifierCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<PurifierCrafting> PURIFIER_RECIPE = new TypePurifierCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	
	private final Ingredient parta;
	private final Ingredient partb;
	private final Ingredient tobepurified;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public PurifierCrafting(ResourceLocation id, Ingredient parta, Ingredient partb, Ingredient tobepurified, ItemStack output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.tobepurified = tobepurified;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEElectricPurifier) {
				if((parta.test(inv.getStackInSlot(1)) && partb.test(inv.getStackInSlot(2))) || (partb.test(inv.getStackInSlot(1)) && parta.test(inv.getStackInSlot(2)))) {
					if(tobepurified.test(inv.getStackInSlot(3))) {
						return true;
					}
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

	
	public boolean requiresUpgrade() {
		if((parta.test(new ItemStack(Items.SAND)) && partb.test(new ItemStack(Items.GRAVEL))) || (parta.test(new ItemStack(Items.GRAVEL)) && partb.test(new ItemStack(Items.SAND)))) {
			return false;
		}
		
		return true;
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
		return PURIFIER_RECIPE;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta);
		nnl.add(partb);
		nnl.add(tobepurified);
		
		return nnl;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted(){
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta);
		nnl.add(partb);
		nnl.add(tobepurified);
		nnl.add(Ingredient.fromItems(Registry.getItem("electric_purifier")));
		
		return nnl;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<PurifierCrafting>{

		@Override
		public PurifierCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.deserialize(JSONUtils.getJsonObject(json, "part_a"));
				final Ingredient ingredientb = Ingredient.deserialize(JSONUtils.getJsonObject(json, "part_b"));
				final Ingredient tobepurified = Ingredient.deserialize(JSONUtils.getJsonObject(json, "tobepurified"));
				
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				final int time = JSONUtils.getInt(json, "time");
				
				return new PurifierCrafting(recipeId, ingredienta, ingredientb, tobepurified, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Purifier recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public PurifierCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient inputa = Ingredient.read(buffer);
			final Ingredient inputb = Ingredient.read(buffer);
			final Ingredient inputc = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int time = buffer.readInt();
			
			return new PurifierCrafting(recipeId, inputa, inputb, inputc, output, time);
		}

		@Override
		public void write(PacketBuffer buffer, PurifierCrafting recipe) {
			recipe.parta.write(buffer);
			recipe.partb.write(buffer);
			recipe.tobepurified.write(buffer);
			buffer.writeItemStack(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypePurifierCrafting implements IRecipeType<PurifierCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:purifier";
		}
	}

	
}
