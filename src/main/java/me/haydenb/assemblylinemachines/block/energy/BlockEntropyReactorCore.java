package me.haydenb.assemblylinemachines.block.energy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

public class BlockEntropyReactorCore extends Block {

	public static final BooleanProperty CORE_CRITICAL = BooleanProperty.create("critical");
	
	public BlockEntropyReactorCore() {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).sound(SoundType.METAL));

		this.registerDefaultState(this.stateDefinition.any().setValue(CORE_CRITICAL, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(CORE_CRITICAL);
	}
}
