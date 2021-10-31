package me.haydenb.assemblylinemachines.block.corrupt;

import me.haydenb.assemblylinemachines.block.corrupt.ChaosbarkLogBlock.ChaosbarkTreeGrower;
import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class ChaosbarkLeavesBlock extends LeavesBlock implements IBlockWithHarvestableTags{

	public ChaosbarkLeavesBlock() {
		super(Properties.of(Material.LEAVES).strength(13f, 30f).sound(SoundType.GRASS).randomTicks().noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
	}

	@Override
	public Named<Block> getToolType() {
		return BlockTags.MINEABLE_WITH_HOE;
	}

	@Override
	public Named<Block> getToolLevel() {
		return BlockTags.NEEDS_DIAMOND_TOOL;
	}
	
	public static class ChaosbarkSaplingBlock extends SaplingBlock{

		public ChaosbarkSaplingBlock() {
			super(new ChaosbarkTreeGrower(), Block.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS));
		}
		
		@Override
		protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
			return CorruptGrassBlock.allowPlaceOn(pState);
		}
	}
}
