package me.haydenb.assemblylinemachines.plugins.other;

import java.util.function.Function;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class PluginTOP {

	private static boolean registered = false;
	
	public static void register() {
		if(!registered) {
			registered = true;
			if(ModList.get().isLoaded("theoneprobe")) {
				InterModComms.sendTo("theoneprobe", "getTheOneProbe", PluginTOPRegistry::new);
			}
		}
	}
	
	static class PluginTOPRegistry implements Function<ITheOneProbe, Void>{
		
		@Override
		public Void apply(ITheOneProbe probe) {
			AssemblyLineMachines.LOGGER.info("The One Probe detected in installation. Adding support...");
			
			probe.registerProvider(new IProbeInfoProvider() {
				
				@Override
				public String getID() {
					return new ResourceLocation(AssemblyLineMachines.MODID, "top").toString();
				}
				
				@Override
				public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData data) {
					
					if(world.getTileEntity(data.getPos()) instanceof TOPProvider) {
						((TOPProvider) world.getTileEntity(data.getPos())).addProbeInfo(mode, info, player, world, state, data);
					}
					
				}
			});
			
			return null;
		}
	}
	
	public static interface TOPProvider{
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState state, IProbeHitData data);
	}
	
}
