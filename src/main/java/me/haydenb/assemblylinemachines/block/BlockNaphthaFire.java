package me.haydenb.assemblylinemachines.block;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockNaphthaFire extends BaseFireBlock implements IBlockWithHarvestableTags {
	public BlockNaphthaFire() {
		super(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().randomTicks()
				.strength(0f).lightLevel((state) -> 15).sound(SoundType.WOOL).noDrops(), 1f);
		this.registerDefaultState(this.stateDefinition.any().setValue(FireBlock.AGE, 0));
	}
	
	@Override
	public Named<Block> getToolType() {
		return null;
	}


	@Override
	public Named<Block> getToolLevel() {
		return null;
	}


	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FireBlock.AGE);
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rand) {
		if (world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
			if (!world.isAreaLoaded(pos, 2)) return;
			if (!state.canSurvive(world, pos)) {
				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				return;
			}

			int age = state.getValue(FireBlock.AGE);
			if(age < 15) {

				Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();


				while(iter.hasNext()) {
					BlockPos posx = iter.next();
					if (rand.nextInt(5) == 0) {
						if(world.getBlockState(posx).getBlock() == Blocks.AIR) {
							int nAge = age + 2 + rand.nextInt(6);
							int xAge = age + 1 + rand.nextInt(4);
							if(nAge > 15) nAge = 15;
							if(xAge > 15) xAge = 15;
							if(world.isLoaded(posx.below()) && !world.getBlockState(posx.below()).getBlock().getTags().contains(new ResourceLocation("assemblylinemachines", "world/naphtha_fireproof"))) {
								world.setBlockAndUpdate(posx, ForgeEventFactory.fireFluidPlaceBlockEvent(world, posx, pos, state.setValue(FireBlock.AGE, nAge)));
								
								world.setBlockAndUpdate(pos, state.setValue(FireBlock.AGE, xAge));
								
								if(xAge == 15) {
									break;
								}
							}

						}

					}
				}

			}
		}
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {

		if(entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			living.addEffect(new MobEffectInstance(Registry.getEffect("deep_burn"), 140, 0));
		}
		
		super.entityInside(state, worldIn, pos, entity);
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.canSurvive(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
	}



	@Override
	public boolean isBurning(BlockState state, BlockGetter world, BlockPos pos) {
		return true;
	}

	@Override
	protected boolean canBurn(BlockState p_196446_1_) {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.below()).getBlock();
		if(block != this && block != Blocks.AIR && !(block instanceof LiquidBlock)) {
			return true;
		}
		return false;
	}

}
