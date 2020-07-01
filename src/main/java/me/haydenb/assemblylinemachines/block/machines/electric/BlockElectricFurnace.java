package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
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

public class BlockElectricFurnace extends BlockScreenTileEntity<BlockElectricFurnace.TEElectricFurnace>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
			Block.makeCuboidShape(0, 13, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 3, 13, 16, 13, 16),
			Block.makeCuboidShape(0, 3, 0, 2, 13, 3),
			Block.makeCuboidShape(14, 3, 0, 16, 13, 3),
			Block.makeCuboidShape(2, 6, 0, 14, 13, 3),
			Block.makeCuboidShape(0, 3, 3, 16, 5, 13),
			Block.makeCuboidShape(0, 11, 3, 16, 13, 13),
			Block.makeCuboidShape(4, 3, 0, 7, 6, 1),
			Block.makeCuboidShape(13, 3, 0, 14, 6, 1),
			Block.makeCuboidShape(2, 3, 0, 3, 6, 1),
			Block.makeCuboidShape(9, 3, 0, 12, 6, 1),
			Block.makeCuboidShape(2, 3, 1, 14, 6, 1),
			Block.makeCuboidShape(2, 5, 3, 2, 11, 13),
			Block.makeCuboidShape(14, 5, 3, 14, 11, 13),
			Block.makeCuboidShape(1, 5, 4, 2, 11, 5),
			Block.makeCuboidShape(14, 5, 4, 15, 11, 5),
			Block.makeCuboidShape(1, 5, 6, 2, 11, 7),
			Block.makeCuboidShape(14, 5, 6, 15, 11, 7),
			Block.makeCuboidShape(1, 5, 9, 2, 11, 10),
			Block.makeCuboidShape(14, 5, 9, 15, 11, 10),
			Block.makeCuboidShape(1, 5, 11, 2, 11, 12),
			Block.makeCuboidShape(14, 5, 11, 15, 11, 12)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockElectricFurnace() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "electric_furnace", BlockElectricFurnace.TEElectricFurnace.class);
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
	public static class TEElectricFurnace extends ManagedSidedMachine<ContainerElectricFurnace> implements ITickableTileEntity{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private EFIInventoryWrapper wrapper = new EFIInventoryWrapper(this);
		
		public TEElectricFurnace(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 5, new TranslationTextComponent(Registry.getBlock("electric_furnace").getTranslationKey()), Registry.getContainerId("electric_furnace"), ContainerElectricFurnace.class, new EnergyProperties(true, false, 20000));
		}
		
		public TEElectricFurnace() {
			this(Registry.getTileEntity("electric_furnace"));
		}

		@Override
		public void tick() {
			if(!world.isRemote) {
				if(timer++ == nTimer) {
					
					boolean sendUpdates = false;
					timer = 0;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					int cost = 80;
					switch(upcount){
					case 3:
						nTimer = 2;
						cost = 560;
						break;
					case 2:
						nTimer = 4;
						cost = 220;
						break;
					case 1:
						nTimer = 8;
						cost = 160;
						break;
					default:
						nTimer = 16;
					}
					
					
					if(output == null || output.isEmpty()) {
						
						Optional<FurnaceRecipe> rOpt = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, wrapper, world);
						FurnaceRecipe recipe = rOpt.orElse(null);
						if(recipe != null) {
							output = recipe.getCraftingResult(wrapper);
							cycles = ((float) recipe.getCookTime() / 10F);
							
							contents.get(1).shrink(1);
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
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 1) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}
			return super.isAllowedInSlot(slot, stack);
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 2; i < 5; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
		
	}
	
	
	public static class EFIInventoryWrapper implements IInventory{

		private final IInventory pinv;
		public EFIInventoryWrapper(IInventory parent) {
			pinv = parent;
		}
		@Override
		public void clear() {
			pinv.clear();
			
		}

		@Override
		public int getSizeInventory() {
			return pinv.getSizeInventory();
		}

		@Override
		public boolean isEmpty() {
			return pinv.isEmpty();
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			if(index == 0) {
				return pinv.getStackInSlot(1);
			}else if(index == 1) {
				return pinv.getStackInSlot(0);
			}
			return pinv.getStackInSlot(index);
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			return pinv.decrStackSize(index, count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			return pinv.removeStackFromSlot(index);
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			pinv.setInventorySlotContents(index, stack);
			
		}

		@Override
		public void markDirty() {
			pinv.markDirty();
			
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player) {
			return pinv.isUsableByPlayer(player);
		}
		
	}
	
	public static class ContainerElectricFurnace extends ContainerALMBase<TEElectricFurnace>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerElectricFurnace(final int windowId, final PlayerInventory playerInventory, final TEElectricFurnace tileEntity) {
			super(Registry.getContainerType("electric_furnace"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 75, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 57, tileEntity));
		}
		
		public ContainerElectricFurnace(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEElectricFurnace.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenElectricFurnace extends ScreenALMEnergyBased<ContainerElectricFurnace>{
		
		TEElectricFurnace tsfm;
		
		public ScreenElectricFurnace(ContainerElectricFurnace screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "electric_furnace", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 16f);
			super.blit(x+95, y+35, 176, 64, prog, 14);
			
			if(tsfm.output != null) {
				super.blit(x+76, y+53, 176, 52, 13, 12);
			}
		}
	}
}
