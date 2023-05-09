package me.haydenb.assemblylinemachines.block.fluids;

import java.util.Iterator;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class FluidGasLike extends SplitFluid {

	public FluidGasLike(boolean source, ForgeFlowingFluid.Properties properties) {
		super(source, properties);
	}

	@Override
	protected boolean isRandomlyTicking() {
		return true;
	}

	@Override
	protected void randomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {

		if(source && ALMConfig.getServerConfig().gasolineExplosions().get()) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos.offset(-3, -1, -3).north().west(), pos.offset(3, 1, 3)).iterator();

			while(iter.hasNext()) {
				BlockPos cor = iter.next();

				if(world.getBlockState(cor).is(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "gas_flammable")))) {
					if(world.getRandom().nextInt(3) == 0) {
						world.explode(null, cor.getX(), cor.getY() + 1, cor.getZ(), breakAndBreakConnected(world, state, cor), true, ExplosionInteraction.BLOCK);

					}
				}
			}
		}
	}

	private float breakAndBreakConnected(Level world, FluidState origState, BlockPos posx) {
		world.setBlockAndUpdate(posx, Blocks.AIR.defaultBlockState());

		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(posx.below().north().west(), posx.above().south().east()).iterator();

		float pow = 2;
		while(iter.hasNext()) {
			BlockPos posq = iter.next();

			FluidState fs = world.getFluidState(posq);
			if(fs.getType() == origState.getType()) pow = pow + breakAndBreakConnected(world, origState, posq);
		}

		return pow;
	}
}
