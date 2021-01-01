package me.haydenb.assemblylinemachines.rendering;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GUIHelper {

	public static void fillAreaWithIcon(TextureAtlasSprite icon, int x, int y, int width, int height) {
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		float zLevel = 0;

		int iconWidth = icon.getWidth();
		int iconHeight = icon.getHeight();

		// number of rows & cols of full size icons
		int fullCols = width / iconWidth;
		int fullRows = height / iconHeight;

		float minU = icon.getMinU();
		float maxU = icon.getMaxU();
		float minV = icon.getMinV();
		float maxV = icon.getMaxV();

		int excessWidth = width % iconWidth;
		int excessHeight = height % iconHeight;

		// interpolated max u/v for the excess row / col
		float partialMaxU = minU + (maxU - minU) * ((float) excessWidth / iconWidth);
		float partialMaxV = minV + (maxV - minV) * ((float) excessHeight / iconHeight);

		int xNow;
		int yNow;
		for (int row = 0; row < fullRows; row++) {
			yNow = y + row * iconHeight;
			for (int col = 0; col < fullCols; col++) {
				// main part, only full icons
				xNow = x + col * iconWidth;
				drawRect(xNow, yNow, iconWidth, iconHeight, zLevel, minU, minV, maxU, maxV);
			}
			if (excessWidth != 0) {
				// last not full width column in every row at the end
				xNow = x + fullCols * iconWidth;
				drawRect(xNow, yNow, iconWidth, iconHeight, zLevel, minU, minV, maxU, maxV);
			}
		}
		if (excessHeight != 0) {
			// last not full height row
			for (int col = 0; col < fullCols; col++) {
				xNow = x + col * iconWidth;
				yNow = y + fullRows * iconHeight;
				drawRect(xNow, yNow, iconWidth, excessHeight, zLevel, minU, minV, maxU, partialMaxV);
			}
			if (excessWidth != 0) {
				// missing quad in the bottom right corner of neither full height nor full width
				xNow = x + fullCols * iconWidth;
				yNow = y + fullRows * iconHeight;
				drawRect(xNow, yNow, excessWidth, excessHeight, zLevel, minU, minV, partialMaxU, partialMaxV);
			}
		}

		t.draw();
	}

	private static void drawRect(float x, float y, float width, float height, float z, float u, float v, float maxU, float maxV) {
		BufferBuilder b = Tessellator.getInstance().getBuffer();

		b.pos(x, y + height, z).tex(u, maxV).endVertex();
		b.pos(x + width, y + height, z).tex(maxU, maxV).endVertex();
		b.pos(x + width, y, z).tex(maxU, v).endVertex();
		b.pos(x, y, z).tex(u, v).endVertex();
	}
}
