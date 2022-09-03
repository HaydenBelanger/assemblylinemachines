package me.haydenb.assemblylinemachines.block.machines;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.IItemHandler;

public class BlockQuarry extends BlockScreenBlockEntity<BlockQuarry.TEQuarry>{

	private static final VoxelShape SHAPE = Block.box(2, 2, 2, 14, 14, 14);

	public BlockQuarry() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "quarry", BlockQuarry.TEQuarry.class);

		BlockState bs = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(StateProperties.MACHINE_ACTIVE, false);
		bs = BlockQuarryAddon.addToBlockState(bs);
		this.registerDefaultState(bs);
	}


	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(HorizontalDirectionalBlock.FACING, StateProperties.MACHINE_ACTIVE);
		BlockQuarryAddon.addToBuilder(builder);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {


		VoxelShape vx = SHAPE;

		for(Direction d : Direction.values()) {

			if(state.getValue(BlockQuarryAddon.getAddonProperty(d))) {
				vx = Shapes.join(vx, BlockQuarryAddon.MAIN_SHAPES.get(d), BooleanOp.OR);

			}

		}

		return vx;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if(world.getBlockState(pos.relative(facing)).getBlock() instanceof BlockQuarryAddon) {
			return state.setValue(BlockQuarryAddon.getAddonProperty(facing), true);
		}else {
			return state.setValue(BlockQuarryAddon.getAddonProperty(facing), false);
		}
	}

	public static class TEQuarry extends ManagedSidedMachine<ContainerQuarry> implements ALMTicker<TEQuarry> {

		//OPERATION
		private int[] min = null;
		private int[] max = null;
		private int[] current = null;
		private ItemStack pending = ItemStack.EMPTY;

		//DATA
		private ServerLevel serverLevel = null;
		private IItemHandler handler = null;

		//SETUP
		private int range = 10;
		private boolean right = true;

		//MISC
		private String status = "";
		private int timer = 0;
		private int nTimer = 16;
		private boolean firstStatusUpdate = false;

		public TEQuarry(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 0, Component.translatable(Registry.getBlock("quarry").getDescriptionId()), Registry.getContainerId("quarry"), ContainerQuarry.class, new EnergyProperties(true, false, 100000), pos, state);
		}

		public TEQuarry(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("quarry"), pos, state);
		}

		@Override
		public void tick() {

			if(!level.isClientSide) {
				boolean sendUpdates = false;
				if(min == null || max == null) {
					status = "Status:\nWaiting for configuration...";
					if(!firstStatusUpdate) {
						firstStatusUpdate = true;
						sendUpdates = true;
					}
				}else {
					if(timer++ == nTimer) {
						timer = 0;


						boolean shutoff = false;
						int cost = 600;
						int upcount = getUpgradeBlockCount(Registry.getBlock("quarry_speed_addon"));

						switch(upcount) {
						case 6:
							nTimer = 1;
							cost = 750;
							break;
						case 5:
							nTimer = 2;
							cost = 750;
							break;
						case 4:
							nTimer = 4;
							cost = 650;
							break;
						case 3:
							nTimer = 6;
							cost = 650;
							break;
						case 2:
							nTimer = 8;
							cost = 600;
							break;
						default:
							nTimer = 16;
						}


						if(handler == null && !getCapability()) {
							status = "Status:\nError\nMissing storage above unit.";
							shutoff = true;
						}else {
							if(!pending.isEmpty()) {
								pending = Utils.attemptDepositIntoAllSlots(pending, handler);
								if(!pending.isEmpty()) {
									status = "Status:\nError\nStorage is full.";
									shutoff = true;
									return;
								}
								sendUpdates = true;
							}

							if(current == null) {
								current = new int[] {min[0], max[1], min[2]};
								sendUpdates = true;
							}

							if(current[1] < min[1]) {
								status = "Status:\nQuarry has finished.";
								shutoff = true;
							}else {


								int fortune = getUpgradeBlockCount(Registry.getBlock("quarry_fortune_addon"));

								switch(fortune) {
								case 6:
									cost = Math.round(cost * 3.5f);
									break;
								case 5:
									cost = Math.round(cost * 3.0f);
									break;
								case 4:
									cost = Math.round(cost * 2.5f);
									break;
								case 3:
									cost = Math.round(cost * 2.0f);
									break;
								case 2:
									cost = Math.round(cost * 1.5f);
									break;
								}

								if(amount - cost >= 0) {
									amount -= cost;
									fept = (float) cost / (float) nTimer;

									BlockState bs = null;
									BlockPos mpos = null;

									int voidup = getUpgradeBlockCount(Registry.getBlock("quarry_void_addon"));
									while(bs == null) {
										if(current[0]++ >= max[0]) {
											current[0] = min[0];
											if(current[2]++ >= max[2]) {
												current[2] = min[2];
												if(current[1]-- <= min[1]) {
													return;
												}
											}
										}
										mpos = new BlockPos(current[0], current[1], current[2]);

										BlockState bsx = this.getLevel().getBlockState(new BlockPos(current[0], current[1], current[2]));

										if(voidup != 0 || bsx.getBlock() != Blocks.DIRT) {
											if(!bsx.isAir() && bsx.getDestroySpeed(this.getLevel(), mpos) < 51 && bsx.getDestroySpeed(this.getLevel(), mpos) != -1) {
												bs = bsx;
											}
										}

									}


									if(serverLevel == null) {
										serverLevel = this.getLevel().getServer().getLevel(this.getLevel().dimension());
									}

									ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
									pick.enchant(Enchantments.BLOCK_FORTUNE, fortune);

									List<ItemStack> stackList = bs.getDrops(new LootContext.Builder(serverLevel).withParameter(LootContextParams.TOOL, pick).withParameter(LootContextParams.ORIGIN, new Vec3(mpos.getX(), mpos.getY(), mpos.getZ())));

									for(ItemStack stack : stackList) {
										pending = Utils.attemptDepositIntoAllSlots(stack, handler);
										if(!pending.isEmpty()) {
											break;
										}
									}


									if(voidup != 0) {
										this.getLevel().setBlockAndUpdate(mpos, Blocks.AIR.defaultBlockState());
										this.getLevel().playSound(null, mpos, bs.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1f, 1f);
									}else {
										this.getLevel().setBlockAndUpdate(mpos, Blocks.DIRT.defaultBlockState());
										this.getLevel().playSound(null, mpos, bs.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1f, 1f);
									}
									status = "Status:\nWorking...\n\n@" + current[0] + ", " + current[1] + ", " + current[2];

									if(!getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
										this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));

									}
									sendUpdates = true;
								}
							}




						}


						if(shutoff) {
							if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
								sendUpdates = true;

							}
						}

					}
				}

				if(sendUpdates) {
					sendUpdates();
				}
			}
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);

			if(compound.contains("assemblylinemachines:min")) {
				min = compound.getIntArray("assemblylinemachines:min");
			}

			if(compound.contains("assemblylinemachines:max")) {
				max = compound.getIntArray("assemblylinemachines:max");
			}

			if(compound.contains("assemblylinemachines:pending")) {
				pending = ItemStack.of(compound.getCompound("assemblylinemachines:pending"));
			}
			if(compound.contains("assemblylinemachines:current")) {
				current = compound.getIntArray("assemblylinemachines:current");
			}

			if(compound.contains("assemblylinemachines:range")) {
				range = compound.getInt("assemblylinemachines:range");
			}

			if(compound.contains("assemblylinemachines:right")) {
				right = compound.getBoolean("assemblylinemachines:right");
			}

			if(compound.contains("assemblylinemachines:status")) {
				status = compound.getString("assemblylinemachines:status");
			}

			if(compound.contains("assemblylinemachines:ntimer")) {
				nTimer = compound.getInt("assemblylinemachines:ntimer");
			}
		}

		@Override
		public void saveAdditional(CompoundTag compound) {

			if(min != null) {
				compound.putIntArray("assemblylinemachines:min", min);
			}

			if(max != null) {
				compound.putIntArray("assemblylinemachines:max", max);
			}


			if(current != null) {
				compound.putIntArray("assemblylinemachines:current", current);
			}

			CompoundTag sub = new CompoundTag();

			pending.save(sub);
			compound.put("assemblylinemachines:pending", sub);
			compound.putInt("assemblylinemachines:range", range);
			compound.putBoolean("assemblylinemachines:right", right);
			compound.putString("assemblylinemachines:status", status);
			compound.putInt("assemblylinemachines:ntimer", nTimer);

			super.saveAdditional(compound);
		}

		public int getUpgradeBlockCount(Block block) {
			int i = 0;
			for(Direction d : Direction.values()) {
				if(this.getLevel().getBlockState(this.getBlockPos().relative(d)).getBlock() == block) {
					i++;
				}
			}

			return i;
		}

		private boolean getCapability() {
			BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(Direction.UP));
			if(te != null) {
				LazyOptional<IItemHandler> cap = te.getCapability(ForgeCapabilities.ITEM_HANDLER,
						Direction.DOWN);
				IItemHandler output = cap.orElse(null);
				if (output != null) {
					TEQuarry ipcte = this;
					cap.addListener(new NonNullConsumer<LazyOptional<IItemHandler>>() {

						@Override
						public void accept(LazyOptional<IItemHandler> t) {
							if (ipcte != null) {
								ipcte.handler = null;
							}
						}
					});

					this.handler = output;
					return true;
				}
			}

			return false;
		}

	}

	public static class ContainerQuarry extends ContainerALMBase<TEQuarry>{

		public ContainerQuarry(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEQuarry.class));
		}

		public ContainerQuarry(final int windowId, final Inventory playerInventory, final TEQuarry tileEntity) {
			super(Registry.getContainerType("quarry"), windowId, tileEntity, playerInventory, null, null, 0, 0);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenQuarry extends ScreenALMEnergyBased<ContainerQuarry>{
		TEQuarry tsfm;

		public ScreenQuarry(ContainerQuarry screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 87), null, null, "quarry", false, new Pair<>(8, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
			renderInventoryText = false;
			renderTitleText = false;

		}

		@Override
		protected void init() {
			super.init();


			int x = this.leftPos;
			int y = this.topPos;

			this.addRenderableWidget(new QuarryButton(x+132, y+46, 8, 8, "Decrement Range", (b) -> sendChangeNum(tsfm.getBlockPos(), false), tsfm));
			this.addRenderableWidget(new QuarryButton(x+158, y+46, 8, 8, "Increment Range", (b) -> sendChangeNum(tsfm.getBlockPos(), true), tsfm));
			this.addRenderableWidget(new QuarryButton(x+145, y+57, 8, 8, "Initialize Quarry", (b) -> sendInitQuarry(tsfm.getBlockPos()), tsfm));
			this.addRenderableWidget(new QuarryButton(x+145, y+46, 176, 52, 8, 8, new TrueFalseButtonSupplier("Change to Left-Oriented", "Change to Right-Oriented", () -> tsfm.right), (b) -> sendDirChange(tsfm.getBlockPos()), tsfm));
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;


			int i = 0;
			int c = 0xffffff;
			for(String s : tsfm.status.split("\n")) {

				float wsc = 92f / this.font.width(s);
				if(wsc > 1f) wsc = 1f;
				if(s.equals("Error")) {
					c = 0xff2626;
				}
				ScreenMath.renderScaledText(this.font, x+33, y+13+(i*9), wsc, s, false, c);
				i++;
			}
			drawCenteredString(this.font, tsfm.range + "", x+148, y+34, 0xffffff);
		}



	}

	private static class QuarryButton extends TrueFalseButton{



		private final TEQuarry te;

		public QuarryButton(int x, int y, int blitx, int blity, int widthIn, int heightIn, TrueFalseButtonSupplier tfbs, OnPress onPress, TEQuarry te) {
			super(x, y, blitx, blity, widthIn, heightIn, tfbs, onPress);
			this.te = te;
		}

		public QuarryButton(int x, int y, int widthIn, int heightIn, String tooltip, OnPress onPress, TEQuarry te) {
			super(x, y, widthIn, heightIn, tooltip, onPress);
			this.te = te;
		}

		@Override
		protected boolean isValidClickButton(int p_230987_1_) {
			return te.min == null || te.max == null;
		}

		@Override
		public void renderToolTip(PoseStack mx, int mouseX, int mouseY) {
			if(te.min == null || te.max == null) {
				super.renderToolTip(mx, mouseX, mouseY);
			}
		}

		@Override
		public int[] getBlitData() {
			if(te.min == null || te.max == null) {
				return super.getBlitData();
			}

			return new int[] {x, y, 131, 4, width, height};
		}

		@Override
		public boolean getSupplierOutput() {
			if(te.min == null || te.max == null) {
				return super.getSupplierOutput();
			}
			return true;
		}

	}

	public static void sendDirChange(BlockPos pos) {

		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "dir");
		pd.writeBlockPos("pos", pos);

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void sendChangeNum(BlockPos pos, boolean incr) {
		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "num");
		pd.writeBlockPos("pos", pos);
		pd.writeBoolean("incr", incr);

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void sendInitQuarry(BlockPos pos) {
		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "init");
		pd.writeBlockPos("pos", pos);

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, Level world) {
		if(pd.getCategory().equals("quarry_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
			if(tex instanceof TEQuarry) {
				TEQuarry te = (TEQuarry) tex;

				String c = pd.get("cat", String.class);

				if(c.equals("dir")) {
					te.right = !te.right;
				}else if(c.equals("num")) {
					Boolean incr = pd.get("incr", Boolean.class);
					if(incr) {
						if(te.range < 30) {
							te.range++;
						}
					}else {
						if(te.range > 5) {
							te.range--;
						}
					}
				}else if(c.equals("init")) {
					Direction offset = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();
					BlockPos posA = te.getBlockPos().relative(offset, 2);

					BlockPos posB;
					if(te.right) {
						posB = posA.relative(offset, te.range).relative(offset.getClockWise(), te.range);
					}else {
						posB = posA.relative(offset, te.range).relative(offset.getCounterClockWise(), te.range);
					}

					int[] min = new int[3];
					int[] max = new int[3];

					if(posA.getX() > posB.getX()) {
						max[0] = posA.getX();
						min[0] = posB.getX();
					}else {
						max[0] = posB.getX();
						min[0] = posA.getX();
					}

					max[1] = posA.getY();
					min[1] = world.getMinBuildHeight() + 1;

					if(posA.getZ() > posB.getZ()) {
						max[2] = posA.getZ();
						min[2] = posB.getZ();
					}else {
						max[2] = posB.getZ();
						min[2] = posA.getZ();
					}

					te.max = max;
					te.min = min;

				}

				te.sendUpdates();
			}
		}
	}
}
