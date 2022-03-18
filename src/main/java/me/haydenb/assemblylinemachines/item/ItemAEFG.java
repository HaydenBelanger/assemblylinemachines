package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemAEFG extends Item implements IToolWithCharge, ISpecialTooltip {

	public ItemAEFG() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB).stacksTo(1));
	}

	@Override
	public IToolWithCharge.PowerToolType getPowerToolType() {
		return IToolWithCharge.PowerToolType.AEFG;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return this.defaultInitCapabilities(stack, nbt);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(this.getPowerToolType().keyName);
		float v = (float) dmg / (float) getMaxPower(stack);
		return ARGB32.color(255, Math.round(v * 255f), Math.round(v * 255f), 255);
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(this.getPowerToolType().keyName);
		return Math.round(((float)dmg/ (float) getMaxPower(stack)) * 13.0f);
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
		this.addEnergyInfoToHoverText(pStack, pTooltipComponents);
		
		pTooltipComponents.add(new TextComponent("Anti-Entropy Field Generator").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		pTooltipComponents.add(new TextComponent("Protects against chaotic effects when charged.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
	
	@Override
	public boolean isEnchantable(ItemStack pStack) {
		return true;
	}
	
	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 30;
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
