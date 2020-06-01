package me.haydenb.assemblylinemachines.block.pipe;

import me.haydenb.assemblylinemachines.misc.TileEntityALMBase;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;

public class EnergyPipeConnectorTileEntity extends TileEntityALMBase implements IPipeConnector{

	public EnergyPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		
	}
	
	public EnergyPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_energy"));
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
		return stack;
	}

	@Override
	public int attemptAcceptPower(int power) {
		// TODO Auto-generated method stub
		return 0;
	}

}
