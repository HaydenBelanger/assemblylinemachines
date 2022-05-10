package me.haydenb.assemblylinemachines.crafting;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ConfigCondition;
import me.haydenb.assemblylinemachines.registry.utils.*;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PneumaticCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<PneumaticCrafting> PNEUMATIC_RECIPE = new RecipeType<PneumaticCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:pneumatic";
		}
	};
	
	public static final PneumaticSerializer SERIALIZER = new PneumaticSerializer();
	
	private final CountIngredient input;
	private final Lazy<ItemStack> output;
	public final int time;
	private final ResourceLocation id;
	public final Item mold;
	
	public PneumaticCrafting(ResourceLocation id, CountIngredient input, Lazy<ItemStack> outputa, int time, Item mold) {
		this.id = id;
		this.input = input;
		this.output = outputa;
		this.time = time;
		this.mold = mold;
	}
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(!this.showInJEI()) return false;
		if(input.test(inv.getItem(1))) {
			return !mold.equals(Items.AIR) ? mold.equals(inv.getItem(2).getItem()) : inv.getItem(2).isEmpty();
		}
		return false;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if(inv instanceof IMachineDataBridge) {
			inv.getItem(1).shrink(input.getCount());
			((IMachineDataBridge) inv).setCycles(time);
		}
		return output.get().copy();
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
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public boolean showInJEI() {
		return !this.input.isEmpty() && !this.output.get().isEmpty();
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(!mold.equals(Items.AIR) ? mold.getDefaultInstance() : ItemStack.EMPTY, input, output.get());
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
		return PNEUMATIC_RECIPE;
	}
	
	public static class PneumaticSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PneumaticCrafting>{

		@Override
		public PneumaticCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				CountIngredient input = CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				Lazy<ItemStack> output = Utils.getItemStackWithTag(GsonHelper.getAsJsonObject(json, "output")).orElseThrow();
				
				int time = GsonHelper.getAsInt(json, "time");
				Mold mold = GsonHelper.isValidNode(json, "mold") ? Mold.valueOf(GsonHelper.getAsString(json, "mold").toUpperCase()) : Mold.NONE;
				Item moldItem = GsonHelper.isValidNode(json, "moldItem") ? ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "moldItem"))) : mold.item.orElse(Items.AIR);
				
				return new PneumaticCrafting(recipeId, input, output, time, moldItem);
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public PneumaticCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CountIngredient input = CountIngredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int time = buffer.readInt();
			Item mold = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
			
			return new PneumaticCrafting(recipeId, input, Lazy.of(() -> output), time, mold);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PneumaticCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeItem(recipe.output.get());
			buffer.writeInt(recipe.time);
			buffer.writeResourceLocation(recipe.mold.getRegistryName());
		}
		
	}
	
	public static enum Mold{
		PLATE(Registry.getItem("plate_mold")), ROD(Registry.getItem("rod_mold")), GEAR(Registry.getItem("gear_mold")), NONE(null);
		
		final Optional<Item> item;
		
		Mold(Item item){
			this.item = Optional.ofNullable(item);
		}
	}

	public static class PneumaticResult implements FinishedRecipe{
	
		private final ResourceLocation rl;
		private final CountIngredient input;
		private final Pair<TagKey<Item>, Integer> output;
		private final int time;
		private final Mold mold;
		private ConfigCondition condition = null;
		
		public PneumaticResult(ResourceLocation rl, CountIngredient input, TagKey<Item> output, int outputCount, Mold mold, int time) {
			this.rl = rl;
			this.input = input;
			this.output = Pair.of(output, outputCount);
			this.time = time;
			this.mold = mold;
		}
		
		@Override
		public void serializeRecipeData(JsonObject json) {
			
			json.add("input", input.toJson());
			
			JsonObject outputJson = new JsonObject();
			outputJson.addProperty("tag", output.getFirst().location().toString());
			if(output.getSecond() > 1) outputJson.addProperty("count", output.getSecond());
			
			json.add("output", outputJson);
			json.addProperty("time", time);
			if(mold != Mold.NONE) json.addProperty("mold", mold.toString().toLowerCase());
			
			if(condition != null) {
				JsonArray conditions = new JsonArray();
				conditions.add(CraftingHelper.serialize(condition));
				json.add("conditions", conditions);
			}
		}
		
		public PneumaticResult addIMCIfTrue(boolean check, String varName) {
			if(check) {
				this.condition = new ConfigCondition(varName, true);
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
