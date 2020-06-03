package me.haydenb.assemblylinemachines.util;

import com.mojang.blaze3d.systems.RenderSystem;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockSimpleFluidMixer;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.util.Utils.Pair;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class AbstractALMMachine<A extends Container> extends LockableLootTileEntity {

	protected NonNullList<ItemStack> contents;
	protected int playersUsing;
	protected int slotCount;
	protected String name;
	protected int containerId;
	protected Class<A> clazz;

	/**
	 * Simplifies creation of a machine GUI down into very basic implementation
	 * code. Remember that A.class in last parameter must be an instance of
	 * Container and MUST have constructor of A(int windowId, PlayerInventory pInv,
	 * this.class te) or it WILL NOT WORK.
	 * 
	 * @param tileEntityTypeIn The specified Tile Entity Type object as obtained
	 *                         from the Registry.
	 * @param slotCount        The number of slots this inventory will have.
	 * @param name             The friendly name the GUI will show.
	 * @param containerId      The ID of the container you wish to use.
	 * @param clazz            The class to construct on createMenu(). View warning
	 *                         above!
	 */
	public AbstractALMMachine(TileEntityType<?> tileEntityTypeIn, int slotCount, String name, int containerId,
			Class<A> clazz) {
		super(tileEntityTypeIn);
		this.containerId = containerId;
		this.name = name;
		this.slotCount = slotCount;
		this.clazz = clazz;
		this.contents = NonNullList.withSize(slotCount, ItemStack.EMPTY);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}

	public void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, getBlockState(), getBlockState());
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
		markDirty();
	}

	@Override
	public int getSizeInventory() {
		return slotCount;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return contents;
	}

	public abstract boolean isAllowedInSlot(int slot, ItemStack stack);

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return isAllowedInSlot(index, stack);
	}

	@Override
	public void setItems(NonNullList<ItemStack> arg0) {
		this.contents = arg0;

	}

	@Override
	public ITextComponent getDefaultName() {
		return new StringTextComponent(name);
	}

	@Override
	public Container createMenu(int id, PlayerInventory player) {
		try {
			return clazz.getConstructor(int.class, PlayerInventory.class, this.getClass()).newInstance(id, player,
					this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (!this.checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, contents);
		}
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		this.contents = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

		if (!this.checkLootAndRead(compound)) {
			ItemStackHelper.loadAllItems(compound, contents);
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == containerId) {
			this.playersUsing = type;
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	@Override
	public void openInventory(PlayerEntity player) {
		if (!player.isSpectator()) {
			if (this.playersUsing < 0) {
				playersUsing = 0;
			}

			playersUsing++;
		}

		toggleGUI();
	}

	@Override
	public void closeInventory(PlayerEntity player) {
		if (!player.isSpectator()) {
			playersUsing--;
			toggleGUI();
		}
	}

	public void toggleGUI() {
		if (this.getBlockState().getBlock() instanceof BlockSimpleFluidMixer) {
			world.addBlockEvent(pos, this.getBlockState().getBlock(), containerId, playersUsing);
			world.notifyNeighborsOfStateChange(pos, this.getBlockState().getBlock());
		}
	}

	public static class SlotWithRestrictions extends Slot {

		private final AbstractALMMachine<?> check;
		protected final int slot;
		private final int maxStackLimit;

		public SlotWithRestrictions(IInventory inventoryIn, int index, int xPosition, int yPosition,
				AbstractALMMachine<?> check, int slotLimit) {
			super(inventoryIn, index, xPosition, yPosition);
			this.slot = index;
			this.check = check;
			this.maxStackLimit = slotLimit;
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return check.isAllowedInSlot(slot, stack);
		}
		
		@Override
			public int getSlotStackLimit() {
				return maxStackLimit;
			}

	}

	// CONTAINER DYNAMIC
	public static class ContainerALMBase<T extends TileEntity> extends Container {

		protected final int invenSize;
		protected final IWorldPosCallable canInteract;
		public final T tileEntity;

		protected ContainerALMBase(ContainerType<?> type, int id, int invenSize, T te, PlayerInventory pInv,
				Pair<Integer, Integer> pmain, Pair<Integer, Integer> phot) {
			super(type, id);
			this.invenSize = invenSize;
			this.canInteract = IWorldPosCallable.of(te.getWorld(), te.getPos());
			this.tileEntity = te;
			bindPlayerInventory(pInv, pmain.x, pmain.y, phot.y, phot.x);

		}

		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return isWithinUsableDistance(canInteract, playerIn, tileEntity.getBlockState().getBlock());
		}

		protected void bindPlayerInventory(PlayerInventory playerInventory, int mainx, int mainy, int hotx, int hoty) {

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					this.addSlot(
							new Slot(playerInventory, 9 + (row * 9) + col, mainx + (18 * col), mainy + (18 * row)));
				}
			}

			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col, hoty + (18 * col), hotx));
			}

		}

		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			ItemStack itemstack = ItemStack.EMPTY;
			Slot slot = this.inventorySlots.get(index);
			if (slot != null && slot.getHasStack()) {
				ItemStack itemstack1 = slot.getStack();
				itemstack = itemstack1.copy();
				if (index < 36) {
					if (!this.mergeItemStack(itemstack1, 36, this.inventorySlots.size(), false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
					return ItemStack.EMPTY;
				}

				if (itemstack1.isEmpty()) {
					slot.putStack(ItemStack.EMPTY);
				} else {
					slot.onSlotChanged();
				}
			}

			return itemstack;
		}

	}

	// SCREEN DYNAMIC
	public static class ScreenALMBase<T extends Container> extends ContainerScreen<T> {
		private final ResourceLocation bg;
		private final Pair<Integer, Integer> titleTextLoc;
		private final Pair<Integer, Integer> invTextLoc;
		public boolean renderTitles;

		public ScreenALMBase(T screenContainer, PlayerInventory inv, ITextComponent titleIn,
				Pair<Integer, Integer> size, Pair<Integer, Integer> titleTextLoc, Pair<Integer, Integer> invTextLoc,
				String guipath, boolean hasCool) {
			super(screenContainer, inv, titleIn);
			this.guiLeft = 0;
			this.guiTop = 0;
			this.xSize = size.x;
			this.ySize = size.y;
			this.titleTextLoc = titleTextLoc;
			this.invTextLoc = invTextLoc;
			String a = "";
			if (hasCool == true && ConfigHolder.COMMON.coolDudeMode.get() == true) {
				a = "cool/";
				renderTitles = false;
			} else {
				renderTitles = true;
			}
			bg = new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/" + a + guipath + ".png");
		}

		@Override
		public void render(final int mousex, final int mousey, final float partialTicks) {
			this.renderBackground();
			super.render(mousex, mousey, partialTicks);
			this.renderHoveredToolTip(mousex, mousey);
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			if (renderTitles == true) {
				this.font.drawString(this.title.getFormattedText(), titleTextLoc.x, titleTextLoc.y, 4210752);
				this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), invTextLoc.x,
						invTextLoc.y, 4210752);
			}
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

			RenderSystem.color4f(1f, 1f, 1f, 1f);
			this.minecraft.getTextureManager().bindTexture(bg);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			this.blit(x, y, 0, 0, this.xSize, this.ySize);

		}
	}

}
