package me.haydenb.assemblylinemachines.registry;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCreativeTab extends ItemGroup {

	
	
	public ModCreativeTab(String label) {
		super(label);
		
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Registry.getItem("steel_gear"));
	}
	
	
	@Override
	public void fill(NonNullList<ItemStack> items) {
		
		
		ArrayList<Item> data = new ArrayList<>(ForgeRegistries.ITEMS.getValues());
		data.sort(new Comparator<Item>() {

			@Override
			public int compare(Item i1, Item i2) {
				
				if(i1 instanceof BlockItem && !(i2 instanceof BlockItem)) {
					return -1;
				}else if(i2 instanceof BlockItem && !(i1 instanceof BlockItem)) {
					return 1;
				}else {
					return i1.getName().getUnformattedComponentText().compareTo(i2.getName().getUnformattedComponentText());
					
				}
			}
		});
		
		for(Item i : data) {
			
			i.fillItemGroup(this, items);
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
