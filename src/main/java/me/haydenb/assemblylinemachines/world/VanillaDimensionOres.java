package me.haydenb.assemblylinemachines.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class VanillaDimensionOres {

	public static final ArrayList<Pair<Predicate<ResourceLocation>, Lazy<Holder<PlacedFeature>>>> ORES = new ArrayList<>();
	
	static {
		
		//TITANIUM
		ORES.add(Pair.of((rl) -> true, Lazy.of(() -> {
			if(cfg().titaniumVeinSize.get() != 0 && cfg().titaniumFrequency.get() != 0) {
				List<TargetBlockState> targetList = List.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState()),
						OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState()),
						OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_stone")), Registry.getBlock("corrupt_titanium_ore").defaultBlockState()));
				
				return getFeature("ore_titanium", targetList, cfg().titaniumVeinSize.get(), getAbsolute(cfg().titaniumOreGenStyle.get(), cfg().titaniumMinHeight.get(), cfg().titaniumMaxHeight.get()), cfg().titaniumFrequency.get());
			}
			return null;
		})));
		
		//BLACK GRANITE
		ORES.add(Pair.of((rl) -> true, Lazy.of(() -> {
			if(cfg().blackGraniteVeinSize.get() != 0 && cfg().blackGraniteFrequency.get() != 0) {
				BlockState state = Registry.getBlock("black_granite").defaultBlockState();
				state = cfg().blackGraniteSpawnsWithNaturalTag.get() ? state.setValue(BlockBlackGranite.NATURAL_GRANITE, true) : state;
				return getFeature("ore_black_granite", getBasicList(OreFeatures.NETHER_ORE_REPLACEABLES, state), cfg().blackGraniteVeinSize.get(), cfg().blackGraniteFrequency.get());
			}
			return null;
		})));
		
		//CHROMIUM
		ORES.add(Pair.of((rl) ->{
			return ConfigHolder.getCommonConfig().chromiumOnDragonIsland.get() || !rl.equals(Biomes.THE_END.location());
		}, Lazy.of(() -> {
			if(cfg().chromiumVeinSize.get() != 0 && cfg().chromiumFrequency.get() != 0) {
				return getFeature("ore_chromium", getBasicList(new BlockMatchTest(Blocks.END_STONE), Registry.getBlock("chromium_ore").defaultBlockState()), cfg().chromiumVeinSize.get(), cfg().chromiumFrequency.get());
			}
			return null;
		})));
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void biomeLoadingEvent(BiomeLoadingEvent event) {
		ORES.forEach((ore) -> {
			if(ore.getFirst().test(event.getName()) && ore.getSecond().get() != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ore.getSecond().get());
		});
	}
	
	//Where the features are initialized during setup.
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
	public static class OreGenerationInitialization{
		
		@SubscribeEvent
		public static void initializeFeatures(FMLCommonSetupEvent event) {
			
			event.enqueueWork(() -> ORES.forEach((ore) -> ore.getSecond().get()));
		}
	}
	
	private static Holder<PlacedFeature> getFeature(String name, List<TargetBlockState> targets, int veinsize, int freq) {
		return getFeature(name, targets, veinsize, (HeightRangePlacement) PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, freq);
	}
	private static Holder<PlacedFeature> getFeature(String name, List<TargetBlockState> targets, int veinsize, HeightRangePlacement range, int freq) {
		Holder<ConfiguredFeature<OreConfiguration, ?>> holderCF = FeatureUtils.register(AssemblyLineMachines.MODID + ":" + name, Feature.ORE, new OreConfiguration(targets, veinsize));
		return PlacementUtils.register(AssemblyLineMachines.MODID + ":" + name, holderCF, InSquarePlacement.spread(), CountPlacement.of(freq), BiomeFilter.biome());
	}
	
	private static List<TargetBlockState> getBasicList(RuleTest target, BlockState result){
		return List.of(OreConfiguration.target(target, result));
	}
	
	private static HeightRangePlacement getAbsolute(OreGenOptions ogo, int min, int max) {
		return ogo == OreGenOptions.TRIANGLE ? HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)) : HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max));
	}
	
	private static ALMCommonConfig cfg() {
		return ConfigHolder.getCommonConfig();
	}
	
}
