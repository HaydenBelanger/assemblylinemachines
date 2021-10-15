package me.haydenb.assemblylinemachines.item.items;

import java.util.List;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

public class ItemLockRemover extends Item {
	
	public ItemLockRemover() {
		super(new Item.Properties().group(Registry.creativeTab));
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		if(!world.isRemote && player.isSneaking()) {
			if(context.getWorld().getTileEntity(context.getPos()) instanceof AbstractMachine) {
				AbstractMachine<?> abs = (AbstractMachine<?>) context.getWorld().getTileEntity(context.getPos());

				
				if(abs.isRandomLocked()) {
					if(abs.removeRandomLock(player)) {
						player.sendStatusMessage(new StringTextComponent("Removed Lock."), true);
					}else {
						player.sendStatusMessage(new StringTextComponent("Could not remove Lock."), true);
					}
					
				}else {
					player.sendStatusMessage(new StringTextComponent("Lock isn't set."), true);
				}
				
			}
		}
		return ActionResultType.CONSUME;
		
		
		
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack is, BlockPos pos, PlayerEntity player) {
		
		World world = player.getEntityWorld();
		if(!world.isRemote) {
			if(!is.hasTag() || !is.getTag().contains("assemblylinemachines:lockcode")){
				
				if(!world.isRemote && player.isSneaking()) {
					if(world.getTileEntity(pos) instanceof AbstractMachine) {
						AbstractMachine<?> abs = (AbstractMachine<?>) world.getTileEntity(pos);
						
						if(abs.isRandomLocked()) {
							if(abs.removeRandomLock(player)) {
								is.setTag(null);
								player.sendStatusMessage(new StringTextComponent("Removed Lock."), true);
							}else {
								player.sendStatusMessage(new StringTextComponent("Could not remove Lock."), true);
							}
							
						}else {
							player.sendStatusMessage(new StringTextComponent("Lock isn't set."), true);
						}
						
					}
				}
			}
		}
		
		return super.onBlockStartBreak(is, pos, player);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		
		tooltip.add(1, new StringTextComponent("SHIFT-RIGHT CLICK a machine you cut to remove the Lock.").deepCopy().mergeStyle(TextFormatting.DARK_GRAY));
	}
}
