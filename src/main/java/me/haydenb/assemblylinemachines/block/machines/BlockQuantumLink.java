package me.haydenb.assemblylinemachines.block.machines;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Utils.*;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager.QuantumLinkHandler.QuantumLinkNetwork;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager.QuantumLinkStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockQuantumLink extends BlockScreenBlockEntity<BlockQuantumLink.TEQuantumLink> {

	public BlockQuantumLink() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "quantum_link", BlockQuantumLink.TEQuantumLink.class);
	}

	public static class TEQuantumLink extends ManagedSidedMachine<ContainerQuantumLink> implements ALMTicker<TEQuantumLink>{


		QuantumLinkNetwork qln = null;
		boolean configured = false;
		boolean passwordEnabled = false;
		boolean connected = false;
		public int[] pfi = new int[] {0,0,0};
		int id = 0;
		Integer password = null;
		int timer = 0;
		FluidStack tank = FluidStack.EMPTY;
		String status = "";
		int statusTimer = 0;

		IFluidHandler handler = new QuantumLinkFluidHandler();
		LazyOptional<IFluidHandler> lazy = LazyOptional.of(() -> handler);

		public TEQuantumLink(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 3, new TranslatableComponent(Registry.getBlock("quantum_link").getDescriptionId()), Registry.getContainerId("quantum_link"), ContainerQuantumLink.class, new EnergyProperties(true, true, 10000000), pos, state);
		}

		public TEQuantumLink(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("quantum_link"), pos, state);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {
				boolean sendupdates = false;
				if(timer++ == 20) {
					timer = 0;
					if(statusTimer > 0) {
						statusTimer--;
					}
					if(statusTimer == 0) {
						status = "";
						sendupdates = true;
					}

					if(qln == null) {

						if(connected == true || passwordEnabled == true) {
							connected = false;
							passwordEnabled = false;
							sendupdates = true;
						}
						if(configured == true) {

							Pair<QuantumLinkStatus, Optional<QuantumLinkNetwork>> result = QuantumLinkManager.getInstance(this.getLevel().getServer()).getHandler().getOrCreateQuantumLink(id, password);

							if(result.getFirst() != QuantumLinkStatus.WRONG_PASSWORD) {
								qln = result.getSecond().orElseThrow(() ->
								{
									return new NoSuchElementException();
								});
								
								if(result.getFirst() == QuantumLinkStatus.CREATED_PASSWORD || result.getFirst() == QuantumLinkStatus.CREATED_INSECURE) {
									status = "Created network.";
								}else {
									status = "Joined network.";
								}
								
								if(result.getFirst() == QuantumLinkStatus.CREATED_PASSWORD || result.getFirst() == QuantumLinkStatus.JOINED_PASSWORD) {
									passwordEnabled = true;
								}else {
									passwordEnabled = false;
								}
								statusTimer = 5;
								sendupdates = true;
							}else {
								configured = false;
								status = "Incorrect password.";
								statusTimer = 5;
								sendupdates = true;
							}
						}
					}else {

						if(connected == false) {
							connected = true;
							sendupdates = true;
						}

						if(!qln.contains(this)) {
							qln.addToNetwork(this);
						}


						if(pfi[0] == 1) {
							amount = qln.attemptInsertIntoNetwork(this, amount);
							sendupdates = true;
						}
						if(pfi[1] == 1) {
							if(!tank.isEmpty()) {
								tank = qln.attemptInsertIntoNetwork(this, tank);
								sendupdates = true;
							}

						}
						if(pfi[2] == 1) {

							if(!getItem(0).isEmpty()) {
								this.setItem(0, qln.attemptInsertIntoNetwork(this, getItem(0)));
							}
							if(!getItem(1).isEmpty()) {
								this.setItem(1, qln.attemptInsertIntoNetwork(this, getItem(1)));
							}
							if(!getItem(2).isEmpty()) {
								this.setItem(2, qln.attemptInsertIntoNetwork(this, getItem(2)));
							}
							
							sendupdates = true;
						}
					}
				}

				if(sendupdates) {
					sendUpdates();
				}
			}

		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {

			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return lazy.cast();
			}
			return super.getCapability(cap, side);
		}

		@Override
		public boolean canExtractFromSide(boolean isEnergy, int slot, Direction direction) {
			return true;
		}

		@Override
		public boolean canInsertToSide(boolean isEnergy, int slot, Direction direction) {
			return true;
		}

		@Override
		public void setRemoved() {
			super.setRemoved();

			if(qln != null) {
				qln.removeFromNetwork(this);
			}
		}
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
		@Override
		public void saveAdditional(CompoundTag compound) {

			compound.putBoolean("assemblylinemachines:configured", configured);
			compound.putIntArray("assemblylinemachines:pfi", pfi);
			compound.putInt("assemblylinemachines:networkid", id);
			if(password != null) {
				compound.putInt("assemblylinemachines:networkpassword", password);
			}

			CompoundTag sub = new CompoundTag();
			tank.writeToNBT(sub);
			compound.put("assemblylinemachines:tank", sub);
			compound.putString("assemblylinemachines:status", status);
			compound.putBoolean("assemblylinemachines:connected", connected);
			compound.putBoolean("assemblylinemachines:passwordconnection", passwordEnabled);

			super.saveAdditional(compound);
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);

			configured = compound.getBoolean("assemblylinemachines:configured");
			if(compound.contains("assemblylinemachines:pfi")) {
				pfi = compound.getIntArray("assemblylinemachines:pfi");
			}
			id = compound.getInt("assemblylinemachines:networkid");

			if(compound.contains("assemblylinemachines:networkpassword")) {
				password = compound.getInt("assemblylinemachines:networkpassword");
			}

			status = compound.getString("assemblylinemachines:status");
			if(compound.contains("assemblylinemachines:tank")) {
				tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tank"));
			}

			connected = compound.getBoolean("assemblylinemachines:connected");
			
			passwordEnabled = compound.getBoolean("assemblylinemachines:passwordconnection");
		}


		private class QuantumLinkFluidHandler implements IFluidHandler{

			QuantumLinkFluidHandler(){
			}
			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				return true;
			}

			@Override
			public int getTanks() {
				return 1;
			}

			@Override
			public int getTankCapacity(int tank) {
				return 4000;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				return TEQuantumLink.this.tank;
			}

			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if (!tank.isEmpty()) {
					if (resource.getFluid() != tank.getFluid()) {
						return 0;
					}
				}

				int attemptedInsert = resource.getAmount();
				int rmCapacity = getTankCapacity(0) - tank.getAmount();
				if (rmCapacity < attemptedInsert) {
					attemptedInsert = rmCapacity;
				}

				if (action != FluidAction.SIMULATE) {
					if (tank.isEmpty()) {
						tank = new FluidStack(resource.getFluid(), attemptedInsert);
					} else {
						tank.setAmount(tank.getAmount() + attemptedInsert);
					}
				}

				sendUpdates();
				return attemptedInsert;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {

				if (tank.getAmount() < maxDrain) {
					maxDrain = tank.getAmount();
				}

				Fluid f = tank.getFluid();
				if (action != FluidAction.SIMULATE) {
					tank.setAmount(tank.getAmount() - maxDrain);
				}

				if (tank.getAmount() <= 0) {
					tank = FluidStack.EMPTY;

				}

				sendUpdates();
				return new FluidStack(f, maxDrain);
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
		}

	}

	public static class ContainerQuantumLink extends ContainerALMBase<TEQuantumLink>{

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerQuantumLink(final int windowId, final Inventory playerInventory, final TEQuantumLink tileEntity) {
			super(Registry.getContainerType("quantum_link"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 8, 59, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 26, 59, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 44, 59, tileEntity));
		}

		public ContainerQuantumLink(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEQuantumLink.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenQuantumLink extends ScreenALMBase<ContainerQuantumLink>{
		TEQuantumLink tsfm;
		private EditBox idField;
		private EditBox pinField;
		private String txtId = "";
		private HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		
		private final TextureAtlasSprite netherPortal;

		public ScreenQuantumLink(ContainerQuantumLink screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), null, null, "quantum_link", false);
			tsfm = screenContainer.tileEntity;
			netherPortal = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(Blocks.NETHER_PORTAL.defaultBlockState(), tsfm.getLevel(), tsfm.getBlockPos());

			renderTitleText = false;
			renderInventoryText = false;

			txtId = tsfm.id + "";
		}

		@Override
		protected void init() {

			super.init();

			int x = leftPos;
			int y = topPos;

			this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

			idField = new EditBox(this.font, x + 138, y + 9, 20, 9, new TextComponent("ID"));
			idField.setCanLoseFocus(true);
			idField.setBordered(false);
			idField.setMaxLength(3);
			idField.setTextColor(0xffffff);
			idField.setTextColorUneditable(0xffffff);
			idField.insertText(txtId);
			idField.setResponder((string) ->{
				txtId = string;
			});
			idField.setFilter((string) ->{
				if(string.trim().isEmpty()) {
					return true;
				}
				
				return StringUtils.isNumeric(string);
			});

			pinField = new EditBox(this.font, x + 138, y + 24, 32, 9, new TextComponent("PIN"));
			pinField.setCanLoseFocus(true);
			pinField.setBordered(false);
			pinField.setMaxLength(4);
			pinField.setTextColor(0xffffff);
			pinField.setTextColorUneditable(0xffffff);
			pinField.setFilter((string) ->{

				if(string.trim().isEmpty()) {
					return true;
				}
				return StringUtils.isNumeric(string);
			});
			this.addRenderableWidget(idField);
			this.addRenderableWidget(pinField);

			this.setFocused(idField);

			this.addRenderableWidget(new QuantumChannelButton(x+136, y+65, 11, 11, (b) -> pressButton(0, tsfm.getBlockPos()), 0));
			this.addRenderableWidget(new QuantumChannelButton(x+148, y+65, 11, 11, (b) -> pressButton(1, tsfm.getBlockPos()), 1));
			this.addRenderableWidget(new QuantumChannelButton(x+160, y+65, 11, 11, (b) -> pressButton(2, tsfm.getBlockPos()), 2));
			this.addRenderableWidget(new TrueFalseButton(x+160, y+5, 11, 11, "Activate Quantum Link", (b) -> {
				if(!idField.getValue().trim().isEmpty()) {
					if(pinField.getValue().trim().isEmpty()) {
						pressConnectButton(Integer.parseInt(txtId), null, tsfm.getBlockPos());
					}else {
						pressConnectButton(Integer.parseInt(txtId), Integer.parseInt(pinField.getValue()), tsfm.getBlockPos());
					}
				}
			}));
		}

		private class QuantumChannelButton extends TrueFalseButton{
			
			final int channel;
			final String tooltip;
			public QuantumChannelButton(int x, int y, int width, int height, OnPress onPress, int channel) {
				super(x, y, width, height, null, onPress);
				this.channel = channel;
				switch(channel) {
				case 0:
					tooltip = "Power";
					break;
				case 1:
					tooltip = "Fluids";
					break;
				case 2:
					tooltip = "Items";
					break;
				default:
					tooltip = null;
				}
			}
			
			@Override
			public boolean getSupplierOutput() {
				if(tsfm.pfi[channel] == 0) {
					return false;
				}
				
				return true;
			}
			
			@Override
			public int[] getBlitData() {
				int xoffset = 12 * channel;
				int yoffset = 0;
				if(tsfm.pfi[channel] == 1) {
					yoffset += 12;
				}
				
				return new int[] {x, y, 176+xoffset, 74+yoffset, 11, 11};
			}
			
			@Override
			public void renderToolTip(PoseStack pMatrixStack, int pMouseX, int pMouseY) {
				if(this.isHoveredOrFocused()) {
					List<String> vals = new ArrayList<>();
					switch(tsfm.pfi[channel]) {
					case 0:
						vals.add(tooltip + " Receive Mode");
						vals.add("§8§oWill receive " + tooltip + " from other QLs.");
						break;
					case 1:
						vals.add(tooltip + " Send Mode");
						vals.add("§8§oWill transfer " + tooltip + " to other QLs.");
						break;
					case 2:
						vals.add(tooltip + " Disabled");
						vals.add("§8§oWill not interact with " + tooltip + ".");
						break;
					}
					
					renderComponentTooltip(vals, pMouseX, pMouseY);
				}
			}
		}
		
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			renderFluid(tsfm.tank, x+13, y+13);

			super.blit(x+64, y+6, 69, 69, 69, netherPortal);
			
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int prog = Math.round(((float) tsfm.amount / (float) tsfm.properties.getCapacity()) * 37F);
			super.blit(x + 39, y + 13 + (37 - prog), 176, (37 - prog), 16, prog);

			renderFluidOverlayBar(tsfm.tank, tsfm.handler.getTankCapacity(0), x + 13, y + 13);

			if(!tsfm.status.isEmpty()) {
				float wsc = 35f / (float) this.font.width(tsfm.status);
				MathHelper.renderScaledText(font, x + 136, y + 35, wsc, tsfm.status, false, 0xffffff);
			}


			if(tsfm.connected) {

				
				if(tsfm.passwordEnabled) {
					super.blit(x+160, y+35, 176, 98, 11, 11);
				}
				

			}else {
				super.blit(x+64, y+6, 187, 99, 69, 69);
			}

			this.idField.renderButton(mx, mouseX, mouseY, partialTicks);
			this.pinField.renderButton(mx, mouseX, mouseY, partialTicks);


		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			if (mouseX >= x + 39 && mouseY >= y + 13 && mouseX <= x + 39 + 15 && mouseY <= y + 13 + 36) {

				if(Screen.hasShiftDown()) {
					ArrayList<String> str = new ArrayList<>();
					str.add(Formatting.GENERAL_FORMAT.format(tsfm.amount) + "/" + Formatting.GENERAL_FORMAT.format(tsfm.properties.getCapacity()) + "FE");
					this.renderComponentTooltip(str,
							mouseX - x, mouseY - y);
				}else {
					this.renderComponentTooltip(Formatting.formatToSuffix(tsfm.amount) + "/" + Formatting.formatToSuffix(tsfm.properties.getCapacity()) + "FE",
							mouseX - x, mouseY - y);
				}

			}
			
			if(tsfm.connected && tsfm.passwordEnabled && mouseX >= x + 160 && mouseY >= y + 35 && mouseX <= x + 160 + 11 && mouseY <= y + 35 + 11) {
				this.renderComponentTooltip("Secure connection!", mouseX - x, mouseY - y);
			}

			renderFluidTooltip(tsfm.tank, mouseX, mouseY, x + 13, y + 13, x, y);
		}

		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				}

				super.blit(xblit, yblit, 37, 37, 37, tas);
			}
		}

		private void renderFluidOverlayBar(FluidStack fs, float capacity, int xblit, int yblit) {
			int fprog = Math.round(((float) fs.getAmount() / capacity) * 37f);
			super.blit(xblit, yblit, 176, 37, 8, 37 - fprog);
		}

		private void renderFluidTooltip(FluidStack fs, int mouseX, int mouseY, int mminx, int mminy, int bx, int by) {

			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 7 && mouseY <= mminy + 36) {
				if (!fs.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(fs.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(Formatting.FEPT_FORMAT.format(fs.getAmount()) + " mB");

					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderComponentTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderComponentTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
		

		public static void pressButton(int button, BlockPos pos) {
			PacketData pd = new PacketData("quantum_link_gui");
			pd.writeUtf("type", "io");
			pd.writeBlockPos("location", pos);
			pd.writeInteger("button", button);

			PacketHandler.INSTANCE.sendToServer(pd);
		}

		public static void pressConnectButton(int channel, Integer password, BlockPos pos) {
			PacketData pd = new PacketData("quantum_link_gui");
			pd.writeUtf("type", "enable");
			pd.writeBlockPos("location", pos);
			pd.writeInteger("channel", channel);
			if(password != null) {
				pd.writeInteger("password", password);
			}else {
				pd.writeInteger("password", -99999);
			}


			PacketHandler.INSTANCE.sendToServer(pd);

		}

	}

	public static void receiveFromServer(PacketData pd, Level world) {
		if(pd.getCategory().equals("quantum_link_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
			if(tex instanceof TEQuantumLink) {
				TEQuantumLink te = (TEQuantumLink) tex;
				String b = pd.get("type", String.class);
				if(b.equals("enable")) {

					te.configured = true;

					if(te.qln != null) {
						te.qln.removeFromNetwork(te);
						te.qln = null;
					}
					te.id = pd.get("channel", Integer.class);

					Integer password = pd.get("password", Integer.class);

					if(password == -99999) {
						te.password = null;
					}else {
						te.password = password;
					}
				}else if(b.equals("io")) {
					int bx = pd.get("button", Integer.class);

					if(te.pfi[bx] == 2) {
						te.pfi[bx] = 0;
					}else {
						te.pfi[bx]++;
					}

				}

				te.sendUpdates();
			}
		}
	}
}
