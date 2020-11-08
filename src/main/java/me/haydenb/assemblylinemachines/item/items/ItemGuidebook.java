package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.plugins.other.PatchouliALMImpl;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ItemGuidebook extends Item {
	public ItemGuidebook() {
		super(new Item.Properties().group(Registry.creativeTab).maxStackSize(1));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
		
		if(!world.isRemote) {
			PatchouliALMImpl.get().openBook((ServerPlayerEntity) player, world);
		}
		
		return super.onItemRightClick(world, player, handIn);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		onItemRightClick(context.getWorld(), context.getPlayer(), context.getHand());
		
		return ActionResultType.CONSUME;
	}
}
