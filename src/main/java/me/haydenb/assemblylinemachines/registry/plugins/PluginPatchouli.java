package me.haydenb.assemblylinemachines.registry.plugins;

@Deprecated
public class PluginPatchouli {

	//Patchouli is not-yet-updated.
	/*
	private static PatchouliInterface pInt = null;
	
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
	
		default void openBook(ServerPlayer player, Level world) {}
		
		default boolean isPatchouliInstalled() {
			return false;
		}
	}
	
	static class PatchouliPresent implements PatchouliInterface{

		@SuppressWarnings("deprecation")
		@Override
		public void openBook(ServerPlayer player, Level world) {
			
			PatchouliAPI.instance.openBookGUI(player, new ResourceLocation("assemblylinemachines", "walkthrough"));
		}

		@Override
		public boolean isPatchouliInstalled() {
			return true;
		}
		
		
		
	}
	
	static class PatchouliNotPresent implements PatchouliInterface{
		
		
	}
	
	*/
	
}
