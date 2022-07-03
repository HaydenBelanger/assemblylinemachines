package me.haydenb.assemblylinemachines.block.machines;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.world.CapabilityChunkFluids;
import me.haydenb.assemblylinemachines.world.CapabilityChunkFluids.IChunkFluidCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockPump extends BlockTileEntity {

	private static final VoxelShape PUMP_N = Stream
			.of(Block.box(4, 8, 7, 6, 10, 9), Block.box(10, 8, 7, 12, 10, 9), Block.box(7, 8, 4, 9, 10, 6),
					Block.box(2, 5, 7, 4, 10, 9), Block.box(12, 5, 7, 14, 10, 9), Block.box(7, 5, 2, 9, 10, 4),
					Block.box(1, 6, 6, 5, 7, 10), Block.box(11, 6, 6, 15, 7, 10), Block.box(6, 6, 1, 10, 7, 5),
					Block.box(5, 7, 6, 6, 11, 10), Block.box(10, 7, 6, 11, 11, 10), Block.box(6, 7, 5, 10, 11, 6),
					Block.box(6, 5, 6, 10, 13, 10), Block.box(3, 13, 3, 13, 16, 13), Block.box(3, 12, 13, 8, 13, 16),
					Block.box(8, 12, 13, 13, 13, 16), Block.box(3, 5, 13, 13, 12, 16), Block.box(0, 0, 0, 16, 5, 16))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	private static final VoxelShape PUMP_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, PUMP_N);
	private static final VoxelShape PUMP_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, PUMP_N);
	private static final VoxelShape PUMP_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, PUMP_N);

	public BlockPump() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "pump");

		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}



	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState) {
		return bteDefaultReturnBlockEntity(pPos, pState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return bteDefaultReturnTicker(level, state, blockEntityType);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
		if (d == Direction.WEST) {
			return PUMP_W;
		} else if (d == Direction.SOUTH) {
			return PUMP_S;
		} else if (d == Direction.EAST) {
			return PUMP_E;
		} else {
			return PUMP_N;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}


	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		if (world.getBlockEntity(pos) instanceof TEPump) {
			TEPump pump = (TEPump) world.getBlockEntity(pos);
			player.displayClientMessage(Component.literal(pump.prevStatusMessage), true);
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.CONSUME;
	}

	public static class BlockPumpshaft extends Block{

		private static final VoxelShape SHAFT = Stream.of(
				Block.box(1, 5, 7, 3, 11, 9),Block.box(7, 5, 13, 9, 11, 15),
				Block.box(4, 5, 4, 12, 11, 12),Block.box(13, 5, 7, 15, 11, 9),
				Block.box(7, 5, 1, 9, 11, 3),Block.box(0, 0, 0, 16, 5, 16),Block.box(0, 11, 0, 16, 16, 16)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();

		public BlockPumpshaft() {
			super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
			this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false));
		}

		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
			builder.add(StateProperties.MACHINE_ACTIVE);
		}

		@Override
		public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
			return SHAFT;
		}
	}

	public static class TEPump extends BasicTileEntity implements ALMTicker<TEPump> {

		private int timer = 0;
		public IFluidHandler handler = null;
		private FluidStack extracted = FluidStack.EMPTY;
		private String prevStatusMessage = "";

		private int amount;
		protected IEnergyStorage energy = new IEnergyStorage() {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {

				if (30000 < maxReceive + amount) {
					maxReceive = 30000 - amount;
				}

				if (!simulate) {
					amount += maxReceive;
					sendUpdates();
				}

				return maxReceive;
			}

			@Override
			public int getMaxEnergyStored() {
				return 30000;
			}

			@Override
			public int getEnergyStored() {
				return amount;
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				return 0;
			}

			@Override
			public boolean canReceive() {
				return true;
			}

			@Override
			public boolean canExtract() {
				return false;
			}
		};
		protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);

		public TEPump(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}

		public TEPump(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("pump"), pos, state);
		}

		@Override
		public void tick(){
			if (!level.isClientSide) {
				if (timer++ == 20) {
					timer = 0;

					if(handler == null) {
						BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().above());
						if(te != null) {
							LazyOptional<IFluidHandler> cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN);
							handler = cap.orElse(null);
							if(handler != null) {
								cap.addListener((h) -> handler = null);
							}
						}
					}

					if (this.getLevel().getBlockState(this.getBlockPos().below()).getBlock() != Registry.getBlock("pumpshaft")) {
						prevStatusMessage = "Not connected to Pumpshaft.";
						forceState(false);
					} else {
						if (extracted.isEmpty()) {

							if (amount - 1800 >= 0) {
								boolean success = false;
								try {
									LazyOptional<IChunkFluidCapability> lazy = CapabilityChunkFluids.getChunkFluidCapability(this.getLevel().getChunkAt(this.getBlockPos()));
									if(lazy.isPresent()) {
										IChunkFluidCapability capability = lazy.orElseThrow(null);
										FluidStack xfs = capability.drain(2000, false);
										if(!xfs.isEmpty() && xfs.getAmount() != 0) {
											success = true;
											amount = amount - 1800;
											extracted = xfs;
										}
									}
								}catch(ExecutionException e) {
									e.printStackTrace();
								}finally {
									if(!success) {
										prevStatusMessage = "Could not connect to reservoir in chunk.";
										forceState(false);
									}
								}
							} else {
								prevStatusMessage = "Not enough power for operation.";
								forceState(false);
							}
						}

						if (!extracted.isEmpty()) {

							if (handler != null) {
								if (handler.fill(extracted, FluidAction.SIMULATE) == extracted.getAmount()) {
									handler.fill(extracted, FluidAction.EXECUTE);
									extracted = FluidStack.EMPTY;
									prevStatusMessage = "Working... (" + FormattingHelper.GENERAL_FORMAT.format(amount) + "/30,000 FE)";
									forceState(true);
								} else {
									prevStatusMessage = "Fluid storage could not accept fluid.";
									forceState(false);
								}
							} else {
								prevStatusMessage = "Could not find tank to export to.";
								forceState(false);
							}
						}
					}

					sendUpdates();

				}
			}

		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (side == getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite() && cap == CapabilityEnergy.ENERGY) {
				return energyHandler.cast();
			}

			return super.getCapability(cap, side);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return super.getCapability(cap);
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);

			if (compound.contains("assemblylinemachines:fluid")) {
				extracted = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluid"));
			}

			amount = compound.getInt("assemblylinemachines:amount");
		}

		@Override
		public void saveAdditional(CompoundTag compound) {
			if (!extracted.isEmpty()) {
				CompoundTag sub = new CompoundTag();
				extracted.writeToNBT(sub);
				compound.put("assemblylinemachines:fluid", sub);
			}

			compound.putInt("assemblylinemachines:amount", amount);
			super.saveAdditional(compound);
		}

		private void forceState(boolean status) {

			BlockState bs = this.getLevel().getBlockState(this.getBlockPos().below());
			if(bs.hasProperty(StateProperties.MACHINE_ACTIVE)) {

				if(bs.getValue(StateProperties.MACHINE_ACTIVE) != status) {
					this.getLevel().setBlockAndUpdate(this.getBlockPos().below(), bs.setValue(StateProperties.MACHINE_ACTIVE, status));
				}
			}
		}
	}
}
