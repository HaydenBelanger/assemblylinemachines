package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidInGroundRecipe implements IRecipe<IInventory>{

	
	public static final IRecipeType<FluidInGroundRecipe> FIG_RECIPE = new TypeFluidInGroundRecipe();
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
	public boolean matches(IInventory inv, World worldIn) {
		
		return true;
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public IRecipeType<?> getType() {
		return FIG_RECIPE;
	}
	
	@Override
	public boolean isDynamic() {
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
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FluidInGroundRecipe>{

		@Override
		public FluidInGroundRecipe read(ResourceLocation recipeId, JsonObject json) {
			try {
				Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(JSONUtils.getString(json, "fluid")));
				if(f == null) {
					throw new IllegalArgumentException("Could not find this fluid.");
				}
				
				int odds = JSONUtils.getInt(json, "chance") - 1;
				if(odds < 0 || odds > 99) {
					throw new IllegalArgumentException("Chance must be more than 0 and less than 101.");
				}
				int min = JSONUtils.getInt(json, "min");
				int max = JSONUtils.getInt(json, "max");
				FluidInGroundCriteria figc = FluidInGroundCriteria.valueOf(JSONUtils.getString(json, "criteria_set").toUpperCase());
				return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Fluid-In-Ground Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public FluidInGroundRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			Fluid f = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			int odds = buffer.readInt();
			int min = buffer.readInt();
			int max = buffer.readInt();
			FluidInGroundCriteria figc = buffer.readEnumValue(FluidInGroundCriteria.class);
			
			return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
		}

		@Override
		public void write(PacketBuffer buffer, FluidInGroundRecipe recipe) {
			buffer.writeResourceLocation(recipe.getFluid().getRegistryName());
			buffer.writeInt(recipe.getChance());
			buffer.writeInt(recipe.getMinimum());
			buffer.writeInt(recipe.getMaximum());
			buffer.writeEnumValue(recipe.getCriteria());
		}
		
	}
	
	public static class TypeFluidInGroundRecipe implements IRecipeType<FluidInGroundRecipe>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:fluid_in_ground";
		}
		
	}
	
	public static enum FluidInGroundCriteria{
		OVERWORLD_PREFCOLD, OVERWORLD_PREFHOT, OVERWORLD_ONLYCOLD, OVERWORLD_ONLYHOT, OVERWORLD_ANY, NETHER, END;
	}
	
}
