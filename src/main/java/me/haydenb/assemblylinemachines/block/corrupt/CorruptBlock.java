package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.List;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.IBlockWithHarvestableTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CorruptBlock extends Block implements IBlockWithHarvestableTags {

	private final Named<Block> type;
	private final Named<Block> level;
	
	public CorruptBlock(BlockBehaviour.Properties properties, Named<Block> type, Named<Block> level) {
		super(properties.strength(13f, 30f));
		this.type = type;
		this.level = level;

	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		poisonAll(worldIn, pos);

	}

	@Override
	public Named<Block> getToolType() {
		return type;
	}


	@Override
	public Named<Block> getToolLevel() {
		return level;
	}
	
	@Override
	public boolean isRandomlyTicking(BlockState p_49921_) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		animate(stateIn, worldIn, pos, rand);
	}

	@OnlyIn(Dist.CLIENT)
	public static void animate(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (rand.nextInt(10) == 0) {
			for(int i = 0; i < 5; i++) {
				worldIn.addParticle(ParticleTypes.MYCELIUM, (double)pos.getX() + rand.nextDouble(), (double)pos.getY() + 1.1D, (double)pos.getZ() + rand.nextDouble(), 0.1D, 0.1D, 0.1D);
			}
			
		}

	}

	public static void poisonAll(ServerLevel world, BlockPos pos) {

		BlockPos pos1 = pos.above();
		BlockPos pos2 = pos.above(2);
		List<Player> players = world.getEntitiesOfClass(Player.class, new AABB(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, pos2.getX() + 1.5, pos2.getY() + 1.5, pos2.getZ() + 1.5));

		for(Player p : players) {
			p.addEffect(new MobEffectInstance(Registry.getEffect("entropy_poisoning"), 100));
		}
	}
}
