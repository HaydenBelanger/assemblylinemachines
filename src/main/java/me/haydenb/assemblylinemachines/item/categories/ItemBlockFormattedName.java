package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemBlockFormattedName extends BlockItem{

	protected final TextFormatting[] formats;
	public ItemBlockFormattedName(Block block, TextFormatting... formats) {
		super(block, new Item.Properties().group(Registry.creativeTab));
		this.formats = formats;
	}
	
	public ItemBlockFormattedName(Block block, Properties properties, TextFormatting... formats) {
		super(block, properties.group(Registry.creativeTab));
		this.formats = formats;
	}

	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return super.getDisplayName(stack).func_230532_e_().func_240701_a_(formats);
	}
}
