package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.utils.CountIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

public class RecipeCategoryBuilder {

	private final IGuiHelper helper;
	private final ResourceLocation uid;
	private final Component title;

	private IDrawable background = null;
	private IDrawable icon = null;
	private Pair<IDrawableAnimated, Pair<Integer, Integer>> progressBar = null;
	private TriConsumer<Recipe<?>, PoseStack, Pair<Double, Double>> draw = null;
	private List<ItemStack> catalysts = List.of();

	private List<Pair<Integer, Integer>> slots = null;
	private Function<Integer, RecipeIngredientRole> role = null;
	private TriConsumer<Recipe<?>, Object, List<Component>> tooltip = null;
	private Pair<Integer, Boolean> ftCapacity = Pair.of(1, false);

	RecipeCategoryBuilder(IGuiHelper helper, String uid, String title) {
		this.helper = helper;
		this.uid = new ResourceLocation(AssemblyLineMachines.MODID, uid);
		this.title = Component.literal(title);
	}

	RecipeCategoryBuilder background(String guiPath, int u, int v, int width, int height) {
		this.background = helper.createDrawable(getGUIPath(guiPath), u, v, width, height);
		return this;
	}

	RecipeCategoryBuilder icon(ItemLike b) {
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, b.asItem().getDefaultInstance());
		return this;
	}

	RecipeCategoryBuilder progressBar(String guiPath, int u, int v, int width, int height, int frameTime, StartDirection dir, boolean inverted, int drawX, int drawY) {
		this.progressBar = Pair.of(helper.drawableBuilder(getGUIPath(guiPath), u, v, width, height).buildAnimated(frameTime, dir, inverted), Pair.of(drawX, drawY));
		return this;
	}

	RecipeCategoryBuilder draw(TriConsumer<Recipe<?>, PoseStack, Pair<Double, Double>> triConsumer) {
		this.draw = triConsumer;
		return this;
	}

	RecipeCategoryBuilder tooltip(TriConsumer<Recipe<?>, Object, List<Component>> tooltipFunction) {
		this.tooltip = tooltipFunction;
		return this;
	}

	RecipeCategoryBuilder fluidTooltipOptions(int capacity, boolean showCapacity) {
		this.ftCapacity = Pair.of(capacity, showCapacity);
		return this;
	}

	@SafeVarargs
	final RecipeCategoryBuilder slots(Function<Integer, RecipeIngredientRole> role, Pair<Integer, Integer>... slots) {
		this.role = role;
		this.slots = Arrays.asList(slots);
		return this;
	}

	RecipeCategoryBuilder catalysts(ItemLike... catalysts) {
		this.catalysts = Stream.of(catalysts).map((il) -> il.asItem().getDefaultInstance()).toList();
		return this;
	}


	//Build
	<R extends Recipe<?> & IRecipeCategoryBuilder> IRecipeCategory<R> build(Class<R> clazz){

		RecipeType<R> type = RecipeType.create(uid.getNamespace(), uid.getPath(), clazz);

		class ALMRecipeCategory implements IRecipeCategory<R>, IInfoProvider{

			@Override
			public RecipeType<R> getRecipeType() {
				return type;
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
			public void draw(R recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
				if(progressBar != null) {
					progressBar.getFirst().draw(stack, progressBar.getSecond().getFirst(), progressBar.getSecond().getSecond());
				}

				if(draw != null) {
					draw.accept(recipe, stack, Pair.of(mouseX, mouseY));
				}
			}

			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {

				IRecipeSlotTooltipCallback cb = (rsv, tt) -> {
					if(tooltip != null && rsv.getDisplayedIngredient().isPresent()) tooltip.accept(recipe, rsv.getDisplayedIngredient().get().getIngredient(), tt);
				};

				if(slots != null && role != null && recipe.getJEIComponents() != null) {
					int i = 0;
					for(Object obj : recipe.getJEIComponents()) {
						IRecipeSlotBuilder sb = builder.addSlot(role.apply(i), slots.get(i).getFirst(), slots.get(i).getSecond()).addTooltipCallback(cb).setFluidRenderer(ftCapacity.getFirst(), ftCapacity.getSecond(), 16, 16);

						if(obj instanceof CountIngredient cIng) {
							sb.addItemStacks(cIng.getCountModifiedItemStacks());
						}else if(obj instanceof Ingredient ing) {
							sb.addIngredients(ing);
						}else if(obj instanceof FluidStack fs) {
							sb.addIngredient(ForgeTypes.FLUID_STACK, fs);
						}else if(obj instanceof ItemStack is) {
							sb.addItemStack(is);
						}else if(obj instanceof List<?> isl) {
							sb.addIngredientsUnsafe(isl);
						}else {
							throw new IllegalArgumentException("JEI received an invalid component \"" + obj.toString() + "\" of type " + obj.getClass().getName() + " for recipe " + recipe.getId().toString() + ".");
						}
						i++;
					}
				}
			}

			@Override
			public List<ItemStack> getCatalysts() {
				return catalysts;
			}

			@Override
			public void registerRecipes(IRecipeRegistration registration) {
				Minecraft mc = Minecraft.getInstance();
				registration.addRecipes(this.getRecipeType(), mc.level.getRecipeManager().getRecipes().stream().filter((r) -> clazz.isInstance(r)).map((r) -> clazz.cast(r)).toList());
			}
		}

		return new ALMRecipeCategory();
	}

	static ResourceLocation getGUIPath(String name) {
		return new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/jei/" + name + ".png");
	}

	public static interface IRecipeCategoryBuilder {

		/**
		 *
		 * @return A list of components you wish to show in the JEI view for this recipe, in slot-order. Can be mixed-type, but all elements of the list must be one of:
		 * <br>- Ingredient<br>- CountIngredient<br>- FluidStack<br>- ItemStack<br>- A List of any of the above
		 * @throws IllegalArgumentException if the returned list contains any of the non-accepted types.
		 */
		public List<?> getJEIComponents();

	}

	public static interface IInfoProvider{

		public List<ItemStack> getCatalysts();

		public void registerRecipes(IRecipeRegistration registration);
	}

}
