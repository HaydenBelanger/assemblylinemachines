package me.haydenb.assemblylinemachines.client;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.config.Config;
import me.haydenb.assemblylinemachines.registry.utils.ScreenMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Class adapted from Relics Mod GitHub with explicit permission from author.
 * 
 * <pre>{@code This class uses a unique license that precedes AGPL-3.0:
 * - All Rights Reserved unless otherwise explicitly stated.
 * - If you wish to use the contents of this class, please reach out to the original author.}</pre>
 * 
 * @author SSKrillSS
 * @see <a href="https://github.com/SSKirillSS/relics">https://github.com/SSKirillSS/relics</a>
 *
 */
@Mod.EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class TooltipBorderHandler {
	
	public static final HashMap<ResourceLocation, ISpecialTooltip> ADHOC_TOOLTIPS = new HashMap<>();
	static {
		ISpecialTooltip steelTooltip = new ISpecialTooltip() {
			private final ResourceLocation rl = new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/steel.png");
			@Override
			public ResourceLocation getTexture() {
				return rl;
			}
			@Override
			public int getTopColor() {
				return 0xff404040;
			}
		};
		//STEEL
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_pickaxe"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_axe"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_shovel"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_sword"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_hoe"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_helmet"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_chestplate"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_leggings"), steelTooltip);
		ADHOC_TOOLTIPS.put(new ResourceLocation(AssemblyLineMachines.MODID, "steel_boots"), steelTooltip);
	}
	
	public static Boolean colors = null;
	public static Boolean frames = null;
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onTooltipColorEvent(RenderTooltipEvent.Color event) {
		if(getColorsEnabled()) {
			ISpecialTooltip special = get(event.getItemStack().getItem());
			if(special != null && special.isSpecial()) {
				
				event.setBorderStart(special.getTopColor());
				event.setBorderEnd(special.getBottomColor());
			}
		}

		
	}
	
	//Method triggered from TooltipMixin.
	@OnlyIn(Dist.CLIENT)
	public static void onPostTooltipEvent(ItemStack itemStack, PoseStack matrix, int x, int y, Font tooltipFont, int width, int height, List<ClientTooltipComponent> components) {
		if(getFramesEnabled()) {
			ISpecialTooltip special = get(itemStack.getItem());
			if(special != null && special.isSpecial()) {
				ResourceLocation rl = special.getTexture();
				if(rl != null) {
					RenderSystem.setShaderTexture(0, rl);
					Minecraft.getInstance().getTextureManager().bindForSetup(rl);

					int texWidth = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
					int texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

					if (texHeight == 0 || texWidth == 0)
						return;

					matrix.pushPose();

					RenderSystem.enableBlend();

					matrix.translate(0, 0, 410.0);

					GuiComponent.blit(matrix, x - 8 - 6, y - 8 - 6, 1, 1 % texHeight, 16, 16, texWidth, texHeight);
					GuiComponent.blit(matrix, x + width - 8 + 6, y - 8 - 6, texWidth - 16 - 1, 1 % texHeight, 16, 16, texWidth, texHeight);

					GuiComponent.blit(matrix, x - 8 - 6, y + height - 8 + 6, 1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);
					GuiComponent.blit(matrix, x + width - 8 + 6, y + height - 8 + 6, texWidth - 16 - 1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);

					if (width >= 94) {
						GuiComponent.blit(matrix, x + (width / 2) - 47, y - 16, 16 + 2 * texWidth + 1, 1 % texHeight, 94, 16, texWidth, texHeight);
						GuiComponent.blit(matrix, x + (width / 2) - 47, y + height, 16 + 2 * texWidth + 1, 1 % texHeight + 16, 94, 16, texWidth, texHeight);
					}

					RenderSystem.disableBlend();

					matrix.popPose();
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static boolean getColorsEnabled() {
		if(colors == null) {
			colors = Config.getClientConfig().customTooltipColors().get();
		}
		return colors;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static boolean getFramesEnabled() {
		if(frames == null) {
			frames = getColorsEnabled() ? Config.getClientConfig().customTooltipFrames().get() : false;
		}
		return frames;
	}
	
	private static ISpecialTooltip get(Item item) {
		return item instanceof ISpecialTooltip ? (ISpecialTooltip) item : ADHOC_TOOLTIPS.get(item.getRegistryName());
	}
	
	public static interface ISpecialTooltip {
		
		static final Cache<Integer, Integer> BOTTOM_COLOR_CACHE = CacheBuilder.newBuilder().build();
		
		/**
		 * @return Whether or not to render special tooltip color and frame. Defaults to true.
		 */
		default public boolean isSpecial() {
			return true;
		}
		
		/**
		 * @return ResourceLocation of texture to use, or null if you do not wish to render a special border.
		 */
		@Nullable
		public ResourceLocation getTexture();
		
		/**
		 * @return The ARGB color value of the top section of the tooltip. Typically, this is lighter than the bottom color.
		 */
		public int getTopColor();
		
		/**
		 * @return A supplier for the ARGB color value of the bottom section of the tooltip. Defaults to 30% darker than getTopColor.
		 */
		default public int getBottomColor(){
			try {
				return BOTTOM_COLOR_CACHE.get(getTopColor(), () -> ScreenMath.multiplyARGBColor(getTopColor(), 0.7f));
			}catch(ExecutionException e) {
				e.printStackTrace();
				return ScreenMath.multiplyARGBColor(getTopColor(), 0.7f);
			}
			
		}
		
	}
	
	
}