package me.haydenb.assemblylinemachines.plugins.other;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

public class PatchouliALMImpl {

	private static PatchouliInterface pInt = null;
	
	@SuppressWarnings("deprecation")
	public static PatchouliInterface get() {
		
		if(pInt == null) {
			if(ModList.get().isLoaded("patchouli")) {
				try {
					pInt = Class.forName("me.haydenb.assemblylinemachines.plugins.other.PatchouliALMImpl$PatchouliPresent").asSubclass(PatchouliInterface.class).newInstance();
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
		
		void openBook(ServerPlayerEntity player, World world);
		
		boolean isPatchouliInstalled();
	}
	
	static class PatchouliPresent implements PatchouliInterface{

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

		@Override
		public void openBook(ServerPlayerEntity player, World world) {
			
			player.sendMessage(new StringTextComponent("Patchouli is not installed! Please install Patchouli to access the Guidebook! https://www.curseforge.com/minecraft/mc-mods/patchouli"), player.getUniqueID());
			
		}

		@Override
		public boolean isPatchouliInstalled() {
			return false;
		}
		
		
	}
	
}
