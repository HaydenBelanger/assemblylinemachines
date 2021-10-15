package me.haydenb.assemblylinemachines.item.categories;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.*;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPublicHoe extends ToolItem{

	private static final Set<Block> field_234683_c_ = ImmutableSet.of(Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.HAY_BLOCK, Blocks.DRIED_KELP_BLOCK, Blocks.TARGET, Blocks.SHROOMLIGHT, Blocks.SPONGE, Blocks.WET_SPONGE, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES);
	   public static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.getDefaultState(), Blocks.GRASS_PATH, Blocks.FARMLAND.getDefaultState(), Blocks.DIRT, Blocks.FARMLAND.getDefaultState(), Blocks.COARSE_DIRT, Blocks.DIRT.getDefaultState()));

	   public ItemPublicHoe(IItemTier tier, float speed, Item.Properties properties) {
	      super(0f - tier.getAttackDamage(), speed, tier, field_234683_c_, properties);
	   }

	   /**
	    * Called when this item is used when targetting a Block
	    */
	   public ActionResultType onItemUse(ItemUseContext context) {
	      World world = context.getWorld();
	      BlockPos blockpos = context.getPos();
	      @SuppressWarnings("deprecation")
		int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
	      if (hook != 0) return hook > 0 ? ActionResultType.SUCCESS : ActionResultType.FAIL;
	      if (context.getFace() != Direction.DOWN && world.isAirBlock(blockpos.up())) {
	         BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(blockpos).getBlock());
	         if (blockstate != null) {
	            PlayerEntity playerentity = context.getPlayer();
	            world.playSound(playerentity, blockpos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
	            if (!world.isRemote) {
	               world.setBlockState(blockpos, blockstate, 11);
	               if (playerentity != null) {
	                  context.getItem().damageItem(1, playerentity, (p_220043_1_) -> {
	                     p_220043_1_.sendBreakAnimation(context.getHand());
	                  });
	               }
	            }

	            return ActionResultType.func_233537_a_(world.isRemote);
	         }
	      }

	      return ActionResultType.PASS;
	   }
	   
	   
	   
}
