package me.haydenb.assemblylinemachines.world;

import java.util.*;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockBlackGranite;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Generation {

	private static final RuleTest END_RULE_TEST = new BlockMatchRuleTest(Blocks.END_STONE);
	
	@SubscribeEvent
	public static void completeLoad(FMLLoadCompleteEvent e) {

		Block titanium = Registry.getBlock("titanium_ore");
		Block black_granite = Registry.getBlock("black_granite");
		Block chromium = Registry.getBlock("chromium_ore");
		
		register(black_granite.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_NETHER, black_granite.getDefaultState().with(BlockBlackGranite.NATURAL_GRANITE, true), 37)).range(126).square().count(7));
		register(titanium.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, titanium.getDefaultState(), 8)).range(16).square().count(3));
		register(chromium.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(END_RULE_TEST, chromium.getDefaultState(), 10)).range(255).square());
	}
	
	@SuppressWarnings("deprecation")
	private static void register(ResourceLocation rl, ConfiguredFeature<?, ?> feature) {
		net.minecraft.util.registry.Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, rl.toString(), feature);

		for (Map.Entry<RegistryKey<Biome>, Biome> biome : WorldGenRegistries.BIOME.getEntries()) {
			addFeatureToBiome(biome.getValue(), GenerationStage.Decoration.UNDERGROUND_ORES, WorldGenRegistries.CONFIGURED_FEATURE.getOrDefault(rl));
		}
	}

	public static void addFeatureToBiome(Biome biome, GenerationStage.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
		List<List<Supplier<ConfiguredFeature<?, ?>>>> biomeFeatures = new ArrayList<>(biome.getGenerationSettings().getFeatures());

		while (biomeFeatures.size() <= decoration.ordinal()) {
			biomeFeatures.add(Lists.newArrayList());
		}

		List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>(biomeFeatures.get(decoration.ordinal()));
		features.add(() -> configuredFeature);
		biomeFeatures.set(decoration.ordinal(), features);

		//Mapping change requires use of accesstransformer.
		biome.getGenerationSettings().features = biomeFeatures;
	}
}
