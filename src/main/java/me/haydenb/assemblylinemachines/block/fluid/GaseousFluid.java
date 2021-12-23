package me.haydenb.assemblylinemachines.block.fluid;

import me.haydenb.assemblylinemachines.registry.FluidRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class GaseousFluid extends ALMFluid{
	
	private final int color;
	public GaseousFluid(String name, boolean source, int color) {
		super(FluidRegistration.buildProperties(name, 0, true, false, false), source);
		
		this.color = color;
	}
	
	@Override
	public Item getFilledBucket() {
		return Items.BUCKET;
	}
	
	//Returns the color of fluid for Mekanism 
	public int getMekanismGasColor() {
		return color;
	}
	
}
