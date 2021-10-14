package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ASMConfig;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class Generation {

	private static final RuleTest END_RULE_TEST = new BlockMatchTest(Blocks.END_STONE);
	private static final RangeDecoratorConfiguration FULL_HEIGHT = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.top()));
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void generateOres(BiomeLoadingEvent event) {
		
		ASMConfig cfg = ConfigHolder.COMMON;
		
		int blgSize = cfg.blackGraniteVeinSize.get();
		int blgFreq = cfg.blackGraniteFrequency.get();
		if(blgSize != 0 && blgFreq != 0) {
			BlockState state = Registry.getBlock("black_granite").defaultBlockState();
			if(cfg.blackGraniteSpawnsWithNaturalTag.get()) {
				state = state.setValue(BlockBlackGranite.NATURAL_GRANITE, true);
			}
			ConfiguredFeature<?, ?> cf = Feature.ORE.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, state, blgSize)).range(FULL_HEIGHT).squared().count(blgFreq);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, cf);
			
		}
		
		int titSize = cfg.titaniumVeinSize.get();
		int dstitFreq = cfg.deepslateTitaniumFrequency.get();
		int titFreq = cfg.titaniumFrequency.get();
		if(titSize != 0) {
			if(titFreq != 0) {
				ConfiguredFeature<?, ?> cf = Feature.ORE.configured(new OreConfiguration(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState(), titSize))
						.range(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(cfg.titaniumMinHeight.get()), VerticalAnchor.aboveBottom(cfg.titaniumMaxHeight.get())))).squared().count(titFreq);
				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, cf);
			}
			if(dstitFreq != 0) {
				ConfiguredFeature<?, ?> cf = Feature.ORE.configured(new OreConfiguration(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState(), titSize))
						.range(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(cfg.titaniumMinHeight.get()), VerticalAnchor.aboveBottom(cfg.titaniumMaxHeight.get())))).squared().count(dstitFreq);
				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, cf);
			}
		}
		
		int chrSize = cfg.chromiumVeinSize.get();
		int chrFreq = cfg.chromiumFrequency.get();
		if(chrSize != 0 && chrFreq != 0 && (cfg.chromiumOnDragonIsland.get() || !event.getName().equals(Biomes.THE_END.location()))) {
			ConfiguredFeature<?, ?> cf = Feature.ORE.configured(new OreConfiguration(END_RULE_TEST, Registry.getBlock("chromium_ore").defaultBlockState(), chrSize)).range(FULL_HEIGHT).squared().count(chrFreq);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, cf);
		}
		
	}
}
