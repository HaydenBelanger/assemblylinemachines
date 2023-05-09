package me.haydenb.assemblylinemachines.client.ter;

import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.joml.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.haydenb.assemblylinemachines.block.machines.BlockQuantumLink.TEQuantumLink;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


//Taken with love from the Ender Gateway renderer.
@OnlyIn(Dist.CLIENT)
public class QuantumLinkTER implements BlockEntityRendererProvider<TEQuantumLink> {

	public static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
	public static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
	private static final Random RANDOM = new Random(31100L);
	private static final List<RenderType> RENDER_TYPES = IntStream.range(0, 16).mapToObj(new IntFunction<RenderType>() {
		@Override
		public RenderType apply(int value) {
			return RenderType.endPortal();
		}
	}).collect(ImmutableList.toImmutableList());
	private static final float MIN = 0.09375f;
	private static final float MAX = 0.90625f;

	@Override
	public BlockEntityRenderer<TEQuantumLink> create(Context ctxt) {
		return new BlockEntityRenderer<>() {

			@Override
			public void render(TEQuantumLink tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
				RANDOM.setSeed(31100L);
				Vec3 v3 = ctxt.getBlockEntityRenderDispatcher().camera.getPosition();
				double d0 = tileEntityIn.getBlockPos().distToCenterSqr(v3.x, v3.y, v3.z);
				int i = getPasses(d0);
				float f = getOffset();
				Matrix4f matrix4f = matrixStackIn.last().pose();
				renderCube(tileEntityIn, f, 0.15F, matrix4f, bufferIn.getBuffer(RENDER_TYPES.get(0)));

				for(int j = 1; j < i; ++j) {
					renderCube(tileEntityIn, f, 2.0F / (18 - j), matrix4f, bufferIn.getBuffer(RENDER_TYPES.get(j)));
				}

			}


		};
	}

	private static void renderCube(TEQuantumLink tileEntityIn, float p_228883_2_, float p_228883_3_, Matrix4f p_228883_4_, VertexConsumer p_228883_5_) {
		float f = (RANDOM.nextFloat() * 0.5F + 0.1F) * p_228883_3_;
		float f1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * p_228883_3_;
		float f2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * p_228883_3_;
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MIN, MAX, MIN, MAX, MAX, MAX, MAX, MAX, f, f1, f2, Direction.SOUTH);
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MIN, MAX, MAX, MIN, MIN, MIN, MIN, MIN, f, f1, f2, Direction.NORTH);
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MAX, MAX, MAX, MIN, MIN, MAX, MAX, MIN, f, f1, f2, Direction.EAST);
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MIN, MIN, MIN, MAX, MIN, MAX, MAX, MIN, f, f1, f2, Direction.WEST);
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MIN, MAX, MIN, MIN, MIN, MIN, MAX, MAX, f, f1, f2, Direction.DOWN);
		renderFace(tileEntityIn, p_228883_4_, p_228883_5_, MIN, MAX, MAX, MAX, MAX, MAX, MIN, MIN, f, f1, f2, Direction.UP);
	}

	private static void renderFace(TEQuantumLink tileEntityIn, Matrix4f p_228884_2_, VertexConsumer p_228884_3_, float p_228884_4_, float p_228884_5_, float p_228884_6_, float p_228884_7_, float p_228884_8_, float p_228884_9_, float p_228884_10_, float p_228884_11_, float p_228884_12_, float p_228884_13_, float p_228884_14_, Direction p_228884_15_) {
		p_228884_3_.vertex(p_228884_2_, p_228884_4_, p_228884_6_, p_228884_8_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
		p_228884_3_.vertex(p_228884_2_, p_228884_5_, p_228884_6_, p_228884_9_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
		p_228884_3_.vertex(p_228884_2_, p_228884_5_, p_228884_7_, p_228884_10_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
		p_228884_3_.vertex(p_228884_2_, p_228884_4_, p_228884_7_, p_228884_11_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
	}

	private static int getPasses(double p_191286_1_) {
		if (p_191286_1_ > 36864.0D) {
			return 1;
		} else if (p_191286_1_ > 25600.0D) {
			return 3;
		} else if (p_191286_1_ > 16384.0D) {
			return 5;
		} else if (p_191286_1_ > 9216.0D) {
			return 7;
		} else if (p_191286_1_ > 4096.0D) {
			return 9;
		} else if (p_191286_1_ > 1024.0D) {
			return 11;
		} else if (p_191286_1_ > 576.0D) {
			return 13;
		} else {
			return p_191286_1_ > 256.0D ? 14 : 15;
		}
	}

	private static float getOffset() {
		return 0.75F;
	}


}
