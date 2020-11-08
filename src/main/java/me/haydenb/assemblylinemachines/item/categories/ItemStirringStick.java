package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStirringStick extends Item {

	final TemperatureResistance sr;
	final boolean burnable;
	public ItemStirringStick(TemperatureResistance sr, boolean burnable, int durability) {
		super(new Item.Properties().maxDamage(durability).group(Registry.creativeTab));
		this.sr = sr;
		this.burnable = burnable;
	}
	
	public boolean useStirStick(ItemStack stack) {
		if(stack.getDamage() >= stack.getMaxDamage()) {
			stack.shrink(1);
			return true;
		}
		
		if(General.RAND.nextInt(3) == 0) {
			stack.setDamage(stack.getDamage() + 1);
		}
		return false;
	}
	
	public TemperatureResistance getStirringResistance() {
		return sr;
	}
	
	public static enum TemperatureResistance{
		COLD, HOT;
	}

	
	@Override
	public int getBurnTime(ItemStack itemStack) {
		if(this.burnable == true) {
			return 200;
		}
		
		return -1;
	}
	
}
