package me.haydenb.assemblylinemachines.client;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, value = {Dist.CLIENT})
public class VersionCheck {
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void join(LoggedInEvent event) {
		if(ALMConfig.getClientConfig().receiveUpdateMessages().get()) {
			CheckResult result = VersionChecker.getResult(AssemblyLineMachines.MOD_CONTAINER.get().getModInfo());
			if(result.status() == Status.BETA_OUTDATED || result.status() == Status.OUTDATED) {
				MutableComponent tc = new TextComponent("[AssemblyLineMachines] Update available, version " + result.target().getCanonical() + ". ").append(new TextComponent("Click to Update!").withStyle(ChatFormatting.DARK_GREEN));
				tc.withStyle(tc.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, result.url())));
				event.getPlayer().sendMessage(tc, null);
			}
		}
	}
}
