package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.item.items.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.FluidRegistration;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class FluidCondensedVoid extends ALMFluid {

	public FluidCondensedVoid(boolean source) {
		super(FluidRegistration.buildProperties("condensed_void", -200, false, true, true), source);
	}

	@Override
	protected int getLevelDecreasePerBlock(IWorldReader worldIn) {
		return 2;
	}
	
	@Override
	protected void randomTick(World world, BlockPos pos, FluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
		while(iter.hasNext()) {
			BlockPos cor = iter.next();
			if(random.nextInt(2) == 0) {
				Block block = world.getBlockState(cor).getBlock();
				if(block.getTags().contains(new ResourceLocation("minecraft", "leaves")) || block.getTags().contains(new ResourceLocation("minecraft", "logs"))
						|| block.getTags().contains(new ResourceLocation("minecraft", "flowers")) || block.getTags().contains(new ResourceLocation("minecraft", "planks"))
						|| block.getTags().contains(new ResourceLocation("minecraft", "wool"))
						|| block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.DEAD_BUSH || block == Blocks.FERN || block == Blocks.COARSE_DIRT
						|| block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.GRASS_PATH) {
					world.destroyBlock(cor, false);
				}else if(block.getDefaultState().getMaterial() == Material.ROCK) {
					world.setBlockState(cor, Blocks.GRAVEL.getDefaultState());
				}else if(block == Blocks.WATER) {
					world.setBlockState(cor, Blocks.PACKED_ICE.getDefaultState());
				}else if(block == Blocks.LAVA) {
					world.setBlockState(cor, Blocks.OBSIDIAN.getDefaultState());
				}
			}
				
		}
		
		super.randomTick(world, pos, state, random);
	}
	
	@Override
	protected boolean ticksRandomly() {
		return true;
	}

	@Override
	public int getTickRate(IWorldReader world) {
		return 5;
	}

	public static class FluidCondensedVoidBlock extends ALMFluidBlock {

		public FluidCondensedVoidBlock() {
			super("condensed_void", ALMFluid.CONDENSED_VOID, Material.WATER);
		}

		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			
			if(entity instanceof ItemEntity) {
				
				ItemEntity itemEntity = (ItemEntity) entity;
				ItemStack stack = itemEntity.getItem();
				if(stack.getItem() != Registry.getItem("corrupted_shard")) {
					itemEntity.setItem(ItemCorruptedShard.corruptItem(stack));
				}else {
					itemEntity.setPositionAndUpdate(itemEntity.lastTickPosX + ((worldIn.rand.nextDouble() * 2D) - 1D), itemEntity.lastTickPosY + ((worldIn.rand.nextDouble() * 4D) - 2D), itemEntity.lastTickPosZ + ((worldIn.rand.nextDouble() * 2D) - 1D));
				}
			}else if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100));
				player.addPotionEffect(new EffectInstance(Effects.WITHER, 40, 6));
				
				entity.setMotionMultiplier(state, new Vector3d(0.02D, 0.02D, 0.02D));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
	}

}
