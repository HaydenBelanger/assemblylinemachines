package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

public class ItemBlockFormattedName extends BlockItem{

	protected final ChatFormatting[] formats;
	public ItemBlockFormattedName(Block block, ChatFormatting... formats) {
		super(block, new Item.Properties().tab(Registry.CREATIVE_TAB));
		this.formats = formats;
	}
	
	public ItemBlockFormattedName(Block block, Properties properties, ChatFormatting... formats) {
		super(block, properties.tab(Registry.CREATIVE_TAB));
		this.formats = formats;
	}

	
	@Override
	public Component getName(ItemStack stack) {
		return super.getName(stack).copy().withStyle(formats);
	}
}
