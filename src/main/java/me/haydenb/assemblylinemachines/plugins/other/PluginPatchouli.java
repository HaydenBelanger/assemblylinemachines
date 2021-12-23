package me.haydenb.assemblylinemachines.plugins.other;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

public class PluginPatchouli {

	private static PatchouliInterface pInt = null;
	
	@SuppressWarnings("deprecation")
	public static PatchouliInterface get() {
		
		if(pInt == null) {
			if(ModList.get().isLoaded("patchouli")) {
				try {
					pInt = Class.forName("me.haydenb.assemblylinemachines.plugins.other.PluginPatchouli$PatchouliPresent").asSubclass(PatchouliInterface.class).newInstance();
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
	
		default void openBook(ServerPlayerEntity player, World world) {}
		
		default boolean isPatchouliInstalled() {
			return false;
		}
	}
	
	static class PatchouliPresent implements PatchouliInterface{

		@SuppressWarnings("deprecation")
		@Override
		public void openBook(ServerPlayerEntity player, World world) {
			
			PatchouliAPI.instance.openBookGUI(player, new ResourceLocation("assemblylinemachines", "walkthrough"));
		}

		@Override
		public boolean isPatchouliInstalled() {
			return true;
		}
		
		
		
	}
	
	static class PatchouliNotPresent implements PatchouliInterface{
		
		
	}
	
}
