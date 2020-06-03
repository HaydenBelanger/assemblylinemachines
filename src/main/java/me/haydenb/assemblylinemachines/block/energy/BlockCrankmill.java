package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.ICrankableMachine;
import me.haydenb.assemblylinemachines.util.TEContainingBlock;
import me.haydenb.assemblylinemachines.util.TileEntityALMBase;
import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.ICrankableMachine.ICrankableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class BlockCrankmill extends TEContainingBlock implements ICrankableBlock{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(7, 7, 2, 9, 9, 6),
			Block.makeCuboidShape(5, 5, 6, 11, 11, 8),
			Block.makeCuboidShape(0, 0, 0, 16, 16, 2),
			Block.makeCuboidShape(0, 0, 14, 16, 16, 16),
			Block.makeCuboidShape(0, 13, 2, 3, 16, 14),
			Block.makeCuboidShape(13, 13, 2, 16, 16, 14),
			Block.makeCuboidShape(0, 0, 2, 16, 3, 14),
			Block.makeCuboidShape(5, 5, 10, 11, 11, 14),
			Block.makeCuboidShape(2, 3, 6, 3, 13, 7),
			Block.makeCuboidShape(2, 3, 10, 3, 13, 11),
			Block.makeCuboidShape(13, 3, 6, 14, 13, 7),
			Block.makeCuboidShape(13, 3, 10, 14, 13, 11),
			Block.makeCuboidShape(3, 10, 6, 5, 11, 7),
			Block.makeCuboidShape(11, 10, 6, 13, 11, 7),
			Block.makeCuboidShape(11, 10, 10, 13, 11, 11),
			Block.makeCuboidShape(3, 10, 10, 5, 11, 11),
			Block.makeCuboidShape(4, 4, 8, 12, 12, 10)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	
	public BlockCrankmill() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), 
				"crankmill", null, true, Direction.NORTH);
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		
		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
		if(d == Direction.WEST) {
			return SHAPE_W;
		}else if(d == Direction.SOUTH) {
			return SHAPE_S;
		}else if(d == Direction.EAST) {
			return SHAPE_E;
		}else {
			return SHAPE_N;
		}
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.CONSUME;
	}

	@Override
	public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.SUCCESS;
	}
	
	
	
	public static class TECrankmill extends TileEntityALMBase implements ICrankableMachine{

		public TECrankmill(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}
		
		public TECrankmill() {
			this(Registry.getTileEntity("crankmill"));
		}

		@Override
		public boolean perform() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
