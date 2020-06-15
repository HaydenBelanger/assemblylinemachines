package me.haydenb.assemblylinemachines.util.machines;

import java.util.ArrayList;

import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.Utils.Pair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class ALMMachineEnergyBased<A extends Container> extends ALMMachineNoExtract<A> {

	public EnergyProperties properties;
	public int amount = 0;
	public float fept = 0;
	public boolean enabled = true;
	protected IEnergyStorage energy = new IEnergyStorage() {
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(!canReceive()) {
				return 0;
			}
			
			if(properties.capacity < maxReceive + amount) {
				maxReceive = properties.getCapacity() - amount;
			}
			
			if(simulate == false) {
				amount += maxReceive;
				sendUpdates();
			}
			
			return maxReceive;
		}
		
		@Override
		public int getMaxEnergyStored() {
			return properties.getCapacity();
		}
		
		@Override
		public int getEnergyStored() {
			return amount;
		}
		
		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if(!canExtract()) {
				return 0;
			}
			if(maxExtract > amount) {
				maxExtract = amount;
			}
			
			if(simulate == false) {
				amount -= maxExtract;
				sendUpdates();
			}
			
			return maxExtract;
		}
		
		@Override
		public boolean canReceive() {
			return properties.getIn();
		}
		
		@Override
		public boolean canExtract() {
			return properties.getOut();
		}
	};
	protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);
	
	
	public ALMMachineEnergyBased(TileEntityType<?> tileEntityTypeIn, int slotCount, TranslationTextComponent name, int containerId,
			Class<A> clazz, EnergyProperties properties) {
		super(tileEntityTypeIn, slotCount, name, containerId, clazz);
		this.properties = properties;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		if(compound.contains("assemblylinemachines:stored")) {
			amount = compound.getInt("assemblylinemachines:stored");
		}
		if(compound.contains("assemblylinemachines:fept")) {
			fept = compound.getFloat("assemblylinemachines:fept");
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("assemblylinemachines:stored", amount);
		compound.putFloat("assemblylinemachines:fept", fept);
		return super.write(compound);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap) {
		if(cap == CapabilityEnergy.ENERGY) {
			return energyHandler.cast();
		}
		
		return super.getCapability(cap);
	}
	
	@Override
	public void remove() {
		super.remove();
		if(energyHandler != null) {
			energyHandler.invalidate();
		}
	}
	
	
	public static class EnergyProperties{
		private boolean in;
		private boolean out;
		private int capacity;
		
		public EnergyProperties(boolean in, boolean out, int capacity) {
			this.in = in;
			this.out = out;
			this.capacity = capacity;
		}
		
		public int getCapacity() {
			return capacity;
		}
		
		public boolean getIn() {
			return in;
		}
		
		public boolean getOut() {
			return out;
		}
	}
	
	public static class ScreenALMEnergyBased<T extends Container> extends ScreenALMBase<T>{

		protected final Pair<Integer, Integer> energyMeterLoc;
		protected final ALMMachineEnergyBased<T> machine;
		protected final boolean usesfept;
		protected final int startx;
		
		public ScreenALMEnergyBased(T screenContainer, PlayerInventory inv, ITextComponent titleIn,
				Pair<Integer, Integer> size, Pair<Integer, Integer> titleTextLoc, Pair<Integer, Integer> invTextLoc,
				String guipath, boolean hasCool, Pair<Integer, Integer> energyMeterLoc,
				ALMMachineEnergyBased<T> machine, boolean usesfept) {
			super(screenContainer, inv, titleIn, size, titleTextLoc, invTextLoc, guipath, hasCool);
			this.energyMeterLoc = energyMeterLoc;
			this.startx = 176;
			this.machine = machine;
			this.usesfept = usesfept;
		}
		
		public ScreenALMEnergyBased(T screenContainer, PlayerInventory inv, ITextComponent titleIn,
				Pair<Integer, Integer> size, Pair<Integer, Integer> titleTextLoc, Pair<Integer, Integer> invTextLoc,
				String guipath, boolean hasCool, Pair<Integer, Integer> energyMeterLoc,
				ALMMachineEnergyBased<T> machine, int startx, boolean usesfept) {
			super(screenContainer, inv, titleIn, size, titleTextLoc, invTextLoc, guipath, hasCool);
			this.energyMeterLoc = energyMeterLoc;
			this.startx = startx;
			this.machine = machine;
			this.usesfept = usesfept;
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if (mouseX >= x + energyMeterLoc.x && mouseY >= y + energyMeterLoc.y && mouseX <= x + energyMeterLoc.x + 15 && mouseY <= y + energyMeterLoc.y + 51) {

				if(Screen.hasShiftDown()) {
					ArrayList<String> str = new ArrayList<>();
					str.add(Utils.FORMAT.format(machine.amount) + "/" + Utils.FORMAT.format(machine.properties.capacity) + "FE");
					if(usesfept) {
						
						
						str.add(Utils.FEPT_FORMAT.format(machine.fept) + " FE/tick");
					}
					this.renderTooltip(str,
							mouseX - x, mouseY - y);
				}else {
					this.renderTooltip(Utils.format(machine.amount) + "/" + Utils.format(machine.properties.capacity) + "FE",
							mouseX - x, mouseY - y);
				}
				
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			int prog = Math.round(((float) machine.amount / (float) machine.properties.capacity) * 52F);
			super.blit(x + energyMeterLoc.x, y + energyMeterLoc.y + (52 - prog), startx, (52 - prog), 16, prog);
		}
		
		
	}

}
