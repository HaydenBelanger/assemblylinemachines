package me.haydenb.assemblylinemachines.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Lifecycle;

import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.world.level.storage.PrimaryLevelData;

@Mixin(PrimaryLevelData.class)
public class MixinExperimentalWorldStifler {

	@Inject(method = "worldGenSettingsLifecycle", at = @At("HEAD"), cancellable = true)
	public void injectStableLifecycle(CallbackInfoReturnable<Lifecycle> cir) {
		Lifecycle lf;
		if(ConfigHolder.COMMON.experimentalWorldScreenDisable.get()) {
			lf = Lifecycle.stable();
		}else {
			lf = ((PrimaryLevelData)(Object) this).worldGenSettingsLifecycle;
		}
		cir.setReturnValue(lf);
	}
}