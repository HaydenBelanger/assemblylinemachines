package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import me.haydenb.assemblylinemachines.item.ItemPowerTool.PowerToolType;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils.IToolWithCharge;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemAEFG extends Item implements IToolWithCharge, ISpecialTooltip {

	public ItemAEFG() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB).stacksTo(1));
	}

	@Override
	public PowerToolType getPowerToolType() {
		return PowerToolType.AEFG;
	}

	@Override
	public String getToolType() {
		return "Item";
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		if(this.getPowerToolType().getHasEnergyCapability()) {
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
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(this.getPowerToolType().getKeyName());
		float v = (float) dmg / (float) getMaxPower(stack);
		return ARGB32.color(255, Math.round(v * 255f), Math.round(v * 255f), 255);
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(this.getPowerToolType().getKeyName());
		return Math.round(((float)dmg/ (float) getMaxPower(stack)) * 13.0f);
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		this.addEnergyInfoToHoverText(pStack, pTooltipComponents);
		
		pTooltipComponents.add(new TextComponent("§7§oAnti-Entropy Field Generator"));
		pTooltipComponents.add(new TextComponent("§8§oProtects against chaotic effects when charged."));
	}
	
	@Override
	public boolean isEnchantable(ItemStack pStack) {
		return true;
	}

	@Override
	public float getActivePropertyState(ItemStack stack, LivingEntity entity) {
		return getCurrentCharge(stack) > 0 && (entity.hasEffect(Registry.getEffect("entropy_poisoning")) || entity.hasEffect(Registry.getEffect("dark_expulsion"))) ? 1f : 0f;
	}
	
	@Override
	public ResourceLocation getTexture() {
		return null;
	}
	
	@Override
	public int getTopColor() {
		return 0xffe3e3e3;
	}
	
	@Override
	public int getBottomColor() {
		return 0xff545454;
	}
}
