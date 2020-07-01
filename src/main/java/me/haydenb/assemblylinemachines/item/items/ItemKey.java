package me.haydenb.assemblylinemachines.item.items;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

public class ItemKey extends Item {
	
	private String translationKey;
	public ItemKey() {
		super(new Item.Properties().group(Registry.creativeTab));
		translationKey = Util.makeTranslationKey("item", new ResourceLocation(AssemblyLineMachines.MODID, "key_configured"));
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		
			ItemStack is = context.getItem();
			
			if(is.hasTag() && is.getTag().contains("assemblylinemachines:lockcode")) {
				return super.onItemUse(context);
			}else {
				
				World world = context.getWorld();
				PlayerEntity player = context.getPlayer();
				if(!world.isRemote && player.isSneaking()) {
					if(context.getWorld().getTileEntity(context.getPos()) instanceof AbstractMachine) {
						AbstractMachine<?> abs = (AbstractMachine<?>) context.getWorld().getTileEntity(context.getPos());
						Block b = context.getWorld().getBlockState(context.getPos()).getBlock();
						
						CompoundNBT nbt = is.getTag();
						
						if(nbt == null) {
							nbt = new CompoundNBT();
						}
						
						String key;
						if(abs.isRandomLocked()) {
							key = abs.getRandomLock(player);
						}else {
							key = abs.setRandomLock(player);
						}
						
						if(key != null) {
							nbt.putString("assemblylinemachines:lockcode", key);
							nbt.putString("assemblylinemachines:boundtranskey", b.getTranslationKey());
							is.setTag(nbt);
							player.sendStatusMessage(new StringTextComponent("Key cut."), true);
						}else {
							player.sendStatusMessage(new StringTextComponent("Could not cut Key."), true);
						}
						
					}
				}
				return ActionResultType.CONSUME;
			}
		
		
		
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack is = playerIn.getHeldItemMainhand();
		if(!worldIn.isRemote && playerIn.isSneaking() && is.hasTag() && is.getTag().contains("assemblylinemachines:lockcode")) {
			
			is.setTag(null);
			playerIn.sendStatusMessage(new StringTextComponent("Cleared Key."), true);
			return super.onItemRightClick(worldIn, playerIn, handIn);
		}else {
			
			return super.onItemRightClick(worldIn, playerIn, handIn);
		}
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:lockcode")) {
			return new TranslationTextComponent(translationKey);
		}
		return super.getDisplayName(stack);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		
		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:lockcode")) {
			String s = stack.getTag().getString("assemblylinemachines:boundtranskey");
			if(s.isBlank()) {
				tooltip.add(1, new StringTextComponent("This Key is cut to a machine.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY));
			}else {
				tooltip.add(1, new StringTextComponent("This Key is cut to " + new TranslationTextComponent(s).getString() + ".").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY));
			}
			
			tooltip.add(2, new StringTextComponent("SHIFT-RIGHT CLICK again to reset the Key.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY));
		}else {
			tooltip.add(1, new StringTextComponent("This Key is not cut.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY));
			tooltip.add(2, new StringTextComponent("SHIFT-RIGHT CLICK a machine to cut the Key to the machine.").func_230532_e_().func_240699_a_(TextFormatting.DARK_GRAY));
		}
	}
}
