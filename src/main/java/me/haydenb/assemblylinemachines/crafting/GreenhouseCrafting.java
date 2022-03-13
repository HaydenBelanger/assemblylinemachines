package me.haydenb.assemblylinemachines.crafting;

import java.util.Random;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.BlockGreenhouse;
import me.haydenb.assemblylinemachines.block.machines.BlockGreenhouse.*;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.StateProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GreenhouseCrafting implements Recipe<TEGreenhouse>, IRecipeCategoryBuilder {

	public static final RecipeType<GreenhouseCrafting> GREENHOUSE_RECIPE = new RecipeType<GreenhouseCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:greenhouse";
		}
	};
	
	public static final GreenhouseSerializer SERIALIZER = new GreenhouseSerializer();
	
	private final ResourceLocation id;
	private final Lazy<Ingredient> input;
	private final ItemStack baseOutput;
	private final int waterPerUnit;
	private final float base;
	private final float additional;
	private final int max;
	private final OutputScaling scaling;
	private final int processingPerUnit;
	private final Sprout sprout;
	private final Soil soil;
	
	public GreenhouseCrafting(ResourceLocation id, Lazy<Ingredient> input, ItemStack baseOutput, int waterPerUnit, float base, float additional, int max, OutputScaling scaling, int processingPerUnit, Sprout sprout, Soil soil) {
		this.id = id;
		this.input = input;
		this.baseOutput = baseOutput;
		this.waterPerUnit = waterPerUnit;
		this.base = base;
		this.additional = additional;
		this.max = max;
		this.scaling = scaling;
		this.processingPerUnit = processingPerUnit;
		this.sprout = sprout;
		this.soil = soil;
	}
	
	@Override
	public boolean matches(TEGreenhouse container, Level level) {
		if(input.get().test(container.getItem(1)) && soil.soil.get().test(container.getItem(3))) {
			if(soil.requiredSpecialization != null && container.getUpgradeAmount(soil.requiredSpecialization) == 0) return false;
			if(sprout.requiredSpecialization != null && container.getUpgradeAmount(sprout.requiredSpecialization) == 0) return false;
			if(sprout.sunlightReq != null) {
				if(sprout.sunlightReq.test(soil)) {
					if(container.getEffectiveLight() < 13) return false;
				}else {
					if(container.getEffectiveDarkness() > 6) return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public ItemStack assemble(TEGreenhouse container) {
		if(container.currentFertilizerRemaining <= 0) return ItemStack.EMPTY;
		int stackAmount = (baseOutput.getCount() + getAdditionalChance(container.getUpgradeAmount(Upgrades.MACHINE_EXTRA), container.getLevel().getRandom())) * container.currentFertilizerMultiplier;
		int actualWater = Math.min(4000, stackAmount * waterPerUnit);
		
		if(container.tank.getAmount() < actualWater) return ItemStack.EMPTY;
		
		int lightPenalty = 1;
		if(sprout.sunlightReq != null) {
			if(sprout.sunlightReq.test(soil)) {
				lightPenalty = 16 - container.getEffectiveLight();
			}else {
				lightPenalty = Math.max(1, container.getEffectiveDarkness() - 3);
			}
		}
		
		container.tank.shrink(actualWater);
		container.cycles = stackAmount * processingPerUnit * lightPenalty;
		container.currentFertilizerRemaining--;
		if(container.currentFertilizerRemaining <= 0) {
			container.currentFertilizerMax = 0;
			container.currentFertilizerMultiplier = 0;
		}
		
		ItemStack output = baseOutput.copy();
		container.getLevel().setBlockAndUpdate(container.getBlockPos(), container.getBlockState().setValue(BlockGreenhouse.SOIL, soil).setValue(BlockGreenhouse.SPROUT, sprout).setValue(StateProperties.MACHINE_ACTIVE, true));
		output.setCount(stackAmount);
		return output;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return baseOutput;
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
		return GREENHOUSE_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	private int getAdditionalChance(int upgrades, Random rand) {
		if(max == 0) return 0;
		if(scaling == OutputScaling.PURE_RANDOM) {
			float actual = base + (additional * upgrades);
			return rand.nextFloat() < actual ? Math.round(max * rand.nextFloat()) : 0;
		}else {
			int output = rand.nextFloat() < base ? 1 : 0;
			if(output != 0 && rand.nextFloat() < (additional * upgrades)) {
				output += Math.round((max - 1) * (upgrades / 3));
			}
			return output;
		}
	}

	public static class GreenhouseSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GreenhouseCrafting>{

		@Override
		public GreenhouseCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> input = Lazy.of(() ->  Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
				ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				int baseWater = GsonHelper.isValidNode(json, "waterPerUnit") ? GsonHelper.getAsInt(json, "waterPerUnit") : 250;
				float base = GsonHelper.isValidNode(json, "baseAdditionalChance") ? GsonHelper.getAsFloat(json, "baseAdditionalChance") : 0f;
				float additional = GsonHelper.isValidNode(json, "upgradeAdditionalChance") ? GsonHelper.getAsFloat(json, "upgradeAdditionalChance") : 0f;
				int max = GsonHelper.isValidNode(json, "maxAdditional") ? GsonHelper.getAsInt(json, "maxAdditional") : 1;
				OutputScaling scaling = GsonHelper.isValidNode(json, "additionalScaling") ? OutputScaling.valueOf(GsonHelper.getAsString(json, "additionalScaling").toUpperCase()) : OutputScaling.UPGRADE;
				int time = GsonHelper.getAsInt(json, "time");
				Soil soil = GsonHelper.isValidNode(json, "soil") ? Soil.valueOf(GsonHelper.getAsString(json, "soil").toUpperCase()) : Soil.DIRT;
				if(soil == Soil.EMPTY) throw new IllegalArgumentException("Soil cannot be EMPTY.");
				
				Sprout sprout = GsonHelper.isValidNode(json, "sprout") ? Sprout.valueOf(GsonHelper.getAsString(json, "sprout").toUpperCase()) : Sprout.SPROUT;
				if(sprout == Sprout.EMPTY) throw new IllegalArgumentException("Sprout cannot be EMPTY.");
				
				return new GreenhouseCrafting(recipeId, input, output, baseWater, base, additional, max, scaling, time, sprout, soil);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Greenhouse Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public GreenhouseCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int baseWater = buffer.readInt();
			float base = buffer.readFloat();
			float additional = buffer.readFloat();
			int max = buffer.readInt();
			OutputScaling scaling = buffer.readEnum(OutputScaling.class);
			int time = buffer.readInt();
			Sprout sprout = buffer.readEnum(Sprout.class);
			Soil soil = buffer.readEnum(Soil.class);
			
			return new GreenhouseCrafting(recipeId, Lazy.of(() -> input), output, baseWater, base, additional, max, scaling, time, sprout, soil);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, GreenhouseCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeItem(recipe.baseOutput);
			buffer.writeInt(recipe.waterPerUnit);
			buffer.writeFloat(recipe.base);
			buffer.writeFloat(recipe.additional);
			buffer.writeInt(recipe.max);
			buffer.writeEnum(recipe.scaling);
			buffer.writeInt(recipe.processingPerUnit);
			buffer.writeEnum(recipe.sprout);
			buffer.writeEnum(recipe.soil);
		}
		
	}
	
	public static enum OutputScaling{
		UPGRADE, PURE_RANDOM;
	}
}
