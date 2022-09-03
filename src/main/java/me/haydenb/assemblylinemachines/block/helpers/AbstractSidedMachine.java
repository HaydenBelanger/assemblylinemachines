package me.haydenb.assemblylinemachines.block.helpers;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.TEBatteryCell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class AbstractSidedMachine<A extends AbstractContainerMenu> extends EnergyMachine<A> {


	protected HashMap<Direction, LazyOptional<SidedEnergyHandler>> energyLazies = new HashMap<>();
	protected HashMap<Direction, LazyOptional<SidedItemHandler>> itemLazies = new HashMap<>();

	public AbstractSidedMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, MutableComponent name,
			int containerId, Class<A> clazz, EnergyProperties properties, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, properties, pos, state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		return LazyOptional.empty();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap == ForgeCapabilities.ITEM_HANDLER || cap == ForgeCapabilities.ENERGY) {

			if(cap == ForgeCapabilities.ITEM_HANDLER) {
				LazyOptional<SidedItemHandler> sih = itemLazies.getOrDefault(side, LazyOptional.empty());
				if(sih.orElse(null) != null) {
					if(sih.orElse(null).side == side) {
						return sih.cast();
					}
					sih.invalidate();
					itemLazies.remove(side);
				}

				LazyOptional<SidedItemHandler> handler = LazyOptional.of(() -> {
					SidedItemHandler sihx = new SidedItemHandler();
					sihx.side = side;
					return sihx;
				});
				itemLazies.put(side, handler);
				return handler.cast();
			}else {
				LazyOptional<SidedEnergyHandler> seh = energyLazies.getOrDefault(side, LazyOptional.empty());
				if(seh.orElse(null) != null) {
					if(seh.orElse(null).side == side) {
						return seh.cast();
					}
					seh.invalidate();
					energyLazies.remove(side);
				}

				LazyOptional<SidedEnergyHandler> handler = LazyOptional.of(() -> {
					SidedEnergyHandler sehx = new SidedEnergyHandler();
					sehx.side = side;
					return sehx;
				});
				energyLazies.put(side, handler);
				return handler.cast();
			}
		}
		return LazyOptional.empty();
	}

	public abstract boolean canExtractFromSide(boolean isEnergy, int slot, Direction direction);

	public abstract boolean canInsertToSide(boolean isEnergy, int slot, Direction direction);

	@Override
	public void setRemoved() {
		for(LazyOptional<?> handler : Stream.concat(energyLazies.values().stream(), itemLazies.values().stream()).collect(Collectors.toSet())) {
			handler.invalidate();
		}

		energyLazies.clear();
		itemLazies.clear();
		super.setRemoved();
	}

	protected class SidedItemHandler extends InvWrapper{

		private Direction side = null;

		public SidedItemHandler() {
			super(AbstractSidedMachine.this);
		}



		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(canExtractFromSide(false, slot, side)) {
				return super.extractItem(slot, amount, simulate);
			}else {
				return ItemStack.EMPTY;
			}

		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(canInsertToSide(false, slot, side)) {

				return super.insertItem(slot, stack, simulate);
			}else {
				return stack;
			}
		}

	}

	protected class SidedEnergyHandler implements IEnergyStorage{

		private Lazy<Optional<TEBatteryCell>> battery = Lazy.of(() -> AbstractSidedMachine.this instanceof TEBatteryCell battery ? Optional.of(battery) : Optional.empty());
		private Direction side = null;

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {

			if(!canReceive()) {
				return 0;
			}

			if(properties.getCapacity() < maxReceive + amount) {
				maxReceive = properties.getCapacity() - amount;
			}

			if(!simulate) {
				amount += maxReceive;
				battery.get().ifPresent((b) -> b.recalcBattery());
				sendUpdates();
			}

			return maxReceive;
		}

		@Override
		public int getMaxEnergyStored() {
			return properties.getCapacity();
		}

		@Override
		public int getEnergyStored() {
			return amount;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {

			if(!canExtract()) {
				return 0;
			}

			if(battery.get().isPresent() && battery.get().get().creative) {
				if(amount != properties.getCapacity()) {
					amount = properties.getCapacity();
					sendUpdates();
				}
				return maxExtract;
			}

			if(maxExtract > amount) {
				maxExtract = amount;
			}

			if(!simulate) {
				amount -= maxExtract;
				battery.get().ifPresent((b) -> b.recalcBattery());
				sendUpdates();
			}

			return maxExtract;
		}

		@Override
		public boolean canReceive() {
			if(canInsertToSide(true, -1, side)) {
				return properties.getIn();
			}
			return false;
		}

		@Override
		public boolean canExtract() {
			if(canExtractFromSide(true, 0, side)) {
				return properties.getOut();
			}
			return false;
		}
	}
}
