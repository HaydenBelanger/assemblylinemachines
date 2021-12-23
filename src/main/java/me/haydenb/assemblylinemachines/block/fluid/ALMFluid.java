package me.haydenb.assemblylinemachines.block.fluid;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.*;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class ALMFluid extends ForgeFlowingFluid{

	public static final Tag<Fluid> OIL = makeWrapperTag("oil");
	public static final Tag<Fluid> OIL_BYPRODUCT = makeWrapperTag("oil_byproduct");
	public static final Tag<Fluid> LIQUID_EXPERIENCE = makeWrapperTag("liquid_experience");
	public static final Tag<Fluid> CONDENSED_VOID = makeWrapperTag("condensed_void");
	public static final Tag<Fluid> NAPHTHA = makeWrapperTag("naphtha");

	protected final boolean source;
	public ALMFluid(Properties properties, boolean source) {
		super(properties);

		this.source = source;

		if(!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, IFluidState> builder) {
		super.fillStateContainer(builder);

		if(!source) {
			builder.add(LEVEL_1_8);
		}
	}

	@Override
	public boolean isSource(IFluidState state) {
		return source;
	}
	@Override
	public int getLevel(IFluidState state) {
		if(!source) {
			return state.get(LEVEL_1_8);
		}else {
			return 8;
		}
	}


	public static class ALMFluidBlock extends FlowingFluidBlock {

		private final Tag<Fluid> tag;

		public ALMFluidBlock(String name, Tag<Fluid> tag, Block.Properties properties) {
			this(name, tag, properties, 0.014d);
		}

		public ALMFluidBlock(String name, Tag<Fluid> tag, Material material) {
			this(name, tag, material, 0.014d);
		}

		public ALMFluidBlock(String name, Tag<Fluid> tag, Material material, double acceleration) {
			this(name, tag, Block.Properties.create(material).hardnessAndResistance(100f).noDrops(), acceleration);
		}

		public ALMFluidBlock(String name, Tag<Fluid> tag, Block.Properties properties, double acceleration) {
			super(() -> (FlowingFluid) Registry.getFluid(name), properties);
			this.tag = tag;
		}




		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity e) {

			if(e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				le.handleFluidAcceleration(tag);
			}
		}

	}

	private static Tag<Fluid> makeWrapperTag(String p_206956_0_) {
		return new FluidTags.Wrapper(new ResourceLocation(p_206956_0_));
	}

}
