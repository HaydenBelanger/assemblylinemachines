package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.FluidRegistration;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;

public class FluidOilProduct extends ALMFluid {

	public FluidOilProduct(String name, boolean source) {
		super(FluidRegistration.buildProperties(name, 350, false, true, true), source);
	}
	
	@Override
	protected boolean ticksRandomly() {
		return true;
	}
	
	@Override
	protected void randomTick(World world, BlockPos pos, IFluidState state, Random random) {
		
		if(source) {
			Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.add(-3, -1, -3).north().west(), pos.add(3, 1, 3)).iterator();
			
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				
				Block block = world.getBlockState(cor).getBlock();
				if(block.getTags().contains(new ResourceLocation("assemblylinemachines", "world/gas_flammable"))) {
					if(General.RAND.nextInt(3) == 0) {
						world.createExplosion(null, cor.getX(), cor.getY() + 1, cor.getZ(), breakAndBreakConnected(world, state, cor), true, Mode.BREAK);
						
					}
				}
			}
		}
	}
	
	private float breakAndBreakConnected(World world, IFluidState origState, BlockPos posx) {
		world.setBlockState(posx, Blocks.AIR.getDefaultState());
		
		Iterator<BlockPos> iter = BlockPos.getAllInBox(posx.down().north().west(), posx.up().south().east()).iterator();
		
		float pow = 2;
		while(iter.hasNext()) {
			BlockPos posq = iter.next();
			
			IFluidState fs = world.getFluidState(posq);
			if(fs.getFluid() == origState.getFluid()) {
				pow = pow + breakAndBreakConnected(world, origState, posq);
			}
		}
		
		return pow;
	}
	
	public static class FluidOilProductBlock extends ALMFluidBlock {

		public FluidOilProductBlock(String name) {
			super(name, ALMFluid.OIL_BYPRODUCT, Material.WATER);
		}

		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addPotionEffect(new EffectInstance(Effects.POISON, 60, 2));
				player.addPotionEffect(new EffectInstance(Effects.HUNGER, 60, 3));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
	}

}
