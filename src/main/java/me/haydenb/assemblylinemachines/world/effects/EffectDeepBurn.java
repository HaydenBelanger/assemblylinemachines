package me.haydenb.assemblylinemachines.world.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectDeepBurn extends MobEffect {

	public EffectDeepBurn() {
		super(MobEffectCategory.HARMFUL, 0x000000);
	}

	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		//pLivingEntity.setSecondsOnFire(30);
	}

	@Override
	public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
		return pDuration % 5 == 0;
	}

	@Override
	public boolean isInstantenous() {
		return false;
	}
}
