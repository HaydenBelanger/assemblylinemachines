package me.haydenb.assemblylinemachines.block;

import java.text.DecimalFormat;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.item.ToolStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.FluidProperty;
import me.haydenb.assemblylinemachines.util.TileEntityALMBase;
import me.haydenb.assemblylinemachines.util.FluidProperty.Fluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockFluidTank extends Block{
	
	private static final VoxelShape SHAPE = Stream.of(
			Block.makeCuboidShape(0, 0, 0, 16, 1, 16),
			Block.makeCuboidShape(0, 1, 15, 1, 15, 16),
			Block.makeCuboidShape(15, 1, 15, 16, 15, 16),
			Block.makeCuboidShape(0, 1, 0, 1, 15, 1),
			Block.makeCuboidShape(15, 1, 0, 16, 15, 1),
			Block.makeCuboidShape(0, 15, 0, 16, 16, 16),
			Block.makeCuboidShape(1, 1, 0, 15, 15, 1),
			Block.makeCuboidShape(1, 1, 15, 15, 15, 16),
			Block.makeCuboidShape(1, 1, 1, 15, 15, 15),
			Block.makeCuboidShape(0, 1, 1, 1, 15, 15),
			Block.makeCuboidShape(15, 1, 1, 16, 15, 15)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");


	private final int _capacity;
	private final TemperatureResistance _tempres;
	
	public BlockFluidTank(int capacity, TemperatureResistance resist) {
		super(Block.Properties.create(Material.GLASS).notSolid().hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.GLASS));
		_capacity = capacity;
		_tempres = resist;
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FluidProperty.FLUID, Fluids.NONE));
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isVariableOpacity() {
		return true;
	}
	
	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof TEFluidTank) {
				worldIn.removeTileEntity(pos);
			}
		}
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(FluidProperty.FLUID);
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TEFluidTank tef = (TEFluidTank) Registry.getTileEntity("fluid_tank").create();
		tef.capacity = _capacity;
		tef.trs = _tempres;
		return tef;
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		if(!world.isRemote) {
			if(player.getActiveHand() == Hand.MAIN_HAND) {
				if(world.getTileEntity(pos) instanceof TEFluidTank) {
					TEFluidTank entity = (TEFluidTank) world.getTileEntity(pos);
					
					if(player.isSneaking()) {
						FluidStack f = entity.getFluid();
						Fluids ff;
						if(f == null || f.isEmpty()) {
							ff = Fluids.NONE;
							player.sendStatusMessage(new StringTextComponent("This tank is empty."), true);
						}else {
							if(f.getFluid() == Fluids.LAVA.getAssocFluid()) {
								ff = Fluids.LAVA;
							}else {
								ff = Fluids.WATER;
							}
							player.sendStatusMessage(new StringTextComponent(FORMAT.format(f.getAmount()) + "/" + FORMAT.format(entity.getCapacity()) + " mB " + ff.getFriendlyName()), true);
						}
						
						if(state.get(FluidProperty.FLUID) != ff) {
							world.setBlockState(pos, state.with(FluidProperty.FLUID, ff));
						}
					}else {
						ItemStack is = player.getHeldItemMainhand();
						
						Fluids ff = null;
						if(is.getItem() == Items.BUCKET) {
							FluidStack f = entity.drain(1000, FluidAction.SIMULATE);
							if(f.getAmount() < 1000) {
								player.sendStatusMessage(new StringTextComponent("This tank is too empty for this."), true);
							}else {
								is.shrink(1);
								if(f.getFluid() == Fluids.LAVA.getAssocFluid()) {
									ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.LAVA_BUCKET));
									world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1f, 1f);
								}else {
									ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.WATER_BUCKET));
									world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1f, 1f);
								}
								
								entity.drain(1000, FluidAction.EXECUTE);
								entity.sendUpdates();
								
								if(entity.getFluidAmount() == 0) {
									ff = Fluids.NONE;
								}
								
							}
						}else if(is.getItem() == Items.LAVA_BUCKET || is.getItem() == Items.WATER_BUCKET) {
							FluidStack fsi = null;
							if(is.getItem() == Items.LAVA_BUCKET) {
								if(entity.getFluidAmount() == 0) {
									ff = Fluids.LAVA;
								}
								fsi = new FluidStack(Fluids.LAVA.getAssocFluid(), 1000);
							}else if(is.getItem() == Items.WATER_BUCKET){
								if(entity.getFluidAmount() == 0) {
									ff = Fluids.WATER;
								}
								fsi = new FluidStack(Fluids.WATER.getAssocFluid(), 1000);
							}
							if(fsi.getFluid() == Fluids.LAVA.getAssocFluid() && _tempres != TemperatureResistance.HOT) {
								player.sendStatusMessage(new StringTextComponent("This fluid is too hot for this tank."), true);
								ff = null;
							}else if(!entity.isEmpty() && fsi.getFluid() != entity.getFluidType()) {
								player.sendStatusMessage(new StringTextComponent("This tank doesn't have this fluid in it."), true);
								ff = null;
							}else {
								if(fsi != null) {
									if(entity.fill(fsi, FluidAction.SIMULATE) < 1000){
										player.sendStatusMessage(new StringTextComponent("This tank is too full for this."), true);
										ff = null;
									}else {
										ItemHandlerHelper.giveItemToPlayer(player, is.getContainerItem());
										is.shrink(1);
										world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1f, 1f);
										entity.fill(fsi, FluidAction.EXECUTE);
										entity.sendUpdates();
									}
									
								}
							}
							
							
							
							
							
							
						}
						
						if(ff != null) {
							world.setBlockState(pos, state.with(FluidProperty.FLUID, ff));
						}
					}
				}
			}
		}
		return ActionResultType.CONSUME;

	}

	public static class TEFluidTank extends TileEntityALMBase implements IFluidTank{


		private FluidStack fluid = null;
		private int capacity = 0;
		private TemperatureResistance trs = TemperatureResistance.COLD;
		
		public TEFluidTank(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEFluidTank() {
			this(Registry.getTileEntity("fluid_tank"));
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:capacity")) {
				capacity = compound.getInt("assemblylinemachines:capacity");
			}
			
			if(compound.contains("assemblylinemachines:fluidstack")) {
				fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluidstack"));
			}
			
			if(compound.contains("assemblylinemachines:temperatureresistance")) {
				trs = TemperatureResistance.valueOf(compound.getString("assemblylinemachines:temperatureresistance"));
			}
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			
			compound.putInt("assemblylinemachines:capacity", capacity);
			compound.putString("assemblylinemachines:temperatureresistance", trs.toString());
			if(fluid != null) {
				CompoundNBT sub = new CompoundNBT();
				fluid.writeToNBT(sub);
				compound.put("assemblylinemachines:fluidstack", sub);
			}
			return compound;
		}
		
		public Fluid getFluidType() {
			return getFluid().getFluid();
		}
		
		public boolean isEmpty() {
			return fluid.isEmpty();
		}

		@Override
		public FluidStack getFluid() {
			if(fluid == null) {
				fluid = FluidStack.EMPTY;
			}
			return fluid;
		}

		@Override
		public int getFluidAmount() {
			return getFluid().getAmount();
		}

		@Override
		public int getCapacity() {
			return capacity;
		}

		@Override
		public boolean isFluidValid(FluidStack stack) {
			if(stack.getFluid() == Fluids.LAVA.getAssocFluid() || stack.getFluid() == Fluids.WATER.getAssocFluid()) {
				return true;
			}
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if(!getFluid().isEmpty()) {
				if(resource.getFluid() != getFluidType()) {
					return 0;
				}
			}
			if(resource.getFluid() == Fluids.LAVA.getAssocFluid() && trs == TemperatureResistance.COLD) {
				return 0;
			}
			int attemptedInsert = resource.getAmount();
			int rmCapacity = getCapacity() - getFluidAmount();
			if(rmCapacity < attemptedInsert) {
				attemptedInsert = rmCapacity;
			}
			
			if(action != FluidAction.SIMULATE) {
				if(getFluid().isEmpty()) {
					fluid = resource;
				}else {
					getFluid().setAmount(getFluidAmount() + attemptedInsert);
				}
				Fluids ff = Fluids.getAssocFluids(fluid.getFluid());
				if(attemptedInsert != 0 && this.getBlockState().get(FluidProperty.FLUID) != ff) {
					world.setBlockState(this.pos, getBlockState().with(FluidProperty.FLUID, ff));
					sendUpdates();
				}
			}
			return attemptedInsert;
			
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			
			if(getFluidAmount() < maxDrain) {
				maxDrain = getFluidAmount();
			}
			
			if(action != FluidAction.SIMULATE) {
				getFluid().setAmount(getFluidAmount() - maxDrain);
			}
			
			if(getFluidAmount() <= 0) {
				fluid = FluidStack.EMPTY;
				
				if(getBlockState().get(FluidProperty.FLUID) != Fluids.NONE) {
					world.setBlockState(pos, getBlockState().with(FluidProperty.FLUID, Fluids.NONE));
				}
			}
			
			return new FluidStack(getFluidType(), maxDrain);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return drain(resource.getAmount(), action);
		}


	}

}
