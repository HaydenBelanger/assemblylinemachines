package me.haydenb.assemblylinemachines.item;

import java.util.List;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ItemKey extends Item {

	private String translationKey;
	public ItemKey() {
		super(new Item.Properties());
		translationKey = Util.makeDescriptionId("item", new ResourceLocation(AssemblyLineMachines.MODID, "key_configured"));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {


			ItemStack is = context.getItemInHand();

			if(is.hasTag() && is.getTag().contains("assemblylinemachines:lockcode")) {
				return super.useOn(context);
			}else {

				Level world = context.getLevel();
				Player player = context.getPlayer();
				if(!world.isClientSide && player.isShiftKeyDown()) {
					if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof AbstractMachine) {
						AbstractMachine<?> abs = (AbstractMachine<?>) context.getLevel().getBlockEntity(context.getClickedPos());
						Block b = context.getLevel().getBlockState(context.getClickedPos()).getBlock();

						CompoundTag nbt = is.getTag();

						if(nbt == null) {
							nbt = new CompoundTag();
						}

						String key;
						if(abs.isRandomLocked()) {
							key = abs.getRandomLock(player);
						}else {
							key = abs.setRandomLock(player);
						}

						if(key != null) {
							nbt.putString("assemblylinemachines:lockcode", key);
							nbt.putString("assemblylinemachines:boundtranskey", b.getDescriptionId());
							is.setTag(nbt);
							player.displayClientMessage(Component.literal("Key cut."), true);
						}else {
							player.displayClientMessage(Component.literal("Could not cut Key."), true);
						}

					}
				}
				return InteractionResult.CONSUME;
			}



	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack is = playerIn.getMainHandItem();
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown() && is.hasTag() && is.getTag().contains("assemblylinemachines:lockcode")) {

			is.setTag(null);
			playerIn.displayClientMessage(Component.literal("Cleared Key."), true);
			return super.use(worldIn, playerIn, handIn);
		}else {

			return super.use(worldIn, playerIn, handIn);
		}
	}

	@Override
	public Component getName(ItemStack stack) {

		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:lockcode")) {
			return Component.translatable(translationKey);
		}
		return super.getName(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

		if(stack.hasTag() && stack.getTag().contains("assemblylinemachines:lockcode")) {
			String s = stack.getTag().getString("assemblylinemachines:boundtranskey");
			if(s.trim().equals("")) {
				tooltip.add(1, Component.literal("This Key is cut to a machine.").withStyle(ChatFormatting.DARK_GRAY));
			}else {
				tooltip.add(1, Component.literal("This Key is cut to " + Component.translatable(s).getString() + ".").withStyle(ChatFormatting.DARK_GRAY));
			}

			tooltip.add(2, Component.literal("SHIFT-RIGHT CLICK again to reset the Key.").withStyle(ChatFormatting.DARK_GRAY));
		}else {
			tooltip.add(1, Component.literal("This Key is not cut.").withStyle(ChatFormatting.DARK_GRAY));
			tooltip.add(2, Component.literal("SHIFT-RIGHT CLICK a machine to cut the Key to the machine.").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
