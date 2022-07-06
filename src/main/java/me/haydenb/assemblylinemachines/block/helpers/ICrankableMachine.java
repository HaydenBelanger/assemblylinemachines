package me.haydenb.assemblylinemachines.block.helpers;

import net.minecraft.core.Direction;

public interface ICrankableMachine {

	public boolean perform();

	public boolean validFrom(Direction dir);

	public boolean requiresGearbox();
}
