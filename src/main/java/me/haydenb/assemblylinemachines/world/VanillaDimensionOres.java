package me.haydenb.assemblylinemachines.world;

import java.util.List;

import com.google.common.collect.ImmutableList;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class VanillaDimensionOres {

	public static Holder<PlacedFeature> titaniumOre = null;
	public static Holder<PlacedFeature> blackGranite = null;
	public static Holder<PlacedFeature> chromiumOre = null;
	
	//Where the initialized features are actually added to the biomes during world load.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void biomeLoadingEvent(BiomeLoadingEvent event) {
		
		if(titaniumOre != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, titaniumOre);
		if(blackGranite != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, blackGranite);
		if(chromiumOre != null && (ConfigHolder.getCommonConfig().chromiumOnDragonIsland.get() || !event.getName().equals(Biomes.THE_END.location()))) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, chromiumOre);
	}
	
	//Where the features are initialized during setup.
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
	public static class OreGenerationInitialization{
		
		@SubscribeEvent
		public static void initializeFeatures(FMLCommonSetupEvent event) {
			ALMCommonConfig cfg = ConfigHolder.getCommonConfig();
			
			//Titanium, Deepslate Titanium, Corrupt Titanium
			if(cfg.titaniumVeinSize.get() != 0 && cfg.titaniumFrequency.get() != 0) {
				ImmutableList<TargetBlockState> targetList = ImmutableList.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState()),
						OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState()),
						OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_stone")), Registry.getBlock("corrupt_titanium_ore").defaultBlockState()));
				
				titaniumOre = getFeature(targetList, cfg.titaniumVeinSize.get(), getAbsolute(cfg.titaniumOreGenStyle.get(), cfg.titaniumMinHeight.get(), cfg.titaniumMaxHeight.get()), cfg.titaniumFrequency.get());
			}
			
			//Black Granite
			if(cfg.blackGraniteVeinSize.get() != 0 && cfg.blackGraniteFrequency.get() != 0) {
				BlockState state = Registry.getBlock("black_granite").defaultBlockState();
				state = cfg.blackGraniteSpawnsWithNaturalTag.get() ? state.setValue(BlockBlackGranite.NATURAL_GRANITE, true) : state;
				blackGranite = getFeature(getBasicList(OreFeatures.NETHER_ORE_REPLACEABLES, state), cfg.blackGraniteVeinSize.get(), cfg.blackGraniteFrequency.get());
			}
			
			//Chromium
			if(cfg.chromiumVeinSize.get() != 0 && cfg.chromiumFrequency.get() != 0) {
				chromiumOre = getFeature(getBasicList(new BlockMatchTest(Blocks.END_STONE), Registry.getBlock("chromium_ore").defaultBlockState()), cfg.chromiumVeinSize.get(), cfg.chromiumFrequency.get());
			}
			
		}	
		
		private static Holder<PlacedFeature> getFeature(List<TargetBlockState> targets, int veinsize, int freq) {
			return getFeature(targets, veinsize, (HeightRangePlacement) PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, freq);
		}
		private static Holder<PlacedFeature> getFeature(List<TargetBlockState> targets, int veinsize, HeightRangePlacement range, int freq) {
			return Holder.direct(new PlacedFeature(Holder.direct(new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, veinsize))), List.of(InSquarePlacement.spread(), CountPlacement.of(freq), BiomeFilter.biome())));
		}
		
		private static List<TargetBlockState> getBasicList(RuleTest target, BlockState result){
			return ImmutableList.of(OreConfiguration.target(target, result));
		}
		
		private static HeightRangePlacement getAbsolute(OreGenOptions ogo, int min, int max) {
			return ogo == OreGenOptions.TRIANGLE ? HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)) : HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max));
		}
	}
	
}
