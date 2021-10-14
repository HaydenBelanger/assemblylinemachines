package me.haydenb.assemblylinemachines.registry.datagen;

import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.Block;

public interface IBlockWithHarvestableTags {

	public Named<Block> getToolType();
	
	public Named<Block> getToolLevel();
}
