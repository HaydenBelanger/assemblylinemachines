package me.haydenb.assemblylinemachines.world;

import java.util.Map.Entry;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Generation {

	private static final FillerBlockType END_RULE_TEST = FillerBlockType.create("end_stone", "end_stone", (bs) -> bs.getBlock().equals(Blocks.END_STONE));
	
	@SubscribeEvent
	public static void completeLoad(FMLLoadCompleteEvent e) {

		ConfiguredFeature<?, ?>[] features = new ConfiguredFeature<?, ?>[3];
		features[0] = Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NETHERRACK, Registry.getBlock("black_granite").getDefaultState().with(BlockBlackGranite.NATURAL_GRANITE, true), 37))
				.withPlacement(Placement.COUNT_RANGE.configure(new CountRangeConfig(7, 0, 0, 255)));
		features[1] = Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, Registry.getBlock("titanium_ore").getDefaultState(), 8))
				.withPlacement((Placement.COUNT_RANGE.configure(new CountRangeConfig(3, 0, 0, 16))));
		features[2] = Feature.ORE.withConfiguration(new OreFeatureConfig(END_RULE_TEST, Registry.getBlock("chromium_ore").getDefaultState(), 10))
				.withPlacement(Placement.COUNT_RANGE.configure(new CountRangeConfig(10, 0, 0, 255)));
		
		for(Entry<ResourceLocation, Biome> biome : ForgeRegistries.BIOMES.getEntries()) {
			
			Biome b = biome.getValue();
			for(ConfiguredFeature<?, ?> feature : features) {
				b.addFeature(Decoration.UNDERGROUND_ORES, feature);
			}
		}
	}
}
