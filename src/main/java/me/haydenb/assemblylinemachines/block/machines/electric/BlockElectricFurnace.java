package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
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

public class BlockElectricFurnace extends BlockScreenBlockEntity<BlockElectricFurnace.TEElectricFurnace>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 13, 0, 16, 16, 16),
			Block.box(0, 3, 13, 16, 13, 16),
			Block.box(0, 3, 0, 2, 13, 3),
			Block.box(14, 3, 0, 16, 13, 3),
			Block.box(2, 6, 0, 14, 13, 3),
			Block.box(0, 3, 3, 16, 5, 13),
			Block.box(0, 11, 3, 16, 13, 13),
			Block.box(4, 3, 0, 7, 6, 1),
			Block.box(13, 3, 0, 14, 6, 1),
			Block.box(2, 3, 0, 3, 6, 1),
			Block.box(9, 3, 0, 12, 6, 1),
			Block.box(2, 3, 1, 14, 6, 1),
			Block.box(2, 5, 3, 2, 11, 13),
			Block.box(14, 5, 3, 14, 11, 13),
			Block.box(1, 5, 4, 2, 11, 5),
			Block.box(14, 5, 4, 15, 11, 5),
			Block.box(1, 5, 6, 2, 11, 7),
			Block.box(14, 5, 6, 15, 11, 7),
			Block.box(1, 5, 9, 2, 11, 10),
			Block.box(14, 5, 9, 15, 11, 10),
			Block.box(1, 5, 11, 2, 11, 12),
			Block.box(14, 5, 11, 15, 11, 12)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockElectricFurnace() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "electric_furnace", BlockElectricFurnace.TEElectricFurnace.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(BathCraftingFluid.MACHINE_ACTIVE, false).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BathCraftingFluid.MACHINE_ACTIVE).add(HorizontalDirectionalBlock.FACING);
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
	public static class TEElectricFurnace extends ManagedSidedMachine<ContainerElectricFurnace> implements ALMTicker<TEElectricFurnace>{
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private EFIInventoryWrapper wrapper = new EFIInventoryWrapper(this);
		
		public TEElectricFurnace(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 5, new TranslatableComponent(Registry.getBlock("electric_furnace").getDescriptionId()), Registry.getContainerId("electric_furnace"), ContainerElectricFurnace.class, new EnergyProperties(true, false, 20000), pos, state);
		}
		
		public TEElectricFurnace(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("electric_furnace"), pos, state);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {
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
						
						Optional<SmeltingRecipe> rOpt = this.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, wrapper, level);
						SmeltingRecipe recipe = rOpt.orElse(null);
						if(recipe != null) {
							output = recipe.assemble(wrapper);
							cycles = ((float) recipe.getCookingTime() / 20F);
							
							contents.get(1).shrink(1);
							sendUpdates = true;
							if(!getBlockState().getValue(BathCraftingFluid.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BathCraftingFluid.MACHINE_ACTIVE, true));
							}
						}else {
							if(getBlockState().getValue(BathCraftingFluid.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BathCraftingFluid.MACHINE_ACTIVE, false));
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
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}else {
				output = null;
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
			}else {
				compound.remove("assemblylinemachines:output");
			}
			return super.save(compound);
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
	
	
	public static class EFIInventoryWrapper implements Container{

		private final Container pinv;
		public EFIInventoryWrapper(Container parent) {
			pinv = parent;
		}
		
		@Override
		public void clearContent() {
			pinv.clearContent();
			
		}

		@Override
		public int getContainerSize() {
			return pinv.getContainerSize();
		}

		@Override
		public boolean isEmpty() {
			return pinv.isEmpty();
		}

		@Override
		public ItemStack getItem(int index) {
			if(index == 0) {
				return pinv.getItem(1);
			}else if(index == 1) {
				return pinv.getItem(0);
			}
			return pinv.getItem(index);
		}
		
		@Override
		public ItemStack removeItem(int pIndex, int pCount) {
			return pinv.removeItem(pIndex, pCount);
		}

		@Override
		public ItemStack removeItemNoUpdate(int pIndex) {
			return pinv.removeItemNoUpdate(pIndex);
		}
		
		@Override
		public void setItem(int pIndex, ItemStack pStack) {
			pinv.setItem(pIndex, pStack);
			
		}
		
		@Override
		public void setChanged() {
			pinv.setChanged();
			
		}
		
		@Override
		public boolean stillValid(Player pPlayer) {
			return pinv.stillValid(pPlayer);
		}
	}
	
	public static class ContainerElectricFurnace extends ContainerALMBase<TEElectricFurnace>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerElectricFurnace(final int windowId, final Inventory playerInventory, final TEElectricFurnace tileEntity) {
			super(Registry.getContainerType("electric_furnace"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 75, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 57, tileEntity));
		}
		
		public ContainerElectricFurnace(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEElectricFurnace.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenElectricFurnace extends ScreenALMEnergyBased<ContainerElectricFurnace>{
		
		TEElectricFurnace tsfm;
		
		public ScreenElectricFurnace(ContainerElectricFurnace screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "electric_furnace", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 16f);
			super.blit(x+95, y+35, 176, 64, prog, 14);
			
			if(tsfm.output != null) {
				super.blit(x+76, y+53, 176, 52, 13, 12);
			}
		}
	}
}
