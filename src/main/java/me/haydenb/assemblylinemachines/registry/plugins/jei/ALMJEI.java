package me.haydenb.assemblylinemachines.registry.plugins.jei;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.plugins.jei.categories.FluidBathRecipeCategory;
import me.haydenb.assemblylinemachines.registry.plugins.jei.categories.GrinderRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class ALMJEI implements IModPlugin{
	
	private GrinderRecipeCategory grinderCategory;
	private FluidBathRecipeCategory bathCategory;
	
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
			
			registration.addRecipeCategories(grinderCategory, bathCategory);
		}
		
		
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if(ConfigHolder.COMMON.jeiSupport.get() == true) {
			registration.addRecipes(JEIHelper.getRecipes(GrinderCrafting.GRINDER_RECIPE), grinderCategory.getUid());
			registration.addRecipes(JEIHelper.getRecipes(BathCrafting.BATH_RECIPE), bathCategory.getUid());
		}
	}

}
