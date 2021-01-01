package me.haydenb.assemblylinemachines.helpers;

import java.util.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleFluidMixer;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.rendering.GUIHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;

public abstract class AbstractMachine<A extends Container> extends LockableLootTileEntity {

	protected NonNullList<ItemStack> contents;
	protected int playersUsing;
	protected int slotCount;
	protected TranslationTextComponent name;
	protected int containerId;
	protected Class<A> clazz;
	private String secureLock;
	private UUID secureLockMaker;

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
	public AbstractMachine(TileEntityType<?> tileEntityTypeIn, int slotCount, TranslationTextComponent name, int containerId, Class<A> clazz) {
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
		handleUpdateTag(world.getBlockState(pos), pkt.getNbtCompound());
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
	public boolean canOpen(PlayerEntity player) {
		if (secureLock == null) {
			return super.canOpen(player);
		} else {

			ItemStack is = player.getHeldItemMainhand();
			if (is.getItem().equals(Registry.getItem("key")) && is.hasTag()) {
				CompoundNBT nbt = is.getTag();
				if (nbt.getString("assemblylinemachines:lockcode").equals(secureLock)) {
					return true;
				}
			}
			player.sendStatusMessage(new TranslationTextComponent("container.isLocked", getDefaultName()), true);
			player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return false;
		}
	}

	public String setRandomLock(PlayerEntity player) {
		if (secureLock == null) {
			secureLock = RandomStringUtils.random(128, true, true);
			secureLockMaker = player.getUniqueID();
			sendUpdates();
			return secureLock;
		}

		return null;

	}

	public boolean isRandomLocked() {
		return secureLock != null;
	}

	public boolean removeRandomLock(PlayerEntity player) {
		if (player.getUniqueID().equals(secureLockMaker)) {
			secureLock = null;
			secureLockMaker = null;
			sendUpdates();
			return true;
		}
		return false;
	}

	public String getRandomLock(PlayerEntity player) {
		if (player.getUniqueID().equals(secureLockMaker)) {
			return secureLock;
		}
		return null;
	}

	@Override
	public void setItems(NonNullList<ItemStack> arg0) {
		this.contents = arg0;

	}

	@Override
	public ITextComponent getDefaultName() {
		return name;
	}

	@Override
	public Container createMenu(int id, PlayerInventory player) {
		try {
			return clazz.getConstructor(int.class, PlayerInventory.class, this.getClass()).newInstance(id, player, this);
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

		if (secureLock != null && secureLockMaker != null) {
			compound.putUniqueId("assemblylinemachines:lock:slmakeruuid", secureLockMaker);
			compound.putString("assemblylinemachines:lock:slcode", secureLock);
		}
		return super.write(compound);
	}

	@Override
	public void func_230337_a_(BlockState state, CompoundNBT compound) {
		super.func_230337_a_(state, compound);
		this.read(compound);
	}

	public void read(CompoundNBT compound) {
		this.contents = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

		if (!this.checkLootAndRead(compound)) {
			ItemStackHelper.loadAllItems(compound, contents);
		}

		if (compound.contains("assemblylinemachines:lock:slmakeruuid")
				&& compound.contains("assemblylinemachines:lock:slcode")) {
			secureLockMaker = compound.getUniqueId("assemblylinemachines:lock:slmakeruuid");
			secureLock = compound.getString("assemblylinemachines:lock:slcode");
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

		private final AbstractMachine<?> check;
		protected final int slot;
		private final boolean outputSlot;

		public SlotWithRestrictions(IInventory inventoryIn, int index, int xPosition, int yPosition, AbstractMachine<?> check, boolean outputSlot) {
			super(inventoryIn, index, xPosition, yPosition);
			this.slot = index;
			this.check = check;
			this.outputSlot = outputSlot;
		}

		public SlotWithRestrictions(IInventory inventoryIn, int index, int xPosition, int yPosition, AbstractMachine<?> check) {
			this(inventoryIn, index, xPosition, yPosition, check, false);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			if (outputSlot) {
				return false;
			}
			return check.isAllowedInSlot(slot, stack);
		}

	}

	// CONTAINER DYNAMIC
	public static class ContainerALMBase<T extends TileEntity> extends Container {

		protected final IWorldPosCallable canInteract;
		public final T tileEntity;
		private final int mergeMinOffset;
		private final int mergeMaxOffset;

		protected ContainerALMBase(ContainerType<?> type, int id, T te, PlayerInventory pInv, Pair<Integer, Integer> pmain, Pair<Integer, Integer> phot, int mergeMinOffset) {
			super(type, id);
			this.canInteract = IWorldPosCallable.of(te.getWorld(), te.getPos());
			this.tileEntity = te;
			this.mergeMinOffset = mergeMinOffset;
			this.mergeMaxOffset = 0;
			if (pmain != null) {
				bindPlayerInventory(pInv, pmain.getFirst(), pmain.getSecond());
			}

			if (phot != null) {
				bindPlayerHotbar(pInv, phot.getFirst(), phot.getSecond());
			}

		}

		protected ContainerALMBase(ContainerType<?> type, int id, T te, PlayerInventory pInv, Pair<Integer, Integer> pmain, Pair<Integer, Integer> phot, int mergeMinOffset,
				int mergeMaxOffset) {
			super(type, id);
			this.canInteract = IWorldPosCallable.of(te.getWorld(), te.getPos());
			this.tileEntity = te;
			this.mergeMinOffset = mergeMinOffset;
			this.mergeMaxOffset = mergeMaxOffset;
			if (pmain != null) {
				bindPlayerInventory(pInv, pmain.getFirst(), pmain.getSecond());
			}
			if (phot != null) {
				bindPlayerHotbar(pInv, phot.getFirst(), phot.getSecond());
			}

		}

		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return isWithinUsableDistance(canInteract, playerIn, tileEntity.getBlockState().getBlock());
		}

		protected void bindPlayerInventory(PlayerInventory playerInventory, int mainx, int mainy) {

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					this.addSlot(new Slot(playerInventory, 9 + (row * 9) + col, mainx + (18 * col), mainy + (18 * row)));
				}
			}
		}

		protected void bindPlayerHotbar(PlayerInventory playerInventory, int hotx, int hoty) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col, hotx + (18 * col), hoty));
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

					if (!this.mergeItemStack(itemstack1, 36 + mergeMinOffset, this.inventorySlots.size() - mergeMaxOffset, false)) {
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
		protected final ResourceLocation bg;
		protected final Pair<Integer, Integer> titleTextLoc;
		protected final Pair<Integer, Integer> invTextLoc;
		protected int width;
		protected int height;
		protected FontRenderer font = null;
		protected boolean renderTitleText;
		protected boolean renderInventoryText;
		protected MatrixStack mx;

		public ScreenALMBase(T screenContainer, PlayerInventory inv, ITextComponent titleIn, Pair<Integer, Integer> size, Pair<Integer, Integer> titleTextLoc,
				Pair<Integer, Integer> invTextLoc, String guipath, boolean hasCool) {
			super(screenContainer, inv, titleIn);
			this.guiLeft = 0;
			this.guiTop = 0;
			this.xSize = size.getFirst();
			this.ySize = size.getSecond();
			this.titleTextLoc = titleTextLoc;
			this.invTextLoc = invTextLoc;
			String a = "";
			if (hasCool == true && ConfigHolder.COMMON.coolDudeMode.get() == true) {
				a = "cool/";
				renderTitleText = false;
				renderInventoryText = false;
			} else {
				renderTitleText = true;
				renderInventoryText = true;
			}
			bg = new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/" + a + guipath + ".png");
		}

		// render
		@Override
		public void func_230430_a_(MatrixStack mx, final int mousex, final int mousey, final float partialTicks) {
			width = this.field_230708_k_;
			height = this.field_230709_l_;
			font = this.field_230712_o_;
			this.mx = mx;
			this.func_230446_a_(mx);
			super.func_230430_a_(mx, mousex, mousey, partialTicks);
			this.func_230459_a_(mx, mousex, mousey);
		}

		// drawGuiContainerBackgroundLayer
		@Override
		protected void func_230450_a_(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {

			this.drawGuiContainerBackgroundLayer(p_230450_2_, p_230450_3_, p_230450_4_);

		}

		// drawGuiContainerForegroundLayer
		@Override
		protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
			
			this.drawGuiContainerForegroundLayer(p_230451_2_, p_230451_3_);
		}

		@Override
		protected void func_231160_c_() {
			super.func_231160_c_();
			this.init();
		}
		
		// BELOW ARE UTILITY METHODS TO WRAP OLD MAPPINGS
		protected void blit(int a, int b, int c, int d, int e, int f) {
			super.func_238474_b_(mx, a, b, c, d, e, f);
		}

		protected void blit(int x, int y, int w, int h, TextureAtlasSprite tas) {
			//super.func_238470_a_(mx, a, b, c, d, e, tas);
			GUIHelper.fillAreaWithIcon(tas, x, y, w, h);
		}
		
		protected void blit(int x, int y, int w, int h, int v, TextureAtlasSprite tas) {
			this.blit(x, y, w, h, tas);
		}

		@Override
		public ITextComponent func_231171_q_() {
			return super.func_231171_q_();
		}

		protected void renderTooltip(String a, int b, int c) {
			List<ITextComponent> list = new ArrayList<>();
			list.add(new StringTextComponent(a));
			super.func_243308_b(mx, list, b, c);
		}

		public void renderTooltip(List<String> a, int b, int c) {
			List<ITextComponent> list = new ArrayList<>();
			for (String s : a) {
				list.add(new StringTextComponent(s));
			}
			super.func_243308_b(mx, list, b, c);

		}

		public void drawCenteredString(FontRenderer a, String b, int c, int d, int e) {
			super.func_238472_a_(mx, a, new StringTextComponent(b), c, d, e);
		}

		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			if (renderTitleText == true) {
				this.field_230712_o_.func_243248_b(mx, this.field_230704_d_, titleTextLoc.getFirst(), titleTextLoc.getSecond(), 4210752);
				
			}
			if(renderInventoryText == true) {
				this.field_230712_o_.func_243248_b(mx, this.playerInventory.getDisplayName(), invTextLoc.getFirst(), invTextLoc.getSecond(), 4210752);
			}
		}
		
		protected void init(){
			
		}

		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			GL11.glColor4f(1f, 1f, 1f, 1f);
			this.field_230706_i_.getTextureManager().bindTexture(bg);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			this.blit(x, y, 0, 0, this.xSize, this.ySize);
		}

		protected <X extends Widget> void addButton(X a) {
			this.func_230480_a_(a);
		}

	}

}
