package me.haydenb.assemblylinemachines.util;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.fluid.Fluid;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

@SuppressWarnings("deprecation")
public class FluidProperty {

	public static final EnumProperty<Fluids> FLUID = EnumProperty.create("fluid", Fluids.class);
	
	
	public static enum Fluids implements IStringSerializable {
		NONE(null), WATER(net.minecraft.fluid.Fluids.WATER), LAVA(net.minecraft.fluid.Fluids.LAVA);

		private Fluid f;
		
		Fluids(Fluid f){
			this.f = f;
		}
		@Override
		public String getName() {
			return toString().toLowerCase();
		}
		
		public Fluid getAssocFluid() {
			return f;
			
		}
		
		public String getFriendlyName() {
			return WordUtils.capitalizeFully(toString().toLowerCase());
		}
		
		public static Fluids getAssocFluids(Fluid f) {
			
			for(Fluids ff : values()) {
				if(ff.getAssocFluid() != null) {
					if(ff.getAssocFluid().equals(f)){
						return ff;
					}
				}
			}
			return NONE;
			
		}
	}
}
