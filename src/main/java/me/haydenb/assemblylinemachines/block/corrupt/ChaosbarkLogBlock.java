package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.HashMap;
import java.util.Random;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.material.Material;

public class ChaosbarkLogBlock extends RotatedPillarBlock implements IBlockWithHarvestableTags {

	public ChaosbarkLogBlock() {
		super(Properties.of(Material.WOOD).strength(13f, 30f).sound(SoundType.WOOD));
	}
	
	@Override
	public Named<Block> getToolType() {
		return BlockTags.MINEABLE_WITH_AXE;
	}


	@Override
	public Named<Block> getToolLevel() {
		return BlockTags.NEEDS_DIAMOND_TOOL;
	}
	
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
