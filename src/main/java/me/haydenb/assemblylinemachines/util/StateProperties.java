package me.haydenb.assemblylinemachines.util;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.IStringSerializable;

@SuppressWarnings("deprecation")
public class StateProperties {

	public static final BooleanProperty MACHINE_ACTIVE = BooleanProperty.create("active");
	public static final IntegerProperty BATTERY_PERCENT_STATE = IntegerProperty.create("fullness", 0, 4);
	
	public static final EnumProperty<DisplayFluids> FLUID = EnumProperty.create("fluid", DisplayFluids.class);
	public static enum DisplayFluids implements IStringSerializable {
		NONE(null), WATER(Fluids.WATER), LAVA(Fluids.LAVA);

		private Fluid f;
		
		DisplayFluids(Fluid f){
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
		
		public static DisplayFluids getAssocFluids(Fluid f) {
			
			for(DisplayFluids ff : values()) {
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
