package me.haydenb.assemblylinemachines.world.effect;

import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class EffectEntropyPoisoning extends MobEffect {

	public EffectEntropyPoisoning() {
		super(MobEffectCategory.HARMFUL, 0x03fc5e);
	}
	
	@Override
	public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
		livingEntity.hurt(DamageSource.MAGIC, 0.5F);
		
		livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60));
		livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60));
		livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60));
		
		
		
		if(livingEntity instanceof Player && Utils.RAND.nextInt(3) == 0) {
			
			BlockPos pos = new BlockPos(livingEntity.position());
			Level world = livingEntity.getCommandSenderWorld();
			
			for(int i = 0; i < 15; i++) {
				
				BlockPos rand = pos;
				
				int ix = 1;
				
				while(Utils.RAND.nextInt(ix) == 0) {
					rand = rand.relative(Direction.getRandom(Utils.RAND), Utils.RAND.nextInt(6));
					rand = rand.relative(Direction.getRandom(Utils.RAND), Utils.RAND.nextInt(6));
					rand = rand.relative(Direction.getRandom(Utils.RAND), Utils.RAND.nextInt(6));
					
					ix++;
				}
				
				BlockPos rand2 = pos.relative(Direction.UP);
				
				
				if(world.getBlockState(rand).getBlock() == Blocks.AIR && world.getBlockState(rand2).getBlock() == Blocks.AIR) {
					livingEntity.moveTo(rand.getX() + 0.5, rand.getY() + 0.5, rand.getZ() + 0.5, (Utils.RAND.nextFloat() * 360f) - 180f, (Utils.RAND.nextFloat() * 180f) - 90f);
				}
			}
		}
		
		
		
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return duration % 10 == 0;
	}

	@Override
	public boolean isInstantenous() {
		return false;
	}
	
}
