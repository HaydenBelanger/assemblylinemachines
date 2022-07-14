package me.haydenb.assemblylinemachines.registry.utils;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class ScreenMath {
	
	
	//Claimed from ItemRenderer#renderItemOverlayIntoGUI, modified.
	@OnlyIn(Dist.CLIENT)
	public static void renderItemSlotBoundScaledText(Font fr, int xpos, int ypos, float scale, String text) {
		
		xpos = Math.round(xpos * (1.0f / scale));
		ypos = Math.round(ypos * (1.0f / scale));
		PoseStack mx = new PoseStack();
		
		mx.translate(0.0D, 0.0D, 200D);
		mx.scale(scale, scale, scale);
		BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		fr.drawInBatch(text, (float) (xpos + 19 - 2 - fr.width(text)), (float) (ypos + 6 + 3), 16777215, true, mx.last().pose(),
				irendertypebuffer$impl, false, 0, 15728880);
		
		irendertypebuffer$impl.endBatch();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text, boolean dropShadow, int color) {
		
		xpos = Math.round(xpos * (1.0f / scale));
		ypos = Math.round(ypos * (1.0f / scale));
		PoseStack mx = new PoseStack();
		
		mx.translate(0.0D, 0.0D, 200D);
		mx.scale(scale, scale, scale);
		BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		fr.drawInBatch(text, (float) xpos, ypos, color, dropShadow, mx.last().pose(),
				irendertypebuffer$impl, false, 0, 15728880);
		irendertypebuffer$impl.endBatch();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text) {
		
		renderScaledText(fr, xpos, ypos, scale, text, false, 0xffffff);
	}
	
	public static boolean isMouseBetween(int globalX, int globalY, int mouseX, int mouseY, int minX, int minY, int maxX, int maxY) {
		return mouseX >= globalX + minX && mouseX <= globalX + maxX && mouseY >= globalY + minY && mouseY <= globalY + maxY;
	}
	
	public static int multiplyARGBColor(int argb, float multiplier) {
		int[] argbSplit = new int[] {ARGB32.alpha(argb), ARGB32.red(argb), ARGB32.green(argb), ARGB32.blue(argb)};
		
		for(int i = 0; i < argbSplit.length; i++) {
			argbSplit[i] = Math.round((float) argbSplit[i] * multiplier);
		}
		
		return ARGB32.color(argbSplit[0], argbSplit[1], argbSplit[2], argbSplit[3]);
	}
	
	public static boolean canFit(ItemStack slotStack, ItemStack newStack) {
		return slotStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(slotStack, newStack) && slotStack.getCount() + newStack.getCount() <= slotStack.getMaxStackSize());
	}
	
	public static boolean doFit(ItemStack slotStack, ItemStack newStack, Consumer<ItemStack> replace, Consumer<Integer> grow) {
		
		if(canFit(slotStack, newStack)) {
			if(slotStack.isEmpty()) {
				replace.accept(newStack);
			}else {
				grow.accept(newStack.getCount());
			}
			return true;
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Font pFont, Component pText, int pX, int pY, int pColor) {
		FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
		pFont.draw(pPoseStack, formattedcharsequence, (float)(pX - pFont.width(formattedcharsequence) / 2), (float)pY, pColor);
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Component pText, int pX, int pY) {
		Minecraft mc = Minecraft.getInstance();
		drawCenteredStringWithoutShadow(pPoseStack, mc.font, pText, pX, pY, 4210752);
	}
}