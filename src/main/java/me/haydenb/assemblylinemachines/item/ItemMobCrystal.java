package me.haydenb.assemblylinemachines.item;

import com.google.common.cache.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemMobCrystal extends Item{

	public static final LoadingCache<EntityType<?>, Integer> MOB_COLORS = CacheBuilder.newBuilder().build(CacheLoader.from((type) -> {
		
		SpawnEggItem sei = ForgeSpawnEggItem.fromEntityType(type);
		return sei != null ? sei.getColor(1) : 0x7d7d7d;
	}));

	private static final String TRANSLATION_TAG = Util.makeDescriptionId("item", new ResourceLocation(AssemblyLineMachines.MODID, "mob_crystal_tuned"));

	public ItemMobCrystal() {
		super(new Item.Properties());


	}

	@Override
	public Component getName(ItemStack stack) {

		if(stack.hasTag()) {

			EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(stack.getTag().getString("assemblylinemachines:mob")));
			if(entity != null) {
				return Component.translatable(TRANSLATION_TAG, entity.getDescription().copy().withStyle(ChatFormatting.AQUA));
			}

		}
		return super.getName(stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if(stack.hasTag()) {
			return true;
		}
		return super.isFoil(stack);
	}
}
