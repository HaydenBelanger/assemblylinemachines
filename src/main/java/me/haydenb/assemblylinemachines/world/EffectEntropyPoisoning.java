package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EffectEntropyPoisoning extends Effect {

	public EffectEntropyPoisoning() {
		super(EffectType.HARMFUL, 0x03fc5e);
	}
	
	@Override
	public void performEffect(LivingEntity livingEntity, int amplifier) {
		livingEntity.attackEntityFrom(DamageSource.MAGIC, 0.5F);
		
		livingEntity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 60));
		livingEntity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 60));
		livingEntity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60));
		
		
		
		if(livingEntity instanceof PlayerEntity && General.RAND.nextInt(3) == 0) {
			
			BlockPos pos = new BlockPos(livingEntity.getPositionVec());
			World world = livingEntity.getEntityWorld();
			
			for(int i = 0; i < 15; i++) {
				
				BlockPos rand = pos;
				
				int ix = 1;
				
				while(General.RAND.nextInt(ix) == 0) {
					rand = rand.offset(Direction.getRandomDirection(General.RAND), General.RAND.nextInt(6));
					rand = rand.offset(Direction.getRandomDirection(General.RAND), General.RAND.nextInt(6));
					rand = rand.offset(Direction.getRandomDirection(General.RAND), General.RAND.nextInt(6));
					
					ix++;
				}
				
				BlockPos rand2 = pos.offset(Direction.UP);
				
				
				if(world.getBlockState(rand).getBlock() == Blocks.AIR && world.getBlockState(rand2).getBlock() == Blocks.AIR) {
					livingEntity.setLocationAndAngles(rand.getX() + 0.5, rand.getY() + 0.5, rand.getZ() + 0.5, (General.RAND.nextFloat() * 360f) - 180f, (General.RAND.nextFloat() * 180f) - 90f);
				}
			}
		}
		
		
		
	}
	
	@Override
	public boolean isReady(int duration, int amplifier) {
		return duration % 10 == 0;
	}

	@Override
	public boolean isInstant() {
		return false;
	}
	
}
