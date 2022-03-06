package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.BlockMetalShaper.TEMetalShaper;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MetalCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<MetalCrafting> METAL_RECIPE = new TypeMetalCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Lazy<Ingredient> input;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public MetalCrafting(ResourceLocation id, Lazy<Ingredient> input, ItemStack outputa, int time) {
		this.id = id;
		this.input = input;
		this.output = outputa;
		this.time = time;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(inv != null) {
			if(inv instanceof TEMetalShaper) {
				if(input.get().test(inv.getItem(1))) {
					return true;
				}
			}
			
			return false;
		}else {
			return true;
		}
	}
	
	@Override
	public ItemStack assemble(Container inv) {
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
	
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(input.get());
		return nnl;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		return List.of(input.get());
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return List.of(output);
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
		return METAL_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MetalCrafting>{

		@Override
		public MetalCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> input = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				
				final int time = GsonHelper.getAsInt(json, "time");
				
				return new MetalCrafting(recipeId, input, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Metal Shaper Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public MetalCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient input = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int time = buffer.readInt();
			
			return new MetalCrafting(recipeId, Lazy.of(() -> input), output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, MetalCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeMetalCrafting implements RecipeType<MetalCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:metal";
		}
	}

	
}
