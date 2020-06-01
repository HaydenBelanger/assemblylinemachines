package me.haydenb.assemblylinemachines.registry;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigHandler {

	public static class ConfigHolder{
		public static final ForgeConfigSpec COMMON_SPEC;
		public static final Config COMMON;
		
		static {
			{
				final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
				COMMON = specPair.getLeft();
				COMMON_SPEC = specPair.getRight();
			}
		}
	}
	
	public static class Config{
		
		public final BooleanValue coolDudeMode;
		public final BooleanValue jeiSupport;
		
		public final ConfigValue<Integer> simpleFluidMixerStirs;
		public final ConfigValue<Integer> simpleFluidMixerCranks;
		public final ConfigValue<Integer> simpleGrinderGrinds;
		public final ConfigValue<Integer> simpleGrinderCranks;
		public final ConfigValue<Integer> crankSnapChance;
		public final ConfigValue<Integer> ticksPerOperationSimple;
		
		public final ConfigValue<Integer> burnTimeEmpoweredCoal;
		
		static ModConfig clientConfig;
		
		public Config(final ForgeConfigSpec.Builder builder) {
			builder.push("Basic Machine Options");
			
			
			simpleFluidMixerStirs = builder.comment("What should the stir multiplier on the Simple Fluid Mixer be?").define("simpleFluidMixerStirs", 2);
			simpleFluidMixerCranks = builder.comment("What should the minimum cranks per Simple Fluid Mixer operation be?").define("simpleFluidMixerCranks", 3);
			simpleGrinderGrinds = builder.comment("What should the grind multiplier on the Simple Grinder be?").define("simpleGrinderGrinds", 2);
			simpleGrinderCranks = builder.comment("What should the minimum cranks per Simple Grinder operation be?").define("simpleGrinderCranks", 3);
			crankSnapChance = builder.comment("If using the Crank without meaning, what chould be the chance it snaps? Higher number means lower chance. Set to -1 to disable snapping completely.").define("crankSnapChance", 40);
			ticksPerOperationSimple = builder.comment("How many ticks/operation should the Simple Machines run at? Lower will mean more responsive machines, however may introduce world lag between server and client.").define("ticksPerOperationSimple", 20);
			
			builder.pop();
			builder.push("Gearbox Configuration");
			burnTimeEmpoweredCoal = builder.comment("What should the base burn time on Empowered Coal be? A vanilla Coal is 1600 ticks.").define("burnTimeEmpoweredCoal", 3200);
			builder.pop();
			builder.push("Miscellaneous");
			coolDudeMode = builder.comment("Are you a cool dude?").define("coolDudeMode", false);
			jeiSupport = builder.comment("If JEI is installed, should support be enabled?").define("jeiSupport", true);
			builder.pop();
			
			
			
		}
		
		public void validateConfig() {
			if(simpleFluidMixerStirs.get() < 1) {
				simpleFluidMixerStirs.set(2);
			}
			if(simpleFluidMixerCranks.get() < 1) {
				simpleFluidMixerCranks.set(3);
			}
			if(simpleGrinderGrinds.get() < 1) {
				simpleGrinderGrinds.set(2);
			}
			if(simpleGrinderCranks.get() < 1) {
				simpleGrinderCranks.set(3);
			}
			if(crankSnapChance.get() < 1 && crankSnapChance.get() != -1) {
				crankSnapChance.set(40);
			}
			
			if(ticksPerOperationSimple.get() < 5) {
				ticksPerOperationSimple.set(20);
			}
			
			if(burnTimeEmpoweredCoal.get() < 100) {
				burnTimeEmpoweredCoal.set(3200);
			}
		}
	}
}
