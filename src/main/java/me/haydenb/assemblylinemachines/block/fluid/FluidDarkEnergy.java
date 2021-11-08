package me.haydenb.assemblylinemachines.block.fluid;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.rendering.FogRendering.ILiquidFogColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;

public class FluidDarkEnergy extends ALMFluid implements ILiquidFogColor {
	
	public FluidDarkEnergy(boolean source) {
		super(Registry.createFluidProperties("dark_energy", -100, false, true, true), source, 69, 69, 69);
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 3;
	}
	
	@Override
	public float getFogDensity() {
		return 12f;
	}
	
	public static class FluidDarkEnergyBlock extends ALMFluidBlock {

		public FluidDarkEnergyBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.getTag("dark_energy"), Material.WATER);
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				if(!player.hasEffect(Registry.getEffect("dark_expulsion"))) {
					player.addEffect(new MobEffectInstance(Registry.getEffect("dark_expulsion"), 129));
				}
				
			}
			super.entityInside(state, worldIn, pos, entity);
		}
	}

}
