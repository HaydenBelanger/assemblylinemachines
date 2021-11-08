package me.haydenb.assemblylinemachines.item.categories;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Multimap;

import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemCrankTool<A extends TieredItem> extends TieredItem implements ICrankableItem{

	private final A parent;
	private final int maxCranks;
	
	public ItemCrankTool(int maxCranks, A parent) {
		super(parent.getTier(), new Item.Properties().tab(parent.getItemCategory()));
		this.parent = parent;
		this.maxCranks = maxCranks;
	}


	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		if(stack.hasTag()) {
			CompoundTag compound = stack.getTag();
			
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
	public boolean isCorrectToolForDrops(BlockState blockIn) {
		return parent.isCorrectToolForDrops(blockIn);
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		
		return parent.canApplyAtEnchantingTable(stack, enchantment);
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
		return parent.hurtEnemy(stack, target, attacker);
	}
	
	@Override
	public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {
		return parent.mineBlock(stack, worldIn, state, pos, entityLiving);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return parent.getAttributeModifiers(slot, stack);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return parent.useOn(context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean hasCraftingRemainingItem() {
		return parent.hasCraftingRemainingItem();
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
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if(stack.hasTag()) {
			
			CompoundTag compound = stack.getTag();
			
			if(compound.contains("assemblylinemachines:cranks")) {
				tooltip.add(new TextComponent("Cranks: " + compound.getInt("assemblylinemachines:cranks") + "/" + getMaxCranks()).withStyle(ChatFormatting.DARK_GREEN));
				return;
			}
		}
		
		tooltip.add(new TextComponent("Cranks: 0/" + getMaxCranks()).withStyle(ChatFormatting.DARK_RED));
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(stack.hasTag()) {
			
			CompoundTag compound = stack.getTag();
			
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
