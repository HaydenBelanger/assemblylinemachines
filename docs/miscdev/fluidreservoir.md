# Fluid Reservoir Capability

As part of standard gameplay, the player has the opportunity to use the Mystium Dowsing Rod to search chunks for fluid reservoirs contained within. If they are able to find one, they can use the Pump and the Pumpshaft to extract the fluids from within. As a mod developer, Assembly Line Machines exposes a custom capability for this system, allowing you to view, access, and extract the stored fluid within a chunk.

!!! error "1.18+ Feature"
    A capability has only been exposed as of version 1.18-1.3.4. Prior to this version, the stored fluids in a chunk was managed through raw NBT and therefore did not have a true capability that could be accessed.

## Obtaining the Capability

The capability can be obtained by calling `LevelChunk#getCapability`. The instance of the Capability can obtained by either accessing it via `CapabilityManager.get`, casting to `Capability<IChunkFluidCapability>`, or by accessing the stored singleton at `FluidCapability.CHUNK_FLUID_CAPABILITY`. Sides are not used, so the side within obtaining the capability can be `null`. Finally, cast the `LazyOptional<IChunkFluidCapability>` to `IChunkFluidCapability` using your preferred method, and you have successfully obtained an instance of the Fluid Reservoir capability!

## Methods

The capability contains a handful of methods to access and modify the stored fluid. Please note that at the time of generation, the fluid contained within a chunk is not yet set, and the fluid is only calculated when the capability is accessed for the first time. A random fluid, or an empty fluid, is obtained randomly from the available and eligible [Fluid-in-Ground Recipes](../recipes/fluidinground.md) at the time of capability accessing.

`IChunkFluidCapability#getChunkFluid`:  
Returns the `Fluid` contained within the chunk, or `Fluids.EMPTY` (`minecraft:empty`) if the chunk is empty or does not have a fluid.

`IChunkFluidCapability#getFluidAmount`:  
Returns an `int` for the mB value of the fluid contained within the chunk, or 0 if the chunk is empty or does not have a fluid.

`IChunkFluidCapability#getDisplayName`:  
Returns a `Component` of the friendly display name of the fluid stored within the chunk. Returns "Empty" if the chunk is empty or does not have a fluid.

`IChunkFluidCapability#drain`:  
- `maxDrainAmount (int)`: *The maximum amount of fluid to be extracted from the chunk. The actual amount extracted may be less, but it will not be more.*  
- `simulate (boolean)`: *If this is true, the method will return an accurate result, but it will not impact and reduce the actual stored amount of fluid within the chunk. Good for testing to make sure the fluid can fit into a tank or something else.*

This method, based on the parameters, will return a `FluidStack` of the stored fluid, as well as the amount of fluid successfully extracted, based on the parameters. Returns `FluidStack.EMPTY` if the chunk is empty or does not have a fluid.

!!! tip
    In the default implementation class of `IChunkFluidCapability` provided, whenever any action happens in which data is changed, for example setting the fluid stored within the chunk or draining any amount, the chunk is automatically marked as dirty by the API, so you need not worry about the capability data saving.