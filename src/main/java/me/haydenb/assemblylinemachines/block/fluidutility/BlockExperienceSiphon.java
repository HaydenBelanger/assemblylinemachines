package me.haydenb.assemblylinemachines.block.fluidutility;

import java.util.List;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockExperienceSiphon extends BlockTileEntity {
	
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 3, 2),Block.box(0, 0, 14, 16, 3, 16),
			Block.box(0, 0, 2, 2, 3, 14),Block.box(14, 0, 2, 16, 3, 14),
			Block.box(2, 0, 2, 14, 2, 14),Block.box(5, 2, 2, 6, 3, 14),
			Block.box(10, 2, 2, 11, 3, 14),Block.box(2, 2, 5, 5, 3, 6),
			Block.box(11, 2, 5, 14, 3, 6),Block.box(11, 2, 10, 14, 3, 11),
			Block.box(2, 2, 10, 5, 3, 11),Block.box(6, 2, 10, 10, 3, 11),
			Block.box(6, 2, 5, 10, 3, 6),Block.box(1, 3, 1, 2, 15, 2),
			Block.box(14, 3, 1, 15, 15, 2),Block.box(1, 3, 14, 2, 15, 15),
			Block.box(14, 3, 14, 15, 15, 15)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	
	public BlockExperienceSiphon() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "experience_siphon",
				SHAPE_N, false, null);
	}

	@Override
	public BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState) {
		return bteDefaultReturnBlockEntity(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state,
			BlockEntityType<T> blockEntityType) {
		return bteDefaultReturnTicker(level, state, blockEntityType);
	}

	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.CONSUME;
	}
	
	public static class TEExperienceSiphon extends BasicTileEntity implements ALMTicker<TEExperienceSiphon>{
		
		private int timer = 0;
		private IFluidHandler output = null;
		
		public TEExperienceSiphon(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
			super(tileEntityType, pos, state);
		}
		
		public TEExperienceSiphon(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("experience_siphon"), pos, state);
		}
		
		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == 10) {
					timer = 0;
					if(output == null) {
						output = Utils.getCapabilityFromDirection(this, (lx) -> {
							if(this != null) output = null;
						}, Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
					}else {
						BlockPos bPos = this.getBlockPos();
						AABB range = new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 2, bPos.getZ() + 1);
						
						List<Player> players = this.getLevel().getEntitiesOfClass(Player.class, range);
						for(Player player : players) {
							int toDrain = player.totalExperience < 100 ? player.totalExperience : 100;
							FluidStack fill = new FluidStack(Registry.getFluid("liquid_experience"), toDrain * 15);
							if(output.fill(fill, FluidAction.SIMULATE) == fill.getAmount()) {
								player.giveExperiencePoints(-toDrain);
								output.fill(fill, FluidAction.EXECUTE);
							}
							
						}
					}
				}
			}
			
		}
		
		
	}
	
}
