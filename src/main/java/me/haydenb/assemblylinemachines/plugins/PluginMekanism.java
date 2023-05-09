package me.haydenb.assemblylinemachines.plugins;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class PluginMekanism {

	/*
	public static final Lazy<MekanismInterface> INTERFACE = Lazy.of(() -> {
		if(ModList.get().isLoaded("mekanism")) {
			try {
				return Class.forName("me.haydenb.assemblylinemachines.plugins.PluginMekanism$MekanismPresent").asSubclass(MekanismInterface.class).getDeclaredConstructor().newInstance();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return new MekanismInterface() {};
	});
	*/
	
	public static final Lazy<MekanismInterface> INTERFACE = Lazy.of(() -> {
		return new MekanismInterface() {};
	});

	public interface MekanismInterface{

		default void registerGases() {}

		default <T> LazyOptional<T> getFluidGasWrapper(Capability<T> cap, IFluidHandler handler){
			return LazyOptional.empty();
		}

	}

	/*
	static class MekanismPresent implements MekanismInterface{

		private final BiMap<Fluid, Gas> gas = HashBiMap.create();

		@Override
		public void registerGases() {

			FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterEvent event) -> {
				event.register(MekanismAPI.gasRegistryName(), (h) -> {
					Registry.getAllFluids().stream().filter((e) -> e.getValue().getFluidType().isLighterThanAir() && e.getValue().getFluidType() instanceof SpecialRenderFluidType).forEach((e) -> {
						SpecialRenderFluidType srft = (SpecialRenderFluidType) e.getValue().getFluidType();
						Gas g = new Gas(GasBuilder.builder().hidden().color(ARGB32.color(255, srft.colorI.getX(), srft.colorI.getY(), srft.colorI.getZ())));
						gas.put(e.getValue(), g);
						h.register(new ResourceLocation(AssemblyLineMachines.MODID, e.getKey()), g);
					});
				});
			});
		}

		@Override
		public <T> LazyOptional<T> getFluidGasWrapper(Capability<T> cap, IFluidHandler handler) {
			if(cap == Capabilities.GAS_HANDLER) {
				return LazyOptional.of(() -> new IGasHandler() {

					@Override
					public void setChemicalInTank(int tank, GasStack stack) {}

					@Override
					public GasStack getEmptyStack() {
						return GasStack.EMPTY;
					}

					@Override
					public boolean isValid(int tank, GasStack stack) {
						return gas.containsValue(stack.getType());
					}

					@Override
					public int getTanks() {
						return handler.getTanks();
					}

					@Override
					public long getTankCapacity(int tank) {
						return handler.getTankCapacity(tank);
					}

					@Override
					public GasStack getChemicalInTank(int tank) {
						FluidStack fs = handler.getFluidInTank(tank);
						if(!fs.isEmpty() && gas.containsKey(fs.getFluid())) return gas.get(fs.getFluid()).getStack(fs.getAmount());
						return GasStack.EMPTY;
					}

					@Override
					public GasStack extractChemical(int tank, long amount, Action action) {
						FluidStack exFs = handler.drain((int) amount, FluidAction.SIMULATE);
						if(exFs.isEmpty() || !gas.containsKey(exFs.getFluid())) return GasStack.EMPTY;
						if(action == Action.EXECUTE) handler.drain((int) amount, FluidAction.EXECUTE);
						return gas.get(exFs.getFluid()).getStack(exFs.getAmount());
					}

					@Override
					public GasStack insertChemical(int tank, GasStack stack, Action action) {
						if(gas.isEmpty() || !gas.containsValue(stack.getType())) return stack;
						FluidStack insFs = new FluidStack(gas.inverse().get(stack.getType()), (int) stack.getAmount());
						int res = handler.fill(insFs, action.toFluidAction());
						return stack.getType().getStack(stack.getAmount() - res);
					}

				}).cast();
			}

			return LazyOptional.empty();
		}
	}
	*/
}