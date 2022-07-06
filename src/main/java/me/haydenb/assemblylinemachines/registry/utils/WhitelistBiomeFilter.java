package me.haydenb.assemblylinemachines.registry.utils;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.ForgeRegistries;

public class WhitelistBiomeFilter extends BiomeFilter{

	public static final Codec<WhitelistBiomeFilter> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(
		Codec.list(ResourceLocation.CODEC).fieldOf("biome_id").forGetter((value) -> value.biomes),
		Codec.BOOL.optionalFieldOf("whitelist", true).forGetter((value) -> value.whitelist))
		.apply(instance, WhitelistBiomeFilter::new);
	});

	private final List<ResourceLocation> biomes;
	private final boolean whitelist;

	public WhitelistBiomeFilter(List<ResourceLocation> biomes, boolean whitelist) {
		this.biomes = biomes;
		this.whitelist = whitelist;
	}

	@Override
	protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
		ResourceLocation biome = ForgeRegistries.BIOMES.getKey(context.getLevel().getBiome(pos).value());
		if((whitelist && !biomes.contains(biome)) || (!whitelist && biomes.contains(biome))) return false;
		return super.shouldPlace(context, random, pos);
	}

	@Override
	public PlacementModifierType<?> type() {
		return Registry.WHITELIST_BIOME_FILTER.get();
	}
}
