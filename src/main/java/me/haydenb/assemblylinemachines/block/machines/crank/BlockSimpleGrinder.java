package me.haydenb.assemblylinemachines.block.machines.crank;

import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleGrinder.TESimpleGrinder;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.ICrankableMachine;
import me.haydenb.assemblylinemachines.util.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.util.TEContainingBlock.GUIContainingBasicBlock;
import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.Utils.Pair;
import me.haydenb.assemblylinemachines.util.machines.ALMMachineNoExtract;
import me.haydenb.assemblylinemachines.util.machines.AbstractALMMachine;
import me.haydenb.assemblylinemachines.util.machines.AbstractALMMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.util.machines.AbstractALMMachine.ScreenALMBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleGrinder extends GUIContainingBasicBlock<TESimpleGrinder> implements ICrankableBlock{
	

	public BlockSimpleGrinder() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "simple_grinder", TESimpleGrinder.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(Utils.MACHINE_ACTIVE, false).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	public boolean validSide(BlockState state, Direction dir) {
		return true;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(Utils.MACHINE_ACTIVE).add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public boolean needsGearbox() {
		return false;
	}
	
	public static class TESimpleGrinder extends ALMMachineNoExtract<ContainerSimpleGrinder> implements ITickableTileEntity, ICrankableMachine{
		
		public int timer;
		public int cranks;
		public boolean pendingOutput = false;
		public ItemStack output;
		public boolean check;
		public float progress = 0;
		public float cycles = 0;
		public ItemStack isa = null;
		public ItemStack isb = null;
		
		
		public TESimpleGrinder(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 2, (TranslationTextComponent) Registry.getBlock("simple_grinder").getNameTextComponent(), Registry.getContainerId("simple_grinder"), ContainerSimpleGrinder.class);
		}
		
		public TESimpleGrinder() {
			this(Registry.getTileEntity("simple_grinder"));
		}

		@Override
		public boolean perform() {
			if(output == null || pendingOutput == true) {
				return false;
			}
			if(contents.get(0).getItem() instanceof ItemGrindingBlade) {
				cranks++;
				return true;
			}
			return false;
			
			
		}
		
		@Override
		public void tick() {
			
			if(timer++ == 20) {
				if(!world.isRemote) {
					timer = 0;
					if(output != null) {
						boolean sendupdate = false;
						boolean end = false;
						if(pendingOutput) {
							Direction opdr = getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).rotateYCCW();
							TileEntity te = world.getTileEntity(pos.offset(opdr));
							if(te != null) {
								LazyOptional<IItemHandler> h = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opdr.getOpposite());
								IItemHandler handler = h.orElse(null);
								if(handler != null) {
									for(int i = 0; i < handler.getSlots(); i++) {
										if(handler.insertItem(i, output, true) == ItemStack.EMPTY) {
											handler.insertItem(i, output, false);
											output = null;
											progress = 0;
											cycles = 0;
											pendingOutput = false;
											sendupdate = true;
											end = true;
											world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, false));
											break;
										}
									}
								}
							}
						}else {
							if(cranks >= 3) {
								cranks = 0;
								if(progress == cycles) {
									pendingOutput = true;
									sendupdate = true;
								}
								ItemStack isa = contents.get(0);
								if(isa.isDamageable() && isa.getItem() instanceof ItemGrindingBlade) {
									isa.setDamage(isa.getDamage() + 1);
									if(isa.getDamage() >= isa.getMaxDamage()) {
										contents.set(0, ItemStack.EMPTY);
									}
									progress++;
									sendupdate = true;
								}
							}
						}
						
						if(end == false && getBlockState().get(Utils.MACHINE_ACTIVE) == false) {
							world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, true));
							sendupdate = true;
						}
						if(sendupdate) {
							sendUpdates();
						}
					}else {
						if(contents.get(0) != isa || contents.get(1) != isb) {
							check = true;
						}
						if(check == true) {
							isa = contents.get(0);
							isb = contents.get(1);
							if(isa.getItem() instanceof ItemGrindingBlade) {
								ItemGrindingBlade igb = (ItemGrindingBlade) isa.getItem();
								GrinderCrafting crafting = world.getRecipeManager().getRecipe(GrinderCrafting.GRINDER_RECIPE, this, world).orElse(null);
								if(crafting != null) {
									if(igb.blade.tier >= crafting.getBlade().tier) {
										isb.shrink(1);
										isb = null;
										isa = null;
										output = crafting.getRecipeOutput().copy();
										cycles = crafting.getGrinds() * 2;
										progress = 0;
										pendingOutput = false;
										sendUpdates();
									}
								}
							}
							
						}
					}
				}
			}
			
			
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putBoolean("assemblylinemachines:pendingoutput", pendingOutput);
			if(output != null) {
				CompoundNBT sub = new CompoundNBT();
				output.write(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			return super.write(compound);
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:cycles")) {
				cycles = compound.getFloat("assemblylinemachines:cycles");
			}
			if(compound.contains("assemblylinemachines:progress")) {
				progress = compound.getFloat("assemblylinemachines:progress");
			}
			if(compound.contains("assemblylinemachines:pendingoutput")) {
				pendingOutput = compound.getBoolean("assemblylinemachines:pendingoutput");
			}
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
			}

		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot == 0) {
				if(stack.getItem() instanceof ItemGrindingBlade) {
					return true;
				}
				return false;
			}
			return true;
		}
	}
	
	public static class ContainerSimpleGrinder extends ContainerALMBase<TESimpleGrinder>{
		
		private static final Pair<Integer, Integer> INPUT_A_POS = new Pair<>(53, 26);
		private static final Pair<Integer, Integer> INPUT_B_POS = new Pair<>(75, 48);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerSimpleGrinder(final int windowId, final PlayerInventory playerInventory, final TESimpleGrinder tileEntity) {
			super(Registry.getContainerType("simple_grinder"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			
			this.addSlot(new AbstractALMMachine.SlotWithRestrictions(this.tileEntity, 0, INPUT_A_POS.x, INPUT_A_POS.y, tileEntity));
			this.addSlot(new Slot(this.tileEntity, 1, INPUT_B_POS.x, INPUT_B_POS.y));
		}
		
		
		public ContainerSimpleGrinder(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, Utils.getTileEntity(playerInventory, data, TESimpleGrinder.class));
		}
		
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenSimpleGrinder extends ScreenALMBase<ContainerSimpleGrinder>{
		
		TESimpleGrinder tsfm;
		
		public ScreenSimpleGrinder(ContainerSimpleGrinder screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "simple_grinder", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
			super.blit(x+73, y+19 + (24 - prog), 176, (24 - prog), 20, prog);
		}
	}

}
