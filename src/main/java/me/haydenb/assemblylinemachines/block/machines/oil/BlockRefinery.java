package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockRefinery.TERefinery;
import me.haydenb.assemblylinemachines.crafting.RefiningCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockRefinery extends BlockScreenBlockEntity<TERefinery> {

	private static final VoxelShape SHAPE_N = Stream.of(Block.box(3, 5, 4, 13, 10, 11), Block.box(3, 5, 2, 5, 10, 4),
			Block.box(11, 5, 2, 13, 10, 4), Block.box(9, 6, 2, 10, 8, 3), Block.box(6, 6, 2, 7, 8, 3),
			Block.box(5, 8, 2, 11, 10, 4), Block.box(5, 5, 2, 11, 6, 4), Block.box(5, 6, 3, 11, 8, 3),
			Block.box(1, 5, 6, 6, 13, 11), Block.box(10, 5, 6, 15, 13, 11), Block.box(3, 10, 5, 4, 14, 6),
			Block.box(12, 10, 5, 13, 14, 6), Block.box(3, 13, 6, 4, 14, 7), Block.box(3, 13, 10, 4, 14, 11),
			Block.box(12, 13, 6, 13, 14, 7), Block.box(12, 13, 10, 13, 14, 11), Block.box(11, 13, 7, 14, 14, 10),
			Block.box(2, 13, 7, 5, 14, 10), Block.box(12, 13, 8, 13, 16, 9), Block.box(3, 13, 8, 4, 16, 9),
			Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 5, 11, 16, 16, 16)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);

	public BlockRefinery() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "refinery",
				BlockRefinery.TERefinery.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE).add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {

		BlockState bs = context.getLevel().getBlockState(context.getClickedPos().above());
		if (bs.getBlock() instanceof BlockRefineryAddon && bs.hasProperty(HorizontalDirectionalBlock.FACING)) {
			return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, bs.getValue(HorizontalDirectionalBlock.FACING));
		} else {
			return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
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

	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null) != null && world.getBlockEntity(pos) instanceof TERefinery) {

			IFluidHandler handler = ((TERefinery) world.getBlockEntity(pos)).fluids;

			if (!handler.getFluidInTank(0).getFluid().getAttributes().isGaseous()) {
				FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
				if (far.isSuccess()) {
					if(!player.isCreative()) {
						if (stack.getCount() == 1) {
							player.getInventory().removeItemNoUpdate(player.getInventory().selected);
						} else {
							stack.shrink(1);
						}
						ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
						
					}
					return InteractionResult.CONSUME;

				}
				FluidActionResult farx = FluidUtil.tryFillContainer(stack, handler, 1000, player, true);
				if (farx.isSuccess()) {
					if(!player.isCreative()) {
						if (stack.getCount() == 1) {
							player.getInventory().removeItemNoUpdate(player.getInventory().selected);
						} else {
							stack.shrink(1);
						}
						ItemHandlerHelper.giveItemToPlayer(player, farx.getResult());
					}
					
					return InteractionResult.CONSUME;
				}
			}
			

		}
		return super.blockRightClickServer(state, world, pos, player);
	}

	@Override
	public void animateTick(BlockState stateIn, Level world, BlockPos pos, Random rand) {

		Block bsu = world.getBlockState(pos.above()).getBlock();
		if (stateIn.getValue(StateProperties.MACHINE_ACTIVE)) {

			if (bsu instanceof BlockRefineryAddon) {
				((BlockRefineryAddon) bsu).animateTickFromBase(stateIn, world, pos.above(), rand);
			} else {
				world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartNext(rand), pos.getY() + getPartNext(rand), pos.getZ() + getPartNext(rand), 0, 0, 0);
			}

		}
		super.animateTick(stateIn, world, pos, rand);
	}

	private static double getPartNext(Random rand) {
		double d = rand.nextDouble();
		if (d < 0.2 || d > 0.8) {
			d = 0.5;
		}
		return d;
	}

	public static class TERefinery extends ManagedSidedMachine<ContainerRefinery> implements ALMTicker<TERefinery> {

		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private boolean showGasMsg = false;

		private RefiningCrafting outputRecipe = null;
		private ResourceLocation outputRecipeRL = null;

		public FluidStack tankin = FluidStack.EMPTY;

		public FluidStack tankouta = FluidStack.EMPTY;
		public FluidStack tankoutb = FluidStack.EMPTY;

		public IFluidHandler fluids = new IFluidHandler() {

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				return true;
			}

			@Override
			public int getTanks() {
				return 3;
			}

			@Override
			public int getTankCapacity(int tank) {
				return 4000;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				if (tank == 0) {
					return tankin;
				} else if (tank == 1) {
					return tankouta;
				} else if (tank == 2) {
					return tankoutb;
				} else {
					return FluidStack.EMPTY;
				}
			}

			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if (!tankin.isEmpty()) {
					if (resource.getFluid() != tankin.getFluid()) {
						return 0;
					}
				}

				int attemptedInsert = resource.getAmount();
				int rmCapacity = getTankCapacity(0) - tankin.getAmount();
				if (rmCapacity < attemptedInsert) {
					attemptedInsert = rmCapacity;
				}

				if (action != FluidAction.SIMULATE) {
					if (tankin.isEmpty()) {
						tankin = new FluidStack(resource.getFluid(), attemptedInsert);
					} else {
						tankin.setAmount(tankin.getAmount() + attemptedInsert);
					}
				}
				sendUpdates();
				return attemptedInsert;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				if (!tankouta.isEmpty()) {
					if (tankouta.getAmount() < maxDrain) {
						maxDrain = tankouta.getAmount();
					}

					Fluid f = tankouta.getFluid();
					if (action != FluidAction.SIMULATE) {
						tankouta.setAmount(tankouta.getAmount() - maxDrain);
					}

					if (tankouta.getAmount() <= 0) {
						tankouta = FluidStack.EMPTY;

					}

					sendUpdates();
					return new FluidStack(f, maxDrain);
				} else if (!tankoutb.isEmpty()) {
					if (tankoutb.getAmount() < maxDrain) {
						maxDrain = tankoutb.getAmount();
					}

					Fluid f = tankoutb.getFluid();
					if (action != FluidAction.SIMULATE) {
						tankoutb.setAmount(tankoutb.getAmount() - maxDrain);
					}

					if (tankoutb.getAmount() <= 0) {
						tankoutb = FluidStack.EMPTY;

					}

					sendUpdates();
					return new FluidStack(f, maxDrain);
				} else {
					return FluidStack.EMPTY;
				}
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
		};
		protected LazyOptional<IFluidHandler> fhandler = LazyOptional.of(() -> fluids);

		@Override
		public void tick() {
			if (!level.isClientSide) {
				if (timer++ == nTimer) {

					boolean sendUpdates = false;
					timer = 0;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					int mulcount = getUpgradeAmount(Upgrades.MACHINE_EXTRA);
					int cost = 800;
					switch (upcount) {
					case 3:
						nTimer = 2;
						cost = 700;
						break;
					case 2:
						nTimer = 4;
						cost = 700;
						break;
					case 1:
						nTimer = 8;
						cost = 700;
						break;
					default:
						nTimer = 16;
					}

					boolean hasGas = false;
					if (getUpgradeAmount(Upgrades.MACHINE_GAS) != 0) {
						hasGas = true;
						cost = Math.round((float) cost * 2.5f);
					}		
					
					//Below determines if the recipe is valid or not. If so, sets outputRecipe to not null.
					if (outputRecipe == null) {

						List<RefiningCrafting> rList = this.getLevel().getRecipeManager().getRecipesFor(RefiningCrafting.REFINING_RECIPE, this, this.getLevel());
						Block b = this.getLevel().getBlockState(this.getBlockPos().above()).getBlock();
						RefiningCrafting recipe = null;
						for (RefiningCrafting r : rList) {
							if (b == r.attachmentBlock) {
								if (!r.itemInput.getFirst().isEmpty()) {

									if (r.itemInput.getFirst().test(getItem(1))) {
										if (!r.fluidInput.getFirst().isEmpty()) {
											if (!tankin.isEmpty() && r.fluidInput.getFirst().isFluidEqual(tankin) && r.fluidInput.getFirst().getAmount() <= tankin.getAmount()) {
												recipe = r;
												break;
											}
										} else {
											recipe = r;
											break;
										}
									}

								} else {
									if (!tankin.isEmpty() && r.fluidInput.getFirst().isFluidEqual(tankin) && r.fluidInput.getFirst().getAmount() <= tankin.getAmount()) {
										recipe = r;
										break;
									}
								}
							}
						}

						if (recipe != null) {
							if (!recipe.fluidInput.getFirst().isEmpty()) {
								float chance = recipe.fluidInput.getSecond();
								switch (getUpgradeAmount(Upgrades.MACHINE_CONSERVATION)) {
								case 3:
									chance = chance * 2f;
									break;
								case 2:
									chance = chance * 1.5f;
									break;
								case 0:
									chance = 0f;
								}
								if (General.RAND.nextFloat() < chance) {
									tankin.shrink(Math.round((float) recipe.fluidInput.getFirst().getAmount() / 3f));
								} else {
									tankin.shrink(recipe.fluidInput.getFirst().getAmount());
								}

							}

							if (!recipe.itemInput.getFirst().isEmpty()) {

								float chance = recipe.itemInput.getSecond();
								switch (getUpgradeAmount(Upgrades.MACHINE_CONSERVATION)) {
								case 3:
									chance = chance * 2f;
									break;
								case 2:
									chance = chance * 1.5f;
									break;
								case 0:
									chance = 0f;
								}

								if (!(General.RAND.nextFloat() < chance)) {
									getItem(1).shrink(1);
								}
							}
							cycles = recipe.time;
							outputRecipe = recipe;
							if (this.getBlockState().getValue(StateProperties.MACHINE_ACTIVE) == false) {
								this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
							}
							sendUpdates = true;

						}

					}

					//Below determines if output can be completed whether it fits in every slot or not, etc.
					if (outputRecipe != null) {

						if (amount - cost >= 0) {
							if (progress >= cycles) {

								ItemStack stackOut = null;
								FluidStack fluidOutA = null;
								FluidStack fluidOutB = null;

								Pair<ItemStack, Float> tempa = outputRecipe.itemOutput;
								if (!tempa.getFirst().isEmpty()) {
									ItemStack outstack = tempa.getFirst().copy();

									float chance = tempa.getSecond();
									switch (mulcount) {
									case 3:
										chance = chance * 2f;
										break;
									case 2:
										chance = chance * 1.5f;
										break;
									case 0:
										chance = 0f;
									}

									if (General.RAND.nextFloat() < chance) {
										outstack.setCount(Math.round(((float) outstack.getCount() * 1.5f)));
										if (outstack.getCount() > 64) {
											outstack.setCount(64);
										}
									}
									if (contents.get(0).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(0), outstack)
											&& contents.get(0).getCount() + outstack.getCount() <= contents.get(0).getMaxStackSize())) {
										stackOut = outstack;
									} else {
										return;
									}
								}

								Pair<FluidStack, Float> tempb = outputRecipe.fluidOutputA;
								if (!tempb.getFirst().isEmpty()) {
									FluidStack outstack = tempb.getFirst().copy();

									float chance = tempb.getSecond();
									switch (mulcount) {
									case 3:
										chance = chance * 2f;
										break;
									case 2:
										chance = chance * 1.5f;
										break;
									case 0:
										chance = 0f;
									}

									if (General.RAND.nextFloat() < chance) {
										outstack.setAmount(Math.round(((float) outstack.getAmount() * 1.5f)));
									}
									if (tankouta.isEmpty() || (tankouta.isFluidEqual(outstack) && tankouta.getAmount() + outstack.getAmount() <= 4000)) {
										fluidOutA = outstack;
									} else if(outputRecipe.fluidOutputB.getFirst().isEmpty() && (tankoutb.isEmpty() || (tankoutb.isFluidEqual(outstack) && tankoutb.getAmount() + outstack.getAmount() <= 4000))){
										fluidOutB = outstack;
									}else {
										return;
									}
								}

								tempb = outputRecipe.fluidOutputB;
								if (!tempb.getFirst().isEmpty()) {
									FluidStack outstack = tempb.getFirst().copy();

									float chance = tempb.getSecond();
									switch (mulcount) {
									case 3:
										chance = chance * 2f;
										break;
									case 2:
										chance = chance * 1.5f;
										break;
									case 0:
										chance = 0f;
									}

									if (General.RAND.nextFloat() < chance) {
										outstack.setAmount(Math.round(((float) outstack.getAmount() * 1.5f)));
									}
									if (tankoutb.isEmpty() || (tankoutb.isFluidEqual(outstack) && tankoutb.getAmount() + outstack.getAmount() <= 4000)) {
										fluidOutB = outstack;
									} else {
										return;
									}
								}

								if (stackOut != null) {
									if (contents.get(0).isEmpty()) {
										contents.set(0, stackOut);
									} else {
										contents.get(0).grow(stackOut.getCount());
									}
								}

								if (fluidOutA != null) {
									if (tankouta.isEmpty()) {
										tankouta = fluidOutA;
									} else {
										tankouta.grow(fluidOutA.getAmount());
									}
								}

								if (fluidOutB != null) {
									if (tankoutb.isEmpty()) {
										tankoutb = fluidOutB;
									} else {
										tankoutb.grow(fluidOutB.getAmount());
									}
								}

								outputRecipe = null;
								progress = 0f;
								cycles = 0f;
								sendUpdates = true;

							} else {

								if (!outputRecipe.fluidInput.getFirst().isEmpty() && outputRecipe.fluidInput.getFirst().getFluid().getAttributes().isGaseous() && hasGas == false) {
									showGasMsg = true;
									sendUpdates = true;
								} else {
									if (showGasMsg = true) {
										showGasMsg = false;
									}
									amount -= cost;
									fept = (float) cost / (float) nTimer;
									progress++;
									sendUpdates = true;
								}

							}

						}

					} else if (getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
						sendUpdates = true;
					}

					if (sendUpdates) {
						sendUpdates();
					}
				}
			}
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			nTimer = compound.getInt("assemblylinemachines:ntimer");
			progress = compound.getFloat("assemblylinemachines:progress");
			cycles = compound.getFloat("assemblylinemachines:cycles");
			showGasMsg = compound.getBoolean("assemblylinemachines:gasmsg");
			if (compound.contains("assemblylinemachines:outputrecipe")) {
				outputRecipeRL = new ResourceLocation(compound.getString("assemblylinemachines:outputrecipe"));
			}

			if (compound.contains("assemblylinemachines:tankin")) {
				tankin = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tankin"));
			}

			if (compound.contains("assemblylinemachines:tankouta")) {
				tankouta = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tankouta"));
			}

			if (compound.contains("assemblylinemachines:tankoutb")) {
				tankoutb = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tankoutb"));
			}
		}

		@Override
		public CompoundTag save(CompoundTag compound) {
			super.save(compound);

			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putBoolean("assemblylinemachines:gasmsg", showGasMsg);

			if (outputRecipe != null) {
				compound.putString("assemblylinemachines:outputrecipe", outputRecipe.getId().toString());
			}
			CompoundTag subin = new CompoundTag();
			tankin.writeToNBT(subin);
			compound.put("assemblylinemachines:tankin", subin);

			CompoundTag subouta = new CompoundTag();
			tankouta.writeToNBT(subouta);
			compound.put("assemblylinemachines:tankouta", subouta);

			CompoundTag suboutb = new CompoundTag();
			tankoutb.writeToNBT(suboutb);
			compound.put("assemblylinemachines:tankoutb", suboutb);

			return compound;
		}

		@Override
		public void onLoad() {

			super.onLoad();

			if (outputRecipeRL != null) {
				Recipe<?> rc = this.getLevel().getRecipeManager().byKey(outputRecipeRL).orElse(null);
				if (rc != null && rc instanceof RefiningCrafting) {
					outputRecipe = (RefiningCrafting) rc;
				} else {
					AssemblyLineMachines.LOGGER.warn("Error loading active recipe from NBT for Refinery @ " + this.getBlockPos() + ". A recipe may have been lost.");
					progress = 0;
					cycles = 0;

				}

			}
		}

		public TERefinery(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 5, new TranslatableComponent(Registry.getBlock("refinery").getDescriptionId()), Registry.getContainerId("refinery"),
					ContainerRefinery.class, new EnergyProperties(true, false, 160000), pos, state);
		}

		public TERefinery(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("refinery"), pos, state);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return fhandler.cast();
			}
			
			/* PLUGIN DISABLED DUE TO MEKANISM NON UPDATE
			if(PluginMekanism.get().isMekanismInstalled()) {
				LazyOptional<T> lO = PluginMekanism.get().getRefineryCapability(cap, this);
				if(lO != null) {
					return lO;
				}
			}
			
			*/
			return super.getCapability(cap, side);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if (slot > 1) {
				if (stack.getItem() instanceof ItemUpgrade) {
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

	public static class ContainerRefinery extends ContainerALMBase<TERefinery> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerRefinery(final int windowId, final Inventory playerInventory, final TERefinery tileEntity) {
			super(Registry.getContainerType("refinery"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 125, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 44, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 57, tileEntity));
		}

		public ContainerRefinery(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TERefinery.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenRefinery extends ScreenALMEnergyBased<ContainerRefinery> {

		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TERefinery tsfm;

		public ScreenRefinery(ContainerRefinery screenContainer, Inventory inv, Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "refinery", false, new Pair<>(14, 17), screenContainer.tileEntity,
					true);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			renderFluid(tsfm.tankin, x + 65, y + 23);
			renderFluid(tsfm.tankouta, x + 99, y + 23);
			renderFluid(tsfm.tankoutb, x + 112, y + 23);
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			float capacity = (float) tsfm.fluids.getTankCapacity(0);
			renderFluidOverlayBar(tsfm.tankin, capacity, x + 65, y + 23);
			renderFluidOverlayBar(tsfm.tankouta, capacity, x + 99, y + 23);
			renderFluidOverlayBar(tsfm.tankoutb, capacity, x + 112, y + 23);

			int prog = Math.round((tsfm.progress / tsfm.cycles) * 18f);
			super.blit(x + 77, y + 37, 176, 89, prog, 10);

			if (tsfm.cycles != 0) {
				super.blit(x + 79, y + 53, 176, 99, 13, 12);
			}
		}

		@Override
		protected void init() {
			super.init();

			int x = leftPos;
			int y = topPos;

			this.addRenderableWidget(new TrueFalseButton(x+65, y+23, 8, 37, null, (b) -> sendDumpTank(tsfm.getBlockPos())));
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			renderFluidTooltip(tsfm.tankin, mouseX, mouseY, x + 65, y + 23, x, y, true);
			renderFluidTooltip(tsfm.tankouta, mouseX, mouseY, x + 99, y + 23, x, y, false);
			renderFluidTooltip(tsfm.tankoutb, mouseX, mouseY, x + 112, y + 23, x, y, false);
			if (tsfm.showGasMsg) {
				renderComponentTooltip("Needs Gas Upgrade", 41, 50);
			}
		}

		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				}

				super.blit(xblit, yblit, 37, 37, 37, tas);
			}
		}

		private void renderFluidOverlayBar(FluidStack fs, float capacity, int xblit, int yblit) {
			int fprog = Math.round(((float) fs.getAmount() / capacity) * 37f);
			super.blit(xblit, yblit, 176, 52, 8, 37 - fprog);
		}

		private void renderFluidTooltip(FluidStack fs, int mouseX, int mouseY, int mminx, int mminy, int bx, int by, boolean cm) {

			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 7 && mouseY <= mminy + 36) {
				if (!fs.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(fs.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(Formatting.FEPT_FORMAT.format(fs.getAmount()) + " mB");

						if (cm == true) {
							str.add("Click to send to output slot.");
						}
					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderComponentTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderComponentTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
	}

	private static void sendDumpTank(BlockPos pos) {
		PacketData pd = new PacketData("refinery_gui");
		pd.writeBlockPos("pos", pos);
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}

	public static void dumpFluid(PacketData pd, Level world) {

		if (world.getBlockEntity(pd.get("pos", BlockPos.class)) instanceof TERefinery) {
			TERefinery tef = (TERefinery) world.getBlockEntity(pd.get("pos", BlockPos.class));

			if (!tef.tankin.isEmpty()) {
				if (tef.tankouta.isEmpty() || (tef.tankouta.isFluidEqual(tef.tankin) && tef.tankouta.getAmount() + tef.tankin.getAmount() <= 4000)) {
					if (tef.tankouta.isEmpty()) {
						tef.tankouta = tef.tankin;
						tef.tankin = FluidStack.EMPTY;
					} else {
						tef.tankouta.grow(tef.tankin.getAmount());
						tef.tankin = FluidStack.EMPTY;
					}
					tef.sendUpdates();
				} else if (tef.tankoutb.isEmpty() || (tef.tankoutb.isFluidEqual(tef.tankin) && tef.tankoutb.getAmount() + tef.tankin.getAmount() <= 4000)) {
					if (tef.tankoutb.isEmpty()) {
						tef.tankoutb = tef.tankin;
						tef.tankin = FluidStack.EMPTY;
					} else {
						tef.tankoutb.grow(tef.tankin.getAmount());
						tef.tankin = FluidStack.EMPTY;
					}
					tef.sendUpdates();
				}
			}
		}
	}
}
