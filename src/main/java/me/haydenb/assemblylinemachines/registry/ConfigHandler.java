package me.haydenb.assemblylinemachines.registry;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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
		
		//MIXINS
		public final BooleanValue experimentalWorldScreenDisable;
		public final BooleanValue farBlockPosGeneratorSuppressed;
		public final BooleanValue seedUnification;
		public final BooleanValue oceansOfDarkEnergy;
		
		//MACHINE OPTIONS
		public final BooleanValue coolDudeMode;
		public final BooleanValue interactorInteractMode;
		public final BooleanValue reactorExplosions;
		public final EnumValue<DebugOptions> interactorInteractDebug;
		public final ConfigValue<Integer> crankSnapChance;
		
		//MISC/WORLD
		public final BooleanValue jeiSupport;
		public final BooleanValue updateChecker;
		//public final BooleanValue guideBook;
		public final BooleanValue mystiumFarmlandDeath;
		public final BooleanValue gasolineExplosions;
		
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
		public final ConfigValue<Integer> mystiumToolEnchantability;
		public final ConfigValue<Integer> mystiumToolDurability;
		public final ConfigValue<Integer> mystiumToolMaxFE;
		
		//NOVASTEEL TOOLS
		public final ConfigValue<Double> novasteelToolAttack;
		public final ConfigValue<Double> novasteelToolHarvestSpeed;
		public final ConfigValue<Integer> novasteelToolEnchantability;
		public final ConfigValue<Integer> novasteelToolDurability;
		public final ConfigValue<Integer> novasteelToolMaxFE;
		
		//TITANIUM
		public final ConfigValue<Integer> titaniumVeinSize;
		public final ConfigValue<Integer> titaniumFrequency;
		public final ConfigValue<Integer> titaniumMinHeight;
		public final ConfigValue<Integer> titaniumMaxHeight;
		
		//CHROMIUM
		public final ConfigValue<Integer> chromiumVeinSize;
		public final ConfigValue<Integer> chromiumFrequency;
		public final BooleanValue chromiumOnDragonIsland;
		
		//BLACK GRANITE
		public final BooleanValue blackGraniteSpawnsWithNaturalTag;
		public final ConfigValue<Integer> blackGraniteVeinSize;
		public final ConfigValue<Integer> blackGraniteFrequency;
		
		//FLUIDS
		private final ConfigValue<List<? extends Config>> geothermalFluidsRaw;
		private final ConfigValue<List<? extends Config>> combustionFluidsRaw;
		private final ConfigValue<List<? extends Config>> coolantFluidsRaw;
		public final ArrayList<Pair<Fluid, Integer>> geothermalFluids = new ArrayList<>();
		public final ArrayList<Pair<Fluid, Integer>> combustionFluids = new ArrayList<>();
		public final ArrayList<Pair<Fluid, Integer>> coolantFluids = new ArrayList<>();
		
		static ModConfig clientConfig;
		
		public ASMConfig(final ForgeConfigSpec.Builder builder) {
			
			builder.push("Machine Options");
			crankSnapChance = builder.comment("If using the Crank without meaning, what chould be the chance it snaps? Higher number means lower chance. Set to -1 to disable snapping completely.").define("crankSnapChance", 100);
			interactorInteractMode = builder.comment("Interact Mode (in the Interactor block) can cause issues with intercompatability with some mods. Do you want this mode enabled?").define("interactorInteractMode", true);
			interactorInteractDebug = builder.comment("Should Interact Mode (in the Interactor block) fail with an exception, what type of logging should be performed?").defineEnum("interactorInteractDebug", DebugOptions.BASIC);
			reactorExplosions = builder.comment("If the Entropy Reactor reaches higher than 98% Entropy, should it explode?").define("reactorExplosions", true);
			
			Predicate<Object> fluidTestPredicate = (object) -> true;
			geothermalFluidsRaw = builder.comment("What fluids should be valid for use in the Geothermal Generator?").defineList("geothermalFluids", getFluidDefaultConfig(Pair.of("minecraft:lava", 115000), Pair.of("assemblylinemachines:naphtha", 650000)), fluidTestPredicate);
			combustionFluidsRaw = builder.comment("What fluids should be valid for use in the Combustion Generator?").defineList("combustionFluids", getFluidDefaultConfig(Pair.of("assemblylinemachines:liquid_carbon", 300000), Pair.of("assemblylinemachines:gasoline", 600000), Pair.of("assemblylinemachines:diesel", 1050000)), fluidTestPredicate);
			coolantFluidsRaw = builder.comment("What fluids should be valid for use as coolant in Combustion and Geothermal Generators? Value is multiplier on burn time.").defineList("coolantFluids", getFluidDefaultConfig(Pair.of("minecraft:water", 2), Pair.of("assemblylinemachines:condensed_void", 4)), fluidTestPredicate);
			builder.pop();
			
			builder.push("Tools and Armor");
			builder.push("Titanium");
			titaniumToolAttack = builder.comment("What is the base damage Titanium Tools should do?").define("titaniumToolAttack", 5d);
			titaniumToolHarvestSpeed = builder.comment("What is the base harvest speed Titanium Tools should do?").define("titaniumToolHarvestSpeed", 7d);
			titaniumEnchantability = builder.comment("What should the enchantability of Titanium Tools and Armor be?").define("titaniumEnchantability", 8);
			titaniumDurability = builder.comment("What should the base durability of Titanium Tools and Armor be?").define("titaniumDurability", 1150);
			titaniumArmorKnockbackResistance = builder.comment("What should the knockback resistance of Titanium Armor be?").define("titaniumArmorKnockbackResistance", 0d);
			titaniumArmorDamageReduction = builder.comment("What should the base damage reduction of Titanium Armor be?").define("titaniumArmorDamageReduction", 4);
			titaniumArmorToughness = builder.comment("What should the toughness of Titanium Armor be?").define("titaniumArmorToughness", 0d);
			builder.pop();
			
			builder.push("Steel");
			steelToolAttack = builder.comment("What is the base damage Steel Tools should do?").define("steelToolAttack", 7d);
			steelToolHarvestSpeed = builder.comment("What is the base harvest speed Steel Tools should do?").define("steelToolHarvestSpeed", 9d);
			steelEnchantability = builder.comment("What should the enchantability of Steel Tools and Armor be?").define("steelEnchantability", 6);
			steelDurability = builder.comment("What should the base durability of Steel Tools and Armor be?").define("steelDurability", 1800);
			steelArmorKnockbackResistance = builder.comment("What should the knockback resistance of Steel Armor be?").define("steelArmorKnockbackResistance", 0.10d);
			steelArmorDamageReduction = builder.comment("What should the base damage reduction of Steel Armor be?").define("steelArmorDamageReduction", 4);
			steelArmorToughness = builder.comment("What should the toughness of Steel Armor be?").define("steelArmorToughness", 0.5d);
			builder.pop();
			
			builder.push("Crank-Powered");
			crankToolAttack = builder.comment("What is the base damage Crank-Powered Tools should do?").define("crankToolAttack", 8d);
			crankToolHarvestSpeed = builder.comment("What is the base harvest speed Crank-Powered Tools should do?").define("crankToolHarvestSpeed", 11d);
			crankToolEnchantability = builder.comment("What should the enchantability of Crank-Powered Tools be?").define("crankToolEnchantability", 16);
			crankToolDurability = builder.comment("What should the base physical durability of Crank-Powered Tools be?").define("crankToolDurability", 75);
			crankToolMaxCranks = builder.comment("What should the base kinetic durability (Cranks) of Crank-Powered Tools be?").define("crankToolMaxCranks", 750);
			builder.pop();
			
			builder.push("Mystium");
			mystiumToolAttack = builder.comment("What is the base damage Mystium Tools should do?").define("mystiumToolAttack", 9d);
			mystiumToolHarvestSpeed = builder.comment("What is the base harvest speed Mystium Tools should do?").define("mystiumToolHarvestSpeed", 19d);
			mystiumToolEnchantability = builder.comment("What should the enchantability of Mystium Tools be?").define("mystiumToolEnchantability", 28);
			mystiumToolDurability = builder.comment("What should the base physical durability of Mystium Tools be?").define("mystiumToolDurability", 150);
			mystiumToolMaxFE = builder.comment("What should the base electrical durability (Forge Energy) of Mystium Tools be?").define("mystiumToolMaxFE", 1000000);
			builder.pop();
			
			builder.push("Novasteel");
			novasteelToolAttack = builder.comment("What is the base damage Novasteel Tools should do?").define("novasteelToolAttack", 10.5d);
			novasteelToolHarvestSpeed = builder.comment("What is the base harvest speed Novasteel Tools should do?").define("novasteelToolHarvestSpeed", 23d);
			novasteelToolEnchantability = builder.comment("What should the enchantability of Novasteel Tools be?").define("novasteelToolEnchantability", 37);
			novasteelToolDurability = builder.comment("What should the base physical durability of Novasteel Tools be?").define("novasteelToolDurability", 300);
			novasteelToolMaxFE = builder.comment("What should the base electrical durability (Forge Energy) of Novasteel Tools be?").define("novasteelToolMaxFE", 20000000);
			
			builder.pop();
			builder.push("World");
			mystiumFarmlandDeath = builder.comment("Should Mystium Farmland get exhausted over time and stop performing grow operations?").define("mystiumFarmlandDeath", true);
			updateChecker = builder.comment("Should the update check message be sent when a player joins a single-player world/the SMP server?").define("updateChecker", true);
			gasolineExplosions = builder.comment("Should Gasoline and Diesel explode when placed next to a flammable block?").define("gasolineExplosions", true);
			
			//guideBook = builder.comment("Should new players be given the guide book when joining the world (as long as Patchouli is installed?) NOTE: For v1.2, this toggle is not functional as Patchouli hasn't been updated.").define("guideBook", true);
			
			builder.push("Titanium Generation");
			titaniumVeinSize = builder.comment("What should the maximum size per vein of Standard/Deepslate/Corrupt Titanium Ore be? Set to 0 to disable completely.").define("titaniumVeinSize", 6);
			titaniumFrequency = builder.comment("How many veins of Standard/Deepslate/Corrupt Titanium Ore should generate per chunk? Set to 0 to disable completely.").define("titaniumFrequency", 3);
			titaniumMinHeight = builder.comment("What is the minimum Y value Standard/Deepslate/Corrupt Titanium Ore should spawn at in the overworld and Chaos Plane?").define("titaniumMinHeight", 8);
			titaniumMaxHeight = builder.comment("What is the maximum Y value Standard/Deepslate/Corrupt Titanium Ore should spawn at in the overworld and Chaos Plane?").define("titaniumMaxHeight", 16);
			builder.pop();
			
			builder.push("Black Granite Generation");
			blackGraniteSpawnsWithNaturalTag = builder.comment("Should generated Black Granite have the 'natural' tag, requiring progression to Crank-Powered Pickaxe as intended?").define("blackGraniteSpawnsWithNaturalTag", true);
			blackGraniteVeinSize = builder.comment("What should the maximum size per vein of Black Granite be? Set to 0 to disable completely.").define("blackGraniteVeinSize", 37);
			blackGraniteFrequency = builder.comment("How many veins of Black Granite should generate per chunk? Set to 0 to disable completely.").define("blackGraniteFrequency", 7);
			builder.pop();
			
			builder.push("Chromium Generation");
			chromiumVeinSize = builder.comment("What should the maximum size per vein of Chromium Ore be? Set to 0 to disable completely.").define("chromiumVeinSize", 10);
			chromiumFrequency = builder.comment("How many veins of Chromium Ore should generate per chunk? Set to 0 to disable completely.").define("chromiumFrequency", 4);
			chromiumOnDragonIsland = builder.comment("Should Chromium Ore generate on the Dragon Island in The End?").define("chromiumOnDragonIsland", false);
			builder.pop();
			builder.pop();
			
			builder.push("Client-Side-Only Options");
			coolDudeMode = builder.comment("Do you want to enable 'Cool Dude Mode', enabling easter-egg/meme effects?").define("coolDudeMode", false);
			jeiSupport = builder.comment("If JEI is installed, should support be enabled?").define("jeiSupport", true);
			builder.pop();
			
			builder.push("Experimental Mixin Settings");
			experimentalWorldScreenDisable = builder.comment("Should Assembly Line Machines suppress the World Experimental Settings screen? This will have no effect if another mod performs the same task.").define("experimentalWorldScreenDisable", true);
			farBlockPosGeneratorSuppressed = builder.comment("Should Assembly Line Machines suppress the warning related to far-away block generation error, an artifact of Mojang debug code? This will suppress ALL INSTANCES of this debug log being outputted.").define("farBlockPosGeneratorSuppressed", true);
			seedUnification = builder.comment("Should Assembly Line Machines attempt to unify the world seed between the Vanilla dimensions and the Chaos Plane? Otherwise, the world will use seed '0'.").define("seedUnification", true);
			oceansOfDarkEnergy = builder.comment("Should Assembly Line Machines inject Dark Energy as the default fluid in the Chaos Plane? Otherwise, the world will have no oceans. This will have no effect if 'seedUnification' is false.").define("oceansOfDarkEnergy", true);
			builder.pop();
			
			
			
		}
		
		public void validateConfig() {
			if(crankSnapChance.get() < 1 && crankSnapChance.get() != -1) {
				crankSnapChance.set(100);
			}
			
			checkFluidLists(Pair.of(geothermalFluidsRaw.get().iterator(), geothermalFluids), Pair.of(combustionFluidsRaw.get().iterator(), combustionFluids), Pair.of(coolantFluidsRaw.get().iterator(), coolantFluids));
			
			oreGenVerification();
			toolsVerification();
			
			
		}
		
		private void toolsVerification() {
			if(titaniumToolAttack.get() < 0.1d) {
				titaniumToolAttack.set(0.1d);
			}
			if(titaniumToolHarvestSpeed.get() < 0.1d) {
				titaniumToolHarvestSpeed.set(0.1d);
			}
			if(titaniumDurability.get() < 10) {
				titaniumDurability.set(10);
			}
			if(titaniumEnchantability.get() < 0) {
				titaniumEnchantability.set(0);
			}
			if(titaniumArmorKnockbackResistance.get() < 0d) {
				titaniumArmorKnockbackResistance.set(0d);
			}
			if(titaniumArmorDamageReduction.get() < 2) {
				titaniumArmorDamageReduction.set(2);
			}
			if(titaniumArmorToughness.get() < 0d) {
				titaniumArmorToughness.set(0d);
			}
			
			if(steelToolAttack.get() < 0.1d) {
				steelToolAttack.set(0.1d);
			}
			if(steelToolHarvestSpeed.get() < 0.1d) {
				steelToolHarvestSpeed.set(0.1d);
			}
			if(steelDurability.get() < 10) {
				steelDurability.set(10);
			}
			if(steelEnchantability.get() < 0) {
				steelEnchantability.set(0);
			}
			if(steelArmorKnockbackResistance.get() < 0d) {
				steelArmorKnockbackResistance.set(0d);
			}
			if(steelArmorDamageReduction.get() < 2) {
				steelArmorDamageReduction.set(2);
			}
			if(steelArmorToughness.get() < 0d) {
				steelArmorToughness.set(0d);
			}
			
			if(crankToolAttack.get() < 0.1d) {
				crankToolAttack.set(0.1d);
			}
			if(crankToolHarvestSpeed.get() < 0.1d) {
				crankToolHarvestSpeed.set(0.1d);
			}
			if(crankToolDurability.get() < 10) {
				crankToolDurability.set(10);
			}
			if(crankToolEnchantability.get() < 0) {
				crankToolEnchantability.set(0);
			}
			
			if(mystiumToolAttack.get() < 0.1d) {
				mystiumToolAttack.set(0.1d);
			}
			if(mystiumToolHarvestSpeed.get() < 0.1d) {
				mystiumToolHarvestSpeed.set(0.1d);
			}
			if(mystiumToolDurability.get() < 10) {
				mystiumToolDurability.set(10);
			}
			if(mystiumToolEnchantability.get() < 0) {
				mystiumToolEnchantability.set(0);
			}
		}
		
		private void oreGenVerification() {
			if(titaniumVeinSize.get() < 0) {
				titaniumVeinSize.set(0);
			}
			if(titaniumFrequency.get() < 0) {
				titaniumFrequency.set(0);
			}
			if(titaniumMinHeight.get() < 0) {
				titaniumMinHeight.set(0);
			}else if(titaniumMinHeight.get() > 255) {
				titaniumMinHeight.set(255);
			}
			if(titaniumMaxHeight.get() < titaniumMinHeight.get()) {
				titaniumMaxHeight.set(titaniumMinHeight.get());
			}else if(titaniumMaxHeight.get() < 0) {
				titaniumMaxHeight.set(0);
			}else if(titaniumMaxHeight.get() > 255) {
				titaniumMaxHeight.set(255);
			}
			
			if(blackGraniteFrequency.get() < 0) {
				blackGraniteFrequency.set(0);
			}
			if(blackGraniteVeinSize.get() < 0) {
				blackGraniteVeinSize.set(0);
			}
			
			if(chromiumFrequency.get() < 0) {
				chromiumFrequency.set(0);
			}
			if(chromiumVeinSize.get() < 0) {
				chromiumVeinSize.set(0);
			}
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
