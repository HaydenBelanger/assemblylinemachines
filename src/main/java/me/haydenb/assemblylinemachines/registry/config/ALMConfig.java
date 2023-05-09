package me.haydenb.assemblylinemachines.registry.config;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ALMConfig {

	private static final Pair<Client, ForgeConfigSpec> CLIENT = new ForgeConfigSpec.Builder().configure(Client::new);
	private static final Pair<Common, ForgeConfigSpec> COMMON = new ForgeConfigSpec.Builder().configure(Common::new);
	private static final Pair<Server, ForgeConfigSpec> SERVER = new ForgeConfigSpec.Builder().configure(Server::new);

	public static Client getClientConfig() {
		return CLIENT.getKey();
	}

	public static Common getCommonConfig() {
		return COMMON.getKey();
	}

	public static Server getServerConfig() {
		return SERVER.getKey();
	}

	public static void registerSpecs(ModLoadingContext mlc) {
		mlc.registerConfig(Type.CLIENT, CLIENT.getValue());
		mlc.registerConfig(Type.COMMON, COMMON.getValue());
		mlc.registerConfig(Type.SERVER, SERVER.getValue());
	}

	/**Config for anything accessed only on client-side during startup post-registry.*/
	public static final record Client(BooleanValue receiveGuideBook, BooleanValue receiveUpdateMessages) {

		public Client(Builder builder) {
			this(
				builder.comment("Do you want to receive a copy of Assembly Lines & You when you first connect to a server and you have Patchouli installed?", "This may be overridden by the server.").define("receiveGuideBook", true),
				builder.comment("Do you want to receive mod update messages when the mod is out of date and you connect to a server?").define("receiveUpdateMessages", true)
			);
		}
	}

	/**Config for standard things, loaded on both sides during startup post-registry.*/
	public static final record Common(BooleanValue lateGamePlatesRequireCompressor, BooleanValue lateGameGearsRequireCompressor, BooleanValue grinderIMC, BooleanValue alloyIMC, BooleanValue compressorStorageBlockIMC,
			BooleanValue compressorNuggetIMC, BooleanValue compressorPlateIMC, BooleanValue compressorGearIMC, BooleanValue compressorRodIMC, BooleanValue titaniumOre, BooleanValue blackGranite, BooleanValue blackGraniteNaturalTag,
			BooleanValue chromiumOre, BooleanValue chromiumOreOnDragonIsland, BooleanValue corruptOres, BooleanValue fleroviumOre, BooleanValue empoweredCoalOre, ConfigValue<String> preferredModid) {

		public Common(Builder builder) {
			this(
				builder.push("Progression Crafting").comment("Should late-game (Mystium and beyond) Plates & Sheets require the Pneumatic Compressor to create?").define("lateGamePlatesRequireCompressor", true),
				builder.comment("Should late game (Mystium and beyond) Gears require the Pneumatic Compressor to create?").define("lateGameGearsRequireCompressor", true),
				builder.pop().push("IMC Crafting").comment("Should the Grinder support creation of other-mod Ground Ores with Ingots, Ore Blocks, and Raw Ores?").define("grinderIMC", true),
				builder.comment("Should the Alloy Smelter support creation of other-mod Alloys with various combinations of Ingots?").define("alloyIMC", true),
				builder.comment("Should the Pneumatic Compressor support creation of other-mod Metal Blocks with Ingots?").define("compressorStorageBlockIMC", true),
				builder.comment("Should the Pneumatic Compressor support creation of other-mod Ingots with Nuggets?").define("compressorNuggetIMC", true),
				builder.comment("Should the Pneumatic Compressor support creation of other-mod Plates with Ingots?").define("compressorPlateIMC", true),
				builder.comment("Should the Pneumatic Compressor support creation of other-mod Gears with Plates?").define("compressorGearIMC", true),
				builder.comment("Should the Pneumatic Compressor support creation of other-mod Rods with Ingots?").define("compressorRodIMC", true),
				builder.pop().push("Ores").comment("Should Titanium and Deepslate Titanium Ore generate in the overworld?").define("titaniumOre", true),
				builder.comment("Should Black Granite generate in the Nether?").define("blackGranite", true),
				builder.comment("Should Black Granite spawn with its natural property, preventing it from being collected with non-electric Pickaxes?").define("blackGraniteNaturalTag", true),
				builder.comment("Should Chromium Ore generate in The End?").define("chromiumOre", true),
				builder.comment("Should Chromium Ore generate on the Dragon Island?").define("chromiumOreOnDragonIsland", false),
				builder.comment("Should all vanilla-counterpart Corrupt Ores generate in the Chaos Plane?").define("corruptOres", true),
				builder.comment("Should Flerovium Ore spawn in the Chaos Plane?").define("fleroviumOre", true),
				builder.comment("Should 20% of Corrupt Basalt Coal Ore be replaced with Corrupt Basalt Empowered Coal Ore?").define("empoweredCoalOre", true),
				builder.pop().push("Miscellaneous").comment("Which modid should be preferred for IMC recipes?", "The first modid alphabetically is a fallback if this modid does not have an item in the requested tag.").define("preferredModid", AssemblyLineMachines.MODID)
			);
		}
	}

	/**Config for world-specific things, loaded during server/world startup and synced to client.*/
	public static final record Server(BooleanValue reactorExplosions, ConfigValue<Double> crankSnapChance, BooleanValue invalidBathReturnsSludge,
			ConfigValue<Double> kineticMachineCycleModifier, BooleanValue interactMode, EnumValue<DebugOptions> interactExceptionReporting, BooleanValue distributeGuideBook, BooleanValue gasolineExplosions,
			ConfigValue<Double> overclockMultiplier, ConfigValue<Double> engineersFuryMultiplier) {
		public Server(Builder builder) {
			this(
				builder.push("Machines").comment("Should the Entropy Reactor explode at 98% Entropy?").define("reactorExplosions", true),
				builder.comment("What is the chance that the Crank will snap when used without a valid recipe?").define("crankSnapChance", 0.01, (o) -> o instanceof Double d && (d >= 0 || d <= 1)),
				builder.comment("Should the fluid bath return Sludge when an invalid recipe is made?", "If false, both of the component items will instead be returned.").define("invalidBathReturnsSludge", true),
				builder.comment("What should the multiplier from stirs or grinds to seconds in the Kinetic Fluid Mixer or Kinetic Grinder be?", "For example, a recipe taking 10 grinds or stirs would take 15 seconds to process with a multiplier of 1.5.").defineInRange("kineticMachineCycleModifier", 1d, Double.MIN_VALUE, Double.MAX_VALUE),
				builder.comment("Should Interact Mode in the Interactor be enabled? Sometimes, this can cause exceptions and other bugs, like interacting with a block with a GUI.").define("interactMode", true),
				builder.comment("If interact mode fails with an exception, what level of logging should be performed?").defineEnum("interactExceptionReporting", DebugOptions.MESSAGE),
				builder.pop().push("World").comment("Should a copy of Assembly Lines & You be distributed to new players?", "This may be overridden by the client.").define("distributeGuideBook", true),
				builder.comment("Should Gasoline and Diesel cause explosions when placed next to an ignition source?").define("gasolineExplosions", true),
				builder.pop().push("Tools").comment("What should every level of the Overclock enchantment multiply the battery capacity of a tool by?", "For example, 0.2 would be a 20% increase per enchantment level.").defineInRange("overclockMultiplier", 0.2d, Double.MIN_VALUE, Double.MAX_VALUE),
				builder.comment("How much knockback should each level of the Engineer's Fury enchantment add?", "For example, 0.1 at Engineer's Fury X would add +1 total knockback.").defineInRange("engineersFuryMultiplier", 0.1d, Double.MIN_VALUE, Double.MAX_VALUE)
			);
		}
	}

	public static enum DebugOptions{
		NONE(null), MESSAGE(Level.WARN), STACK_TRACE(Level.DEBUG);

		public final Level level;

		DebugOptions(Level level){
			this.level = level;
		}
	}
}
