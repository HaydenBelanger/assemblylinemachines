package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemGearboxUpgrade extends Item {

	final String[] positives;
	final String[] negatives;
	public ItemGearboxUpgrade(String[] positives, String[] negatives) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.positives = positives;
		this.negatives = negatives;
	}
	
	public ItemGearboxUpgrade(String positive, String negative) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.positives = new String[] {positive};
		this.negatives = new String[] {negative};
	}
	
	public ItemGearboxUpgrade(String positive) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.positives = new String[] {positive};
		this.negatives = null;
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		for(String s : positives) {
			tooltip.add(new StringTextComponent(s).applyTextStyle(TextFormatting.GREEN));
		}
		
		if(negatives != null) {
			for(String s : negatives) {
				tooltip.add(new StringTextComponent(s).applyTextStyle(TextFormatting.DARK_RED));
			}
			super.addInformation(stack, worldIn, tooltip, flagIn);
		}
	}
	
	public static enum Upgrades{
		EFFICIENCY(Registry.getItem("gearbox_upgrade_efficiency")), POWER(Registry.getItem("gearbox_upgrade_speed")), 
		COMPATABILITY(Registry.getItem("gearbox_upgrade_compatability")), LIMITER(Registry.getItem("gearbox_upgrade_limiter")), NONE(null);
		
		public final Item i;
		
		Upgrades(Item i){
			this.i = i;
		}
		
		public static Upgrades match(Item i) {
			for(Upgrades u : values()) {
				if(u.i == i) {
					return u;
				}
			}
			
			return Upgrades.NONE;
		}
		
		public static Upgrades match(ItemStack i) {
			for(Upgrades u : values()) {
				if(u.i == i.getItem()) {
					return u;
				}
			}
			
			return Upgrades.NONE;
		}
	}
}
