package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.crafting.LumberCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockLumberMill extends BlockScreenBlockEntity<BlockLumberMill.TELumberMill>{
	
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(1, 3, 2, 15, 14, 16),
			Block.box(1, 6, 0, 15, 14, 1),
			Block.box(0, 3, 0, 3, 6, 1),
			Block.box(0, 3, 1, 1, 6, 16),
			Block.box(15, 3, 1, 16, 6, 16),
			Block.box(15, 10, 0, 16, 14, 16),
			Block.box(0, 10, 0, 1, 14, 16),
			Block.box(13, 3, 0, 16, 6, 1),
			Block.box(9, 3, 0, 12, 6, 1),
			Block.box(4, 3, 0, 7, 6, 1),
			Block.box(3, 3, 1, 13, 6, 2),
			Block.box(1, 6, 1, 15, 12, 2)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockLumberMill() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "lumber_mill", BlockLumberMill.TELumberMill.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
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
	
	public static class TELumberMill extends ManagedSidedMachine<ContainerLumberMill> implements ALMTicker<TELumberMill>{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private ItemStack outputb = null;
		
		public TELumberMill(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 6, new TranslatableComponent(Registry.getBlock("lumber_mill").getDescriptionId()), Registry.getContainerId("lumber_mill"), ContainerLumberMill.class, new EnergyProperties(true, false, 20000), pos, state);
		}
		
		public TELumberMill(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("lumber_mill"), pos, state);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == nTimer) {
					
					boolean sendUpdates = false;
					timer = 0;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					int cost = 90;
					switch(upcount){
					case 3:
						nTimer = 2;
						cost = 580;
						break;
					case 2:
						nTimer = 4;
						cost = 240;
						break;
					case 1:
						nTimer = 8;
						cost = 180;
						break;
					default:
						nTimer = 16;
					}
					
					
					if(output == null || output.isEmpty()) {
						
						Optional<LumberCrafting> rOpt = this.getLevel().getRecipeManager().getRecipeFor(LumberCrafting.LUMBER_RECIPE, this, this.getLevel());
						LumberCrafting recipe = rOpt.orElse(null);
						if(recipe != null) {
							output = recipe.getResultItem().copy();
							cycles = (float) recipe.getTime();
							if(!recipe.getSecondaryOutput().isEmpty()) {
								float rtChance = recipe.getOutputChance();
								upcount = getUpgradeAmount(Upgrades.MACHINE_EXTRA);
								switch(upcount){
								case 3:
									rtChance *= 2.5f;
									break;
								case 2:
									rtChance *= 2f;
									break;
								case 1:
									rtChance *= 1.5f;
									break;
								}
								
								if(General.RAND.nextFloat() < rtChance) {
									outputb = recipe.getSecondaryOutput().copy();
								}
								
							}
							
							contents.get(2).shrink(1);
							sendUpdates = true;
							if(!getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
							}
						}else {
							if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
								sendUpdates = true;
							}
						}
					}
					
					if(output != null && !output.isEmpty()) {
						
						if(amount - cost >= 0) {
							if(progress >= cycles) {
								if(contents.get(0).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(0), output) && contents.get(0).getCount() + output.getCount() <= contents.get(0).getMaxStackSize())){
									
									if(outputb == null || (contents.get(1).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(1), outputb) && contents.get(1).getCount() + outputb.getCount() <= contents.get(1).getMaxStackSize()))) {
										if(contents.get(0).isEmpty()) {
											contents.set(0, output);
										}else {
											contents.get(0).grow(output.getCount());
										}
										
										if(outputb != null) {
											if(contents.get(1).isEmpty()) {
												contents.set(1, outputb);
											}else {
												contents.get(1).grow(outputb.getCount());
											}
										}
										output = null;
										outputb = null;
										cycles = 0;
										progress = 0;
										sendUpdates = true;
									}
									
								}
							}else {
								
								amount -= cost;
								fept = (float) cost / (float) nTimer;
								progress++;
								sendUpdates = true;
							}
							
						}
						
					}
					
					
					if(sendUpdates) {
						sendUpdates();
					}
					
				}
			}
			
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}
			
			if(compound.contains("assemblylinemachines:outputb")) {
				outputb = ItemStack.of(compound.getCompound("assemblylinemachines:outputb"));
			}
			if(compound.contains("assemblylinemachines:ntimer")) {
				nTimer = compound.getInt("assemblylinemachines:ntimer");
			}
			cycles = compound.getFloat("assemblylinemachines:cycles");
			progress = compound.getFloat("assemblylinemachines:progress");
			
		}
		
		@Override
		public CompoundTag save(CompoundTag compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			if(output != null) {
				CompoundTag sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			
			if(outputb != null) {
				CompoundTag sub = new CompoundTag();
				outputb.save(sub);
				compound.put("assemblylinemachines:outputb", sub);
			}
			return super.save(compound);
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 2) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}else if(slot < 2) {
				return false;
			}
			return super.isAllowedInSlot(slot, stack);
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
	
	public static class ContainerLumberMill extends ContainerALMBase<TELumberMill>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerLumberMill(final int windowId, final Inventory playerInventory, final TELumberMill tileEntity) {
			super(Registry.getContainerType("lumber_mill"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 2, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 98, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 124, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 51, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 57, tileEntity));
		}
		
		public ContainerLumberMill(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TELumberMill.class));
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenLumberMill extends ScreenALMEnergyBased<ContainerLumberMill>{
		
		TELumberMill tsfm;
		
		private int f = 0;
		private int ff = 0;
		private int t = 0;
		private int tt = 0;
		
		public ScreenLumberMill(ContainerLumberMill screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "lumber_mill", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(t++ == 10) {
				t = 0;
				if(f == 1) {
					f = 0;
				}else {
					f++;
				}
			}

			if(tt++ == 20) {
				tt = 0;
				if(ff == 7) {
					ff = 0;
				}else {
					ff++;
				}
			}
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 19f);
			super.blit(x+71, y+40, 176, 64, prog, 5);
			super.blit(x+71, y+34, 176, 52 + (6*f), prog, 6);
			
			super.blit(x+71, y+34, 196, 52 + (6*ff), prog, 6);
		}
	}
}
