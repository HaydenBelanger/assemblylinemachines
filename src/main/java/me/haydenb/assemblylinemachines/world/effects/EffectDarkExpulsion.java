package me.haydenb.assemblylinemachines.world.effects;

import me.haydenb.assemblylinemachines.item.ItemAEFG;
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
			if(!protectWithAEFG(player, 1000)) {
				for(ItemStack i : player.getInventory().items) {
					if(!i.isEmpty()) {
						player.getInventory().removeItem(i);
						player.drop(i, true, true);
						break;
					}
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

	public static boolean protectWithAEFG(Player player, int damage) {
		for(ItemStack i : player.getInventory().items) {
			if(i.getItem() instanceof ItemAEFG && ((ItemAEFG) i.getItem()).damageItem(i, damage) != null) {
				return true;
			}
		}
		return false;
	}

}
