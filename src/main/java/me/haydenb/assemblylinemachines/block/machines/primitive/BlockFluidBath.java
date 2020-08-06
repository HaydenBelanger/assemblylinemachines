package me.haydenb.assemblylinemachines.block.machines.primitive;

import java.util.Random;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.StateProperties;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidBath extends Block {
	
	private static final VoxelShape SHAPE = Stream.of(Block.makeCuboidShape(1, 0, 1, 15, 16, 2),
			Block.makeCuboidShape(1, 0, 14, 15, 16, 15), Block.makeCuboidShape(1, 0, 2, 2, 16, 14),
			Block.makeCuboidShape(14, 0, 2, 15, 16, 14), Block.makeCuboidShape(2, 0, 2, 14, 1, 14)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public static final EnumProperty<BathStatus> STATUS = EnumProperty.create("status", BathStatus.class);
	
	private static final Random RAND = new Random();

	public BlockFluidBath() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(4f, 15f).harvestLevel(0).
				harvestTool(ToolType.PICKAXE).sound(SoundType.WOOD));
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.FLUID, BathCraftingFluids.NONE).with(STATUS, BathStatus.NONE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(StateProperties.FLUID).add(STATUS);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if(state.getBlock() == this) {
			return true;
		}
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		return SHAPE;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof TEFluidBath) {
				worldIn.removeTileEntity(pos);
			}
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.getTileEntity("fluid_bath").create();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		if (!world.isRemote) {
			if (player.getActiveHand() == Hand.MAIN_HAND) {

				if (world.getTileEntity(pos) instanceof TEFluidBath) {
					TEFluidBath entity = (TEFluidBath) world.getTileEntity(pos);

					if (player.isSneaking()) {

						if (entity.fluid == BathCraftingFluids.NONE || state.get(StateProperties.FLUID) == BathCraftingFluids.NONE) {
							player.sendStatusMessage(new StringTextComponent("The basin is empty."), true);
						} else {
							int maxSludge = 2;
							if(entity.inputa != null) {
								maxSludge = maxSludge + 2;
							}
							if(entity.inputb != null) {
								maxSludge = maxSludge + 2;
							}
							entity.fluid = BathCraftingFluids.NONE;
							entity.stirsRemaining = -1;
							entity.output = null;
							entity.inputa = null;
							entity.fluidColor = 0;
							entity.inputb = null;
							entity.sendUpdates();
							world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1f, 1f);
							world.setBlockState(pos, state.with(StateProperties.FLUID, BathCraftingFluids.NONE).with(STATUS, BathStatus.NONE));
							player.sendStatusMessage(new StringTextComponent("Drained basin."), true);
							
							ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Registry.getItem("sludge"), RAND.nextInt(maxSludge)));
						}

					} else {
						ItemStack held = player.getHeldItemMainhand();
						if (entity.fluid == BathCraftingFluids.NONE || state.get(StateProperties.FLUID) == BathCraftingFluids.NONE) {
							if (held.getItem() == Items.LAVA_BUCKET || held.getItem() == Items.WATER_BUCKET) {
								BathCraftingFluids f;
								if (held.getItem() == Items.LAVA_BUCKET) {
									f = BathCraftingFluids.LAVA;
									world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS,
											1f, 1f);
								} else {
									f = BathCraftingFluids.WATER;
									world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1f,
											1f);
								}
								if(!player.isCreative()) {
									ItemHandlerHelper.giveItemToPlayer(player, held.getContainerItem());
									held.shrink(1);
								}
								world.setBlockState(pos, state.with(StateProperties.FLUID, f));
								entity.fluid = f;
								entity.sendUpdates();
								player.sendStatusMessage(new StringTextComponent("Filled basin."), true);
							} else {
								player.sendStatusMessage(new StringTextComponent("The basin is empty."), true);
							}

						} else {
							if (entity.inputa == null) {
								Item i = held.getItem();
								if (!held.isEmpty() && i != Registry.getItem("wooden_stirring_stick") && i != Registry.getItem("pure_iron_stirring_stick") && i != Registry.getItem("sludge") && i != Items.BUCKET && i != Items.LAVA_BUCKET && i != Items.WATER_BUCKET) {
									entity.inputa = new ItemStack(held.getItem());
									held.shrink(1);
									entity.sendUpdates();
									world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS,
											1f, 1f);
								}

							} else if (entity.inputb == null) {
								Item i = held.getItem();
								if (!held.isEmpty() && i != Registry.getItem("wooden_stirring_stick") && i != Registry.getItem("pure_iron_stirring_stick") && i != Registry.getItem("sludge") && i != Items.BUCKET && i != Items.LAVA_BUCKET && i != Items.WATER_BUCKET) {
									world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS,
											1f, 1f);
									entity.inputb = new ItemStack(held.getItem());
									held.shrink(1);
									BathCrafting crafting = world.getRecipeManager()
											.getRecipe(BathCrafting.BATH_RECIPE, entity, world).orElse(null);
									if (crafting != null && crafting.getFluid().equals(entity.fluid)) {
										
										entity.output = crafting.getRecipeOutput().copy();
										entity.stirsRemaining = crafting.getStirs();
										entity.fluidColor = crafting.getColor();
										entity.sendUpdates();
										world.setBlockState(pos, world.getBlockState(pos).with(STATUS, BathStatus.SUCCESS), 3);
										
										
										
									}else {
										entity.sendUpdates();
										world.setBlockState(pos, state.with(STATUS, BathStatus.FAIL));
									}
								}

							} else {
								if (entity.output != null) {
									if (entity.stirsRemaining <= 0) {
										ItemHandlerHelper.giveItemToPlayer(player, entity.output);
										entity.fluid = BathCraftingFluids.NONE;
										entity.stirsRemaining = -1;
										entity.fluidColor = 0;
										entity.output = null;
										entity.inputa = null;
										entity.inputb = null;
										entity.sendUpdates();
										world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
												1f, 1f);
										world.setBlockState(pos, state.with(StateProperties.FLUID, BathCraftingFluids.NONE).with(STATUS, BathStatus.NONE));
									} else {

										if(!(held.getItem() instanceof ItemStirringStick)) {
											player.sendStatusMessage(new StringTextComponent("Use a Stirring Stick to mix."), true);
										}else {
											ItemStirringStick tss = (ItemStirringStick) held.getItem();
											if(entity.fluid == BathCraftingFluids.LAVA && tss.getStirringResistance() == TemperatureResistance.COLD) {
												player.sendStatusMessage(new StringTextComponent("You need a metal Stirring Stick to stir Lava."), true);
											}else {
												tss.useStirStick(held);
												entity.stirsRemaining--;
											}
										}

									}
								} else {
									player.sendStatusMessage(
											new StringTextComponent(
													"This recipe is invalid. Shift + Right Click to drain basin."),
											true);
								}
							}

						}
					}
				}
			}

		}

		return ActionResultType.CONSUME;

	}

	public static class TEFluidBath extends BasicTileEntity implements IInventory {

		private int stirsRemaining = -1;
		private BathCraftingFluids fluid = BathCraftingFluids.NONE;
		private ItemStack inputa = null;
		private ItemStack inputb = null;
		private ItemStack output = null;
		private int fluidColor = -1;

		public TEFluidBath(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEFluidBath() {
			this(Registry.getTileEntity("fluid_bath"));
		}
		
		@Override
		public void func_230337_a_(BlockState p_230337_1_, CompoundNBT compound) {
			super.func_230337_a_(p_230337_1_, compound);
			
			if (compound.contains("assemblylinemachines:stirs")) {
				stirsRemaining = compound.getInt("assemblylinemachines:stirs");
			}
			if (compound.contains("assemblylinemachines:fluid")) {
				fluid = BathCraftingFluids.valueOf(compound.getString("assemblylinemachines:fluid"));
			}
			if(compound.contains("assemblylinemachines:fluidcolor")) {
				fluidColor = compound.getInt("assemblylinemachines:fluidcolor");
			}
			if (compound.contains("assemblylinemachines:inputa")) {
				inputa = ItemStack.read(compound.getCompound("assemblylinemachines:inputa"));
			}
			if (compound.contains("assemblylinemachines:inputb")) {
				inputb = ItemStack.read(compound.getCompound("assemblylinemachines:inputb"));
			}
			if (compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
			}
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			compound.putInt("assemblylinemachines:stirs", stirsRemaining);
			compound.putString("assemblylinemachines:fluid", fluid.toString());
			compound.putInt("assemblylinemachines:fluidcolor", fluidColor);
			if (inputa != null) {
				CompoundNBT sub = new CompoundNBT();
				inputa.write(sub);
				compound.put("assemblylinemachines:inputa", sub);
			} else {
				compound.remove("assemblylinemachines:inputa");
			}
			if (inputb != null) {
				CompoundNBT sub = new CompoundNBT();
				inputb.write(sub);
				compound.put("assemblylinemachines:inputb", sub);
			} else {
				compound.remove("assemblylinemachines:inputb");
			}
			if (output != null) {
				CompoundNBT sub = new CompoundNBT();
				output.write(sub);
				compound.put("assemblylinemachines:output", sub);
			} else {
				compound.remove("assemblylinemachines:output");
			}
			return compound;
		}

		@Override
		public void clear() {
		}

		@Override
		public ItemStack decrStackSize(int arg0, int arg1) {
			return null;
		}

		@Override
		public int getSizeInventory() {
			return 0;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot == 1) {
				return inputa;
			} else if (slot == 2) {
				return inputb;
			} else {
				return null;
			}
		}

		@Override
		public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
			super.onDataPacket(net, pkt);
			if(pkt.getNbtCompound().contains("assemblylinemachines:fluidcolor") && pkt.getNbtCompound().getInt("assemblylinemachines:fluidcolor") >= 0) {
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
			}
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity arg0) {
			return false;
		}

		@Override
		public ItemStack removeStackFromSlot(int arg0) {
			return null;
		}

		@Override
		public void setInventorySlotContents(int arg0, ItemStack arg1) {
		}
		
		public int getFluidColor() {
			return fluidColor;
		}

	}
	
	public static enum BathStatus implements IStringSerializable {
		NONE, FAIL, SUCCESS;

		@Override
		public String func_176610_l() {
			return toString().toLowerCase();
		}
		
		
	}

}
