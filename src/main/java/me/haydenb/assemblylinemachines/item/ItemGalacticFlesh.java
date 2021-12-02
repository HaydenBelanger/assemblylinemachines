package me.haydenb.assemblylinemachines.item;

import java.util.*;
import java.util.Map.Entry;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemGalacticFlesh extends Item{
	
	private static final Random RAND = new Random();
	
	public ItemGalacticFlesh() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB).food(new FoodProperties.Builder().effect(() -> getRandomMobEffect(), 1f).nutrition(6).fast().saturationMod(0.5f).meat().alwaysEat().build()));
	}
	
	private static MobEffectInstance getRandomMobEffect() {
		Set<Entry<ResourceKey<MobEffect>, MobEffect>> set = ForgeRegistries.MOB_EFFECTS.getEntries();
		Optional<Entry<ResourceKey<MobEffect>, MobEffect>> meO = ForgeRegistries.MOB_EFFECTS.getEntries().stream().skip((int) (set.size() * RAND.nextDouble())).findFirst();
		MobEffect me = meO.isPresent() && !meO.get().getValue().isInstantenous() ? meO.get().getValue() : MobEffects.HUNGER;
		return new MobEffectInstance(me, (RAND.nextInt(116) + 5) * 20, RAND.nextInt(5));
	}

}
