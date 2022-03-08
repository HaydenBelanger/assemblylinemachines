package me.haydenb.assemblylinemachines.item.powertools;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public interface IToolWithCharge{

	default public int getMaxPower(ItemStack stack) {
		if(stack.isEnchanted()) {
			IToolWithCharge.EnchantmentOverclock enchantment = (IToolWithCharge.EnchantmentOverclock) Registry.getEnchantment("overclock");
			return Math.round(this.getPowerToolType().configMaxCharge * (1 + (EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) * enchantment.getMultiplier())));
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
		CompoundTag nbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
		
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
		DecimalFormat df = Formatting.GENERAL_FORMAT;
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		IToolWithCharge.PowerToolType ptt = this.getPowerToolType();
		String colorChar = compound.getInt(ptt.keyName) == 0 ? "c" : "a";
		tooltip.add(new TextComponent("§" + colorChar + df.format(compound.getInt(ptt.keyName)) + "/" + df.format(this.getMaxPower(stack)) + " " + ptt.friendlyNameOfUnit));
		if(compound.getBoolean("assemblylinemachines:secondarystyle")) {
			tooltip.add(new TextComponent("§bSecondary Ability Enabled"));
		}
	}
	
	default public ICapabilityProvider defaultInitCapabilities(ItemStack stack, CompoundTag nbt) {
		if(this.getPowerToolType().hasSecondaryAbilities) {
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
						return true;
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
					if (cap == CapabilityEnergy.ENERGY) {
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
					player.displayClientMessage(new TextComponent("Disabled Secondary Ability.").withStyle(ChatFormatting.RED), true);
				} else {
					nbt.putBoolean("assemblylinemachines:secondarystyle", true);
					player.displayClientMessage(new TextComponent("Enabled Secondary Ability.").withStyle(ChatFormatting.AQUA), true);
				}
				stack.setTag(nbt);
			}
		}
	}
	
	public static enum PowerToolType{
		CRANK("assemblylinemachines:cranks", 1, 30, false, "§6Cranks", false, null, 0.0f, ConfigHolder.getCommonConfig().crankToolMaxCranks.get(), 0xff994a09, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/crank.png")),
		MYSTIUM("assemblylinemachines:fe", 150, 1, true, "§5FE", true, "mystium_farmland", 0.1f, ConfigHolder.getCommonConfig().mystiumMaxFE.get(), 0xff2546cc, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/mystium.png")),
		NOVASTEEL("assemblylinemachines:fe", 75, 1, true, "§3FE", true, "nova_farmland", 0.25f, ConfigHolder.getCommonConfig().novasteelToolMaxFE.get(), 0xff5d0082, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/novasteel.png")),
		AEFG("assemblylinemachines:fe", 1, 1, false, "§9FE", true, null, 0.0f, 10000000, 0x0, null),
		ENHANCED_MYSTIUM("assemblylinemachines:fe", 150, 1, false, "§5FE", true, null, 0.0f, ConfigHolder.getCommonConfig().enhancedMystiumChestplateMaxFE.get(), 0xff2546cc, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/mystium.png"));
		
		public final String keyName;
		public final int costMultiplier;
		public final int chargeMultiplier;
		public final boolean hasSecondaryAbilities;
		public final String friendlyNameOfUnit;
		public final boolean hasEnergyCapability;
		public final String nameOfSecondaryFarmland;
		public final int configMaxCharge;
		public final float chanceToDropMobCrystal;
		public final int argbBorderColor;
		public final ResourceLocation borderTexturePath;
		
		PowerToolType(String keyName, int costMultiplier, int chargeMultiplier, boolean hasSecondaryAbilities, String friendlyNameOfUnit, 
				boolean hasEnergyCapability, String nameOfSecondaryFarmland, float chanceToDropMobCrystal, int configMaxCharge, int argbBorderColor, ResourceLocation borderTexturePath) {
			this.keyName = keyName;
			this.costMultiplier = costMultiplier;
			this.chargeMultiplier = chargeMultiplier;
			this.hasSecondaryAbilities = hasSecondaryAbilities;
			this.friendlyNameOfUnit = friendlyNameOfUnit;
			this.hasEnergyCapability = hasEnergyCapability;
			this.nameOfSecondaryFarmland = nameOfSecondaryFarmland;
			this.configMaxCharge = configMaxCharge;
			this.chanceToDropMobCrystal = chanceToDropMobCrystal;
			this.argbBorderColor = argbBorderColor;
			this.borderTexturePath = borderTexturePath;
			
		}
		
		public Optional<Integer> getBottomARGBBorderColor() {
			return this == PowerToolType.MYSTIUM ? Optional.of(0xffb81818) : Optional.empty();
		}
	}

	public static class EnchantmentOverclock extends Enchantment{
	
		public static final EnchantmentCategory POWER_TOOLS = EnchantmentCategory.create("POWER_TOOLS", (item) -> item instanceof IToolWithCharge);
		private final float multiplier;
		
		public EnchantmentOverclock() {
			super(Rarity.RARE, POWER_TOOLS, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
			this.multiplier = ConfigHolder.getCommonConfig().overclockEnchantmentMultiplier.get().floatValue();
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
			return this.multiplier;
		}
		
	}
}