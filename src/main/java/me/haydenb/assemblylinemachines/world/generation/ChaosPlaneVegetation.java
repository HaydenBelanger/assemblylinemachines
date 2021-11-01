package me.haydenb.assemblylinemachines.world.generation;

import java.util.Random;

import com.mojang.serialization.Codec;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class ChaosPlaneVegetation {

	public static ConfiguredFeature<?, ?> chaosPlaneGrass;
	public static ConfiguredFeature<?, ?> chaosPlaneFlowers;
	
	public static void initializeChaosPlaneVegetationFeatures() {
		RandomPatchConfiguration grassPatch = new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Registry.getBlock("chaosweed").defaultBlockState(), 7)
				.add(Registry.getBlock("tall_chaosweed").defaultBlockState(), 4).add(Registry.getBlock("blooming_chaosweed").defaultBlockState(), 2).add(Registry.getBlock("tall_blooming_chaosweed").defaultBlockState(), 1).build()), SingleOrDoublePlantPlacer.INSTANCE).tries(64).noProjection().build();
		RandomPatchConfiguration flowerPatch = new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Registry.getBlock("prism_rose").defaultBlockState(), 4)
				.add(Registry.getBlock("mandelbloom").defaultBlockState(), 1).build()), SimpleBlockPlacer.INSTANCE).tries(64).build();
		chaosPlaneGrass = Feature.RANDOM_PATCH.configured(grassPatch).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(8);
		chaosPlaneFlowers = Feature.FLOWER.configured(flowerPatch).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).count(3);
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
}
