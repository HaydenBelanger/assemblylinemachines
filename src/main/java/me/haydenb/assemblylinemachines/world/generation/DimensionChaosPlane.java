package me.haydenb.assemblylinemachines.world.generation;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class DimensionChaosPlane {
	
	public static final ResourceKey<Biome> CORRUPT_PLAINS = create("corrupt_plains");
	
	private static ResourceKey<Biome> create(String name){
		return ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(AssemblyLineMachines.MODID, name));
	}
}
