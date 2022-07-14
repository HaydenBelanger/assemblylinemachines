package me.haydenb.assemblylinemachines.registry.config;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;

public class ALMConfig {

	private static final Pair<Client, ForgeConfigSpec> CLIENT = new ForgeConfigSpec.Builder().configure(Client::new);
	private static final Pair<Common, ForgeConfigSpec> COMMON = new ForgeConfigSpec.Builder().configure(Common::new);
	private static final Pair<Server, ForgeConfigSpec> SERVER = new ForgeConfigSpec.Builder().configure(Server::new);
	private static final Pair<PreInit, ForgeConfigSpec> PREINIT = new ForgeConfigSpec.Builder().configure(PreInit::new);
	
	public static Client getClientConfig() {
		return CLIENT.getKey();
	}
	
	public static Common getCommonConfig() {
		return COMMON.getKey();
	}
	
	public static Server getServerConfig() {
		return SERVER.getKey();
	}
	
	public static PreInit getPreInitConfig() {
		return PREINIT.getKey();
	}

	public static void registerSpecs(ModLoadingContext mlc) {
		mlc.registerConfig(Type.CLIENT, CLIENT.getValue(), fileName(Type.CLIENT.extension()));
		mlc.registerConfig(Type.COMMON, COMMON.getValue(), fileName(Type.COMMON.extension()));
		mlc.registerConfig(Type.SERVER, SERVER.getValue());
		loadPreInitConfig(mlc.getActiveContainer());
	}
	
	/**Config for anything accessed only on client-side during startup post-registry.*/
	public static final record Client(BooleanValue customTooltipColors, BooleanValue customTooltipFrames, BooleanValue receiveGuideBook, BooleanValue receiveUpdateMessages) {
		
		public Client(Builder builder) {
			this(
				builder.comment("Do you want to render custom tooltip frame colors for some specific items?", "If false, the tooltip will be standard.").define("customTooltipColors", true),
				builder.comment("Do you want to render custom tooltip frame textures for some specific items?", "If false, the tooltip will be standard. This has no effect if customTooltipColors is false.").define("customTooltipFrames", true),
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
	
	/**Config for all things needed prior to registry initialization. When client connects to server, it will attempt to validate that all options are the same.*/
	@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
	public static final record PreInit(ConfigValue<List<? extends Config>> toolStats) implements Serializable {
		
		public PreInit(Builder builder) {
			this(
				builder.comment("This config's values may be considered extremely unstable and may be removed at any time. Edit at your own risk.",
						"If the client config does not exactly match values given in the server config, you will be unable to join that server.",
						"Therefore, it's recommended to only edit these options when creating a modpack where all clients would always have the same default modified values.",
						"All tools have default stats, this option allows you to override those and provide your own stats.",
						"For more information, view the developer wiki.").defineListAllowEmpty(List.of("toolStats"), () -> List.of(), (o) -> {
					if(o instanceof Config c) {
						List<? extends Number> data = c.get("data");
						ToolDefaults td = Stream.of(ToolDefaults.values()).filter((tx) -> tx.toString().equals(c.get("type"))).findAny().orElse(null);
						return td != null && data.size() == td.statTypes.size();
					}
					return false;
				})
			);
		}
		
		public boolean configsMatch(CommentedConfig serverCfg) {
			List<? extends Config> serverToolStats = serverCfg.get("toolStats");
			List<? extends Config> clientToolStats = toolStats.get();
			if(serverToolStats.size() != clientToolStats.size()) return false;
			for(int i = 0; i < serverToolStats.size(); i++) {
				String clientType = clientToolStats.get(i).get("type");
				String serverType = serverToolStats.get(i).get("type");
				if(!clientType.equals(serverType)) return false;
				List<? extends Number> clientVals = clientToolStats.get(i).get("data");
				List<? extends Number> serverVals = serverToolStats.get(i).get("data");
				if(clientVals.size() != serverVals.size()) return false;
				for(int j = 0; j < serverVals.size(); j++) if(clientVals.get(j).doubleValue() != serverVals.get(j).doubleValue()) return false;
			}
			return true;
		}
		
		@SubscribeEvent
		public static void connectSendValidateRequest(PlayerLoggedInEvent event) {
			try {
				byte[] data = Files.readAllBytes(ConfigTracker.INSTANCE.fileMap().get(fileName("experimental")).getFullPath());
				PacketData pd = new PacketData("experimental_verify");
				pd.writeByteArray("data", data);
				PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()), pd);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public static void connectReceiveValidateRequest(PacketData pd) {
			CommentedConfig serverCfg = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(pd.get("data", byte[].class)));
			if(!getPreInitConfig().configsMatch(serverCfg)) Minecraft.crash(CrashReport.forThrowable(new RuntimeException("Client experimental config did not match server experimental config."), "Failed to validate config parity when joining server."));
			AssemblyLineMachines.LOGGER.debug("Passed verification of experimental config.");
		}
	}
	
	public static enum ToolDefaults{
		
		TITANIUM(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 5, 7, 8, 1150, 0, 4, 0),
		STEEL(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 7, 9, 6, 1800, 0.1, 4, 2.5),
		CRANK(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.SP_ENERGY), 8, 11, 16, 75, 750),
		MYSTIUM(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH, Stats.SP_ENERGY, Stats.SP_ENH_ENERGY), 9, 19, 28, 150, 0.15, 7, 5, 1000000, 10000000),
		NOVASTEEL(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.SP_ENERGY), 10.5, 23, 37, 300, 20000000),
		CRG(List.of(Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 3, 750, 0, 5, 0);
		
		private final List<Stats> statTypes;
		private final HashMap<Stats, Number> stats = new HashMap<>();
		
		ToolDefaults(List<Stats> statTypes, Number... data){
			if(statTypes.size() != data.length) throw new IllegalArgumentException("Stat types does not equal data length.");
			this.statTypes = statTypes;
			for(int i = 0; i < statTypes.size(); i++) stats.put(statTypes.get(i), data[i]);
		}
		
		public Number get(Stats stat) {
			int index = statTypes.indexOf(stat);
			List<? extends Number> config = getPreInitConfig().toolStats().get().stream().filter((cfg) -> cfg.get("type").toString().equals(this.toString())).findAny().map((c) -> {
				List<? extends Number> res = c.get("data");
				return res;
			}).orElse(List.of());
			return !config.isEmpty() && config.size() > index ? config.get(index) : stats.get(stat);
		}
	}
	
	public static enum Stats{
		ATTACK, HRV_SPEED, ENCHANT, DURABILITY, KB_RES, D_REDUCTION, TOUGH, SP_ENERGY, SP_ENH_ENERGY;
	}
	
	public static enum DebugOptions{
		NONE(null), MESSAGE(Level.WARN), STACK_TRACE(Level.DEBUG);
		
		public final Level level;
		
		DebugOptions(Level level){
			this.level = level;
		}
	}
	
	public static void loadPreInitConfig(ModContainer container) {
		try {
			Method method = ObfuscationReflectionHelper.findMethod(ConfigTracker.class, "openConfig", ModConfig.class, Path.class);
			method.invoke(ConfigTracker.INSTANCE, new ModConfig(Type.COMMON, PREINIT.getValue(), container, fileName("experimental")), FMLPaths.CONFIGDIR.get());
		}catch(Exception e) {
			throw new RuntimeException("Error loading experimental config for Assembly Line Machines.", e);
		}
	}
	
	private static String fileName(String type) {
		return AssemblyLineMachines.MODID + "/" + AssemblyLineMachines.MODID + "-" + type + ".toml";
	}
}
