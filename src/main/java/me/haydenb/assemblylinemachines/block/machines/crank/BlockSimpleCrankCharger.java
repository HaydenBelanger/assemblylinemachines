package me.haydenb.assemblylinemachines.block.machines.crank;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleCrankCharger extends BlockTileEntity implements ICrankableBlock{

	public BlockSimpleCrankCharger() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "simple_crank_charger");
	}
	
	
	
	
	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.PASS;
	}


	@Override
	public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.PASS;
	}
	
	@Override
	public BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState) {
		return bteDefaultReturnBlockEntity(pPos, pState);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return bteDefaultReturnTicker(level, state, blockEntityType);
	}
	
	public static class TESimpleCrankCharger extends BasicTileEntity implements ICrankableMachine{

		
		private IItemHandler handler = null;
		public TESimpleCrankCharger(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}

		public TESimpleCrankCharger(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("simple_crank_charger"), pos, state);
		}
		
		@Override
		public boolean perform() {
			if(handler == null && getCapability() == false) {
				return false;
			}
			
			for(int i = 0; i < handler.getSlots(); i++) {
				ItemStack stack = handler.getStackInSlot(i);
				if(stack != ItemStack.EMPTY && stack.getItem() instanceof ICrankableItem) {
					ICrankableItem cr = (ICrankableItem) stack.getItem();
					boolean changed = false;
					CompoundTag compound = stack.getTag();
					if(compound == null) {
						compound = new CompoundTag();
					}
					
					if(!compound.contains("assemblylinemachines:cranks")) {
						compound.putInt("assemblylinemachines:cranks", 30);
						compound.putBoolean("assemblylinemachines:canbreakblackgranite", true);
						changed = true;
					}else {
						int prevCranks = compound.getInt("assemblylinemachines:cranks");
						if(prevCranks != cr.getMaxCranks()) {
							if((prevCranks + 30) > cr.getMaxCranks()) {
								compound.putInt("assemblylinemachines:cranks", cr.getMaxCranks());
								changed = true;
							}else{
								compound.putInt("assemblylinemachines:cranks", prevCranks + 30);
								changed = true;
							}
						}
						
						
					}
					
					if(changed) {
						stack.setTag(compound);
						return true;
					}
					
				}
			}
			
			return false;
		}
		
		private boolean getCapability() {
			BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(Direction.UP));
			if(te != null) {
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
						Direction.DOWN);
				IItemHandler output = cap.orElse(null);
				if (output != null) {
					TESimpleCrankCharger ipcte = this;
					cap.addListener(new NonNullConsumer<LazyOptional<IItemHandler>>() {

						@Override
						public void accept(LazyOptional<IItemHandler> t) {
							if (ipcte != null) {
								ipcte.handler = null;
							}
						}
					});

					this.handler = output;
					return true;
				}
			}
			
			return false;
		}
		
		
		
	}

	@Override
	public boolean validSide(BlockState state, Direction dir) {
		return true;
	}


	@Override
	public boolean needsGearbox() {
		return true;
	}

}
