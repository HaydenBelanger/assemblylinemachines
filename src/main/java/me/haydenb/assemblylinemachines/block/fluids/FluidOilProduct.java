package me.haydenb.assemblylinemachines.block.fluids;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;

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
		
		if(source && ConfigHolder.getCommonConfig().gasolineExplosions.get()) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.offset(-3, -1, -3).north().west(), pos.offset(3, 1, 3)).iterator();
			
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				
				if(Utils.isInTag(world.getBlockState(cor), new ResourceLocation("assemblylinemachines", "world/gas_flammable"))) {
					if(world.getRandom().nextInt(3) == 0) {
						world.explode(null, cor.getX(), cor.getY() + 1, cor.getZ(), breakAndBreakConnected(world, state, cor), true, BlockInteraction.BREAK);
						
					}
				}
			}
		}
	}
	
	private float breakAndBreakConnected(Level world, FluidState origState, BlockPos posx) {
		world.setBlockAndUpdate(posx, Blocks.AIR.defaultBlockState());
		
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(posx.below().north().west(), posx.above().south().east()).iterator();
		
		float pow = 2;
		while(iter.hasNext()) {
			BlockPos posq = iter.next();
			
			FluidState fs = world.getFluidState(posq);
			if(fs.getType() == origState.getType()) {
				pow = pow + breakAndBreakConnected(world, origState, posq);
			}
		}
		
		return pow;
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
