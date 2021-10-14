package me.haydenb.assemblylinemachines.registry;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCreativeTab extends CreativeModeTab {

	
	
	public ModCreativeTab(String label) {
		super(label);
		
	}

	
	@Override
	public ItemStack makeIcon() {
		return new ItemStack(Registry.getItem("steel_gear"));
	}
	
	@Override
	public void fillItemList(NonNullList<ItemStack> items) {
		
		
		ArrayList<Item> data = new ArrayList<>(ForgeRegistries.ITEMS.getValues());
		data.sort(new Comparator<Item>() {

			@Override
			public int compare(Item i1, Item i2) {
				
				if(i1 instanceof BlockItem && !(i2 instanceof BlockItem)) {
					return -1;
				}else if(i2 instanceof BlockItem && !(i1 instanceof BlockItem)) {
					return 1;
				}else {
					return i1.getName(i1.getDefaultInstance()).getContents().compareTo(i2.getName(i2.getDefaultInstance()).getContents());
					
				}
			}
		});
		
		for(Item i : data) {
			i.fillItemCategory(this, items);
		}
	}
	
	@Override
	public ResourceLocation getBackgroundImage() {
		return new ResourceLocation("assemblylinemachines", "textures/gui/creative/page.png");
	}
	
	@Override
	public ResourceLocation getTabsImage() {
		return new ResourceLocation("assemblylinemachines", "textures/gui/creative/tabs.png");
	}
}
