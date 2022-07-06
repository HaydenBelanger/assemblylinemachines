package me.haydenb.assemblylinemachines.registry.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig.Common;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ConfigMatchTest extends RuleTest {

	public static final Codec<ConfigMatchTest> CODEC = RecordCodecBuilder.<ConfigMatchTest>create((instance) -> {
		return instance.group(
		Codec.BOOL.optionalFieldOf("enabled_on", true).forGetter((value) -> value.enableOn),
		Codec.list(Codec.STRING).fieldOf("config_option").forGetter((value) -> value.configOptions),
		RuleTest.CODEC.fieldOf("rule_test").forGetter((value) -> value.ruleTest))
		.apply(instance, ConfigMatchTest::new);
	});

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
	public boolean test(BlockState pState, RandomSource pRandom) {
		return passedAllOptions.get().get() ? ruleTest.test(pState, pRandom) : false;
	}

	@Override
	protected RuleTestType<?> getType() {
		return Registry.CONFIG_RULE_TEST.get();
	}
}