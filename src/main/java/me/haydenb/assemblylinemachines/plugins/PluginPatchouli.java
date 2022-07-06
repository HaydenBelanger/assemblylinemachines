package me.haydenb.assemblylinemachines.plugins;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

public class PluginPatchouli {

	public static final Lazy<PatchouliInterface> INTERFACE = Lazy.of(() -> {
		if(ModList.get().isLoaded("patchouli")) {
			try {
				return Class.forName("me.haydenb.assemblylinemachines.plugins.PluginPatchouli$PatchouliPresent").asSubclass(PatchouliInterface.class).getDeclaredConstructor().newInstance();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return new PatchouliInterface() {};
	});

	public interface PatchouliInterface{

		default ItemStack getBookItem() {
			return ItemStack.EMPTY;
		}

	}

	static class PatchouliPresent implements PatchouliInterface{

		@Override
		public ItemStack getBookItem() {
			return PatchouliAPI.get().getBookStack(new ResourceLocation(AssemblyLineMachines.MODID, "assembly_lines_and_you"));
		}
	}
}