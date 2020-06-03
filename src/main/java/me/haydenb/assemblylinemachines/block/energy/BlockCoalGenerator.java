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

public class BlockCoalGenerator extends TEContainingBlock{

	private static final VoxelShape SHAPE_S = Stream.of(
			Block.makeCuboidShape(14, 9, 3, 16, 16, 13),
			Block.makeCuboidShape(0, 9, 3, 2, 16, 13),
			Block.makeCuboidShape(3, 9, 0, 13, 16, 2),
			Block.makeCuboidShape(0, 0, 0, 16, 9, 16),
			Block.makeCuboidShape(2, 9, 2, 14, 16, 14)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_N = Utils.rotateShape(Direction.SOUTH, Direction.NORTH, SHAPE_S);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.SOUTH, Direction.EAST, SHAPE_S);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.SOUTH, Direction.WEST, SHAPE_S);
	
	public BlockCoalGenerator() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), 
				"coal_generator", null, true, Direction.NORTH);
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
	
	
	
	public static class TECoalGenerator extends TileEntityALMBase{

		public TECoalGenerator(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}
		
		public TECoalGenerator() {
			this(Registry.getTileEntity("coal_generator"));
		}
		
	}
}
