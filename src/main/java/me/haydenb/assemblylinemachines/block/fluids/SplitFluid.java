package me.haydenb.assemblylinemachines.block.fluids;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.math.Vector3f;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent.RenderFog;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class SplitFluid extends ForgeFlowingFluid {

	protected final boolean source;
	private final int maxLevel;

	public SplitFluid(boolean source, Properties properties) {
		this(source, 8, properties);
	}

	public SplitFluid(boolean source, int maxLevel, Properties properties) {
		super(properties);
		this.source = source;
		this.maxLevel = maxLevel;

		if(!source) registerDefaultState(getStateDefinition().any().setValue(LEVEL, maxLevel - 1));
	}

	@Override
	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder) {
		super.createFluidStateDefinition(builder);
		if(!source) builder.add(LEVEL);
	}

	@Override
	public int getAmount(FluidState state) {
		return source ? maxLevel : state.getValue(LEVEL);
	}

	@Override
	public boolean isSource(FluidState p_76140_) {
		return source;
	}

	public static class GasFluid extends EmptyFluid{

		private final Lazy<FluidType> fluidType;

		public GasFluid(String fluidType) {
			this(Lazy.of(() -> Registry.getFluidType(fluidType)));
		}

		public GasFluid(Lazy<FluidType> fluidType) {
			this.fluidType = fluidType;
		}

		@Override
		public FluidType getFluidType() {
			return fluidType.get();
		}

		@Override
		protected boolean isEmpty() {
			return false;
		}
	}

	public static LiquidBlock effectLiquidBlock(String name, Supplier<List<MobEffectInstance>> effects) {
		return effectLiquidBlock(name, effects, Block.Properties.of(Material.WATER).noLootTable());
	}

	public static LiquidBlock effectLiquidBlock(String name, Supplier<List<MobEffectInstance>> effects, Block.Properties blockProperties) {

		class EffectLiquidBlock extends LiquidBlock{

			public EffectLiquidBlock() {
				super(() -> (FlowingFluid) Registry.getFluid(name), blockProperties);
			}

			@Override
			public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
				if(entity instanceof LivingEntity living) {
					for(MobEffectInstance mei : effects.get()) {
						if(!living.hasEffect(mei.getEffect())) {
							living.addEffect(mei);
						}
					}
				}
			}
		}

		return new EffectLiquidBlock();
	}

	public static class SpecialRenderFluidType extends FluidType{

		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;

		private Vector3f colorF = null;
		public Vec3i colorI = null;
		
		private Supplier<Float> fogDepthProvider = null;

		public SpecialRenderFluidType(Properties properties, String textureName, boolean hasFlowing) {
			super(properties);
			stillTexture = new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + textureName);
			flowingTexture = hasFlowing ? new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + textureName + "_flowing") : null;
		}

		public SpecialRenderFluidType color(int red, int green, int blue) {
			this.colorF = new Vector3f(red / 255f, green / 255f, blue / 255f);
			this.colorI = new Vec3i(red, green, blue);
			return this;
		}

		public SpecialRenderFluidType fog(float fogDepth) {
			return fog(() -> fogDepth);
		}

		public SpecialRenderFluidType fog(Supplier<Float> fogDepthProvider) {
			this.fogDepthProvider = fogDepthProvider;
			return this;
		}

		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				@Override
				public ResourceLocation getStillTexture() {
					return stillTexture;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return flowingTexture;
				}

				@Override
				public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
						int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
					return colorF != null ? colorF : fluidFogColor;
				}
			});
		}

		@EventBusSubscriber(value = {Dist.CLIENT}, modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
		@OnlyIn(Dist.CLIENT)
		public static class FogRenderer {

			@SubscribeEvent
			@OnlyIn(Dist.CLIENT)
			public static void renderFog(RenderFog event) {
				FluidType rType = event.getCamera().getBlockAtCamera().getFluidState().getFluidType();
				if(rType instanceof SpecialRenderFluidType type) {
					event.setCanceled(true);
					event.setNearPlaneDistance(-8f);
					event.setFarPlaneDistance(type.fogDepthProvider != null ? type.fogDepthProvider.get() : 24f);
					event.setFogShape(FogShape.SPHERE);
				}

			}

		}
	}



}
