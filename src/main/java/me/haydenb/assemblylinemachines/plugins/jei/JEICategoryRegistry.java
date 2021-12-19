package me.haydenb.assemblylinemachines.plugins.jei;

import java.util.*;
import java.util.function.BiFunction;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.data.models.blockstates.PropertyDispatch.TriFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
		if(ConfigHolder.COMMON.jeiSupport.get() == true) {
			IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
			
			CATEGORY_REGISTRY.put(LumberCrafting.LUMBER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("lumber").title("Lumber Mill Crafting")
					.background("gui_set_a", 145, 69, 73, 50).icon(Registry.getBlock("lumber_mill")).progressBar("gui_set_a", 218, 69, 19, 5, 200, StartDirection.LEFT, false, 23, 13)
					.itemSlots(1, Pair.of(2, 6), Pair.of(2, 30), Pair.of(49, 6), Pair.of(49, 30)).build(LumberCrafting.class));
			
			CATEGORY_REGISTRY.put(AlloyingCrafting.ALLOYING_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("alloying").title("Alloying Crafting")
					.background("gui_set_a", 43, 92, 85, 41).icon(Registry.getBlock("alloy_smelter")).progressBar("gui_set_a", 91, 78, 16, 14, 200, StartDirection.LEFT, false, 42, 13)
					.itemSlots(2, Pair.of(23, 0), Pair.of(23, 23), Pair.of(0, 11), Pair.of(63, 11)).build(AlloyingCrafting.class));
			
			CATEGORY_REGISTRY.put(GrinderCrafting.GRINDER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("grinding").title("Grinding Crafting")
					.background("gui_set_a", 166, 145, 90, 44).icon(Registry.getBlock("electric_grinder")).progressBar("gui_set_a", 237, 131, 19, 14, 200, StartDirection.LEFT, false, 42, 24)
					.itemSlots(1, Pair.of(21, 22), Pair.of(0, 22), Pair.of(0, 0), Pair.of(68, 22)).build(GrinderCrafting.class));
			
			CATEGORY_REGISTRY.put(PurifierCrafting.PURIFIER_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("purifying").title("Purifier Crafting")
					.background("gui_set_a", 0, 153, 111, 44).icon(Registry.getBlock("electric_purifier")).progressBar("gui_set_a", 111, 153, 43, 32, 200, StartDirection.LEFT, false, 41, 6)
					.itemSlots(3, Pair.of(21, 0), Pair.of(21, 26), Pair.of(42, 13), Pair.of(0, 13), Pair.of(89, 13)).build(PurifierCrafting.class));
			
			CATEGORY_REGISTRY.put(MetalCrafting.METAL_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("metal_shaping").title("Metal Shaper Crafting")
					.background("gui_set_a", 147, 119, 90, 26).icon(Registry.getBlock("metal_shaper")).progressBar("gui_set_a", 218, 109, 19, 10, 200, StartDirection.LEFT, false, 42, 8)
					.itemSlots(1, Pair.of(21, 4), Pair.of(0, 4), Pair.of(68, 4)).build(MetalCrafting.class));
			
			CATEGORY_REGISTRY.put(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("world_corruption").title("World Corruption")
					.background("gui_set_b", 0, 10, 90, 26).icon(Registry.getBlock("corrupt_diamond_ore")).progressBar("gui_set_b", 50, 0, 19, 10, 200, StartDirection.LEFT, false, 42, 8)
					.build(WorldCorruptionCrafting.class));
			
			CATEGORY_REGISTRY.put(EntropyReactorCrafting.ERO_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("entropy_reactor_output").title("Entropy Reactor Waste")
					.background("gui_set_a", 130, 24, 85, 41).icon(Registry.getBlock("entropy_reactor_core")).progressBar("gui_set_a", 199, 10, 16, 14, 100, StartDirection.LEFT, false, 42, 13)
					.draw(new TriConsumer<Recipe<?>, PoseStack, Pair<Double,Double>>() {
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							if(k instanceof EntropyReactorCrafting) {
								EntropyReactorCrafting recipe = (EntropyReactorCrafting) k;
								Minecraft minecraft = Minecraft.getInstance();
								String text = recipe.getVarietyReqd() < 0.01f ? "Any Variety" :  " > " + String.format("%.0f%%", (recipe.getVarietyReqd() * 100f)) + " Variety";
								Utils.drawCenteredStringWithoutShadow(v, minecraft.font, new TextComponent(text), 42, 35, 0x800085);
							}
						}
					}).itemSlots(1, Pair.of(0, 11), Pair.of(23, 11), Pair.of(63, 11)).build(EntropyReactorCrafting.class));
			
			CATEGORY_REGISTRY.put(BathCrafting.BATH_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("bathing").title("Fluid Bath Crafting")
					.background("gui_set_a", 0, 197, 87, 41).icon(Registry.getBlock("electric_fluid_mixer"))
					.draw(new TriConsumer<Recipe<?>, PoseStack, Pair<Double,Double>>() {
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
								anim.draw(v, 42, 5);
							}
							
						}
					}).itemSlots(2, Pair.of(0, 4), Pair.of(21, 4), Pair.of(41, 23), Pair.of(65, 4)).build(BathCrafting.class));
			
			CATEGORY_REGISTRY.put(FluidInGroundRecipe.FIG_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("fig").title("Pump Output")
					.background("gui_set_oil", 0, 0, 132, 99).icon(Registry.getBlock("pump"))
					.draw(new TriConsumer<Recipe<?>, PoseStack, Pair<Double,Double>>() {
						HashMap<FluidInGroundCriteria, IDrawable> drawables = new HashMap<>();
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							if(k instanceof FluidInGroundRecipe) {
								FluidInGroundRecipe recipe = (FluidInGroundRecipe) k;
								IDrawable drawable = drawables.get(recipe.getCriteria());
								if(drawable == null) {
									drawable = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_oil"), recipe.getCriteria().getJeiBlitX(), recipe.getCriteria().getJeiBlitY(), 34, 34).build();
									drawables.put(recipe.getCriteria(), drawable);
								}
								drawable.draw(v, 49, 57);
							}
						}
					}).fluidTooltip(new TriFunction<Recipe<?>, FluidStack, TooltipFlag, List<Component>>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, FluidStack pP2, TooltipFlag pP3) {
							if(pP1 instanceof FluidInGroundRecipe) {
								FluidInGroundRecipe recipe = (FluidInGroundRecipe) pP1;
								return recipe.getCriteria().getTooltip(pP2.getDisplayName(), recipe.getChance());
							}
							return List.of(pP2.getDisplayName());
						}
					}).itemSlots(0, Pair.of(57, 12)).fluidSlots(0, Pair.of(58, 66)).build(FluidInGroundRecipe.class));
			
			CATEGORY_REGISTRY.put(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("enchantment_book").title("Enchantment Crafting")
					.background("gui_set_a", 172, 198, 84, 58).icon(Registry.getBlock("experience_mill")).progressBar("gui_set_a", 228, 189, 28, 9, 120, StartDirection.LEFT, false, 24, 20)
					.itemSlots(2, Pair.of(1, 1), Pair.of(1, 30), Pair.of(61, 39), Pair.of(61, 16))
					.itemStackModifier(new BiFunction<Recipe<?>, List<Ingredient>, List<List<ItemStack>>>() {
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
					}).build(EnchantmentBookCrafting.class));
			
			
			CATEGORY_REGISTRY.put(RefiningCrafting.REFINING_RECIPE, new RecipeCategoryBuilder(guiHelper).uid("refining").title("Refinery Crafting")
					.background("gui_set_a", 41, 24, 82, 41).icon(Registry.getBlock("refinery"))
					.draw(new TriConsumer<Recipe<?>, PoseStack, Pair<Double,Double>>() {
						IDrawableAnimated progBar = guiHelper.drawableBuilder(RecipeCategoryBuilder.getGUIPath("gui_set_a"), 123, 24, 7, 3).buildAnimated(400, StartDirection.TOP, false);
						@Override
						public void accept(Recipe<?> k, PoseStack v, Pair<Double, Double> s) {
							progBar.draw(v, 35, 19);
							progBar.draw(v, 44, 19);
							progBar.draw(v, 55, 19);
							progBar.draw(v, 64, 19);
						}
					}).fluidTooltip(new TriFunction<Recipe<?>, FluidStack, TooltipFlag, List<Component>>() {
						@Override
						public List<Component> apply(Recipe<?> pP1, FluidStack pP2, TooltipFlag pP3) {
							return List.of(pP2.getDisplayName(), new TextComponent("§b" + Formatting.GENERAL_FORMAT.format(pP2.getAmount()) + " mB"));
						}
					}).build(RefiningCrafting.class));
			
			
			
			registration.addRecipeCategories(CATEGORY_REGISTRY.values().toArray(new IRecipeCategory<?>[CATEGORY_REGISTRY.size()]));
			AssemblyLineMachines.LOGGER.info("JEI plugin for Assembly Line Machines loaded.");
		}
		
		
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if(ConfigHolder.COMMON.jeiSupport.get() == true) {
			
			ListMultimap<RecipeType<?>, Recipe<?>> allRecipes = getRecipes();
			for(RecipeType<?> type : CATEGORY_REGISTRY.keySet()) {
				registration.addRecipes(allRecipes.get(type), CATEGORY_REGISTRY.get(type).getUid());
			}
		}
	}
	
	private static ListMultimap<RecipeType<?>, Recipe<?>> getRecipes(){
		
		ListMultimap<RecipeType<?>, Recipe<?>> allRecipes = ArrayListMultimap.create();
		Minecraft minecraft = Minecraft.getInstance();
		Iterator<Recipe<?>> iter = minecraft.level.getRecipeManager().getRecipes().iterator();
		while(iter.hasNext()) {
			Recipe<?> recipe = iter.next();
			if(CATEGORY_REGISTRY.containsKey(recipe.getType())) {
				allRecipes.put(recipe.getType(), recipe);
			}
		}
		
		return allRecipes;
	}

}

