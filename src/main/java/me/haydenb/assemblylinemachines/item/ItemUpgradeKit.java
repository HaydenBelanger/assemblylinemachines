package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.crafting.UpgradeKitCrafting;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;

public class ItemUpgradeKit extends Item {
	
	public ItemUpgradeKit() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB).rarity(Rarity.RARE));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {

		if(!context.getLevel().isClientSide()) {
			if(UpgradeKitCrafting.tryUpgrade(context.getClickedPos(), context.getLevel(), context.getItemInHand()) == true) {
				context.getItemInHand().shrink(1);
			}
		}
		
		return InteractionResult.CONSUME;
	}
}
