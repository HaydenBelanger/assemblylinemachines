package me.haydenb.assemblylinemachines.world;

import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

public class Ore {
	
	private static final FillerBlockType endStoneFillerBlock = OreFeatureConfig.FillerBlockType.create("alm_endstone", "alm_endstone", new Predicate<BlockState>() {

		@Override
		public boolean test(BlockState t) {
			if(t.getBlock() == Blocks.END_STONE) {
				return true;
			}
			return false;
		}
	});

	public static void generateOverworldOre(BlockState blockState, int common, int min, int max, int vein) {
		for(Biome b : ForgeRegistries.BIOMES) {
			if(b.getCategory() != Category.NETHER || b.getCategory() != Category.THEEND) {
				generateOre(b, OreFeatureConfig.FillerBlockType.NATURAL_STONE, blockState, common, min, max, vein);
			}
		}
	}
	
	public static void generateNetherOre(BlockState blockState, int common, int min, int max, int vein) {
		for(Biome b : ForgeRegistries.BIOMES) {
			if(b.getCategory() == Category.NETHER) {
				generateOre(b, OreFeatureConfig.FillerBlockType.NETHERRACK, blockState, common, min, max, vein);
			}
		}
	}
	
	public static void generateEndOre(BlockState blockState, int common, int min, int max, int vein) {
		for(Biome b : ForgeRegistries.BIOMES) {
			if(b.getCategory() == Category.THEEND) {
				generateOre(b, endStoneFillerBlock, blockState, common, min, max, vein);
			}
		}
	}
	
	private static void generateOre(Biome b, FillerBlockType fbt, BlockState blockState, int common, int min, int max, int vein) {
		ConfiguredPlacement<CountRangeConfig> placement = Placement.COUNT_RANGE.configure(new CountRangeConfig(common, min, 0, max));
		b.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE.withConfiguration(new OreFeatureConfig(fbt, blockState, vein)).withPlacement(placement));
	}
}
