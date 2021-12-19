package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class ItemUpgrade extends Item {

	
	private static final Component CAN_STACK = new TextComponent("This upgrade's effect can be stacked.").withStyle(ChatFormatting.DARK_GRAY);
	
	final String[] positives;
	final String[] negatives;
	
	
	final boolean stackable;
	public ItemUpgrade(boolean stackable, String[] positives, String[] negatives) {
		super(new Item.Properties().stacksTo(1).tab(Registry.CREATIVE_TAB));
		this.stackable = stackable;
		this.positives = positives;
		this.negatives = negatives;
	}
	
	public ItemUpgrade(boolean stackable, String positive, String negative) {
		super(new Item.Properties().stacksTo(1).tab(Registry.CREATIVE_TAB));
		this.stackable = stackable;
		this.positives = new String[] {positive};
		this.negatives = new String[] {negative};
	}
	
	public ItemUpgrade(boolean stackable, String positive) {
		super(new Item.Properties().stacksTo(1).tab(Registry.CREATIVE_TAB));
		this.stackable = stackable;
		this.positives = new String[] {positive};
		this.negatives = null;
	}
	
	
	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if(stackable) {
			tooltip.add(CAN_STACK);
		}
		for(String s : positives) {
			tooltip.add(new TextComponent(s).withStyle(ChatFormatting.GREEN));
		}
		
		if(negatives != null) {
			for(String s : negatives) {
				tooltip.add(new TextComponent(s).withStyle(ChatFormatting.DARK_RED));
			}
			super.appendHoverText(stack, worldIn, tooltip, flagIn);
		}
	}
	
	public static enum Upgrades{
		GB_EFFICIENCY(Registry.getItem("gearbox_upgrade_efficiency")),
		GB_COMPATABILITY(Registry.getItem("gearbox_upgrade_compatability")), GB_LIMITER(Registry.getItem("gearbox_upgrade_limiter")),
		
		PIPE_STACK(Registry.getItem("item_pipe_upgrade_stack")), 
		PIPE_FILTER(Registry.getItem("item_pipe_upgrade_filter")), PIPE_REDSTONE(Registry.getItem("item_pipe_upgrade_redstone")),
		
		UNIVERSAL_SPEED(Registry.getItem("upgrade_speed")),
		
		MACHINE_CONSERVATION(Registry.getItem("machine_upgrade_conservation")), MACHINE_EXTRA(Registry.getItem("machine_upgrade_extra")),
		MACHINE_GAS(Registry.getItem("machine_upgrade_gas")),
		
		AC_SUSTAINED(Registry.getItem("autocrafting_upgrade_sustained")), AC_RECIPES(Registry.getItem("autocrafting_upgrade_recipes")),
		
		GENERATOR_COOLANT(Registry.getItem("generator_upgrade_coolant")),
		
		EXP_MILL_LEVEL(Registry.getItem("experience_mill_upgrade_level")),
		
		PURIFIER_EXPANDED(Registry.getItem("purifier_upgrade_enhanced")),
		
		E_R_CAPACITY(Registry.getItem("entropy_reactor_upgrade_capacity")), E_R_CYCLE_DELAY(Registry.getItem("entropy_reactor_upgrade_cycle_delayer")),
		
		E_R_VARIETY(Registry.getItem("entropy_reactor_upgrade_variety")), E_R_ENTROPIC_HARNESSER(Registry.getItem("entropy_reactor_upgrade_entropic_harnesser")),
		
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
