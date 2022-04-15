package me.haydenb.assemblylinemachines.registry;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigHandler {

	public static class ConfigHolder{
		
		private static final HashMap<Type, Pair<Object, ForgeConfigSpec>> CONFIGS = new HashMap<>();
		
		static {
			CONFIGS.put(Type.SERVER, new ForgeConfigSpec.Builder().configure(ALMServerConfig::new));
			CONFIGS.put(Type.CLIENT, new ForgeConfigSpec.Builder().configure(ALMClientConfig::new));
			CONFIGS.put(Type.COMMON, new ForgeConfigSpec.Builder().configure(ALMCommonConfig::new));
		}
		
		public static ALMServerConfig getServerConfig() {
			return (ALMServerConfig) CONFIGS.get(Type.SERVER).getKey();
		}
		
		public static ALMCommonConfig getCommonConfig() {
			return (ALMCommonConfig) CONFIGS.get(Type.COMMON).getKey();
		}
		
		public static ALMClientConfig getClientConfig() {
			return (ALMClientConfig) CONFIGS.get(Type.CLIENT).getKey();
		}
		
		public static void registerSpecs(ModLoadingContext mlc) {
			for(Entry<Type, Pair<Object, ForgeConfigSpec>> entry : CONFIGS.entrySet()) mlc.registerConfig(entry.getKey(), entry.getValue().getValue());
		}
	}
	
	public static class ALMClientConfig{
		
		public final BooleanValue customTooltipColors;
		public final BooleanValue customTooltipFrames;
		public final BooleanValue receiveGuideBook;
		public final BooleanValue receiveUpdateMessages;
		
		public ALMClientConfig(ForgeConfigSpec.Builder builder) {
			
			builder.push("Client");
			customTooltipColors = builder.comment("Do you want to render custom tooltip frame colors for some specific items?", "If false, the tooltip will be standard.").define("customTooltipColors", true);
			customTooltipFrames = builder.comment("Do you want to render custom tooltip frame textures for some specific items?", "If false, the tooltip will be standard. This has no effect if customTooltipColors is false.").define("customTooltipFrames", true);
			receiveGuideBook = builder.comment("Do you want to receive a copy of Assembly Lines & You when you first connect to a server and you have Patchouli installed?",
					"If this is false, this will override the config of the server.", 
					"If the server you connect to is configured as false, you will not receive a copy on that server.").define("receiveGuideBook", true);
			receiveUpdateMessages = builder.comment("Do you want to receive mod update messages when the mod is out of date and you connect to a server?").define("receiveUpdateMessages", true);
			builder.pop();
			
		}
	}
	
	public static class ALMCommonConfig{
		//Progression
		public final BooleanValue lateGamePlatesRequireCompressor;
		public final BooleanValue lateGameGearsRequireCompressor;
		//IMC
		public final BooleanValue grinderIMC;
		public final BooleanValue alloyIMC;
		public final BooleanValue compressorStorageBlockIMC;
		public final BooleanValue compressorNuggetIMC;
		public final BooleanValue compressorPlateIMC;
		public final BooleanValue compressorGearIMC;
		public final BooleanValue compressorRodIMC;
		
		public ALMCommonConfig(ForgeConfigSpec.Builder builder) {
			builder.push("Progression Crafting");
			lateGamePlatesRequireCompressor = builder.comment("Should late-game (Mystium and beyond) Plates & Sheets require the Pneumatic Compressor to create?").define("lateGamePlatesRequireCompressor", true);
			lateGameGearsRequireCompressor = builder.comment("Should late game (Mystium and beyond) Gears require the Pneumatic Compressor to create?").define("lateGameGearsRequireCompressor", true);
			builder.pop();
			builder.push("Inter-Mod Compat Crafting");
			grinderIMC = builder.comment("Should the Grinder support creation of other-mod Ground Ores with Ingots, Ore Blocks, and Raw Ores?").define("grinderIMC", true);
			alloyIMC = builder.comment("Should the Alloy Smelter support creation of other-mod Alloys with various combinations of Ingots?").define("alloyIMC", true);
			compressorStorageBlockIMC = builder.comment("Should the Pneumatic Compressor support creation of other-mod Metal Blocks with Ingots?").define("compressorStorageBlockIMC", true);
			compressorNuggetIMC = builder.comment("Should the Pneumatic Compressor support creation of other-mod Ingots with Nuggets?").define("compressorNuggetIMC", true);
			compressorPlateIMC = builder.comment("Should the Pneumatic Compressor support creation of other-mod Plates with Ingots?").define("compressorPlateIMC", true);
			compressorGearIMC = builder.comment("Should the Pneumatic Compressor support creation of other-mod Gears with Plates?").define("compressorGearIMC", true);
			compressorRodIMC = builder.comment("Should the Pneumatic Compressor support creation of other-mod Rods with Ingots?").define("compressorRodIMC", true);
			builder.pop();
		}
	}
	
	public static class ALMServerConfig{
		
		//MACHINE OPTIONS
		public final BooleanValue reactorExplosions;
		public final ConfigValue<Integer> crankSnapChance;
		public final ConfigValue<Integer> toolChargerChargeRate;
		public final ConfigValue<Integer> toolChargerMaxEnergyStorage;
		public final ConfigValue<Integer> experienceSiphonMaxDrain;
		public final ConfigValue<Integer> coalGeneratorMultiplier;
		public final ConfigValue<Integer> naphthaTurbineMultiplier;
		public final ConfigValue<List<? extends String>> disallowedFluidBathItems;
		public final BooleanValue invalidBathReturnsSludge;
		public final ConfigValue<Double> kineticGrinderCycleMultiplier;
		public final ConfigValue<Double> kineticFluidMixerCycleMultiplier;
		
		//INTERACTOR
		public final BooleanValue interactMode;
		public final EnumValue<DebugOptions> interactExceptionReporting;
		public final ConfigValue<String> interactExceptionReportLevel;
		
		//MISC/WORLD
		public final BooleanValue distributeGuideBook;
		public final BooleanValue mystiumFarmlandDeath;
		public final BooleanValue gasolineExplosions;
		
		public final ConfigValue<String> preferredModid;
		
		//GENERAL TOOLS
		public final ConfigValue<Double> overclockEnchantmentMultiplier;
		public final ConfigValue<Double> engineersFuryKnockbackMultiplier;
		
		//TITANIUM TOOLS
		public final ConfigValue<Double> titaniumToolAttack;
		public final ConfigValue<Double> titaniumToolHarvestSpeed;
		public final ConfigValue<Integer> titaniumEnchantability;
		public final ConfigValue<Integer> titaniumDurability;
		public final ConfigValue<Double> titaniumArmorKnockbackResistance;
		public final ConfigValue<Integer> titaniumArmorDamageReduction;
		public final ConfigValue<Double> titaniumArmorToughness;
		
		//STEEL TOOLS
		public final ConfigValue<Double> steelToolAttack;
		public final ConfigValue<Double> steelToolHarvestSpeed;
		public final ConfigValue<Integer> steelEnchantability;
		public final ConfigValue<Integer> steelDurability;
		public final ConfigValue<Double> steelArmorKnockbackResistance;
		public final ConfigValue<Integer> steelArmorDamageReduction;
		public final ConfigValue<Double> steelArmorToughness;
		
		//CRANK TOOLS
		public final ConfigValue<Double> crankToolAttack;
		public final ConfigValue<Double> crankToolHarvestSpeed;
		public final ConfigValue<Integer> crankToolEnchantability;
		public final ConfigValue<Integer> crankToolDurability;
		public final ConfigValue<Integer> crankToolMaxCranks;
		
		//MYSTIUM TOOLS
		public final ConfigValue<Double> mystiumToolAttack;
		public final ConfigValue<Double> mystiumToolHarvestSpeed;
		public final ConfigValue<Integer> mystiumEnchantability;
		public final ConfigValue<Integer> mystiumDurability;
		public final ConfigValue<Double> mystiumArmorKnockbackResistance;
		public final ConfigValue<Integer> mystiumArmorDamageReduction;
		public final ConfigValue<Double> mystiumArmorToughness;
		public final ConfigValue<Integer> mystiumMaxFE;
		public final ConfigValue<Integer> enhancedMystiumChestplateMaxFE;
		
		//NOVASTEEL TOOLS
		public final ConfigValue<Double> novasteelToolAttack;
		public final ConfigValue<Double> novasteelToolHarvestSpeed;
		public final ConfigValue<Integer> novasteelToolEnchantability;
		public final ConfigValue<Integer> novasteelToolDurability;
		public final ConfigValue<Integer> novasteelToolMaxFE;
		
		//WRENCH-O-MATIC
		public final ConfigValue<Double> wrenchAttack;
		public final ConfigValue<Double> wrenchKnockback;
		
		//TITANIUM
		public final ConfigValue<Integer> titaniumVeinSize;
		public final ConfigValue<Integer> titaniumFrequency;
		public final ConfigValue<Integer> titaniumMinHeight;
		public final ConfigValue<Integer> titaniumMaxHeight;
		public final EnumValue<OreGenOptions> titaniumOreGenStyle;
		
		//CHROMIUM
		public final ConfigValue<Integer> chromiumVeinSize;
		public final ConfigValue<Integer> chromiumFrequency;
		public final BooleanValue chromiumOnDragonIsland;
		
		//BLACK GRANITE
		public final BooleanValue blackGraniteSpawnsWithNaturalTag;
		public final ConfigValue<Integer> blackGraniteVeinSize;
		public final ConfigValue<Integer> blackGraniteFrequency;
		
		public ALMServerConfig(ForgeConfigSpec.Builder builder) {
			builder.push("Machines");
			crankSnapChance = builder.comment("If using the Crank without meaning, what chould be the chance it snaps?", "Value is 1 in X chance to snap, where X is the value in the config.", "Set to -1 to disable snapping completely.").defineInRange("crankSnapChance", 100, -1, 1000);
			reactorExplosions = builder.comment("If the Entropy Reactor reaches higher than 98% Entropy, should it explode?").define("reactorExplosions", true);
			toolChargerChargeRate = builder.comment("What is the maximum amount of FE the Tool Charger can place into an item per cycle?", "The Tool Charger runs one cycle every 2.5 seconds.").defineInRange("toolChargerChargeRate", 10000, 1, Integer.MAX_VALUE);
			toolChargerMaxEnergyStorage = builder.comment("What should the FE capacity of the Tool Charger be?").defineInRange("toolChargerMaxEnergyStorage", 100000, 1, Integer.MAX_VALUE);
			experienceSiphonMaxDrain = builder.comment("How many XP Points per half-second should the Experience Siphon drain?").defineInRange("experienceSiphonMaxDrain", 25, 1, 500);
			coalGeneratorMultiplier = builder.comment("What should the base FE multiplier for burn-time-to-FE be for fuel in the Coal Generator?", "Every 1 is equal to 4 FE more per burn tick over the 60 seconds of operation.",
					"For example, every 1 would increase the FE output of Charcoal by 3.2KFE.").defineInRange("coalGeneratorMultiplier", 2, 1, 100);
			naphthaTurbineMultiplier = builder.comment("What should the base operating time multiplier be for when the Naphtha Turbine is placed on the Coal Generator?", "Every 1 is equal to 60 seconds.").defineInRange("naphthaTurbineMultiplier", 4, 1, 100);
			disallowedFluidBathItems = builder.comment("What items should be blacklisted from being accepted into the Fluid Bath?", "A handful of items cannot be accepted regardless of list content.").defineListAllowEmpty(List.of("disallowedFluidBathItems"), () -> List.of(), 
					(s) -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(s.toString())));
			invalidBathReturnsSludge = builder.comment("On a failed recipe, should the Fluid Bath return Sludge when emptied?", "If false, it will instead return both of the input items.").define("invalidBathReturnsSludge", true);
			kineticGrinderCycleMultiplier = builder.comment("What should the multiplier for the amount of grinds (uses of the Manual Grinder) to convert to minimum seconds in the Kinetic Grinder be?", "For example, a recipe with 10 grinds with a multiplier of 2 will take a minimum of 20 seconds to craft.").defineInRange("simpleGrinderCycleModifier", 2d, 0.01d, 100d);
			kineticFluidMixerCycleMultiplier = builder.comment("What should the multiplier for the amount of stirs (uses of a Stirring Stick on Fluid Bath) to convert to minimum seconds in the Kinetic Fluid Mixer be?", "For example, a recipe with 10 stirs with a multiplier of 0.7 will take a minimum of 7 seconds to craft.").defineInRange("simpleFluidMixerCycleModifier", 1d, 0.01d, 100d);
			builder.pop();
			
			builder.push("Interactor Settings");
			interactMode = builder.comment("Interact Mode can cause issues with intercompatability with some mods. Do you want this mode enabled?", "For example, attempting to \"interact with\" a block with a GUI will cause an exception or other fatal issues.").define("interactMode", true);
			interactExceptionReporting = builder.comment("Should Interact Mode fail with an exception, what type of logging should be performed?",
					"NONE - Interact Mode exceptions are silenced.", "MESSAGE - A basic informative message to identify the error.",
					"STACK_TRACE - A full stacktrace, for developers to identify source of error.").defineEnum("interactExceptionReporting", DebugOptions.MESSAGE);
			interactExceptionReportLevel = builder.comment("If logging occurs based on the option selected in interactExceptionReporting, what Log4J level should the log be placed at?").define("interactExceptionReportLevel", "DEBUG", (s) -> Level.getLevel(Optional.ofNullable(s).orElse("").toString()) != null);
			
			builder.pop();
			builder.push("Tools");
			overclockEnchantmentMultiplier = builder.comment("What multiplier should each level of the Overclock enchantment give?", "For example, the default of \"0.2\" is a 20% increase per enchantment level.").defineInRange("overclockEnchantmentMultiplier", 0.2d, 0.001d, 1d);
			engineersFuryKnockbackMultiplier = builder.comment("What additional multiplier should each level of the Engineer's Fury enchantment give to knockback?", "For example, \"0.1\" at Level X would be +1 knockback.").defineInRange("engineersFuryKnockbackMultiplier", 0.1d, 0d, 1d);
			
			builder.push("Titanium");
			titaniumToolAttack = builder.comment("What is the base damage Titanium Tools should do?").defineInRange("titaniumToolAttack", 5d, 0.1d, 1000d);
			titaniumToolHarvestSpeed = builder.comment("What is the base harvest speed Titanium Tools should do?").defineInRange("titaniumToolHarvestSpeed", 7d, 0.1d, 100d);
			titaniumEnchantability = builder.comment("What should the enchantability of Titanium Tools and Armor be?").defineInRange("titaniumEnchantability", 8, 0, 100);
			titaniumDurability = builder.comment("What should the base durability of Titanium Tools and Armor be?").defineInRange("titaniumDurability", 1150, 10, 100000);
			titaniumArmorKnockbackResistance = builder.comment("What should the knockback resistance of Titanium Armor be?").defineInRange("titaniumArmorKnockbackResistance", 0d, 0d, 1d);
			titaniumArmorDamageReduction = builder.comment("What should the base damage reduction of Titanium Armor be?").defineInRange("titaniumArmorDamageReduction", 4, 2, 100);
			titaniumArmorToughness = builder.comment("What should the toughness of Titanium Armor be?").defineInRange("titaniumArmorToughness", 0d, 0d, 100d);
			builder.pop();
			
			builder.push("Steel");
			steelToolAttack = builder.comment("What is the base damage Steel Tools should do?").defineInRange("steelToolAttack", 7d, 0.1d, 1000d);
			steelToolHarvestSpeed = builder.comment("What is the base harvest speed Steel Tools should do?").defineInRange("steelToolHarvestSpeed", 9d, 0.1d, 100d);
			steelEnchantability = builder.comment("What should the enchantability of Steel Tools and Armor be?").defineInRange("steelEnchantability", 6, 0, 100);
			steelDurability = builder.comment("What should the base durability of Steel Tools and Armor be?").defineInRange("steelDurability", 1800, 10, 100000);
			steelArmorKnockbackResistance = builder.comment("What should the knockback resistance of Steel Armor be?").defineInRange("steelArmorKnockbackResistance", 0.10d, 0d, 1d);
			steelArmorDamageReduction = builder.comment("What should the base damage reduction of Steel Armor be?").defineInRange("steelArmorDamageReduction", 4, 2, 100);
			steelArmorToughness = builder.comment("What should the toughness of Steel Armor be?").defineInRange("steelArmorToughness", 2.5d, 0d, 100d);
			builder.pop();
			
			builder.push("Crank");
			crankToolAttack = builder.comment("What is the base damage Crank-Powered Tools should do?").defineInRange("crankToolAttack", 8d, 0.1d, 1000d);
			crankToolHarvestSpeed = builder.comment("What is the base harvest speed Crank-Powered Tools should do?").defineInRange("crankToolHarvestSpeed", 11d, 0.1d, 100d);
			crankToolEnchantability = builder.comment("What should the enchantability of Crank-Powered Tools be?").defineInRange("crankToolEnchantability", 16, 0, 100);
			crankToolDurability = builder.comment("What should the base physical durability of Crank-Powered Tools be?").defineInRange("crankToolDurability", 75, 10, 100000);
			crankToolMaxCranks = builder.comment("What should the base kinetic durability (Cranks) of Crank-Powered Tools be?").defineInRange("crankToolMaxCranks", 750, 1, Integer.MAX_VALUE);
			builder.pop();
			
			builder.push("Mystium");
			mystiumToolAttack = builder.comment("What is the base damage Mystium Tools should do?").defineInRange("mystiumToolAttack", 9d, 0.1d, 1000d);
			mystiumToolHarvestSpeed = builder.comment("What is the base harvest speed Mystium Tools should do?").defineInRange("mystiumToolHarvestSpeed", 19d, 0.1d, 100d);
			mystiumEnchantability = builder.comment("What should the enchantability of Mystium Tools and Armor be?").defineInRange("mystiumToolEnchantability", 28, 0, 100);
			mystiumDurability = builder.comment("What should the base physical durability of Mystium Tools and Armor be?").defineInRange("mystiumToolDurability", 150, 10, 100000);
			mystiumArmorKnockbackResistance = builder.comment("What should the knockback resistance of Mystium Armor be?").defineInRange("mystiumArmorKnockbackResistance", 0.15d, 0d, 1d);
			mystiumArmorDamageReduction = builder.comment("What should the base damage reduction of Mystium Armor be?").defineInRange("mystiumArmorDamageReduction", 7, 2, 100);
			mystiumArmorToughness = builder.comment("What should the toughness of Mystium Armor be?").defineInRange("mystiumArmorToughness", 5d, 0d, 100d);
			mystiumMaxFE = builder.comment("What should the base electrical durability (Forge Energy) of Mystium Tools be?").defineInRange("mystiumToolMaxFE", 1000000, 1, Integer.MAX_VALUE);
			enhancedMystiumChestplateMaxFE = builder.comment("What should the base electrical durability (Forge Energy) of the Enhanced Mystium Chestplate be?").defineInRange("enhancedMystiumChestplateMaxFE", 10000000, 1, Integer.MAX_VALUE);
			builder.pop();
			
			builder.push("Novasteel");
			novasteelToolAttack = builder.comment("What is the base damage Novasteel Tools should do?").defineInRange("novasteelToolAttack", 10.5d, 0.1d, 1000d);
			novasteelToolHarvestSpeed = builder.comment("What is the base harvest speed Novasteel Tools should do?").defineInRange("novasteelToolHarvestSpeed", 23d, 0.1d, 100d);
			novasteelToolEnchantability = builder.comment("What should the enchantability of Novasteel Tools be?").define("novasteelToolEnchantability", 37);
			novasteelToolDurability = builder.comment("What should the base physical durability of Novasteel Tools be?").defineInRange("novasteelToolDurability", 300, 10, 100000);
			novasteelToolMaxFE = builder.comment("What should the base electrical durability (Forge Energy) of Novasteel Tools be?").defineInRange("novasteelToolMaxFE", 20000000, 1, Integer.MAX_VALUE);
			builder.pop();
			
			builder.push("Wrench-O-Matic");
			wrenchAttack = builder.comment("In half-hearts, what is the amount of damage the Wrench-O-Matic in Wrath Mode should do?").defineInRange("wrenchAttack", 6d, 0d, Double.MAX_VALUE);
			wrenchKnockback = builder.comment("How much base knockback should the Wrench-O-Matic do?", "Note that Engineer's Fury adds additional knockback.").defineInRange("wrenchKnockback", 0.5d, 0d, 5d);
			builder.pop();
			builder.pop();
			
			builder.push("World");
			mystiumFarmlandDeath = builder.comment("Should Mystium Farmland get exhausted over time and stop performing grow operations?").define("mystiumFarmlandDeath", true);
			gasolineExplosions = builder.comment("Should Gasoline and Diesel explode when placed next to a flammable block?").define("gasolineExplosions", true);
			distributeGuideBook = builder.comment("Should players receive a copy of Assembly Lines & You when they first connect to this server and Patchouli is installed?",
					"If this is false, this server will override the config of all clients.",
					"If clients are configured to false, that client will not receive a copy regardless of this option.").define("distributeGuideBook", true);
			preferredModid = builder.comment("In recipe types which support inter-mod compatible output, which Mod ID should be preferred?", 
					"If the selected Mod ID is invalid or does not provide an appropriate item for a tag, the first Mod ID alphabetically will be used.").define("preferredModid", "assemblylinemachines");
			builder.push("Titanium");
			titaniumVeinSize = builder.comment("What should the maximum size per vein of Titanium Ore be?", "Set to 0 to disable Titanium Ore generation.").defineInRange("titaniumVeinSize", 5, 0, 1000);
			titaniumFrequency = builder.comment("How many veins of Titanium Ore should generate per chunk?", "Set to 0 to disable Titanium Ore generation.").defineInRange("titaniumFrequency", 7, 0, 1000);
			titaniumMinHeight = builder.comment("What is the minimum Y value Titanium Ore should spawn at in the overworld?", 
					"This can (and by default, does) go below the minimum world limit to change the TRIANGLE-style weighting of generation.").define("titaniumMinHeight", -112);
			titaniumMaxHeight = builder.comment("What is the maximum Y value Titanium Ore should spawn at in the overworld?").define("titaniumMaxHeight", -16);
			titaniumOreGenStyle = builder.comment("What style of ore generation should Titanium Ore use?", "TRIANGLE - Most generation occurs in center of min-max range - Akin to Coal Ore at most altitudes.", 
					"UNIFORM - All generation is equal between min-max range - Akin to Redstone Ore at most altitudes.").defineEnum("titaniumOreGenStyle", OreGenOptions.TRIANGLE);
			builder.pop();
			
			builder.push("Black-Granite");
			blackGraniteSpawnsWithNaturalTag = builder.comment("Should generated Black Granite have the \"natural\" tag?", "This tag, if present, will only allow Black Granite to be dropped if mined with a Crank-Powered Pickaxe.", 
					"This is the intended progression, but can be disabled in order to make all Pickaxes mine it.").define("blackGraniteSpawnsWithNaturalTag", true);
			blackGraniteVeinSize = builder.comment("What should the maximum size per vein of Black Granite be?", "Set to 0 to disable Black Granite generation.").define("blackGraniteVeinSize", 37);
			blackGraniteFrequency = builder.comment("How many veins of Black Granite should generate per chunk?", "Set to 0 to disable Black Granite generation.").define("blackGraniteFrequency", 7);
			builder.pop();
			
			builder.push("Chromium");
			chromiumVeinSize = builder.comment("What should the maximum size per vein of Chromium Ore be?", "Set to 0 to disable Chromium Ore generation.").defineInRange("chromiumVeinSize", 10, 0, 1000);
			chromiumFrequency = builder.comment("How many veins of Chromium Ore should generate per chunk?", "Set to 0 to disable Chromium Ore generation.").defineInRange("chromiumFrequency", 4, 0, 1000);
			chromiumOnDragonIsland = builder.comment("Should Chromium Ore generate on the Dragon Island in The End?", "If false, Chromium Ore will only generate on the outer End islands accessed by the End Gateway.").define("chromiumOnDragonIsland", false);
			builder.pop();
			builder.pop();
		}
	}
	
	public static enum DebugOptions{
		NONE, MESSAGE, STACK_TRACE;
	}
	
	public static enum OreGenOptions{
		UNIFORM((min, max) -> HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max))), 
		TRIANGLE((min, max) -> HeightRangePlacement.triangle(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)));
		
		public BiFunction<Integer, Integer, PlacementModifier> placementModifier;
		
		OreGenOptions(BiFunction<Integer, Integer, PlacementModifier> placementModifier){
			this.placementModifier = placementModifier;
		}
		
		public PlacementModifier apply(int min, int max) {
			return this.placementModifier.apply(min, max);
		}
	}
}
