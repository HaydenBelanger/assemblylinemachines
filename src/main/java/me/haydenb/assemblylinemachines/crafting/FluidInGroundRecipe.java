package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidInGroundRecipe implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<FluidInGroundRecipe> FIG_RECIPE = new RecipeType<FluidInGroundRecipe>() {
		@Override
		public String toString() {
			return "assemblylinemachines:fluid_in_ground";
		}
	};
	
	public static final FluidInGroundSerializer SERIALIZER = new FluidInGroundSerializer();
	
	private final Fluid fluid;
	private final ResourceLocation id;
	public int odds;
	private int minAmount;
	private int maxAmount;
	public FluidInGroundCriteria criteria;
	
	public FluidInGroundRecipe(ResourceLocation id, Fluid fluid, int odds, int minAmount, int maxAmount, FluidInGroundCriteria figc) {
		
		this.fluid = fluid;
		this.id = id;
		this.odds = odds;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.criteria = figc;
	}
	
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
		
		return true;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
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
		return FIG_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<FluidStack> getJEIFluidOutputs() {
		return List.of(new FluidStack(fluid, 1000));
	}
	
	public static FluidStack assemble(ChunkPos pos, Level world) {
		List<FluidInGroundRecipe> recipes = world.getRecipeManager().getAllRecipesFor(FluidInGroundRecipe.FIG_RECIPE);
		
		float tc = world.getBiome(pos.getMiddleBlockPosition(63)).value().getBaseTemperature();
		ResourceLocation currentDim = world.dimension().location();
		
		for (FluidInGroundRecipe recipe : recipes) {

			boolean canProcess = false;
			boolean notPreferredBiome = false;
			
			switch(recipe.criteria) {
			case END:
				if(currentDim.equals(DimensionType.END_LOCATION.location())) canProcess = true;
				break;
			case NETHER:
				if(currentDim.equals(DimensionType.NETHER_LOCATION.location())) canProcess = true;
				break;
			case OVERWORLD_ANY, OVERWORLD_ONLYCOLD, OVERWORLD_ONLYHOT, OVERWORLD_PREFCOLD, OVERWORLD_PREFHOT:
				Pair<Boolean, Boolean> pair = getAllowanceForOverworld(recipe.criteria, currentDim, tc);
				if(pair.getFirst()) canProcess = true;
				if(pair.getSecond()) notPreferredBiome = true;
			}
			if(canProcess) {
				if (world.getRandom().nextInt(100) <= recipe.odds) {
					int amt = (recipe.minAmount + world.getRandom().nextInt(recipe.maxAmount - recipe.minAmount)) * 10000;
					if (notPreferredBiome) amt = Math.round((float) amt / 2f);
					return new FluidStack(recipe.fluid, amt);
				}
			}
		}

		return FluidStack.EMPTY;
	}
	
	private static Pair<Boolean, Boolean> getAllowanceForOverworld(FluidInGroundCriteria criteria, ResourceLocation currentDim, float temperature){
		if(!currentDim.equals(DimensionType.OVERWORLD_LOCATION.location())) return Pair.of(false, false);
		switch(criteria) {
		case OVERWORLD_ONLYCOLD:
		case OVERWORLD_PREFCOLD:
			float maximumTempOffset = criteria == FluidInGroundCriteria.OVERWORLD_ONLYCOLD ? 0f : 1f;
			if(temperature > maximumTempOffset) {
				return Pair.of(false, false);
			}else if(criteria == FluidInGroundCriteria.OVERWORLD_PREFCOLD && temperature > 0f) {
				return Pair.of(true, true);
			}
			break;
		case OVERWORLD_ONLYHOT:
		case OVERWORLD_PREFHOT:
			float minimumTempOffset = criteria == FluidInGroundCriteria.OVERWORLD_ONLYHOT ? 1f : 0f;
			if(temperature < minimumTempOffset) {
				return Pair.of(false, false);
			}else if(criteria == FluidInGroundCriteria.OVERWORLD_PREFHOT && temperature < 1f) {
				return Pair.of(true, true);
			}
		default:
		}
		return Pair.of(true, false);
	}
	
	public static class FluidInGroundSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<FluidInGroundRecipe>{

		@Override
		public FluidInGroundRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
				if(f == null) {
					throw new IllegalArgumentException("Could not find this fluid.");
				}
				
				int odds = GsonHelper.getAsInt(json, "chance") - 1;
				if(odds < 0 || odds > 99) {
					throw new IllegalArgumentException("Chance must be more than 0 and less than 101.");
				}
				int min = GsonHelper.getAsInt(json, "min");
				int max = GsonHelper.getAsInt(json, "max");
				FluidInGroundCriteria figc = FluidInGroundCriteria.valueOf(GsonHelper.getAsString(json, "criteria_set").toUpperCase());
				return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Fluid-In-Ground Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public FluidInGroundRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Fluid f = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			int odds = buffer.readInt();
			int min = buffer.readInt();
			int max = buffer.readInt();
			FluidInGroundCriteria figc = buffer.readEnum(FluidInGroundCriteria.class);
			
			return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FluidInGroundRecipe recipe) {
			buffer.writeResourceLocation(recipe.fluid.getRegistryName());
			buffer.writeInt(recipe.odds);
			buffer.writeInt(recipe.minAmount);
			buffer.writeInt(recipe.maxAmount);
			buffer.writeEnum(recipe.criteria);
		}
		
	}
	
	public static enum FluidInGroundCriteria{
		OVERWORLD_PREFCOLD(68, 111, true, new TextComponent("§9Favors very cold biomes.")), OVERWORLD_PREFHOT(102, 77, true, new TextComponent("§cFavors very hot biomes.")), 
		OVERWORLD_ONLYCOLD(68, 111, true, new TextComponent("§9Only in very cold biomes.")), OVERWORLD_ONLYHOT(102, 77, true, new TextComponent("§cOnly in very hot biomes.")),
		OVERWORLD_ANY(0, 111, false, new TextComponent("§2Found in The Overworld.")), NETHER(34, 111, false, new TextComponent("§4Found in The Nether.")), 
		END(68, 77, false, new TextComponent("§5Found in The End."));
		
		private final int jeiBlitX, jeiBlitY;
		private final TextComponent descriptor;
		private final boolean isOverworld;
		
		FluidInGroundCriteria(int jeiBlitX, int jeiBlitY, boolean isOverworld, TextComponent descriptor){
			this.jeiBlitX = jeiBlitX;
			this.jeiBlitY = jeiBlitY;
			this.descriptor = descriptor;
			this.isOverworld = isOverworld;
		}
		public int getJeiBlitX() {
			return jeiBlitX;
		}
		
		public int getJeiBlitY() {
			return jeiBlitY;
		}
		
		public List<Component> getTooltip(int chanceToGenerate){
			
			if(isOverworld) {
				return List.of(OVERWORLD_ANY.descriptor, this.descriptor, new TextComponent("§e" + chanceToGenerate + "% chance to generate."));
			}else {
				return List.of(this.descriptor, new TextComponent("§e" + chanceToGenerate + "% chance to generate."));
			}
		}
	}
	
}
