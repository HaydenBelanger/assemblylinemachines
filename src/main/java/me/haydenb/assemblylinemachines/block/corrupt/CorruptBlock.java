package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.List;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CorruptBlock extends Block {

	public CorruptBlock(AbstractBlock.Properties properties) {
		super(properties.hardnessAndResistance(13f, 30f).harvestLevel(0));

	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		poisonAll(worldIn, pos);

	}

	@Override
	public boolean ticksRandomly(BlockState state) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		animate(stateIn, worldIn, pos, rand);
	}

	@OnlyIn(Dist.CLIENT)
	public static void animate(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (rand.nextInt(10) == 0) {
			for(int i = 0; i < 5; i++) {
				worldIn.addParticle(ParticleTypes.MYCELIUM, (double)pos.getX() + rand.nextDouble(), (double)pos.getY() + 1.1D, (double)pos.getZ() + rand.nextDouble(), 0.1D, 0.1D, 0.1D);
			}
			
		}

	}

	public static void poisonAll(World world, BlockPos pos) {

		BlockPos pos1 = pos.up();
		BlockPos pos2 = pos.up(2);
		List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, pos2.getX() + 1.5, pos2.getY() + 1.5, pos2.getZ() + 1.5));

		for(PlayerEntity p : players) {
			p.addPotionEffect(new EffectInstance(Registry.getEffect("entropy_poisoning"), 100));
		}
	}
}
