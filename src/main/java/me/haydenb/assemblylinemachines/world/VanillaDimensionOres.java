package me.haydenb.assemblylinemachines.world;

import com.google.common.collect.ImmutableList;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
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
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.Predicates;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class VanillaDimensionOres {

	public static ConfiguredFeature<?, ?> titaniumOre = null;
	public static ConfiguredFeature<?, ?> blackGranite = null;
	public static ConfiguredFeature<?, ?> chromiumOre = null;
	
	//Where the initialized features are actually added to the biomes during world load.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void biomeLoadingEvent(BiomeLoadingEvent event) {
		
		if(titaniumOre != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, titaniumOre);
		if(blackGranite != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, blackGranite);
		if(chromiumOre != null && (ConfigHolder.COMMON.chromiumOnDragonIsland.get() || !event.getName().equals(Biomes.THE_END.location()))) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, chromiumOre);
	}
	
	//Where the features are initialized during setup.
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
	public static class OreGenerationInitialization{
		
		@SubscribeEvent
		public static void initializeFeatures(FMLCommonSetupEvent event) {
			ASMConfig cfg = ConfigHolder.COMMON;
			
			//Titanium, Deepslate Titanium, Corrupt Titanium
			if(cfg.titaniumVeinSize.get() != 0 && cfg.titaniumFrequency.get() != 0) {
				ImmutableList<TargetBlockState> targetList = ImmutableList.of(OreConfiguration.target(Predicates.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState()),
						OreConfiguration.target(Predicates.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState()),
						OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_stone")), Registry.getBlock("corrupt_titanium_ore").defaultBlockState()));
				titaniumOre = Feature.ORE.configured(new OreConfiguration(targetList, cfg.titaniumVeinSize.get())).rangeUniform(VerticalAnchor.absolute(cfg.titaniumMinHeight.get()), VerticalAnchor.absolute(cfg.titaniumMaxHeight.get())).squared().count(cfg.titaniumFrequency.get());
			}
			
			//Black Granite
			if(cfg.blackGraniteVeinSize.get() != 0 && cfg.blackGraniteFrequency.get() != 0) {
				BlockState state = Registry.getBlock("black_granite").defaultBlockState();
				state = cfg.blackGraniteSpawnsWithNaturalTag.get() ? state.setValue(BlockBlackGranite.NATURAL_GRANITE, true) : state;
				blackGranite = Feature.ORE.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, state, cfg.blackGraniteVeinSize.get())).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.top()).squared().count(cfg.blackGraniteFrequency.get());
			}
			
			//Chromium
			if(cfg.chromiumVeinSize.get() != 0 && cfg.chromiumFrequency.get() != 0) {
				chromiumOre = Feature.ORE.configured(new OreConfiguration(new BlockMatchTest(Blocks.END_STONE), Registry.getBlock("chromium_ore").defaultBlockState(), cfg.chromiumVeinSize.get())).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.top()).squared().count(cfg.blackGraniteFrequency.get());
			}
			
		}
	}
	
}
