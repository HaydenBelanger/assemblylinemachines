package me.haydenb.assemblylinemachines.block.helpers;

import java.util.*;
import java.util.function.*;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.SlotWithRestrictions;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.EnergyProperties;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button.OnPress;
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
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.*;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.*;

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
			if(!canRotate) {
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

		public Block build(String teName) {
			return this.build(teName, AbstractMachine.class);
		}

		public <T extends AbstractMachine<?>> Block build(String teName, Class<T> clazz) {

			class MachineBlock extends BlockScreenBlockEntity<T>{

				public MachineBlock(Properties properties, String teName, Class<T> clazz) {
					super(properties, teName, clazz);
					if(activeProperty || shapes.size() > 1 || additionalProperties != null) {
						BlockState state = this.stateDefinition.any();
						if(activeProperty) state = state.setValue(StateProperties.MACHINE_ACTIVE, false);
						if(shapes.size() > 1) state = state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
						if(additionalProperties != null) state = additionalProperties.getFirst().apply(state);
						this.registerDefaultState(state);
					}
				}

				@Override
				protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
					if(activeProperty) builder.add(StateProperties.MACHINE_ACTIVE);
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

			return new MachineBlock(properties, teName, clazz);

		}


	}

	public static class MachineBlockEntityBuilder{

		private EnergyProperties energy = new EnergyProperties(true, false, 20000);
		private int baseNTimer = 16;
		private int baseFECost = 100;
		private int upgradeSlotNumber, primaryOutputSlot, capacity, crankThreshold = 0;
		private BiFunction<BlockEntity, Container, Optional<Recipe<Container>>> recipeProcessor = null;
		private float cycleCountModifier = 1f;
		private int secondaryOutputSlot, totalSlots = 1;
		private int dualProcessingOutputSlot = 2;
		private boolean processesFluids, hasExternalTank, crankable, outputToRight, allowedInZero = false;
		private boolean usesFE = true;
		private boolean hasInternalTank = true;
		private BiConsumer<Container, Recipe<Container>> executeOnRecipeCompletion = null;
		private Function<Integer, Integer> slotIDTransformer = null;
		private Function<Integer, Integer> dualFunctionIDTransformer = null;
		private Predicate<Integer> isExtractableSlot = null;
		private BiFunction<Recipe<Container>, BlockState, BlockState> specialStateModifier = null;
		private Function<Integer, List<Integer>> mustBeFullBefore = null;
		private List<List<Integer>> slotDuplicateCheckingGroups = null;
		private TriPredicate<Integer, ItemStack, BlockEntity> slotValidator = null;
		private Pair<Predicate<Integer>, BiFunction<Container, Integer, ItemStack>> getCapturer = null;

		private MachineBlockEntityBuilder() {}

		public MachineBlockEntityBuilder energy(boolean in, boolean out, int capacity) {
			energy = new EnergyProperties(in, out, capacity);
			return this;
		}

		public MachineBlockEntityBuilder energy(int capacity) {
			energy = new EnergyProperties(true, false, capacity);
			return this;
		}

		public MachineBlockEntityBuilder crankMachine(int crankThresholdPerCycle) {
			usesFE = false;
			crankable = true;
			crankThreshold = crankThresholdPerCycle;
			return energy(false, false, 0);
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

		public MachineBlockEntityBuilder slotExtractableFunction(Predicate<Integer> isExtractableSlot) {
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

		public MachineBlockEntityBuilder hasNoInternalTank() {
			this.hasInternalTank = false;
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

		public MachineBlockEntityBuilder slotContentsValidator(TriPredicate<Integer, ItemStack, BlockEntity> validator) {
			this.slotValidator = validator;
			return this;
		}

		public MachineBlockEntityBuilder getCapturer(Predicate<Integer> operateOn, BiFunction<Container, Integer, ItemStack> stackProvider) {
			this.getCapturer = Pair.of(operateOn, stackProvider);
			return this;
		}

		public MachineBlockEntityBuilder outputToRight() {
			this.outputToRight = true;
			this.primaryOutputSlot = -1;
			this.secondaryOutputSlot = -1;
			this.dualProcessingOutputSlot = -1;
			return this;
		}

		public MachineBlockEntityBuilder allowedInZero() {
			this.allowedInZero = true;
			return this;
		}

		public BlockEntityType<?> build(String blockName, Block... validBlocks) {

			class MachineBlockEntity extends ManagedSidedMachine<AbstractContainerMenu> implements ALMTicker<MachineBlockEntity>, IMachineDataBridge, ICrankableMachine{

				private int timer, nTimer, cranks;
				private float progress, cycles;
				private ItemStack output = ItemStack.EMPTY;
				private ItemStack secondaryOutput = ItemStack.EMPTY;
				private ItemStack dualProcessorOutput = ItemStack.EMPTY;
				private Container processorContainer = null;
				private Container dualFunctionProcessorContainer = null;
				private LazyOptional<IFluidHandler> internalLazy = null;
				private LazyOptional<IFluidHandler> externalLazy = null;
				private LazyOptional<IItemHandler> rightOutput = null;
				private FluidStack internalTank = FluidStack.EMPTY;
				private boolean useInternalTank = true;
				private boolean allowCranks = false;
				private int mkiiPneumaticCompressorMold = 0;

				public MachineBlockEntity(BlockPos pos, BlockState state) {
					super(Registry.getBlockEntity(blockName), totalSlots, Component.translatable(Registry.getBlock(blockName).getDescriptionId())
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
							if(allowCranks) {
								allowCranks = false;
								sendUpdates = true;
							}
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
											if(specialStateModifier != null) this.getLevel().setBlockAndUpdate(this.getBlockPos(), specialStateModifier.apply(recipe, this.blockState()));
											sendUpdates = true;
										}
									}
								}

							}else if(!output.isEmpty() || !secondaryOutput.isEmpty() || !dualProcessorOutput.isEmpty()){
								if(!usesFE || amount - cost >= 0) {
									if(!crankable || cranks >= crankThreshold) {
										if(progress >= cycles) {
											List<Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>>> outputs = new ArrayList<>();
											if(!output.isEmpty()) outputs.add(Triple.of(() -> primaryOutputSlot, () -> output, (v) -> output = ItemStack.EMPTY));
											if(!secondaryOutput.isEmpty()) outputs.add(Triple.of(() -> secondaryOutputSlot, () -> secondaryOutput, (v) -> secondaryOutput = ItemStack.EMPTY));
											if(!dualProcessorOutput.isEmpty()) outputs.add(Triple.of(() -> dualProcessingOutputSlot, () -> dualProcessorOutput, (v) -> dualProcessorOutput = ItemStack.EMPTY));
											ListIterator<Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>>> listIterator = outputs.listIterator();
											while(listIterator.hasNext()) {
												Triple<Supplier<Integer>, Supplier<ItemStack>, Consumer<Void>> outputVals = listIterator.next();
												if(outputToRight) {
													IItemHandler handler = getOrCreateRightOutput();
													if(handler != null) {
														ItemStack resultOfInsert = Utils.attemptDepositIntoAllSlots(outputVals.getMiddle().get(), handler);
														if(resultOfInsert.isEmpty()) {
															outputVals.getRight().accept(null);
															listIterator.remove();
														}else {
															outputVals.getMiddle().get().setCount(resultOfInsert.getCount());
														}
													}
												}else {
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
											}
											if(outputs.isEmpty()) {
												cycles = 0;
												progress = 0;
												if(crankable) allowCranks = true;

											}
										}else {
											if(usesFE) {
												amount -= cost;
												fept = (float) cost / (float) nTimer;
											}
											progress++;
										}
										cranks = 0;
									}
								}

								sendUpdates = true;
							}

							if(blockState().hasProperty(StateProperties.MACHINE_ACTIVE)) {
								boolean machineActive = blockState().getValue(StateProperties.MACHINE_ACTIVE);
								if((machineActive && cycles == 0) || (!machineActive && cycles != 0)) {
									this.getLevel().setBlockAndUpdate(this.getBlockPos(), blockState().setValue(StateProperties.MACHINE_ACTIVE, !machineActive));
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
				public boolean validFrom(Direction dir) {
					return crankable;
				}

				@Override
				public boolean requiresGearbox() {
					return false;
				}

				@Override
				public boolean perform() {
					if(allowCranks) return true;
					if(output.isEmpty()) return false;
					cranks++;
					return true;
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
						if(!slotValidator.test(slot, stack, this)) return false;
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

					if(slot == 0 && allowedInZero) return true;
					return super.isAllowedInSlot(slot, stack);
				}

				@Override
				public boolean canInsertToSide(boolean isEnergy, int slot, Direction direction) {
					if(isEnergy && !usesFE) return false;
					if(isEnergy || isExtractableSlot == null) return super.canInsertToSide(isEnergy, slot, direction);
					if(!enabledSides.getOrDefault(direction, true)) return false;
					return !isExtractableSlot.test(slot);
				}

				@Override
				public boolean canExtractFromSide(boolean isEnergy, int slot, Direction direction) {
					if(isEnergy && !usesFE) return false;
					if(isEnergy || isExtractableSlot == null) return super.canExtractFromSide(isEnergy, slot, direction);
					if(!enabledSides.getOrDefault(direction, true)) return false;
					return isExtractableSlot.test(slot);
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
					cranks = compound.getInt("assemblylinemachines:cranks");
					allowCranks = compound.getBoolean("assemblylinemachines:allowcranks");
					mkiiPneumaticCompressorMold = compound.getInt("assemblylinemachines:mkiipneumaticmold");
				}

				@Override
				public void saveAdditional(CompoundTag compound) {
					compound.putInt("assemblylinemachines:ntimer", nTimer);
					if(cycles != 0) compound.putFloat("assemblylinemachines:cycles", cycles);
					if(progress != 0) compound.putFloat("assemblylinemachines:progress", progress);
					if(!output.isEmpty()) compound.put("assemblylinemachines:output", output.save(new CompoundTag()));
					if(!secondaryOutput.isEmpty()) compound.put("assemblylinemachines:secondaryoutput", secondaryOutput.save(new CompoundTag()));
					if(!dualProcessorOutput.isEmpty()) compound.put("assemblylinemachines:dualprocessoroutput", dualProcessorOutput.save(new CompoundTag()));
					if(!internalTank.isEmpty()) compound.put("assemblylinemachines:fluid", internalTank.writeToNBT(new CompoundTag()));
					if(useInternalTank) compound.putBoolean("assemblylinemachines:useinternaltank", useInternalTank);
					if(cranks != 0) compound.putInt("assemblylinemachines:cranks", cranks);
					if(allowCranks) compound.putBoolean("assemblylinemachines:allowcranks", allowCranks);
					if(mkiiPneumaticCompressorMold != 0) compound.putInt("assemblylinemachines:mkiipneumaticmold", mkiiPneumaticCompressorMold);

					super.saveAdditional(compound);
				}

				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
					if(!usesFE && cap == CapabilityEnergy.ENERGY) return LazyOptional.empty();
					if(processesFluids && hasInternalTank && useInternalTank && cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return LazyOptional.of(() -> getCraftingFluidHandler(Optional.of(true))).cast();
					return super.getCapability(cap, side);
				}

				@Override
				public float getCycles() {
					return this.cycles;
				}

				@Override
				public float getProgress() {
					return this.progress;
				}

				@Override
				public void setSecondaryOutput(ItemStack output) {
					this.secondaryOutput = output;
				}

				@Override
				public void setCycles(float cycles) {
					if(this.cycles < (cycles * cycleCountModifier)) this.cycles = (cycles * cycleCountModifier);
				}

				@Override
				public boolean getUsingInternalTank() {
					if(!hasExternalTank) return true;
					if(!hasInternalTank) return false;
					return useInternalTank;
				}

				@Override
				public BlockState blockState() {
					return this.getBlockState();
				}

				@Override
				public IFluidHandler getCraftingFluidHandler(Optional<Boolean> preferInternal) {

					boolean useInternal = preferInternal.isPresent() ? preferInternal.get() : getUsingInternalTank();

					if(useInternal && hasInternalTank) {
						if(internalLazy == null) {
							internalLazy = LazyOptional.of(() -> IFluidHandlerBypass.getSimpleOneTankHandler(null, capacity, (oFs) -> {
								if(oFs.isPresent()) internalTank = oFs.get();
								return internalTank;
							}, (v) -> this.sendUpdates(), false));
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
				public void receiveButtonPacket(PacketData pd) {
					if(pd.get("function", String.class).equals("toggle_source_tank")) {
						this.useInternalTank = !this.useInternalTank;
						this.sendUpdates();
					}else if(pd.get("function", String.class).equals("change_pneumatic_mold")) {
						this.getOrSetMKIIPCSelMold(Optional.of(pd.get("mold", Integer.class)));
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
				public int getOrSetMKIIPCSelMold(Optional<Integer> set) {
					if(set.isPresent()) {
						mkiiPneumaticCompressorMold = mkiiPneumaticCompressorMold == set.get() ? 0 : set.get();
						this.sendUpdates();
					}
					return mkiiPneumaticCompressorMold;
				}

				private IItemHandler getOrCreateRightOutput() {
					if(rightOutput != null && rightOutput.isPresent()) return rightOutput.orElse(null);
					Direction toRight = this.blockState().getValue(HorizontalDirectionalBlock.FACING).getCounterClockWise();
					BlockEntity be = this.getLevel().getBlockEntity(this.getBlockPos().relative(toRight));
					if(be != null) {
						LazyOptional<?> handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, toRight.getOpposite());
						if(handler.isPresent()) {
							this.rightOutput = handler.cast();
							this.rightOutput.addListener(new NonNullConsumer<LazyOptional<IItemHandler>>() {
								@Override
								public void accept(LazyOptional<IItemHandler> t) {
									MachineBlockEntity.this.rightOutput = null;
								}
							});
							return rightOutput.orElse(null);
						}
					}
					return null;
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
						if(getCapturer != null && getCapturer.getFirst().test(intIn)) return getCapturer.getSecond().apply(MachineBlockEntity.this, intIn);
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
					public boolean isAllowedInSlot(int slot, ItemStack stack) {
						return MachineBlockEntity.this.isAllowedInSlot(slot, stack);
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
					public boolean getUsingInternalTank() {
						return MachineBlockEntity.this.getUsingInternalTank();
					}

					@Override
					public void receiveButtonPacket(PacketData pd) {
						MachineBlockEntity.this.receiveButtonPacket(pd);
					}

					@Override
					public BlockState blockState() {
						return MachineBlockEntity.this.blockState();
					}

					@Override
					public int getOrSetMKIIPCSelMold(Optional<Integer> set) {
						return MachineBlockEntity.this.getOrSetMKIIPCSelMold(set);
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
			public boolean getUsingInternalTank();
			public void receiveButtonPacket(PacketData pd);
			public BlockState blockState();
			public int getOrSetMKIIPCSelMold(Optional<Integer> set);
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

			class MachineContainer extends ContainerALMBase<RandomizableContainerBlockEntity>{

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
			}

			return IForgeMenuType.create((windowId, inv, data) -> new MachineContainer(windowId, inv, data));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class MachineScreenBuilder{

		private Pair<Integer, Integer> inventorySize = Pair.of(176, 166);
		private Pair<Integer, Integer> inventoryTitleLoc = Pair.of(11, 6);
		private Pair<Integer, Integer> playerInvTitleLoc = Pair.of(11, 73);
		private Pair<Integer, Integer> energyBarLoc = Pair.of(14, 17);
		private boolean usesFept = true;
		private boolean usesFe = true;
		private boolean renderTitleText = true;
		private boolean renderInventoryText = true;
		private List<ProgressBar> progressBars = new ArrayList<>();
		private int energyMeterStartX = 176;
		private Function<BlockState, Pair<Integer, Integer>> uvModifier = null;
		private ToIntBiFunction<Integer, Integer> pbSlotChecker = (blitx, blity) -> {
			throw new IllegalArgumentException("Unexpected request: " + blitx + ", " + blity);
		};
		private boolean renderFluidBar = false;
		private boolean hasCoolMode = false;
		private int fluidBarTopLeftX = 0;
		private int fluidBarTopLeftY = 0;
		private int fluidBarHeight = 0;
		private int fluidOverlayStartX = 0;
		private int fluidOverlayStartY = 0;
		private int[] tankButtonStats = null;
		private boolean isMkiiPneumaticCompressor = false;

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

		public MachineScreenBuilder addBar(int blitx, int blity, int uvx, int uvy, int defaultWidth, int defaultHeight, int slotToCheck) {
			ToIntBiFunction<Integer, Integer> previous = pbSlotChecker;
			pbSlotChecker = (cbx, cby) -> {
				if(cbx == blitx && cby == blity) return slotToCheck;
				return previous.applyAsInt(cbx, cby);
			};
			return this.addBar(blitx, blity, uvx, uvy, defaultWidth, defaultHeight, PBDirection.SLOT_EMPTY);

		}

		public MachineScreenBuilder addBar(int blitx, int blity, int uvx, int uvy, int defaultWidth, int defaultHeight, PBDirection direction) {
			return this.addBar(blitx, blity, uvx, uvy, defaultWidth, defaultHeight, direction, 0, 0, List.of());
		}

		public MachineScreenBuilder addBar(int blitx, int blity, int uvx, int uvy, int defaultWidth, int defaultHeight, PBDirection direction, int frames, int frameTime, List<Pair<Integer, Integer>> duplicateAt) {
			this.progressBars.add(new ProgressBar(blitx, blity, uvx, uvy, defaultWidth, defaultHeight, direction, frames, frameTime, duplicateAt));
			return this;
		}

		public MachineScreenBuilder stateBasedBlitPieceModifier(Function<BlockState, Pair<Integer, Integer>> function) {
			this.uvModifier = function;
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

		public MachineScreenBuilder doesNotUseFE() {
			this.usesFe = false;
			return doesNotUseFEPT();
		}

		public MachineScreenBuilder doesNotUseFEPT() {
			this.usesFept = false;
			return this;
		}

		public MachineScreenBuilder defaultMKIIOptions() {
			this.renderInventoryText = false;
			this.renderTitleText = false;
			return this.inventorySize(190, 188).energyMeterStartX(190);
		}

		public MachineScreenBuilder mkiiPneumaticCompressorButtons() {
			this.isMkiiPneumaticCompressor = true;
			return this;
		}

		@SuppressWarnings("unchecked")
		public void buildAndRegister(String blockName) {

			class MachineScreen extends ScreenALMEnergyBased<ContainerALMBase<RandomizableContainerBlockEntity>>{

				private IMachineDataBridge data;
				private Cache<Fluid, TextureAtlasSprite> cache = CacheBuilder.newBuilder().build();
				private ArrayList<ProgressBarInstance> progressBars = new ArrayList<>();

				public MachineScreen(ContainerALMBase<RandomizableContainerBlockEntity> screenContainer, Inventory inv, Component titleIn) {
					super(screenContainer, inv, titleIn, inventorySize, inventoryTitleLoc, playerInvTitleLoc, blockName, hasCoolMode,
							energyBarLoc, (EnergyMachine<?>) screenContainer.tileEntity, usesFept);
					this.data = (IMachineDataBridge) screenContainer.tileEntity;
					this.renderTitleText = MachineScreenBuilder.this.renderTitleText;
					this.renderInventoryText = MachineScreenBuilder.this.renderInventoryText;
					this.startx = energyMeterStartX;

					for(ProgressBar pb : MachineScreenBuilder.this.progressBars) {
						this.progressBars.add(new ProgressBarInstance(this, pb));
					}

					this.usesFe = MachineScreenBuilder.this.usesFe;
				}

				@Override
				protected void init() {
					super.init();


					if(tankButtonStats != null) this.addRenderableWidget(new TrueFalseButton(leftPos+tankButtonStats[0], topPos+tankButtonStats[1], tankButtonStats[2], tankButtonStats[3], tankButtonStats[4], tankButtonStats[5],
							new TrueFalseButtonSupplier("Draw From Internal Tank", "Draw From External Tank", () -> data.getUsingInternalTank()), (b) -> {
						PacketData packet = new PacketData("machine_builder_gui");
						packet.writeBlockPos("pos", ((BlockEntity) data).getBlockPos());
						packet.writeString("function", "toggle_source_tank");
						PacketHandler.INSTANCE.sendToServer(packet);
					}));

					if(isMkiiPneumaticCompressor) {
						OnPress pcOp = (b) -> {
							int bid = switch(((TrueFalseButton) b).blity) {
							case 72 -> 1;
							case 84 -> 2;
							case 96 -> 3;
							default -> 0;};

							PacketData packet = new PacketData("machine_builder_gui");
							packet.writeBlockPos("pos", ((BlockEntity) data).getBlockPos());
							packet.writeString("function", "change_pneumatic_mold");
							packet.writeInteger("mold", bid);
							PacketHandler.INSTANCE.sendToServer(packet);
						};

						this.addRenderableWidget(new TrueFalseButton(leftPos+82, topPos+37, 190, 72, 12, 12, new TrueFalseButtonSupplier("Rod Mold (Selected)", "Rod Mold", () -> data.getOrSetMKIIPCSelMold(Optional.empty()) == 1), pcOp));
						this.addRenderableWidget(new TrueFalseButton(leftPos+82, topPos+50, 190, 84, 12, 12, new TrueFalseButtonSupplier("Plate Mold (Selected)", "Plate Mold", () -> data.getOrSetMKIIPCSelMold(Optional.empty()) == 2), pcOp));
						this.addRenderableWidget(new TrueFalseButton(leftPos+82, topPos+63, 190, 96, 12, 12, new TrueFalseButtonSupplier("Gear Mold (Selected)", "Gear Mold", () -> data.getOrSetMKIIPCSelMold(Optional.empty()) == 3), pcOp));
					}
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
						try { super.blit(x+fluidBarTopLeftX, y+fluidBarTopLeftY, fluidBarHeight, fluidBarHeight, fluidBarHeight, cache.get(fluid, () -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(RenderProperties.get(fluid).getStillTexture())));
						}catch(Exception e) {e.printStackTrace();}
					}

					super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

					if(renderFluidBar && handler != null) {

						int fAmount = Math.round(((float) handler.getFluidInTank(0).getAmount() / (float) handler.getTankCapacity(0)) * fluidBarHeight);
						super.blit(x+fluidBarTopLeftX, y+fluidBarTopLeftY, fluidOverlayStartX, fluidOverlayStartY, 8, fluidBarHeight - fAmount);
					}

					for(ProgressBarInstance pbi : this.progressBars) pbi.render(x, y);
				}

				@Override
				protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
					super.drawGuiContainerForegroundLayer(mouseX, mouseY);

					int x = (this.width - this.imageWidth) / 2;
					int y = (this.height - this.imageHeight) / 2;

					if(ScreenMath.isMouseBetween(x, y, mouseX, mouseY, fluidBarTopLeftX, fluidBarTopLeftY, fluidBarTopLeftX + 8, fluidBarTopLeftY + fluidBarHeight)) {
						IFluidHandler handler = data.getCraftingFluidHandler(Optional.of(true));
						if(renderFluidBar) {
							if(handler != null && handler.getFluidInTank(0).getAmount() > 0) {
								this.renderComponentTooltip(List.of(handler.getFluidInTank(0).getDisplayName().getString(),
										Screen.hasShiftDown() ? FormattingHelper.FEPT_FORMAT.format(handler.getFluidInTank(0).getAmount()) + " mB" :
											FormattingHelper.FEPT_FORMAT.format(handler.getFluidInTank(0).getAmount() / 1000D) + " B"), mouseX - x, mouseY - y);
							}else {
								this.renderComponentTooltip("Empty", mouseX - x, mouseY - y);
							}
						}
					}
				}
			}

			MenuScreens.register((MenuType<ContainerALMBase<RandomizableContainerBlockEntity>>)Registry.getContainerType(blockName),
					new ScreenConstructor<ContainerALMBase<RandomizableContainerBlockEntity>, AbstractContainerScreen<ContainerALMBase<RandomizableContainerBlockEntity>>>() {

				@Override
				public AbstractContainerScreen<ContainerALMBase<RandomizableContainerBlockEntity>> create(ContainerALMBase<RandomizableContainerBlockEntity> container, Inventory pInv, Component component) {
					return new MachineScreen(container, pInv, component);
				}

			});
		}

		class ProgressBarInstance{

			private final IMachineDataBridge bridge;
			private final ScreenALMEnergyBased<ContainerALMBase<RandomizableContainerBlockEntity>> screen;
			private final ProgressBar pb;
			private int frame, tick = 0;

			ProgressBarInstance(ScreenALMEnergyBased<ContainerALMBase<RandomizableContainerBlockEntity>> screen, ProgressBar pb){
				this.bridge = (IMachineDataBridge) screen.getMenu().tileEntity;
				this.screen = screen;
				this.pb = pb;
			}

			void render(int x, int y) {

				if(pb.frames != 0) {
					if(tick++ >= pb.frameTime) {
						tick = 0;
						frame = frame >= pb.frames ? 0 : frame + 1;
					}
				}

				this.render(x, y, this.pb.blitx, this.pb.blity, true);
			}

			void render(int x, int y, int blitx, int blity, boolean renderDuplicates) {
				float progress = bridge.getProgress();
				float cycles = bridge.getCycles();

				if((progress != 0f && cycles != 0f) || pb.direction == PBDirection.SLOT_EMPTY) {
					Pair<Integer, Integer> pieces = uvModifier == null ? Pair.of(pb.uvx, pb.uvy) : uvModifier.apply(screen.getMenu().tileEntity.getBlockState());
					int piecex = pieces.getFirst();
					int piecey = pieces.getSecond();

					switch(pb.direction) {
					case LR -> {
						int prog = Math.min(Math.round((progress/cycles) * pb.defaultWidth), pb.defaultWidth);
						screen.blit(x+blitx, y+blity, piecex, piecey + (pb.defaultHeight * this.frame), prog, pb.defaultHeight);
					}
					case UD -> {
						int prog = Math.min(Math.round((progress/cycles) * pb.defaultHeight), pb.defaultHeight);
						screen.blit(x+blitx, y+blity, piecex, piecey + (pb.defaultHeight * this.frame), pb.defaultWidth, prog);
					}
					case DU -> {
						int prog = Math.min(Math.round((progress/cycles) * pb.defaultHeight), pb.defaultHeight);
						screen.blit(x+blitx, y+blity + (pb.defaultHeight - prog), piecex, piecey + (pb.defaultHeight - prog) + (pb.defaultHeight * this.frame), pb.defaultWidth, prog);
					}
					case STATIC -> {
						screen.blit(x+blitx, y+blity, piecex, piecey + (pb.defaultHeight * this.frame), pb.defaultWidth, pb.defaultHeight);
					}
					case SLOT_EMPTY -> {
						if(screen.getMenu().tileEntity.getItem(pbSlotChecker.applyAsInt(blitx, blity)).isEmpty())
						screen.blit(x+blitx, y+blity, piecex, piecey + (pb.defaultHeight * this.frame), pb.defaultWidth, pb.defaultHeight);
					}}

					if(renderDuplicates) for(Pair<Integer, Integer> pair : pb.duplicateAt) this.render(x, y, pair.getFirst(), pair.getSecond(), false);
				}
			}
		}

		public enum PBDirection{

			LR,UD, DU, STATIC, SLOT_EMPTY;
		}

		record ProgressBar(int blitx, int blity, int uvx, int uvy, int defaultWidth, int defaultHeight, PBDirection direction, int frames, int frameTime, List<Pair<Integer, Integer>> duplicateAt) {}
	}
}
