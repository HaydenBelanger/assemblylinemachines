package me.haydenb.assemblylinemachines.item.powertools;

import java.util.List;
import java.util.function.Consumer;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import me.haydenb.assemblylinemachines.item.ItemMobCrystal;
import me.haydenb.assemblylinemachines.item.ItemTiers;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class ItemPowerSword extends SwordItem implements IToolWithCharge, ISpecialTooltip {

	private final IToolWithCharge.PowerToolType ptt;
	
	public ItemPowerSword(ItemTiers ptt, Properties properties) {
		super(ptt.getItemTier(), 2, -1.5f, properties);
		this.ptt = ptt.getPowerToolType();
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		ItemStack resStack = damageItem(stack, amount);
		return resStack == null ? super.damageItem(stack, amount, entity, onBroken) : super.damageItem(resStack, 0, entity, onBroken);
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (canUseSecondaryAbilities(stack)) {
			stack.hurtAndBreak(2, attacker, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
			return false;
		}
		return super.hurtEnemy(stack, target, attacker);
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
		if(!stack.hasTag() || stack.getTag().getInt(ptt.keyName) == 0) return super.isBarVisible(stack);
		return stack.getTag().getInt(ptt.keyName) != this.getMaxPower(stack);
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(ptt.keyName);
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
		int dmg = compound.getInt(ptt.keyName);
		return dmg == 0 ? super.getBarWidth(stack) : Math.round(((float)dmg/ (float) getMaxPower(stack)) * 13.0f);
	}
	
	@Override
	public ResourceLocation getTexture() {
		return ptt.borderTexturePath;
	}

	@Override
	public int getTopColor() {
		return ptt.argbBorderColor;
	}
	
	@Override
	public int getBottomColor() {
		return ptt.getBottomARGBBorderColor().orElse(ISpecialTooltip.super.getBottomColor());
	}

	@Override
	public IToolWithCharge.PowerToolType getPowerToolType() {
		return ptt;
	}
	
	@SubscribeEvent
	public static void kill(LivingDeathEvent event) {
		if(event.getSource().getEntity() instanceof ServerPlayer) {
			ServerPlayer spe = (ServerPlayer) event.getSource().getEntity();
			ItemStack stack = spe.getMainHandItem();
			
			if(ItemMobCrystal.MOB_COLORS.get(event.getEntity().getType()) != null && stack.getItem() instanceof ItemPowerSword chargeTool) {
				if(spe.getLevel().getRandom().nextFloat() <= chargeTool.getPowerToolType().chanceToDropMobCrystal && chargeTool.canUseSecondaryAbilities(stack)) {
					ItemStack inert = null;
					for(int i = 0; i < spe.getInventory().getContainerSize(); i++) {
						ItemStack sis = spe.getInventory().getItem(i);
						if(sis.getItem() == Registry.getItem("mob_crystal") && !sis.hasTag()) {
							inert = sis;
							break;
						}
					}
					if(spe.isCreative() || inert != null) {
						ItemStack crystal = new ItemStack(Registry.getItem("mob_crystal"), 1);
						CompoundTag tag = new CompoundTag();
						tag.putString("assemblylinemachines:mob", event.getEntity().getType().getRegistryName().toString());
						crystal.setTag(tag);
						event.getEntity().spawnAtLocation(crystal);
						stack.hurtAndBreak(20, spe, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
						if(!spe.isCreative() && inert != null) {
							inert.shrink(1);
						}
					}
				}
			}
		}
	}
}
