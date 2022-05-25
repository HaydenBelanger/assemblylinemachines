package me.haydenb.assemblylinemachines.block.misc;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class BlockMystiumFarmland extends FarmBlock {

	private static final IntegerProperty AGE = IntegerProperty.create("exhaustion", 0, 32);
	
	private final boolean canGetExhausted;
	
	public BlockMystiumFarmland(boolean canGetExhausted) {
		super(Block.Properties.of(Material.DIRT).strength(0.6F).sound(SoundType.GRAVEL).randomTicks());
		this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 7).setValue(AGE, 0));
		this.canGetExhausted = canGetExhausted;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
		
		int age = state.getValue(AGE);
	
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
				BlockState bs = world.getBlockState(pos.above());
				if(bs.getBlock() instanceof BonemealableBlock) {
					BonemealableBlock grbs = (BonemealableBlock) bs.getBlock();
					if(grbs.isValidBonemealTarget(world, pos.above(), bs, world.isClientSide)) {
						if(grbs.isBonemealSuccess(world, random, pos.above(), bs)) {
							grbs.performBonemeal(world, random, pos.above(), bs);
							world.levelEvent(2005, pos.above(), 0);
							if(canGetExhausted && random.nextInt(2) == 0) {
								world.setBlockAndUpdate(pos, state.setValue(AGE, age + 1));
							}
						}
					}
				}
			}
		}
		
		
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(AGE);
		super.createBlockStateDefinition(builder);
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		if(plantable.getPlantType(world, pos) == PlantType.CROP) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos) {
		return true;
	}

}
