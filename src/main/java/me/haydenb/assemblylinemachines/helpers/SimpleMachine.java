package me.haydenb.assemblylinemachines.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class SimpleMachine<A extends Container> extends AbstractMachine<A>{

	
	protected final SimpleInventoryHandlerWrapper items;
	protected final LazyOptional<SimpleInventoryHandlerWrapper> itemHandler;
	public SimpleMachine(TileEntityType<?> tileEntityTypeIn, int slotCount, TranslationTextComponent name, int containerId,
			Class<A> clazz, boolean supp) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz);
		items = new SimpleInventoryHandlerWrapper(this, supp);
		itemHandler = LazyOptional.of(() -> items);
	}
	
	public SimpleMachine(TileEntityType<?> tileEntityTypeIn, int slotCount, TranslationTextComponent name, int containerId,
			Class<A> clazz) {
		this(tileEntityTypeIn, slotCount, name, containerId, clazz, false);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return itemHandler.cast();
		}
		
		return super.getCapability(cap);
	}
	
	@Override
	public void remove() {
		super.remove();
		if(itemHandler != null) {
			itemHandler.invalidate();
		}
	}
	
	
	public boolean canBeExtracted(ItemStack stack) {
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return itemHandler.cast();
		}
		
		return super.getCapability(cap, side);
	}
	
	private class SimpleInventoryHandlerWrapper extends InvWrapper {
		
		
		private final boolean supp;
		SimpleInventoryHandlerWrapper(IInventory inv, boolean supportsExtract){
			super(inv);
			this.supp = supportsExtract;
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			
			if(supp == false || slot != 0 || !canBeExtracted(getStackInSlot(slot))) {
				return ItemStack.EMPTY;
			}else {
				return super.extractItem(slot, amount, simulate);
			}
			
		}
	}

}
