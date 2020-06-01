package me.haydenb.assemblylinemachines.block;

import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.misc.FluidProperty;
import me.haydenb.assemblylinemachines.misc.FluidProperty.Fluids;
import me.haydenb.assemblylinemachines.misc.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.misc.ICrankableMachine;
import me.haydenb.assemblylinemachines.misc.TileEntityALMMachine;
import me.haydenb.assemblylinemachines.misc.TileEntityALMMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.misc.TileEntityALMMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.misc.Utils;
import me.haydenb.assemblylinemachines.misc.Utils.Pair;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleFluidMixer extends Block implements ICrankableBlock{
	

	public BlockSimpleFluidMixer() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		this.setDefaultState(this.stateContainer.getBaseState().with(FluidProperty.FLUID, Fluids.NONE).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(FluidProperty.FLUID).add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if(state.getBlock() == this) {
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.getTileEntity("simple_fluid_mixer").create();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		if(!world.isRemote) {
			if(world.getTileEntity(pos) instanceof TESimpleFluidMixer) {
				NetworkHooks.openGui((ServerPlayerEntity) player, (TESimpleFluidMixer) world.getTileEntity(pos), buf -> buf.writeBlockPos(pos));
			}
		}
		return ActionResultType.CONSUME;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof TESimpleFluidMixer) {
				TESimpleFluidMixer tefm = (TESimpleFluidMixer) worldIn.getTileEntity(pos);
				InventoryHelper.dropItems(worldIn, pos, tefm.getItems());
				worldIn.removeTileEntity(pos);
			}
		}
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if(facing == Direction.DOWN) {
			TileEntity te = worldIn.getTileEntity(currentPos);
			if(te != null && te instanceof TESimpleFluidMixer) {
				TESimpleFluidMixer tsfm = (TESimpleFluidMixer) te;
				tsfm.tankUpdated();
			}
		}
		
		return stateIn;
	}
	
	public static class TESimpleFluidMixer extends TileEntityALMMachine<ContainerSimpleFluidMixer> implements ITickableTileEntity, ICrankableMachine{
		
		public IFluidTank tank;
		public int timer;
		public ItemStack output = null;
		public boolean check;
		public float progress = 0;
		public float cycles = 0;
		public ItemStack isa = null;
		public ItemStack isb = null;
		public Fluids f = null;
		public boolean pendingOutput = false;
		public int cranks;
		
		public TESimpleFluidMixer(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 2, "Simple Fluid Mixer", Registry.getContainerId("simple_fluid_mixer"), ContainerSimpleFluidMixer.class);
		}
		
		public TESimpleFluidMixer() {
			this(Registry.getTileEntity("simple_fluid_mixer"));
		}

		@Override
		public boolean perform() {
			if(output == null || pendingOutput == true) {
				return false;
			}
			cranks++;
			return true;
			
		}
		
		@Override
		public void tick() {
			if(timer++ == ConfigHolder.COMMON.ticksPerOperationSimple.get()) {
				if(!world.isRemote) {
					timer = 0;
					
					if(output != null) {
						boolean sendupdate = false;
						boolean end = false;
						if(pendingOutput) {
							Direction opdr = getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).rotateYCCW();
							TileEntity te = world.getTileEntity(pos.offset(opdr));
							if(te != null) {
								LazyOptional<IItemHandler> h = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opdr.getOpposite());
								IItemHandler handler = h.orElse(null);
								if(handler != null) {
									for(int i = 0; i < handler.getSlots(); i++) {
										if(handler.insertItem(i, output, true) == ItemStack.EMPTY) {
											handler.insertItem(i, output, false);
											output = null;
											progress = 0;
											cycles = 0;
											f = null;
											pendingOutput = false;
											sendupdate = true;
											end = true;
											world.setBlockState(pos, getBlockState().with(FluidProperty.FLUID, Fluids.NONE));
											break;
										}
									}
								}
							}
							
						}else {
							
							if(cranks >= ConfigHolder.COMMON.simpleFluidMixerCranks.get()) {
								cranks = 0;
								if(progress == cycles) {
									pendingOutput = true;
								}
								progress++;
								sendupdate = true;
							}
							
						}
						
						if(getBlockState().get(FluidProperty.FLUID) != f && end == false) {
							world.setBlockState(pos, getBlockState().with(FluidProperty.FLUID, f));
							sendupdate = true;
						}
						
						if(sendupdate) {
							sendUpdates();
						}
						
					}else {
						boolean pass = true;
						if(tank == null) {
							
							TileEntity te = world.getTileEntity(pos.down());
							if(te != null && te instanceof IFluidTank) {
								tank = (IFluidTank) te;
							}else {
								pass = false;
							}
						}
						
						if(pass) {
							if(contents.get(0) != isa || contents.get(1) != isb) {
								
								check = true;
							}
							if(check == true) {
								isa = contents.get(0);
								isb = contents.get(1);
								check = false;
								BathCrafting crafting = world.getRecipeManager().getRecipe(BathCrafting.BATH_RECIPE, this, world).orElse(null);
								if(crafting != null) {
									if(crafting.getFluid().getAssocFluid() == tank.getFluid().getFluid() && tank.drain(1000, FluidAction.SIMULATE).getAmount() == 1000) {
										tank.drain(1000, FluidAction.EXECUTE);
										isa.shrink(1);
										isb.shrink(1);
										isa = null;
										isb = null;
										output = crafting.getRecipeOutput().copy();
										cycles = crafting.getStirs() * ConfigHolder.COMMON.simpleFluidMixerStirs.get();
										f = crafting.getFluid();
										pendingOutput = false;
										sendUpdates();
										
									}else {
										check = true;
									}
								}
							}
						}
					}
				}
				
			}
			
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putBoolean("assemblylinemachines:pendingoutput", pendingOutput);
			if(f != null) {
				compound.putString("assemblylinemachines:fluid", f.toString());
			}
			if(output != null) {
				CompoundNBT sub = new CompoundNBT();
				output.write(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			return super.write(compound);
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if(compound.contains("assemblylinemachines:cycles")) {
				cycles = compound.getFloat("assemblylinemachines:cycles");
			}
			if(compound.contains("assemblylinemachines:progress")) {
				progress = compound.getFloat("assemblylinemachines:progress");
			}
			if(compound.contains("assemblylinemachines:pendingoutput")) {
				pendingOutput = compound.getBoolean("assemblylinemachines:pendingoutput");
			}
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
			}
			if(compound.contains("assemblylinemachines:fluid")) {
				f = Fluids.valueOf(compound.getString("assemblylinemachines:fluid"));
			}
		}
		
		public void tankUpdated() {
			
			TileEntity tankTe = world.getTileEntity(pos.down());
			if(tankTe == null || !(tankTe instanceof IFluidTank)) {
				tank = null;
				
			}
		}

	}
	
	public static class ContainerSimpleFluidMixer extends ContainerALMBase<TESimpleFluidMixer>{
		
		private static final Pair<Integer, Integer> INPUT_A_POS = new Pair<>(63, 48);
		private static final Pair<Integer, Integer> INPUT_B_POS = new Pair<>(88, 48);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerSimpleFluidMixer(final int windowId, final PlayerInventory playerInventory, final TESimpleFluidMixer tileEntity) {
			super(Registry.getContainerType("simple_fluid_mixer"), windowId, 2, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			this.addSlot(new Slot(this.tileEntity, 1, INPUT_B_POS.x, INPUT_B_POS.y));
			this.addSlot(new Slot(this.tileEntity, 0, INPUT_A_POS.x, INPUT_A_POS.y));
		}
		
		public ContainerSimpleFluidMixer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, Utils.getTileEntity(playerInventory, data, TESimpleFluidMixer.class));
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenSimpleFluidMixer extends ScreenALMBase<ContainerSimpleFluidMixer>{
		
		TESimpleFluidMixer tsfm;
		
		public ScreenSimpleFluidMixer(ContainerSimpleFluidMixer screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "simple_fluid_mixer", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if(tsfm.f != null && tsfm.cycles != 0) {
				int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
				if(tsfm.f == Fluids.LAVA) {
					super.blit(x+71, y+19 + (24 - prog), 200, (24 - prog), 24, prog);
				}else if(tsfm.f == Fluids.WATER) {
					super.blit(x+71, y+19 + (24 - prog), 176, (24 - prog), 24, prog);
				}
			}
		}
	}

}
