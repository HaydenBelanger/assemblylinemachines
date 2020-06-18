package me.haydenb.assemblylinemachines.block.machines.crank;

import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleCrankCharger extends BlockTileEntity implements ICrankableBlock{

	public BlockSimpleCrankCharger() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "simple_crank_charger");
	}
	
	
	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.PASS;
	}


	@Override
	public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.PASS;
	}
	
	public static class TESimpleCrankCharger extends BasicTileEntity implements ICrankableMachine{

		
		private IItemHandler handler = null;
		public TESimpleCrankCharger(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TESimpleCrankCharger() {
			this(Registry.getTileEntity("simple_crank_charger"));
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
					CompoundNBT compound = stack.getTag();
					if(compound == null) {
						compound = new CompoundNBT();
					}
					
					if(!compound.contains("assemblylinemachines:cranks")) {
						compound.putInt("assemblylinemachines:cranks", 30);
						compound.putBoolean("assemblylinemachines:hascranks", true);
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
			TileEntity te = world.getTileEntity(pos.offset(Direction.UP));
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
