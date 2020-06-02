package me.haydenb.assemblylinemachines.block.pipe;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.TileEntityALMBase;
import net.minecraft.tileentity.TileEntityType;

public class EnergyPipeConnectorTileEntity extends TileEntityALMBase{

	public EnergyPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		
	}
	
	public EnergyPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_energy"));
	}

}
