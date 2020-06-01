package me.haydenb.assemblylinemachines.block.pipe;

import me.haydenb.assemblylinemachines.misc.TileEntityALMBase;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;

public class FluidPipeConnectorTileEntity extends TileEntityALMBase implements IPipeConnector{

	public FluidPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		
	}
	
	public FluidPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_fluid"));
	}

	@Override
	public void updateTargets(PipeBase<?> pb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public ItemStack attemptAcceptItem(ItemStack stack) {
		return stack;
	}

	@Override
	public FluidStack attemptAcceptLiquid(FluidStack stack) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int attemptAcceptPower(int power) {
		return power;
	}
}
