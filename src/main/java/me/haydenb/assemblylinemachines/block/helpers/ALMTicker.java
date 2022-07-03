package me.haydenb.assemblylinemachines.block.helpers;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface ALMTicker<A extends BlockEntity>{

	void tick();

}
