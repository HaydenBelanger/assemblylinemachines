package me.haydenb.assemblylinemachines.block.misc;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.TagMaster;
import me.haydenb.assemblylinemachines.registry.datagen.TagMaster.IMiningLevelDataGenProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.*;
import net.minecraftforge.registries.ForgeRegistries;

public class CorruptBlock extends Block implements TagMaster.IMiningLevelDataGenProvider {

	private final TagKey<Block> type;
	private final TagKey<Block> level;
	private final boolean isGrass;
	private final boolean shouldBePoisonous;

	public CorruptBlock(BlockBehaviour.Properties properties, TagKey<Block> type, TagKey<Block> level, boolean isGrass, boolean shouldBePoisonous) {
		super(properties);
		this.type = type;
		this.level = level;
		this.isGrass = isGrass;
		this.shouldBePoisonous = shouldBePoisonous;
	}

	public CorruptBlock(BlockBehaviour.Properties properties, TagKey<Block> type, TagKey<Block> level) {
		this(properties, type, level, false, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if(shouldBePoisonous) {
			poisonAll(world, pos);
		}

		if(isGrass) {
			if(!SpreadingSnowyDirtBlock.canBeGrass(state, world, pos)) {
				if(world.isAreaLoaded(pos, 3)) {
					world.setBlockAndUpdate(pos, Registry.getBlock("corrupt_dirt").defaultBlockState());
				}

			}
		}

	}

	@Override
	public TagKey<Block> getToolType() {
		return type;
	}


	@Override
	public TagKey<Block> getToolLevel() {
		return level;
	}

	@Override
	public boolean isRandomlyTicking(BlockState p_49921_) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		if(shouldBePoisonous) {
			animate(stateIn, worldIn, pos, rand);
		}

	}

	//Provides gentle 'corrupt' particles to area of block.
	@OnlyIn(Dist.CLIENT)
	public static void animate(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		if (rand.nextInt(10) == 0) {
			for(int i = 0; i < 5; i++) {
				worldIn.addParticle(ParticleTypes.MYCELIUM, pos.getX() + rand.nextDouble(), pos.getY() + 1.1D, pos.getZ() + rand.nextDouble(), 0.1D, 0.1D, 0.1D);
			}

		}

	}

	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		if(plantable.getPlantType(world, pos) == CorruptTallGrassBlock.CORRUPT_GRASS) {
			Block b = state.getBlock();
			return (b.equals(Registry.getBlock("corrupt_grass")) || b.equals(Registry.getBlock("corrupt_dirt")));
		}
		return false;
	}

	//Applies Entropy Poisoning to all entities in area.
	public static void poisonAll(ServerLevel world, BlockPos pos) {

		BlockPos pos1 = pos.above();
		BlockPos pos2 = pos.above(2);
		world.getEntitiesOfClass(Player.class, new AABB(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, pos2.getX() + 1.5, pos2.getY() + 1.5, pos2.getZ() + 1.5)).stream().filter((p) -> !p.isCreative()).forEach((p) -> {
			p.addEffect(new MobEffectInstance(Registry.ENTROPY_POISONING.get(), 100, 0, false, false, true));
		});
	}

	//Used for Chaosbark/Stripped Chaosbark Logs.
	public static class CorruptBlockWithAxis extends CorruptBlock{

		public CorruptBlockWithAxis(Properties properties, TagKey<Block> type, TagKey<Block> level, boolean isGrass, boolean shouldBePoisonous) {
			super(properties, type, level, isGrass, shouldBePoisonous);
			this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Axis.Y));
		}

		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
			pBuilder.add(RotatedPillarBlock.AXIS);
		}

		@Override
		public BlockState rotate(BlockState pState, Rotation pRotation) {
			return RotatedPillarBlock.rotatePillar(pState, pRotation);
		}

		@Override
		public BlockState getStateForPlacement(BlockPlaceContext pContext) {
			return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, pContext.getClickedFace().getAxis());

		}

		@Override
		public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction,
				boolean simulate) {
			if(ForgeRegistries.BLOCKS.getKey(this).toString().equals("assemblylinemachines:chaosbark_log") && toolAction.equals(ToolActions.AXE_STRIP)) {
				return Registry.getBlock("stripped_chaosbark_log").defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
			}

			return super.getToolModifiedState(state, context, toolAction, simulate);
		}

	}

	public static class CorruptLeavesBlock extends LeavesBlock implements IMiningLevelDataGenProvider{

		public CorruptLeavesBlock(Properties properties) {
			super(properties);
		}

		@Override
		public TagKey<Block> getToolType() {
			return BlockTags.MINEABLE_WITH_HOE;
		}

		@Override
		public TagKey<Block> getToolLevel() {
			return BlockTags.NEEDS_DIAMOND_TOOL;
		}



	}

	public static class ChaosbarkFenceBlock extends FenceBlock {
		public ChaosbarkFenceBlock(Properties properties) {
			super(properties);
		}

		@Override
		public boolean isSameFence(BlockState pState) {
			return pState.is(Registry.getBlock("chaosbark_fence"));
		}
	}

}
