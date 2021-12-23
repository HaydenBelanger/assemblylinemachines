package me.haydenb.assemblylinemachines.plugins.jei.categories;

import java.util.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.EnchantmentBookCrafting;
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

public class EnchantmentBookRecipeCategory implements IRecipeCategory<EnchantmentBookCrafting> {

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated progbar;
	public EnchantmentBookRecipeCategory(IGuiHelper helper) {
		background = helper.createDrawable(JEIHelper.getGUIPath("gui_set_a"), 172, 198, 84, 58);
		icon = helper.createDrawableIngredient(new ItemStack(Registry.getBlock("experience_mill")));
		progbar = helper.drawableBuilder(JEIHelper.getGUIPath("gui_set_a"), 228, 189, 28, 9).buildAnimated(120, StartDirection.LEFT, false);
	}
	
	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "enchantment_book");
	}

	@Override
	public Class<? extends EnchantmentBookCrafting> getRecipeClass() {
		return EnchantmentBookCrafting.class;
	}

	@Override
	public String getTitle() {
		return "Enchantment Crafting";
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
	public void draw(EnchantmentBookCrafting recipe, double mouseX, double mouseY) {
		progbar.draw(24, 20);
	}
	

	@Override
	public void setIngredients(EnchantmentBookCrafting recipe, IIngredients ingredients) {
		
		List<List<ItemStack>> inputs = new ArrayList<>();
		
		List<ItemStack> input = new ArrayList<>();
		for(ItemStack ig : recipe.getIngredients().get(1).getMatchingStacks()) {
			
			ig.setCount(recipe.getAmount());
			input.add(ig);
		}
		
		inputs.add(input);
		
		
		inputs.add(Arrays.asList(recipe.getIngredients().get(0).getMatchingStacks()));
		
		inputs.add(Arrays.asList(new ItemStack[] {new ItemStack(Registry.getItem("experience_mill"))}));
		
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, EnchantmentBookCrafting recipe, IIngredients ingredients) {
		IGuiItemStackGroup gui = recipeLayout.getItemStacks();
		gui.init(0, true, 1, 1);
		gui.init(1, true, 1, 30);
		gui.init(2, true, 61, 39);
		gui.init(3, false, 61, 16);
		
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
