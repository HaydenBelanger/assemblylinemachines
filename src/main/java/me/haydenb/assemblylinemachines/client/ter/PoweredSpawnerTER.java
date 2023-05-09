package me.haydenb.assemblylinemachines.client.ter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.haydenb.assemblylinemachines.block.machines.BlockPoweredSpawner.TEPoweredSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoweredSpawnerTER implements BlockEntityRendererProvider<TEPoweredSpawner> {

	@Override
	public BlockEntityRenderer<TEPoweredSpawner> create(Context p_173571_) {
		return new BlockEntityRenderer<>() {

			@Override
			public void render(TEPoweredSpawner spawner, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0.5D, 0.0D, 0.5D);

				Entity entity = spawner.getEntityForRender();

				if (entity != null) {
					float f = 0.53125F;
					float f1 = Math.max(entity.getBbWidth(), entity.getBbHeight());
					if (f1 > 1.0D) {
						f /= f1;
					}

					matrixStackIn.translate(0.0D, 0.4F, 0.0D);

					float s = spawner.client_renderRot;
					if(spawner.client_renderRot + 10f >= 360f) {
						spawner.client_renderRot = 0f;
					}else {
						spawner.client_renderRot += 10f;
					}

					matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, s, spawner.client_renderRot)));
					matrixStackIn.translate(0.0D, -0.2F, 0.0D);
					matrixStackIn.mulPose(Axis.XP.rotationDegrees(-30.0F));
					matrixStackIn.scale(f, f, f);
					Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);

				}


				matrixStackIn.popPose();
			}
		};
	}




}