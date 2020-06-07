package me.haydenb.assemblylinemachines.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlockBlackGranite extends Block{

	public static final BooleanProperty NATURAL_GRANITE = BooleanProperty.create("natural");
	public BlockBlackGranite() {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 9f));
		this.setDefaultState(this.stateContainer.getBaseState().with(NATURAL_GRANITE, false));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(NATURAL_GRANITE);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn,
			BlockPos pos) {

		if(state.get(NATURAL_GRANITE) == true) {
			ItemStack item = player.getHeldItemMainhand();
			if(item != ItemStack.EMPTY && item.hasTag()) {
				CompoundNBT compound = item.getTag();
				if(compound.getBoolean("assemblylinemachines:hascranks")) {
					return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
				}
			}
			return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos) * 0.05F;
		}
		
		return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
		
	}

}
