package me.haydenb.assemblylinemachines.registry.config;

import java.util.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig.Common;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ConfigMatchTest extends RuleTest {

	public static final Codec<ConfigMatchTest> CODEC = RecordCodecBuilder.<ConfigMatchTest>create((instance) -> {
		return instance.group(
		Codec.BOOL.optionalFieldOf("enabled_on", true).forGetter((value) -> value.enableOn),
		Codec.list(Codec.STRING).fieldOf("config_option").forGetter((value) -> value.configOptions),
		RuleTest.CODEC.fieldOf("rule_test").forGetter((value) -> value.ruleTest))
		.apply(instance, ConfigMatchTest::new);
	});
	
	public static final DeferredRegister<RuleTestType<?>> RULE_TEST_REGISTRY = DeferredRegister.create(Registry.RULE_TEST_REGISTRY, AssemblyLineMachines.MODID);
	public static final RegistryObject<RuleTestType<ConfigMatchTest>> CONFIG_TEST = RULE_TEST_REGISTRY.register("config", () -> () -> CODEC);
	
	private final RuleTest ruleTest;
	private final boolean enableOn;
	private final List<String> configOptions;
	private final Lazy<Optional<Boolean>> passedAllOptions;

	public ConfigMatchTest(boolean enableOn, List<String> configOptions, RuleTest ruleTest) {
		this.enableOn = enableOn;
		this.ruleTest = ruleTest;
		this.configOptions = configOptions;
		this.passedAllOptions = Lazy.of(() -> {
			for(String configOption : this.configOptions) {
				try {
					boolean result = ((BooleanValue) ObfuscationReflectionHelper.findField(Common.class, configOption).get(ALMConfig.getCommonConfig())).get();
					if(result != enableOn) return Optional.of(false);
				}catch(Exception e) {
					e.printStackTrace();
					return Optional.empty();
				}
			}
			return Optional.of(true);
		});
	}
	
	@Override
	public boolean test(BlockState pState, Random pRandom) {
		return passedAllOptions.get().get() == true ? ruleTest.test(pState, pRandom) : false;
	}
	
	@Override
	protected RuleTestType<?> getType() {
		return CONFIG_TEST.get();
	}
}