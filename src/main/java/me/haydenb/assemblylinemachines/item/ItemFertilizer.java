package me.haydenb.assemblylinemachines.item;

import java.util.Iterator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemFertilizer extends Item{

	private final int range;
	public ItemFertilizer(int range) {
		super(new Item.Properties());
		this.range = range;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {

		boolean shrnk = false;

		Level world = context.getLevel();

		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(context.getClickedPos().relative(Direction.NORTH, range).relative(Direction.EAST, range), context.getClickedPos().relative(Direction.SOUTH, range).relative(Direction.WEST, range)).iterator();

		while(iter.hasNext()) {
			BlockPos pos = iter.next();

			if (applyBonemeal(context.getItemInHand(), world, pos, context.getPlayer())) {
				if (!world.isClientSide) {
					shrnk = true;
					world.levelEvent(2005, pos, 0);
				}


			}
		}


		if(shrnk) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}


	public static boolean applyBonemeal(ItemStack stack, Level worldIn, BlockPos pos, Player player) {
		BlockState blockstate = worldIn.getBlockState(pos);
		if (blockstate.getBlock() instanceof BonemealableBlock) {
			BonemealableBlock BonemealableBlock = (BonemealableBlock)blockstate.getBlock();
			if (BonemealableBlock.isValidBonemealTarget(worldIn, pos, blockstate, worldIn.isClientSide)) {
				if (worldIn instanceof ServerLevel) {
					if (BonemealableBlock.isBonemealSuccess(worldIn, worldIn.random, pos, blockstate)) {
						BonemealableBlock.performBonemeal((ServerLevel)worldIn, worldIn.random, pos, blockstate);
					}

				}

				return true;
			}
		}

		return false;
	}
}
