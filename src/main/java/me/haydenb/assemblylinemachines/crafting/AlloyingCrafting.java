package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class AlloyingCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<AlloyingCrafting> ALLOYING_RECIPE = new TypeAlloyingCrafting();
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
	public boolean matches(Container inv, Level worldIn) {
		if((parta.test(inv.getItem(1)) && partb.test(inv.getItem(2))) || (partb.test(inv.getItem(1)) && parta.test(inv.getItem(2)))) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		if(inv instanceof IMachineDataBridge) {
			inv.getItem(1).shrink(1);
			inv.getItem(2).shrink(1);
			((IMachineDataBridge) inv).setCycles(time / 10f);
		}
		return this.output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output;
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
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		return List.of(parta, partb);
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return List.of(output);
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AlloyingCrafting>{

		@Override
		public AlloyingCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a"));
				final Ingredient ingredientb = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b"));
				
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int time = GsonHelper.getAsInt(json, "time");
				
				return new AlloyingCrafting(recipeId, ingredienta, ingredientb, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Alloying recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public AlloyingCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient inputa = Ingredient.fromNetwork(buffer);
			final Ingredient inputb = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int time = buffer.readInt();
			
			return new AlloyingCrafting(recipeId, inputa, inputb, output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlloyingCrafting recipe) {
			recipe.parta.toNetwork(buffer);
			recipe.partb.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeAlloyingCrafting implements RecipeType<AlloyingCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:alloying";
		}
	}

	
}
