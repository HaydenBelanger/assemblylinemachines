package me.haydenb.assemblylinemachines.block.fluids;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.client.FogRendering.ILiquidFogColor;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidGlacierWater extends ALMFluid implements ILiquidFogColor {
	
	private static final List<Block> SNOW_INVALID_BLOCKS = List.of(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BARRIER);
	public FluidGlacierWater(boolean source) {
		super(Registry.createFluidProperties("glacier_water", -100, false, true, true), source, 114, 154, 219);
	}
	
	@Override
	public int getTickDelay(LevelReader world) {
		return 10;
	}
	
	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}
	
	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState fState, Random random) {
		Iterable<BlockPos> iter = BlockPos.betweenClosed(pos.above(2).north(3).east(3), pos.below(2).south(3).west(3));
		iter.forEach((posx) -> {
			if(random.nextInt(3) == 0) {
				BlockState state = world.getBlockState(posx);
				Block block = state.getBlock();
				if(block.equals(Blocks.WATER)) {
					world.setBlockAndUpdate(posx, Blocks.PACKED_ICE.defaultBlockState());
				}else if(block.equals(Blocks.LAVA)) {
					world.setBlockAndUpdate(posx, Blocks.OBSIDIAN.defaultBlockState());
				}else if(block.equals(Blocks.SNOW) && state.hasProperty(SnowLayerBlock.LAYERS)) {
					if(state.getValue(SnowLayerBlock.LAYERS) < 8) {
						world.setBlockAndUpdate(posx, state.setValue(SnowLayerBlock.LAYERS, state.getValue(SnowLayerBlock.LAYERS) + 1));
					}else {
						world.setBlockAndUpdate(posx, Blocks.POWDER_SNOW.defaultBlockState());
					}
				}else if(world.isEmptyBlock(posx) && !SNOW_INVALID_BLOCKS.contains(world.getBlockState(posx.below()).getBlock()) && Block.isFaceFull(world.getBlockState(posx.below()).getCollisionShape(world, posx.below()), Direction.UP)) {
					world.setBlockAndUpdate(posx, Blocks.SNOW.defaultBlockState());
				
				}
			}
		});
		super.randomTick(world, pos, fState, random);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public float getFogDensity(LocalPlayer player) {
		return 32f;
	}
	
	public static class FluidGlacierWaterBlock extends ALMFluidBlock {

		public FluidGlacierWaterBlock(Supplier<? extends FlowingFluid> fluid) {
			super(fluid, ALMFluid.getTag("glacier_water"), Material.WATER);
		}

		@Override
		public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity) {
			
			
			super.entityInside(state, worldIn, pos, entity);
		}
	}

}
