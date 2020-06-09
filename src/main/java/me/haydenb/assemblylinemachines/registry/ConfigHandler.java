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
		
		public final ConfigValue<Integer> crankSnapChance;
		
		public final ConfigValue<Integer> burnTimeEmpoweredCoal;
		
		static ModConfig clientConfig;
		
		public Config(final ForgeConfigSpec.Builder builder) {
			builder.push("Basic Machine Options");
			
			crankSnapChance = builder.comment("If using the Crank without meaning, what chould be the chance it snaps? Higher number means lower chance. Set to -1 to disable snapping completely.").define("crankSnapChance", 40);
			
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
			if(crankSnapChance.get() < 1 && crankSnapChance.get() != -1) {
				crankSnapChance.set(40);
			}
			
			if(burnTimeEmpoweredCoal.get() < 100) {
				burnTimeEmpoweredCoal.set(3200);
			}
		}
	}
}
