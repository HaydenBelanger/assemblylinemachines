package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.fluid.FluidLevelManager;
import me.haydenb.assemblylinemachines.item.categories.ItemBasicFormattedName;
import me.haydenb.assemblylinemachines.util.Formatting;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public class ItemDowsingRod extends ItemBasicFormattedName {

	
	public ItemDowsingRod() {
		super(TextFormatting.LIGHT_PURPLE);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		
		if(!context.getWorld().isRemote) {
			FluidStack fs = FluidLevelManager.getOrCreateFluidStack(context.getPos(), context.getWorld());
			if(fs != FluidStack.EMPTY && fs.getFluid() != Fluids.EMPTY) {
				context.getPlayer().sendStatusMessage(new StringTextComponent("There is " + Formatting.GENERAL_FORMAT.format(fs.getAmount()) + " mB of " + fs.getDisplayName().getFormattedText() + " in this chunk.").applyTextStyle(TextFormatting.GOLD), true);
			}else {
				context.getPlayer().sendStatusMessage(new StringTextComponent("There is no reservoir in this chunk."), true);
			}
		}
		
		return ActionResultType.CONSUME;
	}
}
