package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.block.machines.primitive.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGrindingBlade extends Item{

	public final Blades blade;
	
	public ItemGrindingBlade(Blades blade) {
		super(new Item.Properties().durability(blade.uses).tab(Registry.creativeTab));
		this.blade = blade;
	}
	
	public static boolean damageBlade(ItemStack stack) {
		if(stack.getDamageValue() >= stack.getMaxDamage()) {
			stack.shrink(1);
			return true;
		}
		stack.setDamageValue(stack.getDamageValue() + 1);
		return false;
	}
}
