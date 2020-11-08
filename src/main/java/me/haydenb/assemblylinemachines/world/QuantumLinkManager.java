package me.haydenb.assemblylinemachines.world;

import java.util.*;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.utility.BlockQuantumLink.TEQuantumLink;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class QuantumLinkManager extends WorldSavedData{

	private static final Gson GSON = new Gson();
	
	private QuantumLinkHandler handler = null;
	
	private static final String DATA_PATH = AssemblyLineMachines.MODID + "_QUANTUMLINKMANAGER";
	
	private static QuantumLinkManager manager = null;
	private static ServerWorld csw = null;
	
	public QuantumLinkManager() {
		super(DATA_PATH);
		
	}

	public static QuantumLinkManager getInstance(MinecraftServer server) {
		
		
		
		if(manager == null) {
			csw = server.getWorld(ServerWorld.field_234918_g_);
			DimensionSavedDataManager dsdm = csw.getSavedData();
			manager = dsdm.getOrCreate(QuantumLinkManager::new, DATA_PATH);
			
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
	public void read(CompoundNBT compound) {
		if(compound.contains("assemblylinemachines:handler")) {
			
			AssemblyLineMachines.LOGGER.info("Loading Quantum Link Network data from World...");
			
			try {
				
				handler = GSON.fromJson(compound.getString("assemblylinemachines:handler"), QuantumLinkHandler.class);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("A fatal exception was thrown deserializing the Quantum Link Manager: ");
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		AssemblyLineMachines.LOGGER.info("Saving Quantum Link Network data to World...");
		
		if(handler != null) {
			try {
				compound.putString("assemblylinemachines:handler", GSON.toJson(handler));
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("A fatal exception was thrown serializing the Quantum Link Manager: ");
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
					manager.markDirty();
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
							is = HopperTileEntity.putStackInInventoryAllSlots(src, targ, is, Direction.DOWN);
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
							
							IFluidHandler ifh = targ.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).orElse(null);
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
							
							IEnergyStorage ieh = targ.getCapability(CapabilityEnergy.ENERGY, Direction.UP).orElse(null);
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
