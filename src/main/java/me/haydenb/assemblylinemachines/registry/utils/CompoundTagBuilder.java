package me.haydenb.assemblylinemachines.registry.utils;

import java.util.Optional;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.nbt.CompoundTag;

public class CompoundTagBuilder {

	public static Builder builder() {
		return builder(null);
	}

	public static Builder builder(String prefix) {
		return builder(prefix, new CompoundTag());
	}

	public static Builder builder(String prefix, CompoundTag tag) {
		return new Builder(prefix, tag);
	}

	public static class Builder{

		private final String prefix;
		private final CompoundTag tag;


		public Builder(String prefix, CompoundTag tag) {
			this.prefix = Optional.ofNullable(prefix).orElse(AssemblyLineMachines.MODID);
			this.tag = tag;
		}

		public Builder bool(String key, boolean bool) {
			tag.putBoolean(prefix + ":" + key, bool);
			return this;
		}

		public CompoundTag build() {
			return tag;
		}
	}
}
