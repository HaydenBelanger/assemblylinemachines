package me.haydenb.assemblylinemachines.world.chaosplane;

import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.collect.ImmutableSet;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.*;

public class ChaosPlaneCarver extends CaveWorldCarver {

	private final BlockState carverFluid = Registry.getFluid("condensed_void").defaultFluidState().createLegacyBlock();

	public ChaosPlaneCarver() {
		super(CaveCarverConfiguration.CODEC);
		replaceableBlocks = ImmutableSet.of(Registry.getBlock("corrupt_stone"), Registry.getBlock("corrupt_dirt"), Registry.getBlock("corrupt_sand"), Registry.getBlock("corrupt_grass"), Registry.getBlock("dark_energy_block"));
	}

	@Override
	protected int getCaveBound() {
		return 9;
	}

	@Override
	protected float getThickness(Random p_64893_) {
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
		if (this.canReplaceBlock(access.getBlockState(blockPos))) {
			BlockState blockstate;
			if (blockPos.getY() <= context.getMinGenY() + 31) {
				blockstate = carverFluid;
			} else {
				blockstate = CAVE_AIR;
			}

			access.setBlockState(blockPos, blockstate, false);
			return true;
		} else {
			return false;
		}
	}
}
