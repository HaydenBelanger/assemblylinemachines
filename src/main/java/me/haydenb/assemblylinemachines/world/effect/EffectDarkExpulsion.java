package me.haydenb.assemblylinemachines.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EffectDarkExpulsion extends MobEffect {

	public EffectDarkExpulsion() {
		super(MobEffectCategory.HARMFUL, 0xff4d00);
	}
	
	@Override
	public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
		if(pLivingEntity instanceof Player) {
			Player player = (Player) pLivingEntity;
			for(ItemStack i : player.getInventory().items) {
				if(!i.isEmpty()) {
					player.getInventory().removeItem(i);
					player.drop(i, true, true);
					break;
				}
			}
		}
	}
	
	@Override
	public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
		return pDuration % 10 == 0;
	}
	
	@Override
	public boolean isInstantenous() {
		return false;
	}
	
}
