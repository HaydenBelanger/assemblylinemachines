package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.BathCraftingFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public class BlockPumpshaft extends Block{
	
	private static final VoxelShape SHAFT = Stream.of(
			Block.box(1, 5, 7, 3, 11, 9),
			Block.box(7, 5, 13, 9, 11, 15),
			Block.box(4, 5, 4, 12, 11, 12),
			Block.box(13, 5, 7, 15, 11, 9),
			Block.box(7, 5, 1, 9, 11, 3),
			Block.box(0, 0, 0, 16, 5, 16),
			Block.box(0, 11, 0, 16, 16, 16)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	
	public BlockPumpshaft() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
		
		
		this.registerDefaultState(this.stateDefinition.any().setValue(BathCraftingFluid.MACHINE_ACTIVE, false));
	}
	
	
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		
		builder.add(BathCraftingFluid.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		return SHAFT;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!world.isClientSide && hand.equals(InteractionHand.MAIN_HAND)) {
			
		}
		return InteractionResult.CONSUME;
	}
}
