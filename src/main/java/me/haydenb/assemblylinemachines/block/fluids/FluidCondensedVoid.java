package me.haydenb.assemblylinemachines.block.fluids;

import java.util.Iterator;

import me.haydenb.assemblylinemachines.item.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.chaosplane.ChaosPlaneDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class FluidCondensedVoid extends SplitFluid {

	public FluidCondensedVoid(boolean source) {
		super(source, Registry.basicFFFProperties("condensed_void"));
	}

	@Override
	protected int getDropOff(LevelReader worldIn) {
		return 2;
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
	protected void randomTick(Level world, BlockPos pos, FluidState state, RandomSource randomom) {
		if(!world.dimension().location().equals(ChaosPlaneDimension.CHAOS_PLANE_LOCATION.location())) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				if(randomom.nextInt(2) == 0) {
					Block block = world.getBlockState(cor).getBlock();
					if(world.getBlockState(cor).getDestroySpeed(world, cor) != -1f) {
						BlockState bst = world.getBlockState(cor);
						if(bst.is(TagKey.create(Keys.BLOCKS, new ResourceLocation("minecraft", "leaves"))) || bst.is(TagKey.create(Keys.BLOCKS, new ResourceLocation("minecraft", "logs")))
								|| bst.is(TagKey.create(Keys.BLOCKS, new ResourceLocation("minecraft", "flowers"))) || bst.is(TagKey.create(Keys.BLOCKS, new ResourceLocation("minecraft", "planks")))
								|| bst.is(TagKey.create(Keys.BLOCKS, new ResourceLocation("minecraft", "wool")))
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
		}


		super.randomTick(world, pos, state, randomom);
	}

	public static class FluidCondensedVoidBlock extends LiquidBlock {

		public FluidCondensedVoidBlock() {
			super(() -> (FlowingFluid) Registry.getFluid("condensed_void"), Block.Properties.of(Material.WATER).noLootTable());
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {

			if(entity instanceof ItemEntity itemEntity) {
				ItemStack stack = itemEntity.getItem();
				if(stack.getItem() != Registry.getItem("corrupted_shard")) {
					itemEntity.setItem(ItemCorruptedShard.corruptItem(stack));
				}else {
					itemEntity.setPos(itemEntity.xOld + ((worldIn.random.nextDouble() * 2D) - 1D), itemEntity.yOld + ((worldIn.random.nextDouble() * 4D) - 2D), itemEntity.zOld + ((worldIn.random.nextDouble() * 2D) - 1D));
				}
			}else if (entity instanceof LivingEntity player) {
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
				player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 6));
			}
		}
	}

}
