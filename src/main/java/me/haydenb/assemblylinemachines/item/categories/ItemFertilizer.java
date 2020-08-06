package me.haydenb.assemblylinemachines.item.categories;

import java.util.Iterator;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ItemFertilizer extends Item{

	private final int range;
	public ItemFertilizer(int range) {
		super(new Item.Properties().group(Registry.creativeTab));
		this.range = range;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {

		boolean shrnk = false;

		World world = context.getWorld();

		Iterator<BlockPos> iter = BlockPos.getAllInBox(context.getPos().offset(Direction.NORTH, range).offset(Direction.EAST, range), context.getPos().offset(Direction.SOUTH, range).offset(Direction.WEST, range)).iterator();

		while(iter.hasNext()) {
			BlockPos pos = iter.next();

			if (applyBonemeal(context.getItem(), world, pos, context.getPlayer())) {
				if (!world.isRemote) {
					shrnk = true;
					world.playEvent(2005, pos, 0);
				}


			}
		}


		if(shrnk) {
			context.getItem().shrink(1);
		}
		return ActionResultType.func_233537_a_(world.isRemote);
	}


	public static boolean applyBonemeal(ItemStack stack, World worldIn, BlockPos pos, net.minecraft.entity.player.PlayerEntity player) {
		BlockState blockstate = worldIn.getBlockState(pos);
		if (blockstate.getBlock() instanceof IGrowable) {
			IGrowable igrowable = (IGrowable)blockstate.getBlock();
			if (igrowable.canGrow(worldIn, pos, blockstate, worldIn.isRemote)) {
				if (worldIn instanceof ServerWorld) {
					if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, blockstate)) {
						igrowable.grow((ServerWorld)worldIn, worldIn.rand, pos, blockstate);
					}

				}

				return true;
			}
		}

		return false;
	}
}
