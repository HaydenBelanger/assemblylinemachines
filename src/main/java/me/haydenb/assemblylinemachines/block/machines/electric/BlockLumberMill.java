package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.crafting.LumberCrafting;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockLumberMill extends BlockScreenTileEntity<BlockLumberMill.TELumberMill>{
	
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
			Block.makeCuboidShape(1, 3, 2, 15, 14, 16),
			Block.makeCuboidShape(1, 6, 0, 15, 14, 1),
			Block.makeCuboidShape(0, 3, 0, 3, 6, 1),
			Block.makeCuboidShape(0, 3, 1, 1, 6, 16),
			Block.makeCuboidShape(15, 3, 1, 16, 6, 16),
			Block.makeCuboidShape(15, 10, 0, 16, 14, 16),
			Block.makeCuboidShape(0, 10, 0, 1, 14, 16),
			Block.makeCuboidShape(13, 3, 0, 16, 6, 1),
			Block.makeCuboidShape(9, 3, 0, 12, 6, 1),
			Block.makeCuboidShape(4, 3, 0, 7, 6, 1),
			Block.makeCuboidShape(3, 3, 1, 13, 6, 2),
			Block.makeCuboidShape(1, 6, 1, 15, 12, 2)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockLumberMill() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "lumber_mill", BlockLumberMill.TELumberMill.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.MACHINE_ACTIVE, false).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE).add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
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
	
	public static class TELumberMill extends ManagedSidedMachine<ContainerLumberMill> implements ITickableTileEntity{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private ItemStack outputb = null;
		
		public TELumberMill(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 6, new TranslationTextComponent(Registry.getBlock("lumber_mill").getTranslationKey()), Registry.getContainerId("lumber_mill"), ContainerLumberMill.class, new EnergyProperties(true, false, 20000));
		}
		
		public TELumberMill() {
			this(Registry.getTileEntity("lumber_mill"));
		}

		@Override
		public void tick() {
			if(!world.isRemote) {
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
						
						Optional<LumberCrafting> rOpt = world.getRecipeManager().getRecipe(LumberCrafting.LUMBER_RECIPE, this, world);
						LumberCrafting recipe = rOpt.orElse(null);
						if(recipe != null) {
							output = recipe.getRecipeOutput().copy();
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
							if(!getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
								world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
							}
						}else {
							if(getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
								world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
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
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
			}
			
			if(compound.contains("assemblylinemachines:outputb")) {
				outputb = ItemStack.read(compound.getCompound("assemblylinemachines:outputb"));
			}
			if(compound.contains("assemblylinemachines:ntimer")) {
				nTimer = compound.getInt("assemblylinemachines:ntimer");
			}
			cycles = compound.getFloat("assemblylinemachines:cycles");
			progress = compound.getFloat("assemblylinemachines:progress");
			
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			if(output != null) {
				CompoundNBT sub = new CompoundNBT();
				output.write(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			
			if(outputb != null) {
				CompoundNBT sub = new CompoundNBT();
				outputb.write(sub);
				compound.put("assemblylinemachines:outputb", sub);
			}
			return super.write(compound);
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
		
		public ContainerLumberMill(final int windowId, final PlayerInventory playerInventory, final TELumberMill tileEntity) {
			super(Registry.getContainerType("lumber_mill"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 2, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 98, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 124, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 51, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 57, tileEntity));
		}
		
		public ContainerLumberMill(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TELumberMill.class));
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenLumberMill extends ScreenALMEnergyBased<ContainerLumberMill>{
		
		TELumberMill tsfm;
		
		private int f = 0;
		private int ff = 0;
		private int t = 0;
		private int tt = 0;
		
		public ScreenLumberMill(ContainerLumberMill screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "lumber_mill", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
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
