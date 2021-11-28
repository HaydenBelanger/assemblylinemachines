package me.haydenb.assemblylinemachines.item;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import me.haydenb.assemblylinemachines.world.FluidLevelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class ItemDowsingRod extends Item {

	
	public ItemDowsingRod() {
		super(new Item.Properties().tab(Registry.CREATIVE_TAB));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		
		
		Level world = context.getLevel();
		if(!world.isClientSide) {
			FluidStack fs = FluidLevelManager.getOrCreateFluidStack(context.getClickedPos(), world);
			if(fs != FluidStack.EMPTY && fs.getFluid() != Fluids.EMPTY) {
				context.getPlayer().displayClientMessage(new TextComponent("There is " + Formatting.GENERAL_FORMAT.format(fs.getAmount()) + " mB of " + fs.getDisplayName().getString() + " in this chunk.").withStyle(ChatFormatting.GOLD), true);
			}else {
				context.getPlayer().displayClientMessage(new TextComponent("There is no reservoir in this chunk."), true);
			}
		}
		
		return InteractionResult.CONSUME;
	}
}
