package me.haydenb.assemblylinemachines.item;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;

public interface IGearboxFuel {

	public int getGearboxBurnTime(ItemStack stack);


	public static class ItemGearboxFuel extends Item implements IGearboxFuel{

		public final int burnTime;

		public ItemGearboxFuel(int burnTime) {
			this(burnTime, Rarity.COMMON);
		}

		public ItemGearboxFuel(int burnTime, Rarity rarity) {
			super(new Item.Properties().rarity(rarity));
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
