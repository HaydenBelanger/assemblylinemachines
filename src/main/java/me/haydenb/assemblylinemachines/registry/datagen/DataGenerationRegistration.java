package me.haydenb.assemblylinemachines.registry.datagen;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class DataGenerationRegistration {

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) throws Exception {
		event.getGenerator().addProvider(new MineableBlockProvider(event.getGenerator(), event.getExistingFileHelper()));
		event.getGenerator().run();
	}
}
