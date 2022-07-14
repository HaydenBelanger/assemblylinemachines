package me.haydenb.assemblylinemachines.block.helpers;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.RandomStringUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.client.GUIHelper;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.TrueFalseButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractMachine<A extends AbstractContainerMenu> extends RandomizableContainerBlockEntity {

	protected NonNullList<ItemStack> contents;
	protected int playersUsing;
	protected int slotCount;
	protected TranslatableComponent name;
	protected int containerId;
	protected Class<A> clazz;
	private String secureLock;
	private UUID secureLockMaker;

	/**
	 * Simplifies creation of a machine GUI down into very basic implementation
	 * code. Remember that A.class in last parameter must be an instance of
	 * Container and MUST have constructor of A(int windowId, Inventory pInv,
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
	public AbstractMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, TranslatableComponent name, int containerId, Class<A> clazz, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		this.containerId = containerId;
		this.name = name;
		this.slotCount = slotCount;
		this.clazz = clazz;
		this.contents = NonNullList.withSize(slotCount, ItemStack.EMPTY);
	}

	//Synchronizes data on block update between client and server.
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getTag());
	}
	
	//Synchronizes data on world load between client and server.
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = this.save(super.getUpdateTag());
		this.saveAdditional(tag);
		return tag;
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}
		

	public void sendUpdates() {
		this.getLevel().setBlocksDirty(this.getBlockPos(), getBlockState(), getBlockState());
		this.getLevel().sendBlockUpdated(this.getBlockPos(), getBlockState(), getBlockState(), 2);
		this.setChanged();
	}

	@Override
	public int getContainerSize() {
		return slotCount;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return contents;
	}

	public abstract boolean isAllowedInSlot(int slot, ItemStack stack);

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return isAllowedInSlot(index, stack);
	}

	@Override
	public boolean canOpen(Player player) {
		if (secureLock == null) {
			return super.canOpen(player);
		} else {

			ItemStack is = player.getMainHandItem();
			if (is.getItem().equals(Registry.getItem("key")) && is.hasTag()) {
				CompoundTag nbt = is.getTag();
				if (nbt.getString("assemblylinemachines:lockcode").equals(secureLock)) {
					return true;
				}
			}
			player.displayClientMessage(new TranslatableComponent("container.isLocked", getDefaultName()), true);
			player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
			return false;
		}
	}

	public String setRandomLock(Player player) {
		if (secureLock == null) {
			secureLock = RandomStringUtils.random(128, true, true);
			secureLockMaker = player.getUUID();
			sendUpdates();
			return secureLock;
		}

		return null;

	}

	public boolean isRandomLocked() {
		return secureLock != null;
	}

	public boolean removeRandomLock(Player player) {
		if (player.getUUID().equals(secureLockMaker)) {
			secureLock = null;
			secureLockMaker = null;
			sendUpdates();
			return true;
		}
		return false;
	}

	public String getRandomLock(Player player) {
		if (player.getUUID().equals(secureLockMaker)) {
			return secureLock;
		}
		return null;
	}

	@Override
	public void setItems(NonNullList<ItemStack> arg0) {
		this.contents = arg0;

	}

	@Override
	public BaseComponent getDefaultName() {
		return name;
	}

	
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory player) {
		try {
			return (AbstractContainerMenu) clazz.getConstructor(int.class, Inventory.class, this.getClass()).newInstance(id, player, this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public final CompoundTag save(CompoundTag compound) {
		return compound;
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, contents);
		}

		if (secureLock != null && secureLockMaker != null) {
			compound.putUUID("assemblylinemachines:lock:slmakeruuid", secureLockMaker);
			compound.putString("assemblylinemachines:lock:slcode", secureLock);
		}
		
		this.save(compound);
		super.saveAdditional(compound);
	}

	@Override
	public void load(CompoundTag compound) {
		this.contents = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

		if (!this.tryLoadLootTable(compound)) {
			ContainerHelper.loadAllItems(compound, contents);
		}

		if (compound.contains("assemblylinemachines:lock:slmakeruuid")
				&& compound.contains("assemblylinemachines:lock:slcode")) {
			secureLockMaker = compound.getUUID("assemblylinemachines:lock:slmakeruuid");
			secureLock = compound.getString("assemblylinemachines:lock:slcode");
		}
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == containerId) {
			this.playersUsing = type;
			return true;
		}
		return super.triggerEvent(id, type);
	}

	@Override
	public void startOpen(Player player) {
		if (!player.isSpectator()) {
			if (this.playersUsing < 0) {
				playersUsing = 0;
			}

			playersUsing++;
		}

		toggleGUI();
	}

	@Override
	public void stopOpen(Player player) {
		if (!player.isSpectator()) {
			playersUsing--;
			toggleGUI();
		}
	}

	public void toggleGUI() {
		this.getLevel().blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), containerId, playersUsing);
		this.getLevel().updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
	}

	public static class SlotWithRestrictions extends Slot {

		private final AbstractMachine<?> checkMachine;
		private final IMachineDataBridge checkDataBridge;
		protected final int slot;
		private final boolean outputSlot;

		public SlotWithRestrictions(Container inventoryIn, int index, int xPosition, int yPosition, AbstractMachine<?> check, boolean outputSlot) {
			super(inventoryIn, index, xPosition, yPosition);
			this.slot = index;
			this.checkMachine = check;
			this.checkDataBridge = null;
			this.outputSlot = outputSlot;
		}
		
		public SlotWithRestrictions(Container inventoryIn, int index, int xPosition, int yPosition, IMachineDataBridge check, boolean outputSlot) {
			super(inventoryIn, index, xPosition, yPosition);
			this.slot = index;
			this.checkDataBridge = check;
			this.checkMachine = null;
			this.outputSlot = outputSlot;
		}

		public SlotWithRestrictions(Container inventoryIn, int index, int xPosition, int yPosition, AbstractMachine<?> check) {
			this(inventoryIn, index, xPosition, yPosition, check, false);
		}
		
		public SlotWithRestrictions(Container inventoryIn, int index, int xPosition, int yPosition, IMachineDataBridge check) {
			this(inventoryIn, index, xPosition, yPosition, check, false);
		}
		
		@Override
		public boolean mayPlace(ItemStack pStack) {
			if (outputSlot) {
				return false;
			}
			return checkMachine != null ? checkMachine.isAllowedInSlot(slot, pStack) : checkDataBridge.isAllowedInSlot(slot, pStack);
		}

	}

	// CONTAINER DYNAMIC
	public static class ContainerALMBase<T extends BlockEntity> extends AbstractContainerMenu {

		protected final ContainerLevelAccess canInteract;
		public final T tileEntity;
		private final int mergeMinOffset;
		private final int mergeMaxOffset;

		protected ContainerALMBase(MenuType<?> type, int id, T te, Inventory pInv, Pair<Integer, Integer> pmain, Pair<Integer, Integer> phot, int mergeMinOffset) {
			super(type, id);
			this.canInteract = ContainerLevelAccess.create(te.getLevel(), te.getBlockPos());
			this.tileEntity = te;
			this.mergeMinOffset = mergeMinOffset;
			this.mergeMaxOffset = 0;
			if (pmain != null) {
				bindInventory(pInv, pmain.getFirst(), pmain.getSecond());
			}

			if (phot != null) {
				bindPlayerHotbar(pInv, phot.getFirst(), phot.getSecond());
			}

		}

		protected ContainerALMBase(MenuType<?> type, int id, T te, Inventory pInv, Pair<Integer, Integer> pmain, Pair<Integer, Integer> phot, int mergeMinOffset,
				int mergeMaxOffset) {
			super(type, id);
			this.canInteract = ContainerLevelAccess.create(te.getLevel(), te.getBlockPos());
			this.tileEntity = te;
			this.mergeMinOffset = mergeMinOffset;
			this.mergeMaxOffset = mergeMaxOffset;
			if (pmain != null) {
				bindInventory(pInv, pmain.getFirst(), pmain.getSecond());
			}
			if (phot != null) {
				bindPlayerHotbar(pInv, phot.getFirst(), phot.getSecond());
			}

		}

		@Override
		public boolean stillValid(Player pPlayer) {
			return AbstractContainerMenu.stillValid(canInteract, pPlayer, tileEntity.getBlockState().getBlock());
		}

		protected void bindInventory(Inventory playerInventory, int mainx, int mainy) {

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					this.addSlot(new Slot(playerInventory, 9 + (row * 9) + col, mainx + (18 * col), mainy + (18 * row)));
				}
			}
		}

		protected void bindPlayerHotbar(Inventory playerInventory, int hotx, int hoty) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col, hotx + (18 * col), hoty));
			}
		}
		
		@Override
		public ItemStack quickMoveStack(Player player, int index) {
			ItemStack itemstack = ItemStack.EMPTY;
			Slot slot = this.slots.get(index);
			if (slot != null && slot.hasItem()) {
				ItemStack itemstack1 = slot.getItem();
				itemstack = itemstack1.copy();
				if (index < 36) {

					if (!this.moveItemStackTo(itemstack1, 36 + mergeMinOffset, this.slots.size() - mergeMaxOffset, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
					return ItemStack.EMPTY;
				}

				if (itemstack1.isEmpty()) {
					slot.set(ItemStack.EMPTY);
				} else {
					slot.setChanged();
				}
			}

			return itemstack;
		}

	}

	// SCREEN DYNAMIC
	@OnlyIn(Dist.CLIENT)
	public static class ScreenALMBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
		protected final ResourceLocation bg;
		protected final Pair<Integer, Integer> titleTextLoc;
		protected final Pair<Integer, Integer> invTextLoc;
		protected boolean renderTitleText;
		protected boolean renderInventoryText;
		protected Supplier<Component> inventoryText;
		protected PoseStack mx;

		public ScreenALMBase(T screenContainer, Inventory inv, Component titleIn, Pair<Integer, Integer> size, Pair<Integer, Integer> titleTextLoc,
				Pair<Integer, Integer> invTextLoc, String guipath, boolean hasCool) {
			super(screenContainer, inv, titleIn);
			this.leftPos = 0;
			this.topPos = 0;
			this.imageWidth = size.getFirst();
			this.imageHeight = size.getSecond();
			this.titleTextLoc = titleTextLoc;
			this.invTextLoc = invTextLoc;
			this.inventoryText = () -> this.playerInventoryTitle;
			this.renderTitleText = true;
			this.renderInventoryText = true;
			
			bg = new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/" + guipath + ".png");
		}

		// render
		@Override
		public void render(PoseStack mx, final int mousex, final int mousey, final float partialTicks) {
			
			
			this.mx = mx;
			super.renderBackground(mx);
			super.render(mx, mousex, mousey, partialTicks);
			super.renderTooltip(mx, mousex, mousey);
			
		}
		
		//Wrapped blit with MX so to use internally stored PoseStack
		protected void blit(int a, int b, int c, int d, int e, int f) {
			super.blit(mx, a, b, c, d, e, f);
		}

		protected void blit(int x, int y, int w, int h, TextureAtlasSprite tas) {
			//super.func_238470_a_(mx, a, b, c, d, e, tas);
			GUIHelper.fillAreaWithIcon(tas, x, y, w, h);
		}
		
		protected void blit(int x, int y, int w, int h, int v, TextureAtlasSprite tas) {
			this.blit(x, y, w, h, tas);
		}

		//WRAPPED renderTooltip = renderComponentTooltip
		protected void renderComponentTooltip(String a, int b, int c) {
			super.renderComponentTooltip(mx, List.of((Component) new TextComponent(a)), b, c);
		}
		public void renderComponentTooltip(List<String> a, int b, int c) {
			super.renderComponentTooltip(mx, a.stream().map((s) -> (Component) new TextComponent(s)).toList(), b, c);
		}

		//Filled without MX to use internally stored PoseStack
		public void drawCenteredString(Font a, String b, int c, int d, int e) {
			super.drawCenteredString(mx, a, new TextComponent(b), c, d, e);
		}

		
		//LINKED - renderLabels = drawGuiContainerForegroundLayer
		@Override
		protected void renderLabels(PoseStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
			this.drawGuiContainerForegroundLayer(p_230451_2_, p_230451_3_);
		}
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			if (renderTitleText == true) {
				this.font.draw(mx, this.getTitle(), titleTextLoc.getFirst(), titleTextLoc.getSecond(), 4210752);
				
			}
			if(renderInventoryText == true) {
				this.font.draw(mx, this.inventoryText.get(), invTextLoc.getFirst(), invTextLoc.getSecond(), 4210752);
			}
			
		}

		//LINKED - renderBg = drawGuiContainerBackgroundLayer
		@Override
		protected void renderBg(PoseStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
			
			this.drawGuiContainerBackgroundLayer(p_230450_2_, p_230450_3_, p_230450_4_);

		}
		
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			//GL11.glClearColor(1f, 1f, 1f, 1f);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.setShaderTexture(0, bg);
			//this.getMinecraft().getTextureManager().bindForSetup(bg);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			this.blit(x, y, 0, 0, this.imageWidth, this.imageHeight);
			
			for(Widget w : this.renderables) {
				if(w instanceof TrueFalseButton) {
					TrueFalseButton tfb = (TrueFalseButton) w;
					if(tfb.getSupplierOutput()) {
						int[] blitdata = tfb.getBlitData();
						this.blit(blitdata[0], blitdata[1], blitdata[2], blitdata[3], blitdata[4], blitdata[5]);
					}
				}
			}
		}

		public PoseStack getPoseStack() {
			return this.mx;
		}
	}

}