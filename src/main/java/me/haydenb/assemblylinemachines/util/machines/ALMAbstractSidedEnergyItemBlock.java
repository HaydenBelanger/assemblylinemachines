package me.haydenb.assemblylinemachines.util.machines;

import java.util.HashMap;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class ALMAbstractSidedEnergyItemBlock<A extends Container> extends ALMMachineEnergyBased<A> {
	
	
	protected HashMap<Direction, LazyOptional<SidedCheckingHandler>> lazies = new HashMap<>();
	
	public ALMAbstractSidedEnergyItemBlock(TileEntityType<?> tileEntityTypeIn, int slotCount, String name,
			int containerId, Class<A> clazz, EnergyProperties properties) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, properties);
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
	public void remove() {
		for(LazyOptional<SidedCheckingHandler> handler : lazies.values()) {
			handler.invalidate();
		}
		
		lazies.clear();
	}
	
	protected static class SidedCheckingHandler extends InvWrapper implements IEnergyStorage{
		
		private final ALMAbstractSidedEnergyItemBlock<?> sided;
		private Direction side = null;
		SidedCheckingHandler(ALMAbstractSidedEnergyItemBlock<?> v){
			super(v);
			sided = v;
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(sided.canExtractFromSide(slot, side)) {
				return super.extractItem(slot, amount, simulate);
			}else {
				return null;
			}
			
		}
		
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(sided.canInsertToSide(slot, side)) {
				return super.insertItem(slot, stack, simulate);
			}else {
				return null;
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
			
			if(sided.properties.capacity < maxReceive + sided.amount) {
				maxReceive = sided.properties.capacity - sided.amount;
			}
			
			if(simulate == false) {
				sided.amount += maxReceive;
				sided.sendUpdates();
			}
			
			return maxReceive;
		}
		
		@Override
		public int getMaxEnergyStored() {
			return sided.properties.capacity;
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
				sided.sendUpdates();
			}
			
			return maxExtract;
		}
		
		@Override
		public boolean canReceive() {
			if(sided.canExtractFromSide(0, side)) {
				return sided.properties.in;
			}
			return false;
		}
		
		@Override
		public boolean canExtract() {
			if(sided.canInsertToSide(-1, side)) {
				return sided.properties.out;
			}
			return false;
		}
	}
}
