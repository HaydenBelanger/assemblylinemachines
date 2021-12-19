package me.haydenb.assemblylinemachines.world;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe.FluidInGroundCriteria;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class FluidLevelManager {

	private static final ConcurrentHashMap<ChunkCoords, FluidStack> CHUNK_FLUIDS = new ConcurrentHashMap<>();

	public static void readData(ChunkPos pos, LevelAccessor world, CompoundTag nbt) {

		ChunkCoords cc = new ChunkCoords(world.dimensionType().logicalHeight(), pos.x, pos.z);

		if (nbt.contains("assemblylinemachines:chunkfluid")) {

			CHUNK_FLUIDS.put(cc, FluidStack.loadFluidStackFromNBT(nbt.getCompound("assemblylinemachines:chunkfluid")));
		}
	}

	public static void writeData(ChunkAccess chunk, LevelAccessor world, CompoundTag nbt) {
		ChunkCoords cc = new ChunkCoords(world.dimensionType().logicalHeight(), chunk.getPos().x, chunk.getPos().z);
		if (CHUNK_FLUIDS.containsKey(cc)) {

			CompoundTag sub = new CompoundTag();
			CHUNK_FLUIDS.get(cc).writeToNBT(sub);
			nbt.put("assemblylinemachines:chunkfluid", sub);

		}
	}

	public static void clearData(LevelAccessor world, ChunkPos pos) {
		int dimid = world.dimensionType().logicalHeight();
		CHUNK_FLUIDS.remove(new ChunkCoords(dimid, pos.x, pos.z));
	}

	public static FluidStack getOrCreateFluidStack(BlockPos pos, Level world) {

		ChunkPos chunkpos = world.getChunk(pos).getPos();
		ChunkCoords cc = new ChunkCoords(world.dimensionType().logicalHeight(), chunkpos.x, chunkpos.z);
		FluidStack fs = CHUNK_FLUIDS.get(cc);
		if (fs == null) {

			List<FluidInGroundRecipe> recipes = world.getRecipeManager().getRecipesFor(FluidInGroundRecipe.FIG_RECIPE, null, world);
			
			float tc = world.getBiome(pos).getBaseTemperature();
			ResourceLocation dim = world.dimension().location();
			for (FluidInGroundRecipe recipe : recipes) {

				int chance = recipe.getChance();
				boolean half = false;

				if (dim.equals(DimensionType.OVERWORLD_LOCATION.location())) {

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
				}else if(dim.equals(DimensionType.NETHER_LOCATION.location())) {
					if(recipe.getCriteria() != FluidInGroundCriteria.NETHER) {
						chance = -1;
					}
				}else if(dim.equals(DimensionType.END_LOCATION.location())) {
					if(recipe.getCriteria() != FluidInGroundCriteria.END) {
						chance = -1;
					}
				}/*else if(dim.equals(DimensionChaosPlane.CHAOS_PLANE_LOCATION.location())){
					if(recipe.getCriteria() != FluidInGroundCriteria.CHAOS_PLANE) {
						chance = -1;
					}
				}*/else {
					chance = -1;
				}

				if (chance != -1) {
					if (world.getRandom().nextInt(100) <= chance) {

						int amt = (recipe.getMinimum() + world.getRandom().nextInt(recipe.getMaximum() - recipe.getMinimum())) * 10000;

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

	public static FluidStack drain(BlockPos pos, Level world, int amt) {

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

				if (world.getBlockState(pos.relative(Direction.DOWN, ii)).getBlock() != Blocks.AIR) {
					return FluidStack.EMPTY;
				}
				ii++;
			}
		}

		ChunkPos chunkpos = world.getChunk(pos).getPos();
		ChunkCoords cc = new ChunkCoords(world.dimensionType().logicalHeight(), chunkpos.x, chunkpos.z);

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

	public static class ChunkCoords {
		public int dimID;
		public int posX;
		public int posZ;

		public ChunkCoords(int dim, int x, int z) {
			dimID = dim;
			posX = x;
			posZ = z;
		}
		
		@Override
		public int hashCode() {
			return dimID * posX * posZ;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ChunkCoords) {
				ChunkCoords chunk = (ChunkCoords) obj;
				return chunk.dimID == dimID && chunk.posX == posX && chunk.posZ == posZ;
			}else {
				return false;
			}
			
		}
		
		@Override
		public String toString() {
			return "[" + dimID + "]: " + posX + ", " + posZ;
		}
	}
	
	//Three events related to the storage of Fluid Level per chunk.
	@SubscribeEvent
	public static void chunkLoad(ChunkDataEvent.Load event) {
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.readData(event.getChunk().getPos(), event.getWorld(), event.getData());
		}
	}

	@SubscribeEvent
	public static void chunkSave(ChunkDataEvent.Save event) {
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.writeData(event.getChunk(), event.getWorld(), event.getData().getCompound("Level"));
		}
	}
	
	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.clearData(event.getWorld(), event.getChunk().getPos());
		}
	}
}
