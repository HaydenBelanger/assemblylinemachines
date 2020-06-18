package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidNaphtha extends ForgeFlowingFluid {

	private static final FluidAttributes.Builder ATTRIBUTES = Registry.getFluidAttributes("naphtha").temperature(2200);
	private static final ForgeFlowingFluid.Properties PROPERTIES = Registry.getFluidProperties("naphtha", ATTRIBUTES);
	private final boolean source;

	public FluidNaphtha(boolean source) {
		super(PROPERTIES);
		this.source = source;
		if (!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, IFluidState> builder) {
		super.fillStateContainer(builder);

		if (!source) {
			builder.add(LEVEL_1_8);
		}
	}

	@Override
	public boolean isSource(IFluidState state) {
		return source;
	}

	@Override
	protected void randomTick(World world, BlockPos pos, IFluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
		while(iter.hasNext()) {
			
			BlockPos cor = iter.next();
			
			if(world.getBlockState(cor).getBlock() == Blocks.AIR && (world.isBlockPresent(cor.down()) || isSurroundingBlockFlammable(world, cor))) {
				
				world.setBlockState(cor, ForgeEventFactory.fireFluidPlaceBlockEvent(world, cor, pos, Registry.getBlock("naphtha_fire").getDefaultState()));
				
			}
				
		}
		
		super.randomTick(world, pos, state, random);
	}

	@Override
	protected boolean ticksRandomly() {
		return true;
	}

	@Override
	public int getLevel(IFluidState state) {
		if (!source) {
			return state.get(LEVEL_1_8);
		} else {
			return 8;
		}
	}

	private boolean isSurroundingBlockFlammable(IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (this.getCanBlockBurn(worldIn, pos.offset(direction))) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean getCanBlockBurn(IWorldReader worldIn, BlockPos pos) {
		return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.isBlockLoaded(pos) ? false
				: worldIn.getBlockState(pos).getMaterial().isFlammable();
	}

	@Override
	public int getTickRate(IWorldReader world) {
		return 4;
	}

	public static class FluidNaphthaBlock extends FlowingFluidBlock {

		public FluidNaphthaBlock() {
			super(() -> (FlowingFluid) Registry.getFluid("naphtha"),
					Block.Properties.create(Material.LAVA).hardnessAndResistance(100f).noDrops());
		}
		
		@Override
		public int getLightValue(BlockState state) {
			return 11;
		}
	}

}
