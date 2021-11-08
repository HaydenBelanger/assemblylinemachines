package me.haydenb.assemblylinemachines.item.categories;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import me.haydenb.assemblylinemachines.registry.Utils.IPoweredTool;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

@SuppressWarnings("deprecation")
public class ItemMystiumTool<A extends TieredItem> extends TieredItem implements IPoweredTool {

	private final A parent;
	private final int maxPower;

	public ItemMystiumTool(int maxPower, A parent) {
		super(parent.getTier(), new Item.Properties().tab(parent.getItemCategory()));
		this.parent = parent;
		this.maxPower = maxPower;

	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (stack.hasTag()) {
			CompoundTag compound = stack.getTag();

			if (compound.contains("assemblylinemachines:fe")) {

				int power = compound.getInt("assemblylinemachines:fe");
				if ((power - (amount * 150)) < 1) {
					compound.remove("assemblylinemachines:fe");
					compound.remove("assemblylinemachines:canbreakblackgranite");
				} else {
					compound.putInt("assemblylinemachines:fe", power - (amount * 150));
				}

				stack.setTag(compound);
				return super.damageItem(stack, 0, entity, onBroken);
			}
		}

		return super.damageItem(stack, amount, entity, onBroken);
	}

	//Called from Registry
	@OnlyIn(Dist.CLIENT)
	public void connectItemProperties() {
		ItemProperties.register(this, new ResourceLocation(AssemblyLineMachines.MODID, "active"), new ItemPropertyFunction() {

			@Override
			public float call(ItemStack stack, ClientLevel p_174677_, LivingEntity p_174678_, int p_174679_) {
				if(stack.hasTag()) {
					CompoundTag compound = stack.getTag();
					if(compound.contains("assemblylinemachines:secondarystyle") && compound.contains("assemblylinemachines:fe") && compound.getInt("assemblylinemachines:fe") > 0) {
						return 1f;
					}
				}

				return 0f;
			}


		});
	}

	// Use A (Sword, Pickaxe, Hoe, Shovel, Axe)
	@Override
	public boolean isCorrectToolForDrops(BlockState blockIn) {
		return parent.isCorrectToolForDrops(blockIn);
	}


	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return parent.getDestroySpeed(stack, state);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

		return parent.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
		return parent.canAttackBlock(state, worldIn, pos, player);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();

			if (item == Registry.getItem("mystium_sword")) {

				stack.hurtAndBreak(2, attacker, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
				return false;

			}
		}

