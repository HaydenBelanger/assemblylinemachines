package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.plugins.jei.JEIHelper;
import me.haydenb.assemblylinemachines.util.FluidProperty.Fluids;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FluidBathRecipeCategory implements IRecipeCategory<BathCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbarlava;
	private final IDrawableAnimated progbarwater;
	public FluidBathRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 0, 68, 43, 85);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("fluid_bath")));
		progbarlava = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 67, 68, 24, 24).buildAnimated(200, StartDirection.BOTTOM, false);
		progbarwater = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 43, 68, 24, 24).buildAnimated(200, StartDirection.BOTTOM, false);
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "bathing");
	}

	@Override
	public Class<? extends BathCrafting> getRecipeClass() {
		return BathCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Fluid Bath Recipe";
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}
	
	@Override
	public void draw(BathCrafting recipe, double mouseX, double mouseY) {
		if(recipe.getFluid() == Fluids.LAVA) {
			progbarlava.draw(9, 20);
		}else {
			progbarwater.draw(9, 20);
		}
	}

	@Override
	public void setIngredients(BathCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BathCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 0, 48);
		gui.init(1, true, 25, 48);
		gui.init(2, false, 0, 67);
		gui.init(3, false, 12, 0);
		
		int i = 0;
		for(List<ItemStack> it : ingredients.getInputs(VanillaTypes.ITEM)) {
			gui.set(i, it);
			i++;
		}
		for(List<ItemStack> it : ingredients.getOutputs(VanillaTypes.ITEM)) {
			gui.set(i, it);
			i++;
		}
	}

}
