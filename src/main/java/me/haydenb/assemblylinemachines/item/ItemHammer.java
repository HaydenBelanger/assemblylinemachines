package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge;
import net.minecraft.world.item.*;

public class ItemHammer extends SwordItem {


	public ItemHammer(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder.durability(tier.getUses()));
	}
	
	@Override
	public ItemStack getCraftingRemainingItem(ItemStack origStack) {
		ItemStack itemStack = origStack.copy();

		if(itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
			return ItemStack.EMPTY;
		}

		if(itemStack.getItem() instanceof IToolWithCharge) {
			IToolWithCharge crankable = (IToolWithCharge) itemStack.getItem();
			ItemStack resCrankable = crankable.damageItem(itemStack, 1);
			if(resCrankable != null) return resCrankable;
		}

		itemStack.setDamageValue(itemStack.getDamageValue() + 1);
		return itemStack;
	}

	@Override
	public boolean hasCraftingRemainingItem() {
		return true;
	}
}

