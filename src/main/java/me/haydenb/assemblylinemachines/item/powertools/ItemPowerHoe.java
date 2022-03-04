package me.haydenb.assemblylinemachines.item.powertools;

import java.util.List;
import java.util.function.Consumer;

import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemPowerHoe extends HoeItem implements IToolWithCharge, ISpecialTooltip {

	private final IToolWithCharge.PowerToolType ptt;
	
	public ItemPowerHoe(IToolWithCharge.PowerToolType ptt, Properties properties) {
		super(ptt.getTier(), 0, -0.5f, properties);
		this.ptt = ptt;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		ItemStack resStack = damageItem(stack, amount);
		return resStack == null ? super.damageItem(stack, amount, entity, onBroken) : super.damageItem(resStack, 0, entity, onBroken);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return this.defaultInitCapabilities(stack, nbt);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
		this.defaultUse(p_41432_, p_41433_, p_41434_);
		return super.use(p_41432_, p_41433_, p_41434_);
	}
	
	@Override
	public void appendHoverText(ItemStack p_41421_, Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
		super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
		this.addEnergyInfoToHoverText(p_41421_, p_41423_);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		if(!stack.hasTag() || stack.getTag().getInt(ptt.getKeyName()) == 0) return super.isBarVisible(stack);
		return stack.getTag().getInt(ptt.getKeyName()) != this.getMaxPower(stack);
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(ptt.getKeyName());
		if(dmg == 0) {
			return super.getBarColor(stack);
		}else {
			float v = (float) dmg / (float) getMaxPower(stack);
			return ARGB32.color(255, Math.round(v * 255f), Math.round(v * 255f), 255);
		}
		
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(ptt.getKeyName());
		return dmg == 0 ? super.getBarWidth(stack) : Math.round(((float)dmg/ (float) getMaxPower(stack)) * 13.0f);
	}
	
	@Override
	public ResourceLocation getTexture() {
		return ptt.getBorderTexturePath();
	}

	@Override
	public int getTopColor() {
		return ptt.getARGBBorderColor();
	}
	
	@Override
	public int getBottomColor() {
		return ptt.getBottomARGBBorderColor().orElse(ISpecialTooltip.super.getBottomColor());
	}

	@Override
	public IToolWithCharge.PowerToolType getPowerToolType() {
		return ptt;
	}
}
