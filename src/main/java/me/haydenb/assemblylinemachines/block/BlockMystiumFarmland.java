package me.haydenb.assemblylinemachines.block;

import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class BlockMystiumFarmland extends FarmlandBlock {

	public BlockMystiumFarmland() {
		super(AbstractBlock.Properties.create(Material.EARTH).hardnessAndResistance(0.6F).sound(SoundType.GROUND));
		this.setDefaultState(this.stateContainer.getBaseState().with(MOISTURE, 7));
		
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
		if(plantable.getPlantType(world, pos) == PlantType.CROP) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
		return true;
	}

}
