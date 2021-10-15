package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class BlockNaphthaTurbine extends Block{

	
	private static final VoxelShape SHAPE = Stream.of(
			Block.makeCuboidShape(2, 0, 2, 14, 6, 14),
			Block.makeCuboidShape(1, 6, 1, 15, 12, 15),
			Block.makeCuboidShape(0, 12, 0, 16, 16, 16)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	public BlockNaphthaTurbine() {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 30f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

}