		return parent.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity player) {

		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();

			if (item == Registry.getItem("mystium_pickaxe")) {
				Direction d = Direction.orderedByNearest(player)[0];

				Iterator<BlockPos> iter;
				if (d == Direction.UP || d == Direction.DOWN) {

					iter = BlockPos.betweenClosedStream(pos.north().west(), pos.south().east()).iterator();
				} else {
					iter = BlockPos.betweenClosedStream(pos.above().relative(d.getClockWise()), pos.below().relative(d.getCounterClockWise())).iterator();
				}

				int cost = 0;

				ServerLevel sw = null;
				if(!world.isClientSide) {
					sw = world.getServer().getLevel(world.dimension());
				}
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					BlockState bs = world.getBlockState(posx);
					if (bs.getBlock() != Blocks.AIR && bs.getDestroySpeed(world, posx) != -1f) {
						cost = cost + 2;
						world.destroyBlock(posx, false, player);

						if(!world.isClientSide) {

							performDrops(bs, posx, player, sw);
						}
					}

				}

				stack.hurtAndBreak(cost, player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
				return true;

			} else if (item == Registry.getItem("mystium_axe")) {


				BlockState bs = world.getBlockState(pos);

				if(bs.getBlock().getTags().contains(new ResourceLocation(AssemblyLineMachines.MODID, "world/mystium_axe_mineable"))) {
					stack.hurtAndBreak(breakAndBreakConnected(world, bs, 0, pos, player), player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
				}
				return true;

			} else if (item == Registry.getItem("mystium_shovel")) {
				Direction d = Direction.orderedByNearest(player)[0];

				Iterator<BlockPos> iter = BlockPos.betweenClosedStream(pos, pos.relative(d, 5)).iterator();

				int cost = 0;
				ServerLevel sw = null;
				if(!world.isClientSide) {
					sw = world.getServer().getLevel(world.dimension());
				}
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					BlockState bs = world.getBlockState(posx);
					if (bs.getBlock() != Blocks.AIR && (bs.getMaterial() == Material.DIRT || bs.getMaterial() == Material.CLAY || bs.getMaterial() == Material.SNOW || bs.getMaterial() == Material.TOP_SNOW|| bs.getMaterial() == Material.SAND)) {
						cost = cost + 2;
						world.destroyBlock(posx, false, player);

						if(!world.isClientSide) {
							performDrops(bs, posx, player, sw);
						}
					}

				}

				stack.hurtAndBreak(cost, player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
				return true;
			}
		}
		return parent.mineBlock(stack, world, state, pos, player);
	}

	private int breakAndBreakConnected(Level world, BlockState origState, int ctx, BlockPos posx, LivingEntity player) {
		world.destroyBlock(posx, true, player);

		int cost = 2;
		if(ctx <= 20) {
			Iterator<BlockPos> iter = BlockPos.betweenClosedStream(posx.below().north().west(), posx.above().south().east()).iterator();

			while(iter.hasNext()) {
				BlockPos posq = iter.next();

				BlockState bs = world.getBlockState(posq);
				if(bs.getBlock() == origState.getBlock()) {
					cost = cost + breakAndBreakConnected(world, origState, ctx++, posq, player);
				}
			}
		}

		return cost;
	}

	private void performDrops(BlockState bs, BlockPos pos, LivingEntity player, ServerLevel sw) {
		List<ItemStack> drops = bs.getDrops(new LootContext.Builder(sw).withParameter(LootContextParams.TOOL, player.getMainHandItem()).withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ())));
		Containers.dropContents(sw.getLevel(), pos, NonNullList.of(ItemStack.EMPTY, drops.toArray(new ItemStack[drops.size()])));
	}

	@Override
	public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pSlot) {
		return parent.getDefaultAttributeModifiers(pSlot);
	}


	@Override
	public InteractionResult useOn(UseOnContext pContext) {

		ItemStack stack = pContext.getItemInHand();
		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();


			if(item == Registry.getItem("mystium_hoe")) {

				Level level = pContext.getLevel();
				BlockPos blockpos = pContext.getClickedPos();
				Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItem.TILLABLES.get(level.getBlockState(blockpos).getBlock());
				int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(pContext);
				if (hook != 0) return hook > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
				if (pContext.getClickedFace() != Direction.DOWN && level.isEmptyBlock(blockpos.above())) {
					if (pair == null) {
						return InteractionResult.PASS;
					} else {
						Predicate<UseOnContext> predicate = pair.getFirst();
						if (predicate.test(pContext)) {
							Player player = pContext.getPlayer();
							level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
							if (!level.isClientSide) {
								level.setBlock(blockpos, Registry.getBlock("mystium_farmland").defaultBlockState(), 11);
								if (player != null) {
									pContext.getItemInHand().hurtAndBreak(15, player, (p_150845_) -> {
										p_150845_.broadcastBreakEvent(pContext.getHand());
									});
								}
							}

							return InteractionResult.sidedSuccess(level.isClientSide);
						} else {
							return InteractionResult.PASS;
						}
					}
				}

				return InteractionResult.PASS;

			}
		}

		return parent.useOn(pContext);
	}

	@Override
	public Component getName(ItemStack stack) {
		return super.getName(stack).copy().withStyle(ChatFormatting.DARK_PURPLE);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return parent.hasContainerItem(stack);
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return parent.getContainerItem(itemStack);
	}

	// End Use A

	@Override
	public int getMaxPower() {
		return maxPower;
	}

	private int getCurrentPower(ItemStack stack) {

		if(stack.hasTag()) {

			if(stack.getTag().contains("assemblylinemachines:fe")) {
				return stack.getTag().getInt("assemblylinemachines:fe");
			}else {
				return 0;
			}
		}else {
			return 0;
		}
	}

	private void setCurrentPower(ItemStack stack, int amt) {

		CompoundTag nbt;
		if(stack.hasTag()) {
			nbt = stack.getTag();
		}else {
			nbt = new CompoundTag();
		}
		if(amt <= 0) {
			nbt.remove("assemblylinemachines:fe");
		}else {
			if(amt > getMaxPower()) {
				nbt.putInt("assemblylinemachines:fe", getMaxPower());
			}else {
				nbt.putInt("assemblylinemachines:fe", amt);
			}

		}

		if(amt != 0) {
			nbt.putBoolean("assemblylinemachines:canbreakblackgranite", true);
		}else {
			nbt.remove("assemblylinemachines:canbreakblackgranite");
		}

		stack.setTag(nbt);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new ICapabilityProvider() {

			protected IEnergyStorage energy = new IEnergyStorage() {

				@Override
				public int receiveEnergy(int maxReceive, boolean simulate) {

					if (getMaxPower() < maxReceive + getCurrentPower(stack)) {
						maxReceive = getMaxPower() - getCurrentPower(stack);
					}

					if (simulate == false) {
						setCurrentPower(stack, getCurrentPower(stack) + maxReceive);
					}

					return maxReceive;
				}

				@Override
				public int getMaxEnergyStored() {
					return getMaxPower();
				}

				@Override
				public int getEnergyStored() {
					return getCurrentPower(stack);
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

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

		if (!world.isClientSide && player.isShiftKeyDown() && this != Registry.getItem("mystium_hammer")) {

			ItemStack stack = player.getMainHandItem();

			CompoundTag nbt;
			if (stack.hasTag()) {
				nbt = stack.getTag();
			} else {
				nbt = new CompoundTag();
			}

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
		return super.use(world, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.hasTag()) {

			CompoundTag compound = stack.getTag();

			if (compound.contains("assemblylinemachines:fe")) {
				tooltip.add(new TextComponent(
						Formatting.GENERAL_FORMAT.format(compound.getInt("assemblylinemachines:fe")) + "/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE")
						.withStyle(ChatFormatting.DARK_PURPLE));
			} else {
				tooltip.add(new TextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").withStyle(ChatFormatting.DARK_RED));
			}

			if (compound.contains("assemblylinemachines:secondarystyle")) {
				tooltip.add(new TextComponent("Secondary Ability Enabled").withStyle(ChatFormatting.AQUA));
			}
			return;
		}

		tooltip.add(new TextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").withStyle(ChatFormatting.DARK_RED));

	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(stack.hasTag()) {

			CompoundTag compound = stack.getTag();

			if(compound.contains("assemblylinemachines:fe")) {

				return (double) ((compound.getInt("assemblylinemachines:fe") - getMaxPower()) * -1) / (double) getMaxPower();
			}
		}

		return super.getDurabilityForDisplay(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe")) {
			if(stack.getTag().getInt("assemblylinemachines:fe") == getMaxPower()) {
				return false;
			}
			return true;
		}
		return super.showDurabilityBar(stack);
	}
}
