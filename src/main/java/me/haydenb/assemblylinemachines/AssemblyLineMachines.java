package me.haydenb.assemblylinemachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.haydenb.assemblylinemachines.plugins.PluginTOP;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(AssemblyLineMachines.MODID)
public final class AssemblyLineMachines{
	public static final String MODID = "assemblylinemachines";
	private static ModContainer modInfo = null;
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public AssemblyLineMachines() {
		
		//Registers config to current installation.
		ModLoadingContext mlc = ModLoadingContext.get();
		mlc.registerConfig(Type.COMMON, ConfigHolder.getCommonSpec());
		
		//Registers PacketHandler.
		PacketHandler.INSTANCE.registerMessage(PacketHandler.ID++, PacketData.class, new EncoderConsumer(), new DecoderConsumer(), new MessageHandler());
		
		//The One Probe plugin registration.
		PluginTOP.register();
	}
	
	public static ModContainer getModContainer() {
		
		if(modInfo == null) {
			modInfo = ModList.get().getModContainerById(MODID).orElse(null);
		}
		return modInfo;
	}
	
	
}