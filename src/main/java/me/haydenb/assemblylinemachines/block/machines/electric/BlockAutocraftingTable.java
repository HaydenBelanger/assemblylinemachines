package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.SlotWithRestrictions;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.SimpleButton;
import me.haydenb.assemblylinemachines.util.StateProperties;
import me.haydenb.assemblylinemachines.util.SupplierWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockAutocraftingTable extends BlockScreenTileEntity<BlockAutocraftingTable.TEAutocraftingTable>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 0, 0, 16, 3, 16),Block.makeCuboidShape(0, 3, 3, 3, 13, 13),
			Block.makeCuboidShape(13, 3, 3, 16, 13, 13),Block.makeCuboidShape(11, 3, 0, 16, 9, 3),
			Block.makeCuboidShape(0, 3, 0, 5, 9, 3),Block.makeCuboidShape(0, 3, 13, 5, 9, 16),
			Block.makeCuboidShape(11, 3, 13, 16, 9, 16),Block.makeCuboidShape(5, 6, 0, 11, 9, 16),
			Block.makeCuboidShape(3, 6, 3, 5, 9, 13),Block.makeCuboidShape(11, 6, 3, 13, 9, 13),
			Block.makeCuboidShape(5, 3, 2, 11, 6, 2),Block.makeCuboidShape(5, 3, 14, 11, 6, 14),
			Block.makeCuboidShape(5, 3, 1, 6, 6, 2),Block.makeCuboidShape(7, 3, 1, 8, 6, 2),
			Block.makeCuboidShape(9, 3, 1, 10, 6, 2),Block.makeCuboidShape(10, 3, 14, 11, 6, 15),
			Block.makeCuboidShape(8, 3, 14, 9, 6, 15),Block.makeCuboidShape(6, 3, 14, 7, 6, 15),
			Block.makeCuboidShape(3, 9, 0, 6, 12, 3),Block.makeCuboidShape(10, 9, 0, 13, 12, 3),
			Block.makeCuboidShape(10, 9, 13, 13, 12, 16),Block.makeCuboidShape(3, 9, 13, 6, 12, 16),
			Block.makeCuboidShape(4, 12, 1, 5, 16, 2),Block.makeCuboidShape(11, 12, 1, 12, 16, 2),
			Block.makeCuboidShape(11, 12, 14, 12, 16, 15),Block.makeCuboidShape(4, 12, 14, 5, 16, 15)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	
	public BlockAutocraftingTable() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).notSolid().variableOpacity().sound(SoundType.METAL), "autocrafting_table", BlockAutocraftingTable.TEAutocraftingTable.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.MACHINE_ACTIVE, false).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE).add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	@Override
	public boolean isVariableOpacity() {
		return true;
	}
	
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof TEAutocraftingTable) {
				TEAutocraftingTable v = (TEAutocraftingTable) te;
				for(int i = 1; i < 11; i++) {
					v.setInventorySlotContents(i, ItemStack.EMPTY);
				}
				InventoryHelper.dropItems(worldIn, pos, v.getItems());
				worldIn.removeTileEntity(pos);
			}
		}
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
	
	public static class TEAutocraftingTable extends ManagedSidedMachine<ContainerAutocraftingTable> implements ITickableTileEntity{
		
		private static final Integer[] grnSlots = {17, 18, 26, 27};
		private static final Integer[] magSlots = {19, 20, 28, 29};
		private static final Integer[] orgSlots = {21, 22, 30, 31};
		private static final Integer[] allSlots = Stream.of(grnSlots, magSlots, orgSlots).flatMap(Stream::of).toArray(Integer[]::new);
		private HashMap<Integer, ICraftingRecipe> validRecipes = new HashMap<>();
		private HashMap<Integer, ResourceLocation> rawBuiltRecipes = new HashMap<>();
		
		private byte[] outputMode = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		private byte[] slotTargets = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		private ICraftingRecipe prevEnteredRecipe;
		private int selectedRecipe = 0;
		private int timer = 0;
		private int nTimer = 16;
		private int changeModelTimer = 0;
		
		public TEAutocraftingTable(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 32, (TranslationTextComponent) Blocks.CRAFTING_TABLE.getNameTextComponent(), Registry.getContainerId("autocrafting_table"), ContainerAutocraftingTable.class, new EnergyProperties(true, false, 50000));
		}
		
		public TEAutocraftingTable() {
			this(Registry.getTileEntity("autocrafting_table"));
		}

		@Override
		public boolean canInsertToSide(int slot, Direction direction) {
			return (slot > 13 && slot < 17) || (slot > 22 && slot < 26);
		}
		
		@Override
		public void tick() {
			
			if(!world.isRemote) {
				if(timer++ == nTimer) {
					timer = 0;
					
					int availRecip = getUpgradeAmount(Upgrades.AC_RECIPES);
					
					int mul;
					
					
					boolean devicePerformed = false;
					if(getUpgradeAmount(Upgrades.AC_SUSTAINED) > 0) {
						mul = 0;
						nTimer = 24;
					}else {
						switch(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED)) {
						case 3:
							mul = 4;
							nTimer = 2;
							break;
						case 2:
							mul = 3;
							nTimer = 4;
							break;
						case 1:
							mul = 2;
							nTimer = 8;
							break;
						default:
							mul = 1;
							nTimer = 16;
						}
					}
					
					
					int cost = 0;
					for(Integer nX : validRecipes.keySet()) {
						
						if((nX > 1 && availRecip < 1) || (nX > 3 && availRecip < 2) || (nX > 5) && availRecip < 3) {
							validRecipes.remove(nX);
							break;
						}
						ICraftingRecipe recipe = validRecipes.get(nX);
						int tCost = 0;
						boolean validRecipe = true;
						HashMap<ItemStack, Integer> shrinkStacks = new HashMap<>();
						for(Ingredient ing : recipe.getIngredients()) {
							if(ing.getMatchingStacks().length != 0) {
								tCost += 15 * mul;
								boolean foundMatch = false;
								for(int i = 14; i < 32; i++) {
									ItemStack st = this.getStackInSlot(i);
									if(ing.test(st)) {
										if(shrinkStacks.containsKey(st)) {
											
											if(shrinkStacks.get(st) < st.getCount()) {
												shrinkStacks.put(st, shrinkStacks.get(st) + 1);
												foundMatch = true;
												break;
											}
										}else {
											shrinkStacks.put(st, 1);
											foundMatch = true;
											break;
											
										}
										
									}
								}
								
								if(!foundMatch) {
									validRecipe = false;
									break;
								}
							}
							
							
							
						}
						
						if(validRecipe) {
							
							if(amount - cost - tCost < 0) {
								break;
							}
							ItemStack output = recipe.getRecipeOutput().copy();
							
							
							
							if(outputMode[nX] == 1) {
								if(getStackInSlot(0).isEmpty()) {
									for(ItemStack i : shrinkStacks.keySet()) {
										i.shrink(shrinkStacks.get(i));
									}
									cost += tCost;
									devicePerformed = true;
									setInventorySlotContents(0, output);
								}else if(ItemHandlerHelper.canItemStacksStack(output, getStackInSlot(0)) && getStackInSlot(0).getCount() + output.getCount() <= getStackInSlot(0).getMaxStackSize()) {
									for(ItemStack i : shrinkStacks.keySet()) {
										i.shrink(shrinkStacks.get(i));
									}
									cost += tCost;
									devicePerformed = true;
									getStackInSlot(0).grow(output.getCount());
								}
							}else {
								
								Integer[] vals;
								switch(slotTargets[nX]){
								case 3:
									vals = orgSlots;
									break;
								case 2:
									vals = magSlots;
									break;
								case 1:
									vals = grnSlots;
									break;
								default:
									vals = allSlots;
								}
								for(int x : vals) {
									if(getStackInSlot(x).isEmpty()) {
										for(ItemStack i : shrinkStacks.keySet()) {
											i.shrink(shrinkStacks.get(i));
										}
										cost += tCost;
										devicePerformed = true;
										setInventorySlotContents(x, output);
										break;
									}else if(ItemHandlerHelper.canItemStacksStack(output, getStackInSlot(x)) && getStackInSlot(x).getCount() + output.getCount() <= getStackInSlot(x).getMaxStackSize()) {
										for(ItemStack i : shrinkStacks.keySet()) {
											i.shrink(shrinkStacks.get(i));
										}
										cost += tCost;
										devicePerformed = true;
										getStackInSlot(x).grow(output.getCount());
										break;
									}
								}
								
							}
							
							
							
							
							
							
						}
						
					}
					
					
					
					if(devicePerformed == true) {
						amount -= cost;
						
						fept = (float) cost / (float) nTimer;
						
						if(changeModelTimer == 0 && !getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
						}
						changeModelTimer = 15;
						sendUpdates();
					}else {
						fept = 0;
						if(changeModelTimer == 0) {
							if(getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
								world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
								sendUpdates();
							}
							
						}else {
							changeModelTimer--;
						}
					}
					
				}
			}
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			
			compound.putInt("assemblylinemachines:selected", selectedRecipe);
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			
			for(int i : validRecipes.keySet()) {
				
				ICraftingRecipe recipe = validRecipes.get(i);
				if(recipe != null) {
					compound.putString("assemblylinemachines:recipe" + i + "ns", recipe.getId().getNamespace());
					compound.putString("assemblylinemachines:recipe" + i + "p", recipe.getId().getPath());
				}
			}
			
			compound.putByteArray("assemblylinemachines:outputmodes", outputMode);
			compound.putByteArray("assemblylinemachines:slottargets", slotTargets);
			return super.write(compound);
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			selectedRecipe = compound.getInt("assemblylinemachines:selected");
			nTimer = compound.getInt("assemblylinemachines:ntimer");
			
			validRecipes.clear();
			rawBuiltRecipes.clear();
			
			for(int i = 0; i < 10; i++) {
				
				if(world != null) {
					if(compound.contains("assemblylinemachines:recipe" + i + "ns") && compound.contains("assemblylinemachines:recipe" + i + "p")) {
						Optional<? extends IRecipe<?>> rOpt = world.getRecipeManager().getRecipe(new ResourceLocation(compound.getString("assemblylinemachines:recipe" + i + "ns"), compound.getString("assemblylinemachines:recipe" + i + "p")));
						IRecipe<?> recipe = rOpt.orElse(null);
						if(recipe != null && recipe instanceof ICraftingRecipe) {
							validRecipes.put(i, (ICraftingRecipe) recipe);
						}
					}
				}else {
					if(compound.contains("assemblylinemachines:recipe" + i + "ns") && compound.contains("assemblylinemachines:recipe" + i + "p")) {
						
						rawBuiltRecipes.put(i, new ResourceLocation(compound.getString("assemblylinemachines:recipe" + i + "ns"), compound.getString("assemblylinemachines:recipe" + i + "p")));
					}
				}
				
			}
			
			if(compound.contains("assemblylinemachines:outputmodes")) {
				outputMode = compound.getByteArray("assemblylinemachines:outputmodes");
			}
			if(compound.contains("assemblylinemachines:slottargets")) {
				slotTargets = compound.getByteArray("assemblylinemachines:slottargets");
			}
		}
		
		@Override
		public void onLoad() {
			super.onLoad();
			
			for(int n : rawBuiltRecipes.keySet()) {
				Optional<? extends IRecipe<?>> rOpt = world.getRecipeManager().getRecipe(rawBuiltRecipes.get(n));
				IRecipe<?> recipe = rOpt.orElse(null);
				if(recipe != null && recipe instanceof ICraftingRecipe) {
					validRecipes.put(n, (ICraftingRecipe) recipe);
				}
			}
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			
			if(slot > 10 && slot <= 13) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}else {
				return (slot > 13 && slot < 17) || (slot > 22 && slot < 26);
			}
			
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 11; i < 14; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
		
	}
	
	public static class ContainerAutocraftingTable extends ContainerALMBase<TEAutocraftingTable> {
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(62, 120);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(62, 178);
		
		private final AutocrafterInvWrapper wrapper = new AutocrafterInvWrapper(this, tileEntity);
		
		public ContainerAutocraftingTable(final int windowId, final PlayerInventory playerInventory, final TEAutocraftingTable tileEntity) {
			super(Registry.getContainerType("autocrafting_table"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			//Output slot
			this.addSlot(new SlotWithRestrictions(tileEntity, 0, 158, 30, tileEntity, true));
			
			//3x3 grid slots
			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new SlotWithRestrictions(tileEntity, 1 + (row * 3) + col, 98 + (18 * col),
							12 + (18 * row), tileEntity));
				}
			}
			
			//Recipe view, upgrades slots
			this.addSlot(new SlotWithRestrictions(tileEntity, 10, 77, 48, tileEntity));
			this.addSlot(new SlotWithRestrictions(tileEntity, 11, 203, 12, tileEntity));
			this.addSlot(new SlotWithRestrictions(tileEntity, 12, 203, 30, tileEntity));
			this.addSlot(new SlotWithRestrictions(tileEntity, 13, 203, 48, tileEntity));
			
			//Fill input slots
			for (int row = 0; row < 2; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new SlotWithRestrictions(tileEntity, 14 + (row * 9) + col, 61 + (18 * col),
							69 + (18 * row), tileEntity));
				}
			}
			
			//Fill inter. slots, row one 
			for (int col = 0; col < 6; ++col) {
				this.addSlot(new SlotWithRestrictions(tileEntity, 17 + col, 116 + (18 * col),
						69, tileEntity));
			}
			
			//Fill inter. slots, row two
			for (int col = 0; col < 6; ++col) {
				this.addSlot(new SlotWithRestrictions(tileEntity, 26 + col, 116 + (18 * col),
						87, tileEntity));
			}
			
			if(tileEntity.validRecipes.containsKey(tileEntity.selectedRecipe) == false) {
				checkRecipe();
			}
			
		}
		
		public ContainerAutocraftingTable(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEAutocraftingTable.class));
		}
		
		@Override
		public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if(slot > 36 && slot < 46) {
				if(tileEntity.validRecipes.containsKey(tileEntity.selectedRecipe) == false) {
					ItemStack is = player.inventory.getItemStack();
					if(!is.isEmpty()) {
						tileEntity.setInventorySlotContents(slot - 36, new ItemStack(is.getItem(), 1));
					}else {
						tileEntity.setInventorySlotContents(slot - 36, ItemStack.EMPTY);
					}
					
					checkRecipe();
				}
				
				
				return ItemStack.EMPTY;
			}else if(slot == 46) {
				return ItemStack.EMPTY;
			}else if(slot > 46 && slot < 50){
				
				ItemStack st = super.slotClick(slot, dragType, clickTypeIn, player);
				
				int availRecip = tileEntity.getUpgradeAmount(Upgrades.AC_RECIPES);
				int nX = tileEntity.selectedRecipe;
				if((nX > 1 && availRecip < 1) || (nX > 3 && availRecip < 2) || (nX > 5) && availRecip < 3) {
					tileEntity.selectedRecipe = 0;
					tileEntity.sendUpdates();
					for(int i = 1; i < 10; i++) {
						tileEntity.setInventorySlotContents(i, ItemStack.EMPTY);
					}
					tileEntity.setInventorySlotContents(10, ItemStack.EMPTY);
					
					if(tileEntity.validRecipes.containsKey(tileEntity.selectedRecipe)) {
						tileEntity.validRecipes.remove(tileEntity.selectedRecipe);
					}else {
						if(tileEntity.prevEnteredRecipe != null) {
							tileEntity.validRecipes.put(tileEntity.selectedRecipe, tileEntity.prevEnteredRecipe);
							for(Ingredient ing : tileEntity.prevEnteredRecipe.getIngredients()) {
								for(int i = 1; i < 10; i++) {
									ItemStack stack = tileEntity.getStackInSlot(i);
									if(ing.getMatchingStacks().length > 0) {
										if(stack.isEmpty()) {
											tileEntity.setInventorySlotContents(i, ing.getMatchingStacks()[0].copy());
											break;
										}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getMatchingStacks()[0])) {
											tileEntity.getStackInSlot(i).grow(1);
											break;
										}
									}
									
								}
							}
							tileEntity.setInventorySlotContents(10, tileEntity.prevEnteredRecipe.getRecipeOutput());
							tileEntity.prevEnteredRecipe = null;
						}
					}
				}
				return st;
			}else {
				return super.slotClick(slot, dragType, clickTypeIn, player);
			}
			
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			ItemStack itemstack = ItemStack.EMPTY;
			Slot slot = this.inventorySlots.get(index);
			if (slot != null && slot.getHasStack()) {
				ItemStack itemstack1 = slot.getStack();
				itemstack = itemstack1.copy();
				if (index < 36) {
					if (!this.mergeItemStack(itemstack1, 36 + 14, this.inventorySlots.size(), false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
					return ItemStack.EMPTY;
				}

				if (itemstack1.isEmpty()) {
					slot.putStack(ItemStack.EMPTY);
				} else {
					slot.onSlotChanged();
				}
			}

			return itemstack;
		}
		
		private void checkRecipe() {
			Optional<ICraftingRecipe> rOpt = tileEntity.getWorld().getRecipeManager().getRecipe(IRecipeType.CRAFTING, wrapper, tileEntity.getWorld());
			
			ICraftingRecipe recipe = rOpt.orElse(null);
			
			if(recipe != null) {
				
				tileEntity.setInventorySlotContents(10, recipe.getRecipeOutput());
				tileEntity.prevEnteredRecipe = recipe;
			}else {
				tileEntity.setInventorySlotContents(10, ItemStack.EMPTY);
				tileEntity.prevEnteredRecipe = null;
			}
		}
		
		public static class AutocrafterInvWrapper extends CraftingInventory{

			private final IInventory inv;
			public AutocrafterInvWrapper(Container eventHandlerIn, IInventory inv) {
				super(eventHandlerIn, 3, 3);
				this.inv = inv;
			}
			@Override
			public void clear() {
				inv.clear();
				
			}
			@Override
			public int getSizeInventory() {
				return 9;
			}
			
			@Override
			public ItemStack getStackInSlot(int index) {
				if(index < 9) {
					return inv.getStackInSlot(index + 1);
				}
				
				return ItemStack.EMPTY;
				
			}
			@Override
			public ItemStack decrStackSize(int index, int count) {
				return ItemStack.EMPTY;
			}
			@Override
			public ItemStack removeStackFromSlot(int index) {
				return ItemStack.EMPTY;
			}
			@Override
			public void setInventorySlotContents(int index, ItemStack stack) {
				
				
			}
			@Override
			public void markDirty() {
				inv.markDirty();
				
			}
			@Override
			public boolean isUsableByPlayer(PlayerEntity player) {
				return inv.isUsableByPlayer(player);
			}
			
			@Override
			public void fillStackedContents(RecipeItemHelper helper) {
				for(int i = 1; i < 10; i++) {
					helper.accountPlainStack(inv.getStackInSlot(i));
				}
			}
			
			
			
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenAutocraftingTable extends ScreenALMEnergyBased<ContainerAutocraftingTable>{
		
		TEAutocraftingTable tsfm;
		private final HashMap<String, Pair<SimpleButton, SupplierWrapper>> b;
		
		private SimpleButton bSwitch;
		
		public ScreenAutocraftingTable(ContainerAutocraftingTable screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(230, 202), new Pair<>(11, 6), new Pair<>(62, 109), "autocrafting_table", false, new Pair<>(14, 17), screenContainer.tileEntity, 230, true);
			tsfm = screenContainer.tileEntity;
			
			b = new HashMap<>();
		}
		
		@Override
		protected void init() {
			super.init();
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			
			for (int row = 0; row < 5; ++row) {
				for (int col = 0; col < 2; ++col) {
					int bnum = (row * 2) + col;
					b.put("b" + bnum, new Pair<>(new AutocraftingSlotButton(x + 33 + (col * 11), y + 17 + (row * 11), 231 + (col * 11), 53 + (row * 11), null, (button) -> {

						sendACChangeRecipePacket(tsfm.getPos(), bnum);
					}, tsfm, bnum), new SupplierWrapper("Recipe " + bnum, "Recipe " + bnum, () -> bnum == tsfm.selectedRecipe)));
				}
			}
			
			
			b.put("setrecipe", new Pair<>(new SimpleButton(x + 83, y + 14, 244, 203, 11, 11, null, (button) -> {

				tryLockInRecipe(tsfm.getPos());
			}), new SupplierWrapper("Clear Recipe", "Save Recipe", () -> tsfm.validRecipes.containsKey(tsfm.selectedRecipe))));
			
			b.put("output", new Pair<>(new SimpleButton(x + 154, y + 14, 231, 203, 11, 11, null, (button) -> {

				sendOutputModeChangeRequest(tsfm.getPos(), tsfm.selectedRecipe, "setoutputmode");
			}), new SupplierWrapper("Output to Output Slot", "Output to Internal Inventory", () -> tsfm.outputMode[tsfm.selectedRecipe] != 0)));
			
			bSwitch = new SimpleButton(x + 154, y + 51, 0, 0, 11, 11, null, (button) -> {

				sendOutputModeChangeRequest(tsfm.getPos(), tsfm.selectedRecipe, "settargetslots");
			});
			
			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				this.addButton(bb.getFirst());
			}
			
			this.addButton(bSwitch);
			
		}
		
		private static class AutocraftingSlotButton extends SimpleButton{

			private final TEAutocraftingTable te;
			private final int number;
			public AutocraftingSlotButton(int widthIn, int heightIn, int blitx, int blity, String text,
					IPressable onPress, TEAutocraftingTable te, int number) {
				super(widthIn, heightIn, blitx, blity, text, onPress);
				this.te = te;
				this.number = number;
			}
			
			@Override
			protected boolean isValidClickButton(int p_isValidClickButton_1_) {
				
				return isEnabledSlot();
				
			}
			
			
			private boolean isEnabledSlot(){
				if(number < 2) {
					return true;
				}else {
					
					if(te.getUpgradeAmount(Upgrades.AC_SUSTAINED) == 0) {
						if(number < 4) {
							return te.getUpgradeAmount(Upgrades.AC_RECIPES) > 0;
						}else if(number < 6) {
							return te.getUpgradeAmount(Upgrades.AC_RECIPES) > 1;
						}else {
							return te.getUpgradeAmount(Upgrades.AC_RECIPES) > 2;
						}
					}else {
						return false;
					}
				}
			}
				
		}
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			if (renderTitles == true) {
				this.font.drawString(this.title.getFormattedText(), titleTextLoc.getFirst(), titleTextLoc.getSecond(), 4210752);
				this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), invTextLoc.getFirst(),
						invTextLoc.getSecond(), 4210752);
			}
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			
			//Render energy TT if sustained upgrade is not installed.
			if(tsfm.getUpgradeAmount(Upgrades.AC_SUSTAINED) == 0) {
				if (mouseX >= x + energyMeterLoc.getFirst() && mouseY >= y + energyMeterLoc.getSecond() && mouseX <= x + energyMeterLoc.getFirst() + 15 && mouseY <= y + energyMeterLoc.getSecond() + 51) {

					if(Screen.hasShiftDown()) {
						ArrayList<String> str = new ArrayList<>();
						str.add(Formatting.GENERAL_FORMAT.format(machine.amount) + "/" + Formatting.GENERAL_FORMAT.format(machine.properties.getCapacity()) + "FE");
						if(usesfept) {
							
							
							str.add(Formatting.FEPT_FORMAT.format(machine.fept) + " FE/tick");
						}
						this.renderTooltip(str,
								mouseX - x, mouseY - y);
					}else {
						this.renderTooltip(Formatting.formatToSuffix(machine.amount) + "/" + Formatting.formatToSuffix(machine.properties.getCapacity()) + "FE",
								mouseX - x, mouseY - y);
					}
					
				}
			}

			//Render each button upgrade TT.
			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				if (mouseX >= bb.getFirst().x && mouseX <= bb.getFirst().x + bb.getFirst().sizex && mouseY >= bb.getFirst().y && mouseY <= bb.getFirst().y + bb.getFirst().sizey) {
					
					if(!(bb.getFirst() instanceof AutocraftingSlotButton) || ((AutocraftingSlotButton)bb.getFirst()).isEnabledSlot()) {
						if (bb.getSecond() != null) {
							this.renderTooltip(bb.getSecond().getTextFromSupplier(), mouseX - x, mouseY - y);
						} else {
							this.renderTooltip(bb.getFirst().getMessage(), mouseX - x, mouseY - y);
						}
					}
					
					break;
					
				}
					
			}
			
			//Render special filter slot button TT.
			if (mouseX >= bSwitch.x && mouseX <= bSwitch.x + bSwitch.sizex && mouseY >= bSwitch.y && mouseY <= bSwitch.y + bSwitch.sizey) {
				
				
				
				switch(tsfm.slotTargets[tsfm.selectedRecipe]) {
				case 3:
					this.renderTooltip("Output to Orange Slots", mouseX - x, mouseY - y);
					break;
				case 2:
					this.renderTooltip("Output to Magenta Slots", mouseX - x, mouseY - y);
					break;
				case 1:
					this.renderTooltip("Output to Lime Slots", mouseX - x, mouseY - y);
					break;
				default:
					this.renderTooltip("Output to any Slot", mouseX - x, mouseY - y);
					break;
				}
			}
			
			
			
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			for (Pair<SimpleButton, SupplierWrapper> bb : b.values()) {
				
				if(!(bb.getFirst() instanceof AutocraftingSlotButton) || ((AutocraftingSlotButton) bb.getFirst()).isEnabledSlot()) {
					if (bb.getSecond() != null && bb.getSecond().supplier.get()) {
						//blit each button's pressed texture, if available.
						super.blit(bb.getFirst().x, bb.getFirst().y, bb.getFirst().blitx, bb.getFirst().blity, bb.getFirst().sizex, bb.getFirst().sizey);
					}
				}else {
					//if recipe slot is not enabled blit over it to hide.
					super.blit(bb.getFirst().x, bb.getFirst().y, 54, 17, bb.getFirst().sizex, bb.getFirst().sizey);
				}
				

			}
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if(tsfm.getUpgradeAmount(Upgrades.AC_SUSTAINED) != 0) {
				
				//Blit over the energy bar if sustained upgrade is installed.
				super.blit(x+13, y+16, 178, 10, 18, 54);
			}
			
			
			//Blit over the button depending on which mode is selected.
			switch(tsfm.slotTargets[tsfm.selectedRecipe]) {
			case 3:
				super.blit(x+154, y+51, 231, 142, 11, 11);
				break;
			case 2:
				super.blit(x+154, y+51, 231, 129, 11, 11);
				break;
			case 1:
				super.blit(x+154, y+51, 231, 116, 11, 11);
				break;
			}
			
			if (mouseX >= bSwitch.x && mouseX <= bSwitch.x + bSwitch.sizex && mouseY >= bSwitch.y && mouseY <= bSwitch.y + bSwitch.sizey) {
				
				
				//blit overlay for the color slots on the internal inventory.
				//blit lime
				for (int row = 0; row < 2; ++row) {
					for (int col = 0; col < 2; ++col) {
						
						super.blit(x+116+(col * 18), y+69+(row*18), 230, 154, 16, 16);
						
					}
				}
				
				//blit magenta
				for (int row = 0; row < 2; ++row) {
					for (int col = 0; col < 2; ++col) {
						
						super.blit(x+152+(col * 18), y+69+(row*18), 230, 170, 16, 16);
						
					}
				}
				
				//blit orange
				for (int row = 0; row < 2; ++row) {
					for (int col = 0; col < 2; ++col) {
						
						super.blit(x+188+(col * 18), y+69+(row*18), 230, 186, 16, 16);
						
					}
				}
				
				//blit input-only slots.
				for (int row = 0; row < 2; ++row) {
					for (int col = 0; col < 3; ++col) {
						
						super.blit(x+61+(col * 18), y+69+(row*18), 214, 202, 16, 16);
						
					}
				}
			}
			
		}
		
	}
	
	private static void sendACChangeRecipePacket(BlockPos pos, int bNum) {
		
		PacketData pd = new PacketData("autocrafting_gui");
		pd.writeBlockPos("location", pos);
		pd.writeString("button", "setrecipe");
		pd.writeInteger("number", bNum);
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	private static void tryLockInRecipe(BlockPos pos) {
		
		PacketData pd = new PacketData("autocrafting_gui");
		pd.writeBlockPos("location", pos);
		pd.writeString("button", "lock");
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	private static void sendOutputModeChangeRequest(BlockPos pos, int bNum, String button) {
		PacketData pd = new PacketData("autocrafting_gui");
		pd.writeBlockPos("location", pos);
		pd.writeString("button", button);
		pd.writeInteger("number", bNum);
		
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}
	
	public static void updateDataFromPacket(PacketData pd, World world) {
		
		if(pd.getCategory().equals("autocrafting_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			TileEntity tex = world.getTileEntity(pos);
			if(tex instanceof TEAutocraftingTable) {
				TEAutocraftingTable te = (TEAutocraftingTable) tex;
				String b = pd.get("button", String.class);
				if(b.equals("setrecipe")) {
					te.selectedRecipe = pd.get("number", Integer.class);
					for(int i = 1; i < 10; i++) {
						te.setInventorySlotContents(i, ItemStack.EMPTY);
					}
					te.setInventorySlotContents(10, ItemStack.EMPTY);
					
					ICraftingRecipe recipe = te.validRecipes.get(te.selectedRecipe);
					
					if(recipe != null) {
						for(Ingredient ing : recipe.getIngredients()) {
							for(int i = 1; i < 10; i++) {
								ItemStack stack = te.getStackInSlot(i);
								if(ing.getMatchingStacks().length > 0) {
									if(stack.isEmpty()) {
										te.setInventorySlotContents(i, ing.getMatchingStacks()[0].copy());
										break;
									}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getMatchingStacks()[0])) {
										te.getStackInSlot(i).grow(1);
										break;
									}
								}
								
							}
						}
						
						te.setInventorySlotContents(10, recipe.getRecipeOutput());
					}
				}else if(b.equals("lock")) {
					
					for(int i = 1; i < 10; i++) {
						te.setInventorySlotContents(i, ItemStack.EMPTY);
					}
					te.setInventorySlotContents(10, ItemStack.EMPTY);
					
					if(te.validRecipes.containsKey(te.selectedRecipe)) {
						te.validRecipes.remove(te.selectedRecipe);
					}else {
						if(te.prevEnteredRecipe != null) {
							te.validRecipes.put(te.selectedRecipe, te.prevEnteredRecipe);
							for(Ingredient ing : te.prevEnteredRecipe.getIngredients()) {
								for(int i = 1; i < 10; i++) {
									ItemStack stack = te.getStackInSlot(i);
									if(ing.getMatchingStacks().length > 0) {
										if(stack.isEmpty()) {
											te.setInventorySlotContents(i, ing.getMatchingStacks()[0].copy());
											break;
										}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getMatchingStacks()[0])) {
											te.getStackInSlot(i).grow(1);
											break;
										}
									}
									
								}
							}
							te.setInventorySlotContents(10, te.prevEnteredRecipe.getRecipeOutput());
							te.prevEnteredRecipe = null;
						}
					}
					
				}else if(b.equals("setoutputmode")) {
					int i = pd.get("number", Integer.class);
					if(te.outputMode[i] == 0) {
						te.outputMode[i] = 1;
					}else {
						te.outputMode[i] = 0;
					}
				}else if(b.equals("settargetslots")) {
					int i = pd.get("number", Integer.class);
					if(te.slotTargets[i] == 3) {
						te.slotTargets[i] = 0;
					}else {
						te.slotTargets[i]++;
					}
				}
				
				te.sendUpdates();
			}
		}
	}
}
