package me.haydenb.assemblylinemachines.item;

import java.util.List;
import java.util.UUID;

import com.google.common.cache.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.block.pipes.BlockPipe;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWrenchOMatic extends Item {

	private static final UUID KNOCKBACK_UUID = UUID.fromString("8D9D86FB-0503-4094-A07A-8F8D39D3C434");

	private static final LoadingCache<Integer, Multimap<Attribute, AttributeModifier>> WRATH_MODE_ATTRIBUTE_CACHE = CacheBuilder.newBuilder().build(CacheLoader.from((i) -> {
		Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();

		attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Damage", ALMConfig.getServerConfig().engineersFuryMultiplier().get(), Operation.ADDITION));
		attributes.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Attack Speed", -0.8d, Operation.ADDITION));
		attributes.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(KNOCKBACK_UUID, "Knockback", Math.min(5d, 0.5d + (i * ALMConfig.getServerConfig().engineersFuryMultiplier().get())), Operation.ADDITION));
		return attributes;
	}));

	public ItemWrenchOMatic() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		if(!pLevel.isClientSide && pPlayer.isCrouching() && pUsedHand == InteractionHand.MAIN_HAND){
			CompoundTag compound = pPlayer.getMainHandItem().getOrCreateTag();
			int newMode = switch(compound.getInt("assemblylinemachines:wrenchmode")) {
			case 0 -> 1;
			case 1 -> 2;
			default -> 0;
			};

			pPlayer.displayClientMessage(Component.literal(switch(newMode) {
			case 1 -> "Switched to Interact Mode.";
			case 2 -> "Switched to Wrath Mode.";
			default -> "Switched to Wrench Mode.";
			}), true);

			compound.putInt("assemblylinemachines:wrenchmode", newMode);
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		if(!pContext.getLevel().isClientSide() && pContext.getHand() == InteractionHand.MAIN_HAND) {
			switch(pContext.getItemInHand().getOrCreateTag().getInt("assemblylinemachines:wrenchmode")) {
			//WRENCH
			case 0 -> {
				BlockState state = pContext.getLevel().getBlockState(pContext.getClickedPos());
				if(state.getBlock() instanceof BlockPipe pipe) {
					pContext.getLevel().setBlockAndUpdate(pContext.getClickedPos(), pipe.updateConnections(state, Direction.values(), pContext.getLevel(), pContext.getClickedPos(), pContext.getPlayer(), pContext.getClickedFace().getOpposite()));
				}
			}
			//INTERACT
			case 1 -> {
				BlockState state = pContext.getLevel().getBlockState(pContext.getClickedPos());
				if(state.hasProperty(HorizontalDirectionalBlock.FACING)) {
					Direction dir = state.getValue(HorizontalDirectionalBlock.FACING);
					dir = pContext.getPlayer().isCrouching() ? dir.getCounterClockWise() : dir.getClockWise();
					pContext.getLevel().setBlockAndUpdate(pContext.getClickedPos(), state.setValue(HorizontalDirectionalBlock.FACING, dir));
				}
			}
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
			TooltipFlag pIsAdvanced) {
		pTooltipComponents.addAll(switch(pStack.getOrCreateTag().getInt("assemblylinemachines:wrenchmode")) {
		//Interact
		case 1 -> List.of(Component.literal("Interact Mode:").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC), Component.literal("- Can rotate machines and other blocks.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		//Wrath
		case 2 -> List.of(Component.literal("Wrath Mode:").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), Component.literal("- Deals additional damage and knockback.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
				Component.literal("- Can be enchanted with Engineer's Fury.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		//Wrench
		default -> List.of(Component.literal("Wrench Mode:").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), Component.literal("- Can reconfigure connections on Pipes.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		});
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		if(slot == EquipmentSlot.MAINHAND && stack.getOrCreateTag().getInt("assemblylinemachines:wrenchmode") == 2) {
			return WRATH_MODE_ATTRIBUTE_CACHE.getUnchecked(stack.getEnchantmentLevel(Registry.ENGINEERS_FURY.get()));
		}
		return super.getAttributeModifiers(slot, stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getEnchantmentValue(ItemStack stack) {
		return 30;
	}

	public static class EnchantmentEngineersFury extends Enchantment{

		public static final EnchantmentCategory WRENCH_O_MATIC = EnchantmentCategory.create("WRENCH_O_MATIC", (item) -> item instanceof ItemWrenchOMatic);

		public EnchantmentEngineersFury() {
			super(Rarity.COMMON, WRENCH_O_MATIC, new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
		}

		@Override
		public int getMaxLevel() {
			return 10;
		}

		@Override
		public int getMinCost(int level) {
			return 10 + (level - 1) * 10;
		}

		@Override
		public int getMaxCost(int level) {
			return getMinCost(level) + 100;
		}

		@Override
		public float getDamageBonus(int pLevel, MobType pCreatureType) {
			return 1.0f + Math.max(0, pLevel - 1) * 0.25f;
		}
	}
}
