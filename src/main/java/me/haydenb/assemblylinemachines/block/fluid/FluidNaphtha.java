package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.registry.FluidRegistry;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraftforge.event.ForgeEventFactory;

public class FluidNaphtha extends ALMFluid {

	public FluidNaphtha(boolean source) {
		super(FluidRegistry.buildProperties("naphtha", 2200, false, true, true), source);
	}

	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
		while(iter.hasNext()) {
			
			BlockPos cor = iter.next();
			
			if(world.getBlockState(cor).getBlock() == Blocks.AIR && (world.isLoaded(cor.below()) || isSurroundingBlockFlammable(world, cor)) && !world.getBlockState(cor.below()).getBlock().getTags().contains(new ResourceLocation("assemblylinemachines", "world/naphtha_fireproof"))) {
				
				world.setBlockAndUpdate(cor, ForgeEventFactory.fireFluidPlaceBlockEvent(world, cor, pos, Registry.getBlock("naphtha_fire").defaultBlockState()));
				
			}
				
		}
	}
	
	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}

	private static boolean isSurroundingBlockFlammable(LevelReader worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (getCanBlockBurn(worldIn, pos.relative(direction))) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private static boolean getCanBlockBurn(LevelReader worldIn, BlockPos pos) {
		return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.hasChunkAt(pos) ? false
				: worldIn.getBlockState(pos).getMaterial().isFlammable();
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 4;
	}

	public static class FluidNaphthaBlock extends ALMFluidBlock {

		public FluidNaphthaBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.NAPHTHA, Block.Properties.of(Material.LAVA).strength(100f).lightLevel((state) -> 11).noDrops());
		}
		
		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			
			if(entity instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entity;
				living.addEffect(new MobEffectInstance(Registry.getEffect("deep_burn"), 300, 0));
			}
			super.entityInside(state, worldIn, pos, entity);
		}
		
		
	}

}
