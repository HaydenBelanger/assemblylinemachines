package me.haydenb.assemblylinemachines.block.fluid;

import me.haydenb.assemblylinemachines.registry.FluidRegistration;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class FluidOil extends ALMFluid {

	
	public FluidOil(boolean source) {
		super(FluidRegistration.buildProperties("oil", 400, false, true, true), source);
	}

	@Override
	public int getLevel(IFluidState state) {
		if(!source) {
			return state.get(LEVEL_1_8);
		}else {
			return 5;
		}
	}
	
	@Override
	public int getTickRate(IWorldReader world) {
		return 25;
	}
	
	public static class FluidOilBlock extends ALMFluidBlock{

		public FluidOilBlock() {
			super("oil", ALMFluid.OIL, Material.WATER);
		}
		
		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity e) {
			
			
            
			if(e instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) e;
				
				entity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 60));
				entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 40, 3));
			}
			
			super.onEntityCollision(state, worldIn, pos, e);
		}
		
	}

}
