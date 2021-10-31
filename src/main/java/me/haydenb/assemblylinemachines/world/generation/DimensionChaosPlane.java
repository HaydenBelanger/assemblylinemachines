package me.haydenb.assemblylinemachines.world.generation;

import java.util.ArrayList;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.corrupt.ChaosbarkLogBlock.ChaosbarkTreeGrower;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class DimensionChaosPlane {
	
	
	public static final ResourceKey<Biome> CORRUPT_PLAINS = create(Registry.BIOME_REGISTRY, "corrupt_plains");
	public static final ResourceKey<Biome> CORRUPT_CLIFFS = create(Registry.BIOME_REGISTRY, "corrupt_cliffs");
	public static final ResourceKey<Biome> CORRUPT_DESERT = create(Registry.BIOME_REGISTRY, "corrupt_desert");
	public static final ResourceKey<Biome> CORRUPT_FOREST = create(Registry.BIOME_REGISTRY, "corrupt_forest");
	
	private static final ArrayList<ResourceLocation> CORRUPT_BIOMES = new ArrayList<>();
	static{
		CORRUPT_BIOMES.add(CORRUPT_PLAINS.location());
		CORRUPT_BIOMES.add(CORRUPT_CLIFFS.location());
		CORRUPT_BIOMES.add(CORRUPT_DESERT.location());
		CORRUPT_BIOMES.add(CORRUPT_FOREST.location());
	}
	
	private static final RangeDecoratorConfiguration CHAOSBARK_RANGE = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(64), VerticalAnchor.aboveBottom(170)));
	
	private static <T> ResourceKey<T> create(ResourceKey<Registry<T>> registry, String name){
		return ResourceKey.create(registry, new ResourceLocation(AssemblyLineMachines.MODID, name));
	}
	
	public static void addFeaturesToBiome(BiomeLoadingEvent event) {
		if(CORRUPT_BIOMES.contains(event.getName())) {
			int treeCount = 0;
			if(event.getName().equals(CORRUPT_PLAINS.location())) {
				treeCount = 4;
			}else if(event.getName().equals(CORRUPT_FOREST.location())) {
				treeCount = 120;
			}else if(event.getName().equals(CORRUPT_CLIFFS.location())) {
				treeCount = 8;
			}
			
			if(treeCount != 0) {
				event.getGeneration().addFeature(Decoration.VEGETAL_DECORATION, ChaosbarkTreeGrower.chaosbarkTree.range(CHAOSBARK_RANGE).squared().count(treeCount));
			}
			
		}
		
		
	}
}
