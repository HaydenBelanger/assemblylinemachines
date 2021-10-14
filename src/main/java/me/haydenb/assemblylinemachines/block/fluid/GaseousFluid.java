package me.haydenb.assemblylinemachines.block.fluid;

import me.haydenb.assemblylinemachines.registry.FluidRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class GaseousFluid extends ALMFluid{
	
	private final int color;
	public GaseousFluid(String name, boolean source, int color) {
		super(FluidRegistry.buildProperties(name, 0, true, false, false), source);
		
		this.color = color;
		
		/* PLUGIN DISABLED DUE TO NON UPDATE
		if(source && PluginMekanism.get().isMekanismInstalled()) {
			PluginMekanism.get().registerGas(this, name);
		}
		*/
	}
	
	@Override
	public Item getBucket() {
		return Items.BUCKET;
	}
	
	//Returns the color of fluid for Mekanism 
	public int getMekanismGasColor() {
		return color;
	}
	
}
