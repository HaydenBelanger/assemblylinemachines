package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockLumberMill.TELumberMill;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LumberCrafting implements Recipe<Container>{

	
	public static final RecipeType<LumberCrafting> LUMBER_RECIPE = new TypeLumberCrafting();
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
	public boolean matches(Container inv, Level worldIn) {
		if(inv != null) {
			if(inv instanceof TELumberMill) {
				if(input.test(inv.getItem(2))) {
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
		return this.outputa.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
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
	public boolean isSpecial() {
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
		nnl.add(Ingredient.of(Registry.getItem("lumber_mill")));
		return nnl;
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
		return LUMBER_RECIPE;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<LumberCrafting>{

		@Override
		public LumberCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				ItemStack outputb = ItemStack.EMPTY;
				float opbchance = 0f;
				if(GsonHelper.isValidNode(json, "secondaryoutput")) {
					outputb = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "secondaryoutput"));
					opbchance = GsonHelper.getAsFloat(json, "opbchance");
				}
				final int time = GsonHelper.getAsInt(json, "time");
				
				return new LumberCrafting(recipeId, input, output, outputb, opbchance, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Lumber Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public LumberCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient input = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final ItemStack opb = buffer.readItem();
			final float opbc = buffer.readFloat();
			final int time = buffer.readInt();
			
			return new LumberCrafting(recipeId, input, output, opb, opbc, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, LumberCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeItem(recipe.outputa);
			buffer.writeItem(recipe.outputb);
			buffer.writeFloat(recipe.opbchance);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypeLumberCrafting implements RecipeType<LumberCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:lumber";
		}
	}

	
}
