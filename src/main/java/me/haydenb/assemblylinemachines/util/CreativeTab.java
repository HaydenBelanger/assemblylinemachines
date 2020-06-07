package me.haydenb.assemblylinemachines.util;

import java.util.Comparator;
import java.util.TreeSet;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;

public class CreativeTab extends ItemGroup {

	
	
	public CreativeTab(String label) {
		super(label);
		
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Registry.getItem("titanium_ingot"));
	}
	
	@Override
	public void fill(NonNullList<ItemStack> items) {
		
		TreeSet<Item> data = new TreeSet<>(new Comparator<Item>() {

			@Override
			public int compare(Item i1, Item i2) {
				if(i1 instanceof BlockItem && !(i2 instanceof BlockItem)) {
					return -1;
				}else if(i2 instanceof BlockItem && !(i2 instanceof BlockItem)) {
					return 1;
				}else {
					
					return i1.getName().getUnformattedComponentText().compareTo(i2.getName().getUnformattedComponentText());
					
				}
			}
		});
		
		data.addAll(ForgeRegistries.ITEMS.getValues());
		
		for(Item i : data) {
			
			i.fillItemGroup(this, items);
		}
	}
	
	
}
