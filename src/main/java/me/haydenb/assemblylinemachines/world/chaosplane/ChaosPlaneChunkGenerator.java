package me.haydenb.assemblylinemachines.world.chaosplane;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer.FluidStatus;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;

public class ChaosPlaneChunkGenerator extends NoiseBasedChunkGenerator {

	public static final Codec<ChaosPlaneChunkGenerator> CODEC = RecordCodecBuilder.create((p_188643_) -> {
		return commonCodec(p_188643_).and(p_188643_.group(RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter((p_188716_) -> {
			return p_188716_.noises;
		}), BiomeSource.CODEC.fieldOf("biome_source").forGetter((p_188711_) -> {
			return p_188711_.biomeSource;
		}), Codec.LONG.fieldOf("seed").stable().forGetter((p_188690_) -> {
			return p_188690_.seed;
		}), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_204585_) -> {
			return p_204585_.settings;
		}))).apply(p_188643_, p_188643_.stable(ChaosPlaneChunkGenerator::new));
	});

	public ChaosPlaneChunkGenerator(Registry<StructureSet> p_209106_, Registry<NoiseParameters> p_209107_,
			BiomeSource p_209108_, long p_209109_, Holder<NoiseGeneratorSettings> p_209110_) {
		super(p_209106_, p_209107_, p_209108_, p_209109_, p_209110_);
		
		NoiseGeneratorSettings ngs = settings.value();
		FluidStatus air = new FluidStatus(ngs.noiseSettings().minY() - 1, Blocks.AIR.defaultBlockState());
		this.globalFluidPicker = (x, y, z) -> air;
		AssemblyLineMachines.LOGGER.debug("Aquifiers @ NoiseGeneratorSettings showing as " + ngs.aquifersEnabled() + " for C. Plane.");
	}
	
	@Override
	public ChunkGenerator withSeed(long pSeed) {
		return new ChaosPlaneChunkGenerator(this.structureSets, this.noises, this.biomeSource, pSeed, this.settings);
	}
}
