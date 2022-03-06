package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.*;
import java.util.function.BiFunction;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.ibm.icu.text.DecimalFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.crafting.GeneratorFluidCrafting.GeneratorFluidTypes;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder.ICatalystProvider;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@JeiPlugin
public class JEICategoryRegistry implements IModPlugin{
	
	private static final HashMap<RecipeType<?>, IRecipeCategory<?>> CATEGORY_REGISTRY = new HashMap<>();
	
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "alm");
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if(ConfigHolder.getCommonConfig().jeiSupport.get() == true) {
			IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
			
			//Note: Catalysts not yet set for:
			//Grinding, Fluid Bath, Generator Fluids, or Refining, due to the variable nature of the accepted crafting inventories.
			
			CATEGORY_REGISTRY.put(LumberCrafting.LUMBER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("lumber").title("Lumber Mill Crafting")
					.background("gui_set_pre2022", 145, 69, 102, 30).icon(Registry.getBlock("lumber_mill")).progressBar("gui_set_pre2022", 199, 99, 19, 5, 200, StartDirection.LEFT, false, 23, 13)
					.itemSlots(1, Pair.of(2, 6), Pair.of(49, 6), Pair.of(82, 6)).catalysts(Registry.getBlock("lumber_mill"))
					.itemTooltip(new TriFunction<>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, ItemStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof LumberCrafting) {
								LumberCrafting recipe = (LumberCrafting) pP1;
								if(!recipe.getSecondaryOutput().isEmpty() && pP2.is(recipe.getSecondaryOutput().getItem())){
									return List.of(new TextComponent("�b" + String.format("%.0f%%", (recipe.getOutputChance() * 100f)) + " Chance"));
								}
							}
							return null;
						}
					}).build(LumberCrafting.class));
			
			CATEGORY_REGISTRY.put(AlloyingCrafting.ALLOYING_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("alloying").title("Alloying Crafting")
					.background("gui_set_pre2022", 104, 112, 62, 41).icon(Registry.getBlock("alloy_smelter")).progressBar("gui_set_pre2022", 202, 104, 16, 14, 200, StartDirection.LEFT, false, 19, 13)
					.itemSlots(2, Pair.of(0, 0), Pair.of(0, 23), Pair.of(40, 11)).catalysts(Registry.getBlock("alloy_smelter"), Registry.getBlock("mkii_alloy_smelter")).build(AlloyingCrafting.class));
			
			CATEGORY_REGISTRY.put(GrinderCrafting.GRINDER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("grinding").title("Grinding Crafting")
					.background("gui_set_pre2022", 166, 145, 90, 44).icon(Registry.getBlock("electric_grinder")).progressBar("gui_set_pre2022", 237, 131, 19, 14, 200, StartDirection.LEFT, false, 42, 24)
					.itemSlots(1, Pair.of(21, 22), Pair.of(0, 22), Pair.of(0, 0), Pair.of(68, 22))
					.itemTooltip(new TriFunction<>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, ItemStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof GrinderCrafting) {
								GrinderCrafting recipe = (GrinderCrafting) pP1;
								if(recipe.getChanceToDouble() != 0 && pP2.getCount() == recipe.getResultItem().getCount() * 2) {
									return List.of(new TextComponent("�b" + String.format("%.0f%%", (recipe.getChanceToDouble() * 100f)) + " Chance"));
								}
							}
							return null;
						}
					}).build(GrinderCrafting.class));
			
			CATEGORY_REGISTRY.put(PurifierCrafting.PURIFIER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("purifying").title("Purifier Crafting")
					.background("gui_set_pre2022", 0, 153, 90, 44).icon(Registry.getBlock("electric_purifier")).progressBar("gui_set_pre2022", 90, 153, 43, 32, 200, StartDirection.LEFT, false, 20, 6)
					.itemSlots(3, Pair.of(0, 0), Pair.of(0, 26), Pair.of(21, 13), Pair.of(68, 13)).catalysts(Registry.getBlock("electric_purifier"), Registry.getBlock("mkii_purifier")).build(PurifierCrafting.class));
			
			CATEGORY_REGISTRY.put(MetalCrafting.METAL_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("metal_shaping").title("Metal Shaper Crafting")
					.background("gui_set_pre2022", 168, 119, 69, 26).icon(Registry.getBlock("metal_shaper")).progressBar("gui_set_pre2022", 218, 109, 19, 10, 200, StartDirection.LEFT, false, 21, 8)
					.itemSlots(1, Pair.of(0, 4), Pair.of(47, 4)).catalysts(Registry.getBlock("metal_shaper")).build(MetalCrafting.class));
			
			CATEGORY_REGISTRY.put(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("world_corruption").title("World Corruption")
					.background("gui_set_pre2022", 168, 119, 69, 26).icon(Registry.getBlock("corrupt_diamond_ore")).progressBar("gui_set_pre2022", 218, 99, 19, 10, 200, StartDirection.LEFT, false, 21, 8)
					.itemTooltip(new TriFunction<>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, ItemStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof WorldCorruptionCrafting) {
								WorldCorruptionCrafting recipe = (WorldCorruptionCrafting) pP1;
								if(recipe.hasOptionalResults()) {
									Optional<Float> amount = recipe.getChanceOfSubdrop(pP2.getItem());
									if(amount.isPresent()) {
										return List.of(new TextComponent("�b" + String.format("%.0f%%", (amount.get() * 100f)) + " Chance"));
									}
								}
							}
							return null;
						}
					}).catalysts(Registry.getBlock("entropy_reactor_block"), Registry.getBlock("entropy_reactor_core"), Registry.getBlock("corrupting_basin"))
					.build(WorldCorruptionCrafting.class));
			
			CATEGORY_REGISTRY.put(GeneratorFluidCrafting.GENFLUID_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("generator_fluid").title("Generator Fluids").background("gui_set_pre2022", 166, 0, 90, 26)
					.icon(Items.WATER_BUCKET).itemSlots(0, Pair.of(0, 4)).fluidSlots(1, Pair.of(22, 5)).draw(new TriConsumer<>() {
						private final DecimalFormat df = new DecimalFormat("#.0");
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							if(k instanceof GeneratorFluidCrafting) {
								GeneratorFluidCrafting recipe = (GeneratorFluidCrafting) k;
								TextComponent l1 = recipe.getFluidType() == GeneratorFluidTypes.COOLANT ? new TextComponent("Coolant") : new TextComponent("Fuel");
								TextComponent l2 = recipe.getFluidType() == GeneratorFluidTypes.COOLANT ? new TextComponent(df.format(recipe.checkCoolantFluid(recipe.getFluid())) + "x") : new TextComponent(Formatting.formatToSuffix(recipe.checkBurnableFluid(recipe.getFluid())) + " FE");
								Utils.drawCenteredStringWithoutShadow(v, l1, 64, 6);
								Utils.drawCenteredStringWithoutShadow(v, l2, 64, 15);
							}
						}
					}).build(GeneratorFluidCrafting.class));
			
			CATEGORY_REGISTRY.put(EntropyReactorCrafting.ERO_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("entropy_reactor_output").title("Entropy Reactor Waste")
					.background("gui_set_pre2022", 194, 43, 62, 26).icon(Registry.getBlock("entropy_reactor_core")).progressBar("gui_set_pre2022", 240, 29, 16, 14, 100, StartDirection.LEFT, false, 19, 6)
					.itemTooltip(new TriFunction<>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, ItemStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof EntropyReactorCrafting) {
								EntropyReactorCrafting recipe = (EntropyReactorCrafting) pP1;
								if(recipe.getResultItem().is(pP2.getItem())) {
									String text = recipe.getVarietyReqd() < 0.01f ? "�5Any Variety" :  "�5 > " + String.format("%.0f%%", (recipe.getVarietyReqd() * 100f)) + " Variety";
									return List.of(new TextComponent(text));
								}
							}
							return null;
						}
					}).catalysts(Registry.getBlock("entropy_reactor_block"), Registry.getBlock("entropy_reactor_core")).itemSlots(1, Pair.of(0, 4), Pair.of(40, 4)).build(EntropyReactorCrafting.class));
			
			CATEGORY_REGISTRY.put(BathCrafting.BATH_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("bathing").title("Fluid Bath Crafting")
					.background("gui_set_pre2022", 0, 197, 87, 41).icon(Registry.getBlock("electric_fluid_mixer"))
					.draw(new TriConsumer<>() {
						private HashMap<BathCraftingFluids, IDrawableAnimated> progressBars = new HashMap<>();
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							if(k instanceof BathCrafting) {
								BathCrafting recipe = (BathCrafting) k;
								IDrawableAnimated anim = progressBars.get(recipe.getFluid());
								if(anim == null) {
									anim = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_pre2022"), recipe.getFluid().getJeiBlitPiece().getFirst(), recipe.getFluid().getJeiBlitPiece().getSecond(), 15, 16).buildAnimated(200, StartDirection.LEFT, false);
									progressBars.put(recipe.getFluid(), anim);
								}
								anim.draw(v, 42, 5);
							}
						}
					}).itemSlots(2, Pair.of(0, 4), Pair.of(21, 4), Pair.of(0, 23), Pair.of(65, 4)).fluidSlots(1, Pair.of(42, 24)).build(BathCrafting.class));
			
			CATEGORY_REGISTRY.put(FluidInGroundRecipe.FIG_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("fig").title("Pump Output")
					.background("gui_set_pre2022", 0, 34, 36, 77).icon(Registry.getBlock("pump"))
					.draw(new TriConsumer<>() {
						HashMap<FluidInGroundCriteria, IDrawable> drawables = new HashMap<>();
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							if(k instanceof FluidInGroundRecipe) {
								FluidInGroundRecipe recipe = (FluidInGroundRecipe) k;
								IDrawable drawable = drawables.get(recipe.getCriteria());
								if(drawable == null) {
									drawable = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_pre2022"), recipe.getCriteria().getJeiBlitX(), recipe.getCriteria().getJeiBlitY(), 34, 34).build();
									drawables.put(recipe.getCriteria(), drawable);
								}
								drawable.draw(v, 1, 42);
							}
						}
					}).fluidTooltip(new TriFunction<>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, FluidStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof FluidInGroundRecipe) {
								FluidInGroundRecipe recipe = (FluidInGroundRecipe) pP1;
								return recipe.getCriteria().getTooltip(recipe.getChance());
							}
							return null;
						}
					}).catalysts(Registry.getBlock("pump"), Registry.getBlock("pumpshaft")).fluidSlots(0, Pair.of(10, 51)).build(FluidInGroundRecipe.class));
			
			CATEGORY_REGISTRY.put(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("enchantment_book").title("Enchantment Crafting")
					.background("gui_set_pre2022", 174, 209, 82, 47).icon(Registry.getBlock("experience_mill")).progressBar("gui_set_pre2022", 228, 200, 28, 9, 120, StartDirection.LEFT, false, 23, 19)
					.itemSlots(2, Pair.of(0, 0), Pair.of(0, 29), Pair.of(60, 15))
					.itemStackModifier(new BiFunction<>() {
						@Override
						public List<List<ItemStack>> apply(Recipe<?> t, List<Ingredient> u) {
							List<List<ItemStack>> result = new ArrayList<>();
							for(Ingredient ing : u) {
								result.add(Arrays.asList(ing.getItems()));
							}
							if(t instanceof EnchantmentBookCrafting && result.size() > 0) {
								EnchantmentBookCrafting recipe = (EnchantmentBookCrafting) t;
								for(ItemStack item : result.get(0)) {
									item.setCount(recipe.getAmount());
								}
							}
							return result;
						}
					}).catalysts(Registry.getBlock("experience_mill")).build(EnchantmentBookCrafting.class));
			
			CATEGORY_REGISTRY.put(RefiningCrafting.REFINING_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("refining").title("Refinery Crafting")
					.background("gui_set_pre2022", 112, 26, 82, 41).icon(Registry.getBlock("refinery"))
					.draw(new TriConsumer<>() {
						IDrawableAnimated progBar = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_pre2022"), 194, 26, 7, 3).buildAnimated(400, StartDirection.TOP, false);
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							progBar.draw(v, 35, 19);
							progBar.draw(v, 44, 19);
							progBar.draw(v, 55, 19);
							progBar.draw(v, 64, 19);
						}
					}).build(RefiningCrafting.class));
			
			registration.addRecipeCategories(CATEGORY_REGISTRY.values().toArray(new IRecipeCategory<?>[CATEGORY_REGISTRY.size()]));
			AssemblyLineMachines.LOGGER.info("JEI plugin for Assembly Line Machines loaded.");
		}
		
		
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerRecipes(IRecipeRegistration registration) {
		if(ConfigHolder.getCommonConfig().jeiSupport.get() == true) {
			
			ListMultimap<RecipeType<?>, Recipe<?>> allRecipes = getRecipes();
			for(RecipeType<?> type : CATEGORY_REGISTRY.keySet()) {
				registration.addRecipes(allRecipes.get(type), CATEGORY_REGISTRY.get(type).getUid());
			}
			
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if(ConfigHolder.getCommonConfig().jeiSupport.get() == true) {
			for(IRecipeCategory<?> category : CATEGORY_REGISTRY.values()) {
				if(category instanceof ICatalystProvider) {
					ItemStack[] catalysts = ((ICatalystProvider) category).getCatalysts();
					if(catalysts != null) {
						for(ItemStack stack : catalysts) {
							registration.addRecipeCatalyst(stack, category.getUid());
						}
					}
					
					
				}
			}
			
			registration.addRecipeCatalyst(Registry.getBlock("electric_furnace").asItem().getDefaultInstance(), VanillaRecipeCategoryUid.FURNACE);
		}
	}
	
	private static ListMultimap<RecipeType<?>, Recipe<?>> getRecipes(){
		
		ListMultimap<RecipeType<?>, Recipe<?>> allRecipes = ArrayListMultimap.create();
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.level.getRecipeManager().getRecipes().forEach((recipe) -> {
			if(CATEGORY_REGISTRY.containsKey(recipe.getType())) {
				allRecipes.put(recipe.getType(), recipe);
			}
		});
		
		return allRecipes;
	}

}

