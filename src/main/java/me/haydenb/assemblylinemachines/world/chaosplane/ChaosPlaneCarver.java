package me.haydenb.assemblylinemachines.world.chaosplane;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.*;

public class ChaosPlaneCarver extends CaveWorldCarver {

	private static final List<BlockState> CARVER_FLUIDS = List.of(Registry.getBlock("condensed_void_block").defaultBlockState(), Registry.getBlock("dark_energy_block").defaultBlockState());

	public ChaosPlaneCarver() {
		super(CaveCarverConfiguration.CODEC);
	}

	@Override
	protected int getCaveBound() {
		return 9;
	}

	@Override
	protected float getThickness(RandomSource p_64893_) {
		return (p_64893_.nextFloat() * 2.0F + p_64893_.nextFloat()) * 3.8F;
	}

	@Override
	protected double getYScale() {
		return 6.75D;
	}

	@Override
	public int getRange() {
		return 8;
	}

	@Override
	protected boolean carveBlock(CarvingContext context, CaveCarverConfiguration carver, ChunkAccess access,
			Function<BlockPos, Holder<Biome>> biomePos, CarvingMask mask, BlockPos.MutableBlockPos blockPos, BlockPos.MutableBlockPos blockPos2, Aquifer aquifer, MutableBoolean mutableBoolean) {
		if (this.canReplaceBlock(carver, access.getBlockState(blockPos))) {
			if (blockPos.getY() <= context.getMinGenY() + 31) {
				access.setBlockState(blockPos, CARVER_FLUIDS.get(blockPos.getY() <= context.getMinGenY() + 23 ? 0 : 1), false);
			} else {
				access.setBlockState(blockPos, CAVE_AIR, false);
			}
			return true;
		}
		return false;
	}
}
