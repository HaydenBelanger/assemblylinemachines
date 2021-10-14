package me.haydenb.assemblylinemachines.registry.plugins.jei.categories;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.EntropyReactorCrafting;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.plugins.jei.JEIHelper;
import me.haydenb.assemblylinemachines.util.General;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EntropyReactorWasteCategory implements IRecipeCategory<EntropyReactorCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public EntropyReactorWasteCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 130, 24, 85, 41);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("entropy_reactor_core")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 199, 10, 16, 14).buildAnimated(100, StartDirection.LEFT, false);
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "entropy_reactor_output");
	}

	@Override
	public Class<? extends EntropyReactorCrafting> getRecipeClass() {
		return EntropyReactorCrafting.class;
	}

	@Override
	public Component getTitle() {
		return new TextComponent("Entropy Reactor Waste");
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
	public void draw(EntropyReactorCrafting recipe, PoseStack mx, double mouseX, double mouseY) {
		progbar.draw(mx, 42, 13);
		Minecraft mc = Minecraft.getInstance();
		String text;
		if(recipe.getVarietyReqd() < 0.01f) {
			text = "Any Variety";
		}else {
			text = " > " + String.format("%.0f%%", (recipe.getVarietyReqd() * 100f)) + " Variety";
		}
		General.drawCenteredStringWithoutShadow(mx, mc.font, new TextComponent(text), 42, 35, 0x800085);
		
		
	}
	

	@Override
	public void setIngredients(EntropyReactorCrafting recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredientsJEIFormatted());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, EntropyReactorCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 0, 11);
		gui.init(1, true, 23, 11);
		gui.init(2, false, 63, 11);
		
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
