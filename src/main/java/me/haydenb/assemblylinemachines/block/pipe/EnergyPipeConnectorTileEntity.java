package me.haydenb.assemblylinemachines.block.pipe;

import java.util.*;

import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type.MainType;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyPipeConnectorTileEntity extends BasicTileEntity implements ITickableTileEntity {

	public boolean outputMode = false;
	private int timer = 0;
	private double pendingCooldown = 0;

	private IEnergyStorage output = null;
	
	private Integer transferRate = null;

	private final TreeSet<EnergyPipeConnectorTileEntity> targets = new TreeSet<>(
			new Comparator<EnergyPipeConnectorTileEntity>() {

				@Override
				public int compare(EnergyPipeConnectorTileEntity o1, EnergyPipeConnectorTileEntity o2) {
					if (pos.distanceSq(o1.pos) > pos.distanceSq(o2.pos)) {
						return -1;
					} else {
						return 1;
					}
				}

			});
	

	public EnergyPipeConnectorTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);

	}

	public EnergyPipeConnectorTileEntity() {
		this(Registry.getTileEntity("pipe_connector_energy"));
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if (compound.contains("assemblylinemachines:output")) {
			outputMode = compound.getBoolean("assemblylinemachines:output");
		}
		if (compound.contains("assemblylinemachines:pendingcooldown")) {
			pendingCooldown = compound.getDouble("assemblylinemachines:pendingcooldown");
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("assemblylinemachines:output", outputMode);
		compound.putDouble("assemblylinemachines:pendingcooldown", pendingCooldown);
		return super.write(compound);
	}

	@Override
	public void tick() {
		if (!world.isRemote) {
			if (outputMode == true) {
				if (timer++ == 2) {
					if(transferRate == null) {
						
						PipeBase<?> pb = (PipeBase<?>) getBlockState().getBlock();
						if(pb.type == Type.ADVANCED_POWER) {
							transferRate = 5000;
						}else if(pb.type == Type.BASIC_POWER) {
							transferRate = 1000;
						}else {
							transferRate = 0;
						}
					}
					timer = 0;
					if (pendingCooldown-- <= 0) {
						pendingCooldown = 0;
						
						targets.clear();
						pathToNearestEnergy(world, pos, new ArrayList<>(), pos, targets);

						if (output == null && connectToOutput() == false) {
							return;
						}
						double waitTime = 0;
						for (EnergyPipeConnectorTileEntity tpc : targets.descendingSet()) {
							if (tpc != null) {
								int extracted = tpc.attemptAcceptPower(output.extractEnergy(transferRate, true));

								waitTime += pos.distanceSq(tpc.pos);

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
	
	public void pathToNearestEnergy(World world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, TreeSet<EnergyPipeConnectorTileEntity> targets) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.get(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				TileEntity te = world.getTileEntity(curPos);
				if(te != null && te instanceof EnergyPipeConnectorTileEntity) {
					EnergyPipeConnectorTileEntity ipc = (EnergyPipeConnectorTileEntity) te;
					targets.add(ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.offset(k);
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
			if (getBlockState().get(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
				TileEntity te = world.getTileEntity(pos.offset(d));
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
