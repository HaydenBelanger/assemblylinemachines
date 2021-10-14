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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyPipeConnectorTileEntity extends BasicTileEntity implements ALMTicker<EnergyPipeConnectorTileEntity> {

	public boolean outputMode = false;
	private int timer = 0;
	private double pendingCooldown = 0;

	private IEnergyStorage output = null;
	
	private Integer transferRate = null;
	

	private final TreeSet<EnergyPipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<EnergyPipeConnectorTileEntity>() {

				@Override
				public int compare(EnergyPipeConnectorTileEntity o1, EnergyPipeConnectorTileEntity o2) {
					if (getBlockPos().distSqr(o1.getBlockPos()) > getBlockPos().distSqr(o2.getBlockPos())) {
						return -1;
					} else {
						return 1;
					}
				}

			});
	

	public EnergyPipeConnectorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);

	}

	public EnergyPipeConnectorTileEntity(BlockPos pos, BlockState state) {
		this(Registry.getBlockEntity("pipe_connector_energy"), pos, state);
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		if (compound.contains("assemblylinemachines:output")) {
			outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		if (compound.contains("assemblylinemachines:pendingcooldown")) {
			pendingCooldown = compound.getDouble("assemblylinemachines:pendingcooldown");
		}
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		compound.putBoolean("assemblylinemachines:output", outputMode);
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		return super.save(compound);
	}

	@Override
	public void tick() {
		if (!level.isClientSide) {
			if (outputMode == true) {
				if (timer++ == 2) {
					if(transferRate == null) {
						
						PipeBase<?> pb = (PipeBase<?>) getBlockState().getBlock();
						if(pb.type == Type.ADVANCED_POWER) {
							transferRate = 25000;
						}else if(pb.type == Type.BASIC_POWER) {
							transferRate = 5000;
						}else {
							transferRate = 0;
						}
					}
					timer = 0;
					if (pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						
						targets.clear();
						pathToNearestEnergy(this.getLevel(), this.getBlockPos(), new ArrayList<>(), this.getBlockPos(), targets);

						if (output == null && connectToOutput() == false) {
							return;
						}
						double waitTime = 0;
						for (EnergyPipeConnectorTileEntity tpc : targets.descendingSet()) {
							if (tpc != null) {
								int extracted = tpc.attemptAcceptPower(output.extractEnergy(transferRate, true));

								waitTime += this.getBlockPos().distSqr(tpc.getBlockPos());

								output.extractEnergy(extracted, false);
							}
						}
						
						pendingCooldown = waitTime / 20;
						sendUpdates();

					}
				}
			}
		}

	}
	
	public void pathToNearestEnergy(Level world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, TreeSet<EnergyPipeConnectorTileEntity> targets) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.getValue(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				BlockEntity te = world.getBlockEntity(curPos);
				if(te != null && te instanceof EnergyPipeConnectorTileEntity) {
					EnergyPipeConnectorTileEntity ipc = (EnergyPipeConnectorTileEntity) te;
					targets.add(ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.relative(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof PipeBase) {
						PipeBase<?> t = (PipeBase<?>) world.getBlockState(targPos).getBlock();
						if (t.type.getMainType() == MainType.POWER) {
							pathToNearestEnergy(world, targPos, checked, initial, targets);
						}

					}
				}

			}
		}
	}

	public int attemptAcceptPower(int energy) {
		if (outputMode) {
			return 0;
		}

		if (output == null && connectToOutput() == false) {
			return 0;
		}

		return output.receiveEnergy(energy, false);
	}

	private boolean connectToOutput() {

		for (Direction d : Direction.values()) {
			if (getBlockState().getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(d));
				if (te != null) {
					LazyOptional<IEnergyStorage> cap = te.getCapability(CapabilityEnergy.ENERGY, d.getOpposite());
					IEnergyStorage output = cap.orElse(null);
					if (output != null) {
						EnergyPipeConnectorTileEntity ipcte = this;
						cap.addListener(new NonNullConsumer<LazyOptional<IEnergyStorage>>() {

							@Override
							public void accept(LazyOptional<IEnergyStorage> t) {
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
