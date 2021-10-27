package me.haydenb.assemblylinemachines.world.generation;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import me.haydenb.assemblylinemachines.registry.FluidRegistry;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.*;
import net.minecraft.world.level.material.FluidState;

public class ChaosPlaneCarver extends CaveWorldCarver {

	private final FluidState carverFluid;
	public ChaosPlaneCarver(Codec<CaveCarverConfiguration> codec) {
		super(codec);
		this.replaceableBlocks = ImmutableSet.of(Registry.getBlock("corrupt_stone"), Registry.getBlock("corrupt_dirt"), Registry.getBlock("corrupt_sand"), Registry.getBlock("corrupt_grass"));
		carverFluid = FluidRegistry.CONDENSED_VOID.get().defaultFluidState();
	}
	
	@Override
	public BlockState getCarveState(CarvingContext pContext, CaveCarverConfiguration pConfig, BlockPos pPos, Aquifer pAquifer) {
		if(pPos.getY() <= pConfig.lavaLevel.resolveY(pContext)) {
			return carverFluid.createLegacyBlock();
		}
		return super.getCarveState(pContext, pConfig, pPos, pAquifer);
	}
}
