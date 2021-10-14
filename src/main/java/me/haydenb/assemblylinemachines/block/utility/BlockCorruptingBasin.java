package me.haydenb.assemblylinemachines.block.utility;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.item.items.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import me.haydenb.assemblylinemachines.util.StateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BlockCorruptingBasin extends BlockScreenBlockEntity<BlockCorruptingBasin.TECorruptingBasin>{

	
	private static final VoxelShape SHAPE = Stream.of(Block.box(1, 0, 1, 15, 16, 2),
			Block.box(1, 0, 14, 15, 16, 15), Block.box(1, 0, 2, 2, 16, 14),
			Block.box(14, 0, 2, 15, 16, 14), Block.box(2, 0, 2, 14, 1, 14)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();
	
	public BlockCorruptingBasin() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "corrupting_basin", BlockCorruptingBasin.TECorruptingBasin.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	
	public static class TECorruptingBasin extends SimpleMachine<ContainerCorruptingBasin> implements ALMTicker<TECorruptingBasin>{
		
		public IFluidHandler handler;
		public int timer;
		public int reqCounts;
		public int counts;
		
		
		public TECorruptingBasin(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 1, new TranslatableComponent(Registry.getBlock("corrupting_basin").getDescriptionId()), Registry.getContainerId("corrupting_basin"), ContainerCorruptingBasin.class, true, pos, state);
		}
		
		public TECorruptingBasin(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("corrupting_basin"), pos, state);
		}
		
		@Override
		public void tick() {
			if(timer++ == 20) {
				timer = 0;
				if(!level.isClientSide) {
					boolean sendUpdates = false;
					if(handler == null) {
						handler = General.getCapabilityFromDirection(this, "handler", Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
					}
					
					if(handler != null) {
						
						FluidStack stackInTank = handler.getFluidInTank(0);
						if(stackInTank.getFluid() == Registry.getFluid("condensed_void") && !getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
							sendUpdates = true;
						}
						
						if(stackInTank.getFluid() != Registry.getFluid("condensed_void") && getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
							sendUpdates = true;
						}
						
						if(getItem(0).getItem() != Registry.getItem("corrupted_shard")) {
							if(getItem(0).getCount() != reqCounts) {
								reqCounts = getItem(0).getCount();
								sendUpdates = true;
							}
							
							if(counts != 0 && reqCounts == 0) {
								counts = 0;
								sendUpdates = true;
							}
							
							if(reqCounts != 0) {
								if(counts >= reqCounts) {
									this.setItem(0, ItemCorruptedShard.corruptItem(getItem(0)));
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
						
						
					}else if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)){
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
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
		public CompoundTag save(CompoundTag compound) {
			compound.putInt("assemblylinemachines:reqcounts", reqCounts);
			compound.putInt("assemblylinemachines:counts", counts);
			return super.save(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
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
		
		public ContainerCorruptingBasin(final int windowId, final Inventory playerInventory, final TECorruptingBasin tileEntity) {
			super(Registry.getContainerType("corrupting_basin"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
			this.addSlot(new Slot(this.tileEntity, 0, 80, 26));
		}
		
		public ContainerCorruptingBasin(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TECorruptingBasin.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenCorruptingBasin extends ScreenALMBase<ContainerCorruptingBasin>{
		
		TECorruptingBasin tsfm;
		
		private int cxc;
		private int cxcr;
		
		public ScreenCorruptingBasin(ContainerCorruptingBasin screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "corrupting_basin", false);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			if(tsfm.reqCounts != 0) {
				int x = (this.width - this.imageWidth) / 2;
				int y = (this.height - this.imageHeight) / 2;
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
