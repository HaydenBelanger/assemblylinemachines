package me.haydenb.assemblylinemachines.block.machines;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.block.machines.BlockOmnivoid.TEOmnivoid;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockOmnivoid extends BlockScreenBlockEntity<TEOmnivoid> {

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 11, 16),
			Block.box(1, 11, 1, 15, 14, 15),
			Block.box(6, 14, 7, 7, 16, 9),
			Block.box(9, 14, 7, 10, 16, 9),
			Block.box(7, 15, 7, 9, 16, 9)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	
	public BlockOmnivoid() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL).lightLevel((state) -> 10), "omnivoid", SHAPE_N, true, Direction.NORTH, TEOmnivoid.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_49915_) {
		p_49915_.add(HorizontalDirectionalBlock.FACING);
	}
	
	public static class TEOmnivoid extends SimpleMachine<ContainerOmnivoid> implements ALMTicker<TEOmnivoid>{
		
		//ITEM, FLUID, ENERGY
		private int[] settings = new int[] {1, 1, 1};
		private LazyOptional<VoidingHandler> handler = LazyOptional.of(() -> new VoidingHandler());
	
		public TEOmnivoid(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
			super(tileEntityType, 1, new TranslatableComponent(Registry.getBlock("omnivoid").getDescriptionId()), Registry.getContainerId("omnivoid"), ContainerOmnivoid.class, pos, state);
		}
		
		public TEOmnivoid(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("omnivoid"), pos, state);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:settings")) settings = compound.getIntArray("assemblylinemachines:settings");
		}
		
		@Override
		protected void saveAdditional(CompoundTag compound) {
			
			compound.putIntArray("assemblylinemachines:settings", settings);
			super.saveAdditional(compound);
		}

		@Override
		public void tick() {
			
			if(!level.isClientSide) {
				if(!this.getItem(0).isEmpty()) {
					this.setItem(0, ItemStack.EMPTY);
					this.sendUpdates();
				}
			}
			
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return settings[0] == 1;
		}
		
		@Override
		public void setRemoved() {
			super.setRemoved();
			if(handler != null) handler.invalidate();
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY) {
				return handler.cast();
			}
			return super.getCapability(cap);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY) {
				return handler.cast();
			}
			return super.getCapability(cap, side);
		}
		
		public void toggleSettings(int settingNum) {
			settings[settingNum] = settings[settingNum] == 0 ? 1 : 0;
		}
		
		public class VoidingHandler implements IItemHandler, IFluidHandler, IEnergyStorage{

			//ENERGY
			
			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				return settings[2] == 1 ? maxReceive : 0;
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				return 0;
			}

			@Override
			public int getEnergyStored() {
				return 0;
			}

			@Override
			public int getMaxEnergyStored() {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean canExtract() {
				return false;
			}

			@Override
			public boolean canReceive() {
				return true;
			}

			//FLUID
			
			@Override
			public int fill(FluidStack resource, FluidAction action) {
				return settings[1] == 1 ? resource.getAmount() : 0;
			}
			
			@Override
			public int getTanks() {
				return 1;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				return FluidStack.EMPTY;
			}

			@Override
			public int getTankCapacity(int tank) {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				return true;
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return FluidStack.EMPTY;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				return FluidStack.EMPTY;
			}

			//ITEM
			
			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				return settings[0] == 1 ? ItemStack.EMPTY : stack;
			}
			
			@Override
			public int getSlots() {
				return 1;
			}

			@Override
			public ItemStack getStackInSlot(int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return true;
			}
			
		}
	}
	
	public static class ContainerOmnivoid extends ContainerALMBase<TEOmnivoid>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerOmnivoid(int windowId, Inventory playerInventory, TEOmnivoid tileEntity) {
			super(Registry.getContainerType("omnivoid"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);
			
			this.addSlot(new Slot(this.tileEntity, 0, 80, 44));
		}
		
		public ContainerOmnivoid(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEOmnivoid.class));
		}
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenOmnivoid extends ScreenALMBase<ContainerOmnivoid>{
		
		TEOmnivoid tsfm;
		
		public ScreenOmnivoid(ContainerOmnivoid container, Inventory inv, Component title) {
			super(container, inv, title, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "omnivoid", false);
			this.tsfm = container.tileEntity;
		}
		
		@Override
		protected void init() {
			super.init();
			
			this.addRenderableWidget(new TrueFalseButton(leftPos+53, topPos+20, 176, 36, 18, 18, new TrueFalseButtonSupplier("Items Enabled", "Items Disabled", () -> tsfm.settings[0] == 1), (b) -> sendButtonToggle(tsfm.getBlockPos(), 0)));
			this.addRenderableWidget(new TrueFalseButton(leftPos+79, topPos+20, 176, 18, 18, 18, new TrueFalseButtonSupplier("Fluids Enabled", "Fluids Disabled", () -> tsfm.settings[1] == 1), (b) -> sendButtonToggle(tsfm.getBlockPos(), 1)));
			this.addRenderableWidget(new TrueFalseButton(leftPos+105, topPos+20, 176, 0, 18, 18, new TrueFalseButtonSupplier("Energy Enabled", "Energy Disabled", () -> tsfm.settings[2] == 1), (b) -> sendButtonToggle(tsfm.getBlockPos(), 2)));
		}
		
		private static void sendButtonToggle(BlockPos pos, int settingNum) {
			PacketData pd = new PacketData("omnivoid_gui");
			pd.writeBlockPos("location", pos);
			pd.writeInteger("settingtoggle", settingNum);
			PacketHandler.INSTANCE.sendToServer(pd);
		}
	}
}
