package me.haydenb.assemblylinemachines.client;

import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class GUIHelper {

	public static void fillAreaWithIcon(TextureAtlasSprite icon, int x, int y, int width, int height) {
		Tesselator t = Tesselator.getInstance();
		BufferBuilder b = t.getBuilder();
		b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		float zLevel = 0;

		int iconWidth = icon.contents().width();
		int iconHeight = icon.contents().height();

		// number of rows & cols of full size icons
		int fullCols = width / iconWidth;
		int fullRows = height / iconHeight;

		float minU = icon.getU0();
		float maxU = icon.getU1();
		float minV = icon.getV0();
		float maxV = icon.getV1();

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

		t.end();
	}

	private static void drawRect(float x, float y, float width, float height, float z, float u, float v, float maxU, float maxV) {
		BufferBuilder b = Tesselator.getInstance().getBuilder();

		b.vertex(x, y + height, z).uv(u, maxV).endVertex();
		b.vertex(x + width, y + height, z).uv(maxU, maxV).endVertex();
		b.vertex(x + width, y, z).uv(maxU, v).endVertex();
		b.vertex(x, y, z).uv(u, v).endVertex();
	}
}
