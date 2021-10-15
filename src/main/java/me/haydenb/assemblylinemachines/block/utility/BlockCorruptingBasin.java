package me.haydenb.assemblylinemachines.block.utility;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.item.items.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockCorruptingBasin extends BlockScreenTileEntity<BlockCorruptingBasin.TECorruptingBasin>{

	
	private static final VoxelShape SHAPE = Stream.of(Block.makeCuboidShape(1, 0, 1, 15, 16, 2),
			Block.makeCuboidShape(1, 0, 14, 15, 16, 15), Block.makeCuboidShape(1, 0, 2, 2, 16, 14),
			Block.makeCuboidShape(14, 0, 2, 15, 16, 14), Block.makeCuboidShape(2, 0, 2, 14, 1, 14)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();
	
	public BlockCorruptingBasin() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "corrupting_basin", BlockCorruptingBasin.TECorruptingBasin.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.MACHINE_ACTIVE, false));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
	
	
	public static class TECorruptingBasin extends SimpleMachine<ContainerCorruptingBasin> implements ITickableTileEntity{
		
		public IFluidHandler handler;
		public int timer;
		public int reqCounts;
		public int counts;
		
		
		public TECorruptingBasin(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 1, new TranslationTextComponent(Registry.getBlock("corrupting_basin").getTranslationKey()), Registry.getContainerId("corrupting_basin"), ContainerCorruptingBasin.class, true);
		}
		
		public TECorruptingBasin() {
			this(Registry.getTileEntity("corrupting_basin"));
		}
		
		@Override
		public void tick() {
			if(timer++ == 20) {
				timer = 0;
				if(!world.isRemote) {
					boolean sendUpdates = false;
					if(handler == null) {
						handler = General.getCapabilityFromDirection(this, "handler", Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
					}
					
					if(handler != null) {
						
						FluidStack stackInTank = handler.getFluidInTank(0);
						if(stackInTank.getFluid() == Registry.getFluid("condensed_void") && !getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
							sendUpdates = true;
						}
						
						if(stackInTank.getFluid() != Registry.getFluid("condensed_void") && getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
							sendUpdates = true;
						}
						
						if(getStackInSlot(0).getItem() != Registry.getItem("corrupted_shard")) {
							if(getStackInSlot(0).getCount() != reqCounts) {
								reqCounts = getStackInSlot(0).getCount();
								sendUpdates = true;
							}
							
							if(counts != 0 && reqCounts == 0) {
								counts = 0;
								sendUpdates = true;
							}
							
							if(reqCounts != 0) {
								if(counts >= reqCounts) {
									setInventorySlotContents(0, ItemCorruptedShard.corruptItem(getStackInSlot(0)));
									counts = 0;
									reqCounts = 0;
									sendUpdates = true;
								}else {
									FluidStack drain = handler.drain(10, FluidAction.SIMULATE);
									if(drain.getFluid() == Registry.getFluid("condensed_void") && drain.getAmount() == 10) {
										counts++;
										handler.drain(10, FluidAction.EXECUTE);
										sendUpdates = true;
									}
								}
							}
						}
						
						
					}else if(getBlockState().get(StateProperties.MACHINE_ACTIVE)){
						world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
						sendUpdates = true;
					}
					
					if(sendUpdates) {
						sendUpdates();
					}
				}
			}
			
		}
		
		@Override
		public boolean canBeExtracted(ItemStack stack) {
			return stack.getItem() == Registry.getItem("corrupted_shard");
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putInt("assemblylinemachines:reqcounts", reqCounts);
			compound.putInt("assemblylinemachines:counts", counts);
			return super.write(compound);
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			counts = compound.getInt("assemblylinemachines:counts");
			reqCounts = compound.getInt("assemblylinemachines:reqcounts");
			
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
	}
	
	public static class ContainerCorruptingBasin extends ContainerALMBase<TECorruptingBasin>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerCorruptingBasin(final int windowId, final PlayerInventory playerInventory, final TECorruptingBasin tileEntity) {
			super(Registry.getContainerType("corrupting_basin"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
			this.addSlot(new Slot(this.tileEntity, 0, 80, 26));
		}
		
		public ContainerCorruptingBasin(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TECorruptingBasin.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenCorruptingBasin extends ScreenALMBase<ContainerCorruptingBasin>{
		
		TECorruptingBasin tsfm;
		
		private int cxc;
		private int cxcr;
		
		public ScreenCorruptingBasin(ContainerCorruptingBasin screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "corrupting_basin", false);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			if(tsfm.reqCounts != 0) {
				int x = (this.width - this.xSize) / 2;
				int y = (this.height - this.ySize) / 2;
				if(cxc++ == 20) {
					cxc = 0;
					if(cxcr++ == 4) {
						cxcr = 0;
					}
				}
				
				super.blit(x+75, y+23, 176, 52 + (26 * cxcr), 26, 26);
			}
			
		}
	}
}
