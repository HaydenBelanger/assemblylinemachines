package me.haydenb.assemblylinemachines.registry.plugins.jei;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;

public class JEIHelper {

	public static ResourceLocation getGUIPath(String name) {
		return new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/jei/" + name + ".png");
	}
	
	public static <C extends Container, T extends Recipe<C>> List<T> getRecipes(RecipeType<T> recipe){
		
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		RecipeManager rm = world.getRecipeManager();
		return rm.getRecipesFor(recipe, null, world);
	}
	
}
