package me.haydenb.assemblylinemachines.block.fluids;

import java.util.Iterator;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class FluidNaphtha extends SplitFluid {

	public FluidNaphtha(boolean source) {
		super(source, Registry.basicFFFProperties("naphtha"));
	}

	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}

	@Override
	public int getTickDelay(LevelReader world) {
		return 4;
	}

	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
		while(iter.hasNext()) {
			BlockPos cor = iter.next();

			if(world.getBlockState(cor).isAir() && (world.isLoaded(cor.below()) || ((LavaFluid) Fluids.LAVA).hasFlammableNeighbours(world, cor)) && !world.getBlockState(cor.below()).is(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "naphtha_fireproof")))) {
				world.setBlockAndUpdate(cor, ForgeEventFactory.fireFluidPlaceBlockEvent(world, cor, pos, Registry.getBlock("naphtha_fire").defaultBlockState()));
			}
		}
	}

	@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
	public static class BlockNaphthaFire extends BaseFireBlock {
		public BlockNaphthaFire() {
			super(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().randomTicks()
					.strength(0f).lightLevel((state) -> 15).sound(SoundType.WOOL).noLootTable(), 1f);
			this.registerDefaultState(this.stateDefinition.any().setValue(FireBlock.AGE, 0));
		}


		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
			builder.add(FireBlock.AGE);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
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
							if(world.getBlockState(posx).isAir()) {
								int nAge = age + 2 + rand.nextInt(6);
								int xAge = age + 1 + rand.nextInt(4);
								if(nAge > 15) nAge = 15;
								if(xAge > 15) xAge = 15;
								if(world.isLoaded(posx.below()) && !world.getBlockState(posx.below()).is(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "naphtha_fireproof")))) {
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
				living.addEffect(new MobEffectInstance(Registry.DEEP_BURN.get(), 140, 0));
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

		//Event to allow instant-extinguish of Naphtha Fire.
		@SubscribeEvent
		public static void extinguishFire(PlayerInteractEvent.LeftClickBlock event) {

			Level world = event.getLevel();
			if(event.getFace() == Direction.UP) {
				BlockPos up = event.getPos().above();
				Block block = world.getBlockState(up).getBlock();

				if(block == Registry.getBlock("naphtha_fire")) {
					if(event.getEntity().isCreative()) {
						event.setCanceled(true);
					}
					world.levelEvent(event.getEntity(), 1009, up, 0);
					world.removeBlock(up, false);
				}
			}

		}

	}

}
