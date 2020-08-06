package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockQuarry extends BlockScreenTileEntity<BlockQuarry.TEQuarry>{

	private static final VoxelShape SHAPE = Block.makeCuboidShape(2, 2, 2, 14, 14, 14);

	public BlockQuarry() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "quarry", BlockQuarry.TEQuarry.class);

		BlockState bs = this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(StateProperties.MACHINE_ACTIVE, false);
		bs = BlockQuarryAddon.addToBlockState(bs);
		this.setDefaultState(bs);
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(HorizontalBlock.HORIZONTAL_FACING, StateProperties.MACHINE_ACTIVE);
		BlockQuarryAddon.addToBuilder(builder);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		
		VoxelShape vx = SHAPE;
		
		for(Direction d : Direction.values()) {
			
			if(state.get(BlockQuarryAddon.getAddonProperty(d))) {
				vx = VoxelShapes.combineAndSimplify(vx, BlockQuarryAddon.getConnectionShape(d), IBooleanFunction.OR);
				
			}
			
		}
		
		return vx;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if(world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockQuarryAddon) {
			return state.with(BlockQuarryAddon.getAddonProperty(facing), true);
		}else {
			return state.with(BlockQuarryAddon.getAddonProperty(facing), false);
		}
	}
	
	public static class TEQuarry extends ManagedSidedMachine<ContainerQuarry> implements ITickableTileEntity {
		
		//OPERATION
		private int[] min = null;
		private int[] max = null;
		private int[] current = null;
		private ItemStack pending = ItemStack.EMPTY;
		
		//DATA
		private ServerWorld serverWorld = null;
		private IItemHandler handler = null;
		
		//SETUP
		private int range = 10;
		private boolean right = true;
		
		//MISC
		private String status = "";
		private int timer = 0;
		private int nTimer = 16;

		public TEQuarry(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 0, new TranslationTextComponent(Registry.getBlock("quarry").getTranslationKey()), Registry.getContainerId("quarry"), ContainerQuarry.class, new EnergyProperties(true, false, 100000));
		}

		public TEQuarry() {
			this(Registry.getTileEntity("quarry"));
		}
		
		@Override
		public void tick() {
			
			if(!world.isRemote) {
				if(min == null || max == null) {
					status = "Status:\nWaiting for configuration...";
				}else {
					if(timer++ == nTimer) {
						timer = 0;
						
						boolean sendUpdates = false;
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
								pending = General.attemptDepositIntoAllSlots(pending, handler);
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
									cost = Math.round((float) cost * 3.5f);
									break;
								case 5:
									cost = Math.round((float) cost * 3.0f);
									break;
								case 4:
									cost = Math.round((float) cost * 2.5f);
									break;
								case 3:
									cost = Math.round((float) cost * 2.0f);
									break;
								case 2:
									cost = Math.round((float) cost * 1.5f);
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
										
										BlockState bsx = world.getBlockState(new BlockPos(current[0], current[1], current[2]));
										
										if(voidup != 0 || bsx.getBlock() != Blocks.DIRT) {
											if(bsx.getBlock() != Blocks.AIR && bsx.getBlockHardness(world, mpos) < 51 && bsx.getBlockHardness(world, mpos) != -1) {
												bs = bsx;
											}
										}
										
									}
									
									
									if(serverWorld == null) {
										serverWorld = world.getServer().getWorld(world.func_234923_W_());
									}
									
									ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
									pick.addEnchantment(Enchantments.FORTUNE, fortune);
									
									List<ItemStack> stackList = bs.getDrops(new LootContext.Builder(serverWorld).withParameter(LootParameters.TOOL, pick).withParameter(LootParameters.POSITION, mpos));
									
									for(ItemStack stack : stackList) {
										pending = General.attemptDepositIntoAllSlots(stack, handler);
										if(!pending.isEmpty()) {
											break;
										}
									}
									
									
									if(voidup != 0) {
										world.setBlockState(mpos, Blocks.AIR.getDefaultState());
										world.playSound(null, mpos, bs.getSoundType().getBreakSound(), SoundCategory.BLOCKS, 1f, 1f);
									}else {
										world.setBlockState(mpos, Blocks.DIRT.getDefaultState());
										world.playSound(null, mpos, bs.getSoundType().getBreakSound(), SoundCategory.BLOCKS, 1f, 1f);
									}
									status = "Status:\nWorking...\n\n@" + current[0] + ", " + current[1] + ", " + current[2];
									
									if(!getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
										world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
										
									}
									sendUpdates = true;
								}
							}
							
							
							
							
						}
						
						
						if(shutoff == true) {
							if(getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
								world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
								sendUpdates = true;
								
							}
						}
						if(sendUpdates) {
							sendUpdates();
						}
					}
				}
			}
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:min")) {
				min = compound.getIntArray("assemblylinemachines:min");
			}
			
			if(compound.contains("assemblylinemachines:max")) {
				max = compound.getIntArray("assemblylinemachines:max");
			}
			
			if(compound.contains("assemblylinemachines:pending")) {
				pending = ItemStack.read(compound.getCompound("assemblylinemachines:pending"));
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
		public CompoundNBT write(CompoundNBT compound) {
			
			if(min != null) {
				compound.putIntArray("assemblylinemachines:min", min);
			}
			
			if(max != null) {
				compound.putIntArray("assemblylinemachines:max", max);
			}
			
			
			if(current != null) {
				compound.putIntArray("assemblylinemachines:current", current);
			}
			
			CompoundNBT sub = new CompoundNBT();
			
			pending.write(sub);
			compound.put("assemblylinemachines:pending", sub);
			compound.putInt("assemblylinemachines:range", range);
			compound.putBoolean("assemblylinemachines:right", right);
			compound.putString("assemblylinemachines:status", status);
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			
			return super.write(compound);
		}

		public int getUpgradeBlockCount(Block block) {
			int i = 0;
			for(Direction d : Direction.values()) {
				if(world.getBlockState(pos.offset(d)).getBlock() == block) {
					i++;
				}
			}
			
			return i;
		}
		
		private boolean getCapability() {
			TileEntity te = world.getTileEntity(pos.offset(Direction.UP));
			if(te != null) {
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
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
		
		public ContainerQuarry(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEQuarry.class));
		}
		
		public ContainerQuarry(final int windowId, final PlayerInventory playerInventory, final TEQuarry tileEntity) {
			super(Registry.getContainerType("quarry"), windowId, tileEntity, playerInventory, null, null, 0, 0);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenQuarry extends ScreenALMEnergyBased<ContainerQuarry>{
		TEQuarry tsfm;
		private final ArrayList<SimpleButton> buttons = new ArrayList<>();
		
		public ScreenQuarry(ContainerQuarry screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 87), null, null, "quarry", false, new Pair<>(8, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
			renderInventoryText = false;
			renderTitleText = false;
			
		}
		
		@Override
		protected void init() {
			super.init();
			
			
			int x = this.guiLeft;
			int y = this.guiTop;
			
			buttons.add(new QuarryButton(tsfm, x+145, y+46, "", (button) -> {
				sendDirChange(tsfm.getPos());
			}));
			
			buttons.add(new QuarryButton(tsfm, x+132, y+46, "Decrement Range", (button) -> {
				sendChangeNum(tsfm.getPos(), false);
			}));
			
			buttons.add(new QuarryButton(tsfm, x+158, y+46, "Increment Range", (button) -> {
				sendChangeNum(tsfm.getPos(), true);
			}));
			
			buttons.add(new QuarryButton(tsfm, x+145, y+57, "Initialize Quarry", (button) -> {
				sendInitQuarry(tsfm.getPos());
			}));
			
			for(SimpleButton b : buttons) {
				this.addButton(b);
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if(tsfm.min == null || tsfm.max == null) {
				for(SimpleButton b : buttons) {
					
					if(mouseX >= b.getX() && mouseX <= b.getX() + 8 && mouseY >= b.getY() && mouseY <= b.getY() + 8) {
						if(!b.getMessage().trim().equals("")) {
							this.renderTooltip(b.getMessage(), mouseX - x, mouseY - y);
						}else {
							if(tsfm.right == true) {
								this.renderTooltip("Change to Left-Oriented", mouseX - x, mouseY - y);
							}else {
								this.renderTooltip("Change to Right-Oriented", mouseX - x, mouseY - y);
							}
						}
						
					}
				}
			}
			
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if(tsfm.min == null || tsfm.max == null) {
				this.blit(x+132, y+46, 176, 60, 34, 19);
				
				if(tsfm.right == true) {
					this.blit(x+145, y+46, 176, 52, 8, 8);
				}
			}
			
			
			int i = 0;
			int c = 0xffffff;
			for(String s : tsfm.status.split("\n")) {
				
				float wsc = 92f / (float) this.font.getStringWidth(s);
				if(wsc > 1f) wsc = 1f;
				if(s.equals("Error")) {
					c = 0xff2626;
				}
				MathHelper.renderScaledText(this.font, x+33, y+13+(i*9), wsc, s, false, c);
				i++;
			}
			drawCenteredString(this.font, tsfm.range + "", x+148, y+34, 0xffffff);
		}
		
		
		
	}
	
	private static class QuarryButton extends SimpleButton{

		

		private final TEQuarry te;
		
		public QuarryButton(TEQuarry te, int widthIn, int heightIn, String text, IPressable onPress) {
			super(widthIn, heightIn, text, onPress);
			this.te = te;
		}
		
		@Override
		protected boolean func_230987_a_(int p_230987_1_) {
			return te.min == null || te.max == null;
		}
		
	}
	
	public static void sendDirChange(BlockPos pos) {
		
		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "dir");
		pd.writeBlockPos("pos", pos);
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	public static void sendChangeNum(BlockPos pos, boolean incr) {
		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "num");
		pd.writeBlockPos("pos", pos);
		pd.writeBoolean("incr", incr);
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	public static void sendInitQuarry(BlockPos pos) {
		PacketData pd = new PacketData("quarry_gui");
		pd.writeString("cat", "init");
		pd.writeBlockPos("pos", pos);
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	public static void updateDataFromPacket(PacketData pd, World world) {
		if(pd.getCategory().equals("quarry_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			TileEntity tex = world.getTileEntity(pos);
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
					Direction offset = te.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).getOpposite();
					BlockPos posA = te.getPos().offset(offset, 2);
					
					BlockPos posB;
					if(te.right) {
						posB = posA.offset(offset, te.range).offset(offset.rotateY(), te.range);
					}else {
						posB = posA.offset(offset, te.range).offset(offset.rotateYCCW(), te.range);
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
					min[1] = 1;
					
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
