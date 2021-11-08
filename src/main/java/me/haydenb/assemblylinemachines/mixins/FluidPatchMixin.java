package me.haydenb.assemblylinemachines.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

//Monitor this Mixin, as it may become redundant with future updates to Forge. This is solely to fix a bug.
@Mixin(LiquidBlock.class)
public class FluidPatchMixin {
	
	@Redirect(method = {"getCollisionShape", "isPathfindable", 
			"skipRendering", "onPlace", "updateShape", "neighborChanged",
			"shouldSpreadLiquid", "pickupBlock"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/LiquidBlock;fluid:Lnet/minecraft/world/level/material/FlowingFluid;", opcode = Opcodes.GETFIELD))
	public FlowingFluid fluid(LiquidBlock block) {
		if(block.fluid == null) {
			block.fluid = block.getFluid();
			if(block.fluid == null) {
				Minecraft.crash(new CrashReport("Liquid passed to supplier during patching is null.", new NullPointerException("Error while reading supplier for fluid.")));
			}
		}
		
		return block.fluid;
	}
}
