package me.haydenb.assemblylinemachines.world;

import java.util.*;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.*;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class DimensionChaosPlane {

	//Stored ResourceKeys corresponding to the Biomes, as well as the overall dimension.
	public static final ResourceKey<Biome> CORRUPT_PLAINS = create(net.minecraft.core.Registry.BIOME_REGISTRY, "corrupt_plains");
	public static final ResourceKey<Biome> CORRUPT_CLIFFS = create(net.minecraft.core.Registry.BIOME_REGISTRY, "corrupt_cliffs");
	public static final ResourceKey<Biome> CORRUPT_DESERT = create(net.minecraft.core.Registry.BIOME_REGISTRY, "corrupt_desert");
	public static final ResourceKey<Biome> CORRUPT_FOREST = create(net.minecraft.core.Registry.BIOME_REGISTRY, "corrupt_forest");
	public static final ResourceKey<Biome> CORRUPT_OCEAN = create(net.minecraft.core.Registry.BIOME_REGISTRY, "corrupt_ocean");
	
	public static final ResourceKey<DimensionType> CHAOS_PLANE_LOCATION = create(net.minecraft.core.Registry.DIMENSION_TYPE_REGISTRY, "chaos_plane");
	public static final ResourceKey<Level> CHAOS_PLANE = create(net.minecraft.core.Registry.DIMENSION_REGISTRY, "chaos_plane");
	
	private static <T> ResourceKey<T> create(ResourceKey<net.minecraft.core.Registry<T>> registry, String name){
		return ResourceKey.create(registry, new ResourceLocation(AssemblyLineMachines.MODID, name));
	}

	//Map containing all Corrupt Biomes for checking if biome is a corrupt biome.
	private static final ArrayList<ResourceLocation> CORRUPT_BIOMES = new ArrayList<>();
	static{
		CORRUPT_BIOMES.add(CORRUPT_PLAINS.location());
		CORRUPT_BIOMES.add(CORRUPT_CLIFFS.location());
		CORRUPT_BIOMES.add(CORRUPT_DESERT.location());
		CORRUPT_BIOMES.add(CORRUPT_FOREST.location());
		CORRUPT_BIOMES.add(CORRUPT_OCEAN.location());
	}

	//Biome loader to actually add the features to the appropriate biomes.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void addFeaturesToBiome(BiomeLoadingEvent event) {
		if(CORRUPT_BIOMES.contains(event.getName())) {
			int treeCount = 0;
			int plantCount = 0;
			int flowerCount = 0;
			if(event.getName().equals(CORRUPT_PLAINS.location())) {
				treeCount = 4;
				plantCount = 2;
				flowerCount = 6;
			}else if(event.getName().equals(CORRUPT_FOREST.location())) {
				treeCount = 120;
				plantCount = 4;
			}else if(event.getName().equals(CORRUPT_CLIFFS.location())) {
				treeCount = 8;
				plantCount = 6;

				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptEmeraldOre);
			}else if(event.getName().equals(CORRUPT_DESERT.location())) {
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.brainCactus.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RarityFilter.onAverageOnceEvery(6)));
			}else if(event.getName().equals(CORRUPT_OCEAN.location())) {
				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.fleroviumOre);
			}

			if(treeCount != 0) {
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosbarkTreeGrower.chaosbarkTree.placed(HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(75), VerticalAnchor.aboveBottom(120)), InSquarePlacement.spread(), CountPlacement.of(treeCount)));
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.chaosPlaneGrass.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RarityFilter.onAverageOnceEvery(plantCount)));
				if(flowerCount != 0) {
					event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.chaosPlaneFlowers.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, RarityFilter.onAverageOnceEvery(flowerCount)));
				}

			}

			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptCoalOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptCopperOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptDiamondOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptGoldOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptIronOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptLapisOre);
			event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptRedstoneOre);

		}


	}

	//Overall initialization, as a method, to call all others.
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
	public static class ChaosPlaneInitialization{

		@SubscribeEvent
		public static void initializeChaosPlaneFeatures(FMLCommonSetupEvent event) {
			ChaosPlaneOres.initializeChaosPlaneOreFeatures();
			ChaosbarkTreeGrower.registerTreeGen();
			ChaosbarkTreeGrower.patchStrippables();
			ChaosPlaneVegetation.initializeChaosPlaneVegetationFeatures();
		}
	}

	//Oregen initialization for Corrupt Ore - Excluding Corrupt Titanium
	public static class ChaosPlaneOres {

		private static RuleTest chaosPlaneRule;
		private static ReplaceBlockConfiguration chaosPlaneEmeraldRBC;
		private static ReplaceBlockConfiguration fleroviumRBC;

		public static PlacedFeature corruptCoalOre;
		public static PlacedFeature corruptCopperOre;
		public static PlacedFeature corruptDiamondOre;
		public static PlacedFeature corruptEmeraldOre;
		public static PlacedFeature corruptGoldOre;
		public static PlacedFeature corruptIronOre;
		public static PlacedFeature corruptLapisOre;
		public static PlacedFeature corruptRedstoneOre;
		public static PlacedFeature fleroviumOre;
		
		//Note that Corrupt Titanium Ore is generated in the main OreGeneration, as that could be supplied as an additional predicate to the main Titanium.

		public static void initializeChaosPlaneOreFeatures() {
			chaosPlaneRule = new BlockMatchTest(Registry.getBlock("corrupt_stone"));
			chaosPlaneEmeraldRBC = new ReplaceBlockConfiguration(ImmutableList.of(OreConfiguration.target(chaosPlaneRule, Registry.getBlock("corrupt_emerald_ore").defaultBlockState())));
			fleroviumRBC = new ReplaceBlockConfiguration(ImmutableList.of(OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_gravel")), Registry.getBlock("flerovium_ore").defaultBlockState())));

			corruptCoalOre = getCorruptOreGenerator("corrupt_coal_ore", 17, 30, VerticalAnchor.bottom(), VerticalAnchor.absolute(127));
			corruptCopperOre = getCorruptOreGenerator("corrupt_copper_ore", 10, 6, VerticalAnchor.absolute(0), VerticalAnchor.absolute(96));
			corruptDiamondOre = getCorruptOreGenerator("corrupt_diamond_ore", 8, 1, VerticalAnchor.bottom(), VerticalAnchor.absolute(15));
			corruptGoldOre = getCorruptOreGenerator("corrupt_gold_ore", 9, 2, VerticalAnchor.bottom(), VerticalAnchor.absolute(31));
			corruptIronOre = getCorruptOreGenerator("corrupt_iron_ore", 9, 20, VerticalAnchor.bottom(), VerticalAnchor.absolute(63));
			corruptLapisOre = getCorruptOreGenerator("corrupt_lapis_ore", 7, 1, VerticalAnchor.absolute(0), VerticalAnchor.absolute(30));
			corruptRedstoneOre = getCorruptOreGenerator("corrupt_redstone_ore", 8, 4, VerticalAnchor.bottom(), VerticalAnchor.absolute(15));

			corruptEmeraldOre = Feature.REPLACE_SINGLE_BLOCK.configured(chaosPlaneEmeraldRBC).placed(List.of(HeightRangePlacement.uniform(VerticalAnchor.absolute(4), VerticalAnchor.absolute(31)), InSquarePlacement.spread(), CountPlacement.of(UniformInt.of(3, 8))));
			fleroviumOre = Feature.REPLACE_SINGLE_BLOCK.configured(fleroviumRBC).placed(List.of(HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(63)), InSquarePlacement.spread(), CountPlacement.of(UniformInt.of(12, 20))));
		}

		private static PlacedFeature getCorruptOreGenerator(String block, int size, int freq, VerticalAnchor min, VerticalAnchor max){

			return Feature.ORE.configured(new OreConfiguration(chaosPlaneRule, Registry.getBlock(block).defaultBlockState(), size)).placed(List.of(HeightRangePlacement.uniform(min, max), InSquarePlacement.spread(), CountPlacement.of(freq)));
		}
	}

	//Cave carver
	public static class ChaosPlaneCarver extends CaveWorldCarver {

		private final FluidState carverFluid;
		public ChaosPlaneCarver(Codec<CaveCarverConfiguration> codec) {
			super(codec);
			this.replaceableBlocks = ImmutableSet.of(Registry.getBlock("corrupt_stone"), Registry.getBlock("corrupt_dirt"), Registry.getBlock("corrupt_sand"), Registry.getBlock("corrupt_grass"));
			carverFluid = Registry.getFluid("condensed_void").defaultFluidState();
		}

		@Override
		public BlockState getCarveState(CarvingContext pContext, CaveCarverConfiguration pConfig, BlockPos pPos, Aquifer pAquifer) {
			if(pPos.getY() <= pConfig.lavaLevel.resolveY(pContext)) {
				return carverFluid.createLegacyBlock();
			}
			return super.getCarveState(pContext, pConfig, pPos, pAquifer);
		}
	}

	//Tree generator - Including patching for STRIPPABLES map.
	public static class ChaosbarkTreeGrower extends AbstractTreeGrower{

		public static ConfiguredFeature<?, ?> chaosbarkTree;
		
		
		
		@Override
		protected ConfiguredFeature<?, ?> getConfiguredFeature(Random p_60014_, boolean p_60015_) {
			return chaosbarkTree;
		}

		public static void registerTreeGen() {
			BlockStateProvider bspTrunk = BlockStateProvider.simple(Registry.getBlock("chaosbark_log").defaultBlockState());
			TrunkPlacer trunkPlacer = new StraightTrunkPlacer(4, 2, 0);
			BlockStateProvider bspLeaves = BlockStateProvider.simple(Registry.getBlock("chaosbark_leaves").defaultBlockState());

			FoliagePlacer foliagePlacer = new BlobFoliagePlacer(UniformInt.of(2, 2), UniformInt.of(0, 0), 3);
			FeatureSize size = new TwoLayersFeatureSize(1, 0, 1);
			BlockStateProvider bspDirt = BlockStateProvider.simple(Registry.getBlock("corrupt_dirt").defaultBlockState());

			chaosbarkTree = new TreeFeature(TreeConfiguration.CODEC).configured(new TreeConfiguration.TreeConfigurationBuilder(bspTrunk, trunkPlacer, bspLeaves, foliagePlacer, size).dirt(bspDirt).forceDirt().build());
		}

		public static void patchStrippables() {

			AssemblyLineMachines.LOGGER.info("Patching Strippable Logs to include Chaosbark...");

			HashMap<Block, Block> strippableMap = new HashMap<>();

			for(Block b : AxeItem.STRIPPABLES.keySet()) {
				strippableMap.put(b, AxeItem.STRIPPABLES.get(b));
			}

			strippableMap.put(Registry.getBlock("chaosbark_log"), Registry.getBlock("stripped_chaosbark_log"));

			AxeItem.STRIPPABLES = strippableMap;
		}


	}

	//Vegetation generation - including BlockPlacers for the Chaosweed and Brain Cactus.
	public static class ChaosPlaneVegetation {

		public static ConfiguredFeature<?, ?> chaosPlaneGrass;
		public static ConfiguredFeature<?, ?> chaosPlaneFlowers;
		public static ConfiguredFeature<?, ?> brainCactus;

		public static void initializeChaosPlaneVegetationFeatures() {
			chaosPlaneGrass = Feature.RANDOM_PATCH.configured(getGrassPatch(64, Pair.of("chaosweed", 7), Pair.of("blooming_chaosweed", 2), Pair.of("tall_chaosweed", 4), Pair.of("tall_blooming_chaosweed", 1)));
			chaosPlaneFlowers = Feature.FLOWER.configured(getGrassPatch(64, Pair.of("prism_rose", 4), Pair.of("mandelbloom", 1)));
			brainCactus = Feature.RANDOM_PATCH.configured(FeatureUtils.simpleRandomPatchConfiguration(10, Feature.BLOCK_COLUMN.configured(BlockColumnConfiguration.simple(BiasedToBottomInt.of(1, 3), BlockStateProvider.simple(Registry.getBlock("brain_cactus")))).placed(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(Registry.getBlock("brain_cactus").defaultBlockState(), BlockPos.ZERO))))));
		}
		
		@SafeVarargs
		private static RandomPatchConfiguration getGrassPatch(int passes, Pair<String, Integer>... blocks){
			SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
			for(Pair<String, Integer> block : blocks) {
				builder.add(Registry.getBlock(block.getFirst()).defaultBlockState(), block.getSecond());
			}
			VegetationFeatures.grassPatch(null, 0);
			return VegetationFeatures.grassPatch(new WeightedStateProvider(builder.build()), passes);
		}
	}

	//Chunk generation - seeded additional dimensions using Mixins.
	public static class SeededNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {

		public static final Codec<SeededNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((p_188643_) -> {
		      return p_188643_.group(RegistryLookupCodec.create(net.minecraft.core.Registry.NOISE_REGISTRY).forGetter((p_188716_) -> {
		         return p_188716_.noises;
		      }), BiomeSource.CODEC.fieldOf("biome_source").forGetter((p_188711_) -> {
		         return p_188711_.biomeSource;
		      }), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_188652_) -> {
		         return p_188652_.settings;
		      })).apply(p_188643_, p_188643_.stable(SeededNoiseBasedChunkGenerator::new));
		   });
		/**
		 * Note that the first item of the seed array is the only one read.
		 */
		public SeededNoiseBasedChunkGenerator(net.minecraft.core.Registry<NoiseParameters> noises, BiomeSource source, Supplier<NoiseGeneratorSettings> settings, long... seed) {
			super(noises, source, getFirstOrZero(seed), settings);

		}

		private static long getFirstOrZero(long[] seed) {
			if(seed.length < 1) {
				return 0L;
			}
			return seed[0];
		}

		public static Codec<SeededNoiseBasedChunkGenerator> getCodec() {
			return CODEC;
		}

		@Override
		public ChunkGenerator withSeed(long pSeed) {
			return new SeededNoiseBasedChunkGenerator(this.noises, this.getBiomeSource().withSeed(pSeed), this.settings, pSeed);
		}

	}	



}
