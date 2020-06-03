package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.TEContainingBlock;
import me.haydenb.assemblylinemachines.util.TileEntityALMBase;
import me.haydenb.assemblylinemachines.util.Utils;
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

public class BlockBasicBatteryCell extends TEContainingBlock{

	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(10, 3, 0, 12, 13, 2),Block.makeCuboidShape(4, 3, 0, 6, 13, 2),
			Block.makeCuboidShape(2, 5, 3, 2, 11, 13),Block.makeCuboidShape(14, 5, 3, 14, 11, 13),
			Block.makeCuboidShape(4, 2, 1, 6, 3, 2),Block.makeCuboidShape(10, 2, 1, 12, 3, 2),
			Block.makeCuboidShape(10, 13, 1, 12, 14, 2),Block.makeCuboidShape(4, 13, 1, 6, 14, 2),
			Block.makeCuboidShape(2, 2, 2, 14, 14, 2),Block.makeCuboidShape(7, 2, 0, 9, 14, 2),
			Block.makeCuboidShape(12, 7, 1, 13, 9, 2),Block.makeCuboidShape(9, 7, 1, 10, 9, 2),
			Block.makeCuboidShape(6, 7, 1, 7, 9, 2),Block.makeCuboidShape(3, 7, 1, 4, 9, 2),
			Block.makeCuboidShape(0, 0, 13, 16, 16, 16),Block.makeCuboidShape(0, 0, 0, 16, 2, 13),
			Block.makeCuboidShape(0, 14, 0, 16, 16, 13),Block.makeCuboidShape(0, 2, 3, 2, 5, 13),
			Block.makeCuboidShape(14, 2, 3, 16, 5, 13),Block.makeCuboidShape(0, 11, 3, 2, 14, 13),
			Block.makeCuboidShape(14, 11, 3, 16, 14, 13),Block.makeCuboidShape(0, 2, 0, 3, 14, 3),
			Block.makeCuboidShape(13, 2, 0, 16, 14, 3),Block.makeCuboidShape(14, 5, 4, 15, 11, 5),
			Block.makeCuboidShape(14, 5, 6, 15, 11, 7),Block.makeCuboidShape(14, 5, 9, 15, 11, 10),
			Block.makeCuboidShape(14, 5, 11, 15, 11, 12),Block.makeCuboidShape(1, 5, 6, 2, 11, 7),
			Block.makeCuboidShape(1, 5, 4, 2, 11, 5),Block.makeCuboidShape(1, 5, 9, 2, 11, 10),
			Block.makeCuboidShape(1, 5, 11, 2, 11, 12)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	
	public BlockBasicBatteryCell() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), 
				"basic_battery_cell", null, true, Direction.NORTH);
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
	
	
	
	public static class TEBasicBatteryCell extends TileEntityALMBase{

		public TEBasicBatteryCell(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}
		
		public TEBasicBatteryCell() {
			this(Registry.getTileEntity("basic_battery_cell"));
		}
		
	}
}
