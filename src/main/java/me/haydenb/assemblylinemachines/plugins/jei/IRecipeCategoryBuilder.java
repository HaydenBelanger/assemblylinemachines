package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
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
	
	default public List<FluidStack> getJEIFluidOutputs(){
		return null;
	}
	
	
	default public void setupSlots(IRecipeLayout layout, IGuiHelper helper, Optional<IIngredientRenderer<FluidStack>> renderer) {
		return;
	}
	
}
