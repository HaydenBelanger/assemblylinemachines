package me.haydenb.assemblylinemachines.item.categories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General.IPoweredTool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.TieredItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemMystiumTool<A extends TieredItem> extends TieredItem implements IPoweredTool {

	private final A parent;
	private final int maxPower;

	public ItemMystiumTool(float damage, float speed, ToolType tt, Item.Properties builder, int maxPower, A parent) {
		super(parent.getTier(), builder);
		this.parent = parent;
		this.maxPower = maxPower;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if (stack.hasTag()) {
			CompoundNBT compound = stack.getTag();

			if (compound.contains("assemblylinemachines:fe")) {

				int power = compound.getInt("assemblylinemachines:fe");
				if ((power - (amount * 150)) < 1) {
					compound.remove("assemblylinemachines:fe");
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
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return parent.canPlayerBreakBlockWhileHolding(state, worldIn, pos, player);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					if (world.getBlockState(posx).getBlock() != Blocks.AIR) {
						cost = cost + 2;
						world.destroyBlock(posx, true, player);
					}

				}

				stack.damageItem(cost, player, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				return true;

			} else if (item == Registry.getItem("mystium_axe")) {

				
				BlockState bs = world.getBlockState(pos);
				if(bs.getMaterial() == Material.WOOD) {
					stack.damageItem(breakAndBreakConnected(world, bs, 0, pos, player), player, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
				}
				return true;
				
			} else if (item == Registry.getItem("mystium_shovel")) {
				Direction d = Direction.getFacingDirections(player)[0];

				Iterator<BlockPos> iter = BlockPos.getAllInBox(pos, pos.offset(d, 5)).iterator();

				int cost = 0;
				while (iter.hasNext()) {
					BlockPos posx = iter.next();
					BlockState bs = world.getBlockState(posx);
					if (bs.getBlock() != Blocks.AIR && (bs.getMaterial() == Material.EARTH || bs.getMaterial() == Material.CLAY || bs.getMaterial() == Material.SNOW || bs.getMaterial() == Material.SNOW_BLOCK || bs.getMaterial() == Material.SAND)) {
						cost = cost + 2;
						world.destroyBlock(posx, true, player);
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
	
	@SuppressWarnings("deprecation")
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		return parent.getAttributeModifiers(equipmentSlot);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {

		return parent.onItemUse(context);
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return super.getDisplayName(stack).applyTextStyle(TextFormatting.DARK_PURPLE);
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

		if (!world.isRemote) {

			ItemStack stack = player.getHeldItemMainhand();
			Item item = stack.getItem();

			if (item == Registry.getItem("mystium_pickaxe") || item == Registry.getItem("mystium_axe") || item == Registry.getItem("mystium_shovel")) {
				CompoundNBT nbt;
				if (stack.hasTag()) {
					nbt = stack.getTag();
				} else {
					nbt = new CompoundNBT();
				}

				if (nbt.contains("assemblylinemachines:secondarystyle")) {
					nbt.remove("assemblylinemachines:secondarystyle");
					player.sendStatusMessage(new StringTextComponent("Disabled Secondary Ability.").applyTextStyle(TextFormatting.RED), true);
				} else {
					nbt.putBoolean("assemblylinemachines:secondarystyle", true);
					player.sendStatusMessage(new StringTextComponent("Enabled Secondary Ability.").applyTextStyle(TextFormatting.AQUA), true);
				}

				stack.setTag(nbt);
				return new ActionResult<ItemStack>(ActionResultType.CONSUME, stack);
			}
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
								.applyTextStyle(TextFormatting.DARK_PURPLE));
			} else {
				tooltip.add(new StringTextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").applyTextStyle(TextFormatting.DARK_RED));
			}

			if (compound.contains("assemblylinemachines:secondarystyle")) {
				tooltip.add(new StringTextComponent("Secondary Ability Enabled").applyTextStyle(TextFormatting.AQUA));
			}
			return;
		}

		tooltip.add(new StringTextComponent("0/" + Formatting.GENERAL_FORMAT.format(getMaxPower()) + " FE").applyTextStyle(TextFormatting.DARK_RED));

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
