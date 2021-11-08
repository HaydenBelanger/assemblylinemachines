package me.haydenb.assemblylinemachines.block.machines.primitive;

import java.util.Random;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.BathCraftingFluid;
import me.haydenb.assemblylinemachines.registry.BathCraftingFluid.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidBath extends Block implements EntityBlock {
	
	private static final VoxelShape SHAPE = Stream.of(Block.box(1, 0, 1, 15, 16, 2),
			Block.box(1, 0, 14, 15, 16, 15), Block.box(1, 0, 2, 2, 16, 14),
			Block.box(14, 0, 2, 15, 16, 14), Block.box(2, 0, 2, 14, 1, 14)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public static final IntegerProperty STATUS = IntegerProperty.create("status", 0, 5);
	
	private static final Random RAND = new Random();

	public BlockFluidBath() {
		super(Block.Properties.of(Material.WOOD).strength(4f, 15f).sound(SoundType.WOOD));
		this.registerDefaultState(this.getStateDefinition().any().setValue(BathCraftingFluid.FLUID, BathCraftingFluids.NONE).setValue(STATUS, 0));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		
		builder.add(BathCraftingFluid.FLUID).add(STATUS);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getBlockEntity(pos) instanceof TEFluidBath) {
				worldIn.removeBlockEntity(pos);
			}
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return Registry.getBlockEntity("fluid_bath").create(pPos, pState);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {

		if (!world.isClientSide) {
			if (handIn.equals(InteractionHand.MAIN_HAND)) {

				if (world.getBlockEntity(pos) instanceof TEFluidBath) {
					TEFluidBath entity = (TEFluidBath) world.getBlockEntity(pos);

					if (player.isShiftKeyDown()) {

						if (entity.fluid == BathCraftingFluids.NONE || state.getValue(BathCraftingFluid.FLUID) == BathCraftingFluids.NONE) {
							player.displayClientMessage(new TextComponent("The basin is empty."), true);
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
							entity.drainAmt = 0;
							entity.sendUpdates();
							world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
							world.setBlockAndUpdate(pos, state.setValue(BathCraftingFluid.FLUID, BathCraftingFluids.NONE).setValue(STATUS, 0));
							player.displayClientMessage(new TextComponent("Drained basin."), true);
							
							ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Registry.getItem("sludge"), RAND.nextInt(maxSludge)));
						}

					} else {
						ItemStack held = player.getMainHandItem();
						if (entity.fluid == BathCraftingFluids.NONE || state.getValue(BathCraftingFluid.FLUID) == BathCraftingFluids.NONE) {
							if (held.getItem() == Items.LAVA_BUCKET || held.getItem() == Items.WATER_BUCKET) {
								BathCraftingFluids f;
								if (held.getItem() == Items.LAVA_BUCKET) {
									f = BathCraftingFluids.LAVA;
									world.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS,
											1f, 1f);
								} else {
									f = BathCraftingFluids.WATER;
									world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f,
											1f);
								}
								if(!player.isCreative()) {
									ItemHandlerHelper.giveItemToPlayer(player, held.getContainerItem());
									held.shrink(1);
								}
								world.setBlockAndUpdate(pos, state.setValue(BathCraftingFluid.FLUID, f).setValue(STATUS, 4));
								entity.fluid = f;
								entity.sendUpdates();
								player.displayClientMessage(new TextComponent("Filled basin."), true);
							} else {
								player.displayClientMessage(new TextComponent("The basin is empty."), true);
							}

						} else {
							if (entity.inputa == null) {
								Item i = held.getItem();
								if (!held.isEmpty() && !(i instanceof ItemStirringStick) && i != Registry.getItem("sludge") && i != Items.BUCKET && i != Items.LAVA_BUCKET && i != Items.WATER_BUCKET) {
									entity.inputa = new ItemStack(held.getItem());
									held.shrink(1);
									entity.sendUpdates();
									world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS,
											1f, 1f);
								}

							} else if (entity.inputb == null) {
								Item i = held.getItem();
								if (!held.isEmpty() && !(i instanceof ItemStirringStick) && i != Registry.getItem("sludge") && i != Items.BUCKET && i != Items.LAVA_BUCKET && i != Items.WATER_BUCKET) {
									world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS,
											1f, 1f);
									entity.inputb = new ItemStack(held.getItem());
									held.shrink(1);
									BathCrafting crafting = world.getRecipeManager().getRecipeFor(BathCrafting.BATH_RECIPE, entity, world).orElse(null);
									if (crafting != null && crafting.getFluid().equals(entity.fluid) && state.getValue(STATUS) - crafting.getPercentage().getDrop() >= 0) {
										
										
										entity.output = crafting.getResultItem().copy();
										entity.stirsRemaining = crafting.getStirs();
										entity.fluidColor = crafting.getColor();
										entity.drainAmt = crafting.getPercentage().getDrop();
										entity.sendUpdates();
										
										
										
									}else {
										entity.sendUpdates();
										world.setBlockAndUpdate(pos, state.setValue(STATUS, 5));
									}
								}

							} else {
								if (entity.output != null) {
									if (entity.stirsRemaining <= 0) {
										ItemHandlerHelper.giveItemToPlayer(player, entity.output);
										int setDrain = state.getValue(STATUS) - entity.drainAmt;
										BathCraftingFluids newFluid = entity.fluid;
										if(setDrain <= 0) {
											newFluid = BathCraftingFluids.NONE;
										}
										entity.fluid = newFluid;
										entity.stirsRemaining = -1;
										entity.fluidColor = 0;
										entity.output = null;
										entity.inputa = null;
										entity.inputb = null;
										entity.drainAmt = 0;
										entity.sendUpdates();
										world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS,
												1f, 1f);
										world.setBlockAndUpdate(pos, state.setValue(BathCraftingFluid.FLUID, newFluid).setValue(STATUS, setDrain));
									} else {

										if(!(held.getItem() instanceof ItemStirringStick)) {
											player.displayClientMessage(new TextComponent("Use a Stirring Stick to mix."), true);
										}else {
											ItemStirringStick tss = (ItemStirringStick) held.getItem();
											if(entity.fluid == BathCraftingFluids.LAVA && tss.getStirringResistance() == TemperatureResistance.COLD) {
												player.displayClientMessage(new TextComponent("You need a metal Stirring Stick to stir Lava."), true);
											}else {
												entity.stirsRemaining--;
												tss.useStirStick(held);
											}
										}

									}
								} else {
									player.displayClientMessage(
											new TextComponent(
													"This recipe is invalid. Shift + Right Click to drain basin."),
											true);
								}
							}

						}
					}
				}
			}

		}

		return InteractionResult.CONSUME;

	}

	public static class TEFluidBath extends BasicTileEntity implements Container {

		private int stirsRemaining = -1;
		private BathCraftingFluids fluid = BathCraftingFluids.NONE;
		private ItemStack inputa = null;
		private ItemStack inputb = null;
		private ItemStack output = null;
		private int fluidColor = -1;
		private int drainAmt = 0;

		public TEFluidBath(BlockEntityType<?> tileEntity, BlockPos pos, BlockState state) {
			super(tileEntity, pos, state);
		}

		public TEFluidBath(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("fluid_bath"), pos, state);
		}
		
		

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if (compound.contains("assemblylinemachines:stirs")) {
				stirsRemaining = compound.getInt("assemblylinemachines:stirs");
			}
			if (compound.contains("assemblylinemachines:fluid")) {
				fluid = BathCraftingFluids.valueOf(BathCraftingFluids.class, compound.getString("assemblylinemachines:fluid"));
			}
			if(compound.contains("assemblylinemachines:fluidcolor")) {
				fluidColor = compound.getInt("assemblylinemachines:fluidcolor");
			}
			if (compound.contains("assemblylinemachines:inputa")) {
				inputa = ItemStack.of(compound.getCompound("assemblylinemachines:inputa"));
			}
			if (compound.contains("assemblylinemachines:inputb")) {
				inputb = ItemStack.of(compound.getCompound("assemblylinemachines:inputb"));
			}
			if (compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}
			
			if(compound.contains("assemblylinemachines:drainamt")) {
				drainAmt = compound.getInt("assemblylinemachines:drainamt");
			}
		}

		@Override
		public CompoundTag save(CompoundTag compound) {
			super.save(compound);

			compound.putInt("assemblylinemachines:stirs", stirsRemaining);
			compound.putString("assemblylinemachines:fluid", fluid.toString());
			compound.putInt("assemblylinemachines:fluidcolor", fluidColor);
			compound.putInt("assemblylinemachines:drainamt", drainAmt);
			if (inputa != null) {
				CompoundTag sub = new CompoundTag();
				inputa.save(sub);
				compound.put("assemblylinemachines:inputa", sub);
			} else {
				compound.remove("assemblylinemachines:inputa");
			}
			if (inputb != null) {
				CompoundTag sub = new CompoundTag();
				inputb.save(sub);
				compound.put("assemblylinemachines:inputb", sub);
			} else {
				compound.remove("assemblylinemachines:inputb");
			}
			if (output != null) {
				CompoundTag sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			} else {
				compound.remove("assemblylinemachines:output");
			}
			return compound;
		}

		@Override
		public void clearContent() {
		}

		@Override
		public int getContainerSize() {
			return 0;
		}

		@Override
		public ItemStack getItem(int slot) {
			if (slot == 1) {
				return inputa;
			} else if (slot == 2) {
				return inputb;
			} else {
				return null;
			}
		}

		@Override
		public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
			super.onDataPacket(net, pkt);
			if(pkt.getTag().contains("assemblylinemachines:fluidcolor") && pkt.getTag().getInt("assemblylinemachines:fluidcolor") >= 0) {
				this.getLevel().sendBlockUpdated(this.getBlockPos(), getBlockState(), getBlockState(), 2);
			}
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean stillValid(Player arg0) {
			return false;
		}

		@Override
		public ItemStack removeItemNoUpdate(int arg0) {
			return null;
		}

		@Override
		public void setItem(int arg0, ItemStack arg1) {
		}
		
		public int getFluidColor(BlockAndTintGetter reader, BlockPos pos) {
			
			
			if(output != null && fluidColor != 0) {
				return fluidColor;
			}

			if(getBlockState().getValue(BathCraftingFluid.FLUID) == BathCraftingFluids.LAVA) {
				return 0xcb3d07;
			}else {
				return BiomeColors.getAverageWaterColor(reader, pos);
			}
		}
		
		public boolean hasOutput() {
			return output != null;
		}
		
		@Override
		public ItemStack removeItem(int pIndex, int pCount) {
			return null;
		}

	}

}
