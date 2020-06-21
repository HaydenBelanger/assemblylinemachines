package me.haydenb.assemblylinemachines.rendering;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import me.haydenb.assemblylinemachines.block.BlockFluidTank.TEFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class TankTER extends TileEntityRenderer<TEFluidTank> {

	public TankTER(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TEFluidTank tank, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedLight, int combinedOverlayIn) {

		matrixStack.push(); // push the current transformation matrix + normals matrix
		if (!tank.fluid.isEmpty()) {
			TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
					.apply(tank.fluid.getFluid().getAttributes().getStillTexture());

			IVertexBuilder builder = renderBuffer.getBuffer(RenderType.getTranslucent());

			Vector4f vec;
			if(tank.fluid.getFluid().equals(Fluids.WATER)) {
				vec = getColorVec(BiomeColors.getWaterColor(tank.getWorld(), tank.getPos()));
			}else {
				vec = new Vector4f(1f, 1f, 1f, 1f);
			}
			float endPoint = 0.0625f + (((float) tank.fluid.getAmount() / (float) tank.capacity) * 0.875f);
			
			add(builder, matrixStack, vec, 0 + 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getMinU(), sprite.getMinV());
			add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getMaxU(), sprite.getMinV());
			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getMaxU(), sprite.getInterpolatedV(endPoint * 16));
			add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 1 - 0.0625f, sprite.getMinU(), sprite.getInterpolatedV(endPoint * 16));

			add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 0.0625f, sprite.getMinU(), sprite.getInterpolatedV(endPoint * 16));
			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0.0625f, sprite.getMaxU(), sprite.getInterpolatedV(endPoint * 16));
			add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 0.0625f, sprite.getMaxU(), sprite.getMinV());
			add(builder, matrixStack, vec, 0 + 0.0625f, 0 + 0.0625f, 0.0625f, sprite.getMinU(), sprite.getMinV());

			add(builder, matrixStack, vec, 0.0625f, 0 + 0.0625f, 0 + 0.0625f, sprite.getMaxU(), sprite.getMaxV());
			add(builder, matrixStack, vec, 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getMinU(), sprite.getMaxV());
			add(builder, matrixStack, vec, 0.0625f, endPoint, 1 - 0.0625f, sprite.getMinU(), sprite.getInterpolatedV(16 - endPoint * 16));
			add(builder, matrixStack, vec, 0.0625f, endPoint, 0 + 0.0625f, sprite.getMaxU(), sprite.getInterpolatedV(16 - endPoint * 16));

			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0 + 0.0625f, sprite.getMinU(), sprite.getInterpolatedV(endPoint * 16));
			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getMaxU(), sprite.getInterpolatedV(endPoint * 16));
			add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getMaxU(), sprite.getMinV());
			add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 0 + 0.0625f, sprite.getMinU(), sprite.getMinV());

			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0 + 0.0625f, sprite.getMinU(), sprite.getMaxV());
			add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 0 + 0.0625f, sprite.getMaxU(), sprite.getMaxV());
			add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 1 - 0.0625f, sprite.getMaxU(), sprite.getMinV());
			add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getMinU(), sprite.getMinV());

		}
		matrixStack.pop();

	}

	private void add(IVertexBuilder renderer, MatrixStack stack, Vector4f colors, float x, float y, float z, float u, float v) {
		renderer.pos(stack.getLast().getMatrix(), x, y, z).color(colors.getX(), colors.getY(), colors.getZ(), colors.getW()).tex(u, v).lightmap(0, 240).normal(1, 0, 0).endVertex();
	}
	
	
	private Vector4f getColorVec(int color) {
		
		Color c = new Color(color);
		
		return new Vector4f((float)c.getRed()/255f, (float)c.getGreen()/255f, (float)c.getBlue()/255f, 1f);
	}
}
