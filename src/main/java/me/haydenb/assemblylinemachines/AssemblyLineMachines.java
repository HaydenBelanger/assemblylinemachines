package me.haydenb.assemblylinemachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.haydenb.assemblylinemachines.plugins.PluginTOP;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.world.VanillaDimensionOres;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;

@Mod(AssemblyLineMachines.MODID)
public final class AssemblyLineMachines{
	
	public static final String MODID = "assemblylinemachines";
	public static final Lazy<ModContainer> MOD_CONTAINER = Lazy.of(() -> ModList.get().getModContainerById(MODID).orElse(null));
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public AssemblyLineMachines() {
		
		//Registers all sided-config files.
		ConfigHolder.registerSpecs(ModLoadingContext.get());
		
		//Registers PacketHandler.
		PacketHandler.register();
		
		//Registers VanillaDimensionOres events manually as multi-bus events.
		VanillaDimensionOres.generationBusRegistration();
		
		//The One Probe plugin registration.
		PluginTOP.register();
	}
}