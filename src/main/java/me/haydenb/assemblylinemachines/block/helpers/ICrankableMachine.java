package me.haydenb.assemblylinemachines.block.helpers;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

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
