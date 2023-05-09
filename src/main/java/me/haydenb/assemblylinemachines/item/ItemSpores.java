package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

public class ItemSpores extends Item{

	private final TagKey<Block> from;
	private final Block to;

	public ItemSpores(TagKey<Block> from, Block to) {
		super(new Item.Properties());
		this.from = from;
		this.to = to;
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		Level level = pContext.getLevel();
		BlockPos pos = pContext.getClickedPos();
		if(!level.isClientSide) {
			if(level.getBlockState(pos).is(from)) {
				level.setBlockAndUpdate(pos, to.defaultBlockState());
				PacketData pd = new PacketData("spores_growth");
				pd.writeDouble("x", pos.getX() + 0.5d);
				pd.writeDouble("y", pos.getY() + 1.1d);
				pd.writeDouble("z", pos.getZ() + 0.5d);
				PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), pd);
				pContext.getItemInHand().shrink(1);
			}

		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@OnlyIn(Dist.CLIENT)
	public static void spawnGrowParticles(PacketData pd) {
		Minecraft mc = Minecraft.getInstance();
		for(int i = 0; i < 6; i++) {
			mc.player.getCommandSenderWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, pd.get("x", Double.class) + Utils.RAND.nextDouble(-0.5, 0.5), pd.get("y", Double.class), pd.get("z", Double.class) + Utils.RAND.nextDouble(-0.5, 0.5), 0, 0, 0);
		}
	}

}
