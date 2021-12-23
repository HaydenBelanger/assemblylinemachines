package me.haydenb.assemblylinemachines.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.haydenb.assemblylinemachines.block.machines.mob.BlockPoweredSpawner.TEPoweredSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PoweredSpawnerTER extends TileEntityRenderer<TEPoweredSpawner> {
	public PoweredSpawnerTER(TileEntityRendererDispatcher p_i226016_1_) {
		super(p_i226016_1_);
	}

	public void render(TEPoweredSpawner spawner, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		matrixStackIn.push();
		matrixStackIn.translate(0.5D, 0.0D, 0.5D);

		Entity entity = spawner.getEntityForRender();

		if (entity != null) {
			float f = 0.53125F;
			float f1 = Math.max(entity.getWidth(), entity.getHeight());
			if ((double)f1 > 1.0D) {
				f /= f1;
			}

			matrixStackIn.translate(0.0D, (double)0.4F, 0.0D);
			
			float s = spawner.client_renderRot;
			if(spawner.client_renderRot + 10f >= 360f) {
				spawner.client_renderRot = 0f;
			}else {
				spawner.client_renderRot += 10f;
			}
			
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, s, spawner.client_renderRot)));
			matrixStackIn.translate(0.0D, (double)-0.2F, 0.0D);
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-30.0F));
			matrixStackIn.scale(f, f, f);
			Minecraft.getInstance().getRenderManager().renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrixStackIn, bufferIn, combinedLightIn);

		}


		matrixStackIn.pop();
	}
}