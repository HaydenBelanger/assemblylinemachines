package me.haydenb.assemblylinemachines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.*;
import me.haydenb.assemblylinemachines.plugins.other.PluginMekanism;
import me.haydenb.assemblylinemachines.plugins.other.PluginTOP;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(AssemblyLineMachines.MODID)
public final class AssemblyLineMachines{
	public static final String MODID = "assemblylinemachines";
	private static ModContainer modInfo = null;
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	public AssemblyLineMachines() {
		
		
		ModLoadingContext mlc = ModLoadingContext.get();
		mlc.registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
		
		
		HashPacketImpl.INSTANCE.registerMessage(HashPacketImpl.ID++, PacketData.class, new EncoderConsumer(), new DecoderConsumer(), new MessageHandler());
		
		if(PluginMekanism.get().isMekanismInstalled()) {
			PluginMekanism.get().registerAllGas();
		}
		
		PluginTOP.register();
	}
	
	public static ModContainer getModContainer() {
		
		if(modInfo == null) {
			modInfo = ModList.get().getModContainerById(MODID).orElse(null);
		}
		return modInfo;
	}
	
	
}