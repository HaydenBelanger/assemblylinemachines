package me.haydenb.assemblylinemachines.item.categories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.helpers.ICrankableMachine.ICrankableItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.TieredItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class ItemCrankTool<A extends TieredItem> extends TieredItem implements ICrankableItem{

	private final A parent;
	private final int maxCranks;
	private final ItemStack item;
	public ItemCrankTool(float damage, float speed, ToolType tt, Item.Properties builder, int maxCranks, A parent) {
		super(parent.getTier(), builder);
		this.parent = parent;
		this.maxCranks = maxCranks;
		this.item = new ItemStack(parent);
	}


	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if(stack.hasTag()) {
			CompoundNBT compound = stack.getTag();
			
			if(compound.contains("assemblylinemachines:cranks")) {
				
				int cranks = compound.getInt("assemblylinemachines:cranks");
				if((cranks - amount) < 1) {
					compound.remove("assemblylinemachines:cranks");
					compound.remove("assemblylinemachines:canbreakblackgranite");
				}else {
					compound.putInt("assemblylinemachines:cranks", cranks - amount);
				}
				
				
				
				stack.setTag(compound);
				return super.damageItem(stack, 0, entity, onBroken);
			}
		}
		
		return super.damageItem(stack, amount, entity, onBroken);
	}
	
	
	//Use A (Sword, Pickaxe, Hoe, Shovel, Axe)
	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		return parent.canHarvestBlock(blockIn);
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		
		return parent.canApplyAtEnchantingTable(item, enchantment);
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
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {
		return parent.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		return parent.getAttributeModifiers(equipmentSlot);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return parent.onItemUse(context);
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
	
	//End Use A
	
	@Override
	public int getMaxCranks() {
		return maxCranks;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(stack.hasTag()) {
			
			CompoundNBT compound = stack.getTag();
			
			if(compound.contains("assemblylinemachines:cranks")) {
				tooltip.add(new StringTextComponent("Cranks: " + compound.getInt("assemblylinemachines:cranks") + "/" + getMaxCranks()).func_230532_e_().func_240699_a_(TextFormatting.DARK_GREEN));
				return;
			}
		}
		
		tooltip.add(new StringTextComponent("Cranks: 0/" + getMaxCranks()).func_230532_e_().func_240699_a_(TextFormatting.DARK_RED));
	}
	/**
	 * Returns a CrankTool version of A.
	 * WARNING: A MUST(!!!!!!) be either SwordItem, PickaxeItem, ShovelItem, HoeItem, or AxeItem (Or any other class with the exact constructor of IItemTier, float, (opt) float, Item.Properties, like ItemHammer.)
	 * @return Made CrankTool
	 */
	@SuppressWarnings("unchecked")
	public static <A extends TieredItem> ItemCrankTool<A> makeCrankTool(IItemTier tier, @Nullable ToolType tt, int attackDamage, float attackSpeed, Item.Properties props, int maxCranks, Class<A> clazz) {
		if(tt != null) {
			props = props.addToolType(tt, tier.getHarvestLevel());
		}
		try {
			for(Constructor<?> c : clazz.getConstructors()) {
				if(c.getParameterCount() == 4) {
					return new ItemCrankTool<A>((float) attackDamage, attackSpeed, tt, props, maxCranks, (A) c.newInstance(tier, attackDamage, attackSpeed, props));
				}else {
					return new ItemCrankTool<A>((float) attackDamage, attackSpeed, tt, props, maxCranks, (A) c.newInstance(tier, attackSpeed, props));
				}
			}
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundNBT compound = stack.getTag();
			
			if(compound.contains("assemblylinemachines:cranks")) {
				
				
				return (double) ((compound.getInt("assemblylinemachines:cranks") - getMaxCranks()) * -1) / (double) getMaxCranks();
			}
		}
		
		return super.getDurabilityForDisplay(stack);
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:cranks")) {
			
			if(stack.getTag().getInt("assemblylinemachines:cranks") == getMaxCranks()) {
				return false;
			}
			return true;
		}
		return super.showDurabilityBar(stack);
	}
}
