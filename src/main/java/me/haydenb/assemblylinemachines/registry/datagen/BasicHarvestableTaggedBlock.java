package me.haydenb.assemblylinemachines.registry.datagen;

import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.Block;

public class BasicHarvestableTaggedBlock extends Block implements IBlockWithHarvestableTags {

	private final Named<Block> toolType;
	private final Named<Block> toolLevel;
	
	public BasicHarvestableTaggedBlock(Properties p_49795_, Named<Block> toolType, Named<Block> toolLevel) {
		super(p_49795_);
		if(toolType == null || toolLevel == null) {
			throw new NullPointerException("type and level cannot be null!");
		}
		this.toolType = toolType;
		this.toolLevel = toolLevel;
	}

	@Override
	public Named<Block> getToolType() {
		return toolType;
	}

	@Override
	public Named<Block> getToolLevel() {
		return toolLevel;
	}

}
