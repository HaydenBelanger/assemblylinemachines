package me.haydenb.assemblylinemachines.registry;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class CommonSetup {

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) throws Exception{
		
		FluidRegistry.updateInternalStoredFluids();
	}
}
