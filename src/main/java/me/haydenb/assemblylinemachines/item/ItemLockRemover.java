package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemLockRemover extends Item {

	public ItemLockRemover() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {


		Level world = context.getLevel();
		Player player = context.getPlayer();
		if(!world.isClientSide && player.isShiftKeyDown()) {
			if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof AbstractMachine) {
				AbstractMachine<?> abs = (AbstractMachine<?>) context.getLevel().getBlockEntity(context.getClickedPos());


				if(abs.isRandomLocked()) {
					if(abs.removeRandomLock(player)) {
						player.displayClientMessage(Component.literal("Removed Lock."), true);
					}else {
						player.displayClientMessage(Component.literal("Could not remove Lock."), true);
					}

				}else {
					player.displayClientMessage(Component.literal("Lock isn't set."), true);
				}

			}
		}
		return InteractionResult.CONSUME;



	}

	@Override
	public boolean onBlockStartBreak(ItemStack is, BlockPos pos, Player player) {

		Level world = player.getCommandSenderWorld();
		if(!world.isClientSide) {
			if(!is.hasTag() || !is.getTag().contains("assemblylinemachines:lockcode")){

				if(!world.isClientSide && player.isShiftKeyDown()) {
					if(world.getBlockEntity(pos) instanceof AbstractMachine) {
						AbstractMachine<?> abs = (AbstractMachine<?>) world.getBlockEntity(pos);

						if(abs.isRandomLocked()) {
							if(abs.removeRandomLock(player)) {
								is.setTag(null);
								player.displayClientMessage(Component.literal("Removed Lock."), true);
							}else {
								player.displayClientMessage(Component.literal("Could not remove Lock."), true);
							}

						}else {
							player.displayClientMessage(Component.literal("Lock isn't set."), true);
						}

					}
				}
			}
		}

		return super.onBlockStartBreak(is, pos, player);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

		tooltip.add(1, Component.literal("SHIFT-RIGHT CLICK a machine you cut to remove the Lock.").withStyle(ChatFormatting.DARK_GRAY));
	}
}
