package me.haydenb.assemblylinemachines.registry.utils;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
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
		fr.drawInBatch(text, xpos + 19 - 2 - fr.width(text), ypos + 6 + 3, 16777215, true, mx.last().pose(),
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
		fr.drawInBatch(text, xpos, ypos, color, dropShadow, mx.last().pose(),
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
		
		argbSplit = multiplyARGBColor(argbSplit, multiplier);

		return ARGB32.color(argbSplit[0], argbSplit[1], argbSplit[2], argbSplit[3]);
	}
	
	public static int[] multiplyARGBColor(int[] argb, float multiplier) {
		for(int i = 0; i < argb.length; i++) {
			argb[i] = Math.min(255, Math.max(0, Math.round(argb[i] * multiplier)));
		}
		
		return argb;
	}
	
	public static int[] unpackColor(int argb) {
		return new int[]{ARGB32.alpha(argb), ARGB32.red(argb), ARGB32.green(argb), ARGB32.blue(argb)};
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
		pFont.draw(pPoseStack, formattedcharsequence, pX - pFont.width(formattedcharsequence) / 2, pY, pColor);
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Component pText, int pX, int pY) {
		Minecraft mc = Minecraft.getInstance();
		drawCenteredStringWithoutShadow(pPoseStack, mc.font, pText, pX, pY, 4210752);
	}

	@OnlyIn(Dist.CLIENT)
	public static int getColorFrom(ResourceLocation location) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS);
		if (texture instanceof TextureAtlas) {
			return getColorFrom(((TextureAtlas) texture).getSprite(location));
		}
		return 0;
	}

	@OnlyIn(Dist.CLIENT)
	public static int getColorFrom(TextureAtlasSprite sprite) {
		if (sprite == null) return -1;
		if(sprite.contents().getUniqueFrames().count() == 0) return -1;
		float total = 0, red = 0, blue = 0, green = 0;
		for (int x = 0; x < sprite.contents().width(); x++) {
			for (int y = 0; y < sprite.contents().height(); y++) {
				int color = sprite.getPixelRGBA(0, x, y);
				int alpha = color >> 24 & 0xFF;
			// if (alpha != 255) continue; // this creates problems for translucent textures
			total += alpha;
			red += (color & 0xFF) * alpha;
			green += (color >> 8 & 0xFF) * alpha;
			blue += (color >> 16 & 0xFF) * alpha;
			}
		}

		if (total > 0) return FastColor.ARGB32.color( 255, (int)(red / total), (int) (green / total), (int) (blue / total));
		return -1;
	}
}