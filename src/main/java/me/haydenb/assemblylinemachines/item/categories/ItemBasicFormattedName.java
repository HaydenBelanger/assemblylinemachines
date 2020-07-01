package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemBasicFormattedName extends Item{

	protected final TextFormatting[] formats;
	public ItemBasicFormattedName(TextFormatting... formats) {
		super(new Item.Properties().group(Registry.creativeTab));
		this.formats = formats;
	}
	
	public ItemBasicFormattedName(Properties properties, TextFormatting... formats) {
		super(properties.group(Registry.creativeTab));
		this.formats = formats;
	}

	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return super.getDisplayName(stack).func_230532_e_().func_240701_a_(formats);
	}
}
