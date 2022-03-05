package me.haydenb.assemblylinemachines.world;

import java.util.List;
import java.util.Random;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class VanillaDimensionOres {
	
	//Configured Features
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> titaniumOreFeature;
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> blackGraniteFeature;
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> chromiumOreFeature;
	
	//Placed Features
	public static Holder<PlacedFeature> titaniumOrePlaced;
	public static Holder<PlacedFeature> blackGranitePlaced;
	public static Holder<PlacedFeature> chromiumOrePlaced;
	
	public static void registerConfiguredFeatures() {
		titaniumOreFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_titanium", Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState()),
				OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState()),
				OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_stone")), Registry.getBlock("corrupt_titanium_ore").defaultBlockState())), ConfigHolder.getCommonConfig().titaniumVeinSize.get()));
		
		BlockState state = ConfigHolder.getCommonConfig().blackGraniteSpawnsWithNaturalTag.get() ? Registry.getBlock("black_granite").defaultBlockState().setValue(BlockBlackGranite.NATURAL_GRANITE, true) : Registry.getBlock("black_granite").defaultBlockState();
		blackGraniteFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_black_granite", Feature.ORE, new OreConfiguration(OreFeatures.NETHER_ORE_REPLACEABLES, state, ConfigHolder.getCommonConfig().blackGraniteVeinSize.get()));
	
		chromiumOreFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_chromium", Feature.ORE, new OreConfiguration(new BlockMatchTest(Blocks.END_STONE), Registry.getBlock("chromium_ore").defaultBlockState(), ConfigHolder.getCommonConfig().chromiumVeinSize.get()));
	}
	
	public static void registerPlacedFeatures() {
		
		
		
		if(ConfigHolder.getCommonConfig().titaniumFrequency.get() != 0 && ConfigHolder.getCommonConfig().titaniumVeinSize.get() != 0) {
			titaniumOrePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_titanium", titaniumOreFeature, OrePlacements.commonOrePlacement(ConfigHolder.getCommonConfig().titaniumFrequency.get(), ConfigHolder.getCommonConfig().titaniumOreGenStyle.get().apply(ConfigHolder.getCommonConfig().titaniumMinHeight.get(), ConfigHolder.getCommonConfig().titaniumMaxHeight.get())));
		}
		
		if(ConfigHolder.getCommonConfig().blackGraniteFrequency.get() != 0 && ConfigHolder.getCommonConfig().blackGraniteVeinSize.get() != 0) {
			blackGranitePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_black_granite", blackGraniteFeature, OrePlacements.commonOrePlacement(ConfigHolder.getCommonConfig().blackGraniteFrequency.get(), PlacementUtils.FULL_RANGE));
		}
		
		if(ConfigHolder.getCommonConfig().chromiumFrequency.get() != 0 && ConfigHolder.getCommonConfig().chromiumVeinSize.get() != 0) {
			chromiumOrePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_chromium", chromiumOreFeature, List.of(CountPlacement.of(ConfigHolder.getCommonConfig().chromiumFrequency.get()), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, ConfigHolder.getCommonConfig().chromiumOnDragonIsland.get() ? BiomeFilter.biome() : new BlacklistBiomeFilter(List.of(Biomes.THE_END.location()))));
		}
	}
	
	//Called from main class.
	public static void generationBusRegistration() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> {
			event.enqueueWork(() -> {
				registerConfiguredFeatures();
				registerPlacedFeatures();
			});
		});
		
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (BiomeLoadingEvent event) -> {
			if(titaniumOrePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(titaniumOrePlaced.value()));
			if(blackGranitePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(blackGranitePlaced.value()));
			if(chromiumOrePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(chromiumOrePlaced.value()));
		});
	}
	
	private static class BlacklistBiomeFilter extends BiomeFilter{
		
		private final List<ResourceLocation> blacklistedBiomes;
		
		private BlacklistBiomeFilter(List<ResourceLocation> blacklistedBiomes){
			this.blacklistedBiomes = blacklistedBiomes;
		}
		
		@Override
		protected boolean shouldPlace(PlacementContext context, Random random, BlockPos pos) {
			if(blacklistedBiomes.contains(context.getLevel().getBiome(pos).value().getRegistryName())) return false;
			return super.shouldPlace(context, random, pos);
		}
	}
}
