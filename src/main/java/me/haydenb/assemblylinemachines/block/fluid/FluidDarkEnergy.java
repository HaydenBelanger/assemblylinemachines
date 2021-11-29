package me.haydenb.assemblylinemachines.block.fluid;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.client.FogRendering.ILiquidFogColor;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidDarkEnergy extends ALMFluid implements ILiquidFogColor {
	
	public FluidDarkEnergy(boolean source) {
		super(Registry.createFluidProperties("dark_energy", -100, false, true, true), source, 69, 69, 69);
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 3;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public float getFogDensity(LocalPlayer player) {
		return player.getInventory().getArmor(EquipmentSlot.HEAD.getIndex()).is(Registry.getItem("chaotic_reduction_goggles")) ? 96f : 12f;
	}
	
	public static class FluidDarkEnergyBlock extends ALMFluidBlock {

		public FluidDarkEnergyBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.getTag("dark_energy"), Material.WATER);
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				if(!player.hasEffect(Registry.getEffect("dark_expulsion"))) {
					player.addEffect(new MobEffectInstance(Registry.getEffect("dark_expulsion"), 129, 0, false, false, true));
				}
				
			}
			super.entityInside(state, worldIn, pos, entity);
		}
	}

}
