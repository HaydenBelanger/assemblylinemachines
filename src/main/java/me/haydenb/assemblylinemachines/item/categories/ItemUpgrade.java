package me.haydenb.assemblylinemachines.item.categories;

import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemUpgrade extends Item {

	
	private static final ITextComponent CAN_STACK = new StringTextComponent("This upgrade's effect can be stacked.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY);
	
	final String[] positives;
	final String[] negatives;
	
	
	final boolean stackable;
	public ItemUpgrade(boolean stackable, String[] positives, String[] negatives) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = positives;
		this.negatives = negatives;
	}
	
	public ItemUpgrade(boolean stackable, String positive, String negative) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = new String[] {positive};
		this.negatives = new String[] {negative};
	}
	
	public ItemUpgrade(boolean stackable, String positive) {
		super(new Item.Properties().maxStackSize(1).group(Registry.creativeTab));
		this.stackable = stackable;
		this.positives = new String[] {positive};
		this.negatives = null;
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(stackable) {
			tooltip.add(CAN_STACK);
		}
		for(String s : positives) {
			tooltip.add(new StringTextComponent(s).func_230532_e_().func_240699_a_(TextFormatting.GREEN));
		}
		
		if(negatives != null) {
			for(String s : negatives) {
				tooltip.add(new StringTextComponent(s).func_230532_e_().func_240699_a_(TextFormatting.DARK_RED));
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
		
		MACHINE_CONSERVATION(Registry.getItem("machine_upgrade_conservation")), MACHINE_EXTRA(Registry.getItem("machine_upgrade_extra")),
		MACHINE_GAS(Registry.getItem("machine_upgrade_gas")),
		
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
