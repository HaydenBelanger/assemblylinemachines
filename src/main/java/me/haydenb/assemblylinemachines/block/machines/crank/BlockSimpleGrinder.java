package me.haydenb.assemblylinemachines.block.machines.crank;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleGrinder.TESimpleGrinder;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleGrinder extends BlockScreenBlockEntity<TESimpleGrinder> implements ICrankableBlock{
	

	public BlockSimpleGrinder() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "simple_grinder", TESimpleGrinder.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	@Override
	public boolean validSide(BlockState state, Direction dir) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(StateProperties.MACHINE_ACTIVE).add(HorizontalDirectionalBlock.FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean needsGearbox() {
		return false;
	}
	
	public static class TESimpleGrinder extends SimpleMachine<ContainerSimpleGrinder> implements ALMTicker<TESimpleGrinder>, ICrankableMachine{
		
		public int timer;
		public int cranks;
		public boolean pendingOutput = false;
		public ItemStack output;
		public boolean check;
		public float progress = 0;
		public float cycles = 0;
		public ItemStack isa = null;
		public ItemStack isb = null;
		
		
		public TESimpleGrinder(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 2, new TranslatableComponent(Registry.getBlock("simple_grinder").getDescriptionId()), Registry.getContainerId("simple_grinder"), ContainerSimpleGrinder.class, pos, state);
		}
		
		public TESimpleGrinder(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("simple_grinder"), pos, state);
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
				if(!level.isClientSide) {
					timer = 0;
					if(output != null) {
						boolean sendupdate = false;
						boolean end = false;
						if(pendingOutput) {
							Direction opdr = getBlockState().getValue(HorizontalDirectionalBlock.FACING).getCounterClockWise();
							BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(opdr));
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
											this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
											break;
										}
									}
								}
							}
						}else {
							if(cranks >= 1) {
								cranks = 0;
								if(progress == cycles) {
									pendingOutput = true;
									sendupdate = true;
								}
								ItemStack isa = contents.get(0);
								if(isa.isDamageableItem() && isa.getItem() instanceof ItemGrindingBlade) {
									
									if(General.RAND.nextInt(3) == 0) {
										isa.setDamageValue(isa.getDamageValue() + 1);
									}
									if(isa.getDamageValue() >= isa.getMaxDamage()) {
										contents.set(0, ItemStack.EMPTY);
									}
									progress++;
									sendupdate = true;
								}
							}
						}
						
						if(end == false && getBlockState().getValue(StateProperties.MACHINE_ACTIVE) == false) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
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
								GrinderCrafting crafting = this.getLevel().getRecipeManager().getRecipeFor(GrinderCrafting.GRINDER_RECIPE, this, this.getLevel()).orElse(null);
								if(crafting != null) {
									if(igb.blade.tier >= crafting.getBlade().tier) {
										isb.shrink(1);
										isb = null;
										isa = null;
										output = crafting.getResultItem().copy();
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
		public CompoundTag save(CompoundTag compound) {
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putBoolean("assemblylinemachines:pendingoutput", pendingOutput);
			if(output != null) {
				CompoundTag sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			return super.save(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
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
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
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
		
		public ContainerSimpleGrinder(final int windowId, final Inventory playerInventory, final TESimpleGrinder tileEntity) {
			super(Registry.getContainerType("simple_grinder"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, INPUT_A_POS.getFirst(), INPUT_A_POS.getSecond(), tileEntity));
			this.addSlot(new Slot(this.tileEntity, 1, INPUT_B_POS.getFirst(), INPUT_B_POS.getSecond()));
		}
		
		
		public ContainerSimpleGrinder(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TESimpleGrinder.class));
		}
		
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenSimpleGrinder extends ScreenALMBase<ContainerSimpleGrinder>{
		
		TESimpleGrinder tsfm;
		
		public ScreenSimpleGrinder(ContainerSimpleGrinder screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "simple_grinder", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
			super.blit(x+73, y+19 + (24 - prog), 176, (24 - prog), 20, prog);
		}
	}

}
