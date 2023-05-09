package me.haydenb.assemblylinemachines.block.misc;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.authlib.GameProfile;

import me.haydenb.assemblylinemachines.block.energy.BlockEntropyReactor.ISpecialEntropyPlacement;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.common.util.FakePlayerFactory;

public class CorruptTallGrassBlock extends TallGrassBlock {

	public static final PlantType CORRUPT_GRASS = PlantType.get("corrupt_grass");
	public static final PlantType BRAIN_CACTUS = PlantType.get("brain_cactus");

	public CorruptTallGrassBlock() {
		super(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS));
	}

	@Override
	public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
		DoublePlantBlock dpb;
		if(pState.is(Registry.getBlock("chaosweed"))) {
			dpb = (DoublePlantBlock) Registry.getBlock("tall_chaosweed");
		}else {
			dpb = (DoublePlantBlock) Registry.getBlock("tall_blooming_chaosweed");
		}
		if (dpb.defaultBlockState().canSurvive(pLevel, pPos) && pLevel.isEmptyBlock(pPos.above())) {
			DoublePlantBlock.placeAt(pLevel, dpb.defaultBlockState(), pPos, 2);
		}
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos) {
		return CORRUPT_GRASS;
	}

	public static class CorruptDoubleTallGrassBlock extends DoublePlantBlock implements ISpecialEntropyPlacement {

		public CorruptDoubleTallGrassBlock() {
			super(Block.Properties.of(Material.REPLACEABLE_PLANT).noCollission().instabreak().sound(SoundType.GRASS));
		}

		@Override
		public PlantType getPlantType(BlockGetter world, BlockPos pos) {
			return CORRUPT_GRASS;
		}

		@Override
		public void place(LevelAccessor level, BlockState state, BlockPos pos, int flag) {
			DoublePlantBlock.placeAt(level, state, pos, flag); 
		}
	}

	public static class CorruptFlowerBlock extends FlowerBlock{

		public CorruptFlowerBlock(Supplier<MobEffect> pSuspiciousStewEffect, int pEffectDuration, int lightValue) {
			super(pSuspiciousStewEffect, pEffectDuration, Block.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS).lightLevel((state) -> lightValue));
		}

		@Override
		public PlantType getPlantType(BlockGetter world, BlockPos pos) {
			return CORRUPT_GRASS;
		}

	}

	public static class BrainCactusBlock extends CactusBlock {

		public static final BooleanProperty CAP = BooleanProperty.create("cap");

		private static final GameProfile BRAIN_CACTUS_PROFILE = new GameProfile(null, "Brain Cactus");

		private static final VoxelShape NO_CAP_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
		private static final VoxelShape CAP_SHAPE = Stream.of(
				Block.box(2, 0, 2, 14, 10, 14),
				Block.box(2, 10, 2, 14, 10, 14),
				Block.box(3, 10, 3, 13, 14, 13),
				Block.box(4, 14, 4, 12, 15, 12)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

		public BrainCactusBlock(Properties properties) {
			super(properties);
			this.registerDefaultState(this.stateDefinition.any().setValue(CactusBlock.AGE, Integer.valueOf(0)).setValue(CAP, false));
		}

		@Override
		public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
			return plantable.getPlantType(world, pos) == BRAIN_CACTUS;
		}

		@Override
		public PlantType getPlantType(BlockGetter world, BlockPos pos) {
			return BRAIN_CACTUS;
		}

		@Override
		public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
			return pState.getValue(CAP) ? CAP_SHAPE : NO_CAP_SHAPE;
		}

		@Override
		public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
			super.randomTick(pState, pLevel, pPos, pRandom);
			BlockPos above = pPos.above();
			if(pLevel.getBlockState(above).getBlock().equals(this) && pLevel.getBlockState(above).getValue(CAP) == true && pState.getValue(CAP) == true) {
				pLevel.setBlockAndUpdate(pPos, pState.setValue(CAP, false));
			}
		}

		@Override
		public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
			if (!pState.canSurvive(pLevel, pCurrentPos)) {
				pLevel.getBlockTicks().schedule(new ScheduledTick<Block>(this, pCurrentPos, 1, 1));
			}
			return (pLevel.isEmptyBlock(pCurrentPos.above()) ? true : false) == pState.getValue(CAP) ? pState : pState.setValue(CAP, (pLevel.isEmptyBlock(pCurrentPos.above()) ? true : false));
		}

		@SuppressWarnings("deprecation")
		@Override
		public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
			if(!pLevel.isAreaLoaded(pPos, 1)) return;
			super.tick(pState, pLevel, pPos, pRand);
			boolean shouldBeCap = pLevel.isEmptyBlock(pPos.above()) ? true : false;
			if(pState.getValue(CAP) != shouldBeCap) pLevel.setBlockAndUpdate(pPos, pState.setValue(CAP, shouldBeCap));
		}

		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
			super.createBlockStateDefinition(pBuilder);
			pBuilder.add(CAP);
		}

		@Override
		public BlockState getStateForPlacement(BlockPlaceContext pContext) {
			return pContext.getLevel().isEmptyBlock(pContext.getClickedPos().relative(pContext.getClickedFace().getOpposite()).above()) ? this.defaultBlockState().setValue(CAP, true) : this.defaultBlockState();
		}

		@Override
		public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
			if(!pLevel.isClientSide) {
				if(!(pEntity instanceof ExperienceOrb) && !(pEntity instanceof ItemEntity)) {
					pEntity.hurt(DamageSource.playerAttack(FakePlayerFactory.get((ServerLevel) pLevel, BRAIN_CACTUS_PROFILE)), 4.0f);
				}

			}

		}
	}

}
