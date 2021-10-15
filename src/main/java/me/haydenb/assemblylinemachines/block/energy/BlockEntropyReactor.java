package me.haydenb.assemblylinemachines.block.energy;

import java.text.DecimalFormat;
import java.util.*;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.helpers.*;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.other.PluginTOP.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.MathHelper;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.*;

public class BlockEntropyReactor extends BlockScreenTileEntity<BlockEntropyReactor.TEEntropyReactor> {


	private static final EnumProperty<EntropyReactorOptions> ENTROPY_REACTOR_PIECE = EnumProperty.create("part", EntropyReactorOptions.class);

	public BlockEntropyReactor() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL),
				null, null, false, null, TEEntropyReactor.class);

		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.BLOCK));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if(state.get(ENTROPY_REACTOR_PIECE).hasTE) {
			return true;
		}
		return false;
	}


	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {

		EntropyReactorOptions ero = state.get(ENTROPY_REACTOR_PIECE);

		if(ero == EntropyReactorOptions.SCREEN) {
			return Registry.getTileEntity("entropy_reactor").create();
		}else {
			return Registry.getTileEntity("entropy_reactor_slave").create();
		}

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if(!world.isRemote) {
			if(hand.equals(Hand.MAIN_HAND)) {
				if(hasTileEntity(state) == true && (state.get(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.SCREEN || state.get(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.SCREEN_ACTIVE)) {
					//Open screen if segment is of type SCREEN.
					return super.blockRightClickServer(state, world, pos, player);
				}else if(hasTileEntity(state) == false && state.get(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.BLOCK && player.getHeldItemMainhand().isEmpty() && player.isSneaking()){

					Direction inDir = hit.getFace().getOpposite();
					//This block targets lowest x to highest x, lowest y to highest y, and lowest z to highest z in that order.
					ArrayList<Pair<BlockPos, BlockState>> states = new ArrayList<>();


					Iterator<BlockPos> checker = BlockPos.getAllInBox(pos.offset(inDir, 2).offset(inDir.rotateY()).offset(Direction.UP), pos.offset(inDir.rotateYCCW()).offset(Direction.DOWN)).iterator();

					int i = 1;
					while(checker.hasNext()) {
						BlockPos checkPos = checker.next();


						if(i != 14 && !world.getBlockState(checkPos).getBlock().equals(Registry.getBlock("entropy_reactor_block"))) {
							player.sendStatusMessage(new StringTextComponent("Block @ " + checkPos.getX() + ", " + checkPos.getY() + ", " + checkPos.getZ() + " was not an Entropy Reactor Block.").deepCopy().mergeStyle(TextFormatting.RED), true);
							return ActionResultType.CONSUME;
						}else if(i == 14 && !world.getBlockState(checkPos).getBlock().equals(Registry.getBlock("entropy_reactor_core"))) {
							player.sendStatusMessage(new StringTextComponent("Block @ " + checkPos.getX() + ", " + checkPos.getY() + ", " + checkPos.getZ() + " was not an Entropy Reactor Core.").deepCopy().mergeStyle(TextFormatting.RED), true);
							return ActionResultType.CONSUME;
						}

						
						if(i != 14) {
							BlockState st = world.getBlockState(checkPos);
							Direction d = st.get(HorizontalBlock.HORIZONTAL_FACING);
							EntropyReactorOptions ero = st.get(ENTROPY_REACTOR_PIECE);
							if(i == 1 || i == 3 || i == 19 || i == 21) {

								ero = EntropyReactorOptions.BOTTOM_CORNER;

								switch(i){
								case 1: d = Direction.SOUTH; break;
								case 3: d = Direction.WEST; break;
								case 19: d = Direction.EAST; break;
								case 21: d = Direction.NORTH; break;
								}

							}else if(i == 2 || i == 10 || i == 12 || i == 20) {

								ero = EntropyReactorOptions.BOTTOM_EDGE;

								switch(i){
								case 2: d = Direction.SOUTH; break;
								case 10: d = Direction.EAST; break;
								case 12: d = Direction.WEST; break;
								case 20: d = Direction.NORTH; break;
								}

							}else if(i == 7 || i == 9 || i == 25 || i == 27) {

								ero = EntropyReactorOptions.TOP_CORNER;

								switch(i){
								case 7: d = Direction.SOUTH; break;
								case 9: d = Direction.WEST; break;
								case 25: d = Direction.EAST; break;
								case 27: d = Direction.NORTH; break;
								}

							}else if(i == 8 || i == 16 || i == 18 || i == 26) {

								ero = EntropyReactorOptions.TOP_EDGE;

								switch(i){
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


							}else if(checkPos.equals(pos)) {

								ero = EntropyReactorOptions.SCREEN;

								d = hit.getFace();
							}else if(checkPos.equals(pos.offset(inDir).offset(inDir.rotateYCCW()))) {

								ero = EntropyReactorOptions.ENERGY;

								d = inDir.rotateYCCW();
							}else if(checkPos.equals(pos.offset(inDir).offset(inDir.rotateY()))) {

								ero = EntropyReactorOptions.ITEM;

								d = inDir.rotateY();
							}else if(checkPos.equals(pos.offset(inDir, 2))){

								ero = EntropyReactorOptions.ITEM;
								
								d = inDir.getOpposite();
								
							}else {
								ero = EntropyReactorOptions.WALL;
							}

							states.add(Pair.of(new BlockPos(checkPos.getX(), checkPos.getY(), checkPos.getZ()), st.with(ENTROPY_REACTOR_PIECE, ero).with(HorizontalBlock.HORIZONTAL_FACING, d)));
						}
						
						i++;
						
					}

					for(Pair<BlockPos, BlockState> p : states) {


						world.setBlockState(p.getFirst(), p.getSecond());

					}
				}
			}
		}
		return ActionResultType.PASS;

	}


	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {

		if(!world.getBlockState(pos).getBlock().equals(this)) {
			entropyReactorRemoved(state, world, pos, newState, isMoving, new ArrayList<>(), pos);
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	public void entropyReactorRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving, ArrayList<BlockPos> checked, BlockPos origPos) {

		if(state.get(ENTROPY_REACTOR_PIECE) != EntropyReactorOptions.BLOCK && !checked.contains(pos)) {
			if(!pos.equals(origPos)) {
				world.setBlockState(pos, state.with(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.BLOCK));
			}

			checked.add(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
			if(world.getTileEntity(pos) instanceof TEEntropyReactor) {
				
				TEEntropyReactor ter = (TEEntropyReactor) world.getTileEntity(pos);
				InventoryHelper.dropItems(world, pos, ter.getItems());
				
				float entropy = ter.entropy;
				
				while(entropy > 0.2f) {
					entropy = ter.performEntropyTask(entropy);
				}
				world.removeTileEntity(pos);
			}else if(world.getTileEntity(pos) instanceof TEEntropyReactorSlave) {
				world.removeTileEntity(pos);
			}

			Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
			while(iter.hasNext()) {
				BlockPos iterPos = iter.next();
				if(world.getBlockState(iterPos).getBlock() instanceof BlockEntropyReactor) { 
					((BlockEntropyReactor) world.getBlockState(iterPos).getBlock()).entropyReactorRemoved(state, world, iterPos, newState, isMoving, checked, origPos);
				}
			}
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING).add(ENTROPY_REACTOR_PIECE);
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

					String stk = ItemStack.read(stack.getTag().getCompound("assemblylinemachines:internalitem")).getItem().getRegistryName().toString();
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
				
				ItemStack orig = reactor.getStackInSlot(3);
				
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

		public TEEntropyReactorSlave(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEEntropyReactorSlave() {
			this(Registry.getTileEntity("entropy_reactor_slave"));
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putBoolean("assemblylinemachines:assigned", assigned);

			if(assigned == true) {
				compound.putInt("assemblylinemachines:master_x", master_x);
				compound.putInt("assemblylinemachines:master_y", master_y);
				compound.putInt("assemblylinemachines:master_z", master_z);
			}

			return super.write(compound);
		}

		@Override
		public void read(BlockState state, CompoundNBT compound) {

			assigned = compound.getBoolean("assemblylinemachines:assigned");
			master_x = compound.getInt("assemblylinemachines:master_x");
			master_y = compound.getInt("assemblylinemachines:master_y");
			master_z = compound.getInt("assemblylinemachines:master_z");

			super.read(state, compound);
		}

		private void connectMaster(TEEntropyReactor reactor) {
			master_x = reactor.getPos().getX();
			master_y = reactor.getPos().getY();
			master_z = reactor.getPos().getZ();

			assigned = true;

			this.reactor = reactor;
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(this.getBlockState().get(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.ITEM && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return itemsHandler.cast();
			}else if(this.getBlockState().get(ENTROPY_REACTOR_PIECE) == EntropyReactorOptions.ENERGY && cap == CapabilityEnergy.ENERGY) {
				return energyHandler.cast();
			}

			return LazyOptional.empty();
		}

		private TEEntropyReactor getReactor() {
			if(reactor == null) {

				if(assigned == false) {
					return null;
				}
				TileEntity te = world.getTileEntity(new BlockPos(master_x, master_y, master_z));

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

	public static class TEEntropyReactor extends EnergyMachine<ContainerEntropyReactor> implements ITickableTileEntity, TOPProvider{

		private static final Gson GSON = new Gson();

		private int operationTimer = 0;
		private int burnTimer = 0;
		private int nBurnTimer = 300;

		private HashMap<String, Double> shardMap = new HashMap<>();
		private float varietyRating = 0f;
		private float total = 0f;
		private float capacity = 100f;
		
		private float entropy = 0f;
		private float entropyTimer = 0f;

		private int genPerCycle = 0;
		private int cyclesRemaining = 0;

		private boolean slavesReporting = false;
		
		private ServerWorld sw = null;

		public TEEntropyReactor(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 4, new TranslationTextComponent("tileEntity.assemblylinemachines.entropy_reactor"), Registry.getContainerId("entropy_reactor"), ContainerEntropyReactor.class,
					new EnergyProperties(false, true, 5000000));
		}

		public TEEntropyReactor() {
			this(Registry.getTileEntity("entropy_reactor"));
		}
		
		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState state, IProbeHitData data) {
			
			if(cyclesRemaining != 0) {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new StringTextComponent("§aDischarging...")).text(new StringTextComponent("§a+" + Formatting.FEPT_FORMAT.format((float)genPerCycle / 20f) + " FE/t"));
			}else {
				if(shardMap.isEmpty()) {
					probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new StringTextComponent("§cIdle")).text(new StringTextComponent("0 FE/t"));
				}else {
					probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new StringTextComponent("§dWarming Up...")).text(new StringTextComponent("0 FE/t"));
				}
			}
			
			probeInfo.horizontal().item(new ItemStack(Registry.getItem("corrupted_shard"))).vertical().text(new StringTextComponent("§dShards")).progress(Math.round(total), Math.round(capacity), probeInfo.defaultProgressStyle().filledColor(0xfff003fc).alternateFilledColor(0xfff003fc));
			probeInfo.horizontal().item(new ItemStack(Items.GREEN_DYE)).vertical().text(new StringTextComponent("§eVariety")).progress(Math.round(varietyRating * 100f), 100, probeInfo.defaultProgressStyle().filledColor(0xffc4d10f).alternateFilledColor(0xffc4d10f).suffix("%"));
			probeInfo.horizontal().item(new ItemStack(Registry.getItem("poor_strange_matter"))).vertical().text(new StringTextComponent("§cEntropy")).progress(Math.round(entropy * 100f), 100, probeInfo.defaultProgressStyle().filledColor(0xffd10f42).alternateFilledColor(0xffd10f42).suffix("%"));
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
		public void read(CompoundNBT compound) {

			super.read(compound);

			nBurnTimer = compound.getInt("assemblylinemachines:nburntimer");
			if(compound.contains("assemblylinemachines:shardmap")) {
				shardMap = GSON.fromJson(compound.getString("assemblylinemachines:shardmap"), HashMap.class);
			}

			varietyRating = compound.getFloat("assemblylinemachines:variety");
			total = compound.getFloat("assemblylinemachines:total");
			capacity = compound.getFloat("assemblylinemachines:capacity");

			genPerCycle = compound.getInt("assemblylinemachines:genpercycle");
			cyclesRemaining = compound.getInt("assemblylinemachines:cyclesremaining");
			
			entropy = compound.getFloat("assemblylinemachines:entropy");

		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {

			compound.putInt("assemblylinemachines:nburntimer", nBurnTimer);

			compound.putString("assemblylinemachines:shardmap", GSON.toJson(shardMap));

			compound.putFloat("assemblylinemachines:variety", varietyRating);
			compound.putFloat("assemblylinemachines:total", total);
			compound.putFloat("assemblylinemachines:capacity", capacity);
			
			compound.putFloat("assemblylinemachines:entropy", entropy);

			compound.putInt("assemblylinemachines:genpercycle", genPerCycle);
			compound.putInt("assemblylinemachines:cyclesremaining", cyclesRemaining);
			return super.write(compound);
		}

		@Override
		public void tick() {

			if(!world.isRemote) {
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

						genPerCycle = (int) total * 6000;
						cyclesRemaining = Math.round(varietyRating * 9f);

						entropy += (0.2f - varietyRating) / 4f;
						if(entropy < 0f) {
							entropy = 0f;
						}else if(entropy > 1f) {
							entropy = 1f;
						}
						
						int wastecount = 0;
						
						for(int i = 0; i <= total; i++) {
							if(General.RAND.nextInt(10) == 0) {
								wastecount++;
							}
						}
						
						Item i;
						if(varietyRating < 0.2f) {
							i = Registry.getItem("poor_strange_matter");
						}else if(varietyRating > 0.8f) {
							i = Registry.getItem("rich_strange_matter");
						}else {
							i = Registry.getItem("strange_matter");
						}
						
						ItemStack is = new ItemStack(i, wastecount);
						
						if(contents.get(3).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(3), is) && contents.get(3).getCount() + is.getCount() <= contents.get(3).getMaxStackSize())){
							if(contents.get(3).isEmpty()) {
								contents.set(3, is);
							}else {
								contents.get(3).grow(is.getCount());
							}
						}else {
							entropy += ((float) is.getCount() / 200f);
						}
						
						shardMap.clear();
						varietyRating = 0;
						total = 0;

						world.setBlockState(pos, world.getBlockState(pos).with(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.SCREEN_ACTIVE));

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

						if(slavesReporting == false) {
							Direction inDir = getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).getOpposite();

							TileEntity tePwr = world.getTileEntity(getPos().offset(inDir).offset(inDir.rotateYCCW()));

							if(tePwr instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave tePwrSlave = (TEEntropyReactorSlave) tePwr;
								tePwrSlave.connectMaster(this);
								tePwrSlave.sendUpdates();
							}

							TileEntity teItm = world.getTileEntity(getPos().offset(inDir).offset(inDir.rotateY()));

							if(teItm instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave teItmSlave = (TEEntropyReactorSlave) teItm;
								teItmSlave.connectMaster(this);
								teItmSlave.sendUpdates();
							}
							
							TileEntity teWaste = world.getTileEntity(getPos().offset(inDir, 2));
							if(teWaste instanceof TEEntropyReactorSlave) {
								TEEntropyReactorSlave teWasteSlave = (TEEntropyReactorSlave) teWaste;
								teWasteSlave.connectMaster(this);
								teWasteSlave.sendUpdates();
							}

							slavesReporting = true;
						}


						if(getBlockState().get(ENTROPY_REACTOR_PIECE) != EntropyReactorOptions.SCREEN) {
							world.setBlockState(pos, getBlockState().with(ENTROPY_REACTOR_PIECE, EntropyReactorOptions.SCREEN));
						}

						sendUpdates = true;
					}

				}
				
				if(entropyTimer++ == 200) {
					entropyTimer = 0;
					
					sendUpdates = true;
					
					entropy = performEntropyTask(entropy);
					
					if(entropy < 0f) {
						entropy = 0f;
					}
					
				}

				if(sendUpdates == true) {
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
			
			if(entropy > 0.1f) {
				
				//When entropy is above 10%, randomly performs potion effects between 33-100% of the time, maxing out at 30% entropy
				if((General.RAND.nextFloat() * 0.3f) < entropy) {
					
					int rgM;
					
					if(entropy > 0.3f) {
						rgM = 10;
					}else {
						rgM = 5;
					}
					
					List<PlayerEntity> list = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(getPos().offset(Direction.UP, rgM).offset(Direction.NORTH, rgM).offset(Direction.WEST, rgM), 
							getPos().offset(Direction.DOWN, rgM).offset(Direction.SOUTH, rgM).offset(Direction.EAST, rgM)));
					for(PlayerEntity pl : list) {
						if(entropy < 0.3f) {
							pl.addPotionEffect(new EffectInstance(Effects.POISON, 80));
						}else if(entropy < 0.7f) {
							pl.addPotionEffect(new EffectInstance(Effects.POISON, 100));
							pl.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 120, 1));
							pl.addPotionEffect(new EffectInstance(Effects.HUNGER, 120, 1));
						}else {
							pl.addPotionEffect(new EffectInstance(Registry.getEffect("entropy_poisoning"), 20));
						}
						
					}
					
					entropy -= 0.0025f;
					
				}
				
				
				
			}
			
			if(entropy > 0.3f) {
				
				if((General.RAND.nextFloat() * 0.5f) < entropy) {
					
					if(sw == null) {
						sw = world.getServer().getWorld(world.getDimensionKey());
					}
					
					int count = Math.round(entropy * 4f);
					
					float spamt = 0;
					
					for(int i = 0; i < count; i++) {
						double d0 = (double)pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) 6 + 0.5D;
						double d1 = (double)(pos.getY() + world.rand.nextInt(3) - 1);
						double d2 = (double)pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) 6 + 0.5D;
						
						EntityType<?> type = EntityCorruptShell.CORRUPT_SHELL;
						for(int j = 0; j < 10; j++) {
							
							if(world.hasNoCollisions(type.getBoundingBoxWithSizeApplied(d0, d1, d2)) && EntitySpawnPlacementRegistry.canSpawnEntity(type, sw, SpawnReason.SPAWNER, new BlockPos(d0, d1, d2), world.getRandom())) {
								Entity entity = type.create(world);
								entity.setLocationAndAngles(d0, d1, d2, General.RAND.nextFloat() * 360f, 0f);
								
								world.addEntity(entity);
								spamt++;
								
								break;
								
								
							}
						}
					}
					
					entropy -= (0.0025f * spamt);
					
				}
			}
			
			if(entropy > 0.5f) {
				
				
				int max = Math.round(entropy * 17f);
				int i = 0;
				int ii = 0;
				
				while(i < max) {
					
					double x = pos.getX() + ((General.RAND.nextDouble() * 20) - 10);
					double y = pos.getY() + ((General.RAND.nextDouble() * 10) - 10);
					double z = pos.getZ() + ((General.RAND.nextDouble() * 20) - 10);
					
					BlockPos posx = new BlockPos(new Vector3d(x, y, z));
					BlockState bs = null;
					Block obs = world.getBlockState(posx).getBlock();
					
					if(obs.equals(Blocks.SAND) || obs.equals(Blocks.RED_SAND)) {
						bs = Registry.getBlock("corrupt_sand").getDefaultState();
					}else if(obs.equals(Blocks.STONE)) {
						bs = Registry.getBlock("corrupt_stone").getDefaultState();
					}else if(obs.equals(Blocks.DIRT)) {
						bs = Registry.getBlock("corrupt_dirt").getDefaultState();
					}else if(obs.equals(Blocks.GRASS_BLOCK) || obs.equals(Blocks.PODZOL) || obs.equals(Blocks.MYCELIUM)) {
						bs = Registry.getBlock("corrupt_grass").getDefaultState();
					}
					
					if(bs != null) {
						world.setBlockState(posx, bs);
						entropy -= 0.00025f;
						i++;
					}
					
					if(ii++ == 50) {
						entropy -= 0.001f;
						break;
					}
				}
			}
			
			if(entropy > 0.98f) {
				world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 20f, true, Mode.DESTROY);
				entropy = 0f;
			}
			
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

		public ContainerEntropyReactor(final int windowId, final PlayerInventory playerInventory, final TEEntropyReactor tileEntity) {
			super(Registry.getContainerType("entropy_reactor"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 3);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 57, tileEntity));
		}

		public ContainerEntropyReactor(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEEntropyReactor.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenEntropyReactor extends ScreenALMEnergyBased<ContainerEntropyReactor>{
		TEEntropyReactor tsfm;
		
		private DecimalFormat num = new DecimalFormat("##0.#");

		public ScreenEntropyReactor(ContainerEntropyReactor screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "entropy_reactor", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;

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
				status = "Status: Generating...\nRemaining time: " + tsfm.cyclesRemaining + " seconds!\nGenerating " + Formatting.FEPT_FORMAT.format((float)tsfm.genPerCycle / 20f) + " FE/t!";
			}else {
				if(tsfm.shardMap.isEmpty()) {
					color = 0xffffff;
					status = "Status: Idle...\nAwaiting Corrupted Shards.";
				}else {
					color = 0xe0d100;
					status = "Status: Warming up...\nYou can keep loading shards!";
				}
			}
			
			
			if(!tsfm.getStackInSlot(3).isEmpty()) {
				status = status + "\nWaste needs to be removed!";
			}

			int i = 0;
			for(String s : status.split("\n")) {

				float wsc = 73f / (float) this.font.getStringWidth(s);
				if(wsc > 1f) wsc = 1f;

				if(s.equals("Status:")) {
					MathHelper.renderScaledText(this.font, x+66, y+20+(i*9), wsc, s, false, color);
				}else {
					MathHelper.renderScaledText(this.font, x+66, y+20+(i*9), wsc, s, false, 0xffffff);
				}

				i++;
			}



		}


		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if(mouseX >= x + 50 && mouseY >= y + 17 && mouseX <= x + 55 && mouseY <= y + 68) {
				ArrayList<String> str = new ArrayList<>();
				if(tsfm.total != 0) {
					str.add("§5Total Shards Stored");
					str.add("§d" + num.format(tsfm.total) + "/" + num.format(tsfm.capacity));
				}else {
					str.add("§4Shard Tank Empty");
				}
				
				str.add("§7Runs at " + Formatting.GENERAL_FORMAT.format((tsfm.total * 6000f) / 20f) + " FE/t.");

				this.renderTooltip(str, mouseX - x, mouseY - y);
			}

			if(mouseX >= x + 37 && mouseY >= y + 17 && mouseX <= x + 42 && mouseY <= y + 68) {
				ArrayList<String> str = new ArrayList<>();
				
				String cc;
				if(tsfm.varietyRating < 0.2f) {
					
					cc = "4";
					
				}else if(tsfm.varietyRating < 0.5f) {
					
					cc = "6";
				}else if(tsfm.varietyRating < 0.7f) {
					
					cc = "a";
				}else {
					
					cc = "2";
				}
				
				
				str.add("§eVariety Rating: §" + cc + num.format(tsfm.varietyRating * 100f) + "%");
				
				int cycles = Math.round(tsfm.varietyRating * 9f);
				
				if(cycles == 1) {
					str.add("§7Powered for 1 second.");
				}else {
					str.add("§7Powered for " + cycles + " seconds.");
				}
				
				this.renderTooltip(str, mouseX - x, mouseY - y);
				
			}
			
			if(mouseX >= x + 63 && mouseY >= y + 63 && mouseX <= x + 141 && mouseY <= y + 68) {
				ArrayList<String> str = new ArrayList<>();
				
				String cc;
				if(tsfm.entropy < 0.05f) {
					cc = "2";
				}else if(tsfm.entropy < 0.2f) {
					cc = "c";
				}else {
					cc = "4";
				}
				
				str.add("§cCore Entropy Levels: §" + cc + num.format(tsfm.entropy * 100f) + "%");
				
				if(tsfm.entropy > 0.1f) {
					str.add("§7Bad things will happen if this is not lowered.");
					str.add("§7Lower by keeping Variety high!");
				}
				
				this.renderTooltip(str, mouseX - x, mouseY - y);
			}
			
		}
	}

	public static enum EntropyReactorOptions implements IStringSerializable{
		TOP_EDGE(false), BOTTOM_EDGE(false), TOP_CORNER(false), BOTTOM_CORNER(false), SIDE_EDGE(false), WALL(false), ITEM(true), ENERGY(true), SCREEN(true), SCREEN_ACTIVE(true), BLOCK(false);


		private final boolean hasTE;

		EntropyReactorOptions(boolean hasTE){
			this.hasTE = hasTE;
		}


		@Override
		public String getString() {
			return this.toString().toLowerCase();
		}
		



	}

}
