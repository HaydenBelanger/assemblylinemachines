package me.haydenb.assemblylinemachines.block.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class SimpleMachine<A extends AbstractContainerMenu> extends AbstractMachine<A>{


	protected final SimpleInventoryHandlerWrapper items;
	protected final LazyOptional<SimpleInventoryHandlerWrapper> itemHandler;
	public SimpleMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, MutableComponent name, int containerId,
			Class<A> clazz, boolean supp, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, pos, state);
		items = new SimpleInventoryHandlerWrapper(this, supp);
		itemHandler = LazyOptional.of(() -> items);
	}

	public SimpleMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, MutableComponent name, int containerId,
			Class<A> clazz, BlockPos pos, BlockState state) {
		this(tileEntityTypeIn, slotCount, name, containerId, clazz, false, pos, state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		if(cap == ForgeCapabilities.ITEM_HANDLER) {
			return itemHandler.cast();
		}

		return super.getCapability(cap);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(itemHandler != null) {
			itemHandler.invalidate();
		}
	}

	public boolean canBeExtracted(ItemStack stack, int slot) {
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap == ForgeCapabilities.ITEM_HANDLER) {
			return itemHandler.cast();
		}

		return super.getCapability(cap, side);
	}

	private class SimpleInventoryHandlerWrapper extends InvWrapper {


		private final boolean supp;
		SimpleInventoryHandlerWrapper(Container inv, boolean supportsExtract){
			super(inv);
			this.supp = supportsExtract;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {

			if(!supp || slot != 0 || !canBeExtracted(getItem(slot), slot)) {
				return ItemStack.EMPTY;
			}else {
				return super.extractItem(slot, amount, simulate);
			}

		}
	}

}
