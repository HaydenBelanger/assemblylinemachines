package me.haydenb.assemblylinemachines.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface ICrankableMachine {

	public boolean perform();
	
	public interface ICrankableBlock{
		
		public boolean validSide(BlockState state, Direction dir);
		
		public boolean needsGearbox();
	}
	
	public interface ICrankableItem{
		public int getMaxCranks();
	}
	
}
