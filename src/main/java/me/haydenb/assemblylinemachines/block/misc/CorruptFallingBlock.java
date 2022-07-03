package me.haydenb.assemblylinemachines.block.misc;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.datagen.TagMaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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
	private final TagKey<Block> toolLevelTag;

	public CorruptFallingBlock(int dustColor, Properties properties, TagKey<Block> toolLevelTag) {
		super(properties);
		this.dustColor = dustColor;
		this.toolLevelTag = toolLevelTag;
	}

	public CorruptFallingBlock(int dustColor, Properties properties) {
		this(dustColor, properties, BlockTags.NEEDS_DIAMOND_TOOL);
	}

	@Override
	public TagKey<Block> getToolType() {
		return BlockTags.MINEABLE_WITH_SHOVEL;
	}


	@Override
	public TagKey<Block> getToolLevel() {
		return toolLevelTag;
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {

		CorruptBlock.poisonAll(world, pos);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
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
