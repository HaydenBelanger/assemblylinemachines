package me.haydenb.assemblylinemachines.item.categories;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBasicFormattedName extends Item{

	protected final ChatFormatting[] formats;
	public ItemBasicFormattedName(ChatFormatting... formats) {
		super(new Item.Properties().tab(Registry.creativeTab));
		this.formats = formats;
	}
	
	public ItemBasicFormattedName(Properties properties, ChatFormatting... formats) {
		super(properties.tab(Registry.creativeTab));
		this.formats = formats;
	}

	
	@Override
	public Component getName(ItemStack stack) {
		return super.getName(stack).copy().withStyle(formats);
	}
}
