package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
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

public class EntropyReactorCrafting implements Recipe<Container>{

	
	public static final RecipeType<EntropyReactorCrafting> ERO_RECIPE = new TypeEntropyReactorCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final ItemStack output;
	private final float odds;
	private final float varietyReqd;
	private final int max;
	private final ResourceLocation id;
	
	public EntropyReactorCrafting(ResourceLocation id, ItemStack output, float odds, int max, float varietyReqd) {
		this.id = id;
		this.output = output;
		this.odds = odds;
		this.max = max;
		this.varietyReqd = varietyReqd;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		return true;
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
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted(){
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(Ingredient.of(Registry.getItem("entropy_reactor_block"), Registry.getItem("entropy_reactor_core")));
		nnl.add(Ingredient.of(Registry.getItem("corrupted_shard")));
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
		return ERO_RECIPE;
	}
	
	public float getOdds() {
		return odds;
	}
	
	public float getVarietyReqd() {
		return varietyReqd;
	}
	
	public int getMax() {
		return max;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EntropyReactorCrafting>{

		@Override
		public EntropyReactorCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				float odds = GsonHelper.getAsFloat(json, "odds");
				float varietyReqd = GsonHelper.getAsFloat(json, "varietyReqd");
				if(odds < 0f || odds > 1f) {
					throw new IllegalArgumentException("odds must be between 0 and 1.");
				}
				if(varietyReqd < 0f || varietyReqd > 1f) {
					throw new IllegalArgumentException("varietyReqd must be between 0 and 1.");
				}
				int max = GsonHelper.getAsInt(json, "max");
				if(max < 0) {
					throw new IllegalArgumentException("max must be more than 0.");
				}
				
				return new EntropyReactorCrafting(recipeId, output, odds, max, varietyReqd);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Entropy Reactor Output from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public EntropyReactorCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final ItemStack output = buffer.readItem();
			float odds = buffer.readFloat();
			float varietyReqd = buffer.readFloat();
			int max = buffer.readInt();
			
			return new EntropyReactorCrafting(recipeId, output, odds, max, varietyReqd);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, EntropyReactorCrafting recipe) {
			buffer.writeItem(recipe.output);
			buffer.writeFloat(recipe.odds);
			buffer.writeFloat(recipe.varietyReqd);
			buffer.writeInt(recipe.max);
			
		}
		
	}
	
	public static class TypeEntropyReactorCrafting implements RecipeType<EntropyReactorCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:entropy_reactor";
		}
	}

	
}
