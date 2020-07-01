package me.haydenb.assemblylinemachines.helpers;

import java.util.HashMap;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;

public class ManagedSidedMachine<A extends Container> extends AbstractSidedMachine<A> {

	protected HashMap<Direction, Boolean> enabledSides = new HashMap<>();



	public ManagedSidedMachine(TileEntityType<?> tileEntityTypeIn, int slotCount, TranslationTextComponent name,
			int containerId, Class<A> clazz, EnergyProperties properties) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz, properties);
	}

	@Override
	public boolean canExtractFromSide(int slot, Direction direction) {
		if(enabledSides.getOrDefault(direction, true) && slot == 0) {
			return true;
		}
		return false;
	}



	@Override
	public boolean canInsertToSide(int slot, Direction direction) {
		if(enabledSides.getOrDefault(direction, true) && slot != 0) {
			return true;
		}
		return false;
	}



	@Override
	public boolean isAllowedInSlot(int slot, ItemStack stack) {
		if(slot != 0) {
			return true;
		}
		return false;
	}

	public Direction getDirection(ManagedDirection mdir) {

		Direction dir = getBlockState().get(HorizontalBlock.HORIZONTAL_FACING);

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
	public void read(CompoundNBT compound) {
		super.read(compound);

		enabledSides.put(Direction.UP, compound.getBoolean("assemblylinemachines:up"));
		enabledSides.put(Direction.DOWN, compound.getBoolean("assemblylinemachines:down"));
		enabledSides.put(Direction.NORTH, compound.getBoolean("assemblylinemachines:north"));
		enabledSides.put(Direction.SOUTH, compound.getBoolean("assemblylinemachines:south"));
		enabledSides.put(Direction.EAST, compound.getBoolean("assemblylinemachines:east"));
		enabledSides.put(Direction.WEST, compound.getBoolean("assemblylinemachines:west"));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("assemblylinemachines:up", enabledSides.getOrDefault(Direction.UP, true));
		compound.putBoolean("assemblylinemachines:down", enabledSides.getOrDefault(Direction.DOWN, true));
		compound.putBoolean("assemblylinemachines:north", enabledSides.getOrDefault(Direction.NORTH, true));
		compound.putBoolean("assemblylinemachines:south", enabledSides.getOrDefault(Direction.SOUTH, true));
		compound.putBoolean("assemblylinemachines:east", enabledSides.getOrDefault(Direction.EAST, true));
		compound.putBoolean("assemblylinemachines:west", enabledSides.getOrDefault(Direction.WEST, true));
		return super.write(compound);
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
				return facing.rotateYCCW();
			case RIGHT:
				return facing.rotateY();
			default:
				return rel;
			}
		}
	}



}
