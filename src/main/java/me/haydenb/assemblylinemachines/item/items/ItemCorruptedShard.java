package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.item.categories.IGearboxFuel;
import me.haydenb.assemblylinemachines.item.categories.ItemBasicFormattedName;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
			return intStack.getDisplayName().deepCopy().mergeStyle(formats);
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
	
	public static ItemStack corruptItem(ItemStack orig) {
		if(orig.getItem() != Registry.getItem("corrupted_shard")) {
			CompoundNBT main = new CompoundNBT();
			CompoundNBT sub = new CompoundNBT();
			
			new ItemStack(orig.getItem(), 1).write(sub);
			main.put("assemblylinemachines:internalitem", sub);
			
			orig = new ItemStack(Registry.getItem("corrupted_shard"), orig.getCount());
			
			orig.setTag(main);
		}
		
		return orig;
		
	}
}
