package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
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
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;

public class RecipeCategoryBuilder {

	private final IGuiHelper helper;
	
	//Simple-concept builders.
	private ResourceLocation uid = null;
	private Component title = null;
	private IDrawable background = null;
	private IDrawable icon = null;
	private Pair<IDrawableAnimated, Pair<Integer, Integer>> progressBar = null;
	private List<Pair<Integer, Integer>> itemSlots = null;
	private int numberOfItemInputs = 0;
	private List<Pair<Integer, Integer>> fluidSlots = null;
	private int numberOfFluidInputs = 0;
	
	//More advanced concept functions and consumers - optional.
	private TriConsumer<Recipe<?>, PoseStack, Pair<Double, Double>> draw = null;
	private TriFunction<Recipe<?>, FluidStack, TooltipFlag, List<Component>> fluidTooltip = null;
	private BiFunction<Recipe<?>, List<Ingredient>, List<List<ItemStack>>> stackModifier = null;
	
	RecipeCategoryBuilder(IGuiHelper helper) {
		this.helper = helper;
	}
	
	public RecipeCategoryBuilder uid(ResourceLocation uid) {
		this.uid = uid;
		return this;
	}
	
	public RecipeCategoryBuilder uid(String uid) {
		this.uid = new ResourceLocation(AssemblyLineMachines.MODID, uid);
		return this;
	}
	
	public RecipeCategoryBuilder title(Component title) {
		this.title = title;
		return this;
	}
	
	public RecipeCategoryBuilder title(String title) {
		return this.title(new TextComponent(title));
	}
	
	public RecipeCategoryBuilder background(ResourceLocation guiPath, int u, int v, int width, int height) {
		this.background = helper.createDrawable(guiPath, u, v, width, height);
		return this;
	}
	
	public RecipeCategoryBuilder background(String guiPath, int u, int v, int width, int height) {
		return this.background(getGUIPath(guiPath), u, v, width, height);
	}
	
	public RecipeCategoryBuilder icon(Block b) {
		this.icon = helper.createDrawableIngredient(new ItemStack(b));
		return this;
	}
	
	public RecipeCategoryBuilder progressBar(ResourceLocation guiPath, int u, int v, int width, int height, int frameTime, StartDirection dir, boolean inverted, int drawX, int drawY) {
		this.progressBar = Pair.of(helper.drawableBuilder(guiPath, u, v, width, height).buildAnimated(frameTime, dir, inverted), Pair.of(drawX, drawY));
		return this;
	}
	
	public RecipeCategoryBuilder progressBar(String guiPath, int u, int v, int width, int height, int frameTime, StartDirection dir, boolean inverted, int drawX, int drawY) {
		return this.progressBar(getGUIPath(guiPath), u, v, width, height, frameTime, dir, inverted, drawX, drawY);
	}
	
	public RecipeCategoryBuilder draw(TriConsumer<Recipe<?>, PoseStack, Pair<Double, Double>> triConsumer) {
		this.draw = triConsumer;
		return this;
	}
	
	public RecipeCategoryBuilder fluidTooltip(TriFunction<Recipe<?>, FluidStack, TooltipFlag, List<Component>> tooltipFunction) {
		this.fluidTooltip = tooltipFunction;
		return this;
	}
	
	public RecipeCategoryBuilder itemStackModifier(BiFunction<Recipe<?>, List<Ingredient>, List<List<ItemStack>>> stackModifier) {
		this.stackModifier = stackModifier;
		return this;
	}
	
	@SafeVarargs
	public final RecipeCategoryBuilder itemSlots(int numberOfItemInputs, Pair<Integer, Integer>... slots) {
		this.numberOfItemInputs = numberOfItemInputs - 1;
		this.itemSlots = Arrays.asList(slots);
		return this;
	}
	
	@SafeVarargs
	public final RecipeCategoryBuilder fluidSlots(int numberOfFluidInputs, Pair<Integer, Integer>... slots) {
		this.numberOfFluidInputs = numberOfFluidInputs - 1;
		this.fluidSlots = Arrays.asList(slots);
		return this;
	}
	
	
	
