package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Utils.Localization;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemUpgrade extends Item {

	
	private static final ITextComponent CAN_STACK = Localization.get("tooltip", "upgrade_canstack").applyTextStyle(TextFormatting.DARK_GRAY);
	
	final TranslationTextComponent[] positives;
	final TranslationTextComponent[] negatives;
	
	
	final boolean stackable;
	public ItemUpgrade(boolean stackable, TranslationTextComponent[] positives, TranslationTextComponent[] negatives) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = positives;
		this.negatives = negatives;
	}
	
	public ItemUpgrade(boolean stackable, TranslationTextComponent positive, TranslationTextComponent negative) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = new TranslationTextComponent[] {positive};
		this.negatives = new TranslationTextComponent[] {negative};
	}
	
	public ItemUpgrade(boolean stackable, TranslationTextComponent positive) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = new TranslationTextComponent[] {positive};
		this.negatives = null;
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(stackable) {
			tooltip.add(CAN_STACK);
		}
		for(TranslationTextComponent s : positives) {
			tooltip.add(s.applyTextStyle(TextFormatting.GREEN));
		}
		
		if(negatives != null) {
			for(TranslationTextComponent s : negatives) {
				tooltip.add(s.applyTextStyle(TextFormatting.DARK_RED));
			}
			super.addInformation(stack, worldIn, tooltip, flagIn);
		}
	}
	
	public static enum Upgrades{
		GB_EFFICIENCY(Registry.getItem("gearbox_upgrade_efficiency")),
		GB_COMPATABILITY(Registry.getItem("gearbox_upgrade_compatability")), GB_LIMITER(Registry.getItem("gearbox_upgrade_limiter")),
		
		PIPE_STACK(Registry.getItem("item_pipe_upgrade_stack")), 
		PIPE_FILTER(Registry.getItem("item_pipe_upgrade_filter")), PIPE_REDSTONE(Registry.getItem("item_pipe_upgrade_redstone")),
		
		UNIVERSAL_SPEED(Registry.getItem("upgrade_speed")),
		
		MACHINE_CONSERVATION(Registry.getItem("machine_upgrade_conservation")),
		
		AC_SUSTAINED(Registry.getItem("autocrafting_upgrade_sustained")), AC_RECIPES(Registry.getItem("autocrafting_upgrade_recipes")),
		
		NONE(null);
		
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
