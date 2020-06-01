package me.haydenb.assemblylinemachines.block.pipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IPipeConnector{

	public void updateTargets(PipeBase<?> pb);
	
	public int getPriority();
	
	public ItemStack attemptAcceptItem(ItemStack stack);
	
	public FluidStack attemptAcceptLiquid(FluidStack stack);
	
	public int attemptAcceptPower(int power);
}
