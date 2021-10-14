package me.haydenb.assemblylinemachines.block.fluid;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class ALMFluid extends ForgeFlowingFluid{

	public static final Tag.Named<Fluid> OIL = FluidTags.bind("oil");
	public static final Tag.Named<Fluid> OIL_BYPRODUCT = FluidTags.bind("oil_byproduct");
	public static final Tag.Named<Fluid> LIQUID_EXPERIENCE = FluidTags.bind("liquid_experience");
	public static final Tag.Named<Fluid> CONDENSED_VOID = FluidTags.bind("condensed_void");
	public static final Tag.Named<Fluid> NAPHTHA = FluidTags.bind("naphtha");

	protected final boolean source;
	public ALMFluid(Properties properties, boolean source) {
		super(properties);

		this.source = source;

		if(!source) {
			this.registerDefaultState(this.defaultFluidState().setValue(LEVEL, 7));
		}
	}

	@Override
	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder) {
		super.createFluidStateDefinition(builder);

		if(!source) {
			builder.add(LEVEL);
		}
	}

	@Override
	public boolean isSource(FluidState state) {
		return source;
	}

	@Override
	public int getAmount(FluidState state) {
		if(!source) {
			return state.getValue(LEVEL);
		}else {
			return 8;
		}
	}

	public static class ALMFluidBlock extends LiquidBlock {

		private final Tag.Named<Fluid> tag;
		private final double acceleration;

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, Tag.Named<Fluid> tag, Block.Properties properties) {
			this(fluid, tag, properties, 0.014d);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, Tag.Named<Fluid> tag, Material material) {
			this(fluid, tag, material, 0.014d);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, Tag.Named<Fluid> tag, Material material, double acceleration) {
			this(fluid, tag, Block.Properties.of(material).strength(100f).noDrops(), acceleration);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, Tag.Named<Fluid> tag, Block.Properties properties, double acceleration) {
			super(fluid, properties);
			this.tag = tag;
			this.acceleration = acceleration;
		}


		@Override
		public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity entity) {
			if(entity instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) entity;
				le.updateFluidHeightAndDoFluidPushing(tag, acceleration);
			}
		}

	}


}
