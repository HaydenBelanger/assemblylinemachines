package me.haydenb.assemblylinemachines.registry.utils;

import java.util.List;
import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class WhitelistBiomeFilter extends BiomeFilter{

	public static final Codec<WhitelistBiomeFilter> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(
		Codec.either(ResourceLocation.CODEC, Codec.list(ResourceLocation.CODEC)).fieldOf("biome_id").forGetter((value) -> Either.right(value.biomes)),
		Codec.BOOL.optionalFieldOf("whitelist", true).forGetter((value) -> value.whitelist))
		.apply(instance, WhitelistBiomeFilter::new);
	});
	
	public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_REGISTRY = DeferredRegister.create(Registry.PLACEMENT_MODIFIER_REGISTRY, AssemblyLineMachines.MODID);
	public static final RegistryObject<PlacementModifierType<WhitelistBiomeFilter>> WHITELIST_BIOME_FILTER = PLACEMENT_MODIFIER_REGISTRY.register("whitelist_biome", () -> () -> CODEC);
	
	private final List<ResourceLocation> biomes;
	private final boolean whitelist;
	
	public WhitelistBiomeFilter(Either<ResourceLocation, List<ResourceLocation>> biomes, boolean whitelist) {
		this.biomes = biomes.right().orElse(List.of(biomes.left().get()));
		this.whitelist = whitelist;
	}
	
	@Override
	protected boolean shouldPlace(PlacementContext context, Random random, BlockPos pos) {
		ResourceLocation biome = context.getLevel().getBiome(pos).value().getRegistryName();
		if((whitelist && !biomes.contains(biome)) || (!whitelist && biomes.contains(biome))) return false;
		return super.shouldPlace(context, random, pos);
	}
	
	@Override
	public PlacementModifierType<?> type() {
		return WHITELIST_BIOME_FILTER.get();
	}
}
