package me.haydenb.assemblylinemachines.block.pipe;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type.MainType;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemPipeConnectorTileEntity extends SimpleMachine<ItemPipeConnectorContainer>
		implements ALMTicker<ItemPipeConnectorTileEntity> {

	private boolean inputMode = true;
	private boolean outputMode = false;
	private boolean nearestFirst = true;
	private boolean whitelist = false;
	private boolean redstone = false;
	private boolean isRedstonePowered = false;
	private int priority = 0;

	private int timer = 0;
	private int nTimer = 50;
	private double pendingCooldown = 0;

	private IItemHandler output = null;

	private final TreeSet<ItemPipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<ItemPipeConnectorTileEntity>() {

				@Override
				public int compare(ItemPipeConnectorTileEntity o1, ItemPipeConnectorTileEntity o2) {
					if (o1.getPriority() > o2.getPriority()) {
						return 1;
					} else if (o1.getPriority() < o2.getPriority()) {
						return -1;
					} else {
						if (nearestFirst) {

							if (getBlockPos().distSqr(o1.getBlockPos()) > getBlockPos().distSqr(o2.getBlockPos())) {
								return -1;
							} else {
								return 1;
							}
						} else {
							if (getBlockPos().distSqr(o2.getBlockPos()) > getBlockPos().distSqr(o1.getBlockPos())) {
								return -1;
							} else {
								return 1;
							}
						}
					}
				}

			});


	public ItemPipeConnectorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, 12, new TranslatableComponent(Registry.getBlock("item_pipe").getDescriptionId()), Registry.getContainerId("pipe_connector_item"),
				ItemPipeConnectorContainer.class, pos, state);
	}

	public ItemPipeConnectorTileEntity(BlockPos pos, BlockState state) {
		this(Registry.getBlockEntity("pipe_connector_item"), pos, state);
	}

	public boolean isRedstoneActive() {
		return redstone;
	}

	public void setRedstoneActive(boolean b) {
		isRedstonePowered = b;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isAllowedInSlot(int slot, ItemStack stack) {
		if (slot >= 9 && slot <= 12) {

			if (stack.getItem() instanceof ItemUpgrade) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		if (compound.contains("assemblylinemachines:input")) {
			inputMode = compound.getBoolean("assemblylinemachines:input");
		}
		if (compound.contains("assemblylinemachines:output")) {
			outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		if (compound.contains("assemblylinemachines:nearest")) {
			nearestFirst = compound.getBoolean("assemblylinemachines:nearest");
		}
		if (compound.contains("assemblylinemachines:whitelist")) {
			whitelist = compound.getBoolean("assemblylinemachines:whitelist");
		}
		if (compound.contains("assemblylinemachines:priority")) {
			priority = compound.getInt("assemblylinemachines:priority");
		}
		if (compound.contains("assemblylinemachines:redstone")) {
			redstone = compound.getBoolean("assemblylinemachines:redstone");
		}
		if (compound.contains("assemblylinemachines:redstoneispowered")) {
			isRedstonePowered = compound.getBoolean("assemblylinemachines:redstoneispowered");
		}
		if (compound.contains("assemblylinemachines:pendingcooldown")) {
			pendingCooldown = compound.getDouble("assemblylinemachines:cooldown");
		}
	}

	@Override
	public CompoundTag save(CompoundTag compound) {

		compound.putBoolean("assemblylinemachines:input", inputMode);
		compound.putBoolean("assemblylinemachines:output", outputMode);
		compound.putBoolean("assemblylinemachines:nearest", nearestFirst);
		compound.putBoolean("assemblylinemachines:whitelist", whitelist);
		compound.putBoolean("assemblylinemachines:redstone", redstone);
		compound.putBoolean("assemblylinemachines:redstoneispowered", isRedstonePowered);
		compound.putInt("assemblylinemachines:priority", priority);
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		return super.save(compound);
	}

	@Override
	public void tick() {

		if (!level.isClientSide) {

			if (outputMode == true) {
				if (timer++ == nTimer) {
					timer = 0;
					if (pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						switch (getUpgradeAmount(Upgrades.UNIVERSAL_SPEED)) {
						case 3:
							nTimer = 10;
							break;
						case 2:
							nTimer = 20;
							break;
						case 1:
							nTimer = 35;
							break;
						default:
							nTimer = 50;
						}

						targets.clear();
						pathToNearestItem(this.getLevel(), this.getBlockPos(), new ArrayList<>(), this.getBlockPos(), targets);

						if (output == null && connectToOutput() == false) {
							return;
						}
						if (output != null) {
							if (redstone == true) {
								if (getUpgradeAmount(Upgrades.PIPE_REDSTONE) == 0) {
									redstone = false;
								} else if (isRedstonePowered == false) {
									return;
								}

							}

							int max = 0;
							switch (getUpgradeAmount(Upgrades.PIPE_STACK)) {
							case 3:
								max = 64;
								break;
							case 2:
								max = 32;
								break;
							case 1:
								max = 16;
								break;
							default:
								max = 8;
							}
							for (int i = 0; i < output.getSlots(); i++) {
								ItemStack origStack = output.extractItem(i, max, true);
								if (origStack != ItemStack.EMPTY && checkWhiteBlackList(origStack)) {
									ItemStack copyStack = origStack.copy();
									if (copyStack.getCount() > max) {
										copyStack.setCount(max);
									}
									int origSize = copyStack.getCount();
									int extracted = 0;

									double waitTime = 0;
									for (ItemPipeConnectorTileEntity ipc : targets.descendingSet()) {
										if (ipc != null) {
											extracted = +ipc.attemptAcceptItem(copyStack);

											double thisdist = this.getBlockPos().distSqr(ipc.getBlockPos());
											if (thisdist > waitTime) {
												waitTime = thisdist;
											}
											if (extracted == origSize || extracted >= max) {
												break;
											}
											
										}
									}

									if (extracted != 0) {
										pendingCooldown = (waitTime * extracted) / 140;
										output.extractItem(i, extracted, false);
										break;
									}

								}

							}

							sendUpdates();
						}

					}
				}
			}
		}
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		for (int i = 0; i < 9; i++) {
			if (contents.get(i) != ItemStack.EMPTY && this.enableFilterSlot(i) == false) {
				contents.set(i, ItemStack.EMPTY);
			}
		}

		return contents;
	}
	
	public void pathToNearestItem(Level world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, TreeSet<ItemPipeConnectorTileEntity> targets) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.getValue(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				BlockEntity te = world.getBlockEntity(curPos);
				if(te != null && te instanceof ItemPipeConnectorTileEntity) {
					ItemPipeConnectorTileEntity ipc = (ItemPipeConnectorTileEntity) te;
					targets.add(ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.relative(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof PipeBase) {
						PipeBase<?> t = (PipeBase<?>) world.getBlockState(targPos).getBlock();
						if (t.type.getMainType() == MainType.ITEM) {
							pathToNearestItem(world, targPos, checked, initial, targets);
						}

					}
				}

			}
		}
	}

	public int getUpgradeAmount(Upgrades upgrade) {
		int ii = 0;
		for (int i = 9; i < 12; i++) {
			if (Upgrades.match(contents.get(i)) == upgrade) {
				ii++;
			}
		}

		return ii;
	}
	
	@Override
	public ItemStack removeItem(int pIndex, int pCount) {
		if(pIndex < 9) {
			return ItemStack.EMPTY;
		}
		return super.removeItem(pIndex, pCount);
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int pIndex) {
		if(pIndex < 9) {
			return ItemStack.EMPTY;
		}
		return super.removeItemNoUpdate(pIndex);
	}

	public int attemptAcceptItem(ItemStack stack) {
		if (inputMode == false) {
			return 0;
		}

		if (output == null && connectToOutput() == false) {
			return 0;
		}

		if (checkWhiteBlackList(stack) == false) {
			return 0;
		}

		int added = 0;
		for (int i = 0; i < output.getSlots(); i++) {
			int acc = stack.getCount() - output.insertItem(i, stack, false).getCount();

			if (acc != 0) {
				added += acc;
				break;
			}
		}

		return added;

	}
	
	

	private boolean checkWhiteBlackList(ItemStack stack) {
		for (int i = 0; i < 9; i++) {
			if (whitelist) {
				if (stack.getItem() == contents.get(i).getItem()) {
					return true;
				}
			} else {
				if (stack.getItem() == contents.get(i).getItem()) {
					return false;
				}
			}
		}
		if (!whitelist) {
			return true;
		}
		return false;
	}

	private boolean connectToOutput() {

		for (Direction d : Direction.values()) {
			if (getBlockState().getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(d));
				if (te != null) {
					LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
							d.getOpposite());
					IItemHandler output = cap.orElse(null);
					if (output != null) {
						ItemPipeConnectorTileEntity ipcte = this;
						cap.addListener(new NonNullConsumer<LazyOptional<IItemHandler>>() {

							@Override
							public void accept(LazyOptional<IItemHandler> t) {
								if (ipcte != null) {
									ipcte.output = null;
								}
							}
						});

						this.output = output;
						return true;
					}
				}

			}
		}

		return false;
	}

	public static class ItemPipeConnectorContainer extends AbstractMachine.ContainerALMBase<ItemPipeConnectorTileEntity> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ItemPipeConnectorContainer(final int windowId, final Inventory playerInventory,
				final ItemPipeConnectorTileEntity tileEntity) {
			super(Registry.getContainerType("pipe_connector_item"), windowId, tileEntity, playerInventory,
					PLAYER_INV_POS, PLAYER_HOTBAR_POS, 9);
			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new FilterPipeValidatorSlot(tileEntity, (row * 3) + col, 55 + (18 * col),
							21 + (18 * row), tileEntity));
				}
			}

			for (int row = 0; row < 3; ++row) {
				this.addSlot(new AbstractMachine.SlotWithRestrictions(tileEntity, row + 9, 149, 21 + (row * 18),
						tileEntity));
			}

		}

		public ItemPipeConnectorContainer(final int windowId, final Inventory playerInventory,
				final FriendlyByteBuf data) {
			this(windowId, playerInventory,
					General.getBlockEntity(playerInventory, data, ItemPipeConnectorTileEntity.class));
		}

		private class FilterPipeValidatorSlot extends SlotWithRestrictions {

			private final int filterNum;
			public FilterPipeValidatorSlot(Container inventoryIn, int index, int xPosition, int yPosition,
					AbstractMachine<?> check) {
				super(inventoryIn, index, xPosition, yPosition, check);
				this.filterNum = index;
			}

			@Override
			public boolean isActive() {
				return tileEntity.enableFilterSlot(filterNum);
			}
			
			

		}

		@Override
		public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
			if(slot > 35 && slot < 45) {
				ItemStack is = this.getCarried();
				if(!is.isEmpty()) {
					tileEntity.setItem(slot - 36, new ItemStack(is.getItem(), 1));
				}else {
					tileEntity.setItem(slot - 36, ItemStack.EMPTY);
				}
				
			}
			
			super.clicked(slot, dragType, clickTypeIn, player);
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ItemPipeConnectorScreen extends ScreenALMBase<ItemPipeConnectorContainer> {

		ItemPipeConnectorTileEntity tsfm;
		ItemPipeConnectorContainer container;

		public ItemPipeConnectorScreen(ItemPipeConnectorContainer screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73),
					"pipe_connector_item", false);
			this.renderTitleText = false;
			this.renderInventoryText = false;
			tsfm = screenContainer.tileEntity;
			container = screenContainer;
		}

		@Override
		protected void init() {
			super.init();
			int x = this.leftPos;
			int y = this.topPos;
			
			this.addRenderableWidget(new TrueFalseButton(x+32, y+20, 177, 1, 8, 8, new TrueFalseButtonSupplier("Input Enabled", "Input Disabled", () -> tsfm.inputMode), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "input")));
			this.addRenderableWidget(new TrueFalseButton(x+43, y+20, 177, 11, 8, 8, new TrueFalseButtonSupplier("Output Enabled", "Output Disabled", () -> tsfm.outputMode), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "output")));
			this.addRenderableWidget(new TrueFalseButton(x+32, y+31, 177, 21, 8, 8, new TrueFalseButtonSupplier("Nearest First", "Furthest First", () -> tsfm.nearestFirst), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "nearest")));
			this.addRenderableWidget(new TrueFalseButton(x+43, y+65, 177, 41, 8, 8, new TrueFalseButtonSupplier("Whitelist", "Blacklist", () -> tsfm.whitelist), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "whitelist")));
			this.addRenderableWidget(new TrueFalseButton(x+111, y+32, 8, 8, "Priority Increase", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "priorityup")));
			this.addRenderableWidget(new TrueFalseButton(x+111, y+43, 8, 8, "Priority Decrease", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "prioritydown")));
			this.addRenderableWidget(new TrueFalseButton(x+111, y+54, 8, 8, "Priority Reset", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "priorityzero")));
			this.addRenderableWidget(new RedstoneButton(x+43, y+42, 177, 51, 8, 8, new TrueFalseButtonSupplier("Enabled on Redstone Signal", "Always Active", () -> tsfm.redstone), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "redstone")));

		}

		private class RedstoneButton extends TrueFalseButton{
			
			public RedstoneButton(int x, int y, int blitx, int blity, int width, int height, TrueFalseButtonSupplier tfbs, OnPress onPress) {
				super(x, y, blitx, blity, width, height, tfbs, onPress);
			}
			
			@Override
			protected boolean isValidClickButton(int a) {
				return isRedstoneControlEnabled();
			}
			
			@Override
			public int[] getBlitData() {
				if(isRedstoneControlEnabled()) {
					return super.getBlitData();
				}
				return new int[] {x, y, 9, 12, this.getWidth(), this.getHeight()};
			}
			
			@Override
			public boolean getSupplierOutput() {
				if(!isRedstoneControlEnabled()) {
					return true;
				}
				return super.getSupplierOutput();
			}
			
			@Override
			public void renderToolTip(PoseStack mx, int mouseX, int mouseY) {
				if(isRedstoneControlEnabled()) {
					super.renderToolTip(mx, mouseX, mouseY);
				}
			}
			
			private boolean isRedstoneControlEnabled() {
				if (tsfm.getUpgradeAmount(Upgrades.PIPE_REDSTONE) > 0) {
					return true;
				}
				return false;
			}
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			// Covers the Round Robin and Refresh button.
			super.blit(x + 43, y + 31, 9, 12, 8, 8);
			super.blit(x + 158, y + 10, 9, 12, 8, 8);

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					if (tsfm.enableFilterSlot((row * 3) + col) == false) {
						super.blit(x + 55 + (18 * col), y + 21 + (18 * row), 186, 0, 16, 16);
					}
				}
			}

			this.drawCenteredString(this.font, this.tsfm.priority + "", x + 133, y + 43, 0xffffff);
		}

	}

	public static void sendPipeUpdatePacket(BlockPos pos, String button) {

		PacketData pd = new PacketData("item_pipe_gui");
		pd.writeBlockPos("location", pos);
		pd.writeUtf("button", button);

		HashPacketImpl.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, Level world) {

		if (pd.getCategory().equals("item_pipe_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			BlockEntity te = world.getBlockEntity(pos);
			if (te != null && te instanceof ItemPipeConnectorTileEntity) {
				ItemPipeConnectorTileEntity ipcte = (ItemPipeConnectorTileEntity) te;

				String b = pd.get("button", String.class);

				if (b.equals("input")) {

					ipcte.inputMode = !ipcte.inputMode;

				} else if (b.equals("output")) {
					ipcte.outputMode = !ipcte.outputMode;
				} else if (b.equals("nearest")) {
					ipcte.nearestFirst = !ipcte.nearestFirst;
				} /*
					 * else if (b.equals("roundrobin")) {
					 * 
					 * ipcte.rrMode = !ipcte.rrMode;
					 * 
					 * if (ipcte.rrMode == true) { ipcte.nearestFirst = true;
					 * ipcte.updateTargets((PipeBase<?>) Registry.getBlock("item_pipe")); } }
					 */ else if (b.equals("whitelist")) {
					ipcte.whitelist = !ipcte.whitelist;

				} else if (b.equals("priorityup")) {
					if (ipcte.priority < 99) {
						ipcte.priority++;
					}
				} else if (b.equals("prioritydown")) {
					if (ipcte.priority > -99) {
						ipcte.priority--;
					}

				} else if (b.equals("priorityzero")) {
					ipcte.priority = 0;
				} else if (b.equals("redstone")) {
					ipcte.redstone = !ipcte.redstone;
					if (ipcte.redstone && world.hasNeighborSignal(pos)) {
						ipcte.isRedstonePowered = true;
					}
				}

				ipcte.sendUpdates();
			}
		}
	}

	private boolean enableFilterSlot(int slot) {

		if (slot == 4) {
			return true;
		} else if (slot == 3 || slot == 5) {
			if (this.getUpgradeAmount(Upgrades.PIPE_FILTER) >= 1) {
				return true;
			}
		} else if (slot == 1 || slot == 7) {
			if (this.getUpgradeAmount(Upgrades.PIPE_FILTER) >= 2) {
				return true;
			}
		} else {
			if (this.getUpgradeAmount(Upgrades.PIPE_FILTER) >= 3) {
				return true;
			}
		}

		return false;
	}
}
