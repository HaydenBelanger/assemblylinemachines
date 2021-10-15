package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.FluidRegistration;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class FluidNaphtha extends ALMFluid {

	public FluidNaphtha(boolean source) {
		super(FluidRegistration.buildProperties("naphtha", 2200, false, true, true), source);
	}

	@Override
	protected void randomTick(World world, BlockPos pos, FluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
		while(iter.hasNext()) {
			
			BlockPos cor = iter.next();
			
			if(world.getBlockState(cor).getBlock() == Blocks.AIR && (world.isBlockPresent(cor.down()) || isSurroundingBlockFlammable(world, cor)) && !world.getBlockState(cor.down()).getBlock().getTags().contains(new ResourceLocation("assemblylinemachines", "world/naphtha_fireproof"))) {
				
				world.setBlockState(cor, ForgeEventFactory.fireFluidPlaceBlockEvent(world, cor, pos, Registry.getBlock("naphtha_fire").getDefaultState()));
				
			}
				
		}
	}
	
	@Override
	protected boolean ticksRandomly() {
		return true;
	}

	private boolean isSurroundingBlockFlammable(IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (this.getCanBlockBurn(worldIn, pos.offset(direction))) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean getCanBlockBurn(IWorldReader worldIn, BlockPos pos) {
		return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.isBlockLoaded(pos) ? false
				: worldIn.getBlockState(pos).getMaterial().isFlammable();
	}

	@Override
	public int getTickRate(IWorldReader world) {
		return 4;
	}

	public static class FluidNaphthaBlock extends ALMFluidBlock {

		public FluidNaphthaBlock() {
			super("naphtha", ALMFluid.NAPHTHA, Block.Properties.create(Material.LAVA).hardnessAndResistance(100f).setLightLevel((state) -> 11).noDrops());
		}
		
		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			
			if(entity instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entity;
				living.addPotionEffect(new EffectInstance(Registry.getEffect("deep_burn"), 300, 0));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
		
	}

}
