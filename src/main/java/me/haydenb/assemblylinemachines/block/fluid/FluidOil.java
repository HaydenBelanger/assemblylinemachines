package me.haydenb.assemblylinemachines.block.fluid;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.registry.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;

public class FluidOil extends ALMFluid {

	
	public FluidOil(boolean source) {
		super(FluidRegistry.buildProperties("oil", 400, false, true, true), source);
	}
	
	@Override
	public int getAmount(FluidState state) {
		if(!source) {
			return state.getValue(LEVEL);
		}else {
			return 5;
		}
	}
	
	@Override
	public Fluid getSource() {
		// TODO Auto-generated method stub
		return super.getSource();
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 25;
	}
	
	public static class FluidOilBlock extends ALMFluidBlock{

		public FluidOilBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.OIL, Material.WATER);
		}

		@Override
		public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity e) {
			if(e instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) e;
				
				entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60));
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
			}
			
			super.entityInside(pState, pLevel, pPos, e);
		}
		
	}

}
