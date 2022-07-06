package me.haydenb.assemblylinemachines.mixins.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.PoseStack;

import me.haydenb.assemblylinemachines.client.TooltipBorderHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;

/**
 * Class adapted from Iceberg API GitHub with permission inherited from license.
 *
 * <pre>{@code This class uses a unique license that precedes AGPL-3.0:
 * - Creative Commons Attribution - Non-Commerical - No Derivatives 4.0 International Public License
 * - For more info on this license, please see https://github.com/AHilyard/Iceberg/blob/1.16.5/LICENSE.
 * - Uses of this class are governed by this license agreement.}</pre>
 *
 * @author AHilyard
 * @see <a href="https://github.com/AHilyard/Iceberg">https://github.com/AHilyard/Iceberg</a>
 *
 */
@Mixin(Screen.class)
public class TooltipMixin {

	@Shadow
	protected Font font = null;

	@Shadow(remap = false)
	private Font tooltipFont = null;

	@Shadow(remap = false)
	private ItemStack tooltipStack = ItemStack.EMPTY;

	@Inject(method = "renderTooltipInternal", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;blitOffset:F", ordinal = 2, shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, CallbackInfo info, RenderTooltipEvent.Pre pre, int tooltipWidth, int tooltipHeight, int postX, int postY) {
		TooltipBorderHandler.onPostTooltipEvent(tooltipStack, poseStack, postX, postY, ForgeHooksClient.getTooltipFont(tooltipFont, tooltipStack, font), tooltipWidth, tooltipHeight, components);
	}
}
