package me.haydenb.assemblylinemachines.registry.utils;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class StateProperties {

	public static final BooleanProperty MACHINE_ACTIVE = BooleanProperty.create("active");
	public static final IntegerProperty BATTERY_PERCENT_STATE = IntegerProperty.create("fullness", 0, 4);

	/*
	public static final EnumProperty<BathCraftingFluids> FLUID = EnumProperty.create("fluid", BathCraftingFluids.class);

	
	public static enum BathCraftingFluids implements StringRepresentable {
		NONE(), WATER(Fluids.WATER, false, new Pair<>(176, 0), new Pair<>(176, 52), Pair.of(190, 89), new Pair<>(0, 238)), LAVA(Fluids.LAVA, false, new Pair<>(200, 0), new Pair<>(176, 68), Pair.of(190, 104), new Pair<>(30, 238)), OIL(() -> Registry.getFluid("oil"), true, null, new Pair<>(176, 153), Pair.of(190, 149), new Pair<>(75, 238)),
		NAPHTHA(() -> Registry.getFluid("naphtha"), true, null, new Pair<>(176, 137), Pair.of(190, 134), new Pair<>(45, 238)), CONDENSED_VOID(() -> Registry.getFluid("condensed_void"), true, null, new Pair<>(176, 121), Pair.of(190, 119), new Pair<>(15, 238)),
		DARK_ENERGY(() -> Registry.getFluid("dark_energy"), true, null, new Pair<>(176, 169), Pair.of(190, 164), new Pair<>(60, 238));

		private final Supplier<Fluid> f;
		private Fluid fx;
		private final boolean electricMixerOnly;
		private final Pair<Integer, Integer> simpleBlitPiece;
		private final Pair<Integer, Integer> electricBlitPiece;
		private final Pair<Integer, Integer> mkIIBlitPiece;
		private final Pair<Integer, Integer> jeiBlitPiece;
		
		
		BathCraftingFluids(Supplier<Fluid> f, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece, Pair<Integer, Integer> mkIIBlitPiece, Pair<Integer, Integer> jeiBlitPiece){
			this.f = f;
			this.fx = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
			this.mkIIBlitPiece = mkIIBlitPiece;
			this.jeiBlitPiece = jeiBlitPiece;
		}

		BathCraftingFluids(Fluid fx, boolean electricMixerOnly, Pair<Integer, Integer> simpleBlitPiece, Pair<Integer, Integer> electricBlitPiece, Pair<Integer, Integer> mkIIBlitPiece, Pair<Integer, Integer> jeiBlitPiece){
			this.fx = fx;
			this.f = null;
			this.electricMixerOnly = electricMixerOnly;
			this.simpleBlitPiece = simpleBlitPiece;
			this.electricBlitPiece = electricBlitPiece;
			this.mkIIBlitPiece = mkIIBlitPiece;
			this.jeiBlitPiece = jeiBlitPiece;
		}

		BathCraftingFluids(){
			this.fx = null;
			this.f = null;
			this.electricMixerOnly = false;
			this.simpleBlitPiece = null;
			this.electricBlitPiece = null;
			this.jeiBlitPiece = null;
			this.mkIIBlitPiece = null;
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

		public Pair<Integer, Integer> getMKIIBlitPiece(){
			return mkIIBlitPiece;
		}
	}

	*/
	
	public static final BooleanProperty PURIFIER_STATES = BooleanProperty.create("enhanced");
}
