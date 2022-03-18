package me.haydenb.assemblylinemachines.client;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forgespi.language.IModInfo;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class VersionCheck {

	//Event to send message to players if mod is out of date.
	@SubscribeEvent
	public static void join(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if(!player.getCommandSenderWorld().isClientSide() && ConfigHolder.getCommonConfig().updateChecker.get()) {
			ModContainer mc = AssemblyLineMachines.getModContainer();
			if(mc != null) {
				IModInfo imi = mc.getModInfo();
				CheckResult result = VersionChecker.getResult(imi);
				
				if(result.status() == Status.BETA_OUTDATED || result.status() == Status.OUTDATED) {
					MutableComponent tc = new TextComponent("[AssemblyLineMachines] Update available, version " + result.target().getCanonical() + ", you're using " + imi.getVersion().toString() + ". ").append(new TextComponent("Click to Update!").withStyle(ChatFormatting.DARK_GREEN));
					tc.withStyle(tc.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, result.url())));
					player.sendMessage(tc, null);
				}
			}
		}
		
	}
}
