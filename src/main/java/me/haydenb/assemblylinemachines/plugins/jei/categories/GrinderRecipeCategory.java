package me.haydenb.assemblylinemachines.plugins.jei.categories;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.plugins.jei.JEIHelper;
import me.haydenb.assemblylinemachines.registry.Registry;
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

public class GrinderRecipeCategory implements IRecipeCategory<GrinderCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public GrinderRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 166, 145, 90, 44);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("electric_grinder")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 237, 131, 19, 14).buildAnimated(200, StartDirection.LEFT, false);
		
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "grinding");
	}

	@Override
	public Class<? extends GrinderCrafting> getRecipeClass() {
		return GrinderCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Grinding Recipe";
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
	public void draw(GrinderCrafting recipe, MatrixStack mx, double mouseX, double mouseY) {
		progbar.draw(mx, 42, 24);
	}

	@Override
	public void setIngredients(GrinderCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, GrinderCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 21, 22);
		gui.init(1, false, 0, 22);
		gui.init(2, false, 0, 0);
		gui.init(3, false, 68, 22);
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
