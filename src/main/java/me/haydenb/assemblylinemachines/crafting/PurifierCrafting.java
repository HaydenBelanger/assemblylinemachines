package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.BlockElectricPurifier.TEElectricPurifier;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PurifierCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<PurifierCrafting> PURIFIER_RECIPE = new TypePurifierCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	
	private final Lazy<Ingredient> parta;
	private final Lazy<Ingredient> partb;
	private final Lazy<Ingredient> tobepurified;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public PurifierCrafting(ResourceLocation id, Lazy<Ingredient> parta, Lazy<Ingredient> partb, Lazy<Ingredient> tobepurified, ItemStack output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.tobepurified = tobepurified;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(inv != null) {
			if(inv instanceof TEElectricPurifier) {
				if((parta.get().test(inv.getItem(1)) && partb.get().test(inv.getItem(2))) || (partb.get().test(inv.getItem(1)) && parta.get().test(inv.getItem(2)))) {
					if(tobepurified.get().test(inv.getItem(3))) {
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

	
	public boolean requiresUpgrade() {
		if((parta.get().test(new ItemStack(Items.SAND)) && partb.get().test(new ItemStack(Items.GRAVEL))) || (parta.get().test(new ItemStack(Items.GRAVEL)) && partb.get().test(new ItemStack(Items.SAND)))) {
			return false;
		}
		
		return true;
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
		return PURIFIER_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta.get());
		nnl.add(partb.get());
		nnl.add(tobepurified.get());
		
		return nnl;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		return List.of(parta.get(), partb.get(), tobepurified.get());
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return List.of(output);
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PurifierCrafting>{

		@Override
		public PurifierCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> ingredienta = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a")));
				Lazy<Ingredient> ingredientb = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b")));
				Lazy<Ingredient> tobepurified = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tobepurified")));
				
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int time = GsonHelper.getAsInt(json, "time");
				
				return new PurifierCrafting(recipeId, ingredienta, ingredientb, tobepurified, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Purifier recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public PurifierCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient inputa = Ingredient.fromNetwork(buffer);
			final Ingredient inputb = Ingredient.fromNetwork(buffer);
			final Ingredient inputc = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int time = buffer.readInt();
			
			return new PurifierCrafting(recipeId, Lazy.of(() -> inputa), Lazy.of(() -> inputb), Lazy.of(() -> inputc), output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PurifierCrafting recipe) {
			recipe.parta.get().toNetwork(buffer);
			recipe.partb.get().toNetwork(buffer);
			recipe.tobepurified.get().toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypePurifierCrafting implements RecipeType<PurifierCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:purifier";
		}
	}

	
}
