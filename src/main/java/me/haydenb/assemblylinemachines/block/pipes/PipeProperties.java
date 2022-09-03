package me.haydenb.assemblylinemachines.block.pipes;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class PipeProperties {

	public static final HashMap<Direction, EnumProperty<PipeConnOptions>> DIRECTION_BOOL = new HashMap<>();
	static {
		for(Direction d : Direction.values()) {
			DIRECTION_BOOL.put(d, EnumProperty.create(d.getSerializedName(), PipeConnOptions.class));
		}
	}


	public static enum PipeConnOptions implements StringRepresentable{
		NONE, PIPE, CONNECTOR;

		@Override
		public String getSerializedName() {
			return toString().toLowerCase();
		}
	}

	public static enum PipeType {
		BASIC(1, ""), ADVANCED(5, "advanced_"), ULTIMATE(25, "ultimate_");

		private final int baseMultiplier;
		private final String prefix;

		PipeType(int baseMultiplier, String prefix){
			this.baseMultiplier = baseMultiplier;
			this.prefix = prefix;
		}

		public int getBaseMultiplier() {
			return baseMultiplier;
		}

		public String getPrefix() {
			return prefix;
		}

	}

	public static enum TransmissionType {

		POWER(2, 5000, 1.5f, "energy_", "energy_pipe", "IEnergyStorage", "Energy", Pair.of(Pair.of(28, 58), Pair.of(39, 58))),
		FLUID(40, 1000, 2f, "fluid_", "fluid_pipe", "IFluidHandler", "Fluid", Pair.of(Pair.of(28, 47), Pair.of(39, 47))),
		ITEM(50, 1, 4f, "item_", "item_pipe", "IItemHandler", "Item", Pair.of(Pair.of(28, 36), Pair.of(39, 36))),
		OMNI(20, 0, 0f, "omni", "omnipipe", null, null, null);

		private static final TransmissionType[] TRANSMISSIONS_FOR_OMNIPIPE = {POWER, FLUID, ITEM};

		private final int nTimerBase;
		private final int transferBase;
		private final float stackUpgradeMultiplier;
		private final String name;
		private final String descriptionId;
		private final String capabilityName;
		private final String prettyName;
		private final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> omniCoords;

		TransmissionType(int nTimerBase, int transferBase, float stackUpgradeMultiplier, String name, String descriptionId,
				String capabilityName, String prettyName, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> omniCoords){
			this.nTimerBase = nTimerBase;
			this.transferBase = transferBase;
			this.stackUpgradeMultiplier = stackUpgradeMultiplier;
			this.name = name;
			this.descriptionId = Util.makeDescriptionId("tileEntity", new ResourceLocation(AssemblyLineMachines.MODID, descriptionId));
			this.capabilityName = capabilityName;
			this.prettyName = prettyName;
			this.omniCoords = omniCoords;
		}

		public static ArrayList<Triple<String, PipeType, TransmissionType>> getPipeRegistryValues(){
			ArrayList<Triple<String, PipeType, TransmissionType>> list = new ArrayList<>();
			for(PipeType pt : PipeType.values()) {
				for(TransmissionType tt : TransmissionType.values()) {
					if(!(pt == PipeType.BASIC && tt == TransmissionType.OMNI)) list.add(Triple.of(pt.prefix + tt.name + "pipe", pt, tt));
				}
			}
			return list;
		}

		public boolean hasCapability(Direction dir, BlockEntity blockEntity) {
			if(blockEntity != null) {
				switch(this) {
				case FLUID:
					if(blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).orElse(null) != null) return true;
					break;
				case ITEM:
					if(blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(null) != null) return true;
					break;
				case OMNI:
					for(TransmissionType tt : TRANSMISSIONS_FOR_OMNIPIPE) {
						if(tt.hasCapability(dir, blockEntity)) {
							return true;
						}
					}
					break;
				case POWER:
					if(blockEntity.getCapability(ForgeCapabilities.ENERGY, dir).orElse(null) != null) return true;
					break;
				}
			}
			return false;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Pair<Object, TriConsumer<PipeConnectorTileEntity, Object, Integer>> getCapability(BlockEntity te, Direction dir, PipeConnectorTileEntity pipeConnector) {
			if(te != null) {
				LazyOptional<?> lx;
				Class<?> clazz;
				switch(this) {
				case FLUID:
					lx = te.getCapability(ForgeCapabilities.FLUID_HANDLER, dir);
					clazz = IFluidHandler.class;
					break;
				case ITEM:
					lx = te.getCapability(ForgeCapabilities.ITEM_HANDLER, dir);
					clazz = IItemHandler.class;
					break;
				case POWER:
					lx = te.getCapability(ForgeCapabilities.ENERGY, dir);
					clazz = IEnergyStorage.class;
					break;
				case OMNI:
					for(TransmissionType tt : TRANSMISSIONS_FOR_OMNIPIPE) {
						Pair<Object, TriConsumer<PipeConnectorTileEntity, Object, Integer>> res = tt.getCapability(te, dir, pipeConnector);
						if(res != null) {
							return res;
						}
					}
					return null;
				default:
					lx = LazyOptional.empty();
					clazz = null;
					break;
				}
				if(lx.isPresent() && clazz != null) {
					Object res = lx.orElse(null);
					if(res != null) {
						String key = getTransmissionFromCapability(res).getCapabilityName();
						if(key != null) {
							TriConsumer<PipeConnectorTileEntity, Object, Integer> cons = ProcessingAssistant.CONNECTOR_DEFAULT_PROCESSING.get(key);
							lx.addListener(new NonNullConsumer() {

								@Override
								public void accept(Object t) {
									pipeConnector.conCapabilities.remove(t);

								}
							});

							return Pair.of(clazz.cast(res), cons);
						}
					}
				}
			}
			return null;

		}

		public int getNTimerBase(int upgradeAmount) {
			float modifier = 1f;
			switch(upgradeAmount) {
			case 3:
				modifier = 0.25f;
				break;
			case 2:
				modifier = 0.5f;
				break;
			case 1:
				modifier = 0.75f;
			}
			return Math.round(this.nTimerBase * modifier);
		}

		public int getTransferBase() {
			return transferBase;
		}

		public String getCapabilityName() {
			return capabilityName;
		}

		public String getGUITexture(PipeType pt) {
			if(pt == PipeType.BASIC) {
				return "pipes/basic_pipe_connector";
			}else {
				if(this == TransmissionType.OMNI) {
					return "pipes/upgraded_pipe_connector_omni";
				}else if(this == TransmissionType.POWER) {
					return "pipes/upgraded_pipe_connector_energy";
				}else {
					return "pipes/upgraded_pipe_connector_fluid_item";
				}
			}
		}

		public float getStackUpgradeMultiplier(float upgradeCount) {
			return stackUpgradeMultiplier * upgradeCount;
		}

		private int getMaxTransfer(PipeConnectorTileEntity pipeConnector, PipeType pt) {

			int base = this.getTransferBase() * pt.getBaseMultiplier();
			int upgradeCount = pipeConnector.getUpgradeAmount(Upgrades.PIPE_STACK);
			if(upgradeCount != 0) base = Math.round(base * this.getStackUpgradeMultiplier(upgradeCount));
			if(this == TransmissionType.ITEM) base = Math.min(base, 64);
			return base;
		}

		public int getMaxTransfer(PipeConnectorTileEntity pipeConnector, PipeType pt, Object capability) {
			TransmissionType tt = this;
			if(tt == TransmissionType.OMNI) {
				tt = getTransmissionFromCapability(capability);
			}
			return tt.getMaxTransfer(pipeConnector, pt);
		}

		public String getName() {
			return name;
		}

		public String getDescriptionId() {
			return descriptionId;
		}

		public String getPrettyName() {
			return prettyName;
		}

		public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getOmniCoords() {
			return omniCoords;
		}

		public static TransmissionType getTransmissionFromCapability(Object capability) {
			if(capability instanceof IItemHandler) {
				return TransmissionType.ITEM;
			}else if(capability instanceof IFluidHandler) {
				return TransmissionType.FLUID;
			}else if(capability instanceof IEnergyStorage) {
				return TransmissionType.POWER;
			}
			return null;
		}

	}

	public static class ProcessingAssistant{
		public static final HashMap<String, TriConsumer<PipeConnectorTileEntity, Object, Integer>> CONNECTOR_DEFAULT_PROCESSING = new HashMap<>();
		static {
			CONNECTOR_DEFAULT_PROCESSING.put(TransmissionType.ITEM.getCapabilityName(), (pipeConnector, handler, max) -> {
				IItemHandler output = (IItemHandler) handler;
				for (int i = 0; i < output.getSlots(); i++) {
					ItemStack origStack = output.extractItem(i, max, true);
					if (origStack != ItemStack.EMPTY && pipeConnector.checkWhiteBlackList(origStack)) {
						ItemStack copyStack = origStack.copy();
						if (copyStack.getCount() > max) {
							copyStack.setCount(max);
						}
						int origSize = copyStack.getCount();
						int extracted = 0;

						double waitTime = 0;
						for (PipeConnectorTileEntity ipc : pipeConnector.targets.descendingSet()) {
							if (ipc != null) {
								extracted = +ipc.attemptAccept(IItemHandler.class, copyStack);

								double thisdist = pipeConnector.getBlockPos().distSqr(ipc.getBlockPos());
								if (thisdist > waitTime) {
									waitTime = thisdist;
								}
								if (extracted == origSize || extracted >= max) {
									break;
								}

							}
						}

						if (extracted != 0) {
							pipeConnector.pendingCooldown = (waitTime * extracted) / 140;
							output.extractItem(i, extracted, false);
							break;
						}

					}

				}
			});

			CONNECTOR_DEFAULT_PROCESSING.put(TransmissionType.FLUID.getCapabilityName(), (pipeConnector, handler, max) -> {
				IFluidHandler output = (IFluidHandler) handler;


				FluidStack sim = output.drain(max, FluidAction.SIMULATE);
				if(sim.getAmount() < max) {
					max = sim.getAmount();
				}

				if(!sim.isEmpty() && pipeConnector.checkWhiteBlackList(sim)) {
					int extracted = 0;
					double waitTime = 0;

					for(PipeConnectorTileEntity tpc : pipeConnector.targets.descendingSet()) {
						if(tpc != null) {
							extracted =+ tpc.attemptAccept(IFluidHandler.class, sim);

							double thisdist = pipeConnector.getBlockPos().distSqr(tpc.getBlockPos());

							if(thisdist > waitTime) {
								waitTime = thisdist;
							}

							if(extracted != 0) {
								break;
							}
						}
					}

					if(extracted != 0) {
						pipeConnector.pendingCooldown = waitTime / 10;
						output.drain(extracted, FluidAction.EXECUTE);
					}
				}
			});

			CONNECTOR_DEFAULT_PROCESSING.put(TransmissionType.POWER.getCapabilityName(), (pipeConnector, handler, transferRate) -> {
				IEnergyStorage output = (IEnergyStorage) handler;
				double waitTime = 0;
				for (PipeConnectorTileEntity tpc : pipeConnector.targets.descendingSet()) {
					if (tpc != null) {
						int extracted = tpc.attemptAccept(IEnergyStorage.class, output.extractEnergy(transferRate, true));

						waitTime += pipeConnector.getBlockPos().distSqr(tpc.getBlockPos());

						output.extractEnergy(extracted, false);
					}
				}

				pipeConnector.pendingCooldown = waitTime / 20;
			});
		}
	}
}
