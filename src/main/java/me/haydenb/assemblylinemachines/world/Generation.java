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
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.template.BlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Generation {

	private static final RuleTest END_RULE_TEST = new BlockMatchRuleTest(Blocks.END_STONE);
	
	@SubscribeEvent
	public static void completeLoad(FMLLoadCompleteEvent e) {

		Block titanium = Registry.getBlock("titanium_ore");
		Block black_granite = Registry.getBlock("black_granite");
		Block chromium = Registry.getBlock("chromium_ore");
		
		register(black_granite.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241883_b, black_granite.getDefaultState().with(BlockBlackGranite.NATURAL_GRANITE, true), 37)).func_242733_d(126).func_242728_a().func_242731_b(7));
		register(titanium.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241882_a, titanium.getDefaultState(), 8)).func_242733_d(16).func_242728_a().func_242731_b(3));
		register(chromium.getRegistryName(), Feature.ORE.withConfiguration(new OreFeatureConfig(END_RULE_TEST, chromium.getDefaultState(), 10)).func_242733_d(255).func_242728_a());
	}
	
	@SuppressWarnings("deprecation")
	private static void register(ResourceLocation rl, ConfiguredFeature<?, ?> feature) {
		net.minecraft.util.registry.Registry.register(WorldGenRegistries.field_243653_e, rl.toString(), feature);

		for (Map.Entry<RegistryKey<Biome>, Biome> biome : WorldGenRegistries.field_243657_i.func_239659_c_()) {
			addFeatureToBiome(biome.getValue(), GenerationStage.Decoration.UNDERGROUND_ORES, WorldGenRegistries.field_243653_e.getOrDefault(rl));
		}
	}

	public static void addFeatureToBiome(Biome biome, GenerationStage.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
		List<List<Supplier<ConfiguredFeature<?, ?>>>> biomeFeatures = new ArrayList<>(biome.func_242440_e().func_242498_c());

		while (biomeFeatures.size() <= decoration.ordinal()) {
			biomeFeatures.add(Lists.newArrayList());
		}

		List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>(biomeFeatures.get(decoration.ordinal()));
		features.add(() -> configuredFeature);
		biomeFeatures.set(decoration.ordinal(), features);

		ObfuscationReflectionHelper.setPrivateValue(BiomeGenerationSettings.class, biome.func_242440_e(), biomeFeatures, "field_242484_f");
	}
}
