package me.haydenb.assemblylinemachines.mixins.common;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils.IToolWithCharge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(HoeItem.class)
public class HoeItemTillingMixin {
	
	//This Mixin is responsible for patching in Mystium Farmland when certain conditions are met.
	@Inject(method = "changeIntoState", at = @At("HEAD"), cancellable = true)
	private static void changeIntoState(BlockState state, CallbackInfoReturnable<Consumer<UseOnContext>> cir) {
		cir.setReturnValue((context) ->{
			if(context.getItemInHand().getItem() instanceof IToolWithCharge) {
				IToolWithCharge tool = (IToolWithCharge) context.getItemInHand().getItem();
				if(tool.canUseSecondaryAbilities(context.getItemInHand(), "HoeItem")) {
					context.getLevel().setBlock(context.getClickedPos(), Registry.getBlock(tool.getPowerToolType().nameOfSecondaryFarmland).defaultBlockState(), 11);
					return;
				}
			}
			context.getLevel().setBlock(context.getClickedPos(), state, 11);
		});
	}
	
	//This Mixin is responsible for changing the durability cost of a Hoe when certain conditions are met.
	@Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
	private void hurtAndBreak(ItemStack stack, int amount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
		if(stack.getItem() instanceof IToolWithCharge) {
			IToolWithCharge tool = (IToolWithCharge) stack.getItem();
			if(tool.canUseSecondaryAbilities(stack, "HoeItem")) {
				amount = 15;
			}
		}
		stack.hurtAndBreak(amount, entity, onBroken);
	}
}
