package me.haydenb.assemblylinemachines.block.machines;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.crafting.EnchantmentBookCrafting;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties.BathCraftingFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockExperienceMill {

	public static Block experienceMill() {
		return MachineBuilder.block().voxelShape(Stream.of(
				Block.box(0, 0, 0, 16, 7, 16),Block.box(6, 7, 6, 10, 10, 10),Block.box(3, 10, 3, 13, 13, 13)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true)
				.rightClickAction((state, level, pos, player) -> {
					boolean res = Utils.drainMainHandToHandler(player, ((TEExperienceMill) level.getBlockEntity(pos)).handler);
					return res ? InteractionResult.CONSUME : null;
				}).additionalProperties((state) -> state.setValue(EXP_MILL_PROP, 0), (builder) -> builder.add(EXP_MILL_PROP)).build("experience_mill", TEExperienceMill.class);
	}

	//OFF, ENCHANTMENT, BOOK, ANVIL
	private static final IntegerProperty EXP_MILL_PROP = IntegerProperty.create("display", 0, 3);

	public static class TEExperienceMill extends SimpleMachine<ContainerExperienceMill> implements ALMTicker<TEExperienceMill>{

		private byte mode = 1;
		//1 - Enchantment
		//2 - Book
		//3 - Anvil
		private int timer = 0;
		public FluidStack tank = FluidStack.EMPTY;
		private IFluidHandler handler = IFluidHandlerBypass.getSimpleOneTankHandler((fs) -> fs.getFluid().equals(Registry.getFluid("liquid_experience")), 6000, (oFs) -> {
			if(oFs.isPresent()) tank = oFs.get();
			return tank;
		}, (v) -> this.sendUpdates(), false);
		private LazyOptional<IFluidHandler> lazy = LazyOptional.of(() -> handler);
		private float progress = 0;
		public float cycles = 0;
		private AnvilMenu anvilMenu = null;
		private ItemStack output = null;
		private WeakReference<FakePlayer> anvilFp = null;

		public TEExperienceMill(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 6, Component.translatable(Registry.getBlock("experience_mill").getDescriptionId()), Registry.getContainerId("experience_mill"),
					ContainerExperienceMill.class, true, pos, state);
		}

		public TEExperienceMill(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("experience_mill"), pos, state);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {

				if(timer++ == 20) {
					boolean sendUpdates = false;
					timer = 0;

					if(output == null) {

						if(mode == 1) {


							int level;
							float mul;

							switch(getUpgradeAmount(Upgrades.EXP_MILL_LEVEL)) {
							case 3:
								level = 30;
								mul = 2.5f;
								break;
							case 2:
								level = 20;
								mul = 2f;
								break;
							case 1:
								level = 10;
								mul = 1.5f;
								break;
							default:
								level = 5;
								mul = 1f;
							}

							int cx = Math.round(mul * 150);
							float timeMul;

							switch(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED)) {
							case 3:
								cx = Math.round(cx * 1.3f);
								timeMul = 0.25f;
								break;
							case 2:
								cx = Math.round(cx * 1.2f);
								timeMul = 0.5f;
								break;
							case 1:
								cx = Math.round(cx * 1.1f);
								timeMul = 0.75f;
								break;
							default:
								timeMul = 1f;
							}

							float finTime = 10 * mul * timeMul;
							if(tank.getAmount() >= cx) {
								ItemStack encht = ItemStack.EMPTY;
								int n = 0;
								if(contents.get(1).isEnchantable()) {
									encht = EnchantmentHelper.enchantItem(this.getLevel().getRandom(), contents.get(1).copy(), level, false);
									n = 1;
								}

								if(!encht.isEnchanted() && contents.get(2).isEnchantable()) {
									encht = EnchantmentHelper.enchantItem(this.getLevel().getRandom(), contents.get(2).copy(), level, false);
									n = 2;
								}
								if(encht.isEnchanted()) {
									tank.shrink(cx);
									cycles = finTime;
									contents.get(n).shrink(1);
									output = encht;
									sendUpdates = true;
								}
							}

						}else if(mode == 2) {

							Optional<ItemStack> optional = this.getLevel().getRecipeManager().getRecipeFor(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE, this, this.getLevel()).map((recipe) -> recipe.assemble(this));
							if(optional.isPresent()) {
								this.output = optional.get();
								sendUpdates = true;
							}
						}else {
							if(anvilMenu == null) {
								if(anvilFp == null || anvilFp.get() == null) {
									anvilFp = new WeakReference<>(FakePlayerFactory.getMinecraft((ServerLevel) this.getLevel()));
								}
								if(anvilFp != null && anvilFp.get() != null) {
									anvilMenu = new AnvilMenu(-1, anvilFp.get().getInventory());
								}
							}
							if(anvilMenu != null) {
								anvilMenu.setItem(0, 0, contents.get(1));
								anvilMenu.setItem(1, 0, contents.get(2));
								anvilMenu.createResult();
								if(!anvilMenu.getItems().get(2).isEmpty() && tank.getAmount() >= anvilMenu.getCost() * 75) {
									cycles = anvilMenu.getCost() * 7;
									tank.shrink(anvilMenu.getCost() * 75);
									output = anvilMenu.getItems().get(2);
									anvilMenu.setItem(2, 0, ItemStack.EMPTY);
									contents.set(1, ItemStack.EMPTY);
									if(anvilMenu.repairItemCountCost > 0 && !contents.get(2).isEmpty() && contents.get(2).getCount() > anvilMenu.repairItemCountCost) {
										contents.get(2).shrink(anvilMenu.repairItemCountCost);
									}else {
										contents.set(2, ItemStack.EMPTY);
									}
									sendUpdates = true;
								}
							}
						}
					}

					if(output != null) {
						if(progress >= cycles) {
							if(contents.get(0).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(0), output) && contents.get(0).getCount() + output.getCount() <= contents.get(0).getMaxStackSize())){
								if(contents.get(0).isEmpty()) {
									contents.set(0, output);
								}else {
									contents.get(0).grow(output.getCount());
								}
								output = null;
								cycles = 0;
								progress = 0;
								sendUpdates = true;
							}
						}else {

							progress++;
							sendUpdates = true;
						}
					}

					if(output == null && getBlockState().getValue(EXP_MILL_PROP) != 0) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(EXP_MILL_PROP, 0));
						sendUpdates = true;
					}else if(output != null && getBlockState().getValue(EXP_MILL_PROP) != mode) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(EXP_MILL_PROP, (int) mode));
						sendUpdates = true;
					}
					if(sendUpdates) {
						sendUpdates();
					}
				}
			}

		}

		@Override
		public void saveAdditional(CompoundTag compound) {

			compound.putByte("assemblylinemachines:mode", mode);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			CompoundTag sub = new CompoundTag();
			tank.writeToNBT(sub);
			compound.put("assemblylinemachines:tank", sub);

			if(output != null) {
				sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			super.saveAdditional(compound);
		}

		@Override
		public void load(CompoundTag compound) {
			mode = compound.getByte("assemblylinemachines:mode");
			progress = compound.getFloat("assemblylinemachines:progress");
			cycles = compound.getFloat("assemblylinemachines:cycles");

			if(compound.contains("assemblylinemachines:tank")) {
				tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tank"));
			}

			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}
			super.load(compound);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 2) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}else if(slot == 0) {
				return false;
			}else {
				return true;
			}
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == ForgeCapabilities.FLUID_HANDLER) {
				return lazy.cast();
			}
			return super.getCapability(cap, side);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if (cap == ForgeCapabilities.FLUID_HANDLER) {
				return lazy.cast();
			}
			return super.getCapability(cap);
		}

		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 3; i < 6; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
	}

	public static class ContainerExperienceMill extends ContainerALMBase<TEExperienceMill>{

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerExperienceMill(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEExperienceMill.class));
		}

		public ContainerExperienceMill(final int windowId, final Inventory playerInventory, final TEExperienceMill tileEntity) {
			super(Registry.getContainerType("experience_mill"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);



			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 120, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 37, 19, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 37, 48, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 57, tileEntity));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenExperienceMill extends ScreenALMBase<ContainerExperienceMill>{
		TEExperienceMill tsfm;
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		private TrueFalseButton modeB;

		public ScreenExperienceMill(ContainerExperienceMill screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), null, null, "experience_mill", false);
			tsfm = screenContainer.tileEntity;
			this.renderInventoryText = false;
			this.renderTitleText = false;
		}

		@Override
		protected void init() {
			super.init();

			int x = leftPos;
			int y = topPos;

			modeB = this.addRenderableWidget(new TrueFalseButton(x+130, y+56, 11, 11, null, (b) -> sendModeChange(tsfm.getBlockPos())));
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			if(!tsfm.tank.isEmpty() && tsfm.tank.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(tsfm.tank.getFluid());
				if(tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(tsfm.tank.getFluid()).getStillTexture());
				}

				if(tsfm.tank.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				}

				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

				super.blit(x+13, y+13, 57, 57, 57, tas);
			}
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int fprog = Math.round(((float)tsfm.tank.getAmount()/(float)tsfm.handler.getTankCapacity(0)) * 57f);
			super.blit(x+13, y+13, 176, 31, 8, 57 - fprog);

			int add = -1;
			switch(tsfm.mode) {
			case 2:
				add = 11;
				break;
			case 1:
				add = 0;
				break;
			}

			if(add != -1) {
				super.blit(modeB.x, modeB.y, 176, 9 + add, modeB.getWidth(), modeB.getHeight());
			}

			if(tsfm.mode == 1 || tsfm.mode == 3) {
				super.blit(x+37, y+19, 176, 88, 16, 16);
			}

			if(tsfm.mode == 2 || tsfm.mode == 3) {
				if(tsfm.mode == 2) {
					super.blit(x+37, y+19, 176, 120, 16, 16);
				}

				super.blit(x+37, y+48, 176, 104, 16, 16);
			}

			int prog = Math.round((tsfm.progress/tsfm.cycles) * 51f);
			super.blit(x+59, y+37, 176, 0, prog, 9);

		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if (mouseX >= x+13 && mouseY >= y+13 && mouseX <= x+20 && mouseY <= y+69) {
				if (!tsfm.tank.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(tsfm.tank.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(FormattingHelper.FEPT_FORMAT.format(tsfm.tank.getAmount()) + " mB");
					} else {
						str.add(FormattingHelper.FEPT_FORMAT.format(tsfm.tank.getAmount() / 1000D) + " B");
					}
					this.renderComponentTooltip(str, mouseX - x, mouseY - y);
				} else {
					this.renderComponentTooltip("Empty", mouseX - x, mouseY - y);
				}
			}

			if(modeB.isHoveredOrFocused()) {
				switch(tsfm.mode) {
				case 3:
					this.renderComponentTooltip("Anvil Mode", mouseX - x, mouseY - y);
					break;
				case 2:
					this.renderComponentTooltip("Book Mode", mouseX - x, mouseY - y);
					break;
				case 1:
					this.renderComponentTooltip("Enchantment Mode", mouseX - x, mouseY - y);
					break;
				}
			}
		}



	}

	private static void sendModeChange(BlockPos pos) {
		PacketData pd = new PacketData("exp_mill_gui");
		pd.writeBlockPos("pos", pos);

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, Level world) {

		if (pd.getCategory().equals("exp_mill_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
			if (tex instanceof TEExperienceMill) {
				TEExperienceMill te = (TEExperienceMill) tex;
				if (te.mode == 3) {
					te.mode = 1;
				}else {
					te.mode++;
				}
				te.sendUpdates();
			}
		}
	}
}
