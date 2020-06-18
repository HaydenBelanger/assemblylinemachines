package me.haydenb.assemblylinemachines.fluid;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.ChunkCoords;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fluids.FluidStack;

public class FluidLevelManager {

	
	private static final ConcurrentHashMap<ChunkCoords, FluidStack> CHUNK_FLUIDS = new ConcurrentHashMap<>();
	
	private static final Random RAND = new Random();
	
	public static void readData(ChunkPos pos, IWorld world, CompoundNBT nbt) {
		
		
		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), pos.x, pos.z);
		
		if(nbt.contains("assemblylinemachines:chunkfluid")) {
			
			CHUNK_FLUIDS.put(cc, FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:chunkfluid")));
		}
	}
	
	public static void writeData(IChunk chunk, IWorld world, CompoundNBT nbt) {
		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), chunk.getPos().x, chunk.getPos().z);
		if(CHUNK_FLUIDS.containsKey(cc)) {
			
			CompoundNBT sub = new CompoundNBT();
			CHUNK_FLUIDS.get(cc).writeToNBT(sub);
			nbt.put("assemblylinemachines:chunkfluid", sub);
			
		}
	}
	
	public static void clearData(IWorld world, ChunkPos pos) {
		int dimid = world.getDimension().getType().getId();
		CHUNK_FLUIDS.remove(new ChunkCoords(dimid, pos.x, pos.z));
	}
	
	public static FluidStack getOrCreateFluidStack(BlockPos pos, World world) {
		
		ChunkPos chunkpos = world.getChunk(pos).getPos();
		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), chunkpos.x, chunkpos.z);
		FluidStack fs = CHUNK_FLUIDS.get(cc);
		if(fs == null) {
			
			int val = RAND.nextInt(80);
			
			
			if(world.getDimension().getType() == DimensionType.OVERWORLD) {
				
				
				if(val < 2) {
					fs = new FluidStack(Registry.getFluid("oil"), (RAND.nextInt(4000) + 1001) * 1000);
				}else {
					
					TempCategory temp = world.getBiome(pos).getTempCategory();
					if(temp == TempCategory.COLD) {
						if(val < 18) {
							fs = new FluidStack(Fluids.WATER, (RAND.nextInt(21500) + 1001) * 1000);
						}else {
							fs = FluidStack.EMPTY;
						}
					}else if(temp == TempCategory.WARM){
						if(val < 18) {
							fs = new FluidStack(Fluids.LAVA, (RAND.nextInt(19000) + 1001) * 1000);
						}else {
							fs = FluidStack.EMPTY;
						}
					}else {
						if(val < 6) {
							fs = new FluidStack(Fluids.LAVA, (RAND.nextInt(6500) + 1001) * 1000);
						}else if(val < 12) {
							fs = new FluidStack(Fluids.WATER, (RAND.nextInt(9000) + 1001) * 1000);
						}else {
							fs = FluidStack.EMPTY;
						}
					}
					
				}
			}else if(world.getDimension().getType() == DimensionType.THE_NETHER) {
				if(val == 0) {
					fs = new FluidStack(Registry.getFluid("naphtha"), (RAND.nextInt(1000) + 1001) * 1000);
				}else if(val < 15) {
					fs = new FluidStack(Fluids.LAVA, (RAND.nextInt(29000) + 1001) * 1000);
				}else {
					fs = FluidStack.EMPTY;
				}
			}else if(world.getDimension().getType() == DimensionType.THE_END) {
				if(val == 0) {
					fs = new FluidStack(Registry.getFluid("condensed_void"), (RAND.nextInt(500) + 501) * 1000);
				}else {
					fs = FluidStack.EMPTY;
				}
			}
			
			CHUNK_FLUIDS.put(cc, fs);
		}
		
		return fs;
	}
	
}
