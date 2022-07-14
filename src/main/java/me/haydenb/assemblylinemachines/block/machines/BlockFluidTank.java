package me.haydenb.assemblylinemachines.block.machines;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Stream;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.block.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.item.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.PluginTOPRegistry.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
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
import net.minecraft.world.level.material.Fluids;
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
	public final int capacity;
	private final TemperatureResistance temperatureResistance;
	private final int topProgressColor;

	public BlockFluidTank(int capacity, TemperatureResistance resist, int topProgressColor) {
		super(Block.Properties.of(Material.METAL).noOcclusion().strength(4f, 15f).sound(SoundType.GLASS)
				.dynamicShape());
		this.capacity = capacity;
		this.temperatureResistance = resist;
		this.topProgressColor = topProgressColor;
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
		return Registry.getBlockEntity("fluid_tank").create(pPos, pState);
	}
	
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundTag nbt = stack.getTag();
			if(world.getBlockEntity(pos) instanceof TEFluidTank tank && !nbt.isEmpty()) {
				tank.fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:fluidstack"));
				tank.upgraded = nbt.getByte("assemblylinemachines:upgraded");
				tank.sendUpdates();
			}
		}
		super.setPlacedBy(world, pos, state, placer, stack);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if(stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			
			byte upgraded = nbt.getByte("assemblylinemachines:upgraded");
			switch(upgraded) {
			case 1 -> tooltip.add(1, new TextComponent("This Tank has an Internal Water Generator installed.").withStyle(ChatFormatting.BLUE));
			case 2 -> tooltip.add(1, new TextComponent("This Tank is modified to be creative.").withStyle(ChatFormatting.DARK_PURPLE));
			}
			
			FluidStack fs = FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:fluidstack"));
			if(!fs.isEmpty()) switch(upgraded) {
				case 0 -> tooltip.add(1, new TextComponent("This Tank has " + FormattingHelper.formatToSuffix(fs.getAmount()) + " mB of " + fs.getDisplayName().getString() + " stored.").withStyle(ChatFormatting.GREEN));
				case 2 -> tooltip.add(1, new TextComponent("This Tank has infinite " + fs.getDisplayName().getString() + " stored.").withStyle(ChatFormatting.GREEN));
			}
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {

		if (!world.isClientSide) {
			if (handIn.equals(InteractionHand.MAIN_HAND)) {
				if (world.getBlockEntity(pos) instanceof TEFluidTank) {
					TEFluidTank entity = (TEFluidTank) world.getBlockEntity(pos);
					IFluidHandler handler = entity.fluids;
					if (handler != null) {
						if (player.isShiftKeyDown()) {
							FluidStack f = handler.getFluidInTank(0);
							if (f.isEmpty() || f.getAmount() == 0) {
								player.displayClientMessage(new TextComponent("This tank is empty."), true);
							} else {
								if(f.getAmount() == Integer.MAX_VALUE) {
									player.displayClientMessage(new TextComponent("Infinite " + f.getFluid().getAttributes().getDisplayName(f).getString()), true);
								}else {
									player.displayClientMessage(new TextComponent(FORMAT.format(f.getAmount()) + "/" + FORMAT.format(handler.getTankCapacity(0)) + " mB "
											+ f.getFluid().getAttributes().getDisplayName(f).getString()), true);
								}
								
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

	public static class TEFluidTank extends BasicTileEntity implements TOPProvider {

		public FluidStack fluid = FluidStack.EMPTY;
		public byte upgraded = 0;

		private final IFluidHandler fluids;
		private final LazyOptional<IFluidHandler> handler;
		
		public final BlockFluidTank block;

		public TEFluidTank(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
			block = (BlockFluidTank) state.getBlock();
			fluids = new TankFluidHandler();
			handler = LazyOptional.of(() -> fluids);
		}

		public TEFluidTank(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("fluid_tank"), pos, state);
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);

			fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluidstack"));
			upgraded = compound.getByte("assemblylinemachines:upgraded");
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {

			if (fluid != null) {
				CompoundTag sub = new CompoundTag();
				fluid.writeToNBT(sub);
				compound.put("assemblylinemachines:fluidstack", sub);
			}
			if(upgraded != 0) compound.putByte("assemblylinemachines:upgraded", upgraded);
			
			super.saveAdditional(compound);
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

		public boolean isEmpty() {
			return fluid.isEmpty();
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state,
				IProbeHitData data) {
			ItemStack stack = !this.fluid.isEmpty() && this.fluid.getFluid().getBucket() != null ? this.fluid.getFluid().getBucket().getDefaultInstance() : Items.BUCKET.getDefaultInstance();
			Component text = this.fluid.isEmpty() ? new TextComponent("Empty").withStyle(ChatFormatting.AQUA) : new TextComponent(this.fluid.getDisplayName().getString()).withStyle(ChatFormatting.AQUA);
			probeInfo.horizontal().item(stack).vertical().text(text).progress(this.fluid.getAmount(), this.block.capacity, probeInfo.defaultProgressStyle().filledColor(this.block.topProgressColor).alternateFilledColor(this.block.topProgressColor).suffix("mB").numberFormat(NumberFormat.COMMAS));
		}
		
		public class TankFluidHandler implements IFluidHandler{

			@Override
			public int getTanks() {
				return 1;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				return fluid;
			}

			@Override
			public int getTankCapacity(int tank) {
				return fluid.getAmount() == Integer.MAX_VALUE ? Integer.MAX_VALUE : block.capacity;
			}

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				if(getFluidInTank(0).getAmount() == Integer.MAX_VALUE) return false;
				if(!getFluidInTank(0).isEmpty() && !stack.getFluid().equals(getFluidInTank(0).getFluid())) return false;
				return stack.getFluid().getAttributes().getTemperature() < 800 || block.temperatureResistance != TemperatureResistance.COLD;
			}

			@Override
			public int fill(FluidStack resource, FluidAction action) {
				int toFill = !isFluidValid(0, resource) ? 0 : resource.getAmount() + getFluidInTank(0).getAmount() >= getTankCapacity(0) ? getTankCapacity(0) - getFluidInTank(0).getAmount() : resource.getAmount();
				if(toFill != 0 && action == FluidAction.EXECUTE) {
					if(getFluidInTank(0).isEmpty()) {
						fluid = new FluidStack(resource, toFill);
					}else {
						getFluidInTank(0).grow(toFill);
					}
					if((upgraded == 1 && fluid.getFluid().equals(Fluids.WATER)) || upgraded == 2) {
						fluid.setAmount(Integer.MAX_VALUE);
					}
					sendUpdates();
				}
				return toFill;
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				if(getFluidInTank(0).isEmpty() || !resource.getFluid().equals(getFluidInTank(0).getFluid())) return FluidStack.EMPTY;
				if(resource.getAmount() >= getFluidInTank(0).getAmount()) resource.setAmount(getFluidInTank(0).getAmount());
				FluidStack returnStack = new FluidStack(getFluidInTank(0).getFluid(), resource.getAmount());
				if(action == FluidAction.EXECUTE && getFluidInTank(0).getAmount() != Integer.MAX_VALUE) {
					getFluidInTank(0).shrink(returnStack.getAmount());
					sendUpdates();
				}
				return returnStack;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				return getFluidInTank(0).isEmpty() ? FluidStack.EMPTY : drain(new FluidStack(getFluidInTank(0).getFluid(), maxDrain), action);
			}
			
		}
	}

}
