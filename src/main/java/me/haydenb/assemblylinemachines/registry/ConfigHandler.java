package me.haydenb.assemblylinemachines.registry;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigHandler {

	public static class ConfigHolder{
		public static final ForgeConfigSpec COMMON_SPEC;
		public static final ASMConfig COMMON;
		
		static {
			{
				final Pair<ASMConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ASMConfig::new);
				COMMON = specPair.getLeft();
				COMMON_SPEC = specPair.getRight();
			}
		}
	}
	
	public static class ASMConfig{
		
		public final BooleanValue coolDudeMode;
		public final BooleanValue jeiSupport;
		public final BooleanValue interactorInteractMode;
		public final EnumValue<DebugOptions> interactorInteractDebug;
		
		public final ConfigValue<Integer> crankSnapChance;
		
		private final ConfigValue<List<? extends Config>> geothermalFluidsRaw;
		private final ConfigValue<List<? extends Config>> combustionFluidsRaw;
		private final ConfigValue<List<? extends Config>> coolantFluidsRaw;
		
		public final ArrayList<Pair<Fluid, Integer>> geothermalFluids = new ArrayList<>();
		public final ArrayList<Pair<Fluid, Integer>> combustionFluids = new ArrayList<>();
		public final ArrayList<Pair<Fluid, Integer>> coolantFluids = new ArrayList<>();
		
		static ModConfig clientConfig;
		
		public ASMConfig(final ForgeConfigSpec.Builder builder) {
			
			builder.push("Machine Options");
			crankSnapChance = builder.comment("If using the Crank without meaning, what chould be the chance it snaps? Higher number means lower chance. Set to -1 to disable snapping completely.").define("crankSnapChance", 40);
			interactorInteractMode = builder.comment("Interact Mode (in the Interactor block) can cause issues with intercompatability with some mods. Do you want this mode enabled?").define("interactorInteractMode", true);
			interactorInteractDebug = builder.comment("Should Interact Mode (in the Interactor block) fail with an exception, what type of logging should be performed?").defineEnum("interactorInteractDebug", DebugOptions.BASIC);
			
			Predicate<Object> fluidTestPredicate = (object) -> true;
			geothermalFluidsRaw = builder.comment("What fluids should be valid for use in the Geothermal Generator?").defineList("geothermalFluids", getFluidDefaultConfig(Pair.of("minecraft:lava", 115000), Pair.of("assemblylinemachines:naphtha", 350000)), fluidTestPredicate);
			combustionFluidsRaw = builder.comment("What fluids should be valid for use in the Combustion Generator?").defineList("combustionFluids", getFluidDefaultConfig(Pair.of("assemblylinemachines:gasoline", 600000), Pair.of("assemblylinemachines:diesel", 1050000)), fluidTestPredicate);
			coolantFluidsRaw = builder.comment("What fluids should be valid for use as coolant in various Generators? Value is multiplier on burn time.").defineList("coolantFluids", getFluidDefaultConfig(Pair.of("minecraft:water", 2), Pair.of("assemblylinemachines:condensed_void", 4)), fluidTestPredicate);
			
			builder.pop();
			builder.push("Miscellaneous");
			coolDudeMode = builder.comment("Are you cool?").define("coolDudeMode", false);
			jeiSupport = builder.comment("If JEI is installed, should support be enabled?").define("jeiSupport", true);
			builder.pop();
			
			
			
		}
		
		public void validateConfig() {
			if(crankSnapChance.get() < 1 && crankSnapChance.get() != -1) {
				crankSnapChance.set(40);
			}
			
			checkFluidLists(Pair.of(geothermalFluidsRaw.get().iterator(), geothermalFluids), Pair.of(combustionFluidsRaw.get().iterator(), combustionFluids), Pair.of(coolantFluidsRaw.get().iterator(), coolantFluids));
			
		}
	}
	
	public static enum DebugOptions{
		NONE, BASIC, COMPLETE;
	}
	
	private static Supplier<Map<String, Object>> getFluidSupplier(String fluid, int burnTime){
		return new Supplier<Map<String,Object>>() {
			
			@Override
			public Map<String, Object> get() {
				
				HashMap<String, Object> nH = new HashMap<>();
				
				nH.put("fluid", fluid);
				nH.put("value", burnTime);
				
				return nH;
			}
		};
	}
	
	@SafeVarargs
	private static ArrayList<Config> getFluidDefaultConfig(Pair<String, Integer>... sets) {
		ArrayList<Config> cf = new ArrayList<Config>();
		for(Pair<String, Integer> pairs : sets) {
			cf.add(Config.of(getFluidSupplier(pairs.getLeft(), pairs.getRight()), InMemoryFormat.withUniversalSupport()));
		}
		return cf;
	}
	
	@SafeVarargs
	private static void checkFluidLists(Pair<Iterator<? extends Config>, ArrayList<Pair<Fluid, Integer>>>... iterators) {
		for(Pair<Iterator<? extends Config>, ArrayList<Pair<Fluid, Integer>>> itp : iterators) {
			Iterator<? extends Config> it = itp.getLeft();
			ArrayList<Pair<Fluid, Integer>> set = itp.getRight();
			while(it.hasNext()) {
				Config c = it.next();
				
				if(c.contains("value") && c.contains("fluid")) {
					Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(c.get("fluid")));
					if(fluid != Fluids.EMPTY) {
						
						set.add(Pair.of(fluid, c.getInt("value")));
					}else {
						it.remove();
					}
				}else {
					it.remove();
				}
			}
		}
	}
}
