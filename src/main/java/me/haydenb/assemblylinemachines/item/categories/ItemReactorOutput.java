package me.haydenb.assemblylinemachines.item.categories;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class ItemReactorOutput extends Item {

	private final String quality;
	
	public ItemReactorOutput(String quality) {
		super(new Item.Properties().tab(Registry.creativeTab));
		this.quality = quality;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level pLevel, List<Component> tooltipComponents, TooltipFlag pIsAdvanced) {
		tooltipComponents.add(new TextComponent("§8This is a " + quality + "§8 by-product."));
		super.appendHoverText(stack, pLevel, tooltipComponents, pIsAdvanced);
	}
}
