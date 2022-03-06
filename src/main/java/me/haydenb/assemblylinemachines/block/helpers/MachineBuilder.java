package me.haydenb.assemblylinemachines.block.helpers;

import java.lang.annotation.*;
import java.util.*;
import java.util.function.*;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.SlotWithRestrictions;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.EnergyProperties;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineContainerBuilder.IContainerDataBridge;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Utils.*;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.PropertyDispatch.QuadFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class MachineBuilder {

	public static MachineBlockBuilder block() {
		return new MachineBlockBuilder();
	}
	
	public static MachineBlockEntityBuilder blockEntity() {
		return new MachineBlockEntityBuilder();
	}
	
	public static MachineContainerBuilder container() {
		return new MachineContainerBuilder();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static MachineScreenBuilder screen() {
		return new MachineScreenBuilder();
	}
	
	public static class MachineBlockBuilder {
		
		private boolean activeProperty = false;
		private Properties properties = Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL);
		private HashMap<Direction, VoxelShape> shapes = new HashMap<>();
		private Pair<Function<BlockState, BlockState>, Consumer<Builder<Block, BlockState>>> additionalProperties = null;
		private QuadFunction<BlockState, Level, BlockPos, Player, InteractionResult> rightClickAction = null;
		private Pair<Boolean, BiFunction<BlockState, Direction, Boolean>> crankable = null;
		
		private MachineBlockBuilder() {}
		
		public MachineBlockBuilder properties(Properties properties) {
			this.properties = properties;
			return this;
		}
		
		public MachineBlockBuilder hasActiveProperty() {
			this.activeProperty = true;
			return this;
		}
		
		public MachineBlockBuilder voxelShape(VoxelShape northShape, boolean canRotate) {
			if(canRotate == false) {
				shapes.put(Direction.NORTH, northShape);
			}else {
				shapes.put(Direction.NORTH, northShape);
				shapes.put(Direction.SOUTH, Utils.rotateShape(Direction.NORTH, Direction.SOUTH, northShape));
				shapes.put(Direction.EAST, Utils.rotateShape(Direction.NORTH, Direction.EAST, northShape));
				shapes.put(Direction.WEST, Utils.rotateShape(Direction.NORTH, Direction.WEST, northShape));
			}
			return this;
		}
		
		public MachineBlockBuilder additionalProperties(Function<BlockState, BlockState> defaults, Consumer<Builder<Block, BlockState>> blockStateDefinition) {
			additionalProperties = Pair.of(defaults, blockStateDefinition);
			return this;
		}
		
		public MachineBlockBuilder rightClickAction(QuadFunction<BlockState, Level, BlockPos, Player, InteractionResult> function) {
			rightClickAction = function;
			return this;
		}
		
		public MachineBlockBuilder crankable(boolean requiresGearbox, BiFunction<BlockState, Direction, Boolean> validSides) {
			crankable = Pair.of(requiresGearbox, validSides);
			return this;
		}
		
		public Block build(String teName) {
			return this.build(teName, AbstractMachine.class);
		}
		
		public <T extends AbstractMachine<?>> Block build(String teName, Class<T> clazz) {
			
			class MachineBlock extends BlockScreenBlockEntity<T>{

				public MachineBlock(Properties properties, String teName, Class<T> clazz) {
					super(properties, teName, clazz);
					if(activeProperty == true || shapes.size() > 1 || additionalProperties != null) {
						BlockState state = this.stateDefinition.any();
						if(activeProperty == true) state = state.setValue(StateProperties.MACHINE_ACTIVE, false);
						if(shapes.size() > 1) state = state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
						if(additionalProperties != null) state = additionalProperties.getFirst().apply(state);
						this.registerDefaultState(state);
					}
				}
				
				@Override
				protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
					if(activeProperty == true) builder.add(StateProperties.MACHINE_ACTIVE);
					if(shapes.size() > 1) builder.add(HorizontalDirectionalBlock.FACING);
					if(additionalProperties != null) additionalProperties.getSecond().accept(builder);
					super.createBlockStateDefinition(builder);
				}
				
				@Override
				public BlockState getStateForPlacement(BlockPlaceContext context) {
					return shapes.size() > 1 ? super.getStateForPlacement(context).setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite()) : super.getStateForPlacement(context);
				}
				
				@Override
				public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos,
						CollisionContext context) {
					return switch(shapes.size()) {
					case 0 -> super.getShape(state, worldIn, pos, context);
					case 1 -> shapes.get(Direction.NORTH);
					default -> shapes.get(state.getValue(HorizontalDirectionalBlock.FACING));
					};
				}
				
				@Override
				public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos,
						Player player) {
					if(rightClickAction != null) {
						InteractionResult result = rightClickAction.apply(state, world, pos, player);
						if(result != null) return result;
					}
					return super.blockRightClickServer(state, world, pos, player);
				}
				
			}
			
			if(crankable != null) {
				class CrankableMachineBlock extends MachineBlock implements ICrankableBlock{

					public CrankableMachineBlock(Properties properties, String teName, Class<T> clazz) {
						super(properties, teName, clazz);
					}

					@Override
					public boolean validSide(BlockState state, Direction dir) {
						return crankable.getSecond() != null ? crankable.getSecond().apply(state, dir) : true;
					}

					@Override
					public boolean needsGearbox() {
						return crankable.getFirst();
					}
					
					
					
				}
				
				return new CrankableMachineBlock(properties, teName, clazz);
			}else {
				return new MachineBlock(properties, teName, clazz);
			}
			
		}
		
		
	}
	
	public static class MachineBlockEntityBuilder{
		
		private EnergyProperties energy = new EnergyProperties(true, false, 20000);
		private int baseNTimer = 16;
		private int baseFECost = 100;
		private BiFunction<BlockEntity, Container, Optional<Recipe<Container>>> recipeProcessor = null;
		private int totalSlots = 1;
		private int upgradeSlotNumber = 0;
		private BiConsumer<Container, Recipe<Container>> executeOnRecipeCompletion = null;
		private Function<Integer, Integer> slotIDTransformer = null;
		private Function<Integer, Integer> dualFunctionIDTransformer = null;
		private Function<Integer, Boolean> isExtractableSlot = null;
		private BiFunction<Recipe<Container>, BlockState, BlockState> specialStateModifier = null;
		private float cycleCountModifier = 1f;
		private int primaryOutputSlot = 0;
		private int secondaryOutputSlot = 1;
		private int dualProcessingOutputSlot = 2;
		private boolean processesFluids = false;
		private int capacity = 0;
		private boolean hasExternalTank = false;
		private Function<Integer, List<Integer>> mustBeFullBefore = null;
		private List<List<Integer>> slotDuplicateCheckingGroups = null;
		private TriFunction<Integer, ItemStack, BlockEntity, Boolean> slotValidator = null;
		
		private MachineBlockEntityBuilder() {}
		
		public MachineBlockEntityBuilder energy(boolean in, boolean out, int capacity) {
			energy = new EnergyProperties(in, out, capacity);
			return this;
		}
		
		public MachineBlockEntityBuilder energy(int capacity) {
			energy = new EnergyProperties(true, false, capacity);
			return this;
		}
		
		public MachineBlockEntityBuilder baseProcessingStats(int feCost, int nTimer) {
			this.baseNTimer = nTimer;
			this.baseFECost = feCost;
			return this;
		}
		
		public MachineBlockEntityBuilder recipeProcessor(BiFunction<BlockEntity, Container, Optional<Recipe<Container>>> recipeProcessor) {
			this.recipeProcessor = recipeProcessor;
			return this;
		}
		
		public MachineBlockEntityBuilder slotInfo(int totalSlots, int upgradeSlotNumber) {
			this.totalSlots = totalSlots;
			this.upgradeSlotNumber = upgradeSlotNumber;
			return this;
		}
		
		public MachineBlockEntityBuilder executeOnRecipeCompletion(BiConsumer<Container, Recipe<Container>> execute) {
			this.executeOnRecipeCompletion = execute;
			return this;
		}
		
		public MachineBlockEntityBuilder slotIDTransformer(Function<Integer, Integer> slotTransformer) {
			this.slotIDTransformer = slotTransformer;
			return this;
		}
		
		public MachineBlockEntityBuilder specialStateModifier(BiFunction<Recipe<Container>, BlockState, BlockState> specialStateModifier) {
			this.specialStateModifier = specialStateModifier;
			return this;
		}
		
		public MachineBlockEntityBuilder slotExtractableFunction(Function<Integer, Boolean> isExtractableSlot) {
			this.isExtractableSlot = isExtractableSlot;
			return this;
		}
		
		public MachineBlockEntityBuilder dualProcessorIDTransformer(Function<Integer, Integer> slotTransformer) {
			this.dualFunctionIDTransformer = slotTransformer;
			return this;
		}
		
		public MachineBlockEntityBuilder outputSlots(int primary, int secondary, int dualProcessing) {
			this.primaryOutputSlot = primary;
			this.secondaryOutputSlot = secondary;
			this.dualProcessingOutputSlot = dualProcessing;
			return this;
		}
		
		public MachineBlockEntityBuilder cycleCountModifier(float cycleCountModifier) {
			this.cycleCountModifier = cycleCountModifier;
			return this;
		}
		
		public MachineBlockEntityBuilder processesFluids(int capacity, boolean hasExternalTank) {
			this.processesFluids = true;
			this.hasExternalTank = hasExternalTank;
			this.capacity = capacity;
			return this;
		}
		
		public MachineBlockEntityBuilder duplicateCheckingGroups(List<List<Integer>> slotGroups) {
			this.slotDuplicateCheckingGroups = slotGroups;
			return this;
		}
		
		public MachineBlockEntityBuilder duplicateCheckingGroup(List<Integer> slotGroup) {
			this.slotDuplicateCheckingGroups = List.of(slotGroup);
			return this;
		}
		
		public MachineBlockEntityBuilder mustBeFullBefore(Function<Integer, List<Integer>> mustBeFullBefore) {
			this.mustBeFullBefore = mustBeFullBefore;
			return this;
		}
		
		public MachineBlockEntityBuilder slotContentsValidator(TriFunction<Integer, ItemStack, BlockEntity, Boolean> validator) {
			this.slotValidator = validator;
			return this;
		}
		
		public BlockEntityType<?> build(String blockName, Block... validBlocks) {
			
			class MachineBlockEntity extends ManagedSidedMachine<AbstractContainerMenu> implements ALMTicker<MachineBlockEntity>, IMachineDataBridge{

				private int timer, nTimer;
				private float progress, cycles;
				private ItemStack output = ItemStack.EMPTY;
				private ItemStack secondaryOutput = ItemStack.EMPTY;
				private ItemStack dualProcessorOutput = ItemStack.EMPTY;
				private Container processorContainer = null;
				private Container dualFunctionProcessorContainer = null;
				private LazyOptional<IFluidHandler> internalLazy = null;
				private LazyOptional<IFluidHandler> externalLazy = null;
				private FluidStack internalTank = FluidStack.EMPTY;
				private boolean useInternalTank = true;
				
				public MachineBlockEntity(BlockPos pos, BlockState state) {
					super(Registry.getBlockEntity(blockName), totalSlots, new TranslatableComponent(Registry.getBlock(blockName).getDescriptionId())
							, Registry.getContainerId(blockName), AbstractContainerMenu.class, new EnergyProperties(false, false, 0), pos, state);
					this.properties = MachineBlockEntityBuilder.this.energy;
				}
				
				@Override
				public AbstractContainerMenu createMenu(int id, Inventory player) {
					FriendlyByteBuf fbb = new FriendlyByteBuf(Unpooled.buffer());
					fbb.writeBlockPos(this.getBlockPos());
					return Registry.getContainerType(blockName).create(id, player, fbb);
				}
				
				@Override
				public void tick() {
					if(!level.isClientSide) {
						if(timer++ >= nTimer) {
							timer = 0;
							boolean sendUpdates = false;
							int speedUpgrades = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
							nTimer = speedUpgrades == 0 ? baseNTimer : Math.max(1, Math.round(baseNTimer / speedUpgrades));
							int cost = (int) (speedUpgrades == 0 ? baseFECost : Math.round(((speedUpgrades + 1) * baseFECost) * Math.pow(1.1d, speedUpgrades + 1)));
							if(output.isEmpty() && secondaryOutput.isEmpty() && dualProcessorOutput.isEmpty()) {
								if(processorContainer == null) processorContainer = slotIDTransformer == null ? this : new SlotTransformer(slotIDTransformer);
								if(dualFunctionProcessorContainer == null && dualFunctionIDTransformer != null) dualFunctionProcessorContainer = new SlotTransformer(dualFunctionIDTransformer);
								List<Triple<Optional<Recipe<Container>>, Consumer<ItemStack>, Supplier<Container>>> recipes = new ArrayList<>();
								recipes.add(Triple.of(recipeProcessor.apply(this, processorContainer), (stack) -> output = stack, () -> processorContainer));
								if(dualFunctionProcessorContainer != null) recipes.add(Triple.of(recipeProcessor.apply(this, dualFunctionProcessorContainer), (stack) -> dualProcessorOutput = stack, () -> dualFunctionProcessorContainer));
								for(Triple<Optional<Recipe<Container>>, Consumer<ItemStack>, Supplier<Container>> optRecipePair : recipes) {
									if(optRecipePair.getLeft().isPresent()) {
										Recipe<Container> recipe = optRecipePair.getLeft().get();
										ItemStack result = recipe.assemble(optRecipePair.getRight().get());
										if(!result.isEmpty()) {
											optRecipePair.getMiddle().accept(result);
											if(executeOnRecipeCompletion != null) executeOnRecipeCompletion.accept(optRecipePair.getRight().get(), recipe);
											if(specialStateModifier != null) this.getLevel().setBlockAndUpdate(this.getBlockPos(), specialStateModifier.apply(recipe, this.getBlockState()));
											sendUpdates = true;
										}
									}
								}
								
							}else if(!output.isEmpty() || !secondaryOutput.isEmpty() || !dualProcessorOutput.isEmpty()){
								if(amount - cost >= 0) {
									if(progress >= cycles) {
										List<Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>>> outputs = new ArrayList<>();
										if(!output.isEmpty()) outputs.add(Triple.of(() -> primaryOutputSlot, () -> output, (v) -> output = ItemStack.EMPTY));
										if(!secondaryOutput.isEmpty()) outputs.add(Triple.of(() -> secondaryOutputSlot, () -> secondaryOutput, (v) -> secondaryOutput = ItemStack.EMPTY));
										if(!dualProcessorOutput.isEmpty()) outputs.add(Triple.of(() -> dualProcessingOutputSlot, () -> dualProcessorOutput, (v) -> dualProcessorOutput = ItemStack.EMPTY));
										ListIterator<Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>>> listIterator = outputs.listIterator();
										while(listIterator.hasNext()) {
											Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>> outputVals = listIterator.next();
											ItemStack curStack = contents.get(outputVals.getLeft().get());
											ItemStack targStack = outputVals.getMiddle().get();
											if(curStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(curStack, targStack) && curStack.getCount() + targStack.getCount() <= curStack.getMaxStackSize())){
												if(curStack.isEmpty()) {
													contents.set(outputVals.getLeft().get(), targStack);
												}else {
													curStack.grow(targStack.getCount());
												}
												outputVals.getRight().accept(null);
												listIterator.remove();
											}
										}
										if(outputs.isEmpty()) {
											cycles = 0;
											progress = 0;
										}
									}else {
										amount -= cost;
										fept = (float) cost / (float) nTimer;
										progress++;
									}
								}
								
								sendUpdates = true;
							}
							
							if(getBlockState().hasProperty(StateProperties.MACHINE_ACTIVE)) {
								boolean machineActive = getBlockState().getValue(StateProperties.MACHINE_ACTIVE);
								if((machineActive == true && cycles == 0) || (machineActive == false && cycles != 0)) {
									this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, !machineActive));
									sendUpdates = true;
								}
							}
							
							if(sendUpdates) {
								sendUpdates();
							}
							
						}
					}
				}
				
				
				@Override
				public int getUpgradeAmount(Upgrades upgrade) {
					if(upgradeSlotNumber == 0) return 0;
					int ii = 0;
					for (int i = totalSlots - upgradeSlotNumber; i < totalSlots; i++) {
						if (Upgrades.match(contents.get(i)) == upgrade) {
							ii++;
						}
					}

					return ii;
				}
				
				@Override
				public boolean isAllowedInSlot(int slot, ItemStack stack) {
					if(upgradeSlotNumber != 0 && (slot >= totalSlots - upgradeSlotNumber)) {
						return (stack.getItem() instanceof ItemUpgrade);
					}
					
					if(mustBeFullBefore != null) {
						List<Integer> res = mustBeFullBefore.apply(slot);
						if(res != null) {
							slotCheck:{
								for(Integer slots : res) {
									if(!contents.get(slots).isEmpty()) break slotCheck;
								}
								return false;
							}
						}
					}
					if(slotValidator != null) {
						if(!slotValidator.apply(slot, stack, this)) return false;
					}
					
					if(slotDuplicateCheckingGroups != null) {
						for(List<Integer> groups : slotDuplicateCheckingGroups) {
							if(groups.contains(slot)) {
								for(Integer slots : groups) {
									if(slot != slots && contents.get(slots).getItem().equals(stack.getItem())) return false;
								}
							}
						}
					}
					return super.isAllowedInSlot(slot, stack);
				}
				
				@Override
				public boolean canInsertToSide(boolean isEnergy, int slot, Direction direction) {
					if(isEnergy || isExtractableSlot == null) return super.canInsertToSide(isEnergy, slot, direction);
					if(enabledSides.getOrDefault(direction, true) == false) return false;
					return !isExtractableSlot.apply(slot);
				}
				
				@Override
				public boolean canExtractFromSide(boolean isEnergy, int slot, Direction direction) {
					if(isEnergy || isExtractableSlot == null) return super.canExtractFromSide(isEnergy, slot, direction);
					if(enabledSides.getOrDefault(direction, true) == false) return false;
					return isExtractableSlot.apply(slot);
				}
				
				@Override
				public void load(CompoundTag compound) {
					super.load(compound);
					
					output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
					secondaryOutput = ItemStack.of(compound.getCompound("assemblylinemachines:secondaryoutput"));
					dualProcessorOutput = ItemStack.of(compound.getCompound("assemblylinemachines:dualprocessoroutput"));
					nTimer = compound.getInt("assemblylinemachines:ntimer");
					cycles = compound.getFloat("assemblylinemachines:cycles");
					progress = compound.getFloat("assemblylinemachines:progress");
					internalTank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluid"));
					useInternalTank = compound.getBoolean("assemblylinemachines:useinternaltank");
				}
				
				@Override
				public void saveAdditional(CompoundTag compound) {
					compound.putInt("assemblylinemachines:ntimer", nTimer);
					compound.putFloat("assemblylinemachines:cycles", cycles);
					compound.putFloat("assemblylinemachines:progress", progress);
					compound.put("assemblylinemachines:output", output.save(new CompoundTag()));
					compound.put("assemblylinemachines:secondaryoutput", secondaryOutput.save(new CompoundTag()));
					compound.put("assemblylinemachines:dualprocessoroutput", dualProcessorOutput.save(new CompoundTag()));
					compound.put("assemblylinemachines:fluid", internalTank.writeToNBT(new CompoundTag()));
					compound.putBoolean("assemblylinemachines:useinternaltank", useInternalTank);
					
					super.saveAdditional(compound);
				}
				
				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
					if(processesFluids && cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
						if(useInternalTank) return LazyOptional.of(() -> getCraftingFluidHandler(Optional.of(true))).cast();
					}
					return super.getCapability(cap, side);
				}
				
				@Override
				public float getCycles() {
					return cycles;
				}

				@Override
				public float getProgress() {
					return progress;
				}
				
				@Override
				public void setCycles(float cycles) {
					if(this.cycles < (cycles * cycleCountModifier)) this.cycles = (cycles * cycleCountModifier);
				}
				
				@Override
				public void setSecondaryOutput(ItemStack output) {
					this.secondaryOutput = output;
				}
				
				@Override
				public IFluidHandler getCraftingFluidHandler(Optional<Boolean> preferInternal) {
					
					boolean useInternal = preferInternal.isPresent() ? preferInternal.get() : useInternalTank;
					
					if(useInternal) {
						if(internalLazy == null) {
							internalLazy = LazyOptional.of(() -> new MachineFluidHandler());
							internalLazy.addListener((lazy) -> internalLazy = null);
						}
						return internalLazy.orElse(null);
					}else if(!useInternal && hasExternalTank) {
						if(externalLazy == null) {
							if(this.getLevel().getBlockEntity(this.getBlockPos().below()) != null) {
								externalLazy = this.getLevel().getBlockEntity(this.getBlockPos().below()).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP);
								if(!externalLazy.isPresent()) {
									externalLazy = null;
									return null;
								}else {
									externalLazy.addListener((lazy) -> externalLazy = null); 
								}
							}else {
								return null;
							}
						}
						return externalLazy.orElse(null);
					}
					return null;
				}
				
				@Override
				public boolean getUsingExternalTank() {
					return !useInternalTank;
				}
				
				@Override
				public void receiveButtonPacket(PacketData pd) {
					if(pd.get("function", String.class).equals("toggle_source_tank")) {
						this.useInternalTank = !this.useInternalTank;
						this.sendUpdates();
					}
				}
				
				class SlotTransformer implements Container, IMachineDataBridge{

					private final Function<Integer, Integer> transformer;
					
					private SlotTransformer(Function<Integer, Integer> transformer) {
						this.transformer = transformer;
					}
					@Override
					public void clearContent() {
						MachineBlockEntity.this.clearContent();
					}

					@Override
					public int getContainerSize() {
						return MachineBlockEntity.this.getContainerSize();
					}

					@Override
					public boolean isEmpty() {
						return MachineBlockEntity.this.isEmpty();
					}

					@Override
					public ItemStack getItem(int intIn) {
						return MachineBlockEntity.this.getItem(transformer.apply(intIn));
					}

					@Override
					public ItemStack removeItem(int intIn, int state) {
						return MachineBlockEntity.this.removeItem(transformer.apply(intIn), state);
					}

					@Override
					public ItemStack removeItemNoUpdate(int intIn) {
						return MachineBlockEntity.this.removeItemNoUpdate(transformer.apply(intIn));
					}

					@Override
					public void setItem(int intIn, ItemStack stack) {
						MachineBlockEntity.this.setItem(transformer.apply(intIn), stack);
					}

					@Override
					public void setChanged() {
						MachineBlockEntity.this.setChanged();
					}

					@Override
					public boolean stillValid(Player p_18946_) {
						return MachineBlockEntity.this.stillValid(p_18946_);
					}
					
					@Override
					public void setCycles(float cycles) {
						MachineBlockEntity.this.setCycles(cycles);
					}
					
					@Override
					public int getUpgradeAmount(Upgrades upgrade) {
						return MachineBlockEntity.this.getUpgradeAmount(upgrade);
					}
					
					@Override
					public boolean isAllowedInSlot(int slot, ItemStack stack) {
						return MachineBlockEntity.this.isAllowedInSlot(slot, stack);
					}
					
					@Override
					public float getProgress() {
						return MachineBlockEntity.this.getProgress();
					}
					
					@Override
					public float getCycles() {
						return MachineBlockEntity.this.getCycles();
					}
					
					@Override
					public void setSecondaryOutput(ItemStack output) {
						MachineBlockEntity.this.setSecondaryOutput(output);
					}
					
					@Override
					public IFluidHandler getCraftingFluidHandler(Optional<Boolean> preferInternal) {
						return MachineBlockEntity.this.getCraftingFluidHandler(preferInternal);
					}
					
					@Override
					public boolean getUsingExternalTank() {
						return MachineBlockEntity.this.getUsingExternalTank();
					}
					
					@Override
					public void receiveButtonPacket(PacketData pd) {
						MachineBlockEntity.this.receiveButtonPacket(pd);
					}
				}
				
				class MachineFluidHandler implements IFluidHandler{

					@Override
					public int getTanks() {
						return 1;
					}

					@Override
					public FluidStack getFluidInTank(int tank) {
						return internalTank;
					}

					@Override
					public int getTankCapacity(int tank) {
						return capacity;
					}

					@Override
					public boolean isFluidValid(int tank, FluidStack stack) {
						return internalTank.isEmpty() || stack.getFluid().equals(internalTank.getFluid());
					}

					@Override
					public int fill(FluidStack resource, FluidAction action) {
						int toFill = !isFluidValid(0, resource) ? 0 : resource.getAmount() + internalTank.getAmount() >= capacity ? capacity - internalTank.getAmount() : resource.getAmount();
						if(toFill != 0 && action == FluidAction.EXECUTE) {
							if(internalTank.isEmpty()) {
								internalTank = new FluidStack(resource, toFill);
							}else {
								internalTank.grow(toFill);
							}
							MachineBlockEntity.this.sendUpdates();
						}
						return toFill;
					}

					@Override
					public FluidStack drain(FluidStack resource, FluidAction action) {
						if(internalTank.isEmpty() || !resource.getFluid().equals(internalTank.getFluid())) return FluidStack.EMPTY;
						if(resource.getAmount() >= internalTank.getAmount()) resource.setAmount(internalTank.getAmount());
						FluidStack returnStack = new FluidStack(internalTank.getFluid(), resource.getAmount());
						if(action == FluidAction.EXECUTE) {
							internalTank.shrink(returnStack.getAmount());
							MachineBlockEntity.this.sendUpdates();
						}
						return returnStack;
					}

					@Override
					public FluidStack drain(int maxDrain, FluidAction action) {
						return internalTank.isEmpty() ? FluidStack.EMPTY : drain(new FluidStack(internalTank.getFluid(), maxDrain), action);
					}
					
				}
			}
			
			if(validBlocks.length == 0) validBlocks = new Block[]{Registry.getBlock(blockName)};
			return BlockEntityType.Builder.of((pos, state) -> new MachineBlockEntity(pos, state), validBlocks).build(null);
		}
		
		public static interface IMachineDataBridge {
			
			public void setCycles(float cycles);
			
			public int getUpgradeAmount(Upgrades upgrade);
			
			public boolean isAllowedInSlot(int slot, ItemStack stack);
			
			public float getProgress();
			
			public float getCycles();
			
			public void setSecondaryOutput(ItemStack output);
			
			public IFluidHandler getCraftingFluidHandler(Optional<Boolean> preferInternal);
			
			public boolean getUsingExternalTank();
			
			public void receiveButtonPacket(PacketData pd);
		}
	}
	
	public static class MachineContainerBuilder{
		
		private Pair<Integer, Integer> invPos = new Pair<>(8, 84);
		private Pair<Integer, Integer> hotbarPos = new Pair<>(8, 142);
		private int disallowedMergeBottomUp = 0;
		private int disallowedMergeTopDown = 1;
		private List<Triple<Integer, Integer, Boolean>> slotCoordinates = List.of();
		
		private MachineContainerBuilder() {}
		
		public MachineContainerBuilder playerInventoryPos(int topLeftX, int topLeftY) {
			this.invPos = Pair.of(topLeftX, topLeftY);
			return this;
		}
		
		public MachineContainerBuilder playerHotbarPos(int topLeftX, int topLeftY) {
			this.hotbarPos = Pair.of(topLeftX, topLeftY);
			return this;
		}
		
		public MachineContainerBuilder shiftMergeableSlots(int disallowedMergeBottomUp, int disallowedMergeTopDown) {
			this.disallowedMergeBottomUp = disallowedMergeBottomUp;
			this.disallowedMergeTopDown = disallowedMergeTopDown;
			return this;
		}
		
		public MachineContainerBuilder slotCoordinates(List<Triple<Integer, Integer, Boolean>> slotCoordinates) {
			this.slotCoordinates = slotCoordinates;
			return this;
		}
		
		public MenuType<?> build(String blockName){
			
			class MachineContainer extends ContainerALMBase<RandomizableContainerBlockEntity> implements IContainerDataBridge{
				
				public MachineContainer(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
					this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data));
				}
				
				public MachineContainer(int windowId, Inventory playerInventory, RandomizableContainerBlockEntity tileEntity) {
					super(Registry.getContainerType(blockName), windowId, tileEntity, playerInventory, invPos, hotbarPos, disallowedMergeBottomUp, disallowedMergeTopDown);
					
					IMachineDataBridge bridge = (IMachineDataBridge) tileEntity;
					
					int i = 0;
					for(Triple<Integer, Integer, Boolean> slot : slotCoordinates) {
						this.addSlot(new SlotWithRestrictions(this.tileEntity, i, slot.getLeft(), slot.getMiddle(), bridge, slot.getRight()));
						i++;
					}
				}

				@Override
				public IMachineDataBridge getEntityDataBridge() {
					return (IMachineDataBridge) this.tileEntity;
				}
				
				@Override
				public BlockEntity getBlockEntity() {
					return this.tileEntity;
				}
			}
			
			return IForgeMenuType.create((windowId, inv, data) -> new MachineContainer(windowId, inv, data));
		}
		
		public static interface IContainerDataBridge{
			public IMachineDataBridge getEntityDataBridge();
			
			public BlockEntity getBlockEntity();
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class MachineScreenBuilder{
		
		private Pair<Integer, Integer> inventorySize = Pair.of(176, 166);
		private Pair<Integer, Integer> inventoryTitleLoc = Pair.of(11, 6);
		private Pair<Integer, Integer> playerInvTitleLoc = Pair.of(11, 73);
		private boolean hasCoolMode = false;
		private Pair<Integer, Integer> energyBarLoc = Pair.of(14, 17);
		private boolean usesFept = true;
		private int[] blitLRProgressBar = null;
		private int[] blitUDProgressBar = null;
		private Pair<Integer, Integer> blitUDDuplicateBar = null;
		private int framesLR = 0;
		private int frameTimeLR = 0;
		private int framesUD = 0;
		private int frameTimeUD = 0;
		private int framesStatic = 0;
		private int frameTimeStatic = 0;
		private boolean renderTitleText = true;
		private boolean renderInventoryText = true;
		private int energyMeterStartX = 176;
		private int[] blitWhenActive = null;
		private TriConsumer<AbstractContainerScreen<AbstractContainerMenu>, Integer, Integer> backgroundCustomRendering = null;
		private boolean renderFluidBar = false;
		private int fluidBarTopLeftX = 0;
		private int fluidBarTopLeftY = 0;
		private int fluidBarHeight = 0;
		private int fluidOverlayStartX = 0;
		private int fluidOverlayStartY = 0;
		private int[] tankButtonStats = null;
		
		@OnlyIn(Dist.CLIENT)
		private MachineScreenBuilder() {}
		
		public MachineScreenBuilder inventorySize(int width, int height) {
			this.inventorySize = Pair.of(width, height);
			return this;
		}
		
		public MachineScreenBuilder inventoryTitleLoc(int x, int y) {
			this.inventoryTitleLoc = Pair.of(x, y);
			return this;
		}
		
		public MachineScreenBuilder playerInvTitleLoc(int x, int y) {
			this.playerInvTitleLoc = Pair.of(x, y);
			return this;
		}
		
		public MachineScreenBuilder hasCoolMode() {
			this.hasCoolMode = true;
			return this;
		}
		
		public MachineScreenBuilder energyBarLoc(int x, int y) {
			this.energyBarLoc = Pair.of(x, y);
			return this;
		}
		
		public MachineScreenBuilder usesFept(boolean usesFept) {
			this.usesFept = usesFept;
			return this;
		}
		
		public MachineScreenBuilder blitLRProgressBar(int renderX, int renderY, int blitX, int blitY, int maxWidth, int height) {
			this.blitLRProgressBar = new int[] {renderX, renderY, blitX, blitY, maxWidth, height};
			return this;
		}
		
		public MachineScreenBuilder blitUDProgressBar(int renderX, int renderY, int blitX, int blitY, int width, int maxHeight) {
			this.blitUDProgressBar = new int[] {renderX, renderY, blitX, blitY, width, maxHeight};
			return this;
		}
		
		public MachineScreenBuilder blitUDDuplicateBar(int renderX, int renderY) {
			this.blitUDDuplicateBar = Pair.of(renderX, renderY);
			return this;
		}
		
		public MachineScreenBuilder blitUDFrameData(int numberOfAdditionalFrames, int countToCycle) {
			this.framesUD = numberOfAdditionalFrames;
			this.frameTimeUD = countToCycle;
			return this;
		}
		
		public MachineScreenBuilder blitWhenActive(int renderX, int renderY, int blitX, int blitY, int width, int height) {
			this.blitWhenActive = new int[] {renderX, renderY, blitX, blitY, width, height};
			return this;
		}
		
		public MachineScreenBuilder blitWhenActiveFrameData(int numberOfAdditionalFrames, int countToCycle) {
			this.framesStatic = numberOfAdditionalFrames;
			this.frameTimeStatic = countToCycle;
			return this;
		}
		
		public MachineScreenBuilder blitLRFrameData(int numberOfAdditionalFrames, int countToCycle) {
			this.framesLR = numberOfAdditionalFrames;
			this.frameTimeLR = countToCycle;
			return this;
		}
		
		public MachineScreenBuilder addCustomBackgroundRenderer(TriConsumer<AbstractContainerScreen<AbstractContainerMenu>, Integer, Integer> backgroundCustomRendering) {
			this.backgroundCustomRendering = backgroundCustomRendering;
			return this;
		}
		
		public MachineScreenBuilder doNotRenderTitleText() {
			this.renderTitleText = false;
			return this;
		}
		
		public MachineScreenBuilder doNotRenderInventoryText() {
			this.renderInventoryText = false;
			return this;
		}
		
		public MachineScreenBuilder energyMeterStartX(int energyMeterStartX) {
			this.energyMeterStartX = energyMeterStartX;
			return this;
		}
		
		public MachineScreenBuilder renderFluidBar(int topLeftX, int topLeftY, int height, int blitX, int blitY) {
			this.renderFluidBar = true;
			this.fluidBarTopLeftX = topLeftX;
			this.fluidBarTopLeftY = topLeftY;
			this.fluidBarHeight = height;
			this.fluidOverlayStartX = blitX;
			this.fluidOverlayStartY = blitY;
			return this;
		}
		
		public MachineScreenBuilder internalTankSwitchingButton(int x, int y, int activeBlitX, int activeBlitY, int width, int height) {
			this.tankButtonStats = new int[] {x, y, activeBlitX, activeBlitY, width, height};
			return this;
		}
		
		public MachineScreenBuilder defaultMKIIOptions() {
			return this.inventorySize(190, 188).energyMeterStartX(190).doNotRenderInventoryText().doNotRenderTitleText();
		}
		
		@SuppressWarnings("unchecked")
		public void buildAndRegister(String blockName) {
			
			class MachineScreen extends ScreenALMEnergyBased<AbstractContainerMenu> implements IScreenDataBridge{
				
				private IMachineDataBridge data;
				private Cache<Fluid, TextureAtlasSprite> cache = CacheBuilder.newBuilder().build();
				private int currentFrameLR, tickCountLR, currentFrameUD, tickCountUD, currentFrameStatic, tickCountStatic;
				
				public MachineScreen(AbstractContainerMenu screenContainer, Inventory inv, Component titleIn) {
					super(screenContainer, inv, titleIn, inventorySize, inventoryTitleLoc, playerInvTitleLoc, blockName, hasCoolMode,
							energyBarLoc, (EnergyMachine<?>) ((IContainerDataBridge) screenContainer).getBlockEntity(), usesFept);
					this.data = ((IContainerDataBridge) screenContainer).getEntityDataBridge();
					this.renderTitleText = MachineScreenBuilder.this.renderTitleText;
					this.renderInventoryText = MachineScreenBuilder.this.renderInventoryText;
					this.startx = energyMeterStartX;
				}
				
				@Override
				protected void init() {
					super.init();
					
					
					if(tankButtonStats != null) this.addRenderableWidget(new TrueFalseButton(leftPos+tankButtonStats[0], topPos+tankButtonStats[1], tankButtonStats[2], tankButtonStats[3], tankButtonStats[4], tankButtonStats[5],
							new TrueFalseButtonSupplier("Draw From External Tank", "Draw From Internal Tank", () -> data.getUsingExternalTank()), (b) -> {
						PacketData packet = new PacketData("machine_builder_gui");
						packet.writeBlockPos("pos", ((BlockEntity) data).getBlockPos());
						packet.writeUtf("function", "toggle_source_tank");
						PacketHandler.INSTANCE.sendToServer(packet);
					}));
				}
				
				@Override
				protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
					int x = (this.width - this.imageWidth) / 2;
					int y = (this.height - this.imageHeight) / 2;
					IFluidHandler handler = data.getCraftingFluidHandler(Optional.of(true));
					if(renderFluidBar && handler != null && handler.getFluidInTank(0).getAmount() > 0) {
						Fluid fluid = handler.getFluidInTank(0).getFluid();
						if(fluid == Fluids.WATER) RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
						try { super.blit(x+fluidBarTopLeftX, y+fluidBarTopLeftY, fluidBarHeight, fluidBarHeight, fluidBarHeight, cache.get(fluid, () -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture())));
						}catch(Exception e) {e.printStackTrace();}
					}
					
					super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
					
					if(renderFluidBar && handler != null) {
						
						int fAmount = Math.round(((float) handler.getFluidInTank(0).getAmount() / (float) handler.getTankCapacity(0)) * (float) fluidBarHeight);
						super.blit(x+fluidBarTopLeftX, y+fluidBarTopLeftY, fluidOverlayStartX, fluidOverlayStartY, 8, fluidBarHeight - fAmount);
					}
					
					if(blitLRProgressBar != null) {
						if(framesLR != 0) {
							if(tickCountLR++ >= frameTimeLR) {
								tickCountLR = 0;
								currentFrameLR = currentFrameLR >= framesLR ? 0 : currentFrameLR + 1;
							}
							
						}
						int prog = Math.round((data.getProgress()/data.getCycles()) * blitLRProgressBar[4]);
						super.blit(x+blitLRProgressBar[0], y+blitLRProgressBar[1], blitLRProgressBar[2], 
								blitLRProgressBar[3] + (blitLRProgressBar[5] * currentFrameLR), prog, blitLRProgressBar[5]);
					}
					
					if(blitUDProgressBar != null) {
						
						if(framesUD != 0) {
							if(tickCountUD++ >= frameTimeUD) {
								tickCountUD = 0;
								currentFrameUD = currentFrameUD >= framesUD ? 0 : currentFrameUD + 1;
							}
							
						}
						
						int prog = Math.round((data.getProgress()/data.getCycles()) * blitUDProgressBar[5]);
						super.blit(x+blitUDProgressBar[0], y+blitUDProgressBar[1], blitUDProgressBar[2], blitUDProgressBar[3] + (blitUDProgressBar[5] * currentFrameUD), blitUDProgressBar[4], prog);
						if(blitUDDuplicateBar != null) super.blit(x+blitUDDuplicateBar.getFirst(), y+blitUDDuplicateBar.getSecond(), blitUDProgressBar[2], blitUDProgressBar[3] + (blitUDProgressBar[5] * currentFrameUD), 
								blitUDProgressBar[4], prog);
					}
					
					if(blitWhenActive != null && data.getCycles() != 0f) {
						if(framesStatic != 0) {
							if(tickCountStatic++ >= frameTimeStatic) {
								tickCountStatic = 0;
								currentFrameStatic = currentFrameStatic >= framesStatic ? 0 : currentFrameStatic + 1;
							}
							
						}
						super.blit(x+blitWhenActive[0], y+blitWhenActive[1], blitWhenActive[2], 
								blitWhenActive[3] + (blitWhenActive[5] * currentFrameStatic), blitWhenActive[4], blitWhenActive[5]);
					}
					
					if(backgroundCustomRendering != null) backgroundCustomRendering.accept(this, x, y);
				}
				
				@Override
				protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
					super.drawGuiContainerForegroundLayer(mouseX, mouseY);
					
					int x = (this.width - this.imageWidth) / 2;
					int y = (this.height - this.imageHeight) / 2;
					
					if(MathHelper.isMouseBetween(x, y, mouseX, mouseY, fluidBarTopLeftX, fluidBarTopLeftY, fluidBarTopLeftX + 8, fluidBarTopLeftY + fluidBarHeight)) {
						IFluidHandler handler = data.getCraftingFluidHandler(Optional.of(true));
						if(renderFluidBar) {
							if(handler != null && handler.getFluidInTank(0).getAmount() > 0) {
								this.renderComponentTooltip(List.of(handler.getFluidInTank(0).getDisplayName().getString(),
										Screen.hasShiftDown() ? Formatting.FEPT_FORMAT.format(handler.getFluidInTank(0).getAmount()) + " mB" : 
											Formatting.FEPT_FORMAT.format((double)handler.getFluidInTank(0).getAmount() / 1000D) + " B"), mouseX - x, mouseY - y);
							}else {
								this.renderComponentTooltip("Empty", mouseX - x, mouseY - y);
							}
						}
					}
				}

				@Override
				public PoseStack getPoseStack() {
					return this.mx;
				}

				@Override
				public IMachineDataBridge getDataBridge() {
					return data;
				}
			}
			
			MenuScreens.register((MenuType<AbstractContainerMenu>)Registry.getContainerType(blockName), new ScreenConstructor<AbstractContainerMenu, AbstractContainerScreen<AbstractContainerMenu>>() {

				@Override
				public AbstractContainerScreen<AbstractContainerMenu> create(AbstractContainerMenu container, Inventory pInv, Component component) {
					return new MachineScreen(container, pInv, component);
				}
				
			});
		}
		
		@OnlyIn(Dist.CLIENT)
		public static interface IScreenDataBridge{
			
			public PoseStack getPoseStack();
			
			public IMachineDataBridge getDataBridge();
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface RegisterableMachine{
		Phases phase();
		String blockName();
		
		public static enum Phases{
			BLOCK, CONTAINER, BLOCK_ENTITY, SCREEN;
		}
	}
}
