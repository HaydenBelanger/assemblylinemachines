package me.haydenb.assemblylinemachines.block.pipe;

import java.util.*;

import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.pipe.PipeConnectorTileEntity.PipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.*;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class PipeConnectorTileEntity extends SimpleMachine<PipeConnectorContainer>
		implements ALMTicker<PipeConnectorTileEntity> {

	//Relevant for all pipes.
	private boolean inputMode;
	private boolean outputMode;
	
	//Only relevant if there are upgrades present/the pipe is an advanced-level.
	private boolean nearestFirst = true;
	private boolean whitelist = false;
	private boolean redstone = false;
	private int priority = 0;

	private int timer = 0;
	private int nTimer = 50;
	double pendingCooldown = 0;
	
	private HashMap<TransmissionType, Integer> omniPipeTimer = new HashMap<>();
	
	private HashMap<TransmissionType, Pair<Boolean, Boolean>> omniPipeIO = new HashMap<>();

	HashMap<Object, TriConsumer<PipeConnectorTileEntity, Object, Integer>> conCapabilities = new HashMap<>();
	
	final PipeType pipeType;
	final TransmissionType transType;

	final TreeSet<PipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<PipeConnectorTileEntity>() {

				@Override
				public int compare(PipeConnectorTileEntity o1, PipeConnectorTileEntity o2) {
					if (o1.priority > o2.priority) {
						return 1;
					} else if (o1.priority < o2.priority) {
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


	public PipeConnectorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, 12, new TranslatableComponent(((BlockPipe) state.getBlock()).transType.getDescriptionId()), Registry.getContainerId("pipe_connector"),
				PipeConnectorContainer.class, pos, state);
		BlockPipe bp = (BlockPipe) state.getBlock();
		this.pipeType = bp.pipeType;
		this.transType = bp.transType;
		this.inputMode = true;
		this.outputMode = this.transType == TransmissionType.OMNI;
	}

	public PipeConnectorTileEntity(BlockPos pos, BlockState state) {
		this(Registry.getBlockEntity("pipe_connector"), pos, state);
	}

	@Override
	public boolean isAllowedInSlot(int slot, ItemStack stack) {
		if (this.pipeType != PipeType.BASIC && slot >= 9 && slot <= 12) {
			if (stack.getItem() instanceof ItemUpgrade) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		if (compound.contains("assemblylinemachines:nearest")) nearestFirst = compound.getBoolean("assemblylinemachines:nearest");
		if (compound.contains("assemblylinemachines:whitelist")) whitelist = compound.getBoolean("assemblylinemachines:whitelist");
		if (compound.contains("assemblylinemachines:priority")) priority = compound.getInt("assemblylinemachines:priority");
		if (compound.contains("assemblylinemachines:redstone")) redstone = compound.getBoolean("assemblylinemachines:redstone");
		if (compound.contains("assemblylinemachines:pendingcooldown")) pendingCooldown = compound.getDouble("assemblylinemachines:pendingcooldown");
		if(this.transType == TransmissionType.OMNI) {
			omniPipeIO.clear();
			for(TransmissionType tt : TransmissionType.values()) {
				if(tt != TransmissionType.OMNI) {
					boolean in = true;
					boolean out = false;
					if(compound.contains("assemblylinemachines:input" + tt.toString().toLowerCase())) in = compound.getBoolean("assemblylinemachines:input" + tt.toString().toLowerCase());
					if(compound.contains("assemblylinemachines:output" + tt.toString().toLowerCase())) out = compound.getBoolean("assemblylinemachines:output" + tt.toString().toLowerCase());
					omniPipeIO.put(tt, Pair.of(in, out));
				}
			}
		}else {
			if (compound.contains("assemblylinemachines:input")) inputMode = compound.getBoolean("assemblylinemachines:input");
			if (compound.contains("assemblylinemachines:output")) outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		
	}

	@Override
	public CompoundTag save(CompoundTag compound) {

		if(this.transType != TransmissionType.OMNI) {
			compound.putBoolean("assemblylinemachines:input", inputMode);
			compound.putBoolean("assemblylinemachines:output", outputMode);
		}else {
			for(TransmissionType tt : TransmissionType.values()) {
				if(tt != TransmissionType.OMNI) {
					Pair<Boolean, Boolean> pair = omniPipeIO.getOrDefault(tt, Pair.of(true, false));
					compound.putBoolean("assemblylinemachines:input" + tt.toString().toLowerCase(), pair.getFirst());
					compound.putBoolean("assemblylinemachines:output" + tt.toString().toLowerCase(), pair.getSecond());
				}
			}
		}
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		if(this.pipeType != PipeType.BASIC) {
			if(this.transType != TransmissionType.POWER) compound.putBoolean("assemblylinemachines:whitelist", whitelist);
			compound.putBoolean("assemblylinemachines:nearest", nearestFirst);
			compound.putBoolean("assemblylinemachines:redstone", redstone);
			compound.putInt("assemblylinemachines:priority", priority);
		}
		
		
		
		return super.save(compound);
	}

	@Override
	public void tick() {

		if (!level.isClientSide) {
			if (outputMode == true) {
				if (transType == TransmissionType.OMNI || timer++ == nTimer) {
					timer = 0;
					if (pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						List<TransmissionType> typesToProcess;
						if(transType != TransmissionType.OMNI) {
							nTimer = transType.getNTimerBase(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED));
							typesToProcess = List.of(TransmissionType.values());
						}else {
							typesToProcess = new ArrayList<>();
							for(TransmissionType tt : TransmissionType.values()) {
								if(tt != TransmissionType.OMNI && omniPipeIO.getOrDefault(tt, Pair.of(true, false)).getSecond()) {
									if(omniPipeTimer.getOrDefault(tt, 0) >= tt.getNTimerBase(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED))) {
										omniPipeTimer.remove(tt);
										typesToProcess.add(tt);
									}else {
										omniPipeTimer.put(tt, omniPipeTimer.getOrDefault(tt, 0) + 1);
									}
								}
							}
						}

						if(!typesToProcess.isEmpty()) {
							targets.clear();
							pathToNearest(this.getLevel(), this.getBlockPos(), new ArrayList<>(), this.getBlockPos(), targets);

							if (conCapabilities.isEmpty() && connectToOutput() == false) {
								return;
							}
							if (!conCapabilities.isEmpty()) {
								if (redstone == true) {
									if (getUpgradeAmount(Upgrades.PIPE_REDSTONE) == 0) {
										redstone = false;
									} else if (!level.hasNeighborSignal(this.getBlockPos())) {
										return;
									}

								}

								for(Object obj : conCapabilities.keySet()) {
									if(typesToProcess.contains(TransmissionType.getTransmissionFromCapability(obj))) {
										conCapabilities.get(obj).accept(this, obj, this.transType.getMaxTransfer(this, this.pipeType, obj));
									}
								}

								sendUpdates();
							}
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
	
	public void pathToNearest(Level world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, TreeSet<PipeConnectorTileEntity> targets) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.getValue(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				BlockEntity te = world.getBlockEntity(curPos);
				if(te != null && te instanceof PipeConnectorTileEntity) {
					PipeConnectorTileEntity ipc = (PipeConnectorTileEntity) te;
					targets.add(ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.relative(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof BlockPipe) {
						BlockPipe t = (BlockPipe) world.getBlockState(targPos).getBlock();
						if (t.transType == this.transType && t.pipeType == this.pipeType) {
							pathToNearest(world, targPos, checked, initial, targets);
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
	
	public int attemptAccept(Class<?> clazz, Object object) {
		if(inputMode == false || (conCapabilities.isEmpty() && connectToOutput() == false)) return 0;
		
		
		Object prelimOutput = null;
		for(Object o : conCapabilities.keySet()) {
			if(clazz.isInstance(o)) prelimOutput = clazz.cast(o);
		}
		if(prelimOutput != null) {
			if(this.transType == TransmissionType.OMNI && !this.omniPipeIO.getOrDefault(TransmissionType.getTransmissionFromCapability(prelimOutput), Pair.of(true, false)).getFirst()) return 0;
			if(prelimOutput instanceof IItemHandler && object instanceof ItemStack) {
				ItemStack stack = (ItemStack) object;
				IItemHandler output = (IItemHandler) prelimOutput;
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
			}else if(prelimOutput instanceof IFluidHandler && object instanceof FluidStack) {
				FluidStack stack = (FluidStack) object;
				if(!checkWhiteBlackList(stack)) {
					return 0;
				}
				return ((IFluidHandler) prelimOutput).fill(stack, FluidAction.EXECUTE);
			}else if(prelimOutput instanceof IEnergyStorage && object instanceof Integer) {
				return ((IEnergyStorage) prelimOutput).receiveEnergy((Integer) object, false);
			}
		}
		
		return 0;
		
	}

	boolean checkWhiteBlackList(ItemStack stack) {
		for (int i = 0; i < 9; i++) {
			if (stack.getItem() == contents.get(i).getItem()) {
				return whitelist;
			}
		}
		return !whitelist;
	}
	
	boolean checkWhiteBlackList(FluidStack stack) {
		for(int i = 0; i < 9; i++) {
			IFluidHandlerItem lx = contents.get(i).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
			if(lx != null && lx.getFluidInTank(0).getFluid().equals(stack.getFluid())) {
				return whitelist;
			}
		}
		return !whitelist;
	}
	
	private boolean enableFilterSlot(int slot) {

		if(pipeType == PipeType.BASIC || transType == TransmissionType.POWER) {
			return false;
		}
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

	private boolean connectToOutput() {

		for (Direction d : Direction.values()) {
			if (getBlockState().getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				Pair<Object, TriConsumer<PipeConnectorTileEntity, Object, Integer>> res = this.transType.getCapability(this.getLevel().getBlockEntity(this.getBlockPos().relative(d)), d.getOpposite(), this);
				if(res != null) {
					this.conCapabilities.put(res.getFirst(), res.getSecond());
					return true;
				}
			}
		}

		return false;
	}

	public static class PipeConnectorContainer extends AbstractMachine.ContainerALMBase<PipeConnectorTileEntity> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public PipeConnectorContainer(final int windowId, final Inventory playerInventory,
				final PipeConnectorTileEntity tileEntity) {
			super(Registry.getContainerType("pipe_connector"), windowId, tileEntity, playerInventory,
					PLAYER_INV_POS, PLAYER_HOTBAR_POS, 9);
			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new FilterPipeValidatorSlot(tileEntity, (row * 3) + col, 55 + (18 * col),
							21 + (18 * row), tileEntity));
				}
			}

			for (int row = 0; row < 3; ++row) {
				this.addSlot(new PipeUpgradeSlot(tileEntity, row + 9, 149, 21 + (row * 18),
						tileEntity));
			}

		}

		public PipeConnectorContainer(final int windowId, final Inventory playerInventory,
				final FriendlyByteBuf data) {
			this(windowId, playerInventory,
					Utils.getBlockEntity(playerInventory, data, PipeConnectorTileEntity.class));
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
		
		private class PipeUpgradeSlot extends SlotWithRestrictions {

			public PipeUpgradeSlot(Container inventoryIn, int index, int xPosition, int yPosition, AbstractMachine<?> check) {
				super(inventoryIn, index, xPosition, yPosition, check);
			}
			
			@Override
			public boolean isActive() {
				return tileEntity.pipeType != PipeType.BASIC;
			}
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class PipeConnectorScreen extends ScreenALMBase<PipeConnectorContainer> {

		PipeConnectorTileEntity tsfm;
		PipeConnectorContainer container;

		public PipeConnectorScreen(PipeConnectorContainer screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73),
					screenContainer.tileEntity.transType.getGUITexture(screenContainer.tileEntity.pipeType), false);
			tsfm = screenContainer.tileEntity;
			container = screenContainer;
			if(tsfm.pipeType != PipeType.BASIC && tsfm.transType != TransmissionType.POWER) {
				TranslatableComponent shortInvText = new TranslatableComponent(Util.makeDescriptionId("tileEntity", new ResourceLocation(AssemblyLineMachines.MODID, "inventory_shortened")));
				this.inventoryText = () -> shortInvText;
			}
		}

		@Override
		protected void init() {
			super.init();
			int x = this.leftPos;
			int y = this.topPos;
			
			if(tsfm.transType != TransmissionType.OMNI) {
				int[] ioCoords = new int[]{75, 35, 89, 35};
				if(tsfm.pipeType != PipeType.BASIC && tsfm.transType != TransmissionType.POWER) {
					ioCoords = new int[] {33, 50, 33, 32};
				}
				
				this.addRenderableWidget(new TrueFalseButton(x+ioCoords[0], y+ioCoords[1], 177, 1, 12, 12, new TrueFalseButtonSupplier("Input Enabled", "Input Disabled", () -> tsfm.inputMode), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "input")));
				this.addRenderableWidget(new TrueFalseButton(x+ioCoords[2], y+ioCoords[3], 177, 15, 12, 12, new TrueFalseButtonSupplier("Output Enabled", "Output Disabled", () -> tsfm.outputMode), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "output")));
			}else {
				for(TransmissionType tt : TransmissionType.values()) {
					if(tt != TransmissionType.OMNI) {
						Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> coords = tt.getOmniCoords();
						this.addRenderableWidget(new TrueFalseButton(x+coords.getFirst().getFirst(), y+coords.getFirst().getSecond(), 177, 1, 8, 8, new TrueFalseButtonSupplier(tt.getPrettyName() + " Input Enabled", tt.getPrettyName() + " Input Disabled",
								() -> tsfm.omniPipeIO.getOrDefault(tt, Pair.of(true, false)).getFirst()), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "input", tt.toString().toLowerCase())));
						this.addRenderableWidget(new TrueFalseButton(x+coords.getSecond().getFirst(), y+coords.getSecond().getSecond(), 177, 11, 8, 8, new TrueFalseButtonSupplier(tt.getPrettyName() + " Output Enabled", tt.getPrettyName() + " Output Disabled",
								() -> tsfm.omniPipeIO.getOrDefault(tt, Pair.of(true, false)).getSecond()), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "output", tt.toString().toLowerCase())));
					}
				}
			}
			
			
			if(tsfm.pipeType != PipeType.BASIC) {
				int[] additX = tsfm.transType == TransmissionType.POWER ? new int[] {57, 57} : new int[] {22, 22};
				int[] additY;
				if(tsfm.transType == TransmissionType.OMNI) {
					additX = new int[] {39, 28};
					additY = new int[] {25, 25};
				}else {
					additY = new int[] {32, 43};
				}
				
				this.addRenderableWidget(new TrueFalseButton(x+111, y+32, 8, 8, "Priority Increase", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "priorityup")));
				this.addRenderableWidget(new TrueFalseButton(x+111, y+43, 8, 8, "Priority Decrease", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "prioritydown")));
				this.addRenderableWidget(new TrueFalseButton(x+111, y+54, 8, 8, "Priority Reset", (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "priorityzero")));
				
				this.addRenderableWidget(new RedstoneButton(x+additX[1], y+additY[1], 177, 51, 8, 8, new TrueFalseButtonSupplier("Enabled on Redstone Signal", "Always Active", () -> tsfm.redstone), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "redstone")));
				this.addRenderableWidget(new TrueFalseButton(x+additX[0], y+additY[0] , 177, 29, 8, 8, new TrueFalseButtonSupplier("Nearest First", "Furthest First", () -> tsfm.nearestFirst), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "nearest")));
				
				if(tsfm.transType != TransmissionType.POWER) {
					int[] whitelistData = tsfm.transType == TransmissionType.OMNI ? new int[] {17, 25} : new int[] {22, 54};
					this.addRenderableWidget(new TrueFalseButton(x+whitelistData[0], y+whitelistData[1], 177, 41, 8, 8, new TrueFalseButtonSupplier("Whitelist", "Blacklist", () -> tsfm.whitelist), (b) -> sendPipeUpdatePacket(tsfm.getBlockPos(), "whitelist")));
					
				}
			}
			

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

			if(tsfm.pipeType != PipeType.BASIC && tsfm.transType != TransmissionType.POWER) {
				for (int row = 0; row < 3; ++row) {
					for (int col = 0; col < 3; ++col) {
						if (tsfm.enableFilterSlot((row * 3) + col) == false) {
							super.blit(x + 55 + (18 * col), y + 21 + (18 * row), 190, 0, 16, 16);
						}
					}
				}
			}

			if(tsfm.pipeType != PipeType.BASIC) {
				this.drawCenteredString(this.font, this.tsfm.priority + "", x + 133, y + 43, 0xffffff);
			}
			
		}

	}

	public static void sendPipeUpdatePacket(BlockPos pos, String button) {

		PacketData pd = new PacketData("item_pipe_gui");
		pd.writeBlockPos("location", pos);
		pd.writeUtf("button", button);
		pd.writeUtf("transmissiontype", "none");
		
		PacketHandler.INSTANCE.sendToServer(pd);
	}
	
	public static void sendPipeUpdatePacket(BlockPos pos, String button, String type) {
		PacketData pd = new PacketData("item_pipe_gui");
		pd.writeBlockPos("location", pos);
		pd.writeUtf("button", button);
		pd.writeUtf("transmissiontype", type);

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, Level world) {

		if (pd.getCategory().equals("item_pipe_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			BlockEntity te = world.getBlockEntity(pos);
			if (te != null && te instanceof PipeConnectorTileEntity) {
				PipeConnectorTileEntity ipcte = (PipeConnectorTileEntity) te;

				String b = pd.get("button", String.class);

				if (b.equals("input") || b.equals("output")) {
					String transType = pd.get("transmissiontype", String.class);
					if(!transType.equals("none")) {
						TransmissionType tt = TransmissionType.valueOf(transType.toUpperCase());
						if(tt != TransmissionType.OMNI) {
							Pair<Boolean, Boolean> orig = ipcte.omniPipeIO.getOrDefault(tt, Pair.of(true, false));
							if(b.equals("input")) {
								ipcte.omniPipeIO.put(tt, Pair.of(!orig.getFirst(), orig.getSecond()));
							}else {
								ipcte.omniPipeIO.put(tt, Pair.of(orig.getFirst(), !orig.getSecond()));
							}
						}
						
					}else {
						if(b.equals("input")) {
							ipcte.inputMode = !ipcte.inputMode;
						}else {
							ipcte.outputMode = !ipcte.outputMode;
						}
					}
				}else if(ipcte.pipeType != PipeType.BASIC){
					if (b.equals("nearest")) {
						ipcte.nearestFirst = !ipcte.nearestFirst;
					}else if (b.equals("whitelist") && ipcte.transType != TransmissionType.POWER) {
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
					}
				}
				

				ipcte.sendUpdates();
			}
		}
	}
}
