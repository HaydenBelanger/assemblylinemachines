package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class CorruptGrassBlock extends Block implements IBlockWithHarvestableTags{

	public CorruptGrassBlock() {
		super(Block.Properties.of(Material.DIRT).strength(13f, 30f).sound(SoundType.GRAVEL));

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
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {

		if(!checkIfDead(state, world, pos)) {
			if(world.isAreaLoaded(pos, 3)) {
				world.setBlockAndUpdate(pos, Registry.getBlock("corrupt_dirt").defaultBlockState());
			}

		}
		CorruptBlock.poisonAll(world, pos);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		CorruptBlock.animate(stateIn, worldIn, pos, rand);
	}

	/**
	 * Logic copied from vanilla SpreadableSnowyDirtBlock class
	 */
	private static boolean checkIfDead(BlockState p_220257_0_, BlockGetter p_220257_1_, BlockPos p_220257_2_) {
		BlockPos blockpos = p_220257_2_.above();
		BlockState blockstate = p_220257_1_.getBlockState(blockpos);
		if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) == 1) {
			return true;
		} else if (blockstate.getFluidState().getAmount() == 8) {
			return false;
		} else {
			int i = LayerLightEngine.getLightBlockInto(p_220257_1_, p_220257_0_, p_220257_2_, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(p_220257_1_, blockpos));
			return i < p_220257_1_.getMaxLightLevel();
		}
	}
	
	static boolean allowPlaceOn(BlockState state) {
		return state.is(ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "world/chaosbark_plantable")));
	}


}
