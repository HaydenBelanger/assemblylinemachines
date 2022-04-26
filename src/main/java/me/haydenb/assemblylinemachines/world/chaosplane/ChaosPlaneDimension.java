package me.haydenb.assemblylinemachines.world.chaosplane;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ChaosPlaneDimension {

	public static final ResourceKey<DimensionType> CHAOS_PLANE_LOCATION = create(Registry.DIMENSION_TYPE_REGISTRY, "chaos_plane");
	public static final ResourceKey<Level> CHAOS_PLANE = create(Registry.DIMENSION_REGISTRY, "chaos_plane");
	
	private static <T> ResourceKey<T> create(ResourceKey<Registry<T>> registry, String name){
		return ResourceKey.create(registry, new ResourceLocation(AssemblyLineMachines.MODID, name));
	}
}
