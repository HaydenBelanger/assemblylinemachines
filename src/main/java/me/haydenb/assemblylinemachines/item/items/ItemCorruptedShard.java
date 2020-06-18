package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.item.categories.IGearboxFuel;
import me.haydenb.assemblylinemachines.item.categories.ItemBasicFormattedName;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemCorruptedShard extends ItemBasicFormattedName implements IGearboxFuel{

	public ItemCorruptedShard() {
		super(TextFormatting.OBFUSCATED);
	}
	
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:internalitem")) {
			ItemStack intStack = ItemStack.read(stack.getTag().getCompound("assemblylinemachines:internalitem"));
			return intStack.getDisplayName().applyTextStyles(formats);
		}else {
			return super.getDisplayName(stack);
		}
	}
	
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return 9600;
	}


	@Override
	public int getGearboxBurnTime(ItemStack stack) {
		return this.getBurnTime(stack);
	}
}
