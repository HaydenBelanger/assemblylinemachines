package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ItemCorruptedShard extends Item implements IGearboxFuel{

	public ItemCorruptedShard() {
		super(new Item.Properties());
	}


	@Override
	public Component getName(ItemStack stack) {
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:internalitem")) {
			ItemStack intStack = ItemStack.of(stack.getTag().getCompound("assemblylinemachines:internalitem"));
			return intStack.getDisplayName().copy().withStyle(ChatFormatting.OBFUSCATED);
		}else {
			return super.getName(stack);
		}
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type) {
		return 9600;
	}


	@Override
	public int getGearboxBurnTime(ItemStack stack) {
		return this.getBurnTime(stack, null);
	}

	public static ItemStack corruptItem(ItemStack orig) {
		if(orig.getItem() != Registry.getItem("corrupted_shard")) {
			CompoundTag main = new CompoundTag();
			CompoundTag sub = new CompoundTag();

			new ItemStack(orig.getItem(), 1).save(sub);
			main.put("assemblylinemachines:internalitem", sub);

			orig = new ItemStack(Registry.getItem("corrupted_shard"), orig.getCount());

			orig.setTag(main);
		}

		return orig;

	}
}
