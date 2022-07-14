package me.haydenb.assemblylinemachines.block.misc;

import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;

public class BlockFancyPillar extends Block {
	
	public static final BooleanProperty PILLAR_TOP = BooleanProperty.create("top");
	public static final BooleanProperty PILLAR_BOTTOM = BooleanProperty.create("bottom");
	
	public static final VoxelShape BASE = Block.box(2, 0, 2, 14, 16, 14);
	public static final VoxelShape CAP_BOTTOM = Block.box(0, 0, 0, 16, 4, 16);
	public static final VoxelShape CAP_TOP = Block.box(0, 12, 0, 16, 16, 16);
	public static final VoxelShape BOTTOM_AND_BASE = Stream.of(BASE, CAP_BOTTOM).reduce((v1, v2) -> {
		return Shapes.join(v1, v2, BooleanOp.OR);
	}).get();
	public static final VoxelShape TOP_AND_BASE = Stream.of(BASE, CAP_TOP).reduce((v1, v2) -> {
		return Shapes.join(v1, v2, BooleanOp.OR);
	}).get();
	public static final VoxelShape ALL = Stream.of(TOP_AND_BASE, CAP_BOTTOM).reduce((v1, v2) -> {
		return Shapes.join(v1, v2, BooleanOp.OR);
	}).get();
	
	
	public BlockFancyPillar(Material material, float hardness, float resistance) {
		super(Block.Properties.of(material).strength(hardness, resistance));
		this.registerDefaultState(this.stateDefinition.any().setValue(PILLAR_TOP, true).setValue(PILLAR_BOTTOM, true));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockPos thisPos = pContext.getClickedPos().relative(pContext.getClickedFace());
		BlockState bs = this.defaultBlockState();
		if(pContext.getLevel().getBlockState(thisPos.above()).is(this)) {
			bs = bs.setValue(PILLAR_TOP, false);
		}
		
		if(pContext.getLevel().getBlockState(thisPos.below()).is(this)) {
			bs = bs.setValue(PILLAR_BOTTOM, false);
		}
		
		return bs;
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if(pState.getValue(PILLAR_TOP) == true) {
			return pState.getValue(PILLAR_BOTTOM) == true ? ALL : TOP_AND_BASE;
		}else if(pState.getValue(PILLAR_BOTTOM) == true) {
			return pState.getValue(PILLAR_TOP) == true ? ALL : BOTTOM_AND_BASE;
		}else {
			return BASE;
		}
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(PILLAR_TOP).add(PILLAR_BOTTOM);
	}
	
	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		return pState.setValue(PILLAR_TOP, !pLevel.getBlockState(pCurrentPos.above()).is(this)).setValue(PILLAR_BOTTOM, !pLevel.getBlockState(pCurrentPos.below()).is(this));
	}
}