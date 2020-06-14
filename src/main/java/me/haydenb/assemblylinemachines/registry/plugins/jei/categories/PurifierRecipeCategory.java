package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.PurifierCrafting;
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

public class PurifierRecipeCategory implements IRecipeCategory<PurifierCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public PurifierRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 0, 153, 111, 44);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("electric_purifier")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 111, 153, 43, 32).buildAnimated(200, StartDirection.LEFT, false);
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "purifying");
	}

	@Override
	public Class<? extends PurifierCrafting> getRecipeClass() {
		return PurifierCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Purifier Recipe";
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
	public void draw(PurifierCrafting recipe, double mouseX, double mouseY) {
		progbar.draw(41, 6);
	}

	@Override
	public void setIngredients(PurifierCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, PurifierCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 21, 0);
		gui.init(1, true, 21, 26);
		gui.init(2, true, 42, 13);
		gui.init(3, false, 0, 13);
		gui.init(4, false, 89, 13);
		
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
