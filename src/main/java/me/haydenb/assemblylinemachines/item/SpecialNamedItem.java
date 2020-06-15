package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class SpecialNamedItem extends Item{

	private final TextFormatting[] formats;
	public SpecialNamedItem(TextFormatting... formats) {
		super(new Item.Properties().group(Registry.creativeTab));
		this.formats = formats;
	}

	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return super.getDisplayName(stack).applyTextStyles(formats);
	}
}
