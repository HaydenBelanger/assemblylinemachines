package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public interface IGearboxFuel {

	public int getGearboxBurnTime(ItemStack stack);
	
	
	public static class ItemGearboxFuel extends Item implements IGearboxFuel{

		public final int burnTime;
		
		public ItemGearboxFuel(int burnTime) {
			super(new Item.Properties().tab(Registry.CREATIVE_TAB));
			this.burnTime = burnTime;
		}
		
		@Override
		public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
			
			return burnTime;
		}

		@Override
		public int getGearboxBurnTime(ItemStack stack) {
			return this.getBurnTime(stack, null);
		}
	}
}
