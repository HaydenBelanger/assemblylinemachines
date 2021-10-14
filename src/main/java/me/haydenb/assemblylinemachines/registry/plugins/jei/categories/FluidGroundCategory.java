package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
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
	public Component getTitle() {
		return new TextComponent("Pump Output");
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
	public void draw(FluidInGroundRecipe recipe, PoseStack mx, double mouseX, double mouseY) {
		
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
		ingredients.setInputIngredients(Arrays.asList(new Ingredient[] {Ingredient.of(Registry.getItem("pump"), Registry.getItem("pumpshaft"))}));
		ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(recipe.getFluid(), 1000));
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FluidInGroundRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		
		IGuiFluidStackGroup fgui = recipeLayout.getFluidStacks();
		gui.init(0, false, 57, 12);
		fgui.init(0, false, new IIngredientRenderer<FluidStack>() {
			
			@Override
			public void render(PoseStack mx, int xPosition, int yPosition, FluidStack ingredient) {
				
				helper.createDrawableIngredient(ingredient).draw(mx, xPosition, yPosition);
			}
			
			@Override
			public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
				
				ArrayList<Component> arr = new ArrayList<>();
				
				arr.add(ingredient.getDisplayName());
				if(recipe.getCriteria() == FluidInGroundCriteria.END) {
					arr.add(new TextComponent("Found in The End.").withStyle(ChatFormatting.DARK_PURPLE));
				}else if(recipe.getCriteria() == FluidInGroundCriteria.NETHER) {
					arr.add(new TextComponent("Found in The Nether.").withStyle(ChatFormatting.DARK_RED));
				}else {
					arr.add(new TextComponent("Found in The Overworld.").withStyle(ChatFormatting.DARK_GREEN));
					if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYCOLD) {
						arr.add(new TextComponent("Only in very cold biomes.").withStyle(ChatFormatting.BLUE));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFCOLD) {
						arr.add(new TextComponent("Favors very cold biomes.").withStyle(ChatFormatting.BLUE));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFHOT) {
						arr.add(new TextComponent("Favors very hot biomes.").withStyle(ChatFormatting.RED));
					}else if(recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYHOT) {
						arr.add(new TextComponent("Only in very hot biomes.").withStyle(ChatFormatting.RED));
					}
				}
				
				arr.add(new TextComponent(recipe.getChance() + "% chance to generate.").withStyle(ChatFormatting.YELLOW));
				
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
