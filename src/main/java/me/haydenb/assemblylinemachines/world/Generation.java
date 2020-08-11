package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Generation {
	
	@SubscribeEvent
	public static void completeLoad(FMLLoadCompleteEvent e) {
		Ore.generateOverworldOre(Registry.getBlock("titanium_ore").getDefaultState(), 3, 2, 8, 10);
		Ore.generateNetherOre(Registry.getBlock("black_granite").getDefaultState().with(BlockBlackGranite.NATURAL_GRANITE, true), 2, 1, 255, 25);
		Ore.generateEndOre(Registry.getBlock("chromium_ore").getDefaultState(), 4, 1, 255, 10);
	}
}
