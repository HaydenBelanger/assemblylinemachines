package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;

public class BlockNaphthaTurbine extends Block{

	
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(1, 1, 3, 2, 2, 4),
			Block.box(1, 1, 5, 2, 2, 6),
			Block.box(1, 1, 10, 2, 2, 11),
			Block.box(1, 1, 12, 2, 2, 13),
			Block.box(3, 1, 14, 4, 2, 15),
			Block.box(5, 1, 14, 6, 2, 15),
			Block.box(10, 1, 14, 11, 2, 15),
			Block.box(12, 1, 14, 13, 2, 15),
			Block.box(14, 1, 12, 15, 2, 13),
			Block.box(14, 1, 10, 15, 2, 11),
			Block.box(14, 1, 5, 15, 2, 6),
			Block.box(14, 1, 3, 15, 2, 4),
			Block.box(12, 1, 1, 13, 2, 2),
			Block.box(10, 1, 1, 11, 2, 2),
			Block.box(5, 1, 1, 6, 2, 2),
			Block.box(3, 1, 1, 4, 2, 2),
			Block.box(0, 1, 3, 1, 10, 4),
			Block.box(0, 1, 5, 1, 10, 6),
			Block.box(0, 1, 10, 1, 10, 11),
			Block.box(0, 1, 12, 1, 10, 13),
			Block.box(3, 1, 15, 4, 10, 16),
			Block.box(5, 1, 15, 6, 10, 16),
			Block.box(10, 1, 15, 11, 10, 16),
			Block.box(12, 1, 15, 13, 10, 16),
			Block.box(15, 1, 12, 16, 10, 13),
			Block.box(15, 1, 10, 16, 10, 11),
			Block.box(15, 1, 5, 16, 10, 6),
			Block.box(15, 1, 3, 16, 10, 4),
			Block.box(12, 1, 0, 13, 10, 1),
			Block.box(10, 1, 0, 11, 10, 1),
			Block.box(5, 1, 0, 6, 10, 1),
			Block.box(3, 1, 0, 4, 10, 1),
			Block.box(0, 10, 0, 16, 16, 16),
			Block.box(2, 0, 2, 14, 10, 14)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	
	public BlockNaphthaTurbine() {
		super(Block.Properties.of(Material.STONE).strength(3f, 30f).sound(SoundType.STONE));
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(HorizontalDirectionalBlock.FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
				context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
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
