package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockToolCharger extends BlockTileEntity{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(5, 5, 11, 11, 11, 13),
			Block.box(5, 11, 5, 11, 13, 11),
			Block.box(5, 5, 5, 11, 11, 11),
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 13, 0, 16, 16, 16),
			Block.box(0, 3, 0, 3, 13, 3),
			Block.box(13, 3, 0, 16, 13, 3),
			Block.box(0, 3, 13, 16, 13, 16)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockToolCharger() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "tool_charger");
		
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(StateProperties.MACHINE_ACTIVE, false));
	}
	
	
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		if (world.getBlockEntity(pos) instanceof TEToolCharger) {
			TEToolCharger pump = (TEToolCharger) world.getBlockEntity(pos);
			player.displayClientMessage(new TextComponent(pump.prevStatusMessage), true);
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player) {
		
		return InteractionResult.PASS;
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
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(HorizontalDirectionalBlock.FACING).add(StateProperties.MACHINE_ACTIVE);
	}
	
	
	public static class TEToolCharger extends BasicTileEntity implements ALMTicker<TEToolCharger>{

		
		private IItemHandler handler = null;
		private String prevStatusMessage = "";
		private int timer = 0;
		
		private final int configMaxChargeRate;
		private final int configMaxCapacity;
		
		public TEToolCharger(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
			this.configMaxChargeRate = ConfigHolder.COMMON.toolChargerChargeRate.get();
			this.configMaxCapacity = ConfigHolder.COMMON.toolChargerMaxEnergyStorage.get();
		}

		public TEToolCharger(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("tool_charger"), pos, state);
		}
		
		private int amount;
		protected IEnergyStorage energy = new IEnergyStorage() {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {

				if (configMaxCapacity < maxReceive + amount) {
					maxReceive = configMaxCapacity - amount;
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
			
			amount = compound.getInt("assemblylinemachines:amount");
		}

		@Override
		public CompoundTag save(CompoundTag compound) {

			compound.putInt("assemblylinemachines:amount", amount);
			return super.save(compound);
		}
		
		private boolean getCapability() {
			BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(Direction.UP));
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
			if(!level.isClientSide) {
				if(timer++ == 8) {
					timer = 0;
					
					boolean didSomething = false;
					if(handler != null || getCapability()) {
						for(int i = 0; i < handler.getSlots(); i++) {
							ItemStack stack = handler.getStackInSlot(i);
							LazyOptional<IEnergyStorage> opt = stack.getCapability(CapabilityEnergy.ENERGY);
							IEnergyStorage storage = opt.orElse(null);
							
							if(storage != null) {
								
								int max;
								
								if(amount >= this.configMaxChargeRate) {
									max = this.configMaxChargeRate;
								}else {
									max = amount;
								}
								
								int ext = storage.receiveEnergy(max, false);
								if(ext != 0) {
									amount = amount - ext;
									prevStatusMessage = stack.getItem().getName(stack).getString();
									didSomething = true;
									break;
								}
								
							}
							
						}
					}
					
					if(didSomething) {
						prevStatusMessage = "Charging " + prevStatusMessage + "...";
						if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE) == false) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
						}
					}else {
						prevStatusMessage = "Charger idle...";
						if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
						}
					}
					prevStatusMessage = prevStatusMessage + " (" + Formatting.GENERAL_FORMAT.format(amount) + "/" + Formatting.GENERAL_FORMAT.format(configMaxCapacity) + " FE)";
					sendUpdates();
				}
			}
			
		}
		
		
		
	}

}
