package me.haydenb.assemblylinemachines.item;

import java.util.Arrays;
import java.util.List;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemUpgrade extends Item {


	private static final Component CAN_STACK = Component.literal("This upgrade's effect can be stacked.").withStyle(ChatFormatting.DARK_GRAY);

	final String[] positives;
	final String[] negatives;


	final boolean stackable;
	public ItemUpgrade(boolean stackable, String[] positives, String[] negatives) {
		super(new Item.Properties().stacksTo(1));
		this.stackable = stackable;
		this.positives = positives;
		this.negatives = negatives;
	}

	public ItemUpgrade(boolean stackable, String positive, String negative) {
		super(new Item.Properties().stacksTo(1));
		this.stackable = stackable;
		this.positives = new String[] {positive};
		this.negatives = new String[] {negative};
	}

	public ItemUpgrade(boolean stackable, String positive) {
		super(new Item.Properties().stacksTo(1));
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
			tooltip.add(Component.literal(s).withStyle(ChatFormatting.GREEN));
		}

		if(negatives != null) {
			for(String s : negatives) {
				tooltip.add(Component.literal(s).withStyle(ChatFormatting.DARK_RED));
			}
			super.appendHoverText(stack, worldIn, tooltip, flagIn);
		}
	}

	public static enum Upgrades{
		GB_EFFICIENCY("gearbox_upgrade_efficiency"),
		GB_COMPATABILITY("gearbox_upgrade_compatability"), GB_LIMITER("gearbox_upgrade_limiter"),

		PIPE_STACK("item_pipe_upgrade_stack"),
		PIPE_FILTER("item_pipe_upgrade_filter"), PIPE_REDSTONE("item_pipe_upgrade_redstone"),

		UNIVERSAL_SPEED("upgrade_speed"),

		MACHINE_CONSERVATION("machine_upgrade_conservation"), MACHINE_EXTRA("machine_upgrade_extra"),
		MACHINE_GAS("machine_upgrade_gas"),

		AC_SUSTAINED("autocrafting_upgrade_sustained"), AC_RECIPES("autocrafting_upgrade_recipes"),

		GENERATOR_COOLANT("generator_upgrade_coolant"),

		EXP_MILL_LEVEL("experience_mill_upgrade_level"),

		PURIFIER_EXPANDED("purifier_upgrade_enhanced"),

		E_R_CAPACITY("entropy_reactor_upgrade_capacity"), E_R_CYCLE_DELAY("entropy_reactor_upgrade_cycle_delayer"),
		E_R_VARIETY("entropy_reactor_upgrade_variety"), E_R_ENTROPIC_HARNESSER("entropy_reactor_upgrade_entropic_harnesser"),

		GREENHOUSE_ARBORIST("greenhouse_upgrade_arborists_specialization"), GREENHOUSE_FLORIST("greenhouse_upgrade_florists_specialization"),
		GREENHOUSE_INTERDIM("greenhouse_upgrade_interdimensional_specialization"), GREENHOUSE_LAMP("greenhouse_upgrade_internal_lamp"),
		GREENHOUSE_BLACKOUT("greenhouse_upgrade_blackout_glass"),

		NONE(null);

		public final String name;

		Upgrades(String name){
			this.name = name;
		}

		public Item getItem() {
			return Registry.getItem(name);
		}

		public static Upgrades match(ItemStack i) {
			return match(i.getItem());
		}

		public static Upgrades match(Item i) {
			return Arrays.stream(values()).filter((u) -> u.name != null && u.name.equalsIgnoreCase(ForgeRegistries.ITEMS.getKey(i).getPath())).findFirst().orElse(Upgrades.NONE);
		}
	}
}
