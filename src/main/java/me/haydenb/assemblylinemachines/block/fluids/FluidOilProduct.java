package me.haydenb.assemblylinemachines.block.fluids;

import java.util.*;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class FluidOilProduct extends ALMFluid {
	
	public FluidOilProduct(String name, boolean source) {
		super(Registry.createFluidProperties(name, 350, false, true, true), source, getRGBFromFluidName(name));
	}
	
	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}
	
	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState state, Random random) {
		
		if(source && ALMConfig.getServerConfig().gasolineExplosions().get()) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.offset(-3, -1, -3).north().west(), pos.offset(3, 1, 3)).iterator();
			
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				
				if(world.getBlockState(cor).is(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "gas_flammable")))) {
					if(world.getRandom().nextInt(3) == 0) {
						float pow = (float) Utils.breakConnected(world, Either.left(state), cor, Optional.empty()) * 2f;
						world.explode(null, cor.getX(), cor.getY() + 1, cor.getZ(), pow, true, BlockInteraction.BREAK);
						
					}
				}
			}
		}
	}
	
	private static int[] getRGBFromFluidName(String name) {
		if(name.equalsIgnoreCase("gasoline")) {
			return new int[] {122, 104, 0};
		}else if(name.equalsIgnoreCase("diesel")) {
			return new int[] {82, 69, 0};
		}else if(name.equalsIgnoreCase("liquid_carbon")) {
			return new int[] {110, 110, 110};
		}else {
			return new int[] {0, 0, 0};
		}
	}
	
	public static class FluidOilProductBlock extends ALMFluidBlock {

		public FluidOilProductBlock(Supplier<? extends FlowingFluid> fluid, String name) {
			super(fluid, ALMFluid.getTag(name), Material.WATER);
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2));
				player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 3));
			}
			super.entityInside(state, worldIn, pos, entity);
		}
	}

}
