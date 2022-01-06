package me.haydenb.assemblylinemachines.item;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolTiers;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.IToolWithCharge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class ItemPowerTool<A extends TieredItem> extends TieredItem implements IToolWithCharge, ISpecialTooltip {

	private final A parent;
	private final PowerToolType ptt;
	private final String type;

	public ItemPowerTool(A parent) {
		super(parent.getTier(), new Item.Properties().tab(parent.getItemCategory()));
		this.ptt = PowerToolType.TIER_CONVERT.get(parent.getTier());
		this.parent = parent;
		this.type = parent.getClass().getSimpleName();
		
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		ItemStack resStack = damageItem(stack, amount);
		return resStack == null ? super.damageItem(stack, amount, entity, onBroken) : super.damageItem(resStack, 0, entity, onBroken);
	}
	
	@Override
	public PowerToolType getPowerToolType() {
		return ptt;
	}
	
	//Use A to mimic type action.
	@Override
	public String getToolType() {
		return type;
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { 
		switch(type) {
		case "SwordItem":
			if(enchantment.category == EnchantmentCategory.WEAPON) return true;
			break;
		case "PickaxeItem":
		case "AxeItem":
		case "HoeItem":
		case "ShovelItem":
			if(enchantment.category == EnchantmentCategory.DIGGER) return true;
		}
		return parent.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockIn) {
		return parent.isCorrectToolForDrops(blockIn);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return parent.getDestroySpeed(stack, state);
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
		return parent.canAttackBlock(state, worldIn, pos, player);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (canUseSecondaryAbilities(stack, "SwordItem")) {
			stack.hurtAndBreak(2, attacker, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
			return false;
		}
		return parent.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity player) {
		if(canUseSecondaryAbilities(stack, "PickaxeItem") || canUseSecondaryAbilities(stack, "ShovelItem")) {
			Direction d = Direction.orderedByNearest(player)[0];
			BiFunction<BlockState, BlockPos, Boolean> func;
			Iterator<BlockPos> iterator;
			if(type.equals("PickaxeItem")) {
				int i = ptt == PowerToolType.NOVASTEEL ? 2 : 1;
				BlockPos min = d == Direction.UP || d == Direction.DOWN ? pos.north(i).west(i) : pos.above(i).relative(d.getClockWise(), i);
				BlockPos max = d == Direction.UP || d == Direction.DOWN ? pos.south(i).east(i) : pos.below(i).relative(d.getCounterClockWise(), i);
				iterator = BlockPos.betweenClosedStream(min, max).iterator();
				func = ((statex, posx) -> statex.is(BlockTags.MINEABLE_WITH_PICKAXE) && statex.getDestroySpeed(world, posx) != -1f);
			}else {
				BlockPos min;
				BlockPos max;
				switch(ptt) {
				case NOVASTEEL:
					min = d == Direction.UP || d == Direction.DOWN ? pos.north().west() : pos.above().relative(d.getClockWise());
					max = d == Direction.UP || d == Direction.DOWN ? pos.south().east().relative(d, 5) : pos.below().relative(d.getCounterClockWise()).relative(d, 5);
					break;
				default:
					min = pos;
					max = pos.relative(d, 5);
				}
				iterator = BlockPos.betweenClosedStream(min, max).iterator();
				func = ((statex, posx) -> statex.is(BlockTags.MINEABLE_WITH_SHOVEL));
			}
			
			int cost = 0;
			while (iterator.hasNext()) {
				BlockPos posx = iterator.next();
				BlockState statex = world.getBlockState(posx);
				if (func.apply(statex, posx)) {
					cost = cost + 2;
					if(!world.isClientSide) {
						NonNullList<ItemStack> drops = NonNullList.create();
						for(ItemStack dropStack : statex.getDrops(new LootContext.Builder((ServerLevel) world).withParameter(LootContextParams.TOOL, stack).withParameter(LootContextParams.ORIGIN, new Vec3(posx.getX(), posx.getY(), posx.getZ())))) {
							drops.add(dropStack);
						}
						Containers.dropContents(world, posx, drops);
					}
					
					world.destroyBlock(posx, false);
				}
			}
			stack.hurtAndBreak(cost, player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
			return true;
			
		}else if(canUseSecondaryAbilities(stack, "AxeItem")) {
			
			BlockState bs = world.getBlockState(pos);

			if(bs.getBlock().getTags().contains(new ResourceLocation(AssemblyLineMachines.MODID, "world/mystium_axe_mineable"))) {
				int cmax = ptt == PowerToolType.NOVASTEEL ? 50 : 10;
				stack.hurtAndBreak(Utils.breakAndBreakConnected(world, bs, 0, cmax, pos, player), player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
			}
			return true;
		}
		return parent.mineBlock(stack, world, state, pos, player);
	}

	@Override
	public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pSlot) {
		return parent.getDefaultAttributeModifiers(pSlot);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		//1.17.1 Change: Hoe Secondary Ability is handled using a Mixin.
		return parent.useOn(pContext);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return parent.hasContainerItem(stack);
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return parent.getContainerItem(itemStack);
	}
	
	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return parent.canPerformAction(stack, toolAction);
	}
	
	// End Use A

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		if(ptt.hasEnergyCapability) {
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
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (!world.isClientSide && player.isShiftKeyDown() && ptt.hasSecondaryAbilities && !type.equals("ItemHammer")) {

			ItemStack stack = player.getMainHandItem();

			CompoundTag nbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
			if(nbt.getInt(ptt.keyName) > 0) {
				if (nbt.contains("assemblylinemachines:secondarystyle")) {
					nbt.remove("assemblylinemachines:secondarystyle");
					player.displayClientMessage(new TextComponent("Disabled Secondary Ability.").withStyle(ChatFormatting.RED), true);
				} else {
					nbt.putBoolean("assemblylinemachines:secondarystyle", true);
					player.displayClientMessage(new TextComponent("Enabled Secondary Ability.").withStyle(ChatFormatting.AQUA), true);
				}
				stack.setTag(nbt);
				return new InteractionResultHolder<ItemStack>(InteractionResult.CONSUME, stack);
			}
		}
		return super.use(world, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		this.addEnergyInfoToHoverText(stack, tooltip);
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
		return ptt == PowerToolType.MYSTIUM ? 0xffb81818 : ISpecialTooltip.super.getBottomColor();
	}
	
	public static class EnchantmentOverclock extends Enchantment{

		public static final EnchantmentCategory POWER_TOOLS = EnchantmentCategory.create("POWER_TOOLS", (item) -> item instanceof IToolWithCharge);
		private final float multiplier;
		
		public EnchantmentOverclock() {
			super(Rarity.RARE, POWER_TOOLS, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
			this.multiplier = ConfigHolder.getServerConfig().overclockEnchantmentMultiplier.get().floatValue();
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
	
	public static enum PowerToolType{
		CRANK("assemblylinemachines:cranks", 1, 30, false, "§6Cranks", false, null, 0.0f, ConfigHolder.getServerConfig().crankToolMaxCranks.get(), 0xff994a09, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/crank.png")),
		MYSTIUM("assemblylinemachines:fe", 150, 1, true, "§5FE", true, "mystium_farmland", 0.1f, ConfigHolder.getServerConfig().mystiumToolMaxFE.get(), 0xff2546cc, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/mystium.png")),
		NOVASTEEL("assemblylinemachines:fe", 75, 1, true, "§3FE", true, "nova_farmland", 0.25f, ConfigHolder.getServerConfig().novasteelToolMaxFE.get(), 0xff5d0082, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/tooltip/novasteel.png")),
		AEFG("assemblylinemachines:fe", 1, 1, false, "§9FE", true, null, 0.0f, 10000000, 0x0, null);
		
		private final String keyName;
		private final int costMultiplier;
		private final int chargeMultiplier;
		private final boolean hasSecondaryAbilities;
		private final String friendlyNameOfUnit;
		private final boolean hasEnergyCapability;
		private final String nameOfSecondaryFarmland;
		private final int configMaxCharge;
		private final float chanceToDropMobCrystal;
		private final int argbBorderColor;
		private final ResourceLocation borderTexturePath;
		
		private static final HashMap<Tier, PowerToolType> TIER_CONVERT = new HashMap<>();
		static {
			TIER_CONVERT.put(ToolTiers.CRANK, CRANK);
			TIER_CONVERT.put(ToolTiers.MYSTIUM, MYSTIUM);
			TIER_CONVERT.put(ToolTiers.NOVASTEEL, NOVASTEEL);
		}
		
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
		
		public String getNameOfSecondaryFarmland() {
			return nameOfSecondaryFarmland;
		}
		
		public float getChanceToDropMobCrystal() {
			return chanceToDropMobCrystal;
		}
		
		public int getMaxCharge() {
			return configMaxCharge;
		}
		
		public String getKeyName() {
			return keyName;
		}
		
		public boolean getHasSecondaryAbilities() {
			return hasSecondaryAbilities;
		}
		
		public int getCostMultiplier() {
			return costMultiplier;
		}
		
		public int getChargeMultiplier() {
			return chargeMultiplier;
		}
		
		public boolean getHasEnergyCapability() {
			return hasEnergyCapability;
		}
		
		public String getFriendlyNameOfUnit() {
			return friendlyNameOfUnit;
		}
		
		public int getARGBBorderColor() {
			return argbBorderColor;
		}
		
		public ResourceLocation getBorderTexturePath() {
			return borderTexturePath;
		}
	}
	
	//Gives the Charged Mob Crystal when killed with a charged Secondary Ability Sword.
	@SubscribeEvent
	public static void kill(LivingDeathEvent event) {
		if(event.getSource().getEntity() instanceof ServerPlayer) {
			ServerPlayer spe = (ServerPlayer) event.getSource().getEntity();
			ItemStack stack = spe.getMainHandItem();
			
			if(ItemMobCrystal.MOB_COLORS.get(event.getEntity().getType()) != null && stack.getItem() instanceof IToolWithCharge) {
				IToolWithCharge chargeTool = (IToolWithCharge) stack.getItem();
				if(spe.getLevel().getRandom().nextFloat() <= chargeTool.getPowerToolType().getChanceToDropMobCrystal() && chargeTool.canUseSecondaryAbilities(stack, "SwordItem")) {
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

	@Override
	public float getActivePropertyState(ItemStack stack, LivingEntity entity) {
		return ptt.hasSecondaryAbilities && stack.hasTag() && stack.getTag().contains("assemblylinemachines:secondarystyle") ? 1f : 0f;
	}
}
