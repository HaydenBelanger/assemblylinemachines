package me.haydenb.assemblylinemachines.block;

import java.text.DecimalFormat;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.BlockFluidTank.TEFluidTank.FluidTankHandler;
import me.haydenb.assemblylinemachines.item.ToolStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.FluidProperty;
import me.haydenb.assemblylinemachines.util.FluidProperty.Fluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidTank extends Block {

	private static final VoxelShape SHAPE = Stream.of(Block.makeCuboidShape(0, 0, 0, 16, 1, 16),
			Block.makeCuboidShape(0, 1, 15, 1, 15, 16), Block.makeCuboidShape(15, 1, 15, 16, 15, 16),
			Block.makeCuboidShape(0, 1, 0, 1, 15, 1), Block.makeCuboidShape(15, 1, 0, 16, 15, 1),
			Block.makeCuboidShape(0, 15, 0, 16, 16, 16), Block.makeCuboidShape(1, 1, 0, 15, 15, 1),
			Block.makeCuboidShape(1, 1, 15, 15, 15, 16), Block.makeCuboidShape(1, 1, 1, 15, 15, 15),
			Block.makeCuboidShape(0, 1, 1, 1, 15, 15), Block.makeCuboidShape(15, 1, 1, 16, 15, 15)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

	private final int _capacity;
	private final TemperatureResistance _tempres;

	public BlockFluidTank(int capacity, TemperatureResistance resist) {
		super(Block.Properties.create(Material.GLASS).notSolid().hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.GLASS));
		_capacity = capacity;
		_tempres = resist;

		this.setDefaultState(this.stateContainer.getBaseState().with(FluidProperty.FLUID, Fluids.NONE));
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isVariableOpacity() {
		return true;
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			if (worldIn.getTileEntity(pos) instanceof TEFluidTank) {
				worldIn.removeTileEntity(pos);
			}
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(FluidProperty.FLUID);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if (state.getBlock() == this) {
			return true;
		}
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		return SHAPE;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TEFluidTank tef = (TEFluidTank) Registry.getTileEntity("fluid_tank").create();
		tef.capacity = _capacity;
		tef.trs = _tempres;
		return tef;
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		if (!world.isRemote) {
			if (player.getActiveHand() == Hand.MAIN_HAND) {
				if (world.getTileEntity(pos) instanceof TEFluidTank) {
					TEFluidTank entity = (TEFluidTank) world.getTileEntity(pos);
					FluidTankHandler handler = entity.fluids;
					if (handler != null) {
						if (player.isSneaking()) {
							FluidStack f = handler.getFluidInTank(0);
							Fluids ff = Fluids.getAssocFluids(f.getFluid());
							if (ff.equals(Fluids.NONE)) {
								player.sendStatusMessage(new StringTextComponent("This tank is empty."), true);
							} else {
								player.sendStatusMessage(
										new StringTextComponent(FORMAT.format(f.getAmount()) + "/"
												+ FORMAT.format(handler.getTankCapacity(0)) + " mB " + ff.getFriendlyName()),
										true);
							}
						} else {
							ItemStack is = player.getHeldItemMainhand();

							if(is.getItem() == Items.BUCKET) {
								
								FluidActionResult far = FluidUtil.tryFillContainer(is, handler, 1000, player, true);
								if(far.isSuccess()) {
									is.shrink(1);
									ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
								}
								
								
							}else {
								if(handler.fill(FluidUtil.getFluidContained(is).orElse(FluidStack.EMPTY), FluidAction.SIMULATE, player) == 1000) {
									FluidActionResult far = FluidUtil.tryEmptyContainer(is, handler, 1000, player, true);
									if(far.isSuccess()) {
										is.shrink(1);
										ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
									}
								}
								
							}
						}
					}
				}
			}
		}
		return ActionResultType.CONSUME;

	}

	public static class TEFluidTank extends me.haydenb.assemblylinemachines.util.machines.ALMTileEntity {

		private FluidStack fluid = FluidStack.EMPTY;
		private int capacity = 0;
		private TemperatureResistance trs = TemperatureResistance.COLD;
		
		FluidTankHandler fluids = new FluidTankHandler(this);
		
		protected LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> fluids);

		public TEFluidTank(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEFluidTank() {
			this(Registry.getTileEntity("fluid_tank"));
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (compound.contains("assemblylinemachines:capacity")) {
				capacity = compound.getInt("assemblylinemachines:capacity");
			}

			if (compound.contains("assemblylinemachines:fluidstack")) {
				fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluidstack"));
			}

			if (compound.contains("assemblylinemachines:temperatureresistance")) {
				trs = TemperatureResistance.valueOf(compound.getString("assemblylinemachines:temperatureresistance"));
			}
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return handler.cast();
			}
			
			return LazyOptional.empty();
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return this.getCapability(cap);
		}
		
		
		@Override
		public void remove() {
			super.remove();
			if(handler != null) {
				handler.invalidate();
			}
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			compound.putInt("assemblylinemachines:capacity", capacity);
			compound.putString("assemblylinemachines:temperatureresistance", trs.toString());
			if (fluid != null) {
				CompoundNBT sub = new CompoundNBT();
				fluid.writeToNBT(sub);
				compound.put("assemblylinemachines:fluidstack", sub);
			}
			return compound;
		}

		public boolean isEmpty() {
			return fluid.isEmpty();
		}
		
		
		public static class FluidTankHandler implements IFluidHandler{

			private final TEFluidTank te;
			FluidTankHandler(TEFluidTank te){
				this.te = te;
			}
			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				Fluids ff = Fluids.getAssocFluids(stack.getFluid());
				if(ff != Fluids.NONE) {
					return true;
				}
				return false;
			}

			@Override
			public int getTanks() {
				return 1;
			}

			@Override
			public int getTankCapacity(int tank) {
				return te.capacity;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				return te.fluid;
			}

			public int fill(FluidStack resource, FluidAction action, PlayerEntity player) {
				if (!te.fluid.isEmpty()) {
					if (resource.getFluid() != te.fluid.getFluid()) {
						sendIfNotNull(player, "This is not the same fluid.");
						return 0;
					}
				}
				
				Fluids ff = Fluids.getAssocFluids(resource.getFluid());
				if(ff == Fluids.NONE) {
					sendIfNotNull(player, "This tank cannot store this fluid.");
					return 0;
				}
				
				if (resource.getFluid().getAttributes().getTemperature() >= 800 && te.trs == TemperatureResistance.COLD) {
					sendIfNotNull(player, "This fluid is too hot for this tank");
					return 0;
				}
				int attemptedInsert = resource.getAmount();
				int rmCapacity = te.capacity - te.fluid.getAmount();
				if (rmCapacity < attemptedInsert) {
					attemptedInsert = rmCapacity;
				}

				if (action != FluidAction.SIMULATE) {
					if (te.fluid.isEmpty()) {
						te.fluid = resource;
					} else {
						te.fluid.setAmount(te.fluid.getAmount() + attemptedInsert);
					}
					if (attemptedInsert != 0 && te.getBlockState().get(FluidProperty.FLUID) != ff) {
						te.world.setBlockState(te.pos, te.getBlockState().with(FluidProperty.FLUID, ff));
						te.sendUpdates();
					}
				}
				
				te.sendUpdates();
				return attemptedInsert;
			}
			@Override
			public int fill(FluidStack resource, FluidAction action) {
				return fill(resource, action, null);
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				
				
				if (te.fluid.getAmount() < maxDrain) {
					maxDrain = te.fluid.getAmount();
				}

				Fluid f = te.fluid.getFluid();
				if (action != FluidAction.SIMULATE) {
					te.fluid.setAmount(te.fluid.getAmount() - maxDrain);
				}

				if (te.fluid.getAmount() <= 0) {
					te.fluid = FluidStack.EMPTY;

					if (te.getBlockState().get(FluidProperty.FLUID) != Fluids.NONE) {
						te.world.setBlockState(te.pos, te.getBlockState().with(FluidProperty.FLUID, Fluids.NONE));
					}
				}

				te.sendUpdates();
				return new FluidStack(f, maxDrain);
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
			
			private void sendIfNotNull(PlayerEntity player, String message) {
				if(player != null) {
					player.sendStatusMessage(new StringTextComponent(message), true);
				}
			}
			
		}

	}

}
