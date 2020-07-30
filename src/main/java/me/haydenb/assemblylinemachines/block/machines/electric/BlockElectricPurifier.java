package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.crafting.PurifierCrafting;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockElectricPurifier extends BlockScreenTileEntity<BlockElectricPurifier.TEElectricPurifier>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
			Block.makeCuboidShape(4, 2, 0, 5, 7, 1),
			Block.makeCuboidShape(6, 2, 0, 7, 7, 1),
			Block.makeCuboidShape(9, 2, 0, 10, 7, 1),
			Block.makeCuboidShape(11, 2, 0, 12, 7, 1),
			Block.makeCuboidShape(0, 7, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 2, 0, 2, 7, 16),
			Block.makeCuboidShape(14, 2, 0, 16, 7, 16),
			Block.makeCuboidShape(2, 2, 2, 14, 7, 16)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	private static final Random RAND = new Random();
	
	public BlockElectricPurifier() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "electric_purifier", BlockElectricPurifier.TEElectricPurifier.class);
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
	
	public static class TEElectricPurifier extends ManagedSidedMachine<ContainerElectricPurifier> implements ITickableTileEntity{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 30;
		private ItemStack output = null;
		
		
		public TEElectricPurifier(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 7, new TranslationTextComponent(Registry.getBlock("electric_purifier").getTranslationKey()), Registry.getContainerId("electric_purifier"), ContainerElectricPurifier.class, new EnergyProperties(true, false, 20000));
		}
		
		public TEElectricPurifier() {
			this(Registry.getTileEntity("electric_purifier"));
		}

		@Override
		public void tick() {
			
			
			if(!world.isRemote) {
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
						Optional<PurifierCrafting> rOpt = world.getRecipeManager().getRecipe(PurifierCrafting.PURIFIER_RECIPE, this, world);
						PurifierCrafting recipe = rOpt.orElse(null);
						if(recipe != null) {
							output = recipe.getRecipeOutput().copy();
							cycles = ((float) recipe.getTime() / 10F);
							
							int conserve = getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
							
							if(RAND.nextInt(10) * conserve < 10) {
								contents.get(1).shrink(1);
							}
							if(RAND.nextInt(10) * conserve < 10) {
								contents.get(2).shrink(1);
							}
							contents.get(3).shrink(1);
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
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
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
			return super.write(compound);
		}
	}
	
	public static class ContainerElectricPurifier extends ContainerALMBase<TEElectricPurifier>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerElectricPurifier(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEElectricPurifier.class));
		}
		
		public ContainerElectricPurifier(final int windowId, final PlayerInventory playerInventory, final TEElectricPurifier tileEntity) {
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
		
		public ScreenElectricPurifier(ContainerElectricPurifier screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "electric_purifier", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
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
