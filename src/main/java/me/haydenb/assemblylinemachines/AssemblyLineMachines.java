package me.haydenb.assemblylinemachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.FluidRegistry;
import me.haydenb.assemblylinemachines.registry.SoundRegistry;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.*;
import me.haydenb.assemblylinemachines.registry.plugins.PluginTOP;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AssemblyLineMachines.MODID)
public final class AssemblyLineMachines{
	public static final String MODID = "assemblylinemachines";
	private static ModContainer modInfo = null;
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	public AssemblyLineMachines() {
		
		//Fluids and Sounds are registered in a separate class using Deferred Registries. All others are registered in Registry.class.
		FluidRegistry.FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
		SoundRegistry.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		ModLoadingContext mlc = ModLoadingContext.get();
		mlc.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
		
		
		HashPacketImpl.INSTANCE.registerMessage(HashPacketImpl.ID++, PacketData.class, new EncoderConsumer(), new DecoderConsumer(), new MessageHandler());
		
		//Mekanism is disabled due to non-update.
		/*
		if(PluginMekanism.get().isMekanismInstalled()) {
			PluginMekanism.get().registerAllGas();
		}
		*/
		PluginTOP.register();
		
		
	}
	
	public static ModContainer getModContainer() {
		
		if(modInfo == null) {
			modInfo = ModList.get().getModContainerById(MODID).orElse(null);
		}
		return modInfo;
	}
	
	
}