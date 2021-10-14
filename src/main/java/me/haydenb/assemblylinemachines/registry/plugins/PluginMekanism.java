package me.haydenb.assemblylinemachines.registry.plugins;

@Deprecated
public class PluginMekanism {
	
	//Mekanism is not-yet-updated.
	/*
	private static MekanismInterface mInt = null;
	
	public static MekanismInterface get() {
		if(mInt == null) {
			if(ModList.get().isLoaded("mekanism")) {
				try {
					mInt = Class.forName("me.haydenb.assemblylinemachines.plugins.other.PluginMekanism$MekanismPresent").asSubclass(MekanismInterface.class).newInstance();
					AssemblyLineMachines.LOGGER.info("Mekanism detected in installation. The Refinery will now work with Mekanism Gas storage...");
				}catch(Exception e) {
					mInt = new MekanismNotPresent();
					e.printStackTrace();
				}
			}else {
				mInt = new MekanismNotPresent();
			}
		}
		return mInt;
	}
	
	public interface MekanismInterface{
		
		default boolean isMekanismInstalled() {
			return false;
		}
		
		default void registerGas(GaseousFluid gf, String name) {}
		
		default void registerAllGas() {}
		
		default <T> LazyOptional<T> getRefineryCapability(Capability<T> cap, TERefinery refinery){
			return null;
		}
	}
	
	static class MekanismPresent implements MekanismInterface{

		private final DeferredRegister<Gas> gasDeferredRegister = DeferredRegister.create(Gas.class, AssemblyLineMachines.MODID);
		private final HashMap<Fluid, Gas> gasRegistry = new HashMap<>();
		
		@Override
		public boolean isMekanismInstalled() {
			return true;
		}

		@Override
		public void registerGas(GaseousFluid gf, String name) {
			Gas g = new Gas(GasBuilder.builder().color(gf.getMekanismGasColor()));
			gasRegistry.put(gf, g);
			gasDeferredRegister.register(name, () -> gasRegistry.get(gf));
			
		}

		@Override
		public void registerAllGas() {
			gasDeferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
			
		}
		
		@Override
		public <T> LazyOptional<T> getRefineryCapability(Capability<T> cap, TERefinery refinery) {
			
			if(cap == Capabilities.GAS_HANDLER_CAPABILITY) {
				IGasHandler gasHandler = new IGasHandler() {
					
					@Override
					public GasStack getEmptyStack() {
						return GasStack.EMPTY;
					}
					
					@Override
					public void setChemicalInTank(int arg0, GasStack arg1) {
						
					}
					
					@Override
					public boolean isValid(int tank, GasStack gas) {
						if(Registry.getFluid(gas.getRaw().getName()) != null) {
							return true;
						}
						
						return false;
					}
					
					@Override
					public int getTanks() {
						return 3;
					}
					
					@Override
					public long getTankCapacity(int arg0) {
						return 4000;
					}
					
					@Override
					public GasStack getChemicalInTank(int tank) {
						FluidStack x;
						if(tank == 0) {
							x = refinery.tankin;
						}else if(tank == 1) {
							x = refinery.tankouta;
						}else if(tank == 2) {
							x = refinery.tankoutb;
						}else {
							return GasStack.EMPTY;
						}
						
						Gas g = gasRegistry.get(x.getFluid());
						if(g == null) {
							return GasStack.EMPTY;
						}else {
							return new GasStack(g, x.getAmount());
						}
						
					}
					
					@Override
					public GasStack extractChemical(int tank, long amount, Action action) {
						


						FluidStack fs = refinery.fluids.drain(((int) amount), FluidAction.SIMULATE);
						
						if(fs.isEmpty() || !gasRegistry.containsKey(fs.getFluid())) {
							return GasStack.EMPTY;
						}
						
						if(action == Action.EXECUTE) {
							refinery.fluids.drain(((int) amount), FluidAction.EXECUTE);
							refinery.sendUpdates();
						}
						
						
						
						return new GasStack(gasRegistry.get(fs.getFluid()), fs.getAmount());
						
					}
					
					@Override
					public GasStack insertChemical(int tank, GasStack gas, Action action) {
						
						Fluid fl = Registry.getFluid(gas.getRaw().getName());
						if(fl == null || !(fl instanceof GaseousFluid)) {
							return gas;
						}
						
						GaseousFluid gfl = (GaseousFluid) fl;
						
						int res;
						if(action == Action.EXECUTE) {
							res = refinery.fluids.fill(new FluidStack(gfl, (int) gas.getAmount()), FluidAction.EXECUTE);
						}else {
							res = refinery.fluids.fill(new FluidStack(gfl, (int) gas.getAmount()), FluidAction.SIMULATE);
							
						}
						
						refinery.sendUpdates();
						
						if(gas.getAmount() - res == 0) {
							return GasStack.EMPTY;
						}else {
							return new GasStack(gas.getRaw(), gas.getAmount() - res);
						}
					}
					
				};
				LazyOptional<IGasHandler> lO = LazyOptional.of(() -> gasHandler);
				return lO.cast();
			}
			return null;
		}
		
	}
	
	static class MekanismNotPresent implements MekanismInterface{

	}
	
	*/
	
}
