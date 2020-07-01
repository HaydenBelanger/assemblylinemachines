package me.haydenb.assemblylinemachines.util;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MathHelper {
	
	
	//Claimed from ItemRenderer#renderItemOverlayIntoGUI, modified.
	@OnlyIn(Dist.CLIENT)
	public static void renderItemSlotBoundScaledText(FontRenderer fr, int xpos, int ypos, float scale, String text) {
		
		xpos = Math.round(xpos * (1.0f / scale));
		ypos = Math.round(ypos * (1.0f / scale));
		MatrixStack mx = new MatrixStack();
		
		mx.translate(0.0D, 0.0D, 200D);
		mx.scale(scale, scale, scale);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		fr.renderString(text, (float) (xpos + 19 - 2 - fr.getStringWidth(text)), (float) (ypos + 6 + 3), 16777215, true, mx.getLast().getMatrix(),
				irendertypebuffer$impl, false, 0, 15728880);
		
		irendertypebuffer$impl.finish();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void renderScaledText(FontRenderer fr, int xpos, int ypos, float scale, String text, boolean dropShadow, int color) {
		
		xpos = Math.round(xpos * (1.0f / scale));
		ypos = Math.round(ypos * (1.0f / scale));
		MatrixStack mx = new MatrixStack();
		
		mx.translate(0.0D, 0.0D, 200D);
		mx.scale(scale, scale, scale);
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		fr.renderString(text, (float) xpos, ypos, color, dropShadow, mx.getLast().getMatrix(),
				irendertypebuffer$impl, false, 0, 15728880);
		irendertypebuffer$impl.finish();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void renderScaledText(FontRenderer fr, int xpos, int ypos, float scale, String text) {
		
		renderScaledText(fr, xpos, ypos, scale, text);
	}
}
