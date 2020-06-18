package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStirringStick extends Item {

	TemperatureResistance sr;
	public ItemStirringStick(int durability, TemperatureResistance sr) {
		super(new Item.Properties().maxDamage(durability).group(Registry.creativeTab));
		this.sr = sr;
	}
	
	
	public boolean useStirStick(ItemStack stack) {
		if(stack.getDamage() >= stack.getMaxDamage()) {
			stack.shrink(1);
			return true;
		}
		
		stack.setDamage(stack.getDamage() + 1);
		return false;
	}
	
	public TemperatureResistance getStirringResistance() {
		return sr;
	}
	
	public static enum TemperatureResistance{
		COLD, HOT;
	}

	
}
