package me.haydenb.assemblylinemachines.world;

import java.util.*;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.BlockQuantumLink.TEQuantumLink;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class QuantumLinkManager extends SavedData{

	private QuantumLinkHandler handler = null;

	private static final String DATA_PATH = AssemblyLineMachines.MODID + "_QUANTUMLINKMANAGER";

	private static QuantumLinkManager manager = null;
	private static ServerLevel csw = null;

	public QuantumLinkManager() {
		super();

	}

	public static QuantumLinkManager getInstance(MinecraftServer server) {



		if(manager == null) {
			csw = server.getLevel(Level.OVERWORLD);
			DimensionDataStorage dsdm = csw.getDataStorage();
			manager = dsdm.computeIfAbsent((compound) -> {

				if(compound.contains("assemblylinemachines:handler")) {
					QuantumLinkManager interimQlm = new QuantumLinkManager();
					AssemblyLineMachines.LOGGER.debug("Loading Quantum Link Network data from Level...");
					try {
						interimQlm.handler = Utils.GSON.fromJson(compound.getString("assemblylinemachines.handler"), QuantumLinkHandler.class);
						return interimQlm;
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				return new QuantumLinkManager();
			}, () -> {
				return new QuantumLinkManager();
			}, DATA_PATH);

		}

		return manager;
	}

	public QuantumLinkHandler getHandler() {
		if(handler == null) {
			handler = new QuantumLinkHandler();
		}

		return handler;
	}

	@Override
	public CompoundTag save(CompoundTag compound) {

		AssemblyLineMachines.LOGGER.debug("Saving Quantum Link Network data to Level...");

		if(handler != null) {
			try {
				compound.putString("assemblylinemachines:handler", Utils.GSON.toJson(handler));
			}catch(Exception e) {
				e.printStackTrace();
			}

		}

		return compound;
	}


	public static class QuantumLinkHandler{

		private HashMap<Integer, QuantumLinkNetwork> network = new HashMap<>();

		public Pair<QuantumLinkStatus, Optional<QuantumLinkNetwork>> getOrCreateQuantumLink(Integer id, Integer password) {

			QuantumLinkNetwork net;
			if(!network.containsKey(id)) {
				net = new QuantumLinkNetwork(id, password);
				network.put(id, net);
				if(manager != null) {
					manager.setDirty();
				}

				if(password != null) {
					return Pair.of(QuantumLinkStatus.CREATED_PASSWORD, Optional.of(net));
				}else {
					return Pair.of(QuantumLinkStatus.CREATED_INSECURE, Optional.of(net));
				}

			}else {
				net = network.get(id);

				if(net.password != null) {

					if(net.password.equals(password)) {
						return Pair.of(QuantumLinkStatus.JOINED_PASSWORD, Optional.of(net));
					}else {
						return Pair.of(QuantumLinkStatus.WRONG_PASSWORD, Optional.empty());
					}
				}
				return Pair.of(QuantumLinkStatus.JOINED_INSECURE, Optional.of(net));
			}
		}

		public class QuantumLinkNetwork{

			private final Integer password;
			private final Integer id;
			private transient ArrayList<TEQuantumLink> teList;

			public QuantumLinkNetwork(Integer id, Integer password) {
				this.password = password;
				this.id = id;
			}

			public void addToNetwork(TEQuantumLink te) {
				getList().add(te);
			}

			public boolean contains(TEQuantumLink te) {
				return getList().contains(te);
			}

			public void removeFromNetwork(TEQuantumLink te) {
				getList().remove(te);
			}

			public int getId() {
				return id;
			}

			private ArrayList<TEQuantumLink> getList(){
				if(teList == null) {
					teList = new ArrayList<>();
				}
				return teList;
			}

			public ItemStack attemptInsertIntoNetwork(TEQuantumLink src, ItemStack is) {

				Iterator<TEQuantumLink> iter = getList().iterator();
				while(iter.hasNext()) {
					TEQuantumLink targ = iter.next();

					if(targ.isRemoved()) {
						iter.remove();
					}else {
						if(src != targ && targ.pfi[2] == 0) {
							is = HopperBlockEntity.addItem(src, targ, is, Direction.DOWN);
							if(is.isEmpty()) {
								break;
							}
						}

					}
				}

				return is;
			}

			public FluidStack attemptInsertIntoNetwork(TEQuantumLink src, FluidStack fs) {

				Iterator<TEQuantumLink> iter = getList().iterator();
				while(iter.hasNext()) {
					TEQuantumLink targ = iter.next();

					if(targ.isRemoved()) {
						iter.remove();
					}else {
						if(src != targ && targ.pfi[1] == 0) {

							IFluidHandler ifh = targ.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
							if(ifh == null) {
								iter.remove();
							}else {
								fs.shrink(ifh.fill(fs, FluidAction.EXECUTE));
								if(fs.isEmpty()) {
									break;
								}

							}

						}

					}
				}

				return fs;
			}

			public int attemptInsertIntoNetwork(TEQuantumLink src, int power) {

				Iterator<TEQuantumLink> iter = getList().iterator();
				while(iter.hasNext()) {
					TEQuantumLink targ = iter.next();

					if(targ.isRemoved()) {
						iter.remove();
					}else {
						if(src != targ && targ.pfi[0] == 0) {

							IEnergyStorage ieh = targ.getCapability(ForgeCapabilities.ENERGY, Direction.UP).orElse(null);
							if(ieh == null) {
								iter.remove();
							}else {
								power -= ieh.receiveEnergy(power, false);
								if(power == 0) {
									break;
								}

							}

						}

					}
				}

				return power;
			}

		}
	}

	public static enum QuantumLinkStatus{
		WRONG_PASSWORD,CREATED_PASSWORD,JOINED_PASSWORD,CREATED_INSECURE,JOINED_INSECURE;
	}
}
