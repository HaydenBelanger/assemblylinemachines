package me.haydenb.assemblylinemachines.block.energy;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.crafting.EntropyReactorCrafting;
import me.haydenb.assemblylinemachines.crafting.WorldCorruptionCrafting;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.PluginTOPRegistry.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import me.haydenb.assemblylinemachines.registry.utils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.*;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntropyReactor extends BlockScreenBlockEntity<BlockEntropyReactor.TEEntropyReactor>{

	public static class BlockEntropyReactorCore extends Block {

		public static final BooleanProperty CORE_CRITICAL = BooleanProperty.create("critical");

		public BlockEntropyReactorCore() {
			super(Block.Properties.of(Material.METAL).strength(3f, 15f).sound(SoundType.METAL));

			this.registerDefaultState(this.stateDefinition.any().setValue(CORE_CRITICAL, false));
		}

		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
			pBuilder.add(CORE_CRITICAL);
		}
	}

	private static final EnumProperty<EntropyReactorOptions> ENTROPY_REACTOR_PIECE = EnumProperty.create("part", EntropyReactorOptions.class);

	private static final HashMap<EntropyReactorOptions, HashMap<Direction, VoxelShape>> SHAPES = new HashMap<>();

	private static final VoxelShape SHAPE_C_S = Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(3, 3, 3, 16, 13, 16),
			Block.box(0, 13, 0, 16, 16, 16)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	private static final VoxelShape SHAPE_E_S = Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 3, 3, 16, 13, 16),
			Block.box(0, 13, 0, 16, 16, 16)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	static {
		HashMap<Direction, VoxelShape> bc = new HashMap<>();
		bc.put(Direction.SOUTH, SHAPE_C_S);
		bc.put(Direction.NORTH, Utils.rotateShape(Direction.SOUTH, Direction.NORTH, SHAPE_C_S));
		bc.put(Direction.EAST, Utils.rotateShape(Direction.SOUTH, Direction.EAST, SHAPE_C_S));
		bc.put(Direction.WEST, Utils.rotateShape(Direction.SOUTH, Direction.WEST, SHAPE_C_S));
		SHAPES.put(EntropyReactorOptions.CORNER, bc);
		SHAPES.put(EntropyReactorOptions.CORNER_ACTIVE, bc);

		HashMap<Direction, VoxelShape> be = new HashMap<>();
		be.put(Direction.SOUTH, SHAPE_E_S);
		be.put(Direction.NORTH, Utils.rotateShape(Direction.SOUTH, Direction.NORTH, SHAPE_E_S));
		be.put(Direction.EAST, Utils.rotateShape(Direction.SOUTH, Direction.EAST, SHAPE_E_S));
		be.put(Direction.WEST, Utils.rotateShape(Direction.SOUTH, Direction.WEST, SHAPE_E_S));
		SHAPES.put(EntropyReactorOptions.EDGE, be);
		SHAPES.put(EntropyReactorOptions.EDGE_ACTIVE, be);
	}

	public BlockEntropyReactor() {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).noOcclusion().dynamicShape().sound(SoundType.METAL),
				null, null, false, null, TEEntropyReactor.class);

		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.BLOCK));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

		HashMap<Direction, VoxelShape> shapes = SHAPES.get(state.getValue(ENTROPY_REACTOR_PIECE));
		if(shapes == null) {
			return Shapes.block();
		}else {
			return shapes.get(state.getValue(HorizontalDirectionalBlock.FACING));
		}
	}

	@Override
	public BlockEntity bteExtendBlockEntity(BlockPos pos, BlockState state) {
		EntropyReactorOptions ero = state.getValue(ENTROPY_REACTOR_PIECE);

		if(ero == EntropyReactorOptions.SCREEN || ero == EntropyReactorOptions.SCREEN_ACTIVE) {
			return Registry.getBlockEntity("entropy_reactor").create(pos, state);
		}else if(ero == EntropyReactorOptions.ENERGY || ero == EntropyReactorOptions.ITEM) {
			return Registry.getBlockEntity("entropy_reactor_slave").create(pos, state);
		}else {
			return null;
		}
	}

	private boolean hasBlockEntity(BlockState state) {
		EntropyReactorOptions ero = state.getValue(ENTROPY_REACTOR_PIECE);

		if(ero == EntropyReactorOptions.SCREEN || ero == EntropyReactorOptions.SCREEN_ACTIVE || ero == EntropyReactorOptions.ENERGY || ero == EntropyReactorOptions.ITEM) {
			return true;
		}

		return false;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!world.isClientSide) {
			if(hand.equals(InteractionHand.MAIN_HAND)) {
				if(hasBlockEntity(state) && (state.getValue(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.SCREEN || state.getValue(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.SCREEN_ACTIVE)) {
					return super.blockRightClickServer(state, world, pos, player);
				}else if(!hasBlockEntity(state) && state.getValue(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.BLOCK && player.getMainHandItem().isEmpty() && player.isShiftKeyDown()){

					Direction inDir = hit.getDirection().getOpposite();
					//This block targets lowest x to highest x, lowest y to highest y, and lowest z to highest z in that order.
					ArrayList<Pair<BlockPos, BlockState>> states = new ArrayList<>();


					Iterator<BlockPos> checker = BlockPos.betweenClosedStream(pos.relative(inDir, 2).relative(inDir.getClockWise()).relative(Direction.UP), pos.relative(inDir.getCounterClockWise()).relative(Direction.DOWN)).iterator();

					int i = 1;
					while(checker.hasNext()) {
						BlockPos checkPos = checker.next();


						if(i != 14 && !world.getBlockState(checkPos).getBlock().equals(Registry.getBlock("entropy_reactor_block"))) {
							player.displayClientMessage(Component.literal("Block @ " + checkPos.getX() + ", " + checkPos.getY() + ", " + checkPos.getZ() + " was not an Entropy Reactor Block.").withStyle(ChatFormatting.RED), true);
							return InteractionResult.CONSUME;
						}else if(i == 14 && !world.getBlockState(checkPos).getBlock().equals(Registry.getBlock("entropy_reactor_core"))) {
							player.displayClientMessage(Component.literal("Block @ " + checkPos.getX() + ", " + checkPos.getY() + ", " + checkPos.getZ() + " was not an Entropy Reactor Core.").withStyle(ChatFormatting.RED), true);
							return InteractionResult.CONSUME;
						}


						if(i != 14) {
							BlockState st = world.getBlockState(checkPos);
							Direction d = st.getValue(HorizontalDirectionalBlock.FACING);
							EntropyReactorOptions ero = st.getValue(ENTROPY_REACTOR_PIECE);
							if(i == 1 || i == 3 || i == 19 || i == 21 || i == 7 || i == 9 || i == 25 || i == 27) {

								ero = EntropyReactorOptions.CORNER;

								switch(i){
								case 1: d = Direction.SOUTH; break;
								case 3: d = Direction.WEST; break;
								case 19: d = Direction.EAST; break;
								case 21: d = Direction.NORTH; break;
								case 7: d = Direction.SOUTH; break;
								case 9: d = Direction.WEST; break;
								case 25: d = Direction.EAST; break;
								case 27: d = Direction.NORTH; break;
								}

							}else if(i == 2 || i == 10 || i == 12 || i == 20 || i == 8 || i == 16 || i == 18 || i == 26) {

								ero = EntropyReactorOptions.EDGE;

								switch(i){
								case 2: d = Direction.SOUTH; break;
								case 10: d = Direction.EAST; break;
								case 12: d = Direction.WEST; break;
								case 20: d = Direction.NORTH; break;
								case 8: d = Direction.SOUTH; break;
								case 16: d = Direction.EAST; break;
								case 18: d = Direction.WEST; break;
								case 26: d = Direction.NORTH; break;
								}

							}else if(i == 4 || i == 6 || i == 22 || i == 24) {

								ero = EntropyReactorOptions.SIDE_EDGE;

								switch(i){
								case 4: d = Direction.NORTH; break;
								case 6: d = Direction.EAST; break;
								case 22: d = Direction.WEST; break;
								case 24: d = Direction.SOUTH; break;
								}

							}else if(i == 17) {

								ero = EntropyReactorOptions.WINDOW;

							}else if(checkPos.equals(pos)) {

								ero = EntropyReactorOptions.SCREEN;

								d = hit.getDirection();
							}else if(checkPos.equals(pos.relative(inDir).relative(inDir.getCounterClockWise()))) {

								ero = EntropyReactorOptions.ENERGY;

								d = inDir.getCounterClockWise();
							}else if(checkPos.equals(pos.relative(inDir).relative(inDir.getClockWise()))) {

								ero = EntropyReactorOptions.ITEM;

								d = inDir.getClockWise();
							}else if(checkPos.equals(pos.relative(inDir, 2))){

								ero = EntropyReactorOptions.ITEM;

								d = inDir;

							}else {
								ero = EntropyReactorOptions.WALL;
							}

							states.add(Pair.of(new BlockPos(checkPos.getX(), checkPos.getY(), checkPos.getZ()), st.setValue(ENTROPY_REACTOR_PIECE, ero).setValue(HorizontalDirectionalBlock.FACING, d)));
						}

						i++;

					}

					for(Pair<BlockPos, BlockState> p : states) {

						world.setBlockAndUpdate(p.getFirst(), p.getSecond());

					}
				}
			}
		}
		return InteractionResult.PASS;

	}


	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {

		if(!world.getBlockState(pos).getBlock().equals(this)) {
			entropyReactorRemoved(state, world, pos, newState, isMoving, new ArrayList<>(), pos);
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	public void entropyReactorRemoved(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving, ArrayList<BlockPos> checked, BlockPos origPos) {

		if(state.getValue(ENTROPY_REACTOR_PIECE) != EntropyReactorOptions.BLOCK && !checked.contains(pos)) {
			checked.add(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
			if(world.getBlockEntity(pos) instanceof TEEntropyReactor) {

				TEEntropyReactor ter = (TEEntropyReactor) world.getBlockEntity(pos);
				Containers.dropContents(world, pos, ter.getItems());

				float entropy = ter.entropy;

				while(entropy > 0.2f) {
					entropy = ter.performEntropyTask(entropy);
				}
				world.removeBlockEntity(pos);
			}else if(world.getBlockEntity(pos) instanceof TEEntropyReactorSlave) {
				world.removeBlockEntity(pos);
			}

			if(!pos.equals(origPos)) {
				world.destroyBlock(pos, true);
			}

			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
			while(iter.hasNext()) {
				BlockPos iterPos = iter.next();
				if(world.getBlockState(iterPos).getBlock() instanceof BlockEntropyReactor) {
					((BlockEntropyReactor) world.getBlockState(iterPos).getBlock()).entropyReactorRemoved(state, world, iterPos, newState, isMoving, checked, origPos);
				}else if(world.getBlockState(iterPos).getBlock() instanceof BlockEntropyReactorCore) {
					world.setBlockAndUpdate(iterPos, world.getBlockState(iterPos).setValue(BlockEntropyReactorCore.CORE_CRITICAL, false));
				}
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING).add(ENTROPY_REACTOR_PIECE);
	}


	public static class TEEntropyReactorSlave extends BasicTileEntity {

		private int master_x = 0;
		private int master_y = 0;
		private int master_z = 0;

		private boolean assigned = false;

		private TEEntropyReactor reactor = null;

		private IItemHandler items = new IItemHandler() {

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				if(stack.getItem().equals(Registry.getItem("corrupted_shard")) && stack.hasTag()) {
					return true;
				}

				return false;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				TEEntropyReactor reactor = getReactor();

				if(reactor != null && isItemValid(slot, stack) && stack.getTag().contains("assemblylinemachines:internalitem")) {

					String stk = ForgeRegistries.ITEMS.getKey(ItemStack.of(stack.getTag().getCompound("assemblylinemachines:internalitem")).getItem()).toString();
					if(reactor.addShardToMap(stk, stack.getCount(), simulate)) {
						return ItemStack.EMPTY;
					}
				}


				return stack;

			}

			@Override
			public ItemStack getStackInSlot(int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlots() {
				return 1;
			}

			@Override
			public int getSlotLimit(int slot) {
				if(reactor != null) {
					return (int) reactor.capacity;
				}else {
					return 0;
				}
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				TEEntropyReactor reactor = getReactor();

				if(reactor == null) {
					return ItemStack.EMPTY;
				}

				ItemStack orig = reactor.getItem(3);

				if(orig.isEmpty()) {
					return ItemStack.EMPTY;
				}

				if(orig.getCount() < amount) {
					amount = orig.getCount();
				}

				if(!simulate) {
					orig.shrink(amount);
					reactor.sendUpdates();
				}

				return new ItemStack(orig.getItem(), amount);


			}
		};

		private LazyOptional<IItemHandler> itemsHandler = LazyOptional.of(()-> items);


		private IEnergyStorage energy = new IEnergyStorage() {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				return 0;
			}

			@Override
			public int getMaxEnergyStored() {
				if(reactor != null) {
					return reactor.properties.getCapacity();
				}

				return 0;
			}

			@Override
			public int getEnergyStored() {
				if(reactor != null) {
					return reactor.amount;
				}

				return 0;
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				TEEntropyReactor reactor = getReactor();

				if(reactor != null) {
					if(maxExtract > reactor.amount) {
						maxExtract = reactor.amount;
					}

					if(!simulate) {
						reactor.amount -= maxExtract;
						reactor.sendUpdates();
					}

					return maxExtract;
				}
				return 0;


			}

			@Override
			public boolean canReceive() {
				return false;
			}

			@Override
			public boolean canExtract() {
				return true;
			}
		};

		private LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);

		public TEEntropyReactorSlave(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}

		public TEEntropyReactorSlave(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("entropy_reactor_slave"), pos, state);
		}

		@Override
		public void saveAdditional(CompoundTag compound) {
			compound.putBoolean("assemblylinemachines:assigned", assigned);

			if(assigned) {
				compound.putInt("assemblylinemachines:master_x", master_x);
				compound.putInt("assemblylinemachines:master_y", master_y);
				compound.putInt("assemblylinemachines:master_z", master_z);
			}

			super.saveAdditional(compound);
		}

		@Override
		public void load(CompoundTag compound) {

			assigned = compound.getBoolean("assemblylinemachines:assigned");
			master_x = compound.getInt("assemblylinemachines:master_x");
			master_y = compound.getInt("assemblylinemachines:master_y");
			master_z = compound.getInt("assemblylinemachines:master_z");

			super.load(compound);
		}

		private void connectMaster(TEEntropyReactor reactor) {
			master_x = reactor.getBlockPos().getX();
			master_y = reactor.getBlockPos().getY();
			master_z = reactor.getBlockPos().getZ();

			assigned = true;

			this.reactor = reactor;
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(this.getBlockState().getValue(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.ITEM && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return itemsHandler.cast();
			}else if(this.getBlockState().getValue(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.ENERGY && cap == CapabilityEnergy.ENERGY) {
				return energyHandler.cast();
			}

			return LazyOptional.empty();
		}

		private TEEntropyReactor getReactor() {
			if(reactor == null) {

				if(!assigned) {
					return null;
				}
				BlockEntity te = this.getLevel().getBlockEntity(new BlockPos(master_x, master_y, master_z));

				if(!(te instanceof TEEntropyReactor)) {
					assigned = false;
					master_x = 0;
					master_y = 0;
					master_z = 0;
					sendUpdates();
					return null;
				}

				reactor = (TEEntropyReactor) te;
			}

			return reactor;

		}

	}

	public static class TEEntropyReactor extends EnergyMachine<ContainerEntropyReactor> implements ALMTicker<TEEntropyReactor>, TOPProvider{

		private int operationTimer = 0;
		private int burnTimer = 0;
		private int nBurnTimer = 300;

		private HashMap<String, Double> shardMap = new HashMap<>();
		private float varietyRating = 0f;
		private float total = 0f;
		private float capacity = 100f;

		private float entropy = 0f;
		private int entropyTimer = 0;
		private int nEntropyTimer = 200;

		private int genPerCycle = 0;
		private int cyclesRemaining = 0;

		private boolean slavesReporting = false;

		private ServerLevel sw = null;

		public TEEntropyReactor(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 4, Component.translatable("tileEntity.assemblylinemachines.entropy_reactor"), Registry.getContainerId("entropy_reactor"), ContainerEntropyReactor.class,
					new EnergyProperties(false, true, 5000000), pos, state);
		}

		public TEEntropyReactor(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("entropy_reactor"), pos, state);
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {

			if(cyclesRemaining != 0) {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(Component.literal("Discharging...").withStyle(ChatFormatting.GREEN)).text(Component.literal("+" + FormattingHelper.FEPT_FORMAT.format(genPerCycle / 20f) + " FE/t").withStyle(ChatFormatting.GREEN));
			}else {
				if(shardMap.isEmpty()) {
					probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(Component.literal("Idle").withStyle(ChatFormatting.RED)).text(Component.literal("0 FE/t"));
				}else {
					probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(Component.literal("Warming Up...").withStyle(ChatFormatting.LIGHT_PURPLE)).text(Component.literal("0 FE/t"));
				}
			}

			probeInfo.horizontal().item(new ItemStack(Registry.getItem("corrupted_shard"))).vertical().text(Component.literal("Shards").withStyle(ChatFormatting.LIGHT_PURPLE)).progress(Math.round(total), Math.round(capacity), probeInfo.defaultProgressStyle().filledColor(0xfff003fc).alternateFilledColor(0xfff003fc));
			probeInfo.horizontal().item(new ItemStack(Items.GREEN_DYE)).vertical().text(Component.literal("Variety").withStyle(ChatFormatting.YELLOW)).progress(Math.round(varietyRating * 100f), 100, probeInfo.defaultProgressStyle().filledColor(0xffc4d10f).alternateFilledColor(0xffc4d10f).suffix("%"));
			probeInfo.horizontal().item(new ItemStack(Items.COAL)).vertical().text(Component.literal("Entropy").withStyle(ChatFormatting.RED)).progress(Math.round(entropy * 100f), 100, probeInfo.defaultProgressStyle().filledColor(0xffd10f42).alternateFilledColor(0xffd10f42).suffix("%"));
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(stack.getItem() instanceof ItemUpgrade) {
				return true;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void load(CompoundTag compound) {

			super.load(compound);

			nBurnTimer = compound.getInt("assemblylinemachines:nburntimer");
			if(compound.contains("assemblylinemachines:shardmap")) {
				shardMap = Utils.GSON.fromJson(compound.getString("assemblylinemachines:shardmap"), HashMap.class);
			}

			varietyRating = compound.getFloat("assemblylinemachines:variety");
			total = compound.getFloat("assemblylinemachines:total");
			capacity = compound.getFloat("assemblylinemachines:capacity");

			genPerCycle = compound.getInt("assemblylinemachines:genpercycle");
			cyclesRemaining = compound.getInt("assemblylinemachines:cyclesremaining");

			entropy = compound.getFloat("assemblylinemachines:entropy");

		}

		@Override
		public void saveAdditional(CompoundTag compound) {

			compound.putInt("assemblylinemachines:nburntimer", nBurnTimer);

			compound.putString("assemblylinemachines:shardmap", Utils.GSON.toJson(shardMap));

			compound.putFloat("assemblylinemachines:variety", varietyRating);
			compound.putFloat("assemblylinemachines:total", total);
			compound.putFloat("assemblylinemachines:capacity", capacity);

			compound.putFloat("assemblylinemachines:entropy", entropy);

			compound.putInt("assemblylinemachines:genpercycle", genPerCycle);
			compound.putInt("assemblylinemachines:cyclesremaining", cyclesRemaining);
			super.saveAdditional(compound);
		}

		private void updateAllEdgesAndCorners(boolean active) {
			Direction facingDir = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
			BlockPos minA = this.getBlockPos().above().relative(facingDir.getClockWise());
			BlockPos maxA = this.getBlockPos().above().relative(facingDir.getCounterClockWise()).relative(facingDir.getOpposite(), 2);

			BlockPos minB = this.getBlockPos().below().relative(facingDir.getClockWise());
			BlockPos maxB = this.getBlockPos().below().relative(facingDir.getCounterClockWise()).relative(facingDir.getOpposite(), 2);

			Iterator<BlockPos> totalStream = Stream.concat(BlockPos.betweenClosedStream(minA, maxA), BlockPos.betweenClosedStream(minB, maxB)).iterator();

			while(totalStream.hasNext()) {
				BlockPos mod = totalStream.next();
				EntropyReactorOptions ero = this.getLevel().getBlockState(mod).getValue(ENTROPY_REACTOR_PIECE);
				EntropyReactorOptions setEro = null;
				if(active) {
					switch(ero) {
					case CORNER: setEro = EntropyReactorOptions.CORNER_ACTIVE; break;
					case EDGE: setEro = EntropyReactorOptions.EDGE_ACTIVE; break;
					default:
					}
				}else {
					switch(ero) {
					case CORNER_ACTIVE: setEro = EntropyReactorOptions.CORNER; break;
					case EDGE_ACTIVE: setEro = EntropyReactorOptions.EDGE; break;
					default:
					}
				}
				if(setEro != null) {
					this.getLevel().setBlockAndUpdate(mod, this.getLevel().getBlockState(mod).setValue(ENTROPY_REACTOR_PIECE, setEro));
				}


			}
		}

		private void updateCoreBlockBasedOnEntropy() {
			BlockPos pos = this.getBlockPos().relative(this.getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite());
			BlockState state = this.getLevel().getBlockState(pos);
			if(state.getBlock() instanceof BlockEntropyReactorCore) {
				boolean critical = state.getValue(BlockEntropyReactorCore.CORE_CRITICAL);
				if(entropy < 0.1f && critical) {
					this.getLevel().setBlockAndUpdate(pos, state.setValue(BlockEntropyReactorCore.CORE_CRITICAL, false));
				}else if(entropy >= 0.1f && !critical) {
					this.getLevel().setBlockAndUpdate(pos, state.setValue(BlockEntropyReactorCore.CORE_CRITICAL, true));
				}
			}
		}

		@Override
		public void tick() {

			if(!level.isClientSide) {
				boolean sendUpdates = false;

				if(cyclesRemaining != 0 && operationTimer++ == 20) {

					operationTimer = 0;

					if(genPerCycle + amount <= properties.getCapacity()) {
						amount += genPerCycle;
						cyclesRemaining--;
						if(cyclesRemaining == 0) {
							genPerCycle = 0;
						}
						sendUpdates = true;
					}
				}else {
					if(!shardMap.isEmpty() && burnTimer++ == nBurnTimer) {

						burnTimer = 0;

						sendUpdates = true;

						if(this.getUpgradeAmount(Upgrades.E_R_ENTROPIC_HARNESSER) != 0) {
							entropy += (total * 0.0025f) * (varietyRating * 9f);
						}else {
							genPerCycle = (int) total * 6000;
							cyclesRemaining = Math.round(varietyRating * 9f);

							this.updateAllEdgesAndCorners(true);
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getLevel().getBlockState(this.getBlockPos()).setValue(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.SCREEN_ACTIVE));
						}

						entropy += (0.2f - varietyRating) / 4f;
						if(entropy < 0f) {
							entropy = 0f;
						}else if(entropy > 1f) {
							entropy = 1f;
						}

						List<EntropyReactorCrafting> recipes = level.getRecipeManager().getAllRecipesFor(EntropyReactorCrafting.ERO_RECIPE);
						Collections.sort(recipes, Comparator.comparing((r) -> r.varietyReqd));
						Collections.reverse(recipes);
						for(EntropyReactorCrafting erc : recipes) {

							if(varietyRating >= erc.varietyReqd) {
								int wastecount = 0;
								while(this.getLevel().getRandom().nextFloat() < erc.odds && wastecount < erc.max) {
									wastecount++;
								}
								if(wastecount != 0) {
									ItemStack is = new ItemStack(erc.getResultItem().getItem(), wastecount);
									if(contents.get(3).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(3), is) && contents.get(3).getCount() + is.getCount() <= contents.get(3).getMaxStackSize())){
										if(contents.get(3).isEmpty()) {
											contents.set(3, is);
										}else {
											contents.get(3).grow(is.getCount());
										}
									}else {
										entropy += (is.getCount() / 200f);
									}
									break;

								}
							}

						}



						shardMap.clear();
						varietyRating = 0;
						total = 0;

						updateCoreBlockBasedOnEntropy();

					}

					if(cyclesRemaining == 0 && operationTimer++ == 20) {
						operationTimer = 0;

						int cycleUpCt = getUpgradeAmount(Upgrades.E_R_CYCLE_DELAY);

						switch(cycleUpCt) {
						case 3:
							nBurnTimer = 600;
							break;
						case 2:
							nBurnTimer = 500;
							break;
						case 1:
							nBurnTimer = 400;
							break;
						default:
							nBurnTimer = 300;
						}

						capacity = 20 * (getUpgradeAmount(Upgrades.E_R_CAPACITY) + 1);

						if(total > capacity) {
							total = capacity;
						}


						switch(getUpgradeAmount(Upgrades.E_R_ENTROPIC_HARNESSER)) {
						case 0:
							nEntropyTimer = 200;
							break;
						default:
							nEntropyTimer = 100;
						}

						if(!slavesReporting) {
							Direction inDir = getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();

							BlockEntity tePwr = this.getLevel().getBlockEntity(getBlockPos().relative(inDir).relative(inDir.getCounterClockWise()));

							if(tePwr instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave tePwrSlave = (TEEntropyReactorSlave) tePwr;
								tePwrSlave.connectMaster(this);
								tePwrSlave.sendUpdates();
							}

							BlockEntity teItm = this.getLevel().getBlockEntity(getBlockPos().relative(inDir).relative(inDir.getClockWise()));

							if(teItm instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave teItmSlave = (TEEntropyReactorSlave) teItm;
								teItmSlave.connectMaster(this);
								teItmSlave.sendUpdates();
							}

							BlockEntity teWaste = this.getLevel().getBlockEntity(getBlockPos().relative(inDir, 2));
							if(teWaste instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave teWasteSlave = (TEEntropyReactorSlave) teWaste;
								teWasteSlave.connectMaster(this);
								teWasteSlave.sendUpdates();
							}

							slavesReporting = true;
						}

						this.updateAllEdgesAndCorners(false);
						if(getBlockState().getValue(ENTROPY_REACTOR_PIECE) != EntropyReactorOptions.SCREEN) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.SCREEN));
						}

						sendUpdates = true;
					}

				}

				if(entropyTimer++ == nEntropyTimer) {
					entropyTimer = 0;

					sendUpdates = true;

					entropy = performEntropyTask(entropy);

					if(entropy < 0f) {
						entropy = 0f;
					}else if(entropy > 1f) {
						entropy = 1f;
					}

				}

				if(sendUpdates) {
					sendUpdates();
				}
			}


		}

		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 0; i < 3; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}

		public float performEntropyTask(float entropy) {
			boolean hasUpgrade = getUpgradeAmount(Upgrades.E_R_ENTROPIC_HARNESSER) == 1;
			RandomSource rand = this.getLevel().getRandom();
			if(!hasUpgrade && entropy > 0.1f) {

				//When entropy is above 10%, randomly performs potion effects between 33-100% of the time, maxing out at 30% entropy
				if((rand.nextFloat() * 0.3f) < entropy) {

					int rgM;

					if(entropy > 0.3f) {
						rgM = 10;
					}else {
						rgM = 5;
					}

					List<Player> list = this.getLevel().getEntitiesOfClass(Player.class, new AABB(getBlockPos().relative(Direction.UP, rgM).relative(Direction.NORTH, rgM).relative(Direction.WEST, rgM),
							getBlockPos().relative(Direction.DOWN, rgM).relative(Direction.SOUTH, rgM).relative(Direction.EAST, rgM)));
					for(Player pl : list) {
						if(entropy < 0.3f) {
							pl.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
						}else if(entropy < 0.7f) {
							pl.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
							pl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
							pl.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
						}else {
							pl.addEffect(new MobEffectInstance(Registry.ENTROPY_POISONING.get(), 20));
						}

					}

					entropy -= 0.0025f;

				}



			}

			if(!hasUpgrade && entropy > 0.3f && level.getDifficulty() != Difficulty.PEACEFUL) {

				if((rand.nextFloat() * 0.5f) < entropy) {

					if(sw == null) {
						sw = this.getLevel().getServer().getLevel(this.getLevel().dimension());
					}

					int count = Math.round(entropy * 4f);

					float spamt = 0;

					for(int i = 0; i < count; i++) {
						double d0 = this.getBlockPos().getX() + (level.random.nextDouble() - level.random.nextDouble()) * 6 + 0.5D;
						double d1 = this.getBlockPos().getY() + level.random.nextInt(3) - 1;
						double d2 = this.getBlockPos().getZ() + (level.random.nextDouble() - level.random.nextDouble()) * 6 + 0.5D;

						EntityType<?> type = Registry.CORRUPT_SHELL.get();
						for(int j = 0; j < 10; j++) {

							if(this.getLevel().noCollision(type.getAABB(d0, d1, d2)) && SpawnPlacements.checkSpawnRules(type, sw, MobSpawnType.SPAWNER, new BlockPos(d0, d1, d2), this.getLevel().getRandom())) {
								Entity entity = type.create(this.getLevel());
								entity.moveTo(d0, d1, d2, rand.nextFloat() * 360f, 0f);

								this.getLevel().addFreshEntity(entity);
								spamt++;

								break;


							}
						}
					}

					entropy -= (0.0025f * spamt);

				}
			}

			if(entropy > 0.5f) {

				int upMx = hasUpgrade ? 3 : 1;
				int max = Math.round(entropy * 17f * upMx);
				int i = 0;
				int ii = 0;

				upMx = hasUpgrade ? 2 : 1;

				while(i < max && ii < (max * 3)) {

					double x = this.getBlockPos().getX() + ((rand.nextDouble() * (20 * upMx)) - (10 * upMx));
					double y = this.getBlockPos().getY() + ((rand.nextDouble() * 10) - 5);
					double z = this.getBlockPos().getZ() + ((rand.nextDouble() * (20 * upMx)) - (10 * upMx));

					BlockPos posx = new BlockPos(new Vec3(x, y, z));
					if(!this.getLevel().isEmptyBlock(posx) || !this.getLevel().getFluidState(posx).getType().equals(Fluids.EMPTY)) {
						for(WorldCorruptionCrafting recipe : this.getLevel().getRecipeManager().getAllRecipesFor(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE)) {
							Optional<Block> res = recipe.testBlock(this.getLevel().getRandom(), this.getLevel().getBlockState(posx).getBlock());
							if(res.isPresent()) {
								Block block = res.get();
								//Special processing for DoublePlantBlock
								if(block instanceof ISpecialEntropyPlacement) {
									((ISpecialEntropyPlacement) block).place(this.getLevel(), block.defaultBlockState(), posx);
								}else {
									this.getLevel().setBlockAndUpdate(posx, block.defaultBlockState());
								}
								entropy -= 0.00025f;
								i++;
							}
							Optional<Fluid> resF = recipe.testFluid(this.getLevel().getFluidState(posx).getType());
							if(resF.isPresent()) {
								this.getLevel().setBlockAndUpdate(posx, resF.get().defaultFluidState().createLegacyBlock());
								entropy -= 0.00025f;
								i++;
							}
						}
					}
					ii++;
				}
			}

			if(!hasUpgrade && entropy > 0.98f && ALMConfig.getServerConfig().reactorExplosions().get()) {
				this.getLevel().explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 20f, true, BlockInteraction.DESTROY);
				entropy = 0f;
			}

			updateCoreBlockBasedOnEntropy();

			return entropy;

		}


		private boolean addShardToMap(String shard, int c, boolean sim) {

			total = shardMap.values().stream().reduce(0d, (a, b) -> a + b).intValue();
			if(total > capacity) {
				total = capacity;
			}
			if(total + c > capacity) {

				return false;
			}
			if(!sim) {
				total += c;
				shardMap.put(shard, shardMap.getOrDefault(shard, 0d) + c);
				varietyRating = 0f;
				float types = 0f;



				for(String s : shardMap.keySet()) {

					types++;

					float percent = shardMap.get(s).floatValue() / total;


					if(getUpgradeAmount(Upgrades.E_R_VARIETY) != 0) {

						if(percent < 0.05f) {
							varietyRating += 1.6f;
						}else if(percent < 0.1f) {
							varietyRating += 1.3f;
						}else if(percent < 0.15f) {
							varietyRating += 1.1f;
						}else if(percent < 0.2f) {
							varietyRating += 1f;
						}else if(percent < 0.3f) {
							varietyRating += 0.7f;
						}else if(percent < 0.4f) {
							varietyRating += 0.2f;
						}
					}else {
						if(percent < 0.1f) {
							varietyRating += 1f;
						}else if(percent < 0.25f) {
							varietyRating += 0.8f;
						}else if(percent < 0.5f) {
							varietyRating += 0.6f;
						}else if(percent < 0.75f) {
							varietyRating += 0.5f;
						}else if(percent < 0.8f){
							varietyRating += 0.3f;
						}else if(percent < 0.9f) {
							varietyRating += 0.2f;
						}else {
							varietyRating += 0.1f;
						}
					}

				}

				varietyRating = varietyRating / types;

				sendUpdates();


			}

			return true;

		}


		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return LazyOptional.empty();
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return LazyOptional.empty();
		}

	}

	public static class ContainerEntropyReactor extends ContainerALMBase<TEEntropyReactor>{
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerEntropyReactor(final int windowId, final Inventory playerInventory, final TEEntropyReactor tileEntity) {
			super(Registry.getContainerType("entropy_reactor"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 3);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 57, tileEntity));
		}

		public ContainerEntropyReactor(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEEntropyReactor.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenEntropyReactor extends ScreenALMEnergyBased<ContainerEntropyReactor>{
		TEEntropyReactor tsfm;

		private DecimalFormat num = new DecimalFormat("##0.#");

		public ScreenEntropyReactor(ContainerEntropyReactor screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "entropy_reactor", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			int progVariety = Math.round((tsfm.varietyRating/1f) * 52f);
			if(progVariety > 52) {
				progVariety = 52;
			}
			super.blit(x+37, y+17 + (52 - progVariety), 182, 52 + (52 - progVariety), 6, progVariety);

			int progTotal = Math.round((tsfm.total/tsfm.capacity) * 52f);
			super.blit(x+50, y+17 + (52 - progTotal), 176, 52 + (52 - progTotal), 6, progTotal);

			int progEntropy = Math.round((tsfm.entropy/1f) * 79f);
			if(progEntropy > 79) {
				progEntropy = 79;
			}
			super.blit(x+63, y+63, 176, 104, progEntropy, 6);

			String status;
			int color;

			if(tsfm.cyclesRemaining != 0) {
				color = 0x44a300;
				status = "Status: Generating...\nRemaining time: " + tsfm.cyclesRemaining + " seconds!\nGenerating " + FormattingHelper.FEPT_FORMAT.format(tsfm.genPerCycle / 20f) + " FE/t!";
			}else {
				if(tsfm.shardMap.isEmpty()) {
					color = 0xffffff;
					status = "Status: Idle...\nAwaiting Corrupted Shards.";
				}else {
					color = 0xe0d100;
					status = "Status: Warming up...\nYou can keep loading shards!";
				}
			}


			if(!tsfm.getItem(3).isEmpty()) {
				status = status + "\nWaste needs to be removed!";
			}

			int i = 0;
			for(String s : status.split("\n")) {

				float wsc = 73f / this.font.width(s);
				if(wsc > 1f) wsc = 1f;

				if(s.equals("Status:")) {
					ScreenMath.renderScaledText(this.font, x+66, y+20+(i*9), wsc, s, false, color);
				}else {
					ScreenMath.renderScaledText(this.font, x+66, y+20+(i*9), wsc, s, false, 0xffffff);
				}

				i++;
			}



		}


		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if(mouseX >= x + 50 && mouseY >= y + 17 && mouseX <= x + 55 && mouseY <= y + 68) {
				ArrayList<Component> str = new ArrayList<>();
				if(tsfm.total != 0) {
					str.add(Component.literal("Total Shards Stored").withStyle(ChatFormatting.DARK_PURPLE));
					str.add(Component.literal(num.format(tsfm.total) + "/" + num.format(tsfm.capacity)).withStyle(ChatFormatting.LIGHT_PURPLE));
				}else {
					str.add(Component.literal("Shard Tank Empty").withStyle(ChatFormatting.DARK_RED));
				}

				str.add(Component.literal("Runs at " + FormattingHelper.GENERAL_FORMAT.format((tsfm.total * 6000f) / 20f) + " FE/t.").withStyle(ChatFormatting.GRAY));

				this.renderComponentTooltip(this.mx, str, mouseX - x, mouseY - y);
			}

			if(mouseX >= x + 37 && mouseY >= y + 17 && mouseX <= x + 42 && mouseY <= y + 68) {
				ArrayList<Component> str = new ArrayList<>();

				ChatFormatting cc;
				if(tsfm.varietyRating < 0.2f) {

					cc = ChatFormatting.DARK_RED;

				}else if(tsfm.varietyRating < 0.5f) {

					cc = ChatFormatting.GOLD;
				}else if(tsfm.varietyRating < 0.7f) {

					cc = ChatFormatting.GREEN;
				}else {

					cc = ChatFormatting.DARK_GREEN;
				}


				str.add(Component.literal("Variety Rating: ").withStyle(ChatFormatting.YELLOW).append(Component.literal(num.format(tsfm.varietyRating * 100f) + "%").withStyle(cc)));

				int cycles = Math.round(tsfm.varietyRating * 9f);

				if(cycles == 1) {
					str.add(Component.literal("Powered for 1 second.").withStyle(ChatFormatting.GRAY));
				}else {
					str.add(Component.literal("Powered for " + cycles + " seconds.").withStyle(ChatFormatting.GRAY));
				}

				this.renderComponentTooltip(this.mx, str, mouseX - x, mouseY - y);

			}

			if(mouseX >= x + 63 && mouseY >= y + 63 && mouseX <= x + 141 && mouseY <= y + 68) {
				ArrayList<Component> str = new ArrayList<>();

				ChatFormatting cc;
				if(tsfm.entropy < 0.05f) {
					cc = ChatFormatting.DARK_GREEN;
				}else if(tsfm.entropy < 0.2f) {
					cc = ChatFormatting.RED;
				}else {
					cc = ChatFormatting.DARK_RED;
				}

				str.add(Component.literal("Core Entropy Levels: ").withStyle(ChatFormatting.RED).append(Component.literal(num.format(tsfm.entropy * 100f) + "%").withStyle(cc)));


				if(tsfm.entropy > 0.1f) {
					str.add(Component.literal("Bad things will happen if this is not lowered.").withStyle(ChatFormatting.GRAY));
					str.add(Component.literal("Lower by keeping Variety high!").withStyle(ChatFormatting.GRAY));
				}

				this.renderComponentTooltip(this.mx, str, mouseX - x, mouseY - y);
			}

		}
	}

	public static enum EntropyReactorOptions implements StringRepresentable{
		EDGE(false), CORNER(false), SIDE_EDGE(false), WALL(false), ITEM(true), ENERGY(true), SCREEN(true), SCREEN_ACTIVE(true), BLOCK(false), EDGE_ACTIVE(false), CORNER_ACTIVE(false), WINDOW(false);



		@SuppressWarnings("unused")
		private final boolean hasTE;

		EntropyReactorOptions(boolean hasTE){
			this.hasTE = hasTE;
		}


		@Override
		public String getSerializedName() {
			return this.toString().toLowerCase();
		}
	}

	public static interface ISpecialEntropyPlacement{

		default public void place(LevelAccessor level, BlockState state, BlockPos pos) {
			this.place(level, state, pos, 3);
		}

		public void place(LevelAccessor level, BlockState state, BlockPos pos, int flag);
	}

}
