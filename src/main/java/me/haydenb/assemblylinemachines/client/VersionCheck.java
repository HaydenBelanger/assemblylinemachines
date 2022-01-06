package me.haydenb.assemblylinemachines.client;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.TextComponent;
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
		if(!player.getCommandSenderWorld().isClientSide() && ConfigHolder.getServerConfig().updateChecker.get()) {
			ModContainer mc = AssemblyLineMachines.getModContainer();
			if(mc != null) {
				IModInfo imi = mc.getModInfo();
				CheckResult result = VersionChecker.getResult(imi);
				
				if(result.status() == Status.BETA_OUTDATED || result.status() == Status.OUTDATED) {
					TextComponent tc = new TextComponent("[§aAssemblyLineMachines§f] Update available, version §e" + result.target().getCanonical() + ",§f you're using §e" + imi.getVersion().toString() + ". §2Click to Update!");
					tc.withStyle(tc.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, result.url())));
					player.sendMessage(tc, null);
				}
			}
		}
		
	}
}
