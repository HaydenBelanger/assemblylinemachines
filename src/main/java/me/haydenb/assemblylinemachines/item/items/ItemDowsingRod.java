package me.haydenb.assemblylinemachines.item.items;

import me.haydenb.assemblylinemachines.fluid.FluidLevelManager;
import me.haydenb.assemblylinemachines.item.categories.ItemBasicFormattedName;
import me.haydenb.assemblylinemachines.util.Formatting;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class ItemDowsingRod extends ItemBasicFormattedName {

	
	public ItemDowsingRod() {
		super(TextFormatting.DARK_PURPLE);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		
		World world = context.getWorld();
		if(!world.isRemote) {
			FluidStack fs = FluidLevelManager.getOrCreateFluidStack(context.getPos(), world);
			if(fs != FluidStack.EMPTY && fs.getFluid() != Fluids.EMPTY) {
				context.getPlayer().sendStatusMessage(new StringTextComponent("There is " + Formatting.GENERAL_FORMAT.format(fs.getAmount()) + " mB of " + fs.getDisplayName().deepCopy().getString() + " in this chunk.").deepCopy().applyTextStyles(TextFormatting.GOLD), true);
			}else {
				context.getPlayer().sendStatusMessage(new StringTextComponent("There is no reservoir in this chunk."), true);
			}
		}
		
		return ActionResultType.CONSUME;
	}
}
