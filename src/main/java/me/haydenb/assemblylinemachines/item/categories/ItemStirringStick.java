package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ItemStirringStick extends Item {

	final TemperatureResistance sr;
	final boolean burnable;
	public ItemStirringStick(TemperatureResistance sr, boolean burnable, int durability) {
		super(new Item.Properties().defaultDurability(durability).tab(Registry.creativeTab));
		this.sr = sr;
		this.burnable = burnable;
	}
	
	public boolean useStirStick(ItemStack stack) {
		if(stack.getDamageValue() >= stack.getMaxDamage()) {
			stack.shrink(1);
			return true;
		}
		
		if(General.RAND.nextInt(3) == 0) {
			stack.setDamageValue(stack.getDamageValue() + 1);
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
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type) {
		if(this.burnable == true) {
			return 200;
		}
		
		return -1;
	}
	
}
