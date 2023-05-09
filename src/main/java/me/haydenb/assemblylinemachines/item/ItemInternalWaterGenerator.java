package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.block.machines.BlockFluidTank.TEFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;

public class ItemInternalWaterGenerator extends Item {

	public ItemInternalWaterGenerator() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {

		if(!context.getLevel().isClientSide()) {
			BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
			if(be != null && be instanceof TEFluidTank tank) {
				if(tank.upgraded != 0) {
					context.getPlayer().displayClientMessage(Component.literal("This Tank is already upgraded."), true);
				}else {
					tank.upgraded = 1;
					if(tank.fluid.getFluid().equals(Fluids.WATER)) tank.fluid.setAmount(Integer.MAX_VALUE);
					tank.sendUpdates();
					context.getPlayer().displayClientMessage(Component.literal("Installed Internal Water Generator in " + tank.getBlockState().getBlock().getName().getString() + "."), true);
					context.getItemInHand().shrink(1);
				}
			}
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> tooltip,
			TooltipFlag pIsAdvanced) {
		tooltip.addAll(1, List.of(Component.literal("Harnesses the power of a 2X2 pond.").withStyle(ChatFormatting.DARK_GRAY), Component.literal("Use on a Tank to make infinite Water.").withStyle(ChatFormatting.DARK_GRAY)));
	}
}
