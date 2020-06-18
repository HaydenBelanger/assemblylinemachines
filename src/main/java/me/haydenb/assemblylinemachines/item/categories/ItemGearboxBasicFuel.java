package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ItemGearboxBasicFuel extends Item implements IGearboxFuel{

	public final int burnTime;
	
	public ItemGearboxBasicFuel(int burnTime) {
		super(new Item.Properties().group(Registry.creativeTab));
		this.burnTime = burnTime;
	}
	
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return burnTime;
	}

	@Override
	public int getGearboxBurnTime(ItemStack stack) {
		return this.getBurnTime(stack);
	}
}
