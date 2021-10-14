package me.haydenb.assemblylinemachines.registry.plugins.jei;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.plugins.jei.categories.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ALMJEI implements IModPlugin{
	
	private GrinderRecipeCategory grinderCategory;
	private FluidBathRecipeCategory bathCategory;
	private PurifierRecipeCategory purifierCategory;
	private AlloyingRecipeCategory alloyingCategory;
	private FluidGroundCategory groundCategory;
	private RefineryRecipeCategory refineryCategory;
	private EnchantmentBookRecipeCategory enchantmentCategory;
	private LumberRecipeCategory lumberCategory;
	private MetalShaperRecipeCategory metalCategory;
	private EntropyReactorWasteCategory entropyCategory;
	
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(AssemblyLineMachines.MODID, "alm");
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if(ConfigHolder.COMMON.jeiSupport.get() == true) {
			IJeiHelpers helpers = registration.getJeiHelpers();
			IGuiHelper guiHelper = helpers.getGuiHelper();
			grinderCategory = new GrinderRecipeCategory(guiHelper);
			bathCategory = new FluidBathRecipeCategory(guiHelper);
			purifierCategory = new PurifierRecipeCategory(guiHelper);
			alloyingCategory = new AlloyingRecipeCategory(guiHelper);
			groundCategory = new FluidGroundCategory(guiHelper);
			refineryCategory = new RefineryRecipeCategory(guiHelper);
			enchantmentCategory = new EnchantmentBookRecipeCategory(guiHelper);
			lumberCategory = new LumberRecipeCategory(guiHelper);
			metalCategory = new MetalShaperRecipeCategory(guiHelper);
			entropyCategory = new EntropyReactorWasteCategory(guiHelper);
			
			registration.addRecipeCategories(grinderCategory, bathCategory, purifierCategory, 
					alloyingCategory, groundCategory, refineryCategory, enchantmentCategory, lumberCategory, metalCategory, entropyCategory);
			AssemblyLineMachines.LOGGER.info("JEI plugin for Assembly Line Machines loaded.");
		}
		
		
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if(ConfigHolder.COMMON.jeiSupport.get() == true) {
			registration.addRecipes(JEIHelper.getRecipes(GrinderCrafting.GRINDER_RECIPE), grinderCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(BathCrafting.BATH_RECIPE), bathCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(PurifierCrafting.PURIFIER_RECIPE), purifierCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(AlloyingCrafting.ALLOYING_RECIPE), alloyingCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(FluidInGroundRecipe.FIG_RECIPE), groundCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(RefiningCrafting.REFINING_RECIPE), refineryCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE), enchantmentCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(LumberCrafting.LUMBER_RECIPE), lumberCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(MetalCrafting.METAL_RECIPE), metalCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(EntropyReactorCrafting.ERO_RECIPE), entropyCategory.getUid());
		}
	}

}
