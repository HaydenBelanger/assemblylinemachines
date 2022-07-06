package me.haydenb.assemblylinemachines.block.fluids;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FluidGlacierWater extends SplitFluid {

	private static final List<Block> SNOW_INVALID_BLOCKS = List.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BARRIER);

	public FluidGlacierWater(boolean source) {
		super(source, Registry.basicFFFProperties("glacier_water"));
	}

	@Override
	public int getTickDelay(LevelReader world) {
		return 10;
	}

	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}

	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState fState, RandomSource random) {
		Iterable<BlockPos> iter = BlockPos.betweenClosed(pos.above(2).north(3).east(3), pos.below(2).south(3).west(3));
		iter.forEach((posx) -> {
			if(random.nextInt(3) == 0) {
				BlockState state = world.getBlockState(posx);
				Block block = state.getBlock();
				if(block.equals(Blocks.WATER)) {
					world.setBlockAndUpdate(posx, Blocks.PACKED_ICE.defaultBlockState());
				}else if(block.equals(Blocks.LAVA)) {
					world.setBlockAndUpdate(posx, Blocks.OBSIDIAN.defaultBlockState());
				}else if(block.equals(Blocks.SNOW) && state.hasProperty(SnowLayerBlock.LAYERS)) {
					if(state.getValue(SnowLayerBlock.LAYERS) < 8) {
						world.setBlockAndUpdate(posx, state.setValue(SnowLayerBlock.LAYERS, state.getValue(SnowLayerBlock.LAYERS) + 1));
					}else {
						world.setBlockAndUpdate(posx, Blocks.POWDER_SNOW.defaultBlockState());
					}
				}else if(world.isEmptyBlock(posx) && !SNOW_INVALID_BLOCKS.contains(world.getBlockState(posx.below()).getBlock()) && Block.isFaceFull(world.getBlockState(posx.below()).getCollisionShape(world, posx.below()), Direction.UP)) {
					world.setBlockAndUpdate(posx, Blocks.SNOW.defaultBlockState());

				}
			}
		});
		super.randomTick(world, pos, fState, random);
	}
}
