package me.haydenb.assemblylinemachines.util;

import net.minecraft.fluid.Fluid;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public class FluidProperty {

	public static final EnumProperty<Fluids> FLUID = EnumProperty.create("fluid", Fluids.class);
	
	
	public static enum Fluids implements IStringSerializable {
		NONE("none", null, null), WATER("water", net.minecraft.fluid.Fluids.WATER, "Water"), LAVA("lava", net.minecraft.fluid.Fluids.LAVA, "Lava");

		private String t;
		private Fluid f;
		private String ff;
		
		Fluids(String t, Fluid f, String ff){
			this.t = t;
			this.f = f;
			this.ff = ff;
		}
		@Override
		public String getName() {
			return t;
		}
		
		public Fluid getAssocFluid() {
			return f;
		}
		
		public String getFriendlyName() {
			return ff;
		}
		
		public static Fluids getAssocFluids(Fluid f) {
			if(f == WATER.getAssocFluid()) {
				return WATER;
			}else if(f == LAVA.getAssocFluid()) {
				return LAVA;
			}else {
				return NONE;
			}
			
		}
	}
}
