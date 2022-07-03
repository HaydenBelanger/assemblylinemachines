package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public class BlockCrank extends Block {

	private static final VoxelShape SHAPE = Stream
			.of(Block.box(0, 0, 0, 4, 16, 16), Block.box(4, 7, 7, 8, 9, 9),
					Block.box(8, 7, 7, 10, 16, 9), Block.box(10, 14, 7, 14, 16, 9))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public BlockCrank() {
		super(Block.Properties.of(Material.METAL).strength(1f, 2f).sound(SoundType.METAL));
		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}



	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

		return Utils.rotateShape(Direction.EAST, state.getValue(HorizontalDirectionalBlock.FACING), SHAPE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		for(Direction d : Utils.CARDINAL_DIRS) {
			BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos().relative(d));

			if(entity instanceof ICrankableMachine crankable) {
				if(crankable.validFrom(d.getOpposite()) && !crankable.requiresGearbox()) {
					return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, d.getOpposite());
				}
			}
		}
		return null;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if(!world.isClientSide) {
			if(handIn.equals(InteractionHand.MAIN_HAND)) {
				BlockEntity te = world.getBlockEntity(pos.relative(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite()));
				if(te != null && te instanceof ICrankableMachine crankable) {
					if(crankable.perform()) {
						world.playSound(null, pos, SoundEvents.WOOD_STEP, SoundSource.BLOCKS, 0.7f, 1f + getPitchNext(world.getRandom()));
					}else {
						if(world.getRandom().nextDouble() < ALMConfig.getServerConfig().crankSnapChance().get()) {
							world.playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 1f, 1f);
							world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
							Utils.spawnItem(new ItemStack(Items.STICK, 5), pos, world);
							Utils.spawnItem(new ItemStack(Registry.getItem("steel_nugget"), 27), pos, world);
						}else {
							world.playSound(null, pos, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.7f, 1f);
						}
					}
				}
			}
		}

		if(world.isClientSide) {
			RandomSource r = world.getRandom();
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartNext(r), pos.getY() + getPartNext(r), pos.getZ() + getPartNext(r), 0, 0, 0);
		}
		return InteractionResult.CONSUME;

	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!worldIn.isClientSide()) {
			if (facing == stateIn.getValue(HorizontalDirectionalBlock.FACING).getOpposite()) {
				if (worldIn.getBlockState(currentPos.relative(facing)).isAir()) {
					return Blocks.AIR.defaultBlockState();
				}
			}
		}

		return stateIn;
	}

	private static double getPartNext(RandomSource rand) {
		double d = rand.nextDouble();
		if(d < 0.2 || d > 0.8) {
			d = 0.5;
		}
		return d;
	}

	private static float getPitchNext(RandomSource rand) {
		float f = rand.nextFloat();

		if(f < 0.6f) {
			f = 0f;
		}

		if(f > 0.3f) {
			f = f * -1f;
		}

		return f;

	}

}
