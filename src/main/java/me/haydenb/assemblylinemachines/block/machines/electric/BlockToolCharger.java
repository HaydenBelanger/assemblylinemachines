package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.*;
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
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockToolCharger extends BlockTileEntity{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(5, 5, 11, 11, 11, 13),
			Block.makeCuboidShape(5, 11, 5, 11, 13, 11),
			Block.makeCuboidShape(5, 5, 5, 11, 11, 11),
			Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
			Block.makeCuboidShape(0, 13, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 3, 0, 3, 13, 3),
			Block.makeCuboidShape(13, 3, 0, 16, 13, 3),
			Block.makeCuboidShape(0, 3, 13, 16, 13, 16)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockToolCharger() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "tool_charger");
		
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(StateProperties.MACHINE_ACTIVE, false));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		if (world.getTileEntity(pos) instanceof TEToolCharger) {
			TEToolCharger pump = (TEToolCharger) world.getTileEntity(pos);
			player.sendStatusMessage(new StringTextComponent(pump.prevStatusMessage), true);
		}
		
		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
		if (d == Direction.WEST) {
			return SHAPE_W;
		} else if (d == Direction.SOUTH) {
			return SHAPE_S;
		} else if (d == Direction.EAST) {
			return SHAPE_E;
		} else {
			return SHAPE_N;
		}
	}
	

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(HorizontalBlock.HORIZONTAL_FACING).add(StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.PASS;
	}
	
	
	public static class TEToolCharger extends BasicTileEntity implements ITickableTileEntity{

		
		private IItemHandler handler = null;
		private String prevStatusMessage = "";
		private int timer = 0;
		
		
		public TEToolCharger(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEToolCharger() {
			this(Registry.getTileEntity("tool_charger"));
		}
		
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
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			amount = compound.getInt("assemblylinemachines:amount");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {

			compound.putInt("assemblylinemachines:amount", amount);
			return super.write(compound);
		}
		
		private boolean getCapability() {
			TileEntity te = world.getTileEntity(pos.offset(Direction.UP));
			if(te != null) {
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
						Direction.DOWN);
				IItemHandler output = cap.orElse(null);
				if (output != null) {
					TEToolCharger ipcte = this;
					cap.addListener(new NonNullConsumer<LazyOptional<IItemHandler>>() {

						@Override
						public void accept(LazyOptional<IItemHandler> t) {
							if (ipcte != null) {
								ipcte.handler = null;
							}
						}
					});

					this.handler = output;
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void tick() {
			if(!world.isRemote) {
				if(timer++ == 20) {
					timer = 0;
					
					boolean didSomething = false;
					if(handler != null || getCapability()) {
						for(int i = 0; i < handler.getSlots(); i++) {
							ItemStack stack = handler.getStackInSlot(i);
							LazyOptional<IEnergyStorage> opt = stack.getCapability(CapabilityEnergy.ENERGY);
							IEnergyStorage storage = opt.orElse(null);
							
							if(storage != null) {
								
								int max;
								
								if(amount >= 2000) {
									max = 2000;
								}else {
									max = amount;
								}
								
								int ext = storage.receiveEnergy(max, false);
								if(ext != 0) {
									amount = amount - ext;
									prevStatusMessage = stack.getItem().getDisplayName(stack).getUnformattedComponentText();
									didSomething = true;
									break;
								}
								
							}
							
						}
					}
					
					if(didSomething) {
						prevStatusMessage = "Charging " + prevStatusMessage + "...";
						if(getBlockState().get(StateProperties.MACHINE_ACTIVE) == false) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
						}
					}else {
						prevStatusMessage = "Charger idle...";
						if(getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
						}
					}
					prevStatusMessage = prevStatusMessage + " (" + Formatting.GENERAL_FORMAT.format(amount) + "/30,000 FE)";
					sendUpdates();
				}
			}
			
		}
		
		
		
	}

}
