package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.Random;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;

public abstract class BlockRefineryAddon extends Block{

	public BlockRefineryAddon() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
	}
	
	
	
	
	@Override
	public abstract VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context);
	
	public abstract void animateTickFromBase(BlockState state, Level world, BlockPos pos, Random rand);
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		
		BlockState dbs = context.getLevel().getBlockState(context.getClickedPos().below());
		if(dbs.getBlock() == Registry.getBlock("refinery")) {
			return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dbs.getValue(HorizontalDirectionalBlock.FACING));
		}
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	public static class BlockSeparationAddon extends BlockRefineryAddon{

		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(2, 0, 11, 6, 2, 15),
				Block.box(3, 2, 11, 6, 4, 15),
				Block.box(10, 2, 11, 13, 4, 15),
				Block.box(2, 2, 12, 3, 4, 15),
				Block.box(13, 2, 12, 14, 4, 15),
				Block.box(10, 0, 11, 14, 2, 15),
				Block.box(0, 3, 4, 7, 13, 11),
				Block.box(9, 3, 4, 16, 13, 11),
				Block.box(13, 2, 3, 14, 14, 12),
				Block.box(2, 2, 3, 3, 14, 12),
				Block.box(7, 6, 5, 9, 10, 9),
				Block.box(12, 0, 8, 13, 2, 9),
				Block.box(3, 0, 8, 4, 2, 9),
				Block.box(11, 1, 8, 12, 2, 9),
				Block.box(4, 1, 8, 5, 2, 9),
				Block.box(11, 1, 7, 12, 3, 8),
				Block.box(4, 1, 7, 5, 3, 8),
				Block.box(3, 13, 7, 4, 15, 8),
				Block.box(12, 13, 7, 13, 15, 8),
				Block.box(4, 14, 7, 12, 15, 8)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
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

		@Override
		public void animateTickFromBase(BlockState state, Level world, BlockPos pos, Random rand) {
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartNext(rand), pos.getY() + getPartNext(rand), pos.getZ() + getPartNext(rand), 0, 0, 0);
			
		}
		
		
		
	}
	
	public static class BlockAdditionAddon extends BlockRefineryAddon{

		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(4, 3, 3, 12, 13, 11),
				Block.box(5, 2, 2, 6, 14, 12),
				Block.box(10, 2, 2, 11, 14, 12),
				Block.box(11, 2, 11, 14, 4, 15),
				Block.box(2, 2, 11, 5, 4, 15),
				Block.box(10, 2, 12, 11, 4, 15),
				Block.box(5, 2, 12, 6, 4, 15),
				Block.box(10, 0, 11, 14, 2, 15),
				Block.box(2, 0, 11, 6, 2, 15),
				Block.box(12, 0, 8, 13, 2, 9),
				Block.box(3, 0, 8, 4, 2, 9),
				Block.box(6, 2, 8, 7, 3, 9),
				Block.box(9, 2, 8, 10, 3, 9),
				Block.box(4, 1, 8, 12, 2, 9),
				Block.box(2, 6, 5, 4, 10, 9),
				Block.box(12, 6, 5, 14, 10, 9),
				Block.box(14, 5, 4, 16, 11, 10),
				Block.box(0, 5, 4, 2, 11, 10)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
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

		@Override
		public void animateTickFromBase(BlockState state, Level world, BlockPos pos, Random rand) {
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartNext(rand), pos.getY() + getPartNext(rand), pos.getZ() + getPartNext(rand), 0, 0, 0);
			
		}
		
		
		
	}
	
	public static class BlockHalogenAddon extends BlockRefineryAddon{

		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(10, 3, 12, 14, 4, 15),
				Block.box(2, 3, 12, 6, 4, 15),
				Block.box(10, 0, 11, 14, 3, 15),
				Block.box(2, 0, 11, 6, 3, 15),
				Block.box(1, 3, 7, 6, 10, 12),
				Block.box(10, 3, 7, 15, 10, 12),
				Block.box(10, 11, 7, 15, 16, 12),
				Block.box(5, 4, 6, 11, 10, 7),
				Block.box(6, 6, 9, 10, 7, 10),
				Block.box(8, 7, 9, 9, 12, 10),
				Block.box(3, 12, 9, 10, 13, 10),
				Block.box(3, 10, 9, 4, 12, 10),
				Block.box(12, 10, 9, 13, 11, 10),
				Block.box(12, 0, 8, 13, 2, 9),
				Block.box(3, 0, 8, 4, 2, 9),
				Block.box(11, 1, 8, 12, 3, 9),
				Block.box(4, 1, 8, 5, 3, 9)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
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

		@Override
		public void animateTickFromBase(BlockState state, Level world, BlockPos pos, Random rand) {
			world.addParticle(ParticleTypes.CLOUD, true, pos.getX() + getPartNext(rand), pos.getY() + getPartNext(rand), pos.getZ() + getPartNext(rand), 0, 0, 0);
			
		}
		
		
		
	}
	
	public static class BlockCrackingAddon extends BlockRefineryAddon{

		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(2, 0, 11, 6, 2, 15),
				Block.box(3, 2, 11, 6, 4, 15),
				Block.box(10, 2, 11, 13, 4, 15),
				Block.box(2, 2, 12, 3, 4, 15),
				Block.box(13, 2, 12, 14, 4, 15),
				Block.box(10, 0, 11, 14, 2, 15),
				Block.box(0, 3, 4, 16, 11, 11),
				Block.box(5, 4, 3, 11, 10, 4),
				Block.box(13, 2, 3, 14, 12, 12),
				Block.box(2, 2, 3, 3, 12, 12),
				Block.box(5, 11, 5, 11, 13, 11),
				Block.box(4, 13, 4, 12, 16, 12),
				Block.box(12, 0, 8, 13, 2, 9),
				Block.box(3, 0, 8, 4, 2, 9),
				Block.box(11, 1, 8, 12, 2, 9),
				Block.box(4, 1, 8, 5, 2, 9),
				Block.box(11, 1, 7, 12, 3, 8),
				Block.box(4, 1, 7, 5, 3, 8)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
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

		@Override
		public void animateTickFromBase(BlockState state, Level world, BlockPos pos, Random rand) {
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartThinNext(rand), pos.getY() + 1.05, pos.getZ() + getPartThinNext(rand), 0, 0, 0);
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartThinNext(rand), pos.getY() + 1.05, pos.getZ() + getPartThinNext(rand), 0, 0, 0);
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartThinNext(rand), pos.getY() + 1.05, pos.getZ() + getPartThinNext(rand), 0, 0, 0);
		}
		
		
		
	}
	
	private static double getPartNext(Random rand) {
		double d = rand.nextDouble();
		if(d < 0.2 || d > 0.8) {
			d = 0.5;
		}
		return d;
	}
	
	private static double getPartThinNext(Random rand) {
		double d = rand.nextDouble();
		if(d < 0.3 || d > 0.7) {
			d = 0.5;
		}
		return d;
	}
}
