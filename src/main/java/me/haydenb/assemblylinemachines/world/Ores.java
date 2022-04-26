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
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE, modid = AssemblyLineMachines.MODID)
public class Ores {
	
	//Configured Features
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> titaniumOreFeature;
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> blackGraniteFeature;
	public static Holder<ConfiguredFeature<OreConfiguration, ?>> chromiumOreFeature;
	
	//Placed Features
	public static Holder<PlacedFeature> titaniumOrePlaced;
	public static Holder<PlacedFeature> blackGranitePlaced;
	public static Holder<PlacedFeature> chromiumOrePlaced;
	
	@SubscribeEvent
	public static void placeOres(BiomeLoadingEvent event) {
		if(titaniumOrePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(titaniumOrePlaced.value()));
		if(blackGranitePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(blackGranitePlaced.value()));
		if(chromiumOrePlaced != null) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(chromiumOrePlaced.value()));
	}
	
	public static class BlacklistBiomeFilter extends BiomeFilter{
		
		private final List<ResourceLocation> blacklistedBiomes;
		
		public BlacklistBiomeFilter(List<ResourceLocation> blacklistedBiomes){
			this.blacklistedBiomes = blacklistedBiomes;
		}
		
		@Override
		protected boolean shouldPlace(PlacementContext context, Random random, BlockPos pos) {
			if(blacklistedBiomes.contains(context.getLevel().getBiome(pos).value().getRegistryName())) return false;
			return super.shouldPlace(context, random, pos);
		}
	}
	
	public static void registerOres() {
		//Vanilla dimension ores placed features
		titaniumOreFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_titanium", Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, Registry.getBlock("titanium_ore").defaultBlockState()),
				OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, Registry.getBlock("deepslate_titanium_ore").defaultBlockState()),
				OreConfiguration.target(new BlockMatchTest(Registry.getBlock("corrupt_stone")), Registry.getBlock("corrupt_titanium_ore").defaultBlockState())), ConfigHolder.getServerConfig().titaniumVeinSize.get()));
		BlockState state = ConfigHolder.getServerConfig().blackGraniteSpawnsWithNaturalTag.get() ? Registry.getBlock("black_granite").defaultBlockState().setValue(BlockBlackGranite.NATURAL_GRANITE, true) : Registry.getBlock("black_granite").defaultBlockState();
		blackGraniteFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_black_granite", Feature.ORE, new OreConfiguration(OreFeatures.NETHER_ORE_REPLACEABLES, state, ConfigHolder.getServerConfig().blackGraniteVeinSize.get()));
		chromiumOreFeature = FeatureUtils.register(AssemblyLineMachines.MODID + ":ore_chromium", Feature.ORE, new OreConfiguration(new BlockMatchTest(Blocks.END_STONE), Registry.getBlock("chromium_ore").defaultBlockState(), ConfigHolder.getServerConfig().chromiumVeinSize.get()));
		
		//Vanilla dimension ores configured features
		if(ConfigHolder.getServerConfig().titaniumFrequency.get() != 0 && ConfigHolder.getServerConfig().titaniumVeinSize.get() != 0)
			titaniumOrePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_titanium", titaniumOreFeature, OrePlacements.commonOrePlacement(ConfigHolder.getServerConfig().titaniumFrequency.get(), ConfigHolder.getServerConfig().titaniumOreGenStyle.get().apply(ConfigHolder.getServerConfig().titaniumMinHeight.get(), ConfigHolder.getServerConfig().titaniumMaxHeight.get())));
		if(ConfigHolder.getServerConfig().blackGraniteFrequency.get() != 0 && ConfigHolder.getServerConfig().blackGraniteVeinSize.get() != 0)
			blackGranitePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_black_granite", blackGraniteFeature, OrePlacements.commonOrePlacement(ConfigHolder.getServerConfig().blackGraniteFrequency.get(), PlacementUtils.FULL_RANGE));
		if(ConfigHolder.getServerConfig().chromiumFrequency.get() != 0 && ConfigHolder.getServerConfig().chromiumVeinSize.get() != 0)
			chromiumOrePlaced = PlacementUtils.register(AssemblyLineMachines.MODID + ":ore_chromium", chromiumOreFeature, List.of(CountPlacement.of(ConfigHolder.getServerConfig().chromiumFrequency.get()), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, ConfigHolder.getServerConfig().chromiumOnDragonIsland.get() ? BiomeFilter.biome() : new BlacklistBiomeFilter(List.of(Biomes.THE_END.location()))));
	}
}
