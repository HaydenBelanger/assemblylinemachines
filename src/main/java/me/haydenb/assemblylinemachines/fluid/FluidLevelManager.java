package me.haydenb.assemblylinemachines.fluid;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.ChunkCoords;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fluids.FluidStack;

public class FluidLevelManager {

	private static final ConcurrentHashMap<ChunkCoords, FluidStack> CHUNK_FLUIDS = new ConcurrentHashMap<>();

	private static final Random RAND = new Random();

	public static void readData(ChunkPos pos, IWorld world, CompoundNBT nbt) {

		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), pos.x, pos.z);

		if (nbt.contains("assemblylinemachines:chunkfluid")) {

			CHUNK_FLUIDS.put(cc, FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:chunkfluid")));
		}
	}

	public static void writeData(IChunk chunk, IWorld world, CompoundNBT nbt) {
		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), chunk.getPos().x, chunk.getPos().z);
		if (CHUNK_FLUIDS.containsKey(cc)) {

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
		if (fs == null) {

			List<FluidInGroundRecipe> recipes = world.getRecipeManager().getRecipes(FluidInGroundRecipe.FIG_RECIPE, null, world);
			
			float tc = world.getBiome(pos).getTemperature(pos);
			DimensionType dc = world.getDimension().getType();
			for (FluidInGroundRecipe recipe : recipes) {

				int chance = recipe.getChance();
				boolean half = false;

				if (dc.equals(DimensionType.OVERWORLD)) {

					if (recipe.getCriteria() != FluidInGroundCriteria.OVERWORLD_ANY && recipe.getCriteria() != FluidInGroundCriteria.OVERWORLD_ONLYCOLD
							&& recipe.getCriteria() != FluidInGroundCriteria.OVERWORLD_ONLYHOT && recipe.getCriteria() != FluidInGroundCriteria.OVERWORLD_PREFCOLD
							&& recipe.getCriteria() != FluidInGroundCriteria.OVERWORLD_PREFHOT) {
						chance = -1;
					} else if ((recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYCOLD && tc > 0f)
							|| (recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_ONLYHOT && tc < 1f)
							|| (recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFCOLD && tc >= 1f)
							|| (recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFHOT && tc <= 0f)) {
						chance = -1;
					} else if ((recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFCOLD && tc > 0f)
							|| (recipe.getCriteria() == FluidInGroundCriteria.OVERWORLD_PREFHOT && tc < 1f)) {
						chance = Math.round((float) chance / 2f);
						half = true;
					}
				}else if(dc.equals(DimensionType.THE_NETHER)) {
					if(recipe.getCriteria() != FluidInGroundCriteria.NETHER) {
						chance = -1;
					}
				}else if(dc.equals(DimensionType.THE_END)) {
					if(recipe.getCriteria() != FluidInGroundCriteria.END) {
						chance = -1;
					}
				}else {
					System.out.println("nochance");
					chance = -1;
				}

				if (chance != -1) {
					if (RAND.nextInt(100) <= chance) {

						int amt = (recipe.getMinimum() + RAND.nextInt(recipe.getMaximum() - recipe.getMinimum())) * 10000;

						if (half) {
							amt = Math.round((float) amt / 2f);
						}
						
						fs = new FluidStack(recipe.getFluid(), amt);

						break;
					}
				}

			}

			if (fs == null) {
				fs = FluidStack.EMPTY;
			}

			CHUNK_FLUIDS.put(cc, fs);
		}

		return fs;
	}

	public static FluidStack drain(BlockPos pos, World world, int amt) {

		FluidStack fs = getOrCreateFluidStack(pos, world);

		if (fs.isEmpty() || fs.getAmount() == 0) {
			return fs;
		}

		if (fs.getFluid() == Registry.getFluid("condensed_void")) {

			if (pos.getY() > 20) {
				return FluidStack.EMPTY;
			}
			int ii = 2;
			for (int i = pos.getY() - 2; i > -1; i--) {

				if (world.getBlockState(pos.offset(Direction.DOWN, ii)).getBlock() != Blocks.AIR) {
					return FluidStack.EMPTY;
				}
				ii++;
			}
		}

		ChunkPos chunkpos = world.getChunk(pos).getPos();
		ChunkCoords cc = new ChunkCoords(world.getDimension().getType().getId(), chunkpos.x, chunkpos.z);

		Fluid f = fs.getFluid();
		if (fs.getAmount() <= amt) {
			amt = fs.getAmount();
			fs = FluidStack.EMPTY;
		} else {
			fs.shrink(amt);
		}

		CHUNK_FLUIDS.put(cc, fs);

		return new FluidStack(f, amt);
	}

}
