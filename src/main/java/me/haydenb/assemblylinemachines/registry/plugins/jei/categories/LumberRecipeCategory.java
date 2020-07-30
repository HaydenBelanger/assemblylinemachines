package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.LumberCrafting;
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

public class LumberRecipeCategory implements IRecipeCategory<LumberCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public LumberRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 117, 185, 73, 50);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("lumber_mill")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 190, 185, 19, 5).buildAnimated(200, StartDirection.LEFT, false);
		
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "lumber");
	}

	@Override
	public Class<? extends LumberCrafting> getRecipeClass() {
		return LumberCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Lumber Mill Recipe";
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
	public void draw(LumberCrafting recipe, MatrixStack mx, double mouseX, double mouseY) {
		progbar.draw(mx, 23, 13);
	}

	@Override
	public void setIngredients(LumberCrafting recipe, IIngredients ingredients) {
		
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(new ItemStack[] {recipe.getRecipeOutput(), recipe.getSecondaryOutput()}));
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, LumberCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 2, 6);
		gui.init(1, false, 2, 30);
		gui.init(2, false, 49, 6);
		gui.init(3, false, 49, 30);
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
