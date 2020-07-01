package me.haydenb.assemblylinemachines.block.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer.Builder;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class GaseousFluid extends ForgeFlowingFluid{
	
	private final boolean source;
	public GaseousFluid(boolean source, Properties properties) {
		super(properties);
		this.source = source;
		if (!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, FluidState> builder) {
		super.fillStateContainer(builder);

		if (!source) {
			builder.add(LEVEL_1_8);
		}
	}

	@Override
	public boolean isSource(FluidState state) {
		return source;
	}

	@Override
	public int getLevel(FluidState state) {
		if (!source) {
			return state.get(LEVEL_1_8);
		} else {
			return 8;
		}
	}
	
	@Override
	public Item getFilledBucket() {
		return Items.BUCKET;
	}
	
}
