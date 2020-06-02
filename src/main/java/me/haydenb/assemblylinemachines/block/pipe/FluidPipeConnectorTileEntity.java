package me.haydenb.assemblylinemachines.block.pipe;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.TileEntityALMBase;
import net.minecraft.tileentity.TileEntityType;

public class FluidPipeConnectorTileEntity extends TileEntityALMBase{

	public FluidPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		
	}
	
	public FluidPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_fluid"));
	}
}