	//Build
	public <R extends Recipe<?> & IRecipeCategoryBuilder> IRecipeCategory<R> build(Class<R> clazz){
		
		class ALMRecipeCategory implements IRecipeCategory<R>{

			@Override
			public ResourceLocation getUid() {
				return uid;
			}

			@Override
			public Class<? extends R> getRecipeClass() {
				return clazz;
			}

			@Override
			public Component getTitle() {
				return title;
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
			public void draw(R recipe, PoseStack stack, double mouseX, double mouseY) {
				if(progressBar != null) {
					progressBar.getFirst().draw(stack, progressBar.getSecond().getFirst(), progressBar.getSecond().getSecond());
				}
				
				if(draw != null) {
					draw.accept(recipe, stack, Pair.of(mouseX, mouseY));
				}
			}

			@Override
			public void setIngredients(R recipe, IIngredients ingredients) {
				
				if(recipe.getJEIItemIngredients() != null) {
					if(stackModifier != null) {
						ingredients.setInputLists(VanillaTypes.ITEM, stackModifier.apply(recipe, recipe.getJEIItemIngredients()));
					}else {
						ingredients.setInputIngredients(recipe.getJEIItemIngredients());
					}
				}
				
				if(recipe.getJEIFluidInputs() != null) {
					ingredients.setInputs(VanillaTypes.FLUID, recipe.getJEIFluidInputs());
				}
				
				if(recipe.getJEIItemOutputs() != null) {
					ingredients.setOutputs(VanillaTypes.ITEM, recipe.getJEIItemOutputs());
				}
				
				if(recipe.getJEIFluidOutputs() != null) {
					ingredients.setOutputs(VanillaTypes.FLUID, recipe.getJEIFluidOutputs());
				}
				
			}

			@Override
			public void setRecipe(IRecipeLayout recipeLayout, R recipe, IIngredients ingredients) {
				IGuiItemStackGroup igui = recipeLayout.getItemStacks();
				IGuiFluidStackGroup fgui = recipeLayout.getFluidStacks();
				int i = 0;
				if(itemSlots != null) {
					for(Pair<Integer, Integer> pair : itemSlots) {
						igui.init(i, i <= numberOfItemInputs, pair.getFirst(), pair.getSecond());
						i++;
					}
				}
				if(fluidSlots != null) {
					i = 0;
					
					for(Pair<Integer, Integer> pair : fluidSlots) {
						if(fluidTooltip != null) {
							fgui.init(i, i <= numberOfFluidInputs, getBasicFluidStackRenderer(recipe), pair.getFirst(), pair.getSecond(), 16, 16, 0, 0);
						}else {
							fgui.init(i, i <= numberOfFluidInputs, pair.getFirst(), pair.getSecond());
						}
						
						i++;
					}
				}
				
				if(fluidTooltip != null) {
					recipe.setupSlots(recipeLayout, helper, Optional.of(getBasicFluidStackRenderer(recipe)));
				}else {
					recipe.setupSlots(recipeLayout, helper, Optional.empty());
				}
				
				
				i = 0;
				for(List<ItemStack> it : Stream.of(ingredients.getInputs(VanillaTypes.ITEM), ingredients.getOutputs(VanillaTypes.ITEM)).flatMap(List::stream).collect(Collectors.toList())) {
					igui.set(i, it);
					i++;
				}
				
				i = 0;
				for(List<FluidStack> ft : Stream.of(ingredients.getInputs(VanillaTypes.FLUID), ingredients.getOutputs(VanillaTypes.FLUID)).flatMap(List::stream).collect(Collectors.toList())) {
					fgui.set(i, ft);
					i++;
				}
				
				
			}
			
		}
		
		return new ALMRecipeCategory();
	}
	
	private IIngredientRenderer<FluidStack> getBasicFluidStackRenderer(Recipe<?> recipe){
		return new IIngredientRenderer<FluidStack>() {
			@Override
			public void render(PoseStack stack, int xPosition, int yPosition, FluidStack ingredient) {
				FluidStack modIngredient = ingredient.copy();
				modIngredient.setAmount(1000);
				helper.createDrawableIngredient(modIngredient).draw(stack, xPosition, yPosition);
			}
			@Override
			public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
				return fluidTooltip.apply(recipe, ingredient, tooltipFlag);
			}
		};
	}
	
	public static ResourceLocation getGUIPath(String name) {
		return new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/jei/" + name + ".png");
	}

}
