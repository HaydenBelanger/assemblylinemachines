package me.haydenb.assemblylinemachines.block;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockNaphthaFire extends AbstractFireBlock {
	public BlockNaphthaFire() {
		super(Block.Properties.create(Material.FIRE, MaterialColor.TNT).doesNotBlockMovement().tickRandomly()
				.hardnessAndResistance(0f).func_235838_a_((state) -> 15).sound(SoundType.CLOTH).noDrops(), 1f);
		this.setDefaultState(this.stateContainer.getBaseState().with(FireBlock.AGE, 0));
	}


	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FireBlock.AGE);
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
			if (!world.isAreaLoaded(pos, 2)) return;
			if (!state.isValidPosition(world, pos)) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				return;
			}

			int age = state.get(FireBlock.AGE);
			if(age < 15) {

				Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();


				while(iter.hasNext()) {
					BlockPos posx = iter.next();
					if (rand.nextInt(5) == 0) {
						if(world.getBlockState(posx).getBlock() == Blocks.AIR) {
							int nAge = age + 2 + rand.nextInt(6);
							int xAge = age + 1 + rand.nextInt(4);
							if(nAge > 15) nAge = 15;
							if(xAge > 15) xAge = 15;
							if(world.isBlockPresent(posx.down()) && !world.getBlockState(posx.down()).getBlock().getTags().contains(new ResourceLocation("assemblylinemachines", "world/naphtha_fireproof"))) {
								world.setBlockState(posx, ForgeEventFactory.fireFluidPlaceBlockEvent(world, posx, pos, state.with(FireBlock.AGE, nAge)));
								
								world.setBlockState(pos, state.with(FireBlock.AGE, xAge));
								
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
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {

		if(entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			living.addPotionEffect(new EffectInstance(Registry.getEffect("deep_burn"), 140, 0));
		}
		
		super.onEntityCollision(state, worldIn, pos, entity);
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.isValidPosition(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.getDefaultState();
	}



	@Override
	public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
		return true;
	}

	@Override
	protected boolean canBurn(BlockState p_196446_1_) {
		return true;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.down()).getBlock();
		if(block != this && block != Blocks.AIR && !(block instanceof FlowingFluidBlock)) {
			return true;
		}
		return false;
	}

}
