package me.haydenb.assemblylinemachines.block.helpers;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ManagedSidedMachine<A extends AbstractContainerMenu> extends AbstractSidedMachine<A> {

	protected HashMap<Direction, Boolean> enabledSides = new HashMap<>();



	public ManagedSidedMachine(BlockEntityType<?> tileEntityTypeIn, int slotCount, TranslatableComponent name,
			int containerId, Class<A> clazz, EnergyProperties properties, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, properties, pos, state);
	}

	@Override
	public boolean canExtractFromSide(boolean isEnergy, int slot, Direction direction) {
		if(enabledSides.getOrDefault(direction, true) && slot == 0) {
			return true;
		}
		return false;
	}



	@Override
	public boolean canInsertToSide(boolean isEnergy, int slot, Direction direction) {
		if(enabledSides.getOrDefault(direction, true) && slot != 0) {
			return true;
		}
		return false;
	}



	@Override
	public boolean isAllowedInSlot(int slot, ItemStack stack) {
		return slot != 0;
	}

	public Direction getDirection(ManagedDirection mdir) {

		Direction dir = getBlockState().getValue(HorizontalDirectionalBlock.FACING);

		if(dir == null) {
			return null;
		}

		return mdir.getDirection(dir);
	}

	public boolean getDirectionEnabled(ManagedDirection mdir) {
		return enabledSides.getOrDefault(getDirection(mdir), true);
	}

	public void setDirection(ManagedDirection mdir, boolean b) {
		Direction dir = getDirection(mdir);

		if(dir != null) {
			enabledSides.put(dir, b);
		}
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		enabledSides.put(Direction.UP, compound.getBoolean("assemblylinemachines:up"));
		enabledSides.put(Direction.DOWN, compound.getBoolean("assemblylinemachines:down"));
		enabledSides.put(Direction.NORTH, compound.getBoolean("assemblylinemachines:north"));
		enabledSides.put(Direction.SOUTH, compound.getBoolean("assemblylinemachines:south"));
		enabledSides.put(Direction.EAST, compound.getBoolean("assemblylinemachines:east"));
		enabledSides.put(Direction.WEST, compound.getBoolean("assemblylinemachines:west"));
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		compound.putBoolean("assemblylinemachines:up", enabledSides.getOrDefault(Direction.UP, true));
		compound.putBoolean("assemblylinemachines:down", enabledSides.getOrDefault(Direction.DOWN, true));
		compound.putBoolean("assemblylinemachines:north", enabledSides.getOrDefault(Direction.NORTH, true));
		compound.putBoolean("assemblylinemachines:south", enabledSides.getOrDefault(Direction.SOUTH, true));
		compound.putBoolean("assemblylinemachines:east", enabledSides.getOrDefault(Direction.EAST, true));
		compound.putBoolean("assemblylinemachines:west", enabledSides.getOrDefault(Direction.WEST, true));
		super.saveAdditional(compound);
	}


	public static enum ManagedDirection{
		FRONT(null), LEFT(null), RIGHT(null), BACK(null), TOP(Direction.UP), BOTTOM(Direction.DOWN);

		private Direction rel;

		ManagedDirection(Direction rel){
			this.rel = rel;
		}
		
		public Direction getDirection(Direction facing) {
			switch(this){
			case FRONT:
				return facing;
			case BACK:
				return facing.getOpposite();
			case LEFT:
				return facing.getCounterClockWise();
			case RIGHT:
				return facing.getClockWise();
			default:
				return rel;
			}
		}
	}



}
