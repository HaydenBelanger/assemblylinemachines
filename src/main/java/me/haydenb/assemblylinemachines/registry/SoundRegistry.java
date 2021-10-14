package me.haydenb.assemblylinemachines.registry;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundRegistry {

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AssemblyLineMachines.MODID);
	
	public static final RegistryObject<SoundEvent> SHADOW_AMBIENT = SOUNDS.register("shadow_ambient", () -> new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "shadow_ambient")));
	public static final RegistryObject<SoundEvent> SHADOW_HURT = SOUNDS.register("shadow_hurt", () -> new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "shadow_hurt")));
	public static final RegistryObject<SoundEvent> SHADOW_DEATH = SOUNDS.register("shadow_death", () -> new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "shadow_death")));
	public static final RegistryObject<SoundEvent> SHADOW_STEP = SOUNDS.register("shadow_step", () -> new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "shadow_step")));
	
}
