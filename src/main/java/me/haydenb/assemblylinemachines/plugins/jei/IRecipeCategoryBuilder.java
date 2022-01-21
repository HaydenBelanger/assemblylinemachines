package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.*;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public interface IRecipeCategoryBuilder {

	default public List<Ingredient> getJEIItemIngredients(){
		return null;
	}
	
	default public List<FluidStack> getJEIFluidInputs(){
		return null;
	}
	
	default public List<ItemStack> getJEIItemOutputs(){
		return null;
	}
	
	default public List<List<ItemStack>> getJEIItemOutputLists(){
		return null;
	}
	
	default public List<FluidStack> getJEIFluidOutputs(){
		return null;
	}
	
	
	default public void setupSlots(IRecipeLayout layout, IGuiHelper helper, RecipeCategoryBuilder category) {
		return;
	}
	
	default public Optional<Integer> getOutputCount(){
		return Optional.empty();
	}
	
	public static interface ICatalystProvider{
		
		default public ItemStack[] getCatalysts() {
			return null;
		}
	}
}
