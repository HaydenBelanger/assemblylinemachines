package me.haydenb.assemblylinemachines.client;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
public class FogRendering {

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void applyFogColor(EntityViewRenderEvent.FogColors event) {
	
		Fluid f = event.getInfo().getBlockAtCamera().getFluidState().getType();
		if(f instanceof ILiquidFogColor) {
			ILiquidFogColor fogColor = (ILiquidFogColor) f;
			int[] rgb = fogColor.getRGB();
			event.setRed((float) rgb[0] / 255f);
			event.setGreen((float) rgb[1] / 255f);
			event.setBlue((float) rgb[2] / 255f);
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void applyFogDensity(EntityViewRenderEvent.FogDensity event) {
		Fluid f = event.getInfo().getBlockAtCamera().getFluidState().getType();
		if(f instanceof ILiquidFogColor) {
			ILiquidFogColor fogColor = (ILiquidFogColor) f;
			event.setCanceled(true);
			Minecraft mc = Minecraft.getInstance();
			event.setDensity(fogColor.getFogDensity(mc.player));
		}
	}
	
	public static interface ILiquidFogColor{
		public int[] getRGB();
		
		default public float getFogDensity() {
			//48 is the default return value for Water visibility.
			return 48.0f;
		}
		
		default public float getFogDensity(LocalPlayer player) {
			return this.getFogDensity();
		}
	}
}
