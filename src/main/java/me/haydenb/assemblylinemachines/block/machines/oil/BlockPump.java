package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.fluid.FluidLevelManager;
import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
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
			.of(Block.makeCuboidShape(4, 8, 7, 6, 10, 9), Block.makeCuboidShape(10, 8, 7, 12, 10, 9), Block.makeCuboidShape(7, 8, 4, 9, 10, 6),
					Block.makeCuboidShape(2, 5, 7, 4, 10, 9), Block.makeCuboidShape(12, 5, 7, 14, 10, 9), Block.makeCuboidShape(7, 5, 2, 9, 10, 4),
					Block.makeCuboidShape(1, 6, 6, 5, 7, 10), Block.makeCuboidShape(11, 6, 6, 15, 7, 10), Block.makeCuboidShape(6, 6, 1, 10, 7, 5),
					Block.makeCuboidShape(5, 7, 6, 6, 11, 10), Block.makeCuboidShape(10, 7, 6, 11, 11, 10), Block.makeCuboidShape(6, 7, 5, 10, 11, 6),
					Block.makeCuboidShape(6, 5, 6, 10, 13, 10), Block.makeCuboidShape(3, 13, 3, 13, 16, 13), Block.makeCuboidShape(3, 12, 13, 8, 13, 16),
					Block.makeCuboidShape(8, 12, 13, 13, 13, 16), Block.makeCuboidShape(3, 5, 13, 13, 12, 16), Block.makeCuboidShape(0, 0, 0, 16, 5, 16))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape PUMP_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, PUMP_N);
	private static final VoxelShape PUMP_W = General.rotateShape(Direction.NORTH, Direction.WEST, PUMP_N);
	private static final VoxelShape PUMP_E = General.rotateShape(Direction.NORTH, Direction.EAST, PUMP_N);

	public BlockPump() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "pump");

		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	
	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		if (world.getTileEntity(pos) instanceof TEPump) {
			TEPump pump = (TEPump) world.getTileEntity(pos);
			player.sendStatusMessage(new StringTextComponent(pump.prevStatusMessage), true);
		}
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.CONSUME;
	}

	public static class TEPump extends BasicTileEntity implements ITickableTileEntity {

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

				if (simulate == false) {
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

		public TEPump(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEPump() {
			this(Registry.getTileEntity("pump"));
		}

		@Override
		public void tick() {
			if (!world.isRemote) {
				if (timer++ == 20) {
					timer = 0;

					if(handler == null) {
						handler = General.getCapabilityFromDirection(this, "handler", Direction.UP, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
					}
					if (world.getBlockState(pos.down()).getBlock() != Registry.getBlock("pumpshaft")) {
						prevStatusMessage = "Not connected to Pumpshaft.";
						forceState(false);
					} else {
						if (extracted.isEmpty()) {
							
							if (amount - 1800 >= 0) {
								FluidStack xfs = FluidLevelManager.drain(pos, world, 2000);
								if (!xfs.isEmpty() && xfs.getAmount() != 0) {

									amount = amount - 1800;
									extracted = xfs;

								} else {
									prevStatusMessage = "No fluid could be extracted from chunk.";
									forceState(false);
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
									prevStatusMessage = "Working... (" + Formatting.GENERAL_FORMAT.format(amount) + "/30,000 FE)";
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
			if (side == getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).getOpposite() && cap == CapabilityEnergy.ENERGY) {
				return energyHandler.cast();
			}

			return super.getCapability(cap, side);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return super.getCapability(cap);
		}
		
		@Override
		public void func_230337_a_(BlockState p_230337_1_, CompoundNBT compound) {
			super.func_230337_a_(p_230337_1_, compound);
			
			if (compound.contains("assemblylinemachines:fluid")) {
				extracted = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluid"));
			}

			amount = compound.getInt("assemblylinemachines:amount");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			if (!extracted.isEmpty()) {
				CompoundNBT sub = new CompoundNBT();
				extracted.writeToNBT(sub);
				compound.put("assemblylinemachines:fluid", sub);
			}

			compound.putInt("assemblylinemachines:amount", amount);
			return super.write(compound);
		}
		
		private void forceState(boolean status) {
			
			BlockState bs = world.getBlockState(pos.down());
			if(bs.func_235901_b_(StateProperties.MACHINE_ACTIVE)) {
				
				if(bs.get(StateProperties.MACHINE_ACTIVE) != status) {
					world.setBlockState(pos.down(), bs.with(StateProperties.MACHINE_ACTIVE, status));
				}
			}
		}
	}
}
