package me.haydenb.assemblylinemachines.registry.utils;

import java.util.Optional;
import java.util.function.*;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IFluidHandlerBypass extends IFluidHandler{

	public FluidStack drainBypassRestrictions(FluidStack resource, FluidAction action);

	public FluidStack drainBypassRestrictions(int maxDrain, FluidAction action);

	/**
	 * Returns a simple 1-tank IFluidHandler.
	 * @param getOrSetStack Return the ORIGINAL (NON-COPY) stack, and if a stack is passed in the optional, set it to be the new FluidStack.
	 * @param isValid Predicate to check to make sure a stack is valid. Make null to ignore predicate.
	 * @param updateData A Void consumer which simply must run the client-server sync method, typically sendUpdates in most blocks.
	 */
	public static IFluidHandler getSimpleOneTankHandler(Predicate<FluidStack> isValid, int capacity, Function<Optional<FluidStack>, FluidStack> getOrSetStack, Consumer<Void> updateData, boolean drainingAllowed) {

		return new IFluidHandlerBypass() {

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				if(!getFluidInTank(0).isEmpty() && !stack.getFluid().equals(getFluidInTank(0).getFluid())) return false;
				return isValid != null ? isValid.test(stack) : true;
			}

			@Override
			public int getTanks() {
				return 1;
			}

			@Override
			public int getTankCapacity(int tank) {
				return capacity;
			}

			@Override
			public FluidStack getFluidInTank(int tank) {
				return getOrSetStack.apply(Optional.empty());
			}

			@Override
			public int fill(FluidStack resource, FluidAction action) {
				int toFill = !isFluidValid(0, resource) ? 0 : resource.getAmount() + getFluidInTank(0).getAmount() >= getTankCapacity(0) ? getTankCapacity(0) - getFluidInTank(0).getAmount() : resource.getAmount();
				if(toFill != 0 && action == FluidAction.EXECUTE) {
					if(getFluidInTank(0).isEmpty()) {
						getOrSetStack.apply(Optional.of(new FluidStack(resource, toFill)));
					}else {
						getFluidInTank(0).grow(toFill);
					}
					updateData.accept(null);
				}
				return toFill;
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				if(!drainingAllowed) return FluidStack.EMPTY;
				return drainBypassRestrictions(resource, action);
			}

			@Override
			public FluidStack drainBypassRestrictions(FluidStack resource, FluidAction action) {
				if(getFluidInTank(0).isEmpty() || !resource.getFluid().equals(getFluidInTank(0).getFluid())) return FluidStack.EMPTY;
				if(resource.getAmount() >= getFluidInTank(0).getAmount()) resource.setAmount(getFluidInTank(0).getAmount());
				FluidStack returnStack = new FluidStack(getFluidInTank(0).getFluid(), resource.getAmount());
				if(action == FluidAction.EXECUTE) {
					getFluidInTank(0).shrink(returnStack.getAmount());
					updateData.accept(null);
				}
				return returnStack;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				return getFluidInTank(0).isEmpty() ? FluidStack.EMPTY : drain(new FluidStack(getFluidInTank(0).getFluid(), maxDrain), action);
			}

			@Override
			public FluidStack drainBypassRestrictions(int maxDrain, FluidAction action) {
				return getFluidInTank(0).isEmpty() ? FluidStack.EMPTY : drainBypassRestrictions(new FluidStack(getFluidInTank(0).getFluid(), maxDrain), action);
			}

		};
	}
}