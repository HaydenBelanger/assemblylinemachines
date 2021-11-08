package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.TagMaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;

public class CorruptFallingBlock extends FallingBlock implements TagMaster.IMiningLevelDataGenProvider {

	
	private final int dustColor;
	
	public CorruptFallingBlock(int dustColor, Properties properties) {
		super(properties.strength(3f, 9f));
		this.dustColor = dustColor;
	}
	
	@Override
	public Named<Block> getToolType() {
		return BlockTags.MINEABLE_WITH_SHOVEL;
	}


	@Override
	public Named<Block> getToolLevel() {
		return BlockTags.NEEDS_DIAMOND_TOOL;
	}
	
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
		
		CorruptBlock.poisonAll(world, pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		CorruptBlock.animate(stateIn, worldIn, pos, rand);
	}
	
	@Override
	public int getDustColor(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return this.dustColor;
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		return plantable.getPlantType(world, pos) == CorruptTallGrassBlock.BRAIN_CACTUS ? state.getBlock().equals(Registry.getBlock("corrupt_sand")) : false;
	}
}
