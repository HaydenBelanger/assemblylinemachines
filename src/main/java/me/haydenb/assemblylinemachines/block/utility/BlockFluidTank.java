package me.haydenb.assemblylinemachines.block.utility;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.block.utility.BlockFluidTank.TEFluidTank.FluidTankHandler;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.BathCraftingFluid.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidTank extends Block implements EntityBlock {

	private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

	private static final VoxelShape SHAPE = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 1, 15, 1, 15, 16),
			Block.box(15, 1, 15, 16, 15, 16), Block.box(0, 1, 0, 1, 15, 1), Block.box(15, 1, 0, 16, 15, 1),
			Block.box(0, 15, 0, 16, 16, 16), Block.box(1, 1, 0, 15, 15, 1), Block.box(1, 1, 15, 15, 15, 16),
			Block.box(0, 1, 1, 1, 15, 15), Block.box(15, 1, 1, 16, 15, 15)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();
	private final int _capacity;
	private final TemperatureResistance _tempres;

	public BlockFluidTank(int capacity, TemperatureResistance resist) {
		super(Block.Properties.of(Material.METAL).noOcclusion().strength(4f, 15f).sound(SoundType.GLASS)
				.dynamicShape());
		_capacity = capacity;
		_tempres = resist;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			if (worldIn.getBlockEntity(pos) instanceof TEFluidTank) {
				worldIn.removeBlockEntity(pos);
			}
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		TEFluidTank tef = (TEFluidTank) Registry.getBlockEntity("fluid_tank").create(pPos, pState);
		tef.capacity = _capacity;
		tef.trs = _tempres;
		return tef;
	}
	
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundTag nbt = stack.getTag();
			
			if(world.getBlockEntity(pos) instanceof TEFluidTank && nbt.contains("assemblylinemachines:fluidstack")) {
				TEFluidTank tank = (TEFluidTank) world.getBlockEntity(pos);
				
				tank.fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:fluidstack"));
				tank.sendUpdates();
			}
		}
		super.setPlacedBy(world, pos, state, placer, stack);
	}
	public static class BlockItemFluidTank extends BlockItem{
		public BlockItemFluidTank(Block block) {
			super(block, new Item.Properties().tab(Registry.CREATIVE_TAB));
		}
		
		@Override
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(stack.hasTag()) {
				CompoundTag nbt = stack.getTag();
				if(nbt.contains("assemblylinemachines:fluidstack")) {
					tooltip.add(1, new TextComponent("This Tank has a fluid stored!").withStyle(ChatFormatting.GREEN));
				}
			}
			super.appendHoverText(stack, worldIn, tooltip, flagIn);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {

		if (!world.isClientSide) {
			if (handIn.equals(InteractionHand.MAIN_HAND)) {
				if (world.getBlockEntity(pos) instanceof TEFluidTank) {
					TEFluidTank entity = (TEFluidTank) world.getBlockEntity(pos);
					FluidTankHandler handler = entity.fluids;
					if (handler != null) {
						if (player.isShiftKeyDown()) {
							FluidStack f = handler.getFluidInTank(0);
							if (f.isEmpty() || f.getAmount() == 0) {
								player.displayClientMessage(new TextComponent("This tank is empty."), true);
							} else {
								player.displayClientMessage(new TextComponent(FORMAT.format(f.getAmount()) + "/" + FORMAT.format(handler.getTankCapacity(0)) + " mB "
										+ f.getFluid().getAttributes().getDisplayName(f).getString()), true);
							}
						} else {
							ItemStack stack = player.getMainHandItem();

							if (!handler.getFluidInTank(0).getFluid().getAttributes().isGaseous()) {
								FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
								if (!player.isCreative() && far.isSuccess()) {
									if (stack.getCount() == 1) {
										player.getInventory().removeItemNoUpdate(player.getInventory().selected);
									} else {
										stack.shrink(1);
									}
									ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
									return InteractionResult.CONSUME;

								}
								FluidActionResult farx = FluidUtil.tryFillContainer(stack, handler, 1000, player, true);
								if (!player.isCreative() && farx.isSuccess()) {
									if (stack.getCount() == 1) {
										player.getInventory().removeItemNoUpdate(player.getInventory().selected);
									} else {
										stack.shrink(1);
									}
									ItemHandlerHelper.giveItemToPlayer(player, farx.getResult());
									return InteractionResult.CONSUME;
								}
							}
						}
					}
				}
			}
		}
		
		return InteractionResult.CONSUME;

	}

	public static class TEFluidTank extends BasicTileEntity {

		public FluidStack fluid = FluidStack.EMPTY;
		public int capacity = 0;
		private TemperatureResistance trs = TemperatureResistance.COLD;

		FluidTankHandler fluids = new FluidTankHandler(this);

		protected LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> fluids);

		public TEFluidTank(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}

		public TEFluidTank(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("fluid_tank"), pos, state);
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
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
		public void setRemoved() {
			super.setRemoved();
			if (handler != null) {
				handler.invalidate();
			}
		}

		@Override
		public CompoundTag save(CompoundTag compound) {
			super.save(compound);

			compound.putInt("assemblylinemachines:capacity", capacity);
			compound.putString("assemblylinemachines:temperatureresistance", trs.toString());
			if (fluid != null) {
				CompoundTag sub = new CompoundTag();
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

			public int fill(FluidStack resource, FluidAction action, Player player) {
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

			private void sendIfNotNull(Player player, String message) {
				if (player != null) {
					player.displayClientMessage(new TextComponent(message), true);
				}
			}

		}

	}

}
