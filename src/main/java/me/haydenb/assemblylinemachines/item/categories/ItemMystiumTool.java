package me.haydenb.assemblylinemachines.item.categories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General.IPoweredTool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemMystiumTool<A extends TieredItem> extends TieredItem implements IPoweredTool {

	private final A parent;
	private final ItemStack item;
	private final int maxPower;

	public ItemMystiumTool(float damage, float speed, ToolType tt, Item.Properties builder, int maxPower, A parent) {
		super(parent.getTier(), builder);
		this.parent = parent;
		this.maxPower = maxPower;
		this.item = new ItemStack(parent);
		
		this.addPropertyOverride(new ResourceLocation(AssemblyLineMachines.MODID, "active"), new IItemPropertyGetter() {
			
			@Override
			public float call(ItemStack stack, World p_call_2_, LivingEntity p_call_3_) {
				if(stack.hasTag()) {
					CompoundNBT compound = stack.getTag();
					if(compound.contains("assemblylinemachines:secondarystyle") && compound.contains("assemblylinemachines:fe") && compound.getInt("assemblylinemachines:fe") > 0) {
						return 1f;
					}
				}
				
				return 0f;
			}
		});
		
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (stack.hasTag()) {
			CompoundNBT compound = stack.getTag();

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

	// Use A (Sword, Pickaxe, Hoe, Shovel, Axe)
	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		return parent.canHarvestBlock(blockIn);
	}
	
	

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return parent.getDestroySpeed(stack, state);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

		return parent.canApplyAtEnchantingTable(item, enchantment);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return parent.canPlayerBreakBlockWhileHolding(state, worldIn, pos, player);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {

		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();

			if (item == Registry.getItem("mystium_sword")) {

				stack.damageItem(2, attacker, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				return false;

			}
		}

		return parent.hitEntity(stack, target, attacker);
	}


	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity player) {

		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();

			if (item == Registry.getItem("mystium_pickaxe")) {
				Direction d = Direction.getFacingDirections(player)[0];

				Iterator<BlockPos> iter;
				if (d == Direction.UP || d == Direction.DOWN) {

					iter = BlockPos.getAllInBox(pos.north().west(), pos.south().east()).iterator();
				} else {
					iter = BlockPos.getAllInBox(pos.up().offset(d.rotateY()), pos.down().offset(d.rotateYCCW())).iterator();
				}

				int cost = 0;
				
				ServerWorld sw = null;
				if(!world.isRemote) {
					sw = world.getServer().getWorld(world.getDimension().getType());
				}
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					BlockState bs = world.getBlockState(posx);
					if (bs.getBlock() != Blocks.AIR && bs.getBlockHardness(world, posx) != -1f) {
						cost = cost + 2;
						world.destroyBlock(posx, false, player);
						
						if(!world.isRemote) {
							
							performDrops(bs, posx, player, sw);
						}
					}

				}

				stack.damageItem(cost, player, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				return true;

			} else if (item == Registry.getItem("mystium_axe")) {


				BlockState bs = world.getBlockState(pos);
				
				if(bs.getBlock().getTags().contains(new ResourceLocation(AssemblyLineMachines.MODID, "world/mystium_axe_mineable"))) {
					stack.damageItem(breakAndBreakConnected(world, bs, 0, pos, player), player, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				}
				return true;

			} else if (item == Registry.getItem("mystium_shovel")) {
				Direction d = Direction.getFacingDirections(player)[0];

				Iterator<BlockPos> iter = BlockPos.getAllInBox(pos, pos.offset(d, 5)).iterator();

				int cost = 0;
				ServerWorld sw = null;
				if(!world.isRemote) {
					sw = world.getServer().getWorld(world.getDimension().getType());
				}
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					BlockState bs = world.getBlockState(posx);
					if (bs.getBlock() != Blocks.AIR && (bs.getMaterial() == Material.EARTH || bs.getMaterial() == Material.CLAY || bs.getMaterial() == Material.SNOW || bs.getMaterial() == Material.SNOW_BLOCK || bs.getMaterial() == Material.SAND)) {
						cost = cost + 2;
						world.destroyBlock(posx, false, player);
						
						if(!world.isRemote) {
							performDrops(bs, posx, player, sw);
						}
					}

				}

				stack.damageItem(cost, player, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				return true;
			}
		}
		return parent.onBlockDestroyed(stack, world, state, pos, player);
	}

	private int breakAndBreakConnected(World world, BlockState origState, int ctx, BlockPos posx, LivingEntity player) {
		world.destroyBlock(posx, true, player);

		int cost = 2;
		if(ctx <= 20) {
			Iterator<BlockPos> iter = BlockPos.getAllInBox(posx.down().north().west(), posx.up().south().east()).iterator();

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

	private void performDrops(BlockState bs, BlockPos pos, LivingEntity player, ServerWorld sw) {
		List<ItemStack> drops = bs.getDrops(new LootContext.Builder(sw).withParameter(LootParameters.TOOL, player.getHeldItemMainhand()).withParameter(LootParameters.POSITION, pos));
		InventoryHelper.dropItems(sw.getWorld(), pos, NonNullList.from(ItemStack.EMPTY, drops.toArray(new ItemStack[drops.size()])));
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		return parent.getAttributeModifiers(slot, stack);
	}

	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {

		ItemStack stack = context.getItem();
		if (stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {

			Item item = stack.getItem();


			if(item == Registry.getItem("mystium_hoe")) {

				BlockPos blockpos = context.getPos();
				World world = context.getWorld();
				int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
				if (hook != 0) return hook > 0 ? ActionResultType.SUCCESS : ActionResultType.FAIL;
				if (context.getFace() != Direction.DOWN && world.isAirBlock(blockpos.up())) {
					BlockState blockstate = ItemPublicHoe.HOE_LOOKUP.get(world.getBlockState(blockpos).getBlock());
					if (blockstate != null) {
						
						blockstate = Registry.getBlock("mystium_farmland").getDefaultState();
						PlayerEntity playerentity = context.getPlayer();
						world.playSound(playerentity, blockpos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
						if (!world.isRemote) {
							world.setBlockState(blockpos, blockstate, 11);
							if (playerentity != null) {
								context.getItem().damageItem(15, playerentity, (p_220043_1_) -> {
									p_220043_1_.sendBreakAnimation(context.getHand());
								});
							}
						}

						return world.isRemote ? ActionResultType.SUCCESS : ActionResultType.PASS;
					}
				}

				return ActionResultType.PASS;

			}
		}

		return parent.onItemUse(context);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return super.getDisplayName(stack).deepCopy().applyTextStyles(TextFormatting.DARK_PURPLE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasContainerItem() {
		return parent.hasContainerItem();
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

		CompoundNBT nbt;
		if(stack.hasTag()) {
			nbt = stack.getTag();
		}else {
			nbt = new CompoundNBT();
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
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
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
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {

		if (!world.isRemote && player.isSneaking() && this != Registry.getItem("mystium_hammer")) {

			ItemStack stack = player.getHeldItemMainhand();

			CompoundNBT nbt;
			if (stack.hasTag()) {
				nbt = stack.getTag();
			} else {
				nbt = new CompoundNBT();
			}

			if (nbt.contains("assemblylinemachines:secondarystyle")) {
				nbt.remove("assemblylinemachines:secondarystyle");
				player.sendStatusMessage(new StringTextComponent("Disabled Secondary Ability.").deepCopy().applyTextStyles(TextFormatting.RED), true);
			} else {
				nbt.putBoolean("assemblylinemachines:secondarystyle", true);
				player.sendStatusMessage(new StringTextComponent("Enabled Secondary Ability.").deepCopy().applyTextStyles(TextFormatting.AQUA), true);
			}

			stack.setTag(nbt);
			return new ActionResult<ItemStack>(ActionResultType.CONSUME, stack);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag()) {

			CompoundNBT compound = stack.getTag();

			if (compound.contains("assemblylinemachines:fe")) {
				tooltip.add(new StringTextComponent(
						Formatting.GENERAL_FORMAT.format(compound.getInt("assemblylinemachines:fe")) + "/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE")
						.deepCopy().applyTextStyles(TextFormatting.DARK_PURPLE));
			} else {
				tooltip.add(new StringTextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").deepCopy().applyTextStyles(TextFormatting.DARK_RED));
			}

			if (compound.contains("assemblylinemachines:secondarystyle")) {
				tooltip.add(new StringTextComponent("Secondary Ability Enabled").deepCopy().applyTextStyles(TextFormatting.AQUA));
			}
			return;
		}

		tooltip.add(new StringTextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").deepCopy().applyTextStyles(TextFormatting.DARK_RED));

	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundNBT compound = stack.getTag();
			
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

	/**
	 * Returns a PowerTool version of A. WARNING: A MUST(!!!!!!) be either
	 * SwordItem, PickaxeItem, ShovelItem, HoeItem, or AxeItem (Or any other class
	 * with the exact constructor of IItemTier, float, (opt) float, Item.Properties,
	 * like ItemHammer.)
	 * 
	 * @return Made PowerTool
	 */
	@SuppressWarnings("unchecked")
	public static <A extends TieredItem> ItemMystiumTool<A> makePowerTool(IItemTier tier, @Nullable ToolType tt, int attackDamage, float attackSpeed, Item.Properties props,
			int maxCranks, Class<A> clazz) {
		if (tt != null) {
			props = props.addToolType(tt, tier.getHarvestLevel());
		}
		try {
			for (Constructor<?> c : clazz.getConstructors()) {
				if (c.getParameterCount() == 4) {
					return new ItemMystiumTool<A>((float) attackDamage, attackSpeed, tt, props, maxCranks, (A) c.newInstance(tier, attackDamage, attackSpeed, props));
				} else {
					return new ItemMystiumTool<A>((float) attackDamage, attackSpeed, tt, props, maxCranks, (A) c.newInstance(tier, attackSpeed, props));
				}
			}
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
}
