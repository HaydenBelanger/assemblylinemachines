package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.config.ConfigCondition;
import me.haydenb.assemblylinemachines.registry.utils.*;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class AlloyingCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<AlloyingCrafting> ALLOYING_RECIPE = new RecipeType<AlloyingCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:alloying";
		}
	};
	
	public static final AlloyingSerializer SERIALIZER = new AlloyingSerializer();
	
	
	private final CountIngredient parta;
	private final CountIngredient partb;
	private final Lazy<ItemStack> output;
	private final int time;
	private final ResourceLocation id;
	
	public AlloyingCrafting(ResourceLocation id, CountIngredient parta, CountIngredient partb, Lazy<ItemStack> output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(!this.showInJEI()) return false;
		return ((parta.test(inv.getItem(1)) && partb.test(inv.getItem(2))) || (partb.test(inv.getItem(1)) && parta.test(inv.getItem(2))));
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		if(inv instanceof IMachineDataBridge) {
			int shra = 0;
			int shrb = 0;
			if(parta.test(inv.getItem(1)) && partb.test(inv.getItem(2))) {
				shra = parta.getCount();
				shrb = partb.getCount();
			}else if(partb.test(inv.getItem(1)) && parta.test(inv.getItem(2))) {
				shrb = parta.getCount();
				shra = partb.getCount();
			}else {
				return ItemStack.EMPTY;
			}
			
			inv.getItem(1).shrink(shra);
			inv.getItem(2).shrink(shrb);
			((IMachineDataBridge) inv).setCycles(time / 10f);
		}
		return this.output.get().copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output.get();
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
	public boolean showInJEI() {
		return !this.parta.isEmpty() && !this.partb.isEmpty() && !this.output.get().isEmpty();
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(parta, partb, output.get());
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public static class AlloyingSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AlloyingCrafting>{

		@Override
		public AlloyingCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				CountIngredient ingredienta = CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a"));
				CountIngredient ingredientb = CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b"));
				Lazy<ItemStack> output = Utils.getItemStackWithTag(GsonHelper.getAsJsonObject(json, "output")).orElseThrow();
				
				int time = GsonHelper.getAsInt(json, "time");
				
				return new AlloyingCrafting(recipeId, ingredienta, ingredientb, output, time);
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public AlloyingCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CountIngredient inputa = CountIngredient.fromNetwork(buffer);
			CountIngredient inputb = CountIngredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int time = buffer.readInt();
			
			return new AlloyingCrafting(recipeId, inputa, inputb, Lazy.of(() -> output), time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlloyingCrafting recipe) {
			recipe.parta.toNetwork(buffer);
			recipe.partb.toNetwork(buffer);
			buffer.writeItem(recipe.output.get());
			buffer.writeInt(recipe.time);
			
		}
		
	}

	public static class AlloyResult implements FinishedRecipe{
		private final ResourceLocation rl;
		private final CountIngredient inputa;
		private final CountIngredient inputb;
		private final Pair<TagKey<Item>, Integer> output;
		private ConfigCondition condition = null;
		
		public AlloyResult(ResourceLocation rl, CountIngredient inputa, CountIngredient inputb, TagKey<Item> output, int outputcount) {
			this.rl = rl;
			this.inputa = inputa;
			this.inputb = inputb;
			this.output = Pair.of(output, outputcount);
		}
		
		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("part_a", inputa.toJson());
			json.add("part_b", inputb.toJson());
			json.addProperty("time", 100);
			
			JsonObject outputJson = new JsonObject();
			outputJson.addProperty("tag", output.getFirst().location().toString());
			if(output.getSecond() > 1) outputJson.addProperty("count", output.getSecond());
			json.add("output", outputJson);
			
			if(condition != null) {
				JsonArray conditions = new JsonArray();
				conditions.add(CraftingHelper.serialize(condition));
				json.add("conditions", conditions);
			}
		}
		
		public AlloyResult addIMCIfTrue(boolean check) {
			if(check) {
				this.condition = new ConfigCondition("alloyIMC", true);
			}
			return this;
		}
		
		@Override
		public ResourceLocation getId() {
			return rl;
		}
		
		@Override
		public RecipeSerializer<?> getType() {
			return SERIALIZER;
		}
		
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}
		
		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}
}
