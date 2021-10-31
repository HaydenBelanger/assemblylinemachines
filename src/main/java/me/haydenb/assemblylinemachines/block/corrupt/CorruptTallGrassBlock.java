package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class CorruptTallGrassBlock extends TallGrassBlock {

	public CorruptTallGrassBlock() {
		super(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS));
	}

	@Override
	public void performBonemeal(ServerLevel pLevel, Random pRandom, BlockPos pPos, BlockState pState) {
		DoublePlantBlock dpb;
		if(pState.is(Registry.getBlock("chaosweed"))) {
			dpb = (DoublePlantBlock) Registry.getBlock("tall_chaosweed");
		}else {
			dpb = (DoublePlantBlock) Registry.getBlock("tall_blooming_chaosweed");
		}
		if (dpb.defaultBlockState().canSurvive(pLevel, pPos) && pLevel.isEmptyBlock(pPos.above())) {
			DoublePlantBlock.placeAt(pLevel, dpb.defaultBlockState(), pPos, 2);
		}
	}
	
	@Override
	protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return CorruptGrassBlock.allowPlaceOn(pState);
	}
	
	public static class CorruptDoubleTallGrassBlock extends DoublePlantBlock {

		public CorruptDoubleTallGrassBlock() {
			super(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS));
		}
		
		@Override
		protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
			return CorruptGrassBlock.allowPlaceOn(pState);
		}
	}
	
	public static class CorruptFlowerBlock extends FlowerBlock{

		public CorruptFlowerBlock(MobEffect pSuspiciousStewEffect, int pEffectDuration) {
			super(pSuspiciousStewEffect, pEffectDuration, Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS));
		}
		
		@Override
		protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
			return CorruptGrassBlock.allowPlaceOn(pState);
		}
		
	}

}
