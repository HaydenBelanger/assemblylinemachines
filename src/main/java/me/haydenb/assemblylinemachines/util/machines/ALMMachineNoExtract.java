package me.haydenb.assemblylinemachines.util.machines;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class ALMMachineNoExtract<A extends Container> extends AbstractALMMachine<A>{

	
	protected NoExtractItemHandler items = new NoExtractItemHandler(this);
	protected LazyOptional<NoExtractItemHandler> itemHandler = LazyOptional.of(() -> items);
	public ALMMachineNoExtract(TileEntityType<?> tileEntityTypeIn, int slotCount, String name, int containerId,
			Class<A> clazz) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return itemHandler.cast();
		}
		
		return LazyOptional.empty();
	}
	
	@Override
	public void remove() {
		super.remove();
		if(itemHandler != null) {
			itemHandler.invalidate();
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.getCapability(cap);
	}
	
	private static class NoExtractItemHandler extends InvWrapper {
		
		
		NoExtractItemHandler(IInventory inv){
			super(inv);
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}
	}

}
