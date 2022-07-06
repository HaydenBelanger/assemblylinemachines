package me.haydenb.assemblylinemachines.registry.config;

import java.lang.reflect.Field;
import java.util.Optional;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig.Common;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ConfigCondition implements ICondition {

	private static final ResourceLocation ID = new ResourceLocation(AssemblyLineMachines.MODID, "config");

	private final String configOption;
	private final boolean enableOn;
	private final Lazy<Optional<Boolean>> fieldResult;

	public ConfigCondition(String configOption, boolean enableOn) {
		this.configOption = configOption;
		this.enableOn = enableOn;
		this.fieldResult = Lazy.of(() -> {
			try {
				Field field = ObfuscationReflectionHelper.findField(Common.class, configOption);
				Object obj = field.get(ALMConfig.getCommonConfig());
				return Optional.of(((BooleanValue) obj).get());
			} catch (Exception e) {
				e.printStackTrace();
				return Optional.empty();
			}
		});
	}

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test(IContext context) {
		return fieldResult.get().get() == enableOn;
	}

	public static class ConfigConditionSerializer implements IConditionSerializer<ConfigCondition>{

		public static final ConfigConditionSerializer INSTANCE = new ConfigConditionSerializer();

		@Override
		public void write(JsonObject json, ConfigCondition value) {
			json.addProperty("config_option", value.configOption);
			if(!value.enableOn) json.addProperty("enabled_on", value.enableOn);
		}

		@Override
		public ConfigCondition read(JsonObject json) {
			boolean enableOn = GsonHelper.getAsBoolean(json, "enabled_on", true);
			return new ConfigCondition(GsonHelper.getAsString(json, "config_option"), enableOn);
		}

		@Override
		public ResourceLocation getID() {
			return ID;
		}

	}
}
