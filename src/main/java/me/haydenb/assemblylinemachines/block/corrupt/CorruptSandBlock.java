package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CorruptSandBlock extends SandBlock implements IBlockWithHarvestableTags {

	
	public CorruptSandBlock() {
		super(0x4287f5, Block.Properties.of(Material.SAND).strength(13f, 30f).sound(SoundType.SAND));
		
	}
	
	@Override
	public Named<Block> getToolType() {
		return BlockTags.MINEABLE_WITH_SHOVEL;
	}


	@Override
	public Named<Block> getToolLevel() {
		return BlockTags.NEEDS_DIAMOND_TOOL;
	}
	
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
		
		CorruptBlock.poisonAll(world, pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		CorruptBlock.animate(stateIn, worldIn, pos, rand);
	}
	
}
