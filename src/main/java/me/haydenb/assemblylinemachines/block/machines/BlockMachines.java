package me.haydenb.assemblylinemachines.block.machines;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineScreenBuilder.PBDirection;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.RegisterableMachine;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.RegisterableMachine.Phases;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockMachines {

	//ALLOY SMELTER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="alloy_smelter")
	public static Block alloySmelter() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),Block.box(0, 13, 0, 16, 16, 16),
			Block.box(0, 3, 0, 3, 13, 5),Block.box(13, 3, 0, 16, 13, 5),
			Block.box(0, 3, 11, 16, 13, 16),Block.box(3, 3, 5, 3, 13, 11),
			Block.box(13, 3, 5, 13, 13, 11),Block.box(3, 6, 0, 13, 9, 1),
			Block.box(3, 9, 0, 6, 13, 1),Block.box(10, 9, 0, 13, 13, 1),
			Block.box(4, 3, 0, 7, 6, 1),Block.box(9, 3, 0, 12, 6, 1),
			Block.box(6, 9, 1, 10, 13, 1),Block.box(3, 3, 1, 13, 6, 1),
			Block.box(13, 3, 6, 14, 13, 7),Block.box(2, 3, 6, 3, 13, 7),
			Block.box(2, 3, 9, 3, 13, 10),Block.box(13, 3, 9, 14, 13, 10),
			Block.box(13, 3, 8, 15, 4, 11),Block.box(1, 4, 5, 3, 5, 8),
			Block.box(1, 6, 5, 3, 7, 8),Block.box(1, 8, 5, 3, 9, 8),
			Block.box(1, 10, 5, 3, 11, 8),Block.box(1, 12, 5, 3, 13, 8),
			Block.box(1, 3, 8, 3, 4, 11),Block.box(1, 5, 8, 3, 6, 11),
			Block.box(1, 7, 8, 3, 8, 11),Block.box(1, 9, 8, 3, 10, 11),
			Block.box(1, 11, 8, 3, 12, 11),Block.box(13, 5, 8, 15, 6, 11),
			Block.box(13, 7, 8, 15, 8, 11),Block.box(13, 9, 8, 15, 10, 11),
			Block.box(13, 11, 8, 15, 12, 11),Block.box(13, 4, 5, 15, 5, 8),
			Block.box(13, 6, 5, 15, 7, 8),Block.box(13, 8, 5, 15, 9, 8),
			Block.box(13, 10, 5, 15, 11, 8),Block.box(13, 12, 5, 15, 13, 8)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).build("alloy_smelter");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="alloy_smelter")
	public static MenuType<?> alloySmelterContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(
				Triple.of(119, 34, true), Triple.of(54, 34, false), Triple.of(75, 34, false), Triple.of(149, 21, false),
				Triple.of(149, 39, false), Triple.of(149, 57, false))).build("alloy_smelter");
	}

	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="alloy_smelter")
	public static BlockEntityType<?> alloySmelterEntity() {
		return MachineBuilder.blockEntity().energy(40000).baseProcessingStats(200, 16).recipeProcessor(Utils.recipeFunction(AlloyingCrafting.ALLOYING_RECIPE)).slotInfo(6, 3)
				.duplicateCheckingGroup(List.of(1, 2)).build("alloy_smelter");
	}

	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="alloy_smelter")
	public static void alloySmelterScreen() {
		MachineBuilder.screen().addBar(95, 35, 176, 64, 16, 14, PBDirection.LR).addBar(76, 53, 176, 52, 13, 12, PBDirection.STATIC).buildAndRegister("alloy_smelter");
	}
	
	//MKII ALLOY SMELTER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_alloy_smelter")
	public static Block mkIIAlloySmelter() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),Block.box(0, 13, 2, 16, 16, 16),
			Block.box(0, 13, 0, 6, 16, 2),Block.box(10, 13, 0, 16, 16, 2),
			Block.box(6, 15, 0, 10, 16, 2),Block.box(0, 3, 0, 3, 13, 5),
			Block.box(13, 3, 0, 16, 13, 5),Block.box(0, 3, 11, 16, 13, 16),
			Block.box(3, 3, 5, 3, 13, 11),Block.box(13, 3, 5, 13, 13, 11),
			Block.box(3, 6, 0, 13, 9, 2),Block.box(3, 9, 0, 6, 13, 2),
			Block.box(10, 9, 0, 13, 13, 2),Block.box(4, 3, 0, 7, 6, 1),
			Block.box(9, 3, 0, 12, 6, 1),Block.box(6, 9, 2, 10, 13, 2),
			Block.box(3, 3, 1, 13, 6, 1),Block.box(13, 3, 6, 14, 13, 7),
			Block.box(2, 3, 6, 3, 13, 7),Block.box(2, 3, 9, 3, 13, 10),
			Block.box(13, 3, 9, 14, 13, 10),Block.box(13, 3, 8, 15, 4, 11),
			Block.box(1, 4, 5, 3, 5, 8),Block.box(1, 6, 5, 3, 7, 8),
			Block.box(1, 8, 5, 3, 9, 8),Block.box(1, 10, 5, 3, 11, 8),
			Block.box(1, 12, 5, 3, 13, 8),Block.box(1, 3, 8, 3, 4, 11),
			Block.box(1, 5, 8, 3, 6, 11),Block.box(1, 7, 8, 3, 8, 11),
			Block.box(1, 9, 8, 3, 10, 11),Block.box(1, 11, 8, 3, 12, 11),
			Block.box(13, 5, 8, 15, 6, 11),Block.box(13, 7, 8, 15, 8, 11),
			Block.box(13, 9, 8, 15, 10, 11),Block.box(13, 11, 8, 15, 12, 11),
			Block.box(13, 4, 5, 15, 5, 8),Block.box(13, 6, 5, 15, 7, 8),
			Block.box(13, 8, 5, 15, 9, 8),Block.box(13, 10, 5, 15, 11, 8),
			Block.box(13, 12, 5, 15, 13, 8),Block.box(7, 9, 1, 9, 15, 2),
			Block.box(6, 13, 0, 10, 14, 2)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).build("mkii_alloy_smelter");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_alloy_smelter")
	public static MenuType<?> mkIIAlloySmelterContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(
				Triple.of(53, 61, true), Triple.of(107, 61, true), Triple.of(42, 22, false), Triple.of(64, 22, false), Triple.of(96, 22, false),
				Triple.of(118, 22, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_alloy_smelter");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_alloy_smelter")
	public static BlockEntityType<?> mkIIAlloySmelterEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(800, 16).recipeProcessor(Utils.recipeFunction(AlloyingCrafting.ALLOYING_RECIPE)).slotInfo(12, 6).slotIDTransformer((in) -> switch(in) {
		case 1 -> 2;
		case 2 -> 3;
		default -> in;
		}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
		case 0 -> 1;
		case 1 -> 4;
		case 2 -> 5;
		default -> in;
		}).slotExtractableFunction((slot) -> slot < 2).cycleCountModifier(0.5f).duplicateCheckingGroups(List.of(List.of(2, 3), List.of(4, 5))).build("mkii_alloy_smelter");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_alloy_smelter")
	public static void mkIIAlloySmelterScreen() {
		MachineBuilder.screen().defaultMKIIOptions().addBar(57, 42, 190, 66, 8, 11, PBDirection.UD, 0, 0, List.of(Pair.of(111, 42))).addBar(80, 43, 190, 52, 16, 14, PBDirection.STATIC).buildAndRegister("mkii_alloy_smelter");
	}

	//ELECTRIC FURNACE
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="electric_furnace")
	public static Block electricFurnace() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 0, 0, 16, 3, 16),Block.box(0, 13, 0, 16, 16, 16),
				Block.box(0, 3, 13, 16, 13, 16),Block.box(0, 3, 0, 2, 13, 3),
				Block.box(14, 3, 0, 16, 13, 3),Block.box(2, 6, 0, 14, 13, 3),
				Block.box(0, 3, 3, 16, 5, 13),Block.box(0, 11, 3, 16, 13, 13),
				Block.box(4, 3, 0, 7, 6, 1),Block.box(13, 3, 0, 14, 6, 1),
				Block.box(2, 3, 0, 3, 6, 1),Block.box(9, 3, 0, 12, 6, 1),
				Block.box(2, 3, 1, 14, 6, 1),Block.box(2, 5, 3, 2, 11, 13),
				Block.box(14, 5, 3, 14, 11, 13),Block.box(1, 5, 4, 2, 11, 5),
				Block.box(14, 5, 4, 15, 11, 5),Block.box(1, 5, 6, 2, 11, 7),
				Block.box(14, 5, 6, 15, 11, 7),Block.box(1, 5, 9, 2, 11, 10),
				Block.box(14, 5, 9, 15, 11, 10),Block.box(1, 5, 11, 2, 11, 12),Block.box(14, 5, 11, 15, 11, 12)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).build("electric_furnace");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="electric_furnace")
	public static MenuType<?> electricFurnaceContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(
				Triple.of(119, 34, true), Triple.of(75, 34, false), Triple.of(149, 21, false), Triple.of(149, 39, false),
				Triple.of(149, 57, false))).build("electric_furnace");
	}

	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="electric_furnace")
	public static BlockEntityType<?> electricFurnaceEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(80, 16).recipeProcessor(Utils.recipeFunction(RecipeType.SMELTING))
				.slotInfo(5, 3).executeOnRecipeCompletion((container, recipe) -> {
					container.getItem(0).shrink(1);
					((IMachineDataBridge) container).setCycles(((SmeltingRecipe) recipe).getCookingTime() / 20f);
				})
				.slotIDTransformer((slotIn) -> {
					return switch(slotIn) {
					case 0 -> 1;
					case 1 -> 0;
					default -> slotIn;
					};
				}).build("electric_furnace");
	}

	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="electric_furnace")
	public static void electricFurnaceScreen() {
		MachineBuilder.screen().addBar(95, 35, 176, 64, 16, 14, PBDirection.LR).addBar(76, 53, 176, 52, 13, 12, PBDirection.STATIC).buildAndRegister("electric_furnace");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_furnace")
	public static Block mkIIFurnace() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 0, 0, 16, 3, 16),Block.box(0, 13, 0, 16, 16, 16),
				Block.box(0, 3, 13, 16, 13, 16),Block.box(0, 3, 0, 2, 13, 3),
				Block.box(14, 3, 0, 16, 13, 3),Block.box(2, 6, 0, 14, 13, 3),
				Block.box(0, 3, 3, 16, 5, 13),Block.box(0, 11, 3, 16, 13, 13),
				Block.box(4, 3, 0, 7, 6, 1),Block.box(13, 3, 0, 14, 6, 1),
				Block.box(2, 3, 0, 3, 6, 1),Block.box(9, 3, 0, 12, 6, 1),
				Block.box(2, 3, 1, 14, 6, 1),Block.box(1, 5, 2, 2, 6, 14),
				Block.box(14, 5, 1, 15, 6, 13),Block.box(14, 6, 2, 15, 7, 14),
				Block.box(1, 6, 3, 2, 7, 15),Block.box(14, 8, 2, 15, 9, 14),
				Block.box(1, 8, 3, 2, 9, 15),Block.box(1, 7, 2, 2, 8, 14),
				Block.box(14, 7, 1, 15, 8, 13),Block.box(1, 10, 3, 2, 11, 15),
				Block.box(1, 9, 2, 2, 10, 14),Block.box(14, 9, 1, 15, 10, 13),Block.box(14, 10, 2, 15, 11, 14)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).build("mkii_furnace");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_furnace")
	public static MenuType<?> mkIIFurnaceContainer() {
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 61, true), Triple.of(107, 61, true), Triple.of(53, 22, false), Triple.of(107, 22, false),
				Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_furnace");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_furnace")
	public static BlockEntityType<?> mkIIFurnaceEntity() {
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(320, 16).recipeProcessor(Utils.recipeFunction(RecipeType.SMELTING))
		.slotInfo(10, 6).executeOnRecipeCompletion((container, recipe) -> {
			container.getItem(0).shrink(1);
			((IMachineDataBridge) container).setCycles(((SmeltingRecipe) recipe).getCookingTime() / 20f);
		}).slotIDTransformer((in) -> switch(in) {
		case 0 -> 2;
		case 1 -> 0;
		default -> in;
		}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
		case 0 -> 3;
		default -> 1;
		}).slotExtractableFunction((slot) -> slot < 2).cycleCountModifier(0.5f).build("mkii_furnace");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_furnace")
	public static void mkIIFurnaceScreen() {
		MachineBuilder.screen().defaultMKIIOptions()
		.addBar(57, 42, 190, 66, 8, 11, PBDirection.UD, 0, 0, List.of(Pair.of(111, 42)))
		.addBar(81, 44, 191, 53, 13, 12, PBDirection.STATIC).buildAndRegister("mkii_furnace");
	}

	//KINETIC GRINDER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="kinetic_grinder")
	public static Block kineticGrinder() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Shapes.block(), true).build("kinetic_grinder");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="kinetic_grinder")
	public static MenuType<?> kineticGrinderContainer(){
		return MachineBuilder.container().shiftMergeableSlots(0, 0).slotCoordinates(List.of(Triple.of(53, 26, false), Triple.of(75, 48, false))).build("kinetic_grinder");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="kinetic_grinder")
	public static BlockEntityType<?> kineticGrinderBlockEntity(){
		return MachineBuilder.blockEntity().crankMachine(1).baseProcessingStats(0, 16).recipeProcessor(Utils.recipeFunction(GrinderCrafting.GRINDER_RECIPE))
				.slotInfo(2, 0).outputToRight().allowedInZero().slotExtractableFunction((i) -> false).slotContentsValidator((i, is, be) -> i == 0 ? Blade.getBladeFromItem(is.getItem()) != null : true).build("kinetic_grinder");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="kinetic_grinder")
	public static void kineticGrinderScreen() {
		MachineBuilder.screen().doesNotUseFE().doesNotUseFEPT().addBar(73, 19, 176, 0, 20, 24, PBDirection.DU).buildAndRegister("kinetic_grinder");
	}
	
	//ELECTRIC GRINDER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="electric_grinder")
	public static Block electricGrinder() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(0, 3, 1, 16, 14, 16),Block.box(0, 6, 0, 16, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1), Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),Block.box(4, 3, 0, 7, 6, 1)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).build("electric_grinder");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="electric_grinder")
	public static MenuType<?> electricGrinderContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(
				Triple.of(119, 34, true), Triple.of(72, 34, false), Triple.of(149, 21, false), Triple.of(149, 39, false),
				Triple.of(149, 57, false))).build("electric_grinder");
	}

	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="electric_grinder")
	public static BlockEntityType<?> electricGrinderEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(180, 16).recipeProcessor(Utils.recipeFunction(GrinderCrafting.GRINDER_RECIPE))
				.slotInfo(5, 3).build("electric_grinder");
	}

	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="electric_grinder")
	public static void electricGrinderScreen() {
		MachineBuilder.screen().addBar(92, 35, 176, 52, 19, 14, PBDirection.LR).buildAndRegister("electric_grinder");
	}
	
	//MKII GRINDER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_grinder")
	public static Block mkIIGrinder() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),
				Block.box(0, 0, 0, 16, 3, 16),
				Block.box(0, 3, 1, 16, 14, 16),
				Block.box(0, 6, 0, 16, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1),
				Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),
				Block.box(4, 3, 0, 7, 6, 1)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).build("mkii_grinder");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_grinder")
	public static MenuType<?> mkIIGrinderContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 69, true), Triple.of(107, 69, true), Triple.of(53, 22, false), Triple.of(107, 22, false),
				Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_grinder");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_grinder")
	public static BlockEntityType<?> mkIIGrinderEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(720, 16).recipeProcessor(Utils.recipeFunction(GrinderCrafting.GRINDER_RECIPE))
				.slotInfo(10, 6).slotIDTransformer((in) -> switch(in) {
				case 1 -> 2;
				default -> in;
				}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
				case 0 -> 1;
				case 1 -> 3;
				default -> in;
				}).slotExtractableFunction((slot) -> slot < 2).cycleCountModifier(0.5f).build("mkii_grinder");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_grinder")
	public static void mkIIGrinderScreen() {
		MachineBuilder.screen().defaultMKIIOptions().addBar(54, 42, 190, 52, 14, 19, PBDirection.UD, 0, 0, List.of(Pair.of(108, 42))).buildAndRegister("mkii_grinder");
	}
	
	//KINETIC FLUID MIXER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="kinetic_fluid_mixer")
	public static Block kineticFluidMixer() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Shapes.block(), true).additionalProperties((state) -> state.setValue(StateProperties.FLUID, BathCraftingFluids.NONE), (builder) -> builder.add(StateProperties.FLUID)).build("kinetic_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="kinetic_fluid_mixer")
	public static MenuType<?> kineticFluidMixerContainer(){
		return MachineBuilder.container().shiftMergeableSlots(0, 0).slotCoordinates(List.of(Triple.of(63, 48, false), Triple.of(88, 48, false))).build("kinetic_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="kinetic_fluid_mixer")
	public static BlockEntityType<?> kineticFluidMixerEntity(){
		return MachineBuilder.blockEntity().crankMachine(1).baseProcessingStats(0, 16).recipeProcessor(Utils.recipeFunction(BathCrafting.BATH_RECIPE))
				.slotInfo(2, 0).outputToRight().allowedInZero().slotExtractableFunction((i) -> false).processesFluids(0, true).hasNoInternalTank()
				.specialStateModifier((recipe, state) -> state.setValue(StateProperties.FLUID, ((BathCrafting) recipe).getFluid()))
				.duplicateCheckingGroup(List.of(0, 1)).build("kinetic_fluid_mixer");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="kinetic_fluid_mixer")
	public static void kineticFluidMixerScreen() {
		MachineBuilder.screen().doesNotUseFE().doesNotUseFEPT().addBar(71, 19, 0, 0, 24, 24, PBDirection.DU)
		.stateBasedBlitPieceModifier((bs) -> bs.getValue(StateProperties.FLUID).getSimpleBlitPiece()).buildAndRegister("kinetic_fluid_mixer");
	}
	
	//ELECTRIC FLUID MIXER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="electric_fluid_mixer")
	public static Block electricFluidMixer() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 2, 16),
				Block.box(0, 2, 1, 16, 14, 16),Block.box(0, 5, 0, 16, 14, 1),
				Block.box(0, 2, 0, 3, 5, 1),Block.box(13, 2, 0, 16, 5, 1),
				Block.box(9, 2, 0, 12, 5, 1),Block.box(4, 2, 0, 7, 5, 1),Block.box(7, 4, 0, 9, 5, 1)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true)
				.additionalProperties((state) -> state.setValue(StateProperties.FLUID, BathCraftingFluids.NONE),
						(builder) -> builder.add(StateProperties.FLUID))
				.rightClickAction((state, world, pos, player) -> {
					if(player.getMainHandItem().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent() && world.getBlockEntity(pos) instanceof IMachineDataBridge) {
						FluidActionResult far = FluidUtil.tryEmptyContainer(player.getMainHandItem(), ((IMachineDataBridge) world.getBlockEntity(pos)).getCraftingFluidHandler(Optional.of(true)), 1000, player, true);
						if(far.isSuccess()) {
							if(player.getMainHandItem().getCount() == 1) {
								player.getInventory().removeItemNoUpdate(player.getInventory().selected);
							}else {
								player.getMainHandItem().shrink(1);
							}
							ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
							return InteractionResult.CONSUME;
						}
					}
					return null;
				}).build("electric_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="electric_fluid_mixer")
	public static MenuType<?> electricFluidMixerContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(Triple.of(119, 34, true), Triple.of(54, 34, false),
				Triple.of(75, 34, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false))).build("electric_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="electric_fluid_mixer")
	public static BlockEntityType<?> electricFluidMixerEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(60, 16).recipeProcessor(Utils.recipeFunction(BathCrafting.BATH_RECIPE))
				.slotInfo(6, 3).processesFluids(4000, true).specialStateModifier((recipe, state) -> {
					return state.setValue(StateProperties.FLUID, ((BathCrafting) recipe).getFluid());
				}).duplicateCheckingGroup(List.of(1, 2)).build("electric_fluid_mixer");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="electric_fluid_mixer")
	public static void electricFluidMixerScreen() {
		MachineBuilder.screen().renderFluidBar(41, 23, 37, 176, 84).stateBasedBlitPieceModifier((bs) -> bs.getValue(StateProperties.FLUID).getElectricBlitPiece())
		.addBar(95, 34, 0, 0, 15, 16, PBDirection.LR).internalTankSwitchingButton(129, 57, 192, 41, 11, 11).buildAndRegister("electric_fluid_mixer");
	}
	
	//MKII FLUID MIXER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_fluid_mixer")
	public static Block mkIIFluidMixer() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 2, 16),
				Block.box(0, 2, 1, 16, 14, 3),Block.box(0, 5, 0, 16, 14, 1),
				Block.box(0, 2, 0, 3, 5, 1),Block.box(13, 2, 0, 16, 5, 1),
				Block.box(9, 2, 0, 12, 5, 1),Block.box(4, 2, 0, 7, 5, 1),
				Block.box(7, 4, 0, 9, 5, 1),Block.box(4, 2, 3, 12, 14, 13),
				Block.box(0, 2, 13, 16, 14, 16),Block.box(1, 4, 5, 2, 12, 6),
				Block.box(2, 4, 5, 4, 5, 6),Block.box(2, 11, 5, 4, 12, 6),
				Block.box(0, 6, 4, 3, 7, 7),Block.box(3, 3, 4, 4, 6, 7),
				Block.box(3, 10, 4, 4, 13, 7),Block.box(0, 9, 4, 3, 10, 7),
				Block.box(3, 10, 9, 4, 13, 12),Block.box(2, 11, 10, 4, 12, 11),
				Block.box(1, 4, 10, 2, 12, 11),Block.box(0, 9, 9, 3, 10, 12),
				Block.box(0, 6, 9, 3, 7, 12),Block.box(3, 3, 9, 4, 6, 12),
				Block.box(2, 4, 10, 4, 5, 11),Block.box(14, 4, 10, 15, 12, 11),
				Block.box(12, 4, 10, 14, 5, 11),Block.box(12, 11, 10, 14, 12, 11),
				Block.box(13, 6, 9, 16, 7, 12),Block.box(12, 3, 9, 13, 6, 12),
				Block.box(12, 10, 9, 13, 13, 12),Block.box(13, 9, 9, 16, 10, 12),
				Block.box(12, 10, 4, 13, 13, 7),Block.box(12, 11, 5, 14, 12, 6),
				Block.box(14, 4, 5, 15, 12, 6),Block.box(13, 9, 4, 16, 10, 7),
				Block.box(13, 6, 4, 16, 7, 7),Block.box(12, 3, 4, 13, 6, 7),Block.box(12, 4, 5, 14, 5, 6)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true)
				.additionalProperties((state) -> state.setValue(StateProperties.FLUID, BathCraftingFluids.NONE),
						(builder) -> builder.add(StateProperties.FLUID))
				.rightClickAction((state, world, pos, player) -> {
					if(player.getMainHandItem().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent() && world.getBlockEntity(pos) instanceof IMachineDataBridge) {
						FluidActionResult far = FluidUtil.tryEmptyContainer(player.getMainHandItem(), ((IMachineDataBridge) world.getBlockEntity(pos)).getCraftingFluidHandler(Optional.of(true)), 1000, player, true);
						if(far.isSuccess()) {
							if(player.getMainHandItem().getCount() == 1) {
								player.getInventory().removeItemNoUpdate(player.getInventory().selected);
							}else {
								player.getMainHandItem().shrink(1);
							}
							ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
							return InteractionResult.CONSUME;
						}
					}
					return null;
				}).build("mkii_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_fluid_mixer")
	public static MenuType<?> mkIIFluidMixerContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 65, true), Triple.of(107, 65, true), Triple.of(42, 22, false), Triple.of(64, 22, false), Triple.of(96, 22, false),
				Triple.of(118, 22, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_fluid_mixer");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_fluid_mixer")
	public static BlockEntityType<?> mkIIFluidMixerEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(240, 16).recipeProcessor(Utils.recipeFunction(BathCrafting.BATH_RECIPE)).slotInfo(12, 6).specialStateModifier((recipe, state) -> {
					return state.setValue(StateProperties.FLUID, ((BathCrafting) recipe).getFluid());
				}).slotIDTransformer((in) -> switch(in) {
				case 1 -> 2;
				case 2 -> 3;
				default -> in;
				}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
				case 0 -> 1;
				case 1 -> 4;
				case 2 -> 5;
				default -> in;
				}).slotExtractableFunction((slot) -> slot < 2).cycleCountModifier(0.5f).processesFluids(8000, true).duplicateCheckingGroups(List.of(List.of(2, 3), List.of(4, 5))).build("mkii_fluid_mixer");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_fluid_mixer")
	public static void mkIIFluidMixerScreen() {
		MachineBuilder.screen().defaultMKIIOptions().renderFluidBar(84, 33, 37, 190, 52).internalTankSwitchingButton(82, 72, 206, 40, 12, 12)
		.stateBasedBlitPieceModifier((bs) -> bs.getValue(StateProperties.FLUID).getMKIIBlitPiece()).addBar(53, 42, 0, 0, 16, 15, PBDirection.UD, 0, 0, List.of(Pair.of(107, 42))).buildAndRegister("mkii_fluid_mixer");
	}
	
	//ELECTRIC PURIFIER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="electric_purifier")
	public static Block electricPurifier() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 0, 0, 16, 2, 16),Block.box(4, 2, 0, 5, 7, 1),
				Block.box(6, 2, 0, 7, 7, 1),Block.box(9, 2, 0, 10, 7, 1),
				Block.box(11, 2, 0, 12, 7, 1),Block.box(0, 7, 0, 16, 16, 16),
				Block.box(0, 2, 0, 2, 7, 16),Block.box(14, 2, 0, 16, 7, 16),Block.box(2, 2, 2, 14, 7, 16)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).additionalProperties((state) -> 
				state.setValue(StateProperties.PURIFIER_STATES, false), (builder) -> builder.add(StateProperties.PURIFIER_STATES)).build("electric_purifier");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="electric_purifier")
	public static MenuType<?> electricPurifierContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(Triple.of(119, 34, true),
				Triple.of(51, 21, false), Triple.of(51, 47, false), Triple.of(72, 34, false), Triple.of(149, 21, false),
				Triple.of(149, 39, false), Triple.of(149, 57, false))).build("electric_purifier");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="electric_purifier")
	public static BlockEntityType<?> electricPurifierEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(100, 16).recipeProcessor(Utils.recipeFunction(PurifierCrafting.PURIFIER_RECIPE))
				.slotInfo(7, 3).specialStateModifier((recipe, state) -> {
					return ((PurifierCrafting) recipe).requiresUpgrade() ? state.setValue(StateProperties.PURIFIER_STATES, true) : state.setValue(StateProperties.PURIFIER_STATES, false);
				}).duplicateCheckingGroup(List.of(1, 2, 3)).mustBeFullBefore((i) -> i == 1 || i == 2 ? List.of(3) : null)
				.slotContentsValidator((slot, is, be) -> {
					if(slot != 3) return true;
					return be.getLevel().getRecipeManager().getAllRecipesFor(PurifierCrafting.PURIFIER_RECIPE).stream().anyMatch((rcp) -> rcp.tobepurified.get().test(is));
				}).build("electric_purifier");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="electric_purifier")
	public static void electricPurifierScreen() {
		MachineBuilder.screen().addBar(70, 26, 176, 52, 43, 32, PBDirection.LR, 2, 10, List.of()).buildAndRegister("electric_purifier");
	}
	
	//MKII PURIFIER
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_purifier")
	public static Block mkIIPurifier() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 0, 0, 16, 2, 16),
				Block.box(4, 2, 0, 5, 7, 1),
				Block.box(6, 2, 0, 7, 7, 1),
				Block.box(9, 2, 0, 10, 7, 1),
				Block.box(11, 2, 0, 12, 7, 1),
				Block.box(0, 7, 0, 16, 16, 16),
				Block.box(0, 2, 0, 2, 7, 16),
				Block.box(14, 2, 0, 16, 7, 16),
				Block.box(2, 2, 2, 14, 7, 16)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).additionalProperties((state) -> state.setValue(StateProperties.PURIFIER_STATES, false), (builder) -> builder.add(StateProperties.PURIFIER_STATES)).build("mkii_purifier");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_purifier")
	public static MenuType<?> mkIIPurifierContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 73, true), Triple.of(107, 73, true), Triple.of(53, 42, false),
				Triple.of(107, 42, false), Triple.of(71, 22, false), Triple.of(89, 22, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_purifier");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_purifier")
	public static BlockEntityType<?> mkIIPurifierEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(400, 16).recipeProcessor(Utils.recipeFunction(PurifierCrafting.PURIFIER_RECIPE)).slotInfo(12, 6).specialStateModifier((recipe, state) -> {
			return ((PurifierCrafting) recipe).requiresUpgrade() ? state.setValue(StateProperties.PURIFIER_STATES, true) : state.setValue(StateProperties.PURIFIER_STATES, false);
		}).slotIDTransformer((in) -> switch(in) {
		case 1 -> 4;
		case 2 -> 5;
		case 3 -> 2;
		default -> in;
		}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
		case 0 -> 1;
		case 1 -> 4;
		case 2 -> 5;
		default -> in;
		}).slotExtractableFunction((slot) -> slot < 2).duplicateCheckingGroups(List.of(List.of(2, 4, 5), List.of(3, 4, 5)))
		.mustBeFullBefore((i) -> i == 4 || i == 5 ? List.of(2, 3) : null).slotContentsValidator((slot, is, be) -> {
			if(slot != 2 && slot != 3) return true;
			return be.getLevel().getRecipeManager().getAllRecipesFor(PurifierCrafting.PURIFIER_RECIPE).stream().anyMatch((rcp) -> rcp.tobepurified.get().test(is));
		}).build("mkii_purifier");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_purifier")
	public static void mkIIPurifierScreen() {
		MachineBuilder.screen().defaultMKIIOptions().addBar(57, 59, 190, 52, 8, 12, PBDirection.UD, 2, 20, List.of(Pair.of(111, 59)))
		.addBar(59, 20, 190, 88, 58, 21, PBDirection.STATIC, 3, 40, List.of()).buildAndRegister("mkii_purifier");
	}

	//PNEUMATIC COMPRESSOR

	@RegisterableMachine(phase=Phases.BLOCK, blockName="pneumatic_compressor")
	public static Block pneumaticCompressor() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(1, 3, 2, 15, 14, 16),Block.box(1, 6, 0, 15, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1),Block.box(0, 3, 1, 1, 6, 16),
				Block.box(15, 3, 1, 16, 6, 16),Block.box(15, 10, 0, 16, 14, 16),
				Block.box(0, 10, 0, 1, 14, 16),Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),Block.box(4, 3, 0, 7, 6, 1),
				Block.box(3, 3, 1, 13, 6, 2),Block.box(1, 6, 1, 15, 12, 2)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).build("pneumatic_compressor");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="pneumatic_compressor")
	public static MenuType<?> pneumaticCompressorContainer(){
		return MachineBuilder.container().shiftMergeableSlots(1, 3).slotCoordinates(List.of(Triple.of(120, 34, true),
				Triple.of(72, 34, false), Triple.of(94, 17, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false))).build("pneumatic_compressor");
	}

	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="pneumatic_compressor")
	public static BlockEntityType<?> pneumaticCompressorEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(90, 16).recipeProcessor(Utils.recipeFunction(PneumaticCrafting.PNEUMATIC_RECIPE)).slotInfo(6, 3).build("pneumatic_compressor");
	}

	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="pneumatic_compressor")
	public static void pneumaticCompressorScreen() {
		MachineBuilder.screen().addBar(92, 37, 176, 52, 20, 10, PBDirection.LR).addBar(94, 17, 176, 62, 16, 16, 2).buildAndRegister("pneumatic_compressor");
	}
	
	//MKII PNEUMATIC COMPRESSOR
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_pneumatic_compressor")
	public static Block mkIIPneumaticCompressor() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(1, 3, 1, 15, 14, 16),Block.box(1, 6, 0, 15, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1),Block.box(0, 3, 1, 1, 6, 16),
				Block.box(15, 3, 1, 16, 6, 16),Block.box(15, 10, 0, 16, 14, 16),
				Block.box(0, 10, 0, 1, 14, 16),Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),Block.box(4, 3, 0, 7, 6, 1),
				Block.box(3, 3, 1, 13, 6, 2),Block.box(0, 6, 0, 1, 10, 1),
				Block.box(0, 6, 15, 1, 10, 16),Block.box(15, 6, 15, 16, 10, 16),
				Block.box(15, 6, 0, 16, 10, 1),Block.box(0, 6, 1, 1, 7, 15),
				Block.box(0, 9, 1, 1, 10, 15),Block.box(15, 9, 1, 16, 10, 15),Block.box(15, 6, 1, 16, 7, 15)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).build("mkii_pneumatic_compressor");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_pneumatic_compressor")
	public static MenuType<?> mkIIPneumaticCompressorContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 66, true), Triple.of(107, 66, true), Triple.of(53, 22, false), Triple.of(107, 22, false),
				Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_pneumatic_compressor");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_pneumatic_compressor")
	public static BlockEntityType<?> mkIIPneumaticCompressorEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(360, 16).recipeProcessor(Utils.recipeFunction(PneumaticCrafting.PNEUMATIC_RECIPE)).slotInfo(10, 6)
				.slotIDTransformer((in) -> switch(in) {
				case 1 -> 2;
				default -> in;
				}).outputSlots(0, 0, 1).dualProcessorIDTransformer((in) -> switch(in) {
				case 0 -> 1;
				case 1 -> 3;
				default -> in;
				}).slotExtractableFunction((slot) -> slot < 2)
				.getCapturer((i) -> i == 2, (b, i) -> switch(((IMachineDataBridge) b).getOrSetMKIIPCSelMold(Optional.empty())) {
				case 1 -> Registry.getItem("rod_mold").getDefaultInstance();
				case 2 -> Registry.getItem("plate_mold").getDefaultInstance();
				case 3 -> Registry.getItem("gear_mold").getDefaultInstance();
				default -> ItemStack.EMPTY;
				}).cycleCountModifier(0.5f).build("mkii_pneumatic_compressor");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_pneumatic_compressor")
	public static void mkIIPneumaticCompressorScreen() {
		MachineBuilder.screen().defaultMKIIOptions().addBar(56, 40, 190, 52, 10, 20, PBDirection.UD, 0, 0, List.of(Pair.of(110, 40))).mkiiPneumaticCompressorButtons().buildAndRegister("mkii_pneumatic_compressor");
	}

	//LUMBER MILL
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="lumber_mill")
	public static Block lumberMill() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(1, 3, 2, 15, 14, 16),Block.box(1, 6, 0, 15, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1),Block.box(0, 3, 1, 1, 6, 16),
				Block.box(15, 3, 1, 16, 6, 16),Block.box(15, 10, 0, 16, 14, 16),
				Block.box(0, 10, 0, 1, 14, 16),Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),Block.box(4, 3, 0, 7, 6, 1),
				Block.box(3, 3, 1, 13, 6, 2),Block.box(1, 6, 1, 15, 12, 2)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true).build("lumber_mill");
	}

	@RegisterableMachine(phase=Phases.CONTAINER, blockName="lumber_mill")
	public static MenuType<?> lumberMillContainer(){
		return MachineBuilder.container().shiftMergeableSlots(2, 3).slotCoordinates(List.of(Triple.of(98, 34, true),
				Triple.of(124, 34, false), Triple.of(51, 34, false), Triple.of(149, 21, false), Triple.of(149, 39, false),
				Triple.of(149, 57, false))).build("lumber_mill");
	}

	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="lumber_mill")
	public static BlockEntityType<?> lumberMillEntity(){
		return MachineBuilder.blockEntity().energy(20000).baseProcessingStats(90, 16).recipeProcessor(Utils.recipeFunction(LumberCrafting.LUMBER_RECIPE))
				.slotInfo(6, 3).outputSlots(0, 1, 0).slotExtractableFunction((slot) -> slot <= 1).build("lumber_mill");
	}

	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="lumber_mill")
	public static void lumberMillScreen() {
		MachineBuilder.screen().addBar(71, 40, 176, 64, 19, 5, PBDirection.LR).addBar(71, 34, 176, 52, 19, 6, PBDirection.LR, 1, 10, List.of())
		.addBar(71, 34, 196, 52, 19, 6, PBDirection.LR, 7, 20, List.of()).buildAndRegister("lumber_mill");
	}
	
	//MKII LUMBER MILL
	
	@RegisterableMachine(phase=Phases.BLOCK, blockName="mkii_lumber_mill")
	public static Block mkIILumberMill() {
		return MachineBuilder.block().hasActiveProperty().voxelShape(Stream.of(
				Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(1, 3, 1, 15, 14, 16),Block.box(1, 6, 0, 15, 14, 1),
				Block.box(0, 3, 0, 3, 6, 1),Block.box(0, 3, 1, 1, 6, 16),
				Block.box(15, 3, 1, 16, 6, 16),Block.box(15, 10, 0, 16, 14, 16),
				Block.box(0, 10, 0, 1, 14, 16),Block.box(13, 3, 0, 16, 6, 1),
				Block.box(9, 3, 0, 12, 6, 1),Block.box(4, 3, 0, 7, 6, 1),
				Block.box(3, 3, 1, 13, 6, 2),Block.box(0, 6, 0, 1, 10, 1),
				Block.box(0, 6, 15, 1, 10, 16),Block.box(15, 6, 15, 16, 10, 16),
				Block.box(15, 6, 0, 16, 10, 1),Block.box(0, 6, 1, 1, 7, 15),
				Block.box(0, 9, 1, 1, 10, 15),Block.box(15, 9, 1, 16, 10, 15),Block.box(15, 6, 1, 16, 7, 15)
				).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(), true).build("mkii_lumber_mill");
	}
	
	@RegisterableMachine(phase=Phases.CONTAINER, blockName="mkii_lumber_mill")
	public static MenuType<?> mkIILumberMillContainer(){
		return MachineBuilder.container().shiftMergeableSlots(3, 6).playerInventoryPos(8, 106).playerHotbarPos(8, 164).slotCoordinates(List.of(Triple.of(53, 64, true), Triple.of(107, 64, true), Triple.of(80, 64, true), Triple.of(53, 22, false),
				Triple.of(107, 22, false), Triple.of(149, 21, false), Triple.of(149, 39, false), Triple.of(149, 57, false), Triple.of(167, 21, false), Triple.of(167, 39, false), Triple.of(167, 57, false))).build("mkii_lumber_mill");
	}
	
	@RegisterableMachine(phase=Phases.BLOCK_ENTITY, blockName="mkii_lumber_mill")
	public static BlockEntityType<?> mkIILumberMillBlockEntity(){
		return MachineBuilder.blockEntity().energy(400000).baseProcessingStats(360, 16).recipeProcessor(Utils.recipeFunction(LumberCrafting.LUMBER_RECIPE))
				.slotInfo(11, 6).slotExtractableFunction((slot) -> slot < 3).outputSlots(0, 2, 1)
				.slotIDTransformer((in) -> switch(in) {
				case 2 -> 3;
				default -> in;
				}).dualProcessorIDTransformer((in) -> switch(in) {
				case 2 -> 4;
				default -> in;
				}).build("mkii_lumber_mill");
	}
	
	@OnlyIn(Dist.CLIENT)
	@RegisterableMachine(phase=Phases.SCREEN, blockName="mkii_lumber_mill")
	public static void mkIILumberMillScreen() {
		MachineBuilder.screen().defaultMKIIOptions().addBar(54, 40, 190, 52, 14, 18, PBDirection.UD, 2, 10, List.of(Pair.of(108, 40))).buildAndRegister("mkii_lumber_mill");
	}
}
