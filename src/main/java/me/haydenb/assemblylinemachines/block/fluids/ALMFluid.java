package me.haydenb.assemblylinemachines.block.fluids;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.client.FogRendering.ILiquidFogColor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class ALMFluid extends ForgeFlowingFluid implements ILiquidFogColor{
	
	protected final int[] rgb;
	protected final boolean source;
	
	public ALMFluid(Properties properties, boolean source, int... rgb) {
		super(properties);
		this.source = source;
		this.rgb = rgb;
		
		if(rgb.length < 3) {
			throw new IllegalArgumentException("RGB fog color value array contains less than 3 numbers.");
		}
		
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
	
	@Override
	public int[] getRGB() {
		return rgb;
	}

	public static class ALMFluidBlock extends LiquidBlock {

		private final TagKey<Fluid> tag;
		private final double acceleration;

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, TagKey<Fluid> tag, Block.Properties properties) {
			this(fluid, tag, properties, 0.014d);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, TagKey<Fluid> tag, Material material) {
			this(fluid, tag, material, 0.014d);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, TagKey<Fluid> tag, Material material, double acceleration) {
			this(fluid, tag, Block.Properties.of(material).strength(100f).noDrops(), acceleration);
		}

		public ALMFluidBlock(Supplier<? extends FlowingFluid> fluid, TagKey<Fluid> tag, Block.Properties properties, double acceleration) {
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
	
	public static TagKey<Fluid> getTag(String name){
		return TagKey.create(Keys.FLUIDS, new ResourceLocation(AssemblyLineMachines.MODID, name));
	}


}
