package me.haydenb.assemblylinemachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.haydenb.assemblylinemachines.plugins.PluginMekanism;
import me.haydenb.assemblylinemachines.plugins.PluginTOP;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import me.haydenb.assemblylinemachines.registry.config.ConfigMatchTest;
import me.haydenb.assemblylinemachines.registry.utils.WhitelistBiomeFilter;
import me.haydenb.assemblylinemachines.world.CapabilityBooks;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AssemblyLineMachines.MODID)
public final class AssemblyLineMachines{
	
	public static final String MODID = "assemblylinemachines";
	public static final Lazy<ModContainer> MOD_CONTAINER = Lazy.of(() -> ModList.get().getModContainerById(MODID).orElse(null));
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public AssemblyLineMachines() {
		
		//Registers all sided-config files.
		ALMConfig.registerSpecs(ModLoadingContext.get());
		
		//Registers PacketHandler.
		PacketHandler.register();
		
		//The One Probe plugin registration.
		PluginTOP.register();
		
		//Registers Book Capability events using listeners.
		CapabilityBooks.registerAllEvents();
		
		//Deferred vanilla-type registries.
		ConfigMatchTest.RULE_TEST_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		WhitelistBiomeFilter.PLACEMENT_MODIFIER_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		//Registers gases if Mekanism is installed.
		PluginMekanism.INTERFACE.get().registerGases();
	}
}