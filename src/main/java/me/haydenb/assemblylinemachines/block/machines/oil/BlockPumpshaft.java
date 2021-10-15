package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class BlockPumpshaft extends Block{
	
	private static final VoxelShape SHAFT = Stream.of(
			Block.makeCuboidShape(1, 5, 7, 3, 11, 9),
			Block.makeCuboidShape(7, 5, 13, 9, 11, 15),
			Block.makeCuboidShape(4, 5, 4, 12, 11, 12),
			Block.makeCuboidShape(13, 5, 7, 15, 11, 9),
			Block.makeCuboidShape(7, 5, 1, 9, 11, 3),
			Block.makeCuboidShape(0, 0, 0, 16, 5, 16),
			Block.makeCuboidShape(0, 11, 0, 16, 16, 16)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	
	public BlockPumpshaft() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		
		
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.MACHINE_ACTIVE, false));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		
		builder.add(StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		
		return SHAFT;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if(!world.isRemote && hand.equals(Hand.MAIN_HAND)) {
			
		}
		return ActionResultType.CONSUME;
	}
}
