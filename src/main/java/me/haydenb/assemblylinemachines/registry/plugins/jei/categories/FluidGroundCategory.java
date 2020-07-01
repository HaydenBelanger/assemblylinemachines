package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.*;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.plugins.jei.JEIHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fluids.FluidStack;

public class FluidGroundCategory implements IRecipeCategory<FluidInGroundRecipe> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable progbarsand;
	private final IDrawable progbarendstone;
	private final IDrawable progbardirt;
	private final IDrawable progbarice;
	private final IDrawable progbarnetherrack;
	private final IGuiHelper helper;
	public FluidGroundCategory(IGuiHelper helper) {
		this.helper = helper;
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_oil"), 0, 0, 132, 99);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("pump")));
		progbarnetherrack = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_oil"), 0, 99, 34, 34).build();
		progbarice = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_oil"), 34, 99, 34, 34).build();
		progbarendstone = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_oil"), 68, 99, 34, 34).build();
		progbarsand = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_oil"), 102, 99, 34, 34).build();
		progbardirt = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_oil"), 0, 133, 34, 34).build();
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "fig");
	}

	@Override
	public Class<? extends FluidInGroundRecipe> getRecipeClass() {
		return FluidInGroundRecipe.class;
	}

	@Override
	public String getTitle() {
		return "Pumpable Ground Fluid";
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
	public void draw(FluidInGroundRecipe recipe, MatrixStack mx, double mouseX, double mouseY) {
		
		IDrawable draw;
		if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYCOLD || recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFCOLD) {
			draw = progbarice;
		}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYHOT || recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFHOT) {
			draw = progbarsand;
		}else if(recipe.getCriteria() == FluidInGroundCriteria.END) {
			draw = progbarendstone;
		}else if(recipe.getCriteria() == FluidInGroundCriteria.NETHER) {
			draw = progbarnetherrack;
		}else {
			draw = progbardirt;
		}
		
		draw.draw(mx, 49, 57);
	}

	@Override
	public void setIngredients(FluidInGroundRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(Arrays.asList(new Ingredient[] {Ingredient.fromItems(Registry.getItem("pump"), Registry.getItem("pumpshaft"))}));
		ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(recipe.getFluid(), 1000));
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FluidInGroundRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		
		IGuiFluidStackGroup fgui = recipeLayout.getFluidStacks();
		gui.init(0, false, 57, 12);
		fgui.init(0, false, new IIngredientRenderer<FluidStack>() {
			
			@Override
			public void render(MatrixStack mx, int xPosition, int yPosition, FluidStack ingredient) {
				
				helper.createDrawableIngredient(ingredient).draw(mx, xPosition, yPosition);
			}
			
			@Override
			public List<ITextComponent> getTooltip(FluidStack ingredient, ITooltipFlag tooltipFlag) {
				
				ArrayList<ITextComponent> arr = new ArrayList<>();
				
				arr.add(ingredient.getDisplayName().func_230532_e_());
				if(recipe.getCriteria() == FluidInGroundCriteria.END) {
					arr.add(new StringTextComponent("Found in The End.").func_230532_e_().func_240699_a_(TextFormatting.DARK_PURPLE));
				}else if(recipe.getCriteria() == FluidInGroundCriteria.NETHER) {
					arr.add(new StringTextComponent("Found in The Nether.").func_230532_e_().func_240699_a_(TextFormatting.DARK_RED));
				}else {
					arr.add(new StringTextComponent("Found in The Overworld.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GREEN));
					if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYCOLD) {
						arr.add(new StringTextComponent("Only in very cold biomes.").func_230532_e_().func_240699_a_(TextFormatting.BLUE));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFCOLD) {
						arr.add(new StringTextComponent("Favors very cold biomes.").func_230532_e_().func_240699_a_(TextFormatting.BLUE));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFHOT) {
						arr.add(new StringTextComponent("Favors very hot biomes.").func_230532_e_().func_240699_a_(TextFormatting.RED));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYHOT) {
						arr.add(new StringTextComponent("Only in very hot biomes.").func_230532_e_().func_240699_a_(TextFormatting.RED));
					}
				}
				
				arr.add(new StringTextComponent(recipe.getChance() + "% chance to generate.").func_230532_e_().func_240699_a_(TextFormatting.YELLOW));
				
				return arr;
			}
		}, 58, 66, 16, 16, 0, 0);
		
		for(List<ItemStack> it : ingredients.getInputs(VanillaTypes.ITEM)) {
			gui.set(0, it);
		}
		for(List<FluidStack> f : ingredients.getOutputs(VanillaTypes.FLUID)) {
			
			fgui.set(0, f);
		}
	}

}
