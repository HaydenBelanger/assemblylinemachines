package me.haydenb.assemblylinemachines.plugins.jei;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Supplier;

import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.crafting.GeneratorFluidCrafting.GeneratorFluidTypes;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IInfoProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import me.haydenb.assemblylinemachines.registry.utils.ScreenMath;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties.BathCraftingFluids;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.*;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@JeiPlugin
public class JEICategoryRegistry implements IModPlugin{

	private static final List<IRecipeCategory<? extends Recipe<?>>> CATEGORY_REGISTRY = new ArrayList<>();

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "assemblylinemachines");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
  if(CATEGORY_REGISTRY.isEmpty()) {
		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "lumber", "Lumber Mill Crafting")
				.background("gui_set_a", 145, 69, 102, 30).icon(Registry.getBlock("lumber_mill")).progressBar("gui_set_a", 199, 99, 19, 5, 200, StartDirection.LEFT, false, 23, 13)
				.slots((i) -> i == 0 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, Pair.of(3, 7), Pair.of(50, 7), Pair.of(83, 7)).catalysts(Registry.getBlock("lumber_mill"))
				.tooltip((pP1, pP2, tooltip) -> {
					if(pP1 instanceof LumberCrafting recipe && pP2 instanceof ItemStack stack) {
						if(!recipe.outputb.isEmpty() && stack.is(recipe.outputb.getItem())) tooltip.add(Component.literal(String.format("%.0f%%", (recipe.opbchance * 100f)) + " Chance").withStyle(ChatFormatting.AQUA));
					}
				}).build(LumberCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "alloying", "Alloying Crafting")
				.background("gui_set_a", 104, 112, 62, 41).icon(Registry.getBlock("alloy_smelter")).progressBar("gui_set_a", 202, 104, 16, 14, 200, StartDirection.LEFT, false, 19, 13)
				.slots((i) -> i == 2 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT, Pair.of(1, 1), Pair.of(1, 24), Pair.of(41, 12)).catalysts(Registry.getBlock("alloy_smelter"), Registry.getBlock("mkii_alloy_smelter")).build(AlloyingCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "grinding", "Grinding Crafting")
				.background("gui_set_a", 166, 145, 90, 44).icon(Registry.getBlock("electric_grinder")).progressBar("gui_set_a", 237, 131, 19, 14, 200, StartDirection.LEFT, false, 42, 24)
				.slots((i) -> switch(i) {
				default -> RecipeIngredientRole.INPUT;
				case 1, 2 -> RecipeIngredientRole.CATALYST;
				case 3 -> RecipeIngredientRole.OUTPUT;
				}, Pair.of(22, 23), Pair.of(1, 23), Pair.of(1, 1), Pair.of(69, 23))
				.tooltip((pP1, pP2, tooltip) -> {
					if(pP1 instanceof GrinderCrafting recipe && pP2 instanceof ItemStack stack) {
						if(recipe.chanceToDouble != 0 && stack.getCount() == recipe.getResultItem().getCount() * 2) tooltip.add(Component.literal(String.format("%.0f%%", (recipe.chanceToDouble * 100f)) + " Chance").withStyle(ChatFormatting.AQUA));
					}
				}).build(GrinderCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "purifying", "Purifier Crafting")
				.background("gui_set_a", 0, 153, 90, 44).icon(Registry.getBlock("electric_purifier")).progressBar("gui_set_a", 90, 153, 43, 32, 200, StartDirection.LEFT, false, 20, 6)
				.slots((i) -> i == 3 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT, Pair.of(1, 1), Pair.of(1, 27), Pair.of(22, 14), Pair.of(69, 14)).catalysts(Registry.getBlock("electric_purifier"), Registry.getBlock("mkii_purifier")).build(PurifierCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "pneumatic", "Pneumatic Compressor")
				.background("gui_set_a", 87, 211, 87, 26).icon(Registry.getBlock("pneumatic_compressor")).progressBar("gui_set_a", 155, 201, 19, 10, 200, StartDirection.LEFT, false, 40, 8)
				.slots((i) -> switch(i) {
				default -> RecipeIngredientRole.CATALYST;
				case 1 -> RecipeIngredientRole.INPUT;
				case 2 -> RecipeIngredientRole.OUTPUT;
				}, Pair.of(1, 5), Pair.of(21, 5), Pair.of(66, 5))
				.draw(new TriConsumer<>() {
					IDrawableStatic drawable = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 139, 195, 16, 16).build();
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						if(k instanceof PneumaticCrafting recipe && recipe.mold.equals(Items.AIR)) drawable.draw(v, 1, 5);
					}
				}).catalysts(Registry.getBlock("pneumatic_compressor"), Registry.getBlock("mkii_pneumatic_compressor")).build(PneumaticCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "world_corruption", "World Corruption")
				.background("gui_set_a", 168, 119, 69, 26).icon(Registry.getBlock("corrupt_diamond_ore")).progressBar("gui_set_a", 218, 99, 19, 10, 200, StartDirection.LEFT, false, 21, 8)
				.tooltip((pP1, pP2, tooltip) -> {
					if(pP1 instanceof WorldCorruptionCrafting recipe && pP2 instanceof ItemStack stack) {
						if(recipe.hasOptionalResults()) {
							Optional<Float> amount = recipe.getChanceOfSubdrop(stack.getItem());
							if(amount.isPresent()) {
								tooltip.add(Component.literal(String.format("%.0f%%", (amount.get() * 100f)) + " Chance").withStyle(ChatFormatting.AQUA));
							}
						}
					}
				}).slots((i) -> i == 0 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, Pair.of(1, 5), Pair.of(48, 5)).catalysts(Registry.getBlock("entropy_reactor_block"), Registry.getBlock("entropy_reactor_core"), Registry.getBlock("corrupting_basin"))
				.build(WorldCorruptionCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "generator_fluid", "Generator Fluids").background("gui_set_a", 166, 0, 90, 26)
				.icon(Items.WATER_BUCKET).slots((i) -> i == 0 ? RecipeIngredientRole.CATALYST : RecipeIngredientRole.INPUT, Pair.of(1, 5), Pair.of(22, 5)).draw(new TriConsumer<>() {
					private final DecimalFormat df = new DecimalFormat("#.0");
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						if(k instanceof GeneratorFluidCrafting) {
							GeneratorFluidCrafting recipe = (GeneratorFluidCrafting) k;
							MutableComponent l1 = recipe.fluidType == GeneratorFluidTypes.COOLANT ? Component.literal("Coolant") : Component.literal("Fuel");
							MutableComponent l2 = recipe.fluidType == GeneratorFluidTypes.COOLANT ? Component.literal(df.format(recipe.coolantStrength) + "x") : Component.literal(FormattingHelper.formatToSuffix(recipe.powerPerUnit) + " FE");
							ScreenMath.drawCenteredStringWithoutShadow(v, l1, 64, 6);
							ScreenMath.drawCenteredStringWithoutShadow(v, l2, 64, 15);
						}
					}
				}).build(GeneratorFluidCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "entropy_reactor_output", "Entropy Reactor Waste")
				.background("gui_set_a", 194, 43, 62, 26).icon(Registry.getBlock("entropy_reactor_core")).progressBar("gui_set_a", 240, 29, 16, 14, 100, StartDirection.LEFT, false, 19, 6)
				.tooltip((pP1, pP2, tooltip) -> {
					if(pP1 instanceof EntropyReactorCrafting recipe && pP2 instanceof ItemStack stack) {
						if(recipe.getResultItem().is(stack.getItem())) {
							String text = recipe.varietyReqd < 0.01f ? "Any Variety" :  " > " + String.format("%.0f%%", (recipe.varietyReqd * 100f)) + " Variety";
							tooltip.add(Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE));
						}
					}
				}).catalysts(Registry.getBlock("entropy_reactor_block"), Registry.getBlock("entropy_reactor_core")).slots((i) -> i == 0 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, Pair.of(1, 5), Pair.of(41, 5)).build(EntropyReactorCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "bathing", "Fluid Bath Crafting")
				.background("gui_set_a", 0, 197, 87, 41).icon(Registry.getBlock("electric_fluid_mixer"))
				.draw(new TriConsumer<>() {
					private HashMap<BathCraftingFluids, IDrawableAnimated> progressBars = new HashMap<>();
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						if(k instanceof BathCrafting) {
							BathCrafting recipe = (BathCrafting) k;
							IDrawableAnimated anim = progressBars.get(recipe.getFluid());
							if(anim == null) {
								anim = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), recipe.getFluid().getJeiBlitPiece().getFirst(), recipe.getFluid().getJeiBlitPiece().getSecond(), 15, 16).buildAnimated(200, StartDirection.LEFT, false);
								progressBars.put(recipe.getFluid(), anim);
							}
						}
					}
				}).slots((i) -> switch(i) {
				default -> RecipeIngredientRole.INPUT;
				case 2 -> RecipeIngredientRole.CATALYST;
				case 4 -> RecipeIngredientRole.OUTPUT;
				}, Pair.of(1, 5), Pair.of(22, 5), Pair.of(1, 24), Pair.of(42, 24), Pair.of(66, 5)).build(BathCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "fig", "Pump Output")
				.background("gui_set_a", 0, 34, 36, 77).icon(Registry.getBlock("pump"))
				.draw(new TriConsumer<>() {
					HashMap<FluidInGroundCriteria, IDrawable> drawables = new HashMap<>();
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						if(k instanceof FluidInGroundRecipe) {
							FluidInGroundRecipe recipe = (FluidInGroundRecipe) k;
							IDrawable drawable = drawables.get(recipe.criteria);
							if(drawable == null) {
								drawable = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), recipe.criteria.getJeiBlitX(), recipe.criteria.getJeiBlitY(), 34, 34).build();
								drawables.put(recipe.criteria, drawable);
							}
						}
					}
				}).tooltip((pP1, pP2, tooltip) -> {
					if(pP1 instanceof FluidInGroundRecipe recipe) {
						tooltip.addAll(recipe.criteria.getTooltip(recipe.odds));
					}
				}).catalysts(Registry.getBlock("pump"), Registry.getBlock("pumpshaft")).slots((i) -> RecipeIngredientRole.OUTPUT, Pair.of(10, 51)).build(FluidInGroundRecipe.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "enchantment_book", "Enchantment Crafting")
				.background("gui_set_a", 174, 209, 82, 47).icon(Registry.getBlock("experience_mill")).progressBar("gui_set_a", 228, 200, 28, 9, 120, StartDirection.LEFT, false, 23, 19)
				.slots((i) -> i == 2 ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT, Pair.of(1, 1), Pair.of(1, 30), Pair.of(61, 16)).catalysts(Registry.getBlock("experience_mill")).build(EnchantmentBookCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "refining", "Refinery Crafting")
				.background("gui_set_a", 112, 26, 82, 41).icon(Registry.getBlock("refinery"))
				.draw(new TriConsumer<>() {
					IDrawableAnimated progBar = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 194, 26, 7, 3).buildAnimated(400, StartDirection.TOP, false);
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						progBar.draw(v, 35, 19);
						progBar.draw(v, 44, 19);
						progBar.draw(v, 55, 19);
						progBar.draw(v, 64, 19);
					}
				}).slots((i) -> switch(i) {
				case 0, 1 -> RecipeIngredientRole.CATALYST;
				case 2, 3 -> RecipeIngredientRole.INPUT;
				default -> RecipeIngredientRole.OUTPUT;
				}, Pair.of(1, 1), Pair.of(1, 24), Pair.of(35, 1), Pair.of(55, 1), Pair.of(25, 24), Pair.of(45, 24), Pair.of(65, 24)).build(RefiningCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "greenhouse", "Greenhouse Crops")
				.background("gui_set_a", 48, 0, 64, 60).icon(Registry.getBlock("greenhouse")).slots((i) -> switch(i) {
				case 0 -> RecipeIngredientRole.INPUT;
				default -> RecipeIngredientRole.CATALYST;
				case 4 -> RecipeIngredientRole.OUTPUT;
				}, Pair.of(1, 1), Pair.of(1, 23), Pair.of(1, 43), Pair.of(20, 43), Pair.of(43, 12))
				.draw(new TriConsumer<>() {
					IDrawableAnimated progBarDay = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 76, 60, 18, 12).buildAnimated(200, StartDirection.LEFT, false);
					IDrawableAnimated progBarNight = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 58, 60, 18, 12).buildAnimated(200, StartDirection.LEFT, false);
					IDrawableAnimated progBarReg = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 94, 60, 18, 12).buildAnimated(200, StartDirection.LEFT, false);
					@Override
					public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
						if(k instanceof GreenhouseCrafting recipe) {
							Supplier<IDrawableAnimated> bar;
							if(recipe.sprout.sunlightReq == null) {
								bar = () -> progBarReg;
							}else {
								bar = recipe.sprout.sunlightReq.test(recipe.soil) ? () -> progBarDay : () -> progBarNight;
							}
						}
					}
				}).catalysts(Registry.getItem("greenhouse")).build(GreenhouseCrafting.class));

		CATEGORY_REGISTRY.add(new RecipeCategoryBuilder(guiHelper, "fertilizer", "Greenhouse Fertilizer")
				.background("gui_set_a", 187, 0, 69, 26).icon(Items.BONE_MEAL).slots((i) -> RecipeIngredientRole.INPUT, Pair.of(1, 5)).draw((k, v, s) -> {
					if(k instanceof GreenhouseFertilizerCrafting recipe) {
						String uses = recipe.usesPerItem == 1 ? "Use" : "Uses";
						ScreenMath.drawCenteredStringWithoutShadow(v, Component.literal(recipe.usesPerItem + " " + uses), 43, 6);
						ScreenMath.drawCenteredStringWithoutShadow(v, Component.literal(recipe.multiplication + "x Yield"), 43, 15);

					}
				}).catalysts(Registry.getBlock("greenhouse")).build(GreenhouseFertilizerCrafting.class));
    }
		registration.addRecipeCategories(CATEGORY_REGISTRY.toArray(new IRecipeCategory<?>[CATEGORY_REGISTRY.size()]));

		AssemblyLineMachines.LOGGER.debug("Just Enough Items plugin connected to Assembly Line Machines.");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerRecipes(IRecipeRegistration registration) {

		CATEGORY_REGISTRY.stream().filter((c) -> (c instanceof IInfoProvider)).forEach((c) -> ((IInfoProvider) c).registerRecipes(registration));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		CATEGORY_REGISTRY.stream().filter((c) -> (c instanceof IInfoProvider)).forEach((c) -> ((IInfoProvider) c).getCatalysts().forEach((i) -> registration.addRecipeCatalyst(i, c.getRecipeType())));

		registration.addRecipeCatalyst(Registry.getBlock("electric_furnace").asItem().getDefaultInstance(), RecipeTypes.SMELTING);
		registration.addRecipeCatalyst(Registry.getBlock("mkii_furnace").asItem().getDefaultInstance(), RecipeTypes.SMELTING);
	}
}