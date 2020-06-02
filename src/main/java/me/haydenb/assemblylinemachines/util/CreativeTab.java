package me.haydenb.assemblylinemachines.util;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTab extends ItemGroup {

	public CreativeTab(String label) {
		super(label);
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Registry.getItem("titanium_ingot"));
	}
	
	
}
