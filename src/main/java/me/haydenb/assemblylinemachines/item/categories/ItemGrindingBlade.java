package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.block.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemGrindingBlade extends Item{

	public final Blades blade;
	
	public ItemGrindingBlade(Blades blade) {
		super(new Item.Properties().maxDamage(blade.uses).group(Registry.creativeTab));
		this.blade = blade;
	}
	
	public static boolean damageBlade(ItemStack stack) {
		if(stack.getDamage() >= stack.getMaxDamage()) {
			stack.shrink(1);
			return true;
		}
		stack.setDamage(stack.getDamage() + 1);
		return false;
	}
}
