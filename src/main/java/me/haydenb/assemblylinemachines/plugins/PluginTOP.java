package me.haydenb.assemblylinemachines.plugins;

import java.util.function.Function;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class PluginTOP {

	public static void register() {
		if(ModList.get().isLoaded("theoneprobe")) {
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", PluginTOPRegistry::new);
			AssemblyLineMachines.LOGGER.debug("The One Probe plugin connected to Assembly Line Machines.");
		}
	}

	public static class PluginTOPRegistry implements Function<ITheOneProbe, Void>{

		@Override
		public Void apply(ITheOneProbe probe) {

			probe.registerProvider(new IProbeInfoProvider() {

				@Override
				public ResourceLocation getID() {
					return new ResourceLocation(AssemblyLineMachines.MODID, "top");
				}

				@Override
				public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState state, IProbeHitData data) {

					if(world.getBlockEntity(data.getPos()) instanceof TOPProvider) {
						((TOPProvider) world.getBlockEntity(data.getPos())).addProbeInfo(mode, info, player, world, state, data);
					}else if(state.getBlock() instanceof TOPProvider) {
						((TOPProvider) state.getBlock()).addProbeInfo(mode, info, player, world, state, data);
					}

				}
			});

			return null;
		}

		public static interface TOPProvider{
			public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data);
		}
	}



}