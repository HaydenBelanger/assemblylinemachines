package me.haydenb.assemblylinemachines.block.energy;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine.ManagedDirection;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class BlockBatteryCell extends BlockScreenBlockEntity<BlockBatteryCell.TEBatteryCell> {

	private static final VoxelShape SHAPE_N = Stream.of(Block.box(10, 3, 0, 12, 13, 2),
			Block.box(4, 3, 0, 6, 13, 2), Block.box(2, 5, 3, 2, 11, 13),
			Block.box(14, 5, 3, 14, 11, 13), Block.box(4, 2, 1, 6, 3, 2),
			Block.box(10, 2, 1, 12, 3, 2), Block.box(10, 13, 1, 12, 14, 2),
			Block.box(4, 13, 1, 6, 14, 2), Block.box(2, 2, 2, 14, 14, 2),
			Block.box(7, 2, 0, 9, 14, 2), Block.box(12, 7, 1, 13, 9, 2),
			Block.box(9, 7, 1, 10, 9, 2), Block.box(6, 7, 1, 7, 9, 2),
			Block.box(3, 7, 1, 4, 9, 2), Block.box(0, 0, 13, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 13), Block.box(0, 14, 0, 16, 16, 13),
			Block.box(0, 2, 3, 2, 5, 13), Block.box(14, 2, 3, 16, 5, 13),
			Block.box(0, 11, 3, 2, 14, 13), Block.box(14, 11, 3, 16, 14, 13),
			Block.box(0, 2, 0, 3, 14, 3), Block.box(13, 2, 0, 16, 14, 3),
			Block.box(14, 5, 4, 15, 11, 5), Block.box(14, 5, 6, 15, 11, 7),
			Block.box(14, 5, 9, 15, 11, 10), Block.box(14, 5, 11, 15, 11, 12),
			Block.box(1, 5, 6, 2, 11, 7), Block.box(1, 5, 4, 2, 11, 5),
			Block.box(1, 5, 9, 2, 11, 10), Block.box(1, 5, 11, 2, 11, 12)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);

	private final BatteryCellStats bcs;

	public BlockBatteryCell(BatteryCellStats bcs) {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).sound(SoundType.METAL), "battery_cell", null, true,
				Direction.NORTH, TEBatteryCell.class);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(StateProperties.BATTERY_PERCENT_STATE, 0));
		this.bcs = bcs;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
				context.getHorizontalDirection().getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
		if (d == Direction.WEST) {
			return SHAPE_W;
		} else if (d == Direction.SOUTH) {
			return SHAPE_S;
		} else if (d == Direction.EAST) {
			return SHAPE_E;
		} else {
			return SHAPE_N;
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING).add(StateProperties.BATTERY_PERCENT_STATE);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();

		if(level.getBlockEntity(pos) instanceof TEBatteryCell cell && !nbt.isEmpty()) {
			cell.amount = nbt.getInt("assemblylinemachines:stored");
			if(nbt.contains("assemblylinemachines:fptout")) cell.fept = nbt.getInt("assemblylinemachines:fptout");
			cell.autoIn = nbt.getBoolean("assemblylinemachines:in");
			cell.creative = nbt.getBoolean("assemblylinemachines:creative");
			cell.sendUpdates();
		}
		super.setPlacedBy(level, pos, state, placer, stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip,
			TooltipFlag flag) {
		if(stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			if(nbt.contains("assemblylinemachines:stored")) tooltip.add(1, Component.literal("This Cell has " + FormattingHelper.formatToSuffix(nbt.getInt("assemblylinemachines:stored")) + " FE stored.").withStyle(ChatFormatting.GREEN));
			if(nbt.contains("assemblylinemachines:creative")) tooltip.add(1, Component.literal("This Cell is modified to be creative.").withStyle(ChatFormatting.DARK_PURPLE));
		}
		super.appendHoverText(stack, level, tooltip, flag);
	}

	public static enum BatteryCellStats{

		BASIC(2500000, 2000, 2000), ADVANCED(50000000, 25000, 25000), ULTIMATE(500000000, 75000, 75000);

		public final int capacity;
		public final int maxfept;
		public final int deffept;

		BatteryCellStats(int capacity, int maxfept, int deffept){
			this.capacity = capacity;
			this.maxfept = maxfept;
			this.deffept = deffept;
		}
	}

	public static class TEBatteryCell extends ManagedSidedMachine<ContainerBatteryCell> implements ALMTicker<TEBatteryCell> {

		private int fept;
		private boolean autoIn = true;
		private int timer = 0;
		private final int mx;
		public boolean creative = false;

		public TEBatteryCell(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 0, Component.translatable(state.getBlock().getDescriptionId()), Registry.getContainerId("battery_cell"),
					ContainerBatteryCell.class, new EnergyProperties(true, true, ((BlockBatteryCell) state.getBlock()).bcs.capacity), pos, state);
			BlockBatteryCell bbc = (BlockBatteryCell) state.getBlock();

			this.mx = bbc.bcs.maxfept;
			this.fept = bbc.bcs.deffept;
		}

		public TEBatteryCell(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("battery_cell"), pos, state);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap != CapabilityEnergy.ENERGY || side == getBlockState().getValue(HorizontalDirectionalBlock.FACING)) {
				return LazyOptional.empty();
			}

			return super.getCapability(cap, side);
		}

		private HashMap<Direction, IEnergyStorage> caps = new HashMap<>();

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			if(compound.contains("assemblylinemachines:fptout")) {
				fept = compound.getInt("assemblylinemachines:fptout");
			}
			if(compound.contains("assemblylinemachines:in")) {
				autoIn = compound.getBoolean("assemblylinemachines:in");
			}
			creative = compound.getBoolean("assemblylinemachines:creative");
		}

		@Override
		public void saveAdditional(CompoundTag compound) {

			compound.putInt("assemblylinemachines:fptout", fept);
			compound.putBoolean("assemblylinemachines:in", autoIn);
			if(creative) compound.putBoolean("assemblylinemachines:creative", creative);
			super.saveAdditional(compound);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == 5) {
					timer = 0;
					for(Direction d : Direction.values()) {
						IEnergyStorage storage = caps.get(d);
						if(storage == null) {
							BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(d));
							if(te != null) {
								LazyOptional<IEnergyStorage> lazy = te.getCapability(CapabilityEnergy.ENERGY, d.getOpposite());
								storage = lazy.orElse(null);
								if(storage != null) {
									lazy.addListener(new NonNullConsumer<LazyOptional<IEnergyStorage>>() {

										@Override
										public void accept(LazyOptional<IEnergyStorage> t) {
											caps.remove(d);

										}
									});
									caps.put(d, storage);
								}
							}
						}

						if(storage != null) {
							LazyOptional<IEnergyStorage> schX = this.getCapability(CapabilityEnergy.ENERGY, d);
							IEnergyStorage sch = schX.orElse(null);

							if(sch != null) {
								if(!autoIn) {

									int max = sch.extractEnergy(fept * 5, true);
									int rs = storage.receiveEnergy(max, false);

									if(rs != 0) {
										sch.extractEnergy(rs, false);
										break;
									}
								}else {
									int max = sch.receiveEnergy(fept * 5, true);
									int rs = storage.extractEnergy(max, false);

									if(rs != 0) {
										sch.receiveEnergy(rs, false);
										break;
									}
								}
							}

						}
					}
				}
			}

		}

		public void recalcBattery(){

			if(getBlockState().hasProperty(StateProperties.BATTERY_PERCENT_STATE)) {
				double div = (double) amount / (double) properties.getCapacity();
				if(div > 0.95d) div = 1d;
				int fx = (int) Math.floor(div * 4d);
				if(fx > 4) fx = 4;
				if(fx < 0) fx = 0;
				if(getBlockState().getValue(StateProperties.BATTERY_PERCENT_STATE) != fx) {
					getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StateProperties.BATTERY_PERCENT_STATE, fx));
				}
			}
		}

		public static void updateDataFromPacket(PacketData pd, Level world) {
			if (pd.getCategory().equals("battery_cell_gui")) {
				BlockPos pos = pd.get("location", BlockPos.class);
				BlockEntity te = world.getBlockEntity(pos);
				if (te != null && te instanceof TEBatteryCell) {
					TEBatteryCell tebbc = (TEBatteryCell) te;

					String b = pd.get("button", String.class);

					if (b.equals("up")) {
						ManagedDirection mdir = ManagedDirection.TOP;
						tebbc.setDirection(mdir, !tebbc.getDirectionEnabled(mdir));
					} else if (b.equals("down")) {
						ManagedDirection mdir = ManagedDirection.BOTTOM;
						tebbc.setDirection(mdir, !tebbc.getDirectionEnabled(mdir));
					} else if (b.equals("left")) {
						ManagedDirection mdir = ManagedDirection.LEFT;
						tebbc.setDirection(mdir, !tebbc.getDirectionEnabled(mdir));
					} else if (b.equals("right")) {
						ManagedDirection mdir = ManagedDirection.RIGHT;
						tebbc.setDirection(mdir, !tebbc.getDirectionEnabled(mdir));
					} else if (b.equals("back")) {
						ManagedDirection mdir = ManagedDirection.BACK;
						tebbc.setDirection(mdir, !tebbc.getDirectionEnabled(mdir));
					}else if (b.equals("feptup")) {
						Boolean bl = pd.get("shifting", Boolean.class);
						Boolean cr = pd.get("ctrling", Boolean.class);

						int lim;
						if(bl == true && cr == true) {
							lim = 1;
						}else if(bl) {
							lim = 50;
						}else if(cr){
							lim = 200;
						}else {
							lim = 100;
						}

						if((tebbc.fept + lim) > tebbc.mx) {
							tebbc.fept = tebbc.mx;
						}else {
							tebbc.fept += lim;
						}
					}else if (b.equals("feptdown")) {
						Boolean bl = pd.get("shifting", Boolean.class);
						Boolean cr = pd.get("ctrling", Boolean.class);

						int lim;
						if(bl == true && cr == true) {
							lim = 1;
						}else if(bl) {
							lim = 50;
						}else if(cr){
							lim = 200;
						}else {
							lim = 100;
						}

						if((tebbc.fept - lim) < 0) {
							tebbc.fept = 0;
						}else {
							tebbc.fept -= lim;
						}
					}else if (b.equals("automode")) {
						tebbc.autoIn = !tebbc.autoIn;
					}

					tebbc.sendUpdates();
					tebbc.getLevel().updateNeighborsAt(pos, tebbc.getBlockState().getBlock());
				}
			}
		}

	}

	public static class ContainerBatteryCell extends ContainerALMBase<TEBatteryCell> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerBatteryCell(final int windowId, final Inventory playerInventory,
				final TEBatteryCell tileEntity) {
			super(Registry.getContainerType("battery_cell"), windowId, tileEntity, playerInventory,
					PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
		}

		public ContainerBatteryCell(final int windowId, final Inventory playerInventory,
				final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEBatteryCell.class));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenBatteryCell extends ScreenALMEnergyBased<ContainerBatteryCell> {

		TEBatteryCell tsfm;

		public ScreenBatteryCell(ContainerBatteryCell screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73),
					"battery_cell", false, new Pair<>(75, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}


		@Override
		protected void init() {
			super.init();

			int x = this.leftPos;
			int y = this.topPos;

			this.addRenderableWidget(new TrueFalseButton(x+51, y+28, 177, 83, 8, 8, new TrueFalseButtonSupplier("Top Face Enabled", "Top Face Disabled", () -> tsfm.getDirectionEnabled(ManagedDirection.TOP)), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "up")));
			this.addRenderableWidget(new TrueFalseButton(x+51, y+50, 177, 73, 8, 8, new TrueFalseButtonSupplier("Bottom Face Enabled", "Bottom Face Disabled", () -> tsfm.getDirectionEnabled(ManagedDirection.BOTTOM)), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "down")));
			this.addRenderableWidget(new TrueFalseButton(x+40, y+39, 177, 103, 8, 8, new TrueFalseButtonSupplier("Left Face Enabled", "Left Face Disabled", () -> tsfm.getDirectionEnabled(ManagedDirection.LEFT)), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "left")));
			this.addRenderableWidget(new TrueFalseButton(x+62, y+39, 177, 93, 8, 8, new TrueFalseButtonSupplier("Right Face Enabled", "Right Face Disabled", () -> tsfm.getDirectionEnabled(ManagedDirection.RIGHT)), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "right")));
			this.addRenderableWidget(new TrueFalseButton(x+51, y+39, 177, 63, 8, 8, new TrueFalseButtonSupplier("Back Face Enabled", "Back Face Disabled", () -> tsfm.getDirectionEnabled(ManagedDirection.BACK)), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "back")));
			this.addRenderableWidget(new TrueFalseButton(x+95, y+16, 177, 53, 8, 8, new TrueFalseButtonSupplier("Auto-Input Enabled", "Auto-Input Disabled", () -> tsfm.autoIn), (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "automode")));
			this.addRenderableWidget(new TrueFalseButton(x+95, y+38, 8, 8, "Decrease Automatic Throughput", (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "feptdown", Screen.hasShiftDown(), Screen.hasControlDown())));
			this.addRenderableWidget(new TrueFalseButton(x+143, y+38, 8, 8, "Increase Automatic Throughput", (b) -> sendCellUpdatePacket(tsfm.getBlockPos(), "feptup", Screen.hasShiftDown(), Screen.hasControlDown())));
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			this.drawCenteredString(this.font, FormattingHelper.GENERAL_FORMAT.format(tsfm.fept), x + 122, y + 38, 0xffffff);
		}

		public static void sendCellUpdatePacket(BlockPos pos, String button) {
			PacketData pd = new PacketData("battery_cell_gui");
			pd.writeBlockPos("location", pos);
			pd.writeString("button", button);
			PacketHandler.INSTANCE.sendToServer(pd);
		}

		public static void sendCellUpdatePacket(BlockPos pos, String button, Boolean shifting, Boolean ctrling) {
			PacketData pd = new PacketData("battery_cell_gui");
			pd.writeBlockPos("location", pos);
			pd.writeString("button", button);
			pd.writeBoolean("shifting", shifting);
			pd.writeBoolean("ctrling", ctrling);
			PacketHandler.INSTANCE.sendToServer(pd);
		}

	}
}
