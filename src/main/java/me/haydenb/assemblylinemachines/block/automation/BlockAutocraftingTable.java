package me.haydenb.assemblylinemachines.block.automation;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.automation.BlockAutocraftingTable.TEAutocraftingTable.SerializableRecipe;
import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.SlotWithRestrictions;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockAutocraftingTable extends BlockScreenBlockEntity<BlockAutocraftingTable.TEAutocraftingTable>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),Block.box(0, 3, 3, 3, 13, 13),
			Block.box(13, 3, 3, 16, 13, 13),Block.box(11, 3, 0, 16, 9, 3),
			Block.box(0, 3, 0, 5, 9, 3),Block.box(0, 3, 13, 5, 9, 16),
			Block.box(11, 3, 13, 16, 9, 16),Block.box(5, 6, 0, 11, 9, 16),
			Block.box(3, 6, 3, 5, 9, 13),Block.box(11, 6, 3, 13, 9, 13),
			Block.box(5, 3, 2, 11, 6, 2),Block.box(5, 3, 14, 11, 6, 14),
			Block.box(5, 3, 1, 6, 6, 2),Block.box(7, 3, 1, 8, 6, 2),
			Block.box(9, 3, 1, 10, 6, 2),Block.box(10, 3, 14, 11, 6, 15),
			Block.box(8, 3, 14, 9, 6, 15),Block.box(6, 3, 14, 7, 6, 15),
			Block.box(3, 9, 0, 6, 12, 3),Block.box(10, 9, 0, 13, 12, 3),
			Block.box(10, 9, 13, 13, 12, 16),Block.box(3, 9, 13, 6, 12, 16),
			Block.box(4, 12, 1, 5, 16, 2),Block.box(11, 12, 1, 12, 16, 2),
			Block.box(11, 12, 14, 12, 16, 15),Block.box(4, 12, 14, 5, 16, 15)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	
	public BlockAutocraftingTable() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).noOcclusion().dynamicShape().sound(SoundType.METAL), "autocrafting_table", BlockAutocraftingTable.TEAutocraftingTable.class);
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
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof TEAutocraftingTable) {
				TEAutocraftingTable v = (TEAutocraftingTable) te;
				for(int i = 1; i < 11; i++) {
					v.setItem(i, ItemStack.EMPTY);
				}
				Containers.dropContents(worldIn, pos, v.getItems());
				worldIn.removeBlockEntity(pos);
			}
		}
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
	
	public static class TEAutocraftingTable extends ManagedSidedMachine<ContainerAutocraftingTable> implements ALMTicker<TEAutocraftingTable>{
		
		private static final Type GSON_TYPE_TOKEN = new TypeToken<HashMap<Integer, SerializableRecipe>>() {
			private static final long serialVersionUID = -5336168241880605825L;}.getType();
		
		private static final Integer[] grnSlots = {17, 18, 26, 27};
		private static final Integer[] magSlots = {19, 20, 28, 29};
		private static final Integer[] orgSlots = {21, 22, 30, 31};
		private static final Integer[] allSlots = Stream.of(grnSlots, magSlots, orgSlots).flatMap(Stream::of).toArray(Integer[]::new);
		
		private HashMap<Integer, SerializableRecipe> serializedRecipes = new HashMap<>();
		
		private byte[] outputMode = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		private byte[] slotTargets = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		private CraftingRecipe prevEnteredRecipe;
		private int selectedRecipe = 0;
		private int timer = 0;
		private int nTimer = 16;
		private int changeModelTimer = 0;
		
		
		public TEAutocraftingTable(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 32, new TranslatableComponent(Blocks.CRAFTING_TABLE.getDescriptionId()), Registry.getContainerId("autocrafting_table"), ContainerAutocraftingTable.class, new EnergyProperties(true, false, 50000), pos, state);
		}
		
		public TEAutocraftingTable(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("autocrafting_table"), pos, state);
		}

		@Override
		public boolean canInsertToSide(int slot, Direction direction) {
			return (slot == -1) || (slot > 13 && slot < 17) || (slot > 22 && slot < 26);
		}
		
		@Override
		public void tick() {
			if(!level.isClientSide) {
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
					for(Integer nX : serializedRecipes.keySet()) {
						
						if((nX > 1 && availRecip < 1) || (nX > 3 && availRecip < 2) || (nX > 5) && availRecip < 3) {
							serializedRecipes.remove(nX);
							break;
						}
						CraftingRecipe recipe = serializedRecipes.get(nX).getRecipe(level);
						int tCost = 0;
						boolean validRecipe = true;
						
						ArrayList<Integer> containerItemSlots = new ArrayList<>();
						HashMap<ItemStack, Integer> shrinkStacks = new HashMap<>();
						for(Ingredient ing : recipe.getIngredients()) {
							if(ing.getItems().length != 0) {
								tCost += 15 * mul;
								boolean foundMatch = false;
								for(int i = 14; i < 32; i++) {
									ItemStack st = this.getItem(i);
									if(ing.test(st)) {
										
										if(st.hasContainerItem()) {
											
											if(!containerItemSlots.contains(i)) {
												containerItemSlots.add(i);
												foundMatch = true;
												break;
											}
										}else {
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
							ItemStack output = recipe.getResultItem().copy();
							
							
							
							if(outputMode[nX] == 1) {
								if(getItem(0).isEmpty()) {
									for(ItemStack i : shrinkStacks.keySet()) {
										i.shrink(shrinkStacks.get(i));
									}
									for(Integer i : containerItemSlots) {
										
										setItem(i, getItem(i).getContainerItem());
									}
									cost += tCost;
									devicePerformed = true;
									setItem(0, output);
								}else if(ItemHandlerHelper.canItemStacksStack(output, getItem(0)) && getItem(0).getCount() + output.getCount() <= getItem(0).getMaxStackSize()) {
									for(ItemStack i : shrinkStacks.keySet()) {
										i.shrink(shrinkStacks.get(i));
									}
									for(Integer i : containerItemSlots) {
										
										setItem(i, getItem(i).getContainerItem());
									}
									cost += tCost;
									devicePerformed = true;
									getItem(0).grow(output.getCount());
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
									if(getItem(x).isEmpty()) {
										for(ItemStack i : shrinkStacks.keySet()) {
												
											i.shrink(shrinkStacks.get(i));
											
										}
										for(Integer i : containerItemSlots) {
											
											setItem(i, getItem(i).getContainerItem());
										}
										cost += tCost;
										devicePerformed = true;
										setItem(x, output);
										break;
									}else if(ItemHandlerHelper.canItemStacksStack(output, getItem(x)) && getItem(x).getCount() + output.getCount() <= getItem(x).getMaxStackSize()) {
										for(ItemStack i : shrinkStacks.keySet()) {
											i.shrink(shrinkStacks.get(i));
										}
										for(Integer i : containerItemSlots) {
											
											setItem(i, getItem(i).getContainerItem());
										}
										cost += tCost;
										devicePerformed = true;
										getItem(x).grow(output.getCount());
										break;
									}
								}
								
							}
							
							
							
							
							
							
						}
						
					}
					
					
					
					if(devicePerformed == true) {
						amount -= cost;
						
						fept = (float) cost / (float) nTimer;
						
						if(changeModelTimer == 0 && !getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
						}
						changeModelTimer = 15;
						sendUpdates();
					}else {
						fept = 0;
						if(changeModelTimer == 0) {
							if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
								sendUpdates();
							}
							
						}else {
							changeModelTimer--;
						}
					}
					
				}else if(timer > 24) {
					timer = 0;
				}
			}
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			
			compound.putInt("assemblylinemachines:selected", selectedRecipe);
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			
			if(!serializedRecipes.isEmpty()) {
				Gson gson = new Gson();
				
				compound.putString("assemblylinemachines:recipes", gson.toJson(serializedRecipes, GSON_TYPE_TOKEN));
			}
			
			
			compound.putByteArray("assemblylinemachines:outputmodes", outputMode);
			compound.putByteArray("assemblylinemachines:slottargets", slotTargets);
			super.saveAdditional(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			selectedRecipe = compound.getInt("assemblylinemachines:selected");
			nTimer = compound.getInt("assemblylinemachines:ntimer");
			
			if(compound.contains("assemblylinemachines:recipes")) {
				Gson gson = new Gson();
				serializedRecipes = gson.fromJson(compound.getString("assemblylinemachines:recipes"), GSON_TYPE_TOKEN);
			}else {
				serializedRecipes = new HashMap<>();
			}
			
			if(compound.contains("assemblylinemachines:outputmodes")) {
				outputMode = compound.getByteArray("assemblylinemachines:outputmodes");
			}
			if(compound.contains("assemblylinemachines:slottargets")) {
				slotTargets = compound.getByteArray("assemblylinemachines:slottargets");
			}
		}
		
		static class SerializableRecipe {
			
			private transient CraftingRecipe internalRecipe = null;
			private final ResourceLocation rl;
			
			public SerializableRecipe(CraftingRecipe recipe) {
				this.rl = recipe.getId();
				this.internalRecipe = recipe;
			}
			
			public CraftingRecipe getRecipe(Level level) {
				if(internalRecipe != null) {
					return internalRecipe;
				}else {
					Recipe<?> rcp = level.getRecipeManager().byKey(rl).orElse(null);
					if(rcp != null && rcp instanceof CraftingRecipe) {
						internalRecipe = (CraftingRecipe) rcp;
						return internalRecipe;
					}
					return null;
				}
				
			}
		}
		
		private SerializableRecipe build(CraftingRecipe recipe) {
			return new SerializableRecipe(recipe);
		}
		
		@Override
		public ItemStack removeItem(int pIndex, int pCount) {
			if(pIndex > 0 && pIndex < 11) {
				return ItemStack.EMPTY;
			}
			return super.removeItem(pIndex, pCount);
		}
		
		@Override
		public ItemStack removeItemNoUpdate(int pIndex) {
			if(pIndex > 0 && pIndex < 11) {
				return ItemStack.EMPTY;
			}
			return super.removeItemNoUpdate(pIndex);
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
		
		public ContainerAutocraftingTable(final int windowId, final Inventory playerInventory, final TEAutocraftingTable tileEntity) {
			super(Registry.getContainerType("autocrafting_table"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 14);
			
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
			
			if(tileEntity.serializedRecipes.containsKey(tileEntity.selectedRecipe) == false) {
				checkRecipe();
			}
			
		}
		
		public ContainerAutocraftingTable(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEAutocraftingTable.class));
		}
		
		@Override
		public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
			if(slot > 36 && slot < 46) {
				if(tileEntity.serializedRecipes.containsKey(tileEntity.selectedRecipe) == false) {
					ItemStack is = this.getCarried();
					if(!is.isEmpty()) {
						tileEntity.setItem(slot - 36, new ItemStack(is.getItem(), 1));
					}else {
						tileEntity.setItem(slot - 36, ItemStack.EMPTY);
					}
					
					checkRecipe();
				}
				
				return;
			}else if(slot == 46) {
				return;
			}else if(slot > 46 && slot < 50){
				
				super.clicked(slot, dragType, clickTypeIn, player);
				
				int availRecip = tileEntity.getUpgradeAmount(Upgrades.AC_RECIPES);
				int nX = tileEntity.selectedRecipe;
				if((nX > 1 && availRecip < 1) || (nX > 3 && availRecip < 2) || (nX > 5) && availRecip < 3) {
					tileEntity.selectedRecipe = 0;
					tileEntity.sendUpdates();
					for(int i = 1; i < 10; i++) {
						tileEntity.setItem(i, ItemStack.EMPTY);
					}
					tileEntity.setItem(10, ItemStack.EMPTY);
					
					if(tileEntity.serializedRecipes.containsKey(tileEntity.selectedRecipe)) {
						tileEntity.serializedRecipes.remove(tileEntity.selectedRecipe);
					}else {
						if(tileEntity.prevEnteredRecipe != null) {
							tileEntity.serializedRecipes.put(tileEntity.selectedRecipe, tileEntity.build(tileEntity.prevEnteredRecipe));
							for(Ingredient ing : tileEntity.prevEnteredRecipe.getIngredients()) {
								for(int i = 1; i < 10; i++) {
									ItemStack stack = tileEntity.getItem(i);
									if(ing.getItems().length > 0) {
										if(stack.isEmpty()) {
											tileEntity.setItem(i, ing.getItems()[0].copy());
											break;
										}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getItems()[0])) {
											tileEntity.getItem(i).grow(1);
											break;
										}
									}
									
								}
							}
							tileEntity.setItem(10, tileEntity.prevEnteredRecipe.getResultItem());
							tileEntity.prevEnteredRecipe = null;
						}
					}
				}
			}else {
				super.clicked(slot, dragType, clickTypeIn, player);
			}
		}
		
		private void checkRecipe() {
			Optional<CraftingRecipe> rOpt = tileEntity.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, wrapper, tileEntity.getLevel());
			
			CraftingRecipe recipe = rOpt.orElse(null);
			
			if(recipe != null) {
				
				tileEntity.setItem(10, recipe.getResultItem());
				tileEntity.prevEnteredRecipe = recipe;
			}else {
				tileEntity.setItem(10, ItemStack.EMPTY);
				tileEntity.prevEnteredRecipe = null;
			}
		}
		
		public static class AutocrafterInvWrapper extends CraftingContainer{

			private final Container inv;
			public AutocrafterInvWrapper(AbstractContainerMenu eventHandlerIn, Container inv) {
				super(eventHandlerIn, 3, 3);
				this.inv = inv;
			}
			
			@Override
			public void clearContent() {
				inv.clearContent();
			}
			
			@Override
			public int getContainerSize() {
				return 9;
			}
			
			@Override
			public ItemStack getItem(int index) {
				if(index < 9) {
					return inv.getItem(index + 1);
				}
				
				return ItemStack.EMPTY;
				
			}
			
			@Override
			public ItemStack removeItem(int pIndex, int pCount) {
				return ItemStack.EMPTY;
			}
			
			@Override
			public ItemStack removeItemNoUpdate(int pIndex) {
				return ItemStack.EMPTY;
			}
			@Override
			public void setItem(int index, ItemStack stack) {
				
				
			}
			
			@Override
			public void setChanged() {
				inv.setChanged();
			}
			
			@Override
			public boolean stillValid(Player pPlayer) {
				return inv.stillValid(pPlayer);
			}
			
			@Override
			public void fillStackedContents(StackedContents helper) {
				for(int i = 1; i < 10; i++) {
					helper.accountSimpleStack(inv.getItem(i));
				}
			}
			
			
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenAutocraftingTable extends ScreenALMEnergyBased<ContainerAutocraftingTable>{
		
		TEAutocraftingTable tsfm;
		
		private TrueFalseButton bSwitch;
		
		public ScreenAutocraftingTable(ContainerAutocraftingTable screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(230, 202), new Pair<>(11, 6), new Pair<>(62, 109), "autocrafting_table", false, new Pair<>(14, 17), screenContainer.tileEntity, 230, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void init() {
			super.init();
			
			int x = this.leftPos;
			int y = this.topPos;
			
			
			for (int row = 0; row < 5; ++row) {
				for (int col = 0; col < 2; ++col) {
					int bnum = (row * 2) + col;
					int bx = x + 33 + (col * 11);
					int by = y + 17 + (row * 11);
					int blitx = 231 + (col * 11);
					int blity = 53 + (row * 11);
					this.addRenderableWidget(new AutocraftingSlotButton(bx, by, blitx, blity, 8, 8, new TrueFalseButtonSupplier("Recipe " + bnum, () -> bnum == tsfm.selectedRecipe), (b) -> sendACChangeRecipePacket(tsfm.getBlockPos(), bnum), tsfm, bnum));
				}
			}
			
			this.addRenderableWidget(new TrueFalseButton(x+83, y+14, 244, 203, 11, 11, new TrueFalseButtonSupplier("Clear Recipe", "Save Recipe", () -> tsfm.serializedRecipes.containsKey(tsfm.selectedRecipe)), (b) -> tryLockInRecipe(tsfm.getBlockPos())));
			this.addRenderableWidget(new TrueFalseButton(x+154, y+14, 231, 203, 11, 11, 
					new TrueFalseButtonSupplier("Output to Output Slot", "Output to Internal Inventory", () -> tsfm.outputMode[tsfm.selectedRecipe] != 0), (b) -> sendOutputModeChangeRequest(tsfm.getBlockPos(), tsfm.selectedRecipe, "setoutputmode")));
			bSwitch = this.addRenderableWidget(new TrueFalseButton(x+154, y+51, 11, 11, null, (b) -> sendOutputModeChangeRequest(tsfm.getBlockPos(), tsfm.selectedRecipe, "settargetslots")));
			
		}
		
		private static class AutocraftingSlotButton extends TrueFalseButton{

			private final TEAutocraftingTable te;
			private final int number;
			public AutocraftingSlotButton(int x, int y, int blitx, int blity, int widthIn, int heightIn, TrueFalseButtonSupplier tfbs, OnPress onPress, TEAutocraftingTable te, int number) {
				super(x, y, blitx, blity, widthIn, heightIn, tfbs, onPress);
				this.te = te;
				this.number = number;
			}
			
			@Override
			protected boolean isValidClickButton(int p_230987_1_) {
				return isEnabledSlot();
			}
			
			@Override
			public int[] getBlitData() {
				if(!isEnabledSlot()) {
					return new int[] {x, y, 54, 17, width, height};
				}
				return super.getBlitData();
			}
			
			@Override
			public boolean getSupplierOutput() {
				if(!isEnabledSlot()) {
					return true;
				}
				
				return super.getSupplierOutput();
			}
			
			@Override
			public void renderToolTip(PoseStack mx, int mouseX, int mouseY) {
				if(isEnabledSlot()) {
					super.renderToolTip(mx, mouseX, mouseY);
				}
			}
			
			private boolean isEnabledSlot(){
				if(number < 2) {
					return true;
				}else {
					
					int rcps = te.getUpgradeAmount(Upgrades.AC_RECIPES);
					if(te.getUpgradeAmount(Upgrades.AC_SUSTAINED) == 0) {
						if(number < 4) {
							return rcps > 0;
						}else if(number < 6) {
							return rcps > 1;
						}else {
							return rcps > 2;
						}
					}else {
						return false;
					}
				}
			}
			
				
		}
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			
			//Render energy TT if sustained upgrade is not installed.
			if(tsfm.getUpgradeAmount(Upgrades.AC_SUSTAINED) == 0) {
				if (mouseX >= x + energyMeterLoc.getFirst() && mouseY >= y + energyMeterLoc.getSecond() && mouseX <= x + energyMeterLoc.getFirst() + 15 && mouseY <= y + energyMeterLoc.getSecond() + 51) {

					if(Screen.hasShiftDown()) {
						ArrayList<String> str = new ArrayList<>();
						str.add(Formatting.GENERAL_FORMAT.format(machine.amount) + "/" + Formatting.GENERAL_FORMAT.format(machine.properties.getCapacity()) + "FE");
						if(usesfept) {
							
							
							str.add(Formatting.FEPT_FORMAT.format(machine.fept) + " FE/tick");
						}
						this.renderComponentTooltip(str,
								mouseX - x, mouseY - y);
					}else {
						this.renderComponentTooltip(Formatting.formatToSuffix(machine.amount) + "/" + Formatting.formatToSuffix(machine.properties.getCapacity()) + "FE",
								mouseX - x, mouseY - y);
					}
					
				}
			}
			
			//Render special filter slot button TT.
			if(bSwitch.isHoveredOrFocused()) {
				switch(tsfm.slotTargets[tsfm.selectedRecipe]) {
				case 3:
					this.renderComponentTooltip("Output to Orange Slots", mouseX - x, mouseY - y);
					break;
				case 2:
					this.renderComponentTooltip("Output to Magenta Slots", mouseX - x, mouseY - y);
					break;
				case 1:
					this.renderComponentTooltip("Output to Lime Slots", mouseX - x, mouseY - y);
					break;
				default:
					this.renderComponentTooltip("Output to any Slot", mouseX - x, mouseY - y);
					break;
				}
			}
			
			
			
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
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
			
			if (bSwitch.isHoveredOrFocused()) {
				
				
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
		pd.writeUtf("button", "setrecipe");
		pd.writeInteger("number", bNum);
		
		PacketHandler.INSTANCE.sendToServer(pd);
	}
	
	private static void tryLockInRecipe(BlockPos pos) {
		
		PacketData pd = new PacketData("autocrafting_gui");
		pd.writeBlockPos("location", pos);
		pd.writeUtf("button", "lock");
		
		PacketHandler.INSTANCE.sendToServer(pd);
	}
	
	private static void sendOutputModeChangeRequest(BlockPos pos, int bNum, String button) {
		PacketData pd = new PacketData("autocrafting_gui");
		pd.writeBlockPos("location", pos);
		pd.writeUtf("button", button);
		pd.writeInteger("number", bNum);
		
		PacketHandler.INSTANCE.sendToServer(pd);
	}
	
	public static void updateDataFromPacket(PacketData pd, Level world) {
		
		if(pd.getCategory().equals("autocrafting_gui")) {
			BlockPos pos = pd.get("location", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
			if(tex instanceof TEAutocraftingTable) {
				TEAutocraftingTable te = (TEAutocraftingTable) tex;
				String b = pd.get("button", String.class);
				if(b.equals("setrecipe")) {
					te.selectedRecipe = pd.get("number", Integer.class);
					for(int i = 1; i < 10; i++) {
						te.setItem(i, ItemStack.EMPTY);
					}
					te.setItem(10, ItemStack.EMPTY);
					
					SerializableRecipe recipA = te.serializedRecipes.get(te.selectedRecipe);
					CraftingRecipe recipe;
					if(recipA != null && (recipe = recipA.getRecipe(te.getLevel())) != null) {
						for(Ingredient ing : recipe.getIngredients()) {
							for(int i = 1; i < 10; i++) {
								ItemStack stack = te.getItem(i);
								if(ing.getItems().length > 0) {
									if(stack.isEmpty()) {
										te.setItem(i, ing.getItems()[0].copy());
										break;
									}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getItems()[0])) {
										te.getItem(i).grow(1);
										break;
									}
								}
								
							}
						}
						
						te.setItem(10, recipe.getResultItem());
					}
				}else if(b.equals("lock")) {
					
					for(int i = 1; i < 10; i++) {
						te.setItem(i, ItemStack.EMPTY);
					}
					te.setItem(10, ItemStack.EMPTY);
					
					if(te.serializedRecipes.containsKey(te.selectedRecipe)) {
						te.serializedRecipes.remove(te.selectedRecipe);
					}else {
						if(te.prevEnteredRecipe != null) {
							te.serializedRecipes.put(te.selectedRecipe, te.build(te.prevEnteredRecipe));
							for(Ingredient ing : te.prevEnteredRecipe.getIngredients()) {
								for(int i = 1; i < 10; i++) {
									ItemStack stack = te.getItem(i);
									if(ing.getItems().length > 0) {
										if(stack.isEmpty()) {
											te.setItem(i, ing.getItems()[0].copy());
											break;
										}else if(ItemHandlerHelper.canItemStacksStack(stack, ing.getItems()[0])) {
											te.getItem(i).grow(1);
											break;
										}
									}
									
								}
							}
							te.setItem(10, te.prevEnteredRecipe.getResultItem());
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
