package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidInGroundRecipe implements Recipe<Container>{

	
	public static final RecipeType<FluidInGroundRecipe> FIG_RECIPE = new TypeFluidInGroundRecipe();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Fluid fluid;
	private final ResourceLocation id;
	private int odds;
	private int minAmount;
	private int maxAmount;
	private FluidInGroundCriteria figc;
	
	public FluidInGroundRecipe(ResourceLocation id, Fluid fluid, int odds, int minAmount, int maxAmount, FluidInGroundCriteria figc) {
		
		this.fluid = fluid;
		this.id = id;
		this.odds = odds;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.figc = figc;
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
	
	public int getChance() {
		return odds;
	}
	
	public FluidInGroundCriteria getCriteria() {
		return figc;
	}
	
	public int getMinimum() {
		return minAmount;
	}
	
	public int getMaximum() {
		return maxAmount;
	}
	
	public Fluid getFluid() {
		return fluid;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<FluidInGroundRecipe>{

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
			buffer.writeResourceLocation(recipe.getFluid().getRegistryName());
			buffer.writeInt(recipe.getChance());
			buffer.writeInt(recipe.getMinimum());
			buffer.writeInt(recipe.getMaximum());
			buffer.writeEnum(recipe.getCriteria());
		}
		
	}
	
	public static class TypeFluidInGroundRecipe implements RecipeType<FluidInGroundRecipe>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:fluid_in_ground";
		}
		
	}
	
	public static enum FluidInGroundCriteria{
		OVERWORLD_PREFCOLD, OVERWORLD_PREFHOT, OVERWORLD_ONLYCOLD, OVERWORLD_ONLYHOT, OVERWORLD_ANY, NETHER, END;
	}
	
}
