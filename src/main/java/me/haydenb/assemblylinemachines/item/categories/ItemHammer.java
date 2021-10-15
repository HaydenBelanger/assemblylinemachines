package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;

public class ItemHammer extends SwordItem {

	
	public ItemHammer(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
		super(tier, attackDamageIn, attackSpeedIn, builder.maxDamage(tier.getMaxUses()));
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		
		if(itemStack.getDamage() >= itemStack.getMaxDamage()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack stack = itemStack.copy();
		
		if(stack.getItem() == Registry.getItem("crank_hammer")) {
			if(stack.hasTag()) {
				CompoundNBT compound = stack.getTag();
				
				if(compound.contains("assemblylinemachines:cranks")) {
					
					int cranks = compound.getInt("assemblylinemachines:cranks");
					if((cranks - 1) < 1) {
						compound.remove("assemblylinemachines:cranks");
						compound.remove("assemblylinemachines:hascranks");
					}else {
						compound.putInt("assemblylinemachines:cranks", cranks - 1);
					}
					
					
					
					stack.setTag(compound);
					return stack;
				}
			}
		}else if(stack.getItem() == Registry.getItem("mystium_hammer")) {
			if(stack.hasTag()) {
				CompoundNBT compound = stack.getTag();
				
				int fe = compound.getInt("assemblylinemachines:fe");
				
				if(fe >= 200) {
					compound.putInt("assemblylinemachines:fe", fe - 200);
					stack.setTag(compound);
					return stack;
				}
			}
		}
		stack.setDamage(itemStack.getDamage() + 1);
		
		return stack;
	}
	
	@Override
	public boolean hasContainerItem() {
		return true;
	}
}

