package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.AlloyingCrafting;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.plugins.jei.JEIHelper;
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

public class AlloyingRecipeCategory implements IRecipeCategory<AlloyingCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public AlloyingRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 43, 92, 85, 41);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("alloy_smelter")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 91, 78, 16, 14).buildAnimated(200, StartDirection.LEFT, false);
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "alloying");
	}

	@Override
	public Class<? extends AlloyingCrafting> getRecipeClass() {
		return AlloyingCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Alloying Recipe";
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
	public void draw(AlloyingCrafting recipe, double mouseX, double mouseY) {
		progbar.draw(42, 13);
	}

	@Override
	public void setIngredients(AlloyingCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AlloyingCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 23, 0);
		gui.init(1, true, 23, 23);
		gui.init(2, true, 0, 11);
		gui.init(3, false, 63, 11);
		
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
