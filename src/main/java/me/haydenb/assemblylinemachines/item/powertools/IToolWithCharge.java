package me.haydenb.assemblylinemachines.item.powertools;

import java.text.DecimalFormat;
import java.util.List;

import me.haydenb.assemblylinemachines.item.ItemAEFG;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolDefaults;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolDefaults.Stats;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public interface IToolWithCharge{

	default public int getMaxPower(ItemStack stack) {
		if(stack.isEnchanted()) {
			IToolWithCharge.EnchantmentOverclock enchantment = (IToolWithCharge.EnchantmentOverclock) Registry.OVERCLOCK.get();
			return Math.round(this.getPowerToolType().configMaxCharge * (1 + (stack.getEnchantmentLevel(enchantment) * enchantment.getMultiplier())));
		}
		return this.getPowerToolType().configMaxCharge;
	}

	default public ItemStack damageItem(ItemStack stack, int amount) {
		if (stack.hasTag()) {
			CompoundTag compound = stack.getTag();

			IToolWithCharge.PowerToolType ptt = this.getPowerToolType();
			if (compound.contains(ptt.keyName)) {

				int power = compound.getInt(ptt.keyName);
				if ((power - (amount * ptt.costMultiplier)) < 1) {
					compound.remove(ptt.keyName);
					compound.remove("assemblylinemachines:canbreakblackgranite");
					compound.remove("assemblylinemachines:secondarystyle");
				} else {
					compound.putInt(ptt.keyName, power - (amount * ptt.costMultiplier));
				}

				stack.setTag(compound);
				return stack;
			}
		}
		return null;
	}

	default public int addCharge(ItemStack stack, int amount, boolean simulated) {
		CompoundTag nbt = stack.getOrCreateTag();

		IToolWithCharge.PowerToolType ptt = this.getPowerToolType();
		int current = nbt.getInt(ptt.keyName);

		if(current + (amount * ptt.chargeMultiplier) > getMaxPower(stack)) {
			amount = getMaxPower(stack) - current;
			if(!simulated) nbt.putInt(ptt.keyName, getMaxPower(stack));
		}else {
			if(!simulated) nbt.putInt(ptt.keyName, current + (amount * ptt.chargeMultiplier));

		}
		if((simulated && current > 0) || (!simulated && current + (amount * ptt.chargeMultiplier) > 0)) {
			nbt.putBoolean("assemblylinemachines:canbreakblackgranite", true);
		}else {
			nbt.remove("assemblylinemachines:canbreakblackgranite");
		}

		stack.setTag(nbt);

		return amount;
	}

	public IToolWithCharge.PowerToolType getPowerToolType();

	default public float getActivePropertyState(ItemStack stack, LivingEntity entity) {
		return getPowerToolType().hasSecondaryAbilities && stack.hasTag() && stack.getTag().contains("assemblylinemachines:secondarystyle") ? 1f : 0f;
	}

	default public int getCurrentCharge(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().getInt(this.getPowerToolType().keyName) : 0;
	}

	default public boolean canUseSecondaryAbilities(ItemStack stack) {
		return this.getPowerToolType().hasSecondaryAbilities && stack.hasTag() && stack.getTag().contains(getPowerToolType().keyName)
				&& stack.getTag().contains("assemblylinemachines:secondarystyle");
	}

	default public void addEnergyInfoToHoverText(ItemStack stack, List<Component> tooltip) {
		DecimalFormat df = FormattingHelper.GENERAL_FORMAT;
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		IToolWithCharge.PowerToolType ptt = this.getPowerToolType();
		ChatFormatting colorChar = compound.getInt(ptt.keyName) == 0 ? ChatFormatting.RED : ChatFormatting.GREEN;
		tooltip.add(Component.literal(df.format(compound.getInt(ptt.keyName)) + "/" + df.format(this.getMaxPower(stack)) + " ").withStyle(colorChar).append(ptt.friendlyNameOfUnit));
		if(compound.getBoolean("assemblylinemachines:secondarystyle")) {
			tooltip.add(Component.literal("Secondary Ability Enabled").withStyle(ChatFormatting.AQUA));
		}
	}

	default public ICapabilityProvider defaultInitCapabilities(ItemStack stack, CompoundTag nbt) {
		if(this.getPowerToolType().hasEnergyCapability) {
			return new ICapabilityProvider() {

				protected IEnergyStorage energy = new IEnergyStorage() {

					@Override
					public int receiveEnergy(int maxReceive, boolean simulate) {

						return addCharge(stack, maxReceive, simulate);
					}
					@Override
					public int getMaxEnergyStored() {
						return getMaxPower(stack);
					}
					@Override
					public int getEnergyStored() {
						return getCurrentCharge(stack);
					}
					@Override
					public int extractEnergy(int maxExtract, boolean simulate) {
						return 0;
					}
					@Override
					public boolean canReceive() {
						return getEnergyStored() < getMaxEnergyStored();
					}
					
					@Override
					public boolean canExtract() {
						return false;
					}
				};
				protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);

				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
					return this.getCapability(cap);
				}
				@Override
				public <T> LazyOptional<T> getCapability(Capability<T> cap) {
					if (cap == ForgeCapabilities.ENERGY) {
						return energyHandler.cast();
					}
					return  LazyOptional.empty();
				}
			};
		}
		return null;
	}

	default public void defaultUse(Level world, Player player, InteractionHand hand){
		if (!world.isClientSide && player.isShiftKeyDown() && this.getPowerToolType().hasSecondaryAbilities) {

			ItemStack stack = player.getMainHandItem();

			CompoundTag nbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
			if(nbt.getInt(this.getPowerToolType().keyName) > 0) {
				if (nbt.contains("assemblylinemachines:secondarystyle")) {
					nbt.remove("assemblylinemachines:secondarystyle");
					player.displayClientMessage(Component.literal("Disabled Secondary Ability.").withStyle(ChatFormatting.RED), true);
				} else {
					nbt.putBoolean("assemblylinemachines:secondarystyle", true);
					player.displayClientMessage(Component.literal("Enabled Secondary Ability.").withStyle(ChatFormatting.AQUA), true);
				}
				stack.setTag(nbt);
			}
		}
	}

	public static enum PowerToolType{
		CRANK("assemblylinemachines:cranks", 1, 30, false, Component.literal("Cranks").withStyle(ChatFormatting.GOLD), false, null, 0.0f, ToolDefaults.CRANK.get(Stats.SP_ENERGY).intValue()),
		MYSTIUM("assemblylinemachines:fe", 150, 1, true, Component.literal("FE").withStyle(ChatFormatting.DARK_PURPLE), true, "mystium_farmland", 0.1f, ToolDefaults.MYSTIUM.get(Stats.SP_ENERGY).intValue()),
		NOVASTEEL("assemblylinemachines:fe", 75, 1, true, Component.literal("FE").withStyle(ChatFormatting.DARK_AQUA), true, "nova_farmland", 0.25f, ToolDefaults.NOVASTEEL.get(Stats.SP_ENERGY).intValue()),
		AEFG("assemblylinemachines:fe", 1, 1, false, Component.literal("FE").withStyle(ChatFormatting.BLUE), true, null, 0.0f, 10000000),
		ENHANCED_MYSTIUM("assemblylinemachines:fe", 150, 1, false, MYSTIUM.friendlyNameOfUnit, true, null, 0.0f, ToolDefaults.MYSTIUM.get(Stats.SP_ENH_ENERGY).intValue());
		
		public final String keyName;
		public final int costMultiplier;
		public final int chargeMultiplier;
		public final boolean hasSecondaryAbilities;
		public final MutableComponent friendlyNameOfUnit;
		public final boolean hasEnergyCapability;
		public final String nameOfSecondaryFarmland;
		public final int configMaxCharge;
		public final float chanceToDropMobCrystal;
		
		PowerToolType(String keyName, int costMultiplier, int chargeMultiplier, boolean hasSecondaryAbilities, MutableComponent friendlyNameOfUnit, 
				boolean hasEnergyCapability, String nameOfSecondaryFarmland, float chanceToDropMobCrystal, int configMaxCharge) {
			this.keyName = keyName;
			this.costMultiplier = costMultiplier;
			this.chargeMultiplier = chargeMultiplier;
			this.hasSecondaryAbilities = hasSecondaryAbilities;
			this.friendlyNameOfUnit = friendlyNameOfUnit;
			this.hasEnergyCapability = hasEnergyCapability;
			this.nameOfSecondaryFarmland = nameOfSecondaryFarmland;
			this.configMaxCharge = configMaxCharge;
			this.chanceToDropMobCrystal = chanceToDropMobCrystal;
			
		}
		
		public boolean needsActiveModel(Item item) {
			return (this.hasSecondaryAbilities && (item instanceof SwordItem || item instanceof PickaxeItem || item instanceof HoeItem || item instanceof AxeItem
					|| item instanceof ShovelItem)) || item instanceof ItemAEFG;
		}
	}

	public static class EnchantmentOverclock extends Enchantment{

		public static final EnchantmentCategory POWER_TOOLS = EnchantmentCategory.create("POWER_TOOLS", (item) -> item instanceof IToolWithCharge);
		private final Lazy<Float> multiplier;

		public EnchantmentOverclock() {
			super(Rarity.RARE, POWER_TOOLS, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
			this.multiplier = Lazy.of(() -> ALMConfig.getServerConfig().overclockMultiplier().get().floatValue());
		}

		@Override
		public int getMaxLevel() {
			return 10;
		}

		@Override
		public int getMinCost(int level) {
			return 5 + (level - 1) * 5;
		}

		@Override
		public int getMaxCost(int level) {
			return getMinCost(level) + 100;
		}

		public float getMultiplier() {
			return this.multiplier.get();
		}

	}
}