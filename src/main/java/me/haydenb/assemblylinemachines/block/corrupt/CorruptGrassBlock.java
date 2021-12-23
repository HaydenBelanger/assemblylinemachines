package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.lighting.LightEngine;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class CorruptGrassBlock extends Block{

	
	public CorruptGrassBlock() {
		super(Block.Properties.create(Material.EARTH).hardnessAndResistance(13f, 30f).harvestLevel(0).harvestTool(ToolType.SHOVEL).sound(SoundType.GROUND));
		
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		
		if(!checkIfDead(state, world, pos)) {
			if(world.isAreaLoaded(pos, 3)) {
				world.setBlockState(pos, Registry.getBlock("corrupt_dirt").getDefaultState());
			}
			
		}
		CorruptBlock.poisonAll(world, pos);
	}
	
	@Override
	public boolean ticksRandomly(BlockState state) {
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		CorruptBlock.animate(stateIn, worldIn, pos, rand);
	}
	
	/**
	 * Logic copied from vanilla SpreadableSnowyDirtBlock class
	 */
	private static boolean checkIfDead(BlockState p_220257_0_, IWorldReader p_220257_1_, BlockPos p_220257_2_) {
	      BlockPos blockpos = p_220257_2_.up();
	      BlockState blockstate = p_220257_1_.getBlockState(blockpos);
	      if (blockstate.getBlock().equals(Blocks.SNOW) && blockstate.get(SnowBlock.LAYERS) == 1) {
	         return true;
	      } else if (blockstate.getFluidState().getLevel() == 8) {
	         return false;
	      } else {
	         int i = LightEngine.func_215613_a(p_220257_1_, p_220257_0_, p_220257_2_, blockstate, blockpos, Direction.UP, blockstate.getOpacity(p_220257_1_, blockpos));
	         return i < p_220257_1_.getMaxLightLevel();
	      }
	   }

	
}
