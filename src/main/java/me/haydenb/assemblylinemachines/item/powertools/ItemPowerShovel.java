package me.haydenb.assemblylinemachines.item.powertools;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import me.haydenb.assemblylinemachines.client.TooltipBorderHandler.ISpecialTooltip;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemPowerShovel extends ShovelItem implements IToolWithCharge, ISpecialTooltip {

	private final IToolWithCharge.PowerToolType ptt;
	
	public ItemPowerShovel(IToolWithCharge.PowerToolType ptt, Properties properties) {
		super(ptt.getTier(), 0, -1.3f, properties);
		this.ptt = ptt;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		ItemStack resStack = damageItem(stack, amount);
		return resStack == null ? super.damageItem(stack, amount, entity, onBroken) : super.damageItem(resStack, 0, entity, onBroken);
	}
	
	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos,
			LivingEntity player) {
		if(canUseSecondaryAbilities(stack)) {
			Direction d = Direction.orderedByNearest(player)[0];
			BiFunction<BlockState, BlockPos, Boolean> func;
			Iterator<BlockPos> iterator;
			BlockPos min;
			BlockPos max;
			switch(ptt) {
			case NOVASTEEL:
				min = d == Direction.UP || d == Direction.DOWN ? pos.north().west() : pos.above().relative(d.getClockWise());
				max = d == Direction.UP || d == Direction.DOWN ? pos.south().east().relative(d, 5) : pos.below().relative(d.getCounterClockWise()).relative(d, 5);
				break;
			default:
				min = pos;
				max = pos.relative(d, 5);
			}
			iterator = BlockPos.betweenClosedStream(min, max).iterator();
			func = ((statex, posx) -> statex.is(BlockTags.MINEABLE_WITH_SHOVEL));
			int cost = 0;
			while (iterator.hasNext()) {
				BlockPos posx = iterator.next();
				BlockState statex = world.getBlockState(posx);
				if (func.apply(statex, posx)) {
					cost = cost + 2;
					if(!world.isClientSide) {
						NonNullList<ItemStack> drops = NonNullList.create();
						for(ItemStack dropStack : statex.getDrops(new LootContext.Builder((ServerLevel) world).withParameter(LootContextParams.TOOL, stack).withParameter(LootContextParams.ORIGIN, new Vec3(posx.getX(), posx.getY(), posx.getZ())))) {
							drops.add(dropStack);
						}
						Containers.dropContents(world, posx, drops);
					}
					
					world.destroyBlock(posx, false);
				}
			}
			stack.hurtAndBreak(cost, player, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
			return true;
		}
		return super.mineBlock(stack, world, state, pos, player);
		
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return this.defaultInitCapabilities(stack, nbt);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
		this.defaultUse(p_41432_, p_41433_, p_41434_);
		return super.use(p_41432_, p_41433_, p_41434_);
	}
	
	@Override
	public void appendHoverText(ItemStack p_41421_, Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
		super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
		this.addEnergyInfoToHoverText(p_41421_, p_41423_);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		if(!stack.hasTag() || stack.getTag().getInt(ptt.getKeyName()) == 0) return super.isBarVisible(stack);
		return stack.getTag().getInt(ptt.getKeyName()) != this.getMaxPower(stack);
	}
	
	@Override
	public int getBarColor(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(ptt.getKeyName());
		if(dmg == 0) {
			return super.getBarColor(stack);
		}else {
			float v = (float) dmg / (float) getMaxPower(stack);
			return ARGB32.color(255, Math.round(v * 255f), Math.round(v * 255f), 255);
		}
		
	}
	
	@Override
	public int getBarWidth(ItemStack stack) {
		CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
		int dmg = compound.getInt(ptt.getKeyName());
		return dmg == 0 ? super.getBarWidth(stack) : Math.round(((float)dmg/ (float) getMaxPower(stack)) * 13.0f);
	}
	
	@Override
	public ResourceLocation getTexture() {
		return ptt.getBorderTexturePath();
	}

	@Override
	public int getTopColor() {
		return ptt.getARGBBorderColor();
	}
	
	@Override
	public int getBottomColor() {
		return ptt.getBottomARGBBorderColor().orElse(ISpecialTooltip.super.getBottomColor());
	}

	@Override
	public IToolWithCharge.PowerToolType getPowerToolType() {
		return ptt;
	}
}
