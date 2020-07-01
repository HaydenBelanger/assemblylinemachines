package me.haydenb.assemblylinemachines.block.machines.oil;

import java.util.*;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockRefinery.TERefinery;
import me.haydenb.assemblylinemachines.crafting.RefiningCrafting;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockRefinery extends BlockScreenTileEntity<TERefinery> {

	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(3, 5, 4, 13, 10, 11), Block.makeCuboidShape(3, 5, 2, 5, 10, 4),
			Block.makeCuboidShape(11, 5, 2, 13, 10, 4), Block.makeCuboidShape(9, 6, 2, 10, 8, 3), Block.makeCuboidShape(6, 6, 2, 7, 8, 3),
			Block.makeCuboidShape(5, 8, 2, 11, 10, 4), Block.makeCuboidShape(5, 5, 2, 11, 6, 4), Block.makeCuboidShape(5, 6, 3, 11, 8, 3),
			Block.makeCuboidShape(1, 5, 6, 6, 13, 11), Block.makeCuboidShape(10, 5, 6, 15, 13, 11), Block.makeCuboidShape(3, 10, 5, 4, 14, 6),
			Block.makeCuboidShape(12, 10, 5, 13, 14, 6), Block.makeCuboidShape(3, 13, 6, 4, 14, 7), Block.makeCuboidShape(3, 13, 10, 4, 14, 11),
			Block.makeCuboidShape(12, 13, 6, 13, 14, 7), Block.makeCuboidShape(12, 13, 10, 13, 14, 11), Block.makeCuboidShape(11, 13, 7, 14, 14, 10),
			Block.makeCuboidShape(2, 13, 7, 5, 14, 10), Block.makeCuboidShape(12, 13, 8, 13, 16, 9), Block.makeCuboidShape(3, 13, 8, 4, 16, 9),
			Block.makeCuboidShape(0, 0, 0, 16, 5, 16), Block.makeCuboidShape(0, 5, 11, 16, 16, 16)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);

	public BlockRefinery() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "refinery",
				BlockRefinery.TERefinery.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.MACHINE_ACTIVE, false).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE).add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {

		BlockState bs = context.getWorld().getBlockState(context.getPos().up());
		if (bs.getBlock() instanceof BlockRefineryAddon && bs.func_235901_b_(HorizontalBlock.HORIZONTAL_FACING)) {
			return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, bs.get(HorizontalBlock.HORIZONTAL_FACING));
		} else {
			return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
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

	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null) != null && world.getTileEntity(pos) instanceof TERefinery) {

			IFluidHandler handler = ((TERefinery) world.getTileEntity(pos)).fluids;

			if (!handler.getFluidInTank(0).getFluid().getAttributes().isGaseous()) {
				FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
				if (far.isSuccess()) {
					if(!player.isCreative()) {
						if (stack.getCount() == 1) {
							player.inventory.removeStackFromSlot(player.inventory.currentItem);
						} else {
							stack.shrink(1);
						}
						ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
						
					}
					return ActionResultType.CONSUME;

				}
				FluidActionResult farx = FluidUtil.tryFillContainer(stack, handler, 1000, player, true);
				if (farx.isSuccess()) {
					if(!player.isCreative()) {
						if (stack.getCount() == 1) {
							player.inventory.removeStackFromSlot(player.inventory.currentItem);
						} else {
							stack.shrink(1);
						}
						ItemHandlerHelper.giveItemToPlayer(player, farx.getResult());
					}
					
					return ActionResultType.CONSUME;
				}
			}
			

		}
		return super.blockRightClickServer(state, world, pos, player);
	}

	@Override
	public void animateTick(BlockState stateIn, World world, BlockPos pos, Random rand) {

		Block bsu = world.getBlockState(pos.up()).getBlock();
		if (stateIn.get(StateProperties.MACHINE_ACTIVE)) {

			if (bsu instanceof BlockRefineryAddon) {
				((BlockRefineryAddon) bsu).animateTickFromBase(stateIn, world, pos.up(), rand);
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

	public static class TERefinery extends ManagedSidedMachine<ContainerRefinery> implements ITickableTileEntity {

		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private boolean showGasMsg = false;

		private RefiningCrafting outputRecipe = null;
		private ResourceLocation outputRecipeRL = null;

		private FluidStack tankin = FluidStack.EMPTY;

		private FluidStack tankouta = FluidStack.EMPTY;
		private FluidStack tankoutb = FluidStack.EMPTY;

		protected IFluidHandler fluids = new IFluidHandler() {

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
						tankin = resource;
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
			if (!world.isRemote) {
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

						List<RefiningCrafting> rList = world.getRecipeManager().getRecipes(RefiningCrafting.REFINING_RECIPE, this, world);
						Block b = world.getBlockState(pos.up()).getBlock();
						RefiningCrafting recipe = null;
						for (RefiningCrafting r : rList) {
							if (b == r.attachmentBlock) {
								if (!r.itemInput.getFirst().hasNoMatchingItems()) {

									if (r.itemInput.getFirst().test(getStackInSlot(1))) {
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

							if (!recipe.itemInput.getFirst().hasNoMatchingItems()) {

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
									getStackInSlot(1).shrink(1);
								}
							}
							cycles = recipe.time;
							outputRecipe = recipe;
							if (this.getBlockState().get(StateProperties.MACHINE_ACTIVE) == false) {
								world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
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

					} else if (getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
						world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
						sendUpdates = true;
					}

					if (sendUpdates) {
						sendUpdates();
					}
				}
			}
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
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
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putBoolean("assemblylinemachines:gasmsg", showGasMsg);

			if (outputRecipe != null) {
				compound.putString("assemblylinemachines:outputrecipe", outputRecipe.getId().toString());
			}
			CompoundNBT subin = new CompoundNBT();
			tankin.writeToNBT(subin);
			compound.put("assemblylinemachines:tankin", subin);

			CompoundNBT subouta = new CompoundNBT();
			tankouta.writeToNBT(subouta);
			compound.put("assemblylinemachines:tankouta", subouta);

			CompoundNBT suboutb = new CompoundNBT();
			tankoutb.writeToNBT(suboutb);
			compound.put("assemblylinemachines:tankoutb", suboutb);

			return compound;
		}

		@Override
		public void onLoad() {

			super.onLoad();

			if (outputRecipeRL != null) {
				IRecipe<?> rc = world.getRecipeManager().getRecipe(outputRecipeRL).orElse(null);
				if (rc != null && rc instanceof RefiningCrafting) {
					outputRecipe = (RefiningCrafting) rc;
				} else {
					AssemblyLineMachines.LOGGER.warn("Error loading active recipe from NBT for Refinery @ " + pos + ". A recipe may have been lost.");
					progress = 0;
					cycles = 0;

				}

			}
		}

		public TERefinery(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 5, new TranslationTextComponent(Registry.getBlock("refinery").getTranslationKey()), Registry.getContainerId("refinery"),
					ContainerRefinery.class, new EnergyProperties(true, false, 160000));
		}

		public TERefinery() {
			this(Registry.getTileEntity("refinery"));
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return fhandler.cast();
			}
			return super.getCapability(cap, side);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return fhandler.cast();
			}
			return super.getCapability(cap);
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

		public ContainerRefinery(final int windowId, final PlayerInventory playerInventory, final TERefinery tileEntity) {
			super(Registry.getContainerType("refinery"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 125, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 44, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 57, tileEntity));
		}

		public ContainerRefinery(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TERefinery.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenRefinery extends ScreenALMEnergyBased<ContainerRefinery> {

		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TERefinery tsfm;

		public ScreenRefinery(ContainerRefinery screenContainer, PlayerInventory inv, ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "refinery", false, new Pair<>(14, 17), screenContainer.tileEntity,
					true);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			field_230706_i_.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

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

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

			this.addButton(new SimpleButton(x + 65, y + 23, 0, 0, 8, 37, "", (button) -> {
				sendDumpTank(tsfm.getPos());
			}));
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

			renderFluidTooltip(tsfm.tankin, mouseX, mouseY, x + 65, y + 23, x, y, true);
			renderFluidTooltip(tsfm.tankouta, mouseX, mouseY, x + 99, y + 23, x, y, false);
			renderFluidTooltip(tsfm.tankoutb, mouseX, mouseY, x + 112, y + 23, x, y, false);
			if (tsfm.showGasMsg) {
				renderTooltip("Needs Gas Upgrade", 41, 50);
			}
		}

		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					GL11.glColor4f(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					GL11.glColor4f(1f, 1f, 1f, 1f);
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

					str.add(fs.getDisplayName().func_230532_e_().getString());
					if (Screen.func_231173_s_()) {

						str.add(Formatting.FEPT_FORMAT.format(fs.getAmount()) + " mB");

						if (cm == true) {
							str.add("Click to send to output slot.");
						}
					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
	}

	private static void sendDumpTank(BlockPos pos) {
		PacketData pd = new PacketData("refinery_gui");
		pd.writeBlockPos("pos", pos);
		HashPacketImpl.INSTANCE.sendToServer(pd);
	}

	public static void dumpFluid(PacketData pd, World world) {

		if (world.getTileEntity(pd.get("pos", BlockPos.class)) instanceof TERefinery) {
			TERefinery tef = (TERefinery) world.getTileEntity(pd.get("pos", BlockPos.class));

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
