package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidOilProduct extends ForgeFlowingFluid {

	private final boolean source;

	public FluidOilProduct(ForgeFlowingFluid.Properties properties, boolean source) {
		super(properties);
		this.source = source;
		if (!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, FluidState> builder) {
		super.fillStateContainer(builder);

		if (!source) {
			builder.add(LEVEL_1_8);
		}
	}

	@Override
	public boolean isSource(FluidState state) {
		return source;
	}

	@Override
	public int getLevel(FluidState state) {
		if (!source) {
			return state.get(LEVEL_1_8);
		} else {
			return 8;
		}
	}

	@Override
	protected boolean ticksRandomly() {
		return true;
	}
	
	@Override
	protected void randomTick(World world, BlockPos pos, FluidState state, Random random) {
		
		if(source) {
			Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.add(-3, -1, -3).north().west(), pos.add(3, 1, 3)).iterator();
			
			while(iter.hasNext()) {
				BlockPos cor = iter.next();
				
				Block block = world.getBlockState(cor).getBlock();
				if(block.getTags().contains(new ResourceLocation("assemblylinemachines", "world/gas_flammable"))) {
					if(General.RAND.nextInt(3) == 0) {
						world.createExplosion(null, cor.getX(), cor.getY() + 1, cor.getZ(), breakAndBreakConnected(world, state, cor), true, Mode.BREAK);
						
					}
				}
			}
		}
	}
	
	private float breakAndBreakConnected(World world, FluidState origState, BlockPos posx) {
		world.setBlockState(posx, Blocks.AIR.getDefaultState());
		
		Iterator<BlockPos> iter = BlockPos.getAllInBox(posx.down().north().west(), posx.up().south().east()).iterator();
		
		float pow = 2;
		while(iter.hasNext()) {
			BlockPos posq = iter.next();
			
			FluidState fs = world.getFluidState(posq);
			if(fs.getFluid() == origState.getFluid()) {
				pow = pow + breakAndBreakConnected(world, origState, posq);
			}
		}
		
		return pow;
	}
	
	public static class FluidOilProductBlock extends FlowingFluidBlock {

		public FluidOilProductBlock(Supplier<FlowingFluid> fluid) {
			super(fluid,
					Block.Properties.create(Material.WATER).hardnessAndResistance(100f).noDrops());
		}

		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addPotionEffect(new EffectInstance(Effects.POISON, 60, 2));
				player.addPotionEffect(new EffectInstance(Effects.HUNGER, 60, 3));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
	}

}
