package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.plugins.PluginPatchouli;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class ItemGuidebook extends Item {
	
	public ItemGuidebook() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB).stacksTo(1));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand());
		return InteractionResult.CONSUME;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		if(!pLevel.isClientSide) {
			PluginPatchouli.get().openBook((ServerPlayer) pPlayer, pLevel);
		}
		
		return super.use(pLevel, pPlayer, pUsedHand);
	}
	
	//Event to give new players the Guidebook.
	@SubscribeEvent
	public static void joinGiveBook(PlayerLoggedInEvent event) {
		CompoundTag nbt = event.getPlayer().getPersistentData();
		if(ConfigHolder.COMMON.guideBook.get() && (!nbt.contains("assemblylinemachines:book") || nbt.getBoolean("assemblylinemachines:book") == false)) {
			
			if(PluginPatchouli.get().isPatchouliInstalled()) {
				nbt.putBoolean("assemblylinemachines:book", true);
				event.getPlayer().addItem(new ItemStack(Registry.getItem("guidebook")));
				event.getPlayer().save(nbt);
			}
		}
	}
	
}