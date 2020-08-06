package me.haydenb.assemblylinemachines.plugins.jei.categories;

import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.plugins.jei.JEIHelper;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
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
	private final IGuiHelper helper;
	
	private HashMap<BathCraftingFluids, IDrawableAnimated> bars = new HashMap<>();
	
	public FluidBathRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 0, 197, 87, 41);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("electric_fluid_mixer")));
		this.helper = helper;
		
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
	public void draw(BathCrafting recipe, MatrixStack mx, double mouseX, double mouseY) {
		IDrawableAnimated anim = bars.get(recipe.getFluid());
		if(anim == null) {
			anim = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), recipe.getFluid().getJeiBlitPiece().getFirst(), recipe.getFluid().getJeiBlitPiece().getSecond(), 15, 16).buildAnimated(200, StartDirection.LEFT, false);
			
			bars.put(recipe.getFluid(), anim);
		}
		anim.draw(mx, 42, 5);
		
	}

	@Override
	public void setIngredients(BathCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BathCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 0, 4);
		gui.init(1, true, 21, 4);
		gui.init(2, false, 41, 23);
		gui.init(3, false, 65, 4);
		
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
