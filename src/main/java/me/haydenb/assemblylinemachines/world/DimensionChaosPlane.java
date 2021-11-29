package me.haydenb.assemblylinemachines.world;

import java.util.*;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.chaosplane.CorruptTallGrassBlock.BrainCactusBlock;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.Features.Decorators;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.blockplacers.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
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

	//Ranges, for plant and tree generation.
	private static final RangeDecoratorConfiguration PLANTS_RANGE = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(64), VerticalAnchor.aboveBottom(170)));
	private static final RangeDecoratorConfiguration CHAOSBARK_RANGE = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(75), VerticalAnchor.aboveBottom(120)));

	//Biome loader to actually add the features to the appropriate biomes.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void addFeaturesToBiome(BiomeLoadingEvent event) {
		if(CORRUPT_BIOMES.contains(event.getName())) {
			int treeCount = 0;
			int plantCount = 0;
			int flowerCount = 0;
			if(event.getName().equals(CORRUPT_PLAINS.location())) {
				treeCount = 4;
				plantCount = 3;
				flowerCount = 1;
			}else if(event.getName().equals(CORRUPT_FOREST.location())) {
				treeCount = 120;
				plantCount = 2;
			}else if(event.getName().equals(CORRUPT_CLIFFS.location())) {
				treeCount = 8;
				plantCount = 1;

				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.corruptEmeraldOre);
			}else if(event.getName().equals(CORRUPT_DESERT.location())) {
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.brainCactus);
			}else if(event.getName().equals(CORRUPT_OCEAN.location())) {
				event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, ChaosPlaneOres.fleroviumOre);
			}

			if(treeCount != 0) {
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosbarkTreeGrower.chaosbarkTree.range(CHAOSBARK_RANGE).squared().count(treeCount));
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.chaosPlaneGrass.range(PLANTS_RANGE).squared().count(plantCount));
				if(flowerCount != 0) {
					event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosPlaneVegetation.chaosPlaneFlowers.range(PLANTS_RANGE).squared().count(flowerCount));
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

		public static ConfiguredFeature<?, ?> corruptCoalOre;
		public static ConfiguredFeature<?, ?> corruptCopperOre;
		public static ConfiguredFeature<?, ?> corruptDiamondOre;
		public static ConfiguredFeature<?, ?> corruptEmeraldOre;
		public static ConfiguredFeature<?, ?> corruptGoldOre;
		public static ConfiguredFeature<?, ?> corruptIronOre;
		public static ConfiguredFeature<?, ?> corruptLapisOre;
		public static ConfiguredFeature<?, ?> corruptRedstoneOre;
		public static ConfiguredFeature<?, ?> fleroviumOre;
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

			corruptEmeraldOre = Feature.REPLACE_SINGLE_BLOCK.configured(chaosPlaneEmeraldRBC).rangeUniform(VerticalAnchor.absolute(4), VerticalAnchor.absolute(31)).squared().count(UniformInt.of(3, 8));
			fleroviumOre = Feature.REPLACE_SINGLE_BLOCK.configured(fleroviumRBC).rangeUniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(63)).squared().count(UniformInt.of(12, 20));
		}

		private static ConfiguredFeature<?, ?> getCorruptOreGenerator(String block, int size, int freq, VerticalAnchor min, VerticalAnchor max){

			return Feature.ORE.configured(new OreConfiguration(chaosPlaneRule, Registry.getBlock(block).defaultBlockState(), size))
					.rangeUniform(min, max).squared().count(freq);
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

		public static ConfiguredFeature<TreeConfiguration, ?> chaosbarkTree;

		@Override
		protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random pRandom, boolean pLargeHive) {

			return chaosbarkTree;
		}

		public static void registerTreeGen() {
			BlockStateProvider bspTrunk = new SimpleStateProvider(Registry.getBlock("chaosbark_log").defaultBlockState());
			TrunkPlacer trunkPlacer = new StraightTrunkPlacer(4, 2, 0);
			BlockStateProvider bspLeaves = new SimpleStateProvider(Registry.getBlock("chaosbark_leaves").defaultBlockState());

			FoliagePlacer foliagePlacer = new BlobFoliagePlacer(UniformInt.of(2, 2), UniformInt.of(0, 0), 3);
			BlockStateProvider bspSapling = new SimpleStateProvider(Registry.getBlock("chaosbark_sapling").defaultBlockState());
			FeatureSize size = new TwoLayersFeatureSize(1, 0, 1);
			BlockStateProvider bspDirt = new SimpleStateProvider(Registry.getBlock("corrupt_dirt").defaultBlockState());

			chaosbarkTree = new TreeFeature(TreeConfiguration.CODEC).configured(new TreeConfiguration.TreeConfigurationBuilder(bspTrunk, trunkPlacer, bspLeaves, bspSapling, foliagePlacer, size).dirt(bspDirt).forceDirt().build());
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
			RandomPatchConfiguration grassPatch = new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Registry.getBlock("chaosweed").defaultBlockState(), 7)
					.add(Registry.getBlock("tall_chaosweed").defaultBlockState(), 4).add(Registry.getBlock("blooming_chaosweed").defaultBlockState(), 2).add(Registry.getBlock("tall_blooming_chaosweed").defaultBlockState(), 1).build()), SingleOrDoublePlantPlacer.INSTANCE).tries(64).noProjection().build();
			RandomPatchConfiguration flowerPatch = new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Registry.getBlock("prism_rose").defaultBlockState(), 4)
					.add(Registry.getBlock("mandelbloom").defaultBlockState(), 1).build()), SimpleBlockPlacer.INSTANCE).tries(64).build();
			chaosPlaneGrass = Feature.RANDOM_PATCH.configured(grassPatch).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(8);
			chaosPlaneFlowers = Feature.FLOWER.configured(flowerPatch).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).count(3);
			brainCactus = Feature.RANDOM_PATCH.configured(new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Registry.getBlock("brain_cactus").defaultBlockState()), ColumnPlacerWithDifferentTop.of(BiasedToBottomInt.of(1, 3), BrainCactusBlock.CAP, true)).tries(10).noProjection().build()).decorated(Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(10);
		}

		public static class SingleOrDoublePlantPlacer extends SimpleBlockPlacer{

			public static final SingleOrDoublePlantPlacer INSTANCE = new SingleOrDoublePlantPlacer();
			public static final Codec<SingleOrDoublePlantPlacer> CODEC = Codec.unit(() -> {return INSTANCE;});
			public static final BlockPlacerType<?> TYPE = new BlockPlacerType<>(CODEC);

			@Override
			public void place(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Random pRandom) {
				if(pState.getBlock() instanceof DoublePlantBlock) {
					DoublePlantBlock.placeAt(pLevel, pState, pPos, 2);
				}else {
					super.place(pLevel, pPos, pState, pRandom);
				}

			}

			@Override
			protected BlockPlacerType<?> type() {
				return TYPE;
			}

		}

		public static class ColumnPlacerWithDifferentTop<T extends Comparable<T>> extends ColumnPlacer {

			public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create((p_160711_) -> {
				return p_160711_.group(IntProvider.NON_NEGATIVE_CODEC.fieldOf("size").forGetter((p_160713_) -> {
					return p_160713_.size;
				})).apply(p_160711_, ColumnPlacerWithDifferentTop::new);
			});
			public static final BlockPlacerType<?> TYPE = new BlockPlacerType<>(CODEC);
			
			private final Property<T> property;
			private final T value;

			private ColumnPlacerWithDifferentTop(IntProvider provider) {
				this(provider, null, null);
			}

			private ColumnPlacerWithDifferentTop(IntProvider provider, Property<T> property, T value) {
				super(provider);
				this.property = property;
				this.value = value;
			}

			@Override
			public void place(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Random pRandom) {
				BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
				int i = this.size.sample(pRandom);

				for(int j = 0; j < i; ++j) {

					BlockState setState = property != null && value != null ? j == i - 1 ? pState.setValue(property, value) : pState : pState;

					pLevel.setBlock(blockpos$mutableblockpos, setState, 2);
					blockpos$mutableblockpos.move(Direction.UP);
				}
			}
			
			public static BlockPlacerType<?> getType() {
				return TYPE;
			}

			public static <T extends Comparable<T>> ColumnPlacerWithDifferentTop<T> of(IntProvider provider, Property<T> property, T value) {
				return new ColumnPlacerWithDifferentTop<T>(provider, property, value);
			}


		}
	}

	//Chunk generation - seeded additional dimensions using Mixins.
	public static class SeededNoiseBasedChunkGenerator extends NoiseBasedChunkGenerator {

		public static final Codec<SeededNoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((p_64405_) -> {
			return p_64405_.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((p_158489_) -> {
				return p_158489_.biomeSource;
			}), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_158458_) -> {
				return p_158458_.settings;
			})).apply(p_64405_, p_64405_.stable(SeededNoiseBasedChunkGenerator::new));
		});
		/**
		 * Note that the first item of the seed array is the only one read.
		 */
		public SeededNoiseBasedChunkGenerator(BiomeSource source, Supplier<NoiseGeneratorSettings> settings, long... seed) {
			super(source, getFirstOrZero(seed), settings);

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
			return new SeededNoiseBasedChunkGenerator(this.getBiomeSource().withSeed(pSeed), this.settings, pSeed);
		}

	}	



}
