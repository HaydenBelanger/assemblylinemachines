package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.item.ItemTiers.ArmorTiers;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class ItemChaoticReductionGoggles extends ArmorItem {

	public ItemChaoticReductionGoggles() {
		super(ArmorTiers.CRG, EquipmentSlot.HEAD, new Item.Properties().tab(Registry.CREATIVE_TAB));
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		pTooltipComponents.add(new TextComponent("§8§oKeeps Dark Energy out of your eyes."));
	}
}
