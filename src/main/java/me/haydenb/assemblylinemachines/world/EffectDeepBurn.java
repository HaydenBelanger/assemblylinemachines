package me.haydenb.assemblylinemachines.world;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class EffectDeepBurn extends Effect {

	public EffectDeepBurn() {
		super(EffectType.HARMFUL, 0xff4d00);
	}
	
	@Override
	public void performEffect(LivingEntity livingEntity, int amplifier) {
		
		livingEntity.setFire(30);
		
		
		
	}
	
	@Override
	public boolean isReady(int duration, int amplifier) {
		return duration % 5 == 0;
	}

	@Override
	public boolean isInstant() {
		return false;
	}
	
}
