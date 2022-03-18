package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
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

	
	public static final RecipeType<MetalCrafting> METAL_RECIPE = new RecipeType<MetalCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:metal";
		}
	};
	
	public static final MetalSerializer SERIALIZER = new MetalSerializer();
	
	private final Lazy<Ingredient> input;
	private final ItemStack output;
	public final int time;
	private final ResourceLocation id;
	
	public MetalCrafting(ResourceLocation id, Lazy<Ingredient> input, ItemStack outputa, int time) {
		this.id = id;
		this.input = input;
		this.output = outputa;
		this.time = time;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		return input.get().test(inv.getItem(1));
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if(inv instanceof IMachineDataBridge) {
			inv.getItem(1).shrink(1);
			((IMachineDataBridge) inv).setCycles(time);
		}
		return output.copy();
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
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(input.get(), output);
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
	
	public static class MetalSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<MetalCrafting>{

		@Override
		public MetalCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> input = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
				ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				
				int time = GsonHelper.getAsInt(json, "time");
				
				return new MetalCrafting(recipeId, input, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Metal Shaper Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public MetalCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int time = buffer.readInt();
			
			return new MetalCrafting(recipeId, Lazy.of(() -> input), output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, MetalCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
}
