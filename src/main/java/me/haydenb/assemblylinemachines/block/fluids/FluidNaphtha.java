package me.haydenb.assemblylinemachines.block.fluids;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.client.FogRendering.ILiquidFogColor;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

public class FluidNaphtha extends ALMFluid implements ILiquidFogColor {
	
	public FluidNaphtha(boolean source) {
		super(Registry.createFluidProperties("naphtha", 2200, false, true, true), source, 222, 79, 22);
	}

	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.above().north().east(), pos.below().south().west()).iterator();
		while(iter.hasNext()) {
			
			BlockPos cor = iter.next();
			
			if(world.getBlockState(cor).getBlock() == Blocks.AIR && (world.isLoaded(cor.below()) || isSurroundingBlockFlammable(world, cor)) && !Utils.isInTag(world.getBlockState(cor.below()), new ResourceLocation(AssemblyLineMachines.MODID, "world/naphtha_fireproof"))) {
				
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
			super(fluid, ALMFluid.getTag("naphtha"), Block.Properties.of(Material.LAVA).strength(100f).lightLevel((state) -> 11).noDrops());
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
	
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
	public static class BlockNaphthaFire extends BaseFireBlock {
		public BlockNaphthaFire() {
			super(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().randomTicks()
					.strength(0f).lightLevel((state) -> 15).sound(SoundType.WOOL).noDrops(), 1f);
			this.registerDefaultState(this.stateDefinition.any().setValue(FireBlock.AGE, 0));
		}


		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
			builder.add(FireBlock.AGE);
		}

		@SuppressWarnings("deprecation")
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
								if(world.isLoaded(posx.below()) && !Utils.isInTag(world.getBlockState(posx.below()), new ResourceLocation(AssemblyLineMachines.MODID, "world/naphtha_fireproof"))) {
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
		
		//Event to allow instant-extinguish of Naphtha Fire.
		@SubscribeEvent
		public static void extinguishFire(PlayerInteractEvent.LeftClickBlock event) {

			Level world = event.getWorld();
			if(event.getFace() == Direction.UP) {
				BlockPos up = event.getPos().above();
				Block block = world.getBlockState(up).getBlock();
				
				if(block == Registry.getBlock("naphtha_fire")) {
					if(event.getPlayer().isCreative()) {
						event.setCanceled(true);
					}
					world.levelEvent(event.getPlayer(), 1009, up, 0);
					world.removeBlock(up, false);
				}
			}
				
		}

	}

}
