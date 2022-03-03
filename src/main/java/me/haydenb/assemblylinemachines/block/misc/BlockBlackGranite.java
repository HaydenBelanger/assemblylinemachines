package me.haydenb.assemblylinemachines.block.misc;

import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;

public class BlockBlackGranite extends Block /*implements TOPProvider*/{

	public static final BooleanProperty NATURAL_GRANITE = BooleanProperty.create("natural");
	public BlockBlackGranite() {
		super(Block.Properties.of(Material.STONE).strength(3f, 9f));
		this.registerDefaultState(this.stateDefinition.any().setValue(NATURAL_GRANITE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(NATURAL_GRANITE);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn,
			BlockPos pos) {
		if(canToolMine(player, state)) {
			return super.getDestroyProgress(state, player, worldIn, pos);
		}
		
		return super.getDestroyProgress(state, player, worldIn, pos) * 0.05F;
	}

	/*
	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {
		if(state.getValue(NATURAL_GRANITE) == true) {
			if(canToolMine(player, state)) {
				probeInfo.horizontal().text(new TextComponent("§aBlock will drop when mined."));
			}else {
				probeInfo.horizontal().text(new TextComponent("§cBlock will not drop when mined."));
			}
		}
		
		
	}
	*/
	
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if(!player.isCreative() && !canToolMine(player, state)) {
			player.displayClientMessage(new TextComponent("§cA Pickaxe with mechanical power is needed to extract the Black Granite."), true);
		}
		return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
	}
	
	private static boolean canToolMine(Player player, BlockState state) {
		if(state.getValue(NATURAL_GRANITE) == true) {
			ItemStack item = player.getMainHandItem();
			if(item != ItemStack.EMPTY && item.hasTag()) {
				CompoundTag compound = item.getTag();
				if(compound.getBoolean("assemblylinemachines:canbreakblackgranite")) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	public static class BlockBlackGranitePillar extends Block {
		
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
		
		
		public BlockBlackGranitePillar() {
			super(Block.Properties.of(Material.STONE).strength(3f, 9f));
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

}
