package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.item.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.DimensionChaosPlane;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.Vec3;

public class FluidCondensedVoid extends ALMFluid {

	public FluidCondensedVoid(boolean source) {
		super(Registry.createFluidProperties("condensed_void", -200, false, true, true), source, 0, 0, 0);
	}

	@Override
	protected int getDropOff(LevelReader worldIn) {
		return 2;
	}
	
	@Override	
	protected void randomTick(Level world, BlockPos pos, FluidState state, Random randomom) {
		if(!world.dimension().location().equals(DimensionChaosPlane.CHAOS_PLANE_LOCATION.location())) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				if(randomom.nextInt(2) == 0) {
					Block block = world.getBlockState(cor).getBlock();
					if(block.getTags().contains(new ResourceLocation("minecraft", "leaves")) || block.getTags().contains(new ResourceLocation("minecraft", "logs"))
							|| block.getTags().contains(new ResourceLocation("minecraft", "flowers")) || block.getTags().contains(new ResourceLocation("minecraft", "planks"))
							|| block.getTags().contains(new ResourceLocation("minecraft", "wool"))
							|| block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.DEAD_BUSH || block == Blocks.FERN || block == Blocks.COARSE_DIRT
							|| block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.DIRT_PATH) {
						world.destroyBlock(cor, false);
					}else if(block.defaultBlockState().getMaterial() == Material.STONE) {
						world.setBlockAndUpdate(cor, Blocks.GRAVEL.defaultBlockState());
					}else if(block == Blocks.WATER) {
						world.setBlockAndUpdate(cor, Blocks.PACKED_ICE.defaultBlockState());
					}else if(block == Blocks.LAVA) {
						world.setBlockAndUpdate(cor, Blocks.OBSIDIAN.defaultBlockState());
					}
				}
					
			}
		}
		
		
		super.randomTick(world, pos, state, randomom);
	}
	
	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 5;
	}
	
	@Override
	public float getFogDensity() {
		return 1f;
	}

	public static class FluidCondensedVoidBlock extends ALMFluidBlock {

		public FluidCondensedVoidBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.getTag("condensed_void"), Material.WATER);
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			
			if(entity instanceof ItemEntity) {
				
				ItemEntity itemEntity = (ItemEntity) entity;
				ItemStack stack = itemEntity.getItem();
				if(stack.getItem() != Registry.getItem("corrupted_shard")) {
					itemEntity.setItem(ItemCorruptedShard.corruptItem(stack));
				}else {
					itemEntity.setPos(itemEntity.xOld + ((worldIn.random.nextDouble() * 2D) - 1D), itemEntity.yOld + ((worldIn.random.nextDouble() * 4D) - 2D), itemEntity.zOld + ((worldIn.random.nextDouble() * 2D) - 1D));
				}
			}else if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
				player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 6));
				
				entity.makeStuckInBlock(state, new Vec3(0.02D, 0.02D, 0.02D));
			}
			super.entityInside(state, worldIn, pos, entity);
		}
	}

}
