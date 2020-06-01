package me.haydenb.assemblylinemachines.block;

import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.misc.ICrankableMachine;
import me.haydenb.assemblylinemachines.misc.ICrankableMachine.ICrankableBlock;
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
import net.minecraft.inventory.IInventory;
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
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleGrinder extends Block implements ICrankableBlock{
	

	public BlockSimpleGrinder() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		this.setDefaultState(this.stateContainer.getBaseState().with(Utils.MACHINE_ACTIVE, false).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(Utils.MACHINE_ACTIVE).add(HorizontalBlock.HORIZONTAL_FACING);
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
		return Registry.getTileEntity("simple_grinder").create();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {

		if(!world.isRemote) {
			if(world.getTileEntity(pos) instanceof TESimpleGrinder) {
				NetworkHooks.openGui((ServerPlayerEntity) player, (TESimpleGrinder) world.getTileEntity(pos), buf -> buf.writeBlockPos(pos));
			}
		}
		return ActionResultType.CONSUME;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof TESimpleGrinder) {
				TESimpleGrinder tefm = (TESimpleGrinder) worldIn.getTileEntity(pos);
				InventoryHelper.dropItems(worldIn, pos, tefm.getItems());
				worldIn.removeTileEntity(pos);
			}
		}
	}
	
	public static class TESimpleGrinder extends TileEntityALMMachine<ContainerSimpleGrinder> implements ITickableTileEntity, ICrankableMachine{
		
		public int timer;
		public int cranks;
		public boolean pendingOutput = false;
		public ItemStack output;
		public boolean check;
		public float progress = 0;
		public float cycles = 0;
		public ItemStack isa = null;
		public ItemStack isb = null;
		
		
		public TESimpleGrinder(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 2, "Simple Grinder", Registry.getContainerId("simple_grinder"), ContainerSimpleGrinder.class);
		}
		
		public TESimpleGrinder() {
			this(Registry.getTileEntity("simple_grinder"));
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
											pendingOutput = false;
											sendupdate = true;
											end = true;
											world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, false));
											break;
										}
									}
								}
							}
						}else {
							if(cranks >= ConfigHolder.COMMON.simpleGrinderCranks.get()) {
								cranks = 0;
								if(progress == cycles) {
									pendingOutput = true;
									sendupdate = true;
								}
								ItemStack isa = contents.get(0);
								if(isa.isDamageable() && isa.getItem() instanceof ItemGrindingBlade) {
									isa.setDamage(isa.getDamage() + 1);
									progress++;
									sendupdate = true;
								}
							}
						}
						
						if(end == false && getBlockState().get(Utils.MACHINE_ACTIVE) == false) {
							world.setBlockState(pos, getBlockState().with(Utils.MACHINE_ACTIVE, true));
							sendupdate = true;
						}
						if(sendupdate) {
							sendUpdates();
						}
					}else {
						if(contents.get(0) != isa || contents.get(1) != isb) {
							check = true;
						}
						if(check == true) {
							isa = contents.get(0);
							isb = contents.get(1);
							if(isa.getItem() instanceof ItemGrindingBlade) {
								ItemGrindingBlade igb = (ItemGrindingBlade) isa.getItem();
								GrinderCrafting crafting = world.getRecipeManager().getRecipe(GrinderCrafting.GRINDER_RECIPE, this, world).orElse(null);
								if(crafting != null) {
									if(igb.blade.tier >= crafting.getBlade().tier) {
										isb.shrink(1);
										isb = null;
										isa = null;
										output = crafting.getRecipeOutput().copy();
										cycles = crafting.getGrinds() * ConfigHolder.COMMON.simpleGrinderGrinds.get();
										progress = 0;
										pendingOutput = false;
										sendUpdates();
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

		}
		
		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			if(slot == 0) {
				if(stack.getItem() instanceof ItemGrindingBlade) {
					return true;
				}
				return false;
			}
			return true;
		}
	}
	
	public static class ContainerSimpleGrinder extends ContainerALMBase<TESimpleGrinder>{
		
		private static final Pair<Integer, Integer> INPUT_A_POS = new Pair<>(53, 26);
		private static final Pair<Integer, Integer> INPUT_B_POS = new Pair<>(75, 48);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerSimpleGrinder(final int windowId, final PlayerInventory playerInventory, final TESimpleGrinder tileEntity) {
			super(Registry.getContainerType("simple_grinder"), windowId, 2, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			this.addSlot(new Slot(this.tileEntity, 1, INPUT_B_POS.x, INPUT_B_POS.y));
			this.addSlot(new BladeSlot(this.tileEntity, 0, INPUT_A_POS.x, INPUT_A_POS.y));
		}
		
		
		public ContainerSimpleGrinder(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, Utils.getTileEntity(playerInventory, data, TESimpleGrinder.class));
		}
		
		
		
	}
	
	public static class BladeSlot extends Slot{

		public BladeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		public boolean isItemValid(ItemStack stack) {
			if(stack.getItem() instanceof ItemGrindingBlade) {
				return true;
			}
			return false;
		}
		
		@Override
		public int getSlotStackLimit() {
			return 1;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenSimpleGrinder extends ScreenALMBase<ContainerSimpleGrinder>{
		
		TESimpleGrinder tsfm;
		
		public ScreenSimpleGrinder(ContainerSimpleGrinder screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "simple_grinder", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
			super.blit(x+73, y+19 + (24 - prog), 176, (24 - prog), 20, prog);
		}
	}

}
