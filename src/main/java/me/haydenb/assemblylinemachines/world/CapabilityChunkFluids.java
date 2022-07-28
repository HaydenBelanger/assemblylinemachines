package me.haydenb.assemblylinemachines.world;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.FluidInGroundRecipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class CapabilityChunkFluids {

	public static final Capability<IChunkFluidCapability> CHUNK_FLUID_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	private static final LoadingCache<LevelChunk, LazyOptional<IChunkFluidCapability>> CACHED_FLUID_CHUNK_LAZIES = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build(CacheLoader.from((chunk) -> chunk.getCapability(CHUNK_FLUID_CAPABILITY, null)));

	public static LazyOptional<IChunkFluidCapability> getChunkFluidCapability(LevelChunk chunk) {
		return CACHED_FLUID_CHUNK_LAZIES.getUnchecked(chunk);
	}

	@SubscribeEvent
	public static void registerFluidManagerCapability(RegisterCapabilitiesEvent event) {
		event.register(IChunkFluidCapability.class);
	}

	@SubscribeEvent
	public static void attachFluidManagerCapability(AttachCapabilitiesEvent<LevelChunk> event) {

		ChunkFluidCapability chunkFluid = new ChunkFluidCapability(event.getObject());
		LazyOptional<IChunkFluidCapability> lazyChunkFluid = LazyOptional.of(() -> chunkFluid);
		lazyChunkFluid.addListener((lo) -> CACHED_FLUID_CHUNK_LAZIES.invalidate(event.getObject()));
		ICapabilitySerializable<CompoundTag> serializableProvider = new ICapabilitySerializable<>() {

			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
				if(cap == CapabilityChunkFluids.CHUNK_FLUID_CAPABILITY) {
					return lazyChunkFluid.cast();
				}
				return LazyOptional.empty();
			}

			@Override
			public CompoundTag serializeNBT() {
				return chunkFluid.save();
			}

			@Override
			public void deserializeNBT(CompoundTag nbt) {
				chunkFluid.load(nbt);

			}
		};

		event.addCapability(new ResourceLocation(AssemblyLineMachines.MODID, "chunk_fluid"), serializableProvider);
	}

	public static interface IChunkFluidCapability{

		/**
		 * @return The type of fluid stored within the chunk, or Fluids.EMPTY if this chunk has no reservoir.
		 */
		public Fluid getChunkFluid();

		/**
		 * This method will extract some amount of whatever fluid is stored within the chunk.
		 * @param maxDrainAmount The maximum amount of fluid to drain.
		 * @param simulate If true, the fluid will not be drained from the chunk.
		 * @return The FluidStack of the amount successfully extracted. Will be FluidStack.EMPTY if nothing could be extracted.
		 */
		public FluidStack drain(int maxDrainAmount, boolean simulate);

		/**
		 *
		 * @return The amount of contained fluid within the chunk's reservoir.
		 */
		public int getFluidAmount();

		/**
		 *
		 * @return The display name of the stored fluid.
		 */
		public Component getDisplayName();

		/**
		 * Sets the fluid in the chunk to the given fluid. Also overrides any existing fluid.
		 * @param stack The fluid to place within the chunk.
		 */
		public void setFluid(FluidStack stack);
	}

	public static class ChunkFluidCapability implements IChunkFluidCapability{

		private boolean initialized = false;
		private FluidStack storedFluid = FluidStack.EMPTY;
		private final LevelChunk chunk;

		public ChunkFluidCapability(LevelChunk chunk) {
			this.chunk = chunk;
		}

		/**
		 * @return Serialized data into a CompoundTag, preferably containing the FluidStack stored within the chunk.
		 */
		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();

			tag.put("assemblylinemachines:storedfluid", storedFluid.writeToNBT(new CompoundTag()));
			tag.putBoolean("assemblylinemachines:initialized", initialized);

			return tag;
		}

		/**
		 * Loads in the CompoundTag from the chunk storage into memory.
		 * @param tag The data saved to disk.
		 */
		public void load(CompoundTag tag) {
			storedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("assemblylinemachines:storedfluid"));
			initialized = tag.getBoolean("assemblylinemachines:initialized");
		}

		@Override
		public Fluid getChunkFluid() {
			if(!initialized) setFluid(FluidInGroundRecipe.assemble(chunk.getPos(), chunk.getLevel()));
			return storedFluid.getFluid();
		}

		@Override
		public void setFluid(FluidStack stack) {
			if(!initialized) initialized = true;
			storedFluid = stack;
			chunk.setUnsaved(true);
		}

		@Override
		public FluidStack drain(int maxDrainAmount, boolean simulate) {
			if(getChunkFluid().equals(Fluids.EMPTY)) return FluidStack.EMPTY;
			if(maxDrainAmount > storedFluid.getAmount()) {
				maxDrainAmount = storedFluid.getAmount();
			}
			FluidStack fs = new FluidStack(storedFluid.getFluid(), maxDrainAmount);
			if(!simulate) {
				storedFluid.shrink(maxDrainAmount);
				if(storedFluid.isEmpty() || storedFluid.getAmount() == 0) storedFluid = FluidStack.EMPTY;
				chunk.setUnsaved(true);
			}
			return fs;
		}

		@Override
		public int getFluidAmount() {
			if(this.getChunkFluid().equals(Fluids.EMPTY)) return 0;
			return storedFluid.getAmount();
		}

		@Override
		public Component getDisplayName() {
			if(this.getChunkFluid().equals(Fluids.EMPTY)) return FluidStack.EMPTY.getDisplayName();
			return storedFluid.getDisplayName();
		}

	}
}
