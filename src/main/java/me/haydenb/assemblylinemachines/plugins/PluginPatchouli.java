package me.haydenb.assemblylinemachines.plugins;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

public class PluginPatchouli {

	private static PatchouliInterface pInt = null;
	
	@SuppressWarnings("deprecation")
	public static PatchouliInterface get() {
		
		if(pInt == null) {
			if(ModList.get().isLoaded("patchouli")) {
				try {
					pInt = Class.forName("me.haydenb.assemblylinemachines.plugins.PluginPatchouli$PatchouliPresent").asSubclass(PatchouliInterface.class).newInstance();
					AssemblyLineMachines.LOGGER.info("Detected Patchouli in installation. Creating in-game walkthrough book...");
				}catch(Exception e) {
					pInt = new PatchouliNotPresent();
					e.printStackTrace();
				}
			}else {
				pInt = new PatchouliNotPresent();
			}
			
		}
		
		return pInt;
	}
	
	public interface PatchouliInterface{
	
		default void openBook(ServerPlayer player, Level world) {}
		
		default boolean isPatchouliInstalled() {
			return false;
		}
	}
	
	static class PatchouliPresent implements PatchouliInterface{

		@Override
		public void openBook(ServerPlayer player, Level world) {
			
			PatchouliAPI.get().openBookGUI(player, new ResourceLocation("assemblylinemachines", "walkthrough"));
		}

		@Override
		public boolean isPatchouliInstalled() {
			return true;
		}
		
		
		
	}
	
	static class PatchouliNotPresent implements PatchouliInterface{
		
		
	}
	
}