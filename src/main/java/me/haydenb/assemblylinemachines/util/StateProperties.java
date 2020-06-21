package me.haydenb.assemblylinemachines.util;

import java.util.function.Supplier;

import org.apache.commons.lang3.text.WordUtils;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.registry.Registry;
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
	
	public static final EnumProperty<BathCraftingFluids> FLUID = EnumProperty.create("fluid", BathCraftingFluids.class);
	
	public static enum BathCraftingFluids implements IStringSerializable {
		NONE(), WATER(Fluids.WATER, false, new Pair<>(176, 0), new Pair<>(176, 52)), LAVA(Fluids.LAVA, false, new Pair<>(200, 0), new Pair<>(176, 68)), OIL(() -> Registry.getFluid("oil"), true, null, new Pair<>(176, 153)), 
		NAPHTHA(() -> Registry.getFluid("naphtha"), true, null, new Pair<>(176, 137)), CONDENSED_VOID(() -> Registry.getFluid("condensed_void"), true, null, new Pair<>(176, 121));

		private Supplier<Fluid> f;
		private Fluid fx;
		private boolean electricMixerOnly;
		private Pair<Integer, Integer> simpleBlitPiece;
		private Pair<Integer, Integer> electricBlitPiece;
		
		BathCraftingFluids(Supplier<Fluid> f, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece){
			this.f = f;
			this.fx = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
		}
		
		BathCraftingFluids(Fluid fx, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece){
			this.fx = fx;
			this.f = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
		}
		
		BathCraftingFluids(){
			this.fx = null;
			this.f = null;
			this.electricMixerOnly = false;
			this.simpleBlitPiece = null;
			this.electricBlitPiece = null;
		}
		
		@Override
		public String getName() {
			return toString().toLowerCase();
		}
		
		public Fluid getAssocFluid() {
			if(f == null && fx == null) {
				return null;
			}
			
			Fluid fi;
			if(fx != null) {
				fi = fx;
			}else {
				fi = f.get();
			}
			
			if(fi != null && fx == null) {
				fx = fi;
			}
			
			return fi;
			
			
		}
		
		
		public String getFriendlyName() {
			return WordUtils.capitalizeFully(toString().toLowerCase().replace("_", " "));
		}
		
		public static BathCraftingFluids getAssocFluids(Fluid f) {
			
			for(BathCraftingFluids ff : values()) {
				if(ff.getAssocFluid() != null) {
					if(ff.getAssocFluid().equals(f)){
						return ff;
					}
					
				}
			}
			return NONE;
			
		}
		public boolean isElectricMixerOnly() {
			return electricMixerOnly;
		}

		public Pair<Integer, Integer> getSimpleBlitPiece() {
			return simpleBlitPiece;
		}

		public Pair<Integer, Integer> getElectricBlitPiece() {
			return electricBlitPiece;
		}
	}
}
