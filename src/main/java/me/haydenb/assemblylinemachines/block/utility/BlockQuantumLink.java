package me.haydenb.assemblylinemachines.block.utility;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager.QuantumLinkHandler.QuantumLinkNetwork;
import me.haydenb.assemblylinemachines.world.QuantumLinkManager.QuantumLinkStatus;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockQuantumLink extends BlockScreenTileEntity<BlockQuantumLink.TEQuantumLink> {

	public BlockQuantumLink() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "quantum_link", BlockQuantumLink.TEQuantumLink.class);
	}

	public static class TEQuantumLink extends ManagedSidedMachine<ContainerQuantumLink> implements ITickableTileEntity{


		QuantumLinkNetwork qln = null;
		boolean configured = false;
		boolean passwordEnabled = false;
		boolean connected = false;
		public int[] pfi = new int[] {0,0,0};
		int id = 0;
		Integer password = null;
		int timer = 0;
		FluidStack tank = FluidStack.EMPTY;
		ServerWorld sw = null;
		String status = "";
		int statusTimer = 0;

		IFluidHandler handler = new QuantumLinkFluidHandler();
		LazyOptional<IFluidHandler> lazy = LazyOptional.of(() -> handler);

		public TEQuantumLink(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 3, new TranslationTextComponent(Registry.getBlock("quantum_link").getTranslationKey()), Registry.getContainerId("quantum_link"), ContainerQuantumLink.class, new EnergyProperties(true, true, 10000000));
		}

		public TEQuantumLink() {
			this(Registry.getTileEntity("quantum_link"));
		}

		@Override
		public void tick() {
			if(!world.isRemote) {
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
							if(sw == null) {
								sw = getServerWorld(world);
							}

							Pair<QuantumLinkStatus, Optional<QuantumLinkNetwork>> result = QuantumLinkManager.getInstance(sw).getHandler().getOrCreateQuantumLink(id, password);

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

							if(!getStackInSlot(0).isEmpty()) {
								setInventorySlotContents(0, qln.attemptInsertIntoNetwork(this, getStackInSlot(0)));
							}
							if(!getStackInSlot(1).isEmpty()) {
								setInventorySlotContents(1, qln.attemptInsertIntoNetwork(this, getStackInSlot(1)));
							}
							if(!getStackInSlot(2).isEmpty()) {
								setInventorySlotContents(2, qln.attemptInsertIntoNetwork(this, getStackInSlot(2)));
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
		public boolean canExtractFromSide(int slot, Direction direction) {
			return true;
		}

		@Override
		public boolean canInsertToSide(int slot, Direction direction) {
			return true;
		}

		@Override
		public void remove() {
			super.remove();

			if(qln != null) {
				qln.removeFromNetwork(this);
			}
		}
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
		@Override
		public CompoundNBT write(CompoundNBT compound) {

			compound.putBoolean("assemblylinemachines:configured", configured);
			compound.putIntArray("assemblylinemachines:pfi", pfi);
			compound.putInt("assemblylinemachines:networkid", id);
			if(password != null) {
				compound.putInt("assemblylinemachines:networkpassword", password);
			}

			CompoundNBT sub = new CompoundNBT();
			tank.writeToNBT(sub);
			compound.put("assemblylinemachines:tank", sub);
			compound.putString("assemblylinemachines:status", status);
			compound.putBoolean("assemblylinemachines:connected", connected);
			compound.putBoolean("assemblylinemachines:passwordconnection", passwordEnabled);

			return super.write(compound);
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);

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

		public ContainerQuantumLink(final int windowId, final PlayerInventory playerInventory, final TEQuantumLink tileEntity) {
			super(Registry.getContainerType("quantum_link"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 8, 59, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 26, 59, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 44, 59, tileEntity));
		}

		public ContainerQuantumLink(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEQuantumLink.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenQuantumLink extends ScreenALMBase<ContainerQuantumLink>{
		TEQuantumLink tsfm;
		private TextFieldWidget idField;
		private TextFieldWidget pinField;
		private String txtId = "";
		private SimpleButton p;
		private SimpleButton f;
		private SimpleButton i;
		private SimpleButton apply;
		private HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		
		@SuppressWarnings("deprecation")
		private final TextureAtlasSprite netherPortal = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.NETHER_PORTAL.getDefaultState());

		public ScreenQuantumLink(ContainerQuantumLink screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), null, null, "quantum_link", false);
			tsfm = screenContainer.tileEntity;


			renderTitleText = false;
			renderInventoryText = false;

			txtId = tsfm.id + "";
		}

		@Override
		protected void init() {

			super.init();

			int x = guiLeft;
			int y = guiTop;

			this.field_230706_i_.keyboardListener.enableRepeatEvents(true);

			idField = new TextFieldWidget(this.field_230712_o_, x + 138, y + 9, 20, 9, new StringTextComponent("ID"));
			idField.setCanLoseFocus(true);
			idField.setEnableBackgroundDrawing(false);
			idField.setMaxStringLength(3);
			idField.setTextColor(0xffffff);
			idField.setDisabledTextColour(0xffffff);
			idField.setText(txtId);
			idField.setResponder((string) ->{
				txtId = string;
			});
			idField.setValidator((string) ->{
				if(string.trim().isEmpty()) {
					return true;
				}
				
				return StringUtils.isNumeric(string);
			});

			pinField = new TextFieldWidget(this.field_230712_o_, x + 138, y + 24, 32, 9, new StringTextComponent("PIN"));
			pinField.setCanLoseFocus(true);
			pinField.setEnableBackgroundDrawing(false);
			pinField.setMaxStringLength(4);
			pinField.setTextColor(0xffffff);
			pinField.setDisabledTextColour(0xffffff);
			pinField.setValidator((string) ->{

				if(string.trim().isEmpty()) {
					return true;
				}
				return StringUtils.isNumeric(string);
			});

			this.field_230705_e_.add(idField);
			this.field_230705_e_.add(pinField);

			this.setFocusedDefault(idField);

			p = new SimpleButton(x+ 136, y+65, 0, 0, 11, 11, null, (button) ->{
				pressButton(0, tsfm.getPos());
			});

			f = new SimpleButton(x+ 148, y+65, 0, 0, 11, 11, null, (button) ->{
				pressButton(1, tsfm.getPos());
			});

			i = new SimpleButton(x+ 160, y+65, 0, 0, 11, 11, null, (button) ->{
				pressButton(2, tsfm.getPos());
			});

			apply = new SimpleButton(x+ 160, y+5, 0, 0, 11, 11, null, (button) ->{
				if(!idField.getText().trim().isEmpty()) {
					if(pinField.getText().trim().isEmpty()) {
						pressConnectButton(Integer.parseInt(txtId), null, tsfm.getPos());
					}else {
						pressConnectButton(Integer.parseInt(txtId), Integer.parseInt(pinField.getText()), tsfm.getPos());
					}
				}
				

			});

			this.addButton(p);
			this.addButton(f);
			this.addButton(i);
			this.addButton(apply);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

			field_230706_i_.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

			renderFluid(tsfm.tank, x+13, y+13);


			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int prog = Math.round(((float) tsfm.amount / (float) tsfm.properties.getCapacity()) * 37F);
			super.blit(x + 39, y + 13 + (37 - prog), 176, (37 - prog), 16, prog);

			renderFluidOverlayBar(tsfm.tank, tsfm.handler.getTankCapacity(0), x + 13, y + 13);

			renderCovering(x, y, 0);
			renderCovering(x, y, 1);
			renderCovering(x, y, 2);

			if(!tsfm.status.isEmpty()) {
				float wsc = 35f / (float) this.font.getStringWidth(tsfm.status);
				MathHelper.renderScaledText(font, x + 136, y + 35, wsc, tsfm.status, false, 0xffffff);
			}


			if(tsfm.connected) {

				
				if(tsfm.passwordEnabled) {
					super.blit(x+160, y+35, 176, 98, 11, 11);
				}
				field_230706_i_.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
				super.blit(x + 64, y + 6, 69, 69, 69, netherPortal);

			}

			this.idField.func_230431_b_(mx, mouseX, mouseY, partialTicks);
			this.pinField.func_230431_b_(mx, mouseX, mouseY, partialTicks);


		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

			if (mouseX >= x + 39 && mouseY >= y + 13 && mouseX <= x + 39 + 15 && mouseY <= y + 13 + 36) {

				if(Screen.func_231173_s_()) {
					ArrayList<String> str = new ArrayList<>();
					str.add(Formatting.GENERAL_FORMAT.format(tsfm.amount) + "/" + Formatting.GENERAL_FORMAT.format(tsfm.properties.getCapacity()) + "FE");
					this.renderTooltip(str,
							mouseX - x, mouseY - y);
				}else {
					this.renderTooltip(Formatting.formatToSuffix(tsfm.amount) + "/" + Formatting.formatToSuffix(tsfm.properties.getCapacity()) + "FE",
							mouseX - x, mouseY - y);
				}

			}
			
			if(tsfm.connected && tsfm.passwordEnabled && mouseX >= x + 160 && mouseY >= y + 35 && mouseX <= x + 160 + 11 && mouseY <= y + 35 + 11) {
				this.renderTooltip("Secure connection!", mouseX - x, mouseY - y);
			}

			renderButtonTooltip(x, y, mouseX, mouseY, 0, p, "Power");
			renderButtonTooltip(x, y, mouseX, mouseY, 1, f, "Fluids");
			renderButtonTooltip(x, y, mouseX, mouseY, 2, i, "Items");
			renderButtonTooltip(x, y, mouseX, mouseY, 3, apply, null);

			renderFluidTooltip(tsfm.tank, mouseX, mouseY, x + 13, y + 13, x, y);
		}

		private void renderButtonTooltip(int x, int y, int mouseX, int mouseY, int id, SimpleButton bb, String text) {


			if(mouseX >= bb.getX() && mouseX <= bb.getX() + 11 && mouseY >= bb.getY() && mouseY <= bb.getY() + 11) {
				String a;

				if(id == 3) {
					a = "Activate Quantum Link";
				}else {
					if(tsfm.pfi[id] == 0) {
						a = text + " Input Mode";
					}else if(tsfm.pfi[id] == 1) {
						a = text + " Output Mode";
					}else{
						a = text + " Disabled";
					}
				}


				this.renderTooltip(a, mouseX - x, mouseY - y);
			}



		}

		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					GL11.glColor4f(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					GL11.glColor4f(1f, 1f, 1f, 1f);
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

					str.add(fs.getDisplayName().func_230532_e_().getString());
					if (Screen.func_231173_s_()) {

						str.add(Formatting.FEPT_FORMAT.format(fs.getAmount()) + " mB");

					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}

		private void renderCovering(int x, int y, int id) {

			int mode = tsfm.pfi[id];
			if(mode != 0) {

				int xoffset = 12 * id;
				int yoffset = 0;
				if(mode == 1) {
					yoffset = 12;
				}

				super.blit(x + 136 + xoffset, y + 65, 176 + xoffset, 74 + yoffset, 11, 11);
			}
		}

		public static void pressButton(int button, BlockPos pos) {
			PacketData pd = new PacketData("quantum_link_gui");
			pd.writeString("type", "io");
			pd.writeBlockPos("location", pos);
			pd.writeInteger("button", button);

			HashPacketImpl.INSTANCE.sendToServer(pd);
		}

		public static void pressConnectButton(int channel, Integer password, BlockPos pos) {
			PacketData pd = new PacketData("quantum_link_gui");
			pd.writeString("type", "enable");
			pd.writeBlockPos("location", pos);
			pd.writeInteger("channel", channel);
			if(password != null) {
				pd.writeInteger("password", password);
			}else {
				pd.writeInteger("password", -99999);
			}


			HashPacketImpl.INSTANCE.sendToServer(pd);

		}

	}

	public static void receiveFromServer(PacketData pd, World world) {
		if(pd.getCategory().equals("quantum_link_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			TileEntity tex = world.getTileEntity(pos);
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


	private static ServerWorld getServerWorld(World world) {
		return world.getServer().getWorld(world.func_234923_W_());
	}
}
