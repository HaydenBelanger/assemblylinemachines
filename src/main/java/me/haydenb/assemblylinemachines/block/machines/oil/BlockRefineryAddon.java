package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public abstract class BlockRefineryAddon extends Block{

	public BlockRefineryAddon() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	
	@Override
	public abstract VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context);
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		
		BlockState dbs = context.getWorld().getBlockState(context.getPos().down());
		if(dbs.getBlock() == Registry.getBlock("refinery")) {
			return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, dbs.get(HorizontalBlock.HORIZONTAL_FACING));
		}
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	public static class BlockSeparationAddon extends BlockRefineryAddon{

		private static final VoxelShape SHAPE_N = Stream.of(
				Block.makeCuboidShape(2, 0, 11, 6, 2, 15),
				Block.makeCuboidShape(3, 2, 11, 6, 4, 15),
				Block.makeCuboidShape(10, 2, 11, 13, 4, 15),
				Block.makeCuboidShape(2, 2, 12, 3, 4, 15),
				Block.makeCuboidShape(13, 2, 12, 14, 4, 15),
				Block.makeCuboidShape(10, 0, 11, 14, 2, 15),
				Block.makeCuboidShape(0, 3, 4, 7, 13, 11),
				Block.makeCuboidShape(9, 3, 4, 16, 13, 11),
				Block.makeCuboidShape(13, 2, 3, 14, 14, 12),
				Block.makeCuboidShape(2, 2, 3, 3, 14, 12),
				Block.makeCuboidShape(7, 6, 5, 9, 10, 9),
				Block.makeCuboidShape(12, 0, 8, 13, 2, 9),
				Block.makeCuboidShape(3, 0, 8, 4, 2, 9),
				Block.makeCuboidShape(11, 1, 8, 12, 2, 9),
				Block.makeCuboidShape(4, 1, 8, 5, 2, 9),
				Block.makeCuboidShape(11, 1, 7, 12, 3, 8),
				Block.makeCuboidShape(4, 1, 7, 5, 3, 8),
				Block.makeCuboidShape(3, 13, 7, 4, 15, 8),
				Block.makeCuboidShape(12, 13, 7, 13, 15, 8),
				Block.makeCuboidShape(4, 14, 7, 12, 15, 8)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
		
		private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
			if (d == Direction.WEST) {
				return SHAPE_W;
			} else if (d == Direction.SOUTH) {
				return SHAPE_S;
			} else if (d == Direction.EAST) {
				return SHAPE_E;
			} else {
				return SHAPE_N;
			}
		}
		
	}
}
