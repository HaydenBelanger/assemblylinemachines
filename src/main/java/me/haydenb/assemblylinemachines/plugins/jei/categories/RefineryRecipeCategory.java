package me.haydenb.assemblylinemachines.plugins.jei.categories;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.RefiningCrafting;
import me.haydenb.assemblylinemachines.plugins.jei.JEIHelper;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fluids.FluidStack;

public class RefineryRecipeCategory implements IRecipeCategory<RefiningCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	private final IGuiHelper helper;
	public RefineryRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 41, 24, 82, 41);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("refinery")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 123, 24, 7, 3).buildAnimated(400, StartDirection.TOP, false);
		this.helper = helper;
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "refining");
	}

	@Override
	public Class<? extends RefiningCrafting> getRecipeClass() {
		return RefiningCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Refinery Recipe";
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
	public void draw(RefiningCrafting recipe, MatrixStack mx, double mouseX, double mouseY) {
		progbar.draw(mx, 35, 19);
		progbar.draw(mx, 44, 19);
		progbar.draw(mx, 55, 19);
		progbar.draw(mx, 64, 19);
	}

	@Override
	public void setIngredients(RefiningCrafting recipe, IIngredients ingredients) {
		
		List<Ingredient> inputItems = new ArrayList<>();
		inputItems.add(Ingredient.fromItems(Item.BLOCK_TO_ITEM.get(recipe.attachmentBlock)));
		inputItems.add(Ingredient.fromItems(Registry.getItem("refinery")));
		if(!recipe.itemInput.getFirst().hasNoMatchingItems()) {
			inputItems.add(recipe.itemInput.getFirst());
		}
		ingredients.setInputIngredients(inputItems);
		
		if(!recipe.fluidInput.getFirst().isEmpty()) {
			ingredients.setInput(VanillaTypes.FLUID, recipe.fluidInput.getFirst());
		}
		
		if(!recipe.fluidOutputA.getFirst().isEmpty()) {
			List<FluidStack> fluidOutputs = new ArrayList<>();
			
			fluidOutputs.add(recipe.fluidOutputA.getFirst());
			
			if(!recipe.fluidOutputB.getFirst().isEmpty()) {
				fluidOutputs.add(recipe.fluidOutputB.getFirst());
			}
			
			ingredients.setOutputs(VanillaTypes.FLUID, fluidOutputs);
		}
		
		if(!recipe.itemOutput.getFirst().isEmpty()) {
			ingredients.setOutput(VanillaTypes.ITEM, recipe.itemOutput.getFirst());
		}
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RefiningCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		IGuiFluidStackGroup fgui = recipeLayout.getFluidStacks();
		RefineryFluidIngredientRenderer renderer = new RefineryFluidIngredientRenderer();
		int itemctr = 2;
		int flcounter = 0;
		gui.init(0, false, 0, 0);
		gui.init(1, false, 0, 23);
		if(!recipe.fluidInput.getFirst().isEmpty()) {
			fgui.init(flcounter, false, renderer, 35, 1, 16, 16, 0, 0);
			flcounter++;
			if(!recipe.itemInput.getFirst().hasNoMatchingItems()) {
				gui.init(itemctr, false, 54, 0);
				itemctr++;
			}
		}else {
			gui.init(itemctr, false, 34, 0);
			itemctr++;
		}
		
		int offset = 0;
		if(!recipe.fluidOutputA.getFirst().isEmpty()) {
			fgui.init(flcounter, false, renderer, 25, 24, 16, 16, 0, 0);
			offset += 20;
			flcounter++;
			if(!recipe.fluidOutputB.getFirst().isEmpty()) {
				fgui.init(flcounter, false, renderer, 45, 24, 16, 16, 0, 0);
				offset += 20;
				flcounter++;
			}
		}
		
		if(!recipe.itemOutput.getFirst().isEmpty()) {
			gui.init(itemctr, false, 24 + offset, 23);
			itemctr++;
		}
		
		int ii = 0;
		for(List<ItemStack> it : ingredients.getInputs(VanillaTypes.ITEM)) {
			gui.set(ii, it);
			ii++;
		}
		for(List<ItemStack> it : ingredients.getOutputs(VanillaTypes.ITEM)) {
			gui.set(ii, it);
			ii++;
		}	
		
		
		ii = 0;
		for(List<FluidStack> it : ingredients.getInputs(VanillaTypes.FLUID)) {
			fgui.set(ii, it);
			ii++;
		}
		
		for(List<FluidStack> it : ingredients.getOutputs(VanillaTypes.FLUID)) {
			fgui.set(ii, it);
			ii++;
		}
		
	}
	
	private class RefineryFluidIngredientRenderer implements IIngredientRenderer<FluidStack>{

		@Override
		public void render(MatrixStack mx, int xPosition, int yPosition, FluidStack ingredient) {
			helper.createDrawableIngredient(new FluidStack(ingredient.getFluid(), 1000)).draw(mx, xPosition, yPosition);
			
		}

		@Override
		public List<ITextComponent> getTooltip(FluidStack ingredient, ITooltipFlag tooltipFlag) {
			List<ITextComponent> tooltip = new ArrayList<>();
			tooltip.add(ingredient.getDisplayName().func_230532_e_());
			tooltip.add(new StringTextComponent(Formatting.GENERAL_FORMAT.format(ingredient.getAmount()) + " mB").func_230532_e_().func_240699_a_(TextFormatting.AQUA));
			
			
			return tooltip;
		}
		
	}	

}
