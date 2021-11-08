package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.HashMap;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;

public abstract class BlockQuarryAddon extends Block {

	
	
	private static final HashMap<Direction, BooleanProperty> QUARRY_ADDON_PROPERTIES = new HashMap<>();
	static {
		
		for(Direction d : Direction.values()) {
			QUARRY_ADDON_PROPERTIES.put(d, BooleanProperty.create(d.toString().toLowerCase()));
		}
		
	}
	
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(3, 10, 0, 6, 13, 2),
			Block.box(10, 10, 0, 13, 13, 2),
			Block.box(10, 3, 0, 13, 6, 2),
			Block.box(3, 3, 0, 6, 6, 2),
			Block.box(7, 7, 0, 9, 9, 2)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	
	private static final VoxelShape SHAPE_U = Stream.of(
			Block.box(3, 14, 3, 6, 16, 6),
			Block.box(10, 14, 3, 13, 16, 6),
			Block.box(10, 14, 10, 13, 16, 13),
			Block.box(3, 14, 10, 6, 16, 13),
			Block.box(7, 14, 7, 9, 16, 9)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_D = Stream.of(
			Block.box(3, 0, 3, 6, 2, 6),
			Block.box(10, 0, 3, 13, 2, 6),
			Block.box(10, 0, 10, 13, 2, 13),
			Block.box(3, 0, 10, 6, 2, 13),
			Block.box(7, 0, 7, 9, 2, 9)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockQuarryAddon() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));

