package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.crafting.PurifierCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockElectricPurifier extends BlockScreenBlockEntity<BlockElectricPurifier.TEElectricPurifier>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(4, 2, 0, 5, 7, 1),
			Block.box(6, 2, 0, 7, 7, 1),
			Block.box(9, 2, 0, 10, 7, 1),
			Block.box(11, 2, 0, 12, 7, 1),
			Block.box(0, 7, 0, 16, 16, 16),
			Block.box(0, 2, 0, 2, 7, 16),
			Block.box(14, 2, 0, 16, 7, 16),
			Block.box(2, 2, 2, 14, 7, 16)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	private static final Random RAND = new Random();
	private static final EnumProperty<PurifierStates> PURIFIER_STATES = EnumProperty.create("active", PurifierStates.class);
	
	public BlockElectricPurifier() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "electric_purifier", BlockElectricPurifier.TEElectricPurifier.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(PURIFIER_STATES, PurifierStates.FALSE).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(PURIFIER_STATES).add(HorizontalDirectionalBlock.FACING);
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
	
	public static enum PurifierStates implements StringRepresentable{
		FALSE, TRUE, ENHANCEDFALSE, ENHANCEDTRUE;
		
		@Override
		public String getSerializedName() {
			return toString().toLowerCase();
		}
		
		
	}
	public static class TEElectricPurifier extends ManagedSidedMachine<ContainerElectricPurifier> implements ALMTicker<TEElectricPurifier>{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 30;
		private ItemStack output = null;
		
		
		public TEElectricPurifier(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 7, new TranslatableComponent(Registry.getBlock("electric_purifier").getDescriptionId()), Registry.getContainerId("electric_purifier"), ContainerElectricPurifier.class, new EnergyProperties(true, false, 20000), pos, state);
		}
		
		public TEElectricPurifier(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("electric_purifier"), pos, state);
		}

		@Override
		public void tick() {
			
			
			if(!level.isClientSide) {
				if(timer++ == nTimer) {
					timer = 0;
					boolean sendUpdates = false;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					int cost = 100;
					switch(upcount) {
					case 3:
						nTimer = 2;
						cost = 700;
						break;
					case 2:
						nTimer = 4;
						cost = 360;
						break;
					case 1:
						nTimer = 8;
						cost = 220;
						break;
					default:
						nTimer = 16;
					}
					
					if(output == null || output.isEmpty()) {
						Optional<PurifierCrafting> rOpt = this.getLevel().getRecipeManager().getRecipeFor(PurifierCrafting.PURIFIER_RECIPE, this, this.getLevel());
						PurifierCrafting recipe = rOpt.orElse(null);
						if(recipe != null) {
							
							boolean reqUpgrade = recipe.requiresUpgrade();
							
							if(reqUpgrade == false || getUpgradeAmount(Upgrades.PURIFIER_EXPANDED) != 0) {
								output = recipe.getResultItem().copy();
								cycles = ((float) recipe.getTime() / 10F);
								
								int conserve;
								
								if(reqUpgrade) {
									conserve = 0;
									cost = Math.round((float) cost * 2.2f);
								}else {
									conserve = getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
								}
								
								if(RAND.nextInt(10) * conserve < 10) {
									contents.get(1).shrink(1);
								}
								if(RAND.nextInt(10) * conserve < 10) {
									contents.get(2).shrink(1);
								}
								contents.get(3).shrink(1);
								sendUpdates = true;
								
								
								if(reqUpgrade) {
									if(getBlockState().getValue(PURIFIER_STATES) != PurifierStates.ENHANCEDTRUE) {
										this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(PURIFIER_STATES, PurifierStates.ENHANCEDTRUE));
									}
								}else {
									if(getBlockState().getValue(PURIFIER_STATES) != PurifierStates.TRUE) {
										this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(PURIFIER_STATES, PurifierStates.TRUE));
									}
								}
								
							}
							
						}else {
							
							if(getBlockState().getValue(PURIFIER_STATES) == PurifierStates.ENHANCEDTRUE) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(PURIFIER_STATES, PurifierStates.ENHANCEDFALSE));
							}else if(getBlockState().getValue(PURIFIER_STATES) == PurifierStates.TRUE) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(PURIFIER_STATES, PurifierStates.FALSE));
							}
						}
						
					}
					
					if(output != null && !output.isEmpty()) {
						
						if(amount - cost >= 0) {
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
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 3) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}
			return super.isAllowedInSlot(slot, stack);
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 4; i < 7; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
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
			return super.save(compound);
		}
	}
	
	public static class ContainerElectricPurifier extends ContainerALMBase<TEElectricPurifier>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerElectricPurifier(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TEElectricPurifier.class));
		}
		
		public ContainerElectricPurifier(final int windowId, final Inventory playerInventory, final TEElectricPurifier tileEntity) {
			super(Registry.getContainerType("electric_purifier"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 51, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 51, 47, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 72, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 6, 149, 57, tileEntity));
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenElectricPurifier extends ScreenALMEnergyBased<ContainerElectricPurifier>{
		TEElectricPurifier tsfm;
		
		private int f = 0;
		private int t = 0;
		
		public ScreenElectricPurifier(ContainerElectricPurifier screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "electric_purifier", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(t++ == 10) {
				t = 0;
				if(f == 2) {
					f = 0;
				}else {
					f++;
				}
			}
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 43f);
			super.blit(x+70, y+26, 176, 52 + (32 * f), prog, 32);
			
		}
		
	}
	
}
