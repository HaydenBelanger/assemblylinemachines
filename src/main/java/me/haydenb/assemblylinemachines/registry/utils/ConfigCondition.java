package me.haydenb.assemblylinemachines.registry.utils;

import java.lang.reflect.Field;
import java.util.Optional;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ALMCommonConfig;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
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
				Field field = ObfuscationReflectionHelper.findField(ALMCommonConfig.class, configOption);
				Object obj = field.get(ConfigHolder.getCommonConfig());
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
	public boolean test() {
		return fieldResult.get().get() == enableOn;
	}
	
	public static class ConfigConditionSerializer implements IConditionSerializer<ConfigCondition>{

		public static final ConfigConditionSerializer INSTANCE = new ConfigConditionSerializer();
		
		@Override
		public void write(JsonObject json, ConfigCondition value) {
			json.addProperty("configOption", value.configOption);
			if(!value.enableOn) json.addProperty("enableOn", value.enableOn);
		}

		@Override
		public ConfigCondition read(JsonObject json) {
			boolean enableOn = GsonHelper.getAsBoolean(json, "enableOn", true);
			return new ConfigCondition(GsonHelper.getAsString(json, "configOption"), enableOn);
		}

		@Override
		public ResourceLocation getID() {
			return ID;
		}
		
	}
}
