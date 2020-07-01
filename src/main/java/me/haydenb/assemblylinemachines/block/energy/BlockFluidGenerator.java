package me.haydenb.assemblylinemachines.block.energy;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.TECrankmill;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine.ManagedDirection;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class BlockFluidGenerator extends BlockScreenTileEntity<TECrankmill> {
	
	private FluidGeneratorTypes type;
	
	public BlockFluidGenerator(FluidGeneratorTypes type) {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "crankmill", TECrankmill.class);
		
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(StateProperties.MACHINE_ACTIVE, true));
		this.type = type;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING,
				context.getPlacementHorizontalFacing().getOpposite());
	}
	
	public FluidGeneratorTypes getType() {
		return type;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING, StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
		if (d == Direction.WEST) {
			return type.shapeW;
		} else if (d == Direction.SOUTH) {
			return type.shapeS;
		} else if (d == Direction.EAST) {
			return type.shapeE;
		} else {
			return type.shapeN;
		}
	}

	public static enum FluidGeneratorTypes{
		
		
		COMBUSTION(Stream.of(
				Block.makeCuboidShape(3, 3, 3, 13, 7, 13),Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
				Block.makeCuboidShape(0, 7, 0, 16, 10, 16),Block.makeCuboidShape(0, 3, 0, 16, 7, 3),
				Block.makeCuboidShape(0, 3, 13, 3, 7, 16),Block.makeCuboidShape(13, 3, 13, 16, 7, 16),
				Block.makeCuboidShape(4, 10, 4, 12, 14, 12),Block.makeCuboidShape(3, 14, 3, 13, 16, 13),
				Block.makeCuboidShape(4, 3, 15, 5, 5, 16),Block.makeCuboidShape(6, 3, 15, 7, 5, 16),
				Block.makeCuboidShape(9, 3, 15, 10, 5, 16),Block.makeCuboidShape(11, 3, 15, 12, 5, 16),
				Block.makeCuboidShape(4, 5, 13, 5, 6, 16),Block.makeCuboidShape(6, 5, 13, 7, 6, 16),
				Block.makeCuboidShape(9, 5, 13, 10, 6, 16),Block.makeCuboidShape(11, 5, 13, 12, 6, 16),
				Block.makeCuboidShape(13, 3, 11, 16, 7, 12),Block.makeCuboidShape(13, 3, 9, 16, 7, 10),
				Block.makeCuboidShape(13, 3, 6, 16, 7, 7),Block.makeCuboidShape(13, 3, 4, 16, 7, 5),
				Block.makeCuboidShape(0, 3, 4, 3, 7, 5),Block.makeCuboidShape(0, 3, 6, 3, 7, 7),
				Block.makeCuboidShape(0, 3, 9, 3, 7, 10),Block.makeCuboidShape(0, 3, 11, 3, 7, 12),
				Block.makeCuboidShape(13, 4, 3, 15, 6, 13),Block.makeCuboidShape(1, 4, 3, 3, 6, 13)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), true, null, ManagedDirection.TOP, () -> ConfigHolder.COMMON.combustionFluids),
		GEOTHERMAL(Stream.of(
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),Block.makeCuboidShape(5, 6, 0, 11, 16, 16),Block.makeCuboidShape(2, 2, 1, 14, 5, 16),Block.makeCuboidShape(5, 5, 1, 11, 6, 16),
				Block.makeCuboidShape(14, 2, 0, 16, 5, 16),Block.makeCuboidShape(14, 5, 0, 16, 6, 2),Block.makeCuboidShape(0, 5, 0, 2, 6, 2),Block.makeCuboidShape(11, 5, 1, 14, 6, 2),
				Block.makeCuboidShape(2, 5, 1, 5, 6, 2),Block.makeCuboidShape(11, 6, 0, 16, 11, 2),Block.makeCuboidShape(0, 6, 0, 5, 11, 2),Block.makeCuboidShape(11, 5, 11, 16, 16, 16),
				Block.makeCuboidShape(0, 5, 11, 5, 16, 16),Block.makeCuboidShape(11, 14, 0, 16, 16, 11),Block.makeCuboidShape(0, 14, 0, 5, 16, 11),Block.makeCuboidShape(11, 11, 0, 16, 14, 5),
				Block.makeCuboidShape(0, 11, 0, 5, 14, 5),Block.makeCuboidShape(0, 2, 0, 2, 5, 16),Block.makeCuboidShape(1, 10, 6, 5, 14, 10),Block.makeCuboidShape(11, 10, 6, 15, 14, 10),
				Block.makeCuboidShape(1, 6, 2, 5, 10, 10),Block.makeCuboidShape(11, 6, 2, 15, 10, 10),Block.makeCuboidShape(0, 5, 3, 5, 11, 4),Block.makeCuboidShape(11, 5, 3, 16, 11, 4),
				Block.makeCuboidShape(0, 5, 5, 5, 11, 6),Block.makeCuboidShape(11, 5, 5, 16, 11, 6),Block.makeCuboidShape(0, 10, 6, 5, 11, 11),Block.makeCuboidShape(11, 10, 6, 16, 11, 11),
				Block.makeCuboidShape(0, 12, 5, 5, 13, 11),Block.makeCuboidShape(11, 12, 5, 16, 13, 11),Block.makeCuboidShape(2, 2, 1, 14, 6, 1),Block.makeCuboidShape(11, 2, 0, 12, 6, 1),
				Block.makeCuboidShape(9, 2, 0, 10, 6, 1),Block.makeCuboidShape(6, 2, 0, 7, 6, 1),Block.makeCuboidShape(4, 2, 0, 5, 6, 1)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), true, null, null, () -> ConfigHolder.COMMON.geothermalFluids);
		
		private final VoxelShape shapeN;
		private final VoxelShape shapeS;
		private final VoxelShape shapeW;
		private final VoxelShape shapeE;
		public final ManagedDirection inputSide;
		public final ManagedDirection outputSide;
		public final Supplier<ArrayList<Pair<Fluid, Integer>>> supplier;
		FluidGeneratorTypes(VoxelShape NShape, boolean supportsCoolant, ManagedDirection inputSide, ManagedDirection outputSide, Supplier<ArrayList<Pair<Fluid, Integer>>> validFluids){
			shapeN = NShape;
			shapeS = General.rotateShape(Direction.NORTH, Direction.SOUTH, shapeN);
			shapeW = General.rotateShape(Direction.NORTH, Direction.WEST, shapeN);
			shapeE = General.rotateShape(Direction.NORTH, Direction.EAST, shapeN);
			supplier = validFluids;
			this.inputSide = inputSide;
			this.outputSide = outputSide;
		}
		
	}
}
