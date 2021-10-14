package me.haydenb.assemblylinemachines.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MathHelper {
	
	
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
	
}
