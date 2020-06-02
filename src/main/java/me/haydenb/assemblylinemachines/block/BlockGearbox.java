package me.haydenb.assemblylinemachines.block;

import java.util.Random;

import me.haydenb.assemblylinemachines.item.ItemGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.ALMMachineNoExtract;
import me.haydenb.assemblylinemachines.util.AbstractALMMachine;
import me.haydenb.assemblylinemachines.util.ICrankableMachine;
import me.haydenb.assemblylinemachines.util.TEContainingBlock.GUIContainingBasicBlock;
import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.AbstractALMMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.util.AbstractALMMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.util.Utils.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;

public class BlockGearbox extends GUIContainingBasicBlock<BlockGearbox.TEGearbox> {

	private static final Random RAND = new Random();
	
	public BlockGearbox() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1f, 2f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "gearbox", TEGearbox.class);
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(Utils.MACHINE_ACTIVE, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(HorizontalBlock.HORIZONTAL_FACING).add(Utils.MACHINE_ACTIVE);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!worldIn.isRemote()) {
			if (facing == stateIn.get(HorizontalBlock.HORIZONTAL_FACING)) {
				if (worldIn.getBlockState(currentPos.offset(facing)).getBlock() == Blocks.AIR) {
					return Blocks.AIR.getDefaultState();
				}
			}
		}

		return stateIn;
	}
	
	public static class TEGearbox extends ALMMachineNoExtract<ContainerGearbox> implements ITickableTileEntity{
		
		public int timer = 0;
		public float maxBurnTime = 0;
		public float burnTime = 0;
		public int nTimer = 40;
		public ICrankableMachine mch;
		public TEGearbox(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 2, "Gearbox", Registry.getContainerId("gearbox"), ContainerGearbox.class);
		}
		
		public TEGearbox() {
			this(Registry.getTileEntity("gearbox"));
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putFloat("assemblylinemachines:maxburntime", maxBurnTime);
			compound.putFloat("assemblylinemachines:burntime", burnTime);
			compound.putInt("assemblylinemachines:prevtimer", nTimer);
			return super.write(compound);
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:maxburntime")) {
				maxBurnTime = compound.getFloat("assemblylinemachines:maxburntime");
			}
			if(compound.contains("assemblylinemachines:burntime")) {
				burnTime = compound.getFloat("assemblylinemachines:burntime");
			}
			if(compound.contains("assemblylinemachines:prevtimer")) {
				nTimer = compound.getInt("assemblylinemachines:prevtimer");
			}
		}
		@Override
		public void tick() {
			
			if(!world.isRemote) {
				if(timer++ == nTimer) {
					
					boolean machineValid = true;
					if(mch == null) {
						TileEntity te = world.getTileEntity(pos.offset(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING)));
						if(te != null && te instanceof ICrankableMachine) {
							mch = (ICrankableMachine) te;
						}else {
							machineValid = false;
						}
					}
					
					if(machineValid == true) {
						boolean sendUpdate = false;
						timer = 0;
						
						Upgrades u = Upgrades.match(contents.get(0));
						int mul;
						switch (u) {
						case POWER:
							nTimer = 20;
							mul = 120;
							break;
						case EFFICIENCY:
							nTimer = 80;
							mul = 20;
							break;
						default:
							nTimer = 40;
							mul = 40;
							break;
						}
						
						if(burnTime != 0) {
							
							
							if(mch.perform()) {
								burnTime = burnTime - mul;
								sendUpdate = true;
								world.playSound(null, pos, SoundEvents.BLOCK_WOOD_STEP, SoundCategory.BLOCKS, 0.4f, 1f + getPitchNext());
								
							}else {
								if(u != Upgrades.LIMITER) {
									burnTime = burnTime - mul;
									sendUpdate = true;
									world.playSound(null, pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.4f, 1f);
								}
							}
							
							
							
						}
						
						if(burnTime <= 0) {
							
							if(contents.get(1) != ItemStack.EMPTY) {
								burnTime = ForgeHooks.getBurnTime(contents.get(1));
								maxBurnTime = burnTime;
								contents.get(1).shrink(1);
								if(getBlockState().get(Utils.MACHINE_ACTIVE) == false) {
									world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, true));
									
								}
								sendUpdate = true;
							}else {
								if(maxBurnTime != 0) {
									burnTime = 0;
									maxBurnTime = 0;
									if(getBlockState().get(Utils.MACHINE_ACTIVE) == true) {
										world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, false));
										
									}
									sendUpdate = true;
								}
							}
						}
						
						if(sendUpdate) {
							sendUpdates();
						}
					}
					
				}
			}
			
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot == 0) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
			}else {
				if(Upgrades.match(contents.get(0)) == Upgrades.COMPATABILITY) {
					if(ForgeHooks.getBurnTime(stack) != 0) {
						return true;
					}
				}else {
					if(stack.getItem() instanceof ItemGearboxFuel) {
						return true;
					}
				}
				
			}
			return false;
		}
		
		
		@Override
		public NonNullList<ItemStack> getItems() {
			if(contents.get(1) != ItemStack.EMPTY && contents.get(1).getItem() != Items.AIR && !(contents.get(1).getItem() instanceof ItemGearboxFuel)) {
				if(Upgrades.match(contents.get(0)) != Upgrades.COMPATABILITY) {
					Utils.spawnItem(contents.get(1), pos.up(), world);
					contents.set(1, ItemStack.EMPTY);
					burnTime = 0;
					sendUpdates();
				}
			}
			return super.getItems();
		}
	}
	
	public static class ContainerGearbox extends ContainerALMBase<TEGearbox>{
		
		private static final Pair<Integer, Integer> UPGRADE_POS = new Pair<>(55, 34);
		private static final Pair<Integer, Integer> INPUT_POS = new Pair<>(75, 34);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerGearbox(final int windowId, final PlayerInventory playerInventory, final TEGearbox tileEntity) {
			super(Registry.getContainerType("gearbox"), windowId, 2, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			this.addSlot(new AbstractALMMachine.SlotWithRestrictions(this.tileEntity, 0, UPGRADE_POS.x, UPGRADE_POS.y, tileEntity, 1));
			this.addSlot(new AbstractALMMachine.SlotWithRestrictions(this.tileEntity, 1, INPUT_POS.x, INPUT_POS.y, tileEntity, 64));
		}
		
		
		public ContainerGearbox(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, Utils.getTileEntity(playerInventory, data, TEGearbox.class));
		}
		
		
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenGearbox extends ScreenALMBase<ContainerGearbox>{
		
		TEGearbox tsfm;
		
		public ScreenGearbox(ContainerGearbox screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "gearbox", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			if(tsfm.maxBurnTime > 0) {
				int x = (this.width - this.xSize) / 2;
				int y = (this.height - this.ySize) / 2;
				super.blit(x+29, y+33, 176, 0, 18, 18);
				super.blit(x+119, y+33, 176, 0, 18, 18);
				
				int prog = Math.round((tsfm.burnTime/tsfm.maxBurnTime) * 13f);
				super.blit(x+77, y+18 + (13 - prog), 176, 18 + (13 - prog), 13, prog);
			}
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		}
	}
	
	private static float getPitchNext() {
		float f = RAND.nextFloat();
		
		if(f < 0.6f) {
			f = 0f;
		}
		
		if(f > 0.3f) {
			f = f * -1f;
		}
		
		return f;
		
	}

}
