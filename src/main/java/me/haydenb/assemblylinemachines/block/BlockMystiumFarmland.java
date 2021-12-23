package me.haydenb.assemblylinemachines.block;

import java.util.Random;

import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class BlockMystiumFarmland extends FarmlandBlock {

	private static final IntegerProperty AGE = IntegerProperty.create("exhaustion", 0, 32);
	
	public BlockMystiumFarmland() {
		super(Block.Properties.create(Material.EARTH).hardnessAndResistance(0.6F).sound(SoundType.GROUND).tickRandomly());
		this.setDefaultState(this.stateContainer.getBaseState().with(MOISTURE, 7).with(AGE, 0));
		
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		
		int age = state.get(AGE);
	
		if(age != 32) {
			int rnx = 1;
			if(age > 5 && age <= 10) {
				rnx = 2;
			}else if(age > 10 && age <= 22) {
				rnx = 3;
			}else {
				rnx = 4;
			}
			if(random.nextInt(rnx) == 0) {
				BlockState bs = world.getBlockState(pos.up());
				if(bs.getBlock() instanceof IGrowable) {
					IGrowable grbs = (IGrowable) bs.getBlock();
					if(grbs.canGrow(world, pos.up(), bs, world.isRemote)) {
						if(grbs.canUseBonemeal(world, random, pos.up(), bs)) {
							grbs.grow(world, random, pos.up(), bs);
							world.playEvent(2005, pos.up(), 0);
							if(random.nextInt(2) == 0) {
								
								if(ConfigHolder.COMMON.mystiumFarmlandDeath.get()) {
									world.setBlockState(pos, state.with(AGE, age + 1));
								}
								
								
							}
						}
					}
				}
			}
		}
		
		
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AGE);
		super.fillStateContainer(builder);
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
		if(plantable.getPlantType(world, pos) == PlantType.Crop) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
		return true;
	}

}
