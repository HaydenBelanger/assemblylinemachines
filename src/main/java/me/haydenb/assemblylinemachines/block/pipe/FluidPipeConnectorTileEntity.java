package me.haydenb.assemblylinemachines.block.pipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.machines.ALMTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidPipeConnectorTileEntity extends ALMTileEntity implements ITickableTileEntity{

	
	public boolean outputMode = false;
	private int timer = 0;
	private double pendingCooldown = 0;
	
	private IFluidHandler output = null;
	
	private final TreeSet<FluidPipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<FluidPipeConnectorTileEntity>() {

				@Override
				public int compare(FluidPipeConnectorTileEntity o1, FluidPipeConnectorTileEntity o2) {
					if (pos.distanceSq(o1.pos) > pos.distanceSq(o2.pos)) {
						return -1;
					} else {
						return 1;
					}
				}
				
			}
	);
	
	private boolean targetsUpdated = false;
	
	public FluidPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		
	}
	
	public void updateTargets(PipeBase<?> pb) {
		if(!world.isRemote) {
			targets.clear();
			pb.pathToNearestFluid(world, pos, new ArrayList<>(), pos, targets);
		}
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if(compound.contains("assemblylinemachines:output")) {
			outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		if(compound.contains("assemblylinemachines:pendingcooldown")) {
			pendingCooldown = compound.getDouble("assemblylinemachines:pendingcooldown");
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("assemblylinemachines:output", outputMode);
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		return super.write(compound);
	}
	
	
	public FluidPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_fluid"));
	}
	
	@Override
	public void tick() {
		if(!world.isRemote) {
			if(outputMode == true) {
				if(timer++ == 40) {
					timer = 0;
					if(pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						if(targetsUpdated == false) {
							targetsUpdated = true;
							updateTargets((PipeBase<?>) Registry.getBlock("fluid_pipe"));
							((PipeBase<?>) Registry.getBlock("fluid_pipe")).updateAllAlongPath(this.world, this.pos, new ArrayList<>(), new ArrayList<>());
						}
						
						if(output == null && connectToOutput() == false) {
							return;
						}
						int max = 1000;
						for(int i = 0; i < output.getTanks(); i++) {
							FluidStack origStack = output.getFluidInTank(i);
							if(origStack != FluidStack.EMPTY) {
								FluidStack copyStack = origStack.copy();
								if(copyStack.getAmount() > max) {
									copyStack.setAmount(max);
								}
								
								int origSize = copyStack.getAmount();
								int extracted = 0;
								double waitTime = 0;
								
								for(FluidPipeConnectorTileEntity tpc : targets.descendingSet()) {
									if(tpc != null) {
										extracted =+ tpc.attemptAcceptFluid(copyStack);
										
										double thisdist = pos.distanceSq(tpc.pos);
										
										if(thisdist > waitTime) {
											waitTime = thisdist;
										}
										
										if(extracted == origSize || extracted >= max) {
											break;
										}
									}
								}
								
								if(extracted != 0) {
									pendingCooldown = waitTime / 10;
									output.drain(extracted, FluidAction.EXECUTE);
									break;
								}
							}
						}
						
						sendUpdates();
					}
				}
			}
		}
		
	}
	
	public int attemptAcceptFluid(FluidStack stack){
		if(outputMode) {
			return 0;
		}
		
		if(output == null && connectToOutput() == false) {
			return 0;
		}
		
		return output.fill(stack, FluidAction.EXECUTE);
	}
	
	private boolean connectToOutput() {
		
		for (Direction d : Direction.values()) {
			if (getBlockState().get(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				TileEntity te = world.getTileEntity(pos.offset(d));
				if (te != null) {
					LazyOptional<IFluidHandler> cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
							d.getOpposite());
					IFluidHandler output = cap.orElse(null);
					if (output != null) {
						FluidPipeConnectorTileEntity ipcte = this;
						cap.addListener(new NonNullConsumer<LazyOptional<IFluidHandler>>() {

							@Override
							public void accept(LazyOptional<IFluidHandler> t) {
								if (ipcte != null) {
									ipcte.output = null;
								}
							}
						});

						this.output = output;
						return true;
					}
				}

			}
		}

		return false;
	}
}
