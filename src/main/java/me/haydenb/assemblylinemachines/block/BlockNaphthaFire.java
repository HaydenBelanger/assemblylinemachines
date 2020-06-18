package me.haydenb.assemblylinemachines.block;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockNaphthaFire extends FireBlock {

	public BlockNaphthaFire() {
		super(Block.Properties.create(Material.FIRE, MaterialColor.TNT).doesNotBlockMovement().tickRandomly()
				.hardnessAndResistance(0f).lightValue(15).sound(SoundType.CLOTH).noDrops());
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
			if (!world.isAreaLoaded(pos, 2)) return;
			if (!state.isValidPosition(world, pos)) {
				world.removeBlock(pos, false);
			}

			int age = state.get(AGE);
			if(age < 15) {
				
				Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
				
				
				while(iter.hasNext()) {
					BlockPos posx = iter.next();
					if (rand.nextInt(10) < 2) {
						if(world.getBlockState(posx).getBlock() == Blocks.AIR) {
							if(world.isBlockPresent(posx.down())) {
								int nAge = age + 1 + rand.nextInt(3);
								if(nAge > 15) nAge = 15;
								world.setBlockState(posx, ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, posx, Registry.getBlock("naphtha_fire").getDefaultState().with(AGE, nAge)));
							}
							
						}
						
					}
				}
				
			}
		}
	}
	
	@Override
	public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
		return true;
	}
	
	

}
