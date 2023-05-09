package me.haydenb.assemblylinemachines.client.ter;

import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.haydenb.assemblylinemachines.block.machines.BlockFluidTank.TEFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;


@OnlyIn(Dist.CLIENT)
public class TankTER implements BlockEntityRendererProvider<TEFluidTank> {

	@Override
	public BlockEntityRenderer<TEFluidTank> create(Context p_173571_) {

		return new BlockEntityRenderer<>() {

			@Override
			public void render(TEFluidTank tank, float partialTicks, PoseStack matrixStack, MultiBufferSource renderBuffer, int combinedLight, int combinedOverlayIn) {
				matrixStack.pushPose(); // push the current transformation matrix + normals matrix
				if (!tank.fluid.isEmpty()) {
					TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
							.apply(IClientFluidTypeExtensions.of(tank.fluid.getFluid()).getStillTexture());

					VertexConsumer builder = renderBuffer.getBuffer(RenderType.translucent());

					Vector4f vec;
					if(tank.fluid.getFluid().equals(Fluids.WATER)) {
						vec = getColorVec(BiomeColors.getAverageWaterColor(tank.getLevel(), tank.getBlockPos()));
					}else {
						vec = new Vector4f(1f, 1f, 1f, 1f);
					}
					float endPoint = 0.0625f + (((float) Math.min(tank.fluid.getAmount(), tank.block.capacity) / (float) tank.block.capacity) * 0.875f);

					add(builder, matrixStack, vec, 0 + 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getU0(), sprite.getV0());
					add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getU1(), sprite.getV0());
					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getU1(), sprite.getV(endPoint * 16));
					add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 1 - 0.0625f, sprite.getU0(), sprite.getV(endPoint * 16));

					add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 0.0625f, sprite.getU0(), sprite.getV(endPoint * 16));
					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0.0625f, sprite.getU1(), sprite.getV(endPoint * 16));
					add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 0.0625f, sprite.getU1(), sprite.getV0());
					add(builder, matrixStack, vec, 0 + 0.0625f, 0 + 0.0625f, 0.0625f, sprite.getU0(), sprite.getV0());

					add(builder, matrixStack, vec, 0.0625f, 0 + 0.0625f, 0 + 0.0625f, sprite.getU1(), sprite.getV1());
					add(builder, matrixStack, vec, 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getU0(), sprite.getV1());
					add(builder, matrixStack, vec, 0.0625f, endPoint, 1 - 0.0625f, sprite.getU0(), sprite.getV(16 - endPoint * 16));
					add(builder, matrixStack, vec, 0.0625f, endPoint, 0 + 0.0625f, sprite.getU1(), sprite.getV(16 - endPoint * 16));

					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0 + 0.0625f, sprite.getU0(), sprite.getV(endPoint * 16));
					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getU1(), sprite.getV(endPoint * 16));
					add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 1 - 0.0625f, sprite.getU1(), sprite.getV0());
					add(builder, matrixStack, vec, 1 - 0.0625f, 0 + 0.0625f, 0 + 0.0625f, sprite.getU0(), sprite.getV0());

					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 0 + 0.0625f, sprite.getU0(), sprite.getV1());
					add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 0 + 0.0625f, sprite.getU1(), sprite.getV1());
					add(builder, matrixStack, vec, 0 + 0.0625f, endPoint, 1 - 0.0625f, sprite.getU1(), sprite.getV0());
					add(builder, matrixStack, vec, 1 - 0.0625f, endPoint, 1 - 0.0625f, sprite.getU0(), sprite.getV0());

				}
				matrixStack.popPose();

			}
		};
	}

	private static void add(VertexConsumer renderer, PoseStack stack, Vector4f colors, float x, float y, float z, float u, float v) {
		renderer.vertex(stack.last().pose(), x, y, z).color(colors.x(), colors.y(), colors.z(), colors.w()).uv(u, v).uv2(0, 240).normal(1, 0, 0).endVertex();
	}


	private static Vector4f getColorVec(int color) {

		return new Vector4f(ARGB32.red(color)/255f, ARGB32.green(color)/255f, ARGB32.blue(color)/255f, 1f);
	}
}
