package me.haydenb.assemblylinemachines.item;

import java.util.concurrent.ExecutionException;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import me.haydenb.assemblylinemachines.world.CapabilityChunkFluids;
import me.haydenb.assemblylinemachines.world.CapabilityChunkFluids.IChunkFluidCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;

public class ItemDowsingRod extends Item {

	
	public ItemDowsingRod() {
		super(new Item.Properties().stacksTo(1).tab(Registry.CREATIVE_TAB));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		
		
		Level world = context.getLevel();
		if(!world.isClientSide) {
			boolean success = false;
			try {
				LazyOptional<IChunkFluidCapability> lazy = CapabilityChunkFluids.getChunkFluidCapability(world.getChunkAt(context.getClickedPos()));
				if(lazy.isPresent()) {
					IChunkFluidCapability capability = lazy.orElseThrow(null);
					if(!capability.getChunkFluid().equals(Fluids.EMPTY)){
						success = true;
						context.getPlayer().displayClientMessage(new TextComponent("There is " + FormattingHelper.GENERAL_FORMAT.format(capability.getFluidAmount()) + " mB of " + capability.getDisplayName().getString() + " in this chunk.").withStyle(ChatFormatting.GOLD), true);
					}
				}
			}catch(ExecutionException e) {
				e.printStackTrace();
			}finally {
				if(!success) {
					context.getPlayer().displayClientMessage(new TextComponent("There is no reservoir in this chunk."), true);
				}
			}
		}
		
		return InteractionResult.CONSUME;
	}
}
