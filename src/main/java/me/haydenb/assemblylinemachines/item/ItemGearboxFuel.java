package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemGearboxFuel extends Item {

	public final int burnTime;
	
	public ItemGearboxFuel(int burnTime) {
		super(new Item.Properties().group(Registry.creativeTab));
		this.burnTime = burnTime;
	}
	
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return burnTime;
	}
}