		BlockState bs = this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.DOWN);
		bs = addToBlockState(bs);
		this.registerDefaultState(bs);
	}
	
	
	public static void addToBuilder(Builder<Block, BlockState> builder){
		
		for(Direction d : Direction.values()) {
			builder.add(QUARRY_ADDON_PROPERTIES.get(d));
		}
	}
	
	public static BlockState addToBlockState(BlockState bs) {
		for(Direction d : Direction.values()) {
			bs = bs.setValue(QUARRY_ADDON_PROPERTIES.get(d), false);
		}
		return bs;
	}
	
	public static BooleanProperty getAddonProperty(Direction d) {
		return QUARRY_ADDON_PROPERTIES.get(d);
	}
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		
		addToBuilder(builder);
		builder.add(BlockStateProperties.FACING);
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!worldIn.isClientSide()) {
			if (facing == stateIn.getValue(BlockStateProperties.FACING)) {
				if (worldIn.getBlockState(currentPos.relative(facing)).getBlock() == Blocks.AIR) {
					return Blocks.AIR.defaultBlockState();
				}
			}
		}

		return stateIn;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		for(Direction d : Direction.values()) {
			
			if(state.getValue(QUARRY_ADDON_PROPERTIES.get(d))) {
				return Shapes.join(getConnectionShape(d), getAddonBaseShape(d), BooleanOp.OR);
				
			}
			
		}
		return Shapes.empty();
	}
	
	public static VoxelShape getConnectionShape(Direction d) {
		if(d == Direction.UP) {
			return SHAPE_U;
		}else if(d == Direction.DOWN) {
			return SHAPE_D;
		}else if(d == Direction.NORTH) {
			return SHAPE_N;
		}else if(d == Direction.SOUTH) {
			return SHAPE_S;
		}else if(d == Direction.EAST) {
			return SHAPE_E;
		}else {
			return SHAPE_W;
		}
	}
	
	public abstract VoxelShape getAddonBaseShape(Direction d);
	
	public static class BlockSpeedQuarryAddon extends BlockQuarryAddon{
		
		private static final VoxelShape SHAPE_D = Stream.of(
				Block.box(2, 2, 2, 14, 4, 14),
				Block.box(2, 12, 2, 4, 14, 14),
				Block.box(2, 4, 2, 4, 12, 4),
				Block.box(2, 4, 12, 4, 12, 14),
				Block.box(12, 4, 12, 14, 12, 14),
				Block.box(12, 4, 2, 14, 12, 4),
				Block.box(12, 12, 2, 14, 14, 14),
				Block.box(4, 12, 12, 12, 14, 14),
				Block.box(4, 12, 2, 12, 14, 4),
				Block.box(5, 5, 5, 6, 6, 11),
				Block.box(5, 10, 5, 6, 11, 11),
				Block.box(10, 5, 5, 11, 6, 11),
				Block.box(10, 10, 5, 11, 11, 11),
				Block.box(6, 5, 10, 10, 6, 11),
				Block.box(6, 10, 10, 10, 11, 11),
				Block.box(6, 5, 5, 10, 6, 6),
				Block.box(6, 10, 5, 10, 11, 6),
				Block.box(10, 6, 5, 11, 10, 6),
				Block.box(10, 6, 10, 11, 10, 11),
				Block.box(5, 6, 10, 6, 10, 11),
				Block.box(5, 6, 5, 6, 10, 6),
				Block.box(7, 7, 7, 9, 9, 9)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_U = Stream.of(
				Block.box(2, 12, 2, 14, 14, 14),
				Block.box(2, 2, 2, 4, 4, 14),
				Block.box(2, 4, 12, 4, 12, 14),
				Block.box(2, 4, 2, 4, 12, 4),
				Block.box(12, 4, 2, 14, 12, 4),
				Block.box(12, 4, 12, 14, 12, 14),
				Block.box(12, 2, 2, 14, 4, 14),
				Block.box(4, 2, 2, 12, 4, 4),
				Block.box(4, 2, 12, 12, 4, 14),
				Block.box(5, 10, 5, 6, 11, 11),
				Block.box(5, 5, 5, 6, 6, 11),
				Block.box(10, 10, 5, 11, 11, 11),
				Block.box(10, 5, 5, 11, 6, 11),
				Block.box(6, 10, 5, 10, 11, 6),
				Block.box(6, 5, 5, 10, 6, 6),
				Block.box(6, 10, 10, 10, 11, 11),
				Block.box(6, 5, 10, 10, 6, 11),
				Block.box(10, 6, 10, 11, 10, 11),
				Block.box(10, 6, 5, 11, 10, 6),
				Block.box(5, 6, 5, 6, 10, 6),
				Block.box(5, 6, 10, 6, 10, 11),
				Block.box(7, 7, 7, 9, 9, 9)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(2, 2, 2, 14, 14, 4),
				Block.box(2, 2, 12, 4, 14, 14),
				Block.box(2, 12, 4, 4, 14, 12),
				Block.box(2, 2, 4, 4, 4, 12),
				Block.box(12, 2, 4, 14, 4, 12),
				Block.box(12, 12, 4, 14, 14, 12),
				Block.box(12, 2, 12, 14, 14, 14),
				Block.box(4, 2, 12, 12, 4, 14),
				Block.box(4, 12, 12, 12, 14, 14),
				Block.box(5, 5, 5, 6, 11, 6),
				Block.box(5, 5, 10, 6, 11, 11),
				Block.box(10, 5, 5, 11, 11, 6),
				Block.box(10, 5, 10, 11, 11, 11),
				Block.box(6, 5, 5, 10, 6, 6),
				Block.box(6, 5, 10, 10, 6, 11),
				Block.box(6, 10, 5, 10, 11, 6),
				Block.box(6, 10, 10, 10, 11, 11),
				Block.box(10, 10, 6, 11, 11, 10),
				Block.box(10, 5, 6, 11, 6, 10),
				Block.box(5, 5, 6, 6, 6, 10),
				Block.box(5, 10, 6, 6, 11, 10),
				Block.box(7, 7, 7, 9, 9, 9)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
		@Override
		public VoxelShape getAddonBaseShape(Direction d) {
			if(d == Direction.UP) {
				return SHAPE_U;
			}else if(d == Direction.DOWN) {
				return SHAPE_D;
			}else if(d == Direction.NORTH) {
				return SHAPE_N;
			}else if(d == Direction.SOUTH) {
				return SHAPE_S;
			}else if(d == Direction.EAST) {
				return SHAPE_E;
			}else {
				return SHAPE_W;
			}
		}
	}
	
	public static class BlockFortuneVoidQuarryAddon extends BlockQuarryAddon{
		
		private static final VoxelShape SHAPE_D = Stream.of(
				Block.box(2, 2, 2, 14, 4, 14),
				Block.box(2, 12, 2, 4, 14, 14),
				Block.box(2, 4, 2, 4, 12, 4),
				Block.box(2, 4, 12, 4, 12, 14),
				Block.box(12, 4, 12, 14, 12, 14),
				Block.box(12, 4, 2, 14, 12, 4),
				Block.box(12, 12, 2, 14, 14, 14),
				Block.box(4, 12, 12, 12, 14, 14),
				Block.box(4, 12, 2, 12, 14, 4),
				Block.box(5, 5, 5, 11, 11, 11)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_N = Stream.of(
				Block.box(2, 2, 2, 14, 14, 4),
				Block.box(2, 2, 12, 4, 14, 14),
				Block.box(2, 12, 4, 4, 14, 12),
				Block.box(2, 2, 4, 4, 4, 12),
				Block.box(12, 2, 4, 14, 4, 12),
				Block.box(12, 12, 4, 14, 14, 12),
				Block.box(12, 2, 12, 14, 14, 14),
				Block.box(4, 2, 12, 12, 4, 14),
				Block.box(4, 12, 12, 12, 14, 14),
				Block.box(5, 5, 5, 11, 11, 11)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_U = Stream.of(
				Block.box(2, 12, 2, 14, 14, 14),
				Block.box(2, 12, 2, 14, 14, 14),
				Block.box(2, 2, 2, 4, 4, 14),
				Block.box(2, 4, 2, 4, 12, 4),
				Block.box(2, 4, 12, 4, 12, 14),
				Block.box(12, 4, 12, 14, 12, 14),
				Block.box(12, 4, 2, 14, 12, 4),
				Block.box(12, 2, 2, 14, 4, 14),
				Block.box(4, 2, 12, 12, 4, 14),
				Block.box(4, 2, 2, 12, 4, 4),
				Block.box(5, 5, 5, 11, 11, 11)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
		
		private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
		private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
		private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
		
		@Override
		public VoxelShape getAddonBaseShape(Direction d) {
			if(d == Direction.UP) {
				return SHAPE_U;
			}else if(d == Direction.DOWN) {
				return SHAPE_D;
			}else if(d == Direction.NORTH) {
				return SHAPE_N;
			}else if(d == Direction.SOUTH) {
				return SHAPE_S;
			}else if(d == Direction.EAST) {
				return SHAPE_E;
			}else {
				return SHAPE_W;
			}
		}
	}
}
