package me.haydenb.assemblylinemachines.block.energy;

import java.util.HashMap;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.TEContainingBlock.GUIContainingBasicBlock;
import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.Utils.Pair;
import me.haydenb.assemblylinemachines.util.Utils.SimpleButton;
import me.haydenb.assemblylinemachines.util.Utils.SupplierWrapper;
import me.haydenb.assemblylinemachines.util.machines.ALMMachineEnergyBased.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.util.machines.ALMManagedSidedMachineBlock;
import me.haydenb.assemblylinemachines.util.machines.ALMManagedSidedMachineBlock.ManagedDirection;
import me.haydenb.assemblylinemachines.util.machines.AbstractALMMachine.ContainerALMBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class BlockBasicBatteryCell extends GUIContainingBasicBlock<BlockBasicBatteryCell.TEBasicBatteryCell> {

	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(10, 3, 0, 12, 13, 2),
			Block.makeCuboidShape(4, 3, 0, 6, 13, 2), Block.makeCuboidShape(2, 5, 3, 2, 11, 13),
			Block.makeCuboidShape(14, 5, 3, 14, 11, 13), Block.makeCuboidShape(4, 2, 1, 6, 3, 2),
			Block.makeCuboidShape(10, 2, 1, 12, 3, 2), Block.makeCuboidShape(10, 13, 1, 12, 14, 2),
			Block.makeCuboidShape(4, 13, 1, 6, 14, 2), Block.makeCuboidShape(2, 2, 2, 14, 14, 2),
			Block.makeCuboidShape(7, 2, 0, 9, 14, 2), Block.makeCuboidShape(12, 7, 1, 13, 9, 2),
			Block.makeCuboidShape(9, 7, 1, 10, 9, 2), Block.makeCuboidShape(6, 7, 1, 7, 9, 2),
			Block.makeCuboidShape(3, 7, 1, 4, 9, 2), Block.makeCuboidShape(0, 0, 13, 16, 16, 16),
			Block.makeCuboidShape(0, 0, 0, 16, 2, 13), Block.makeCuboidShape(0, 14, 0, 16, 16, 13),
			Block.makeCuboidShape(0, 2, 3, 2, 5, 13), Block.makeCuboidShape(14, 2, 3, 16, 5, 13),
			Block.makeCuboidShape(0, 11, 3, 2, 14, 13), Block.makeCuboidShape(14, 11, 3, 16, 14, 13),
			Block.makeCuboidShape(0, 2, 0, 3, 14, 3), Block.makeCuboidShape(13, 2, 0, 16, 14, 3),
			Block.makeCuboidShape(14, 5, 4, 15, 11, 5), Block.makeCuboidShape(14, 5, 6, 15, 11, 7),
			Block.makeCuboidShape(14, 5, 9, 15, 11, 10), Block.makeCuboidShape(14, 5, 11, 15, 11, 12),
			Block.makeCuboidShape(1, 5, 6, 2, 11, 7), Block.makeCuboidShape(1, 5, 4, 2, 11, 5),
			Block.makeCuboidShape(1, 5, 9, 2, 11, 10), Block.makeCuboidShape(1, 5, 11, 2, 11, 12)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);

	public BlockBasicBatteryCell() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "basic_battery_cell", null, true,
				Direction.NORTH, TEBasicBatteryCell.class);
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING,
				context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
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
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	public static class TEBasicBatteryCell extends ALMManagedSidedMachineBlock<ContainerBasicBatteryCell> implements ITickableTileEntity {

		private int fept = 200;
		private boolean autoIn = true;
		private int timer = 0;
		public TEBasicBatteryCell(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 0, "Basic Battery Cell", Registry.getContainerId("basic_battery_cell"),
					ContainerBasicBatteryCell.class, new EnergyProperties(true, true, 250000));
		}

		public TEBasicBatteryCell() {
			this(Registry.getTileEntity("basic_battery_cell"));
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap != CapabilityEnergy.ENERGY || side == getBlockState().get(HorizontalBlock.HORIZONTAL_FACING)) {
				return LazyOptional.empty();
			}
			
			return super.getCapability(cap, side);
		}

		private HashMap<Direction, IEnergyStorage> caps = new HashMap<>();
		
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:fept")) {
				fept = compound.getInt("assemblylinemachines:fept");
			}
			if(compound.contains("assemblylinemachines:in")) {
				autoIn = compound.getBoolean("assemblylinemachines:in");
			}
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			
			compound.putInt("assemblylinemachines:fept", fept);
			compound.putBoolean("assemblylinemachines:in", autoIn);
			return super.write(compound);
		}
		
		@Override
		public void tick() {
			if(!world.isRemote) {
				if(timer++ == 5) {
					timer = 0;
					for(Direction d : Direction.values()) {
						IEnergyStorage storage = caps.get(d);
						if(storage == null) {
							TileEntity te = world.getTileEntity(pos.offset(d));
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

	}

	public static class ContainerBasicBatteryCell extends ContainerALMBase<TEBasicBatteryCell> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerBasicBatteryCell(final int windowId, final PlayerInventory playerInventory,
				final TEBasicBatteryCell tileEntity) {
			super(Registry.getContainerType("basic_battery_cell"), windowId, 0, tileEntity, playerInventory,
					PLAYER_INV_POS, PLAYER_HOTBAR_POS);
		}

		public ContainerBasicBatteryCell(final int windowId, final PlayerInventory playerInventory,
				final PacketBuffer data) {
			this(windowId, playerInventory, Utils.getTileEntity(playerInventory, data, TEBasicBatteryCell.class));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenBasicBatteryCell extends ScreenALMEnergyBased<ContainerBasicBatteryCell> {

		TEBasicBatteryCell tsfm;
		private final HashMap<String, Pair<SimpleButton, SupplierWrapper>> b;

		public ScreenBasicBatteryCell(ContainerBasicBatteryCell screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73),
					"basic_battery_cell", false, new Pair<>(75, 17), screenContainer.tileEntity);
			tsfm = screenContainer.tileEntity;
			b = new HashMap<>();
		}

		@Override
		protected void init() {
			super.init();
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			b.put("up", new Pair<>(new SimpleButton(x + 51, y + 28, 177, 83, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "up");
			}), new SupplierWrapper("Top Face Enabled", "Top Face Disabled",
					() -> tsfm.getDirectionEnabled(ManagedDirection.TOP))));
			b.put("down", new Pair<>(new SimpleButton(x + 51, y + 50, 177, 73, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "down");
			}), new SupplierWrapper("Bottom Face Enabled", "Bottom Face Disabled",
					() -> tsfm.getDirectionEnabled(ManagedDirection.BOTTOM))));
			b.put("left", new Pair<>(new SimpleButton(x + 40, y + 39, 177, 103, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "left");
			}), new SupplierWrapper("Left Face Enabled", "Left Face Disabled",
					() -> tsfm.getDirectionEnabled(ManagedDirection.LEFT))));
			b.put("right", new Pair<>(new SimpleButton(x + 62, y + 39, 177, 93, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "right");
			}), new SupplierWrapper("Right Face Enabled", "Right Face Disabled",
					() -> tsfm.getDirectionEnabled(ManagedDirection.RIGHT))));
			b.put("back", new Pair<>(new SimpleButton(x + 51, y + 39, 177, 63, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "back");
			}), new SupplierWrapper("Back Face Enabled", "Back Face Disabled",
					() -> tsfm.getDirectionEnabled(ManagedDirection.BACK))));
			b.put("automode", new Pair<>(new SimpleButton(x + 95, y + 16, 177, 53, null, (button) -> {

				sendCellUpdatePacket(tsfm.getPos(), "automode");
			}), new SupplierWrapper("Auto-Input Enabled", "Auto-Output Enabled",
					() -> tsfm.autoIn)));
			b.put("prioritydown", new Pair<>(new SimpleButton(x + 95, y + 38, "Decrease Throughput", (button) -> {
				sendCellUpdatePacket(tsfm.getPos(), "feptdown", Screen.hasShiftDown(), Screen.hasControlDown());

			}), null));
			b.put("priorityup", new Pair<>(new SimpleButton(x + 143, y + 38, "Increase Throughput", (button) -> {
				sendCellUpdatePacket(tsfm.getPos(), "feptup", Screen.hasShiftDown(), Screen.hasControlDown());

			}), null));

			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				this.addButton(bb.x);
			}
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				if (mouseX >= bb.x.x && mouseX <= bb.x.x + 8 && mouseY >= bb.x.y && mouseY <= bb.x.y + 8) {
					if (bb.y != null) {
						this.renderTooltip(bb.y.getTextFromSupplier(), mouseX - x, mouseY - y);
					} else {
						this.renderTooltip(bb.x.getMessage(), mouseX - x, mouseY - y);
					}

					break;
				}
			}
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				if (bb.y != null && bb.y.supplier.get()) {
					super.blit(bb.x.x, bb.x.y, bb.x.blitx, bb.x.blity, 8, 8);
				}

			}
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			this.drawCenteredString(this.font, Utils.FORMAT.format(tsfm.fept), x + 122, y + 38, 0xffffff);
		}

		public static void sendCellUpdatePacket(BlockPos pos, String button) {
			PacketData pd = new PacketData("battery_cell_gui");
			pd.writeBlockPos("location", pos);
			pd.writeString("button", button);
			HashPacketImpl.INSTANCE.sendToServer(pd);
		}
		
		public static void sendCellUpdatePacket(BlockPos pos, String button, Boolean shifting, Boolean ctrling) {
			PacketData pd = new PacketData("battery_cell_gui");
			pd.writeBlockPos("location", pos);
			pd.writeString("button", button);
			pd.writeBoolean("shifting", shifting);
			pd.writeBoolean("ctrling", ctrling);
			HashPacketImpl.INSTANCE.sendToServer(pd);
		}

		public static void updateDataFromPacket(PacketData pd, World world) {
			if (pd.getCategory().equals("battery_cell_gui")) {
				BlockPos pos = pd.get("location", BlockPos.class);
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof TEBasicBatteryCell) {
					TEBasicBatteryCell tebbc = (TEBasicBatteryCell) te;

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
						}else if(bl == true) {
							lim = 50;
						}else if(cr == true){
							lim = 200;
						}else {
							lim = 100;
						}
						
						if((tebbc.fept + lim) > 2000) {
							tebbc.fept = 2000;
						}else {
							tebbc.fept += lim;
						}
					}else if (b.equals("feptdown")) {
						Boolean bl = pd.get("shifting", Boolean.class);
						Boolean cr = pd.get("ctrling", Boolean.class);
						
						int lim;
						if(bl == true && cr == true) {
							lim = 1;
						}else if(bl == true) {
							lim = 50;
						}else if(cr == true){
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
					tebbc.getWorld().notifyNeighbors(pos, tebbc.getBlockState().getBlock());
				}
			}
		}

	}
}
