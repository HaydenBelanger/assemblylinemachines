package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.item.categories.ItemBasicFormattedName;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.world.generation.FluidLevelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class ItemDowsingRod extends ItemBasicFormattedName {

	
	public ItemDowsingRod() {
		super(ChatFormatting.DARK_PURPLE);
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
