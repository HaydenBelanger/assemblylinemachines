package me.haydenb.assemblylinemachines.item;

import java.util.Optional;

import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.TEBatteryCell;
import me.haydenb.assemblylinemachines.block.machines.BlockBottomlessStorageUnit.TEBottomlessStorageUnit;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidTank.TEFluidTank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemCreativeUpgradeKit extends Item {

	public static final Rarity CREATIVE_RARITY = Rarity.create("CREATIVE", ChatFormatting.DARK_PURPLE);

	public ItemCreativeUpgradeKit() {
		super(new Item.Properties().rarity(CREATIVE_RARITY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if(!context.getLevel().isClientSide()) {
			Optional<MutableComponent> result = makeCreative(context.getLevel(), context.getClickedPos());
			if(result.isPresent()) {
				context.getItemInHand().shrink(1);
				context.getPlayer().displayClientMessage(Component.literal("Upgraded " + result.get().getString() + " to creative."), true);
			}
		}
		return InteractionResult.CONSUME;
	}

	public static Optional<MutableComponent> makeCreative(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if(be != null) {
			boolean success = false;
			if(be instanceof TEFluidTank tank) {
				tank.upgraded = 2;
				if(!tank.fluid.isEmpty()) tank.fluid.setAmount(Integer.MAX_VALUE);
				tank.sendUpdates();
				success = true;
			}else if(be instanceof TEBatteryCell battery) {
				battery.creative = true;
				if(battery.properties.getCapacity() != battery.amount) battery.amount = battery.properties.getCapacity();
				battery.recalcBattery();
				battery.sendUpdates();
				success = true;
			}else if(be instanceof TEBottomlessStorageUnit bsu) {
				bsu.creative = true;
				if(bsu.storedItem != null) bsu.internalStored = Long.MAX_VALUE;
				bsu.sendUpdates();
				success = true;
			}
			if(success) return Optional.of(be.getBlockState().getBlock().getName());
		}
		return Optional.empty();
	}
}
