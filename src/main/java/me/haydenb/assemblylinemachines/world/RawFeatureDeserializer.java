package me.haydenb.assemblylinemachines.world;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record RawFeatureDeserializer(Optional<HolderSet<Biome>> biomes) implements BiomeModifier {

	public static final Lazy<ArrayList<Holder<PlacedFeature>>> RAW_PLACED_FEATURES = Lazy.of(() -> {
		ArrayList<Holder<PlacedFeature>> features = new ArrayList<>();

		try {
			Files.find(Paths.get(Thread.currentThread().getContextClassLoader().getResource("data/assemblylinemachines/worldgen/placed_feature_raw").toURI()), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile()).forEach((p) -> {
				try {
					PlacedFeature.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(new BufferedInputStream(Files.newInputStream(p)))))
						.resultOrPartial((s) -> AssemblyLineMachines.LOGGER.fatal("In relation to " + p.getFileName().toString() + ": " + s)).ifPresent((cf) -> features.add(cf));
				}catch(Exception e) {
					AssemblyLineMachines.LOGGER.fatal("Error while codec-deserializing " + p.getFileName().toString() + ": " + e.getMessage());
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
		}
		return features;
	});

	public static final Codec<? extends BiomeModifier> CODEC = RecordCodecBuilder.<RawFeatureDeserializer>create((instance) -> {
		return instance.group(
		Biome.LIST_CODEC.optionalFieldOf("biomes").forGetter((value) -> value.biomes))
		.apply(instance, RawFeatureDeserializer::new);
	});

	@Override
	public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
		if(phase == Phase.ADD && (biomes.isEmpty() || biomes.get().contains(biome))) {
			for(var placedFeature : RAW_PLACED_FEATURES.get()) builder.getGenerationSettings().addFeature(Decoration.UNDERGROUND_ORES, placedFeature);
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
