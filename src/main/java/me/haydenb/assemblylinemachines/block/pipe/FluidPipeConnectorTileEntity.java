package me.haydenb.assemblylinemachines.block.pipe;

import java.util.*;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type.MainType;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidPipeConnectorTileEntity extends BasicTileEntity implements ALMTicker<FluidPipeConnectorTileEntity>{

	
	public boolean outputMode = false;
	private int timer = 0;
	private double pendingCooldown = 0;
	
	private IFluidHandler output = null;
	private Integer transferRate = null;
	
	private final TreeSet<FluidPipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<FluidPipeConnectorTileEntity>() {

				@Override
				public int compare(FluidPipeConnectorTileEntity o1, FluidPipeConnectorTileEntity o2) {
					if (getBlockPos().distSqr(o1.getBlockPos()) > getBlockPos().distSqr(o2.getBlockPos())) {
						return -1;
					} else {
						return 1;
					}
				}
				
			}
	);
	
	public FluidPipeConnectorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		if(compound.contains("assemblylinemachines:output")) {
			outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		if(compound.contains("assemblylinemachines:pendingcooldown")) {
			pendingCooldown = compound.getDouble("assemblylinemachines:pendingcooldown");
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag compound) {
		compound.putBoolean("assemblylinemachines:output", outputMode);
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		return super.save(compound);
	}
	
	
	public FluidPipeConnectorTileEntity(BlockPos pos, BlockState state) {
		this(Registry.getBlockEntity("pipe_connector_fluid"), pos, state);
	}
	
	@Override
	public void tick() {
		if(!level.isClientSide) {
			if(outputMode == true) {
				if(timer++ == 40) {
					timer = 0;
					if(transferRate == null) {
						
						PipeBase<?> pb = (PipeBase<?>) getBlockState().getBlock();
						if(pb.type == Type.ADVANCED_FLUID) {
							transferRate = 5000;
						}else if(pb.type == Type.BASIC_FLUID) {
							transferRate = 1000;
						}else {
							transferRate = 0;
						}
					}
					if(pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						
						targets.clear();
						pathToNearestFluid(this.getLevel(), this.getBlockPos(), new ArrayList<>(), this.getBlockPos(), targets);
						
						if(output == null && connectToOutput() == false) {
							return;
						}
						int max = transferRate;
						FluidStack sim = output.drain(max, FluidAction.SIMULATE);
						if(sim.getAmount() < max) {
							max = sim.getAmount();
						}
						
						if(!sim.isEmpty()) {
							int extracted = 0;
							double waitTime = 0;
							
							for(FluidPipeConnectorTileEntity tpc : targets.descendingSet()) {
								if(tpc != null) {
									extracted =+ tpc.attemptAcceptFluid(sim);
									
									double thisdist = this.getBlockPos().distSqr(tpc.getBlockPos());
									
									if(thisdist > waitTime) {
										waitTime = thisdist;
									}
									
									if(extracted != 0) {
										break;
									}
								}
							}
							
							if(extracted != 0) {
								pendingCooldown = waitTime / 10;
								output.drain(extracted, FluidAction.EXECUTE);
							}
						}
						
						sendUpdates();
					}
				}
			}
		}
		
	}
	
	public void pathToNearestFluid(Level world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, TreeSet<FluidPipeConnectorTileEntity> targets) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.getValue(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				BlockEntity te = world.getBlockEntity(curPos);
				if(te != null && te instanceof FluidPipeConnectorTileEntity) {
					FluidPipeConnectorTileEntity ipc = (FluidPipeConnectorTileEntity) te;
					targets.add(ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.relative(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof PipeBase) {
						PipeBase<?> t = (PipeBase<?>) world.getBlockState(targPos).getBlock();
						if (t.type.getMainType() == MainType.FLUID) {
							pathToNearestFluid(world, targPos, checked, initial, targets);
						}

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
			if (getBlockState().getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(d));
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
