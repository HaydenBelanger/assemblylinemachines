package me.haydenb.assemblylinemachines.item;

import java.util.*;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import me.haydenb.assemblylinemachines.client.armor.ArmorData;
import me.haydenb.assemblylinemachines.client.armor.ArmorModel;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class ItemPowerArmor extends ArmorItem implements IToolWithCharge, ISpecialTooltip{

	private final PowerToolType ptt;

	private static final ArrayList<UUID> FLYING = new ArrayList<>();

	public ItemPowerArmor(ArmorMaterial pMaterial, EquipmentSlot pSlot, Properties pProperties) {
		this(ItemTiers.getTier(pMaterial).getPowerToolType(), pMaterial, pSlot, pProperties);
	}

	public ItemPowerArmor(PowerToolType ptt, ArmorMaterial pMaterial, EquipmentSlot pSlot, Properties pProperties) {
		super(pMaterial, pSlot, pProperties);
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
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
			TooltipFlag pIsAdvanced) {
		this.addEnergyInfoToHoverText(pStack, pTooltipComponents);
		if(ptt == PowerToolType.ENHANCED_MYSTIUM) pTooltipComponents.add(Component.literal("Enables creative flight at the cost of power.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		return ptt == PowerToolType.ENHANCED_MYSTIUM ? true : super.isFoil(pStack);
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
	public PowerToolType getPowerToolType() {
		return ptt;
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
		return ptt == PowerToolType.MYSTIUM || ptt == PowerToolType.ENHANCED_MYSTIUM ? 0xffb81818 : ISpecialTooltip.super.getBottomColor();
	}

	@Override
	public boolean canUseSecondaryAbilities(ItemStack stack) {
		return false;
	}

	@Override
	public float getActivePropertyState(ItemStack stack, LivingEntity entity) {
		return 0f;
	}
	
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private Optional<ArmorModel> model = null;

			@Override
			public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
					EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if(model == null) model = ArmorData.get(ptt.toString().toLowerCase(), equipmentSlot);
				return model.isPresent() ? model.get() : original;
			}
		});
	}

	@Override
	public void onArmorTick(ItemStack stack, Level world, Player player) {
		if(!world.isClientSide) {
			if(stack.getItem().equals(Registry.getItem("enhanced_mystium_chestplate"))) {

				CompoundTag tag = stack.getOrCreateTag();
				if(tag.getInt(ptt.keyName) > 0) {
					if(!FLYING.contains(player.getUUID())) {
						FLYING.add(player.getUUID());
						player.getAbilities().mayfly = true;
						player.onUpdateAbilities();
					}
					if(player.getAbilities().flying) {
						int flightTimer = tag.getInt("assemblylinemachines:flight_timer");
						if(flightTimer <= 0) {
							stack.hurtAndBreak(1, player, (br) -> br.broadcastBreakEvent(EquipmentSlot.MAINHAND));
							tag.putInt("assemblylinemachines:flight_timer", 5);
						}else {
							tag.putInt("assemblylinemachines:flight_timer", flightTimer - 1);
						}
					}
				}else {
					FlightManagementEvents.remove(player, world);
				}

			}
		}

		super.onArmorTick(stack, world, player);
	}

	@SubscribeEvent
	public static void cancelFlight(PlayerTickEvent event) {
		if(!event.player.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Registry.getItem("enhanced_mystium_chestplate"))) {
			FlightManagementEvents.remove(event.player, event.player.level);
		}
	}

	@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.FORGE)
	public static class FlightManagementEvents{

		
		@SubscribeEvent
		public static void clearOnQuit(EntityLeaveLevelEvent event) {
			remove(event.getEntity(), event.getLevel());
		}

		@SubscribeEvent
		public static void clearOnJoin(EntityJoinLevelEvent event) {
			remove(event.getEntity(), event.getLevel());
		}

		private static void remove(Entity entity, Level world) {
			if(!world.isClientSide() && entity instanceof Player player && FLYING.contains(player.getUUID())) {
				FLYING.remove(player.getUUID());
				if(!player.isCreative()) {
					player.getAbilities().flying = false;
					player.getAbilities().mayfly = false;
					player.onUpdateAbilities();
				}

			}
		}
	}

}
