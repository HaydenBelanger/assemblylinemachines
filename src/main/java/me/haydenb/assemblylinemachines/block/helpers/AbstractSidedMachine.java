package me.haydenb.assemblylinemachines.block.helpers;

import java.util.HashMap;

import me.haydenb.assemblylinemachines.registry.StateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class AbstractSidedMachine<A extends AbstractContainerMenu> extends EnergyMachine<A> {
	
	
	protected HashMap<Direction, LazyOptional<SidedCheckingHandler>> lazies = new HashMap<>();
	
	public AbstractSidedMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, TranslatableComponent name,
			int containerId, Class<A> clazz, EnergyProperties properties, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, properties, pos, state);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		return LazyOptional.empty();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY) {
			
			
			LazyOptional<SidedCheckingHandler> vx = lazies.get(side);
			if(vx != null) {
				SidedCheckingHandler sch = vx.orElse(null);
				if(sch != null && sch.side == side) {
					return vx.cast();
				}else {
					vx.invalidate();
					lazies.remove(side);
				}
			}
			
			LazyOptional<SidedCheckingHandler> v = LazyOptional.of(() -> new SidedCheckingHandler(this).assignSide(side));
			lazies.put(side, v);
			return v.cast();
		}
		
		return LazyOptional.empty();
	}
	
	public abstract boolean canExtractFromSide(int slot, Direction direction);
	
	public abstract boolean canInsertToSide(int slot, Direction direction);
	
	@Override
	public void setRemoved() {
		for(LazyOptional<SidedCheckingHandler> handler : lazies.values()) {
			handler.invalidate();
		}
		
		lazies.clear();
		super.setRemoved();
	}
	
	protected static class SidedCheckingHandler extends InvWrapper implements IEnergyStorage{
		
		private final AbstractSidedMachine<?> sided;
		private Direction side = null;
		SidedCheckingHandler(AbstractSidedMachine<?> v){
			super(v);
			sided = v;
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(sided.canExtractFromSide(slot, side)) {
				return super.extractItem(slot, amount, simulate);
			}else {
				return ItemStack.EMPTY;
			}
			
		}
		
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(sided.canInsertToSide(slot, side)) {
				
				return super.insertItem(slot, stack, simulate);
			}else {
				return stack;
			}
		}
		
		private SidedCheckingHandler assignSide(Direction dir) {
			side = dir;
			return this;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			
			if(!canReceive()) {
				return 0;
			}
			
			if(sided.properties.getCapacity() < maxReceive + sided.amount) {
				maxReceive = sided.properties.getCapacity() - sided.amount;
			}
			
			if(simulate == false) {
				sided.amount += maxReceive;
				recalcBattery();
				sided.sendUpdates();
			}
			
			return maxReceive;
		}
		
		@Override
		public int getMaxEnergyStored() {
			return sided.properties.getCapacity();
		}
		
		@Override
		public int getEnergyStored() {
			return sided.amount;
		}
		
		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {

			if(!canExtract()) {
				return 0;
			}
			if(maxExtract > sided.amount) {
				maxExtract = sided.amount;
			}
			
			if(simulate == false) {
				sided.amount -= maxExtract;
				recalcBattery();
				sided.sendUpdates();
			}
			
			return maxExtract;
		}
		
		@Override
		public boolean canReceive() {
			if(sided.canInsertToSide(-1, side)) {
				return sided.properties.getIn();
			}
			return false;
		}
		
		@Override
		public boolean canExtract() {
			if(sided.canExtractFromSide(0, side)) {
				return sided.properties.getOut();
			}
			return false;
		}
		
		private void recalcBattery(){
			
			if(sided.getBlockState().hasProperty(StateProperties.BATTERY_PERCENT_STATE)) {
				int fx = (int) Math.floor(((double) sided.amount / (double) sided.properties.getCapacity()) * 4d);
				if(sided.getBlockState().getValue(StateProperties.BATTERY_PERCENT_STATE) != fx) {
					sided.getLevel().setBlockAndUpdate(sided.getBlockPos(), sided.getBlockState().setValue(StateProperties.BATTERY_PERCENT_STATE, fx));
				}
			}
		}
	}
}
