package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class CorruptSandBlock extends SandBlock {

	
	public CorruptSandBlock() {
		super(0x4287f5, AbstractBlock.Properties.create(Material.SAND).hardnessAndResistance(13f, 30f).harvestLevel(0).harvestTool(ToolType.SHOVEL).sound(SoundType.SAND));
		
	}
	
	@Override
	public boolean ticksRandomly(BlockState state) {
		return true;
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		
		CorruptBlock.poisonAll(world, pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		CorruptBlock.animate(stateIn, worldIn, pos, rand);
	}
	
}
