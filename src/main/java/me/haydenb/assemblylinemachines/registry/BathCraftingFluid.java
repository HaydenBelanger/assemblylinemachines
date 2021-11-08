package me.haydenb.assemblylinemachines.registry;

import java.util.function.Supplier;

import org.apache.commons.lang3.text.WordUtils;

import com.mojang.datafixers.util.Pair;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("deprecation")
public class BathCraftingFluid {

	public static final BooleanProperty MACHINE_ACTIVE = BooleanProperty.create("active");
	public static final IntegerProperty BATTERY_PERCENT_STATE = IntegerProperty.create("fullness", 0, 4);
	
	public static final EnumProperty<BathCraftingFluids> FLUID = EnumProperty.create("fluid", BathCraftingFluids.class);
	
	public static enum BathCraftingFluids implements StringRepresentable {
		NONE(), WATER(Fluids.WATER, false, new Pair<>(176, 0), new Pair<>(176, 52), new Pair<>(87, 197)), LAVA(Fluids.LAVA, false, new Pair<>(200, 0), new Pair<>(176, 68), new Pair<>(87, 213)), OIL(() -> Registry.getFluid("oil"), true, null, new Pair<>(176, 153), new Pair<>(102, 229)), 
		NAPHTHA(() -> Registry.getFluid("naphtha"), true, null, new Pair<>(176, 137), new Pair<>(102, 213)), CONDENSED_VOID(() -> Registry.getFluid("condensed_void"), true, null, new Pair<>(176, 121), new Pair<>(102, 197));

		private Supplier<Fluid> f;
		private Fluid fx;
		private boolean electricMixerOnly;
		private Pair<Integer, Integer> simpleBlitPiece;
		private Pair<Integer, Integer> electricBlitPiece;
		private Pair<Integer, Integer> jeiBlitPiece;
		
		BathCraftingFluids(Supplier<Fluid> f, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece, Pair<Integer, Integer> jeiBlitPiece){
			this.f = f;
			this.fx = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
			this.jeiBlitPiece = jeiBlitPiece;
		}
		
		BathCraftingFluids(Fluid fx, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece, Pair<Integer, Integer> jeiBlitPiece){
			this.fx = fx;
			this.f = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
			this.jeiBlitPiece = jeiBlitPiece;
		}
		
		BathCraftingFluids(){
			this.fx = null;
			this.f = null;
			this.electricMixerOnly = false;
			this.simpleBlitPiece = null;
			this.electricBlitPiece = null;
		}
		
		@Override
		public String getSerializedName() {
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

		public Pair<Integer, Integer> getJeiBlitPiece() {
			return jeiBlitPiece;
		}
	}
}
