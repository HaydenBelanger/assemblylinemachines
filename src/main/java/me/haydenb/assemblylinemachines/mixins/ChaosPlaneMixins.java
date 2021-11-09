package me.haydenb.assemblylinemachines.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Lifecycle;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.world.generation.DimensionChaosPlane.SeededNoiseBasedChunkGenerator;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;

public class ChaosPlaneMixins {

	//This Mixin will redirect calls to the Minecraft warning logger for out-of-range generation, as they cannot be avoided.
	@Mixin(WorldGenRegion.class)
	private static final class BlockEditErrorSuppressor {
		
		@Redirect(method = "ensureCanWrite", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V"))
		private void logAndPauseIfInIde(String message) {
			
			if(!ConfigHolder.COMMON.farBlockPosGeneratorSuppressed.get()) Util.logAndPauseIfInIde(message);
		}
		
		
		
	}
	
	//This Mixin will make it so every world shows as STABLE even if it is actually EXPERIMENTAL, thereby suppressing the warning during world-load.
	@Mixin(PrimaryLevelData.class)
	private static final class LifecycleInterceptor {

		@Inject(method = "worldGenSettingsLifecycle", at = @At("HEAD"), cancellable = true)
		private void worldGenSettingsLifecycle(CallbackInfoReturnable<Lifecycle> cir) {
			if(ConfigHolder.COMMON.experimentalWorldScreenDisable.get()) {
				cir.setReturnValue(Lifecycle.stable());
			}else {
				cir.setReturnValue(((PrimaryLevelData)(Object) this).worldGenSettingsLifecycle);
			}
		}
	}
	
	//This Mixin will patch in the saveseed into all dimensions with generator of type SeededNoiseBasedChunkGenerator.
	@Mixin(WorldGenSettings.class)
	private static final class SeededNoiseGeneratorInjector {
		
		@Inject(method = "<init>(JZZLnet/minecraft/core/MappedRegistry;Ljava/util/Optional;)V", at = @At(value = "TAIL"))
		private void init(long seed, boolean generateFeautres, boolean generateBonusChest, MappedRegistry<LevelStem> dimensions, Optional<String> legacySettings, CallbackInfo ci) {
			if(ConfigHolder.COMMON.seedUnification.get()) {
				for(LevelStem d : dimensions) {
					if(d.generator() instanceof SeededNoiseBasedChunkGenerator) {
						SeededNoiseBasedChunkGenerator generator = (SeededNoiseBasedChunkGenerator) d.generator();
						d.generator = generator.withSeed(seed);
						ResourceLocation rl = dimensions.getKey(d);
						if(rl.equals(new ResourceLocation(AssemblyLineMachines.MODID, "chaos_plane"))) {
							AssemblyLineMachines.LOGGER.info("Patched in save-seed into Chaos Plane. Disable in config.");
						}else {
							AssemblyLineMachines.LOGGER.info("Patched in save-seed into " + rl + ", a datapack dimension using chunk generator type assemblylinemachines:seeded_noise. Disable in config.");
						}
					}
				}
			}
			
		}
		
	}
}
