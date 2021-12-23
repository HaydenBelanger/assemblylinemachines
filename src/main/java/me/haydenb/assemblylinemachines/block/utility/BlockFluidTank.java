package me.haydenb.assemblylinemachines.block.utility;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.utility.BlockFluidTank.TEFluidTank.FluidTankHandler;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidTank extends Block {

	private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

	private static final VoxelShape SHAPE = Stream.of(Block.makeCuboidShape(0, 0, 0, 16, 1, 16), Block.makeCuboidShape(0, 1, 15, 1, 15, 16),
			Block.makeCuboidShape(15, 1, 15, 16, 15, 16), Block.makeCuboidShape(0, 1, 0, 1, 15, 1), Block.makeCuboidShape(15, 1, 0, 16, 15, 1),
			Block.makeCuboidShape(0, 15, 0, 16, 16, 16), Block.makeCuboidShape(1, 1, 0, 15, 15, 1), Block.makeCuboidShape(1, 1, 15, 15, 15, 16),
			Block.makeCuboidShape(0, 1, 1, 1, 15, 15), Block.makeCuboidShape(15, 1, 1, 16, 15, 15)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();
	private final int _capacity;
	private final TemperatureResistance _tempres;

	public BlockFluidTank(int capacity, TemperatureResistance resist) {
		super(Block.Properties.create(Material.GLASS).notSolid().hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.GLASS)
				.variableOpacity());
		_capacity = capacity;
		_tempres = resist;
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if (state.getBlock() == this) {
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TEFluidTank tef = (TEFluidTank) Registry.getTileEntity("fluid_tank").create();
		tef.capacity = _capacity;
		tef.trs = _tempres;
		return tef;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundNBT nbt = stack.getTag();
			
			if(world.getTileEntity(pos) instanceof TEFluidTank && nbt.contains("assemblylinemachines:fluidstack")) {
				TEFluidTank tank = (TEFluidTank) world.getTileEntity(pos);
				
				tank.fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:fluidstack"));
				tank.sendUpdates();
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	public static class BlockItemFluidTank extends BlockItem{
		public BlockItemFluidTank(Block block) {
			super(block, new Item.Properties().group(Registry.creativeTab));
		}
		
		@Override
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			if(stack.hasTag()) {
				CompoundNBT nbt = stack.getTag();
				if(nbt.contains("assemblylinemachines:fluidstack")) {
					tooltip.add(1, new StringTextComponent("This Tank has a fluid stored!").deepCopy().applyTextStyles(TextFormatting.GREEN));
				}
			}
			super.addInformation(stack, worldIn, tooltip, flagIn);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

		if (!world.isRemote) {
			if (handIn.equals(Hand.MAIN_HAND)) {
				if (world.getTileEntity(pos) instanceof TEFluidTank) {
					TEFluidTank entity = (TEFluidTank) world.getTileEntity(pos);
					FluidTankHandler handler = entity.fluids;
					if (handler != null) {
						if (player.isSneaking()) {
							FluidStack f = handler.getFluidInTank(0);
							if (f.isEmpty() || f.getAmount() == 0) {
								player.sendStatusMessage(new StringTextComponent("This tank is empty."), true);
							} else {
								player.sendStatusMessage(new StringTextComponent(FORMAT.format(f.getAmount()) + "/" + FORMAT.format(handler.getTankCapacity(0)) + " mB "
										+ f.getFluid().getAttributes().getDisplayName(f).deepCopy().getString()), true);
							}
						} else {
							ItemStack stack = player.getHeldItemMainhand();

							if (!handler.getFluidInTank(0).getFluid().getAttributes().isGaseous()) {
								FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
								if (!player.isCreative() && far.isSuccess()) {
									if (stack.getCount() == 1) {
										player.inventory.removeStackFromSlot(player.inventory.currentItem);
									} else {
										stack.shrink(1);
									}
									ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
									return ActionResultType.CONSUME;

								}
								FluidActionResult farx = FluidUtil.tryFillContainer(stack, handler, 1000, player, true);
								if (!player.isCreative() && farx.isSuccess()) {
									if (stack.getCount() == 1) {
										player.inventory.removeStackFromSlot(player.inventory.currentItem);
									} else {
										stack.shrink(1);
									}
									ItemHandlerHelper.giveItemToPlayer(player, farx.getResult());
									return ActionResultType.CONSUME;
								}
							}
						}
					}
				}
			}
		}
		
		return ActionResultType.CONSUME;

	}

	public static class TEFluidTank extends me.haydenb.assemblylinemachines.helpers.BasicTileEntity {

		public FluidStack fluid = FluidStack.EMPTY;
		public int capacity = 0;
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
			if (handler != null) {
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

		public static class FluidTankHandler implements IFluidHandler {

			private final TEFluidTank te;

			FluidTankHandler(TEFluidTank te) {
				this.te = te;
			}

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(stack.getFluid());
				if (ff != BathCraftingFluids.NONE) {
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

				}

				te.sendUpdates();
				return new FluidStack(f, maxDrain);
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}

			private void sendIfNotNull(PlayerEntity player, String message) {
				if (player != null) {
					player.sendStatusMessage(new StringTextComponent(message), true);
				}
			}

		}

	}

}
