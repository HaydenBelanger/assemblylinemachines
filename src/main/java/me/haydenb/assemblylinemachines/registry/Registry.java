package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.*;
import me.haydenb.assemblylinemachines.block.BlockFluidBath.BathStatus;
import me.haydenb.assemblylinemachines.block.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.block.BlockFluidRouter.*;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder.TEHandGrinder;
import me.haydenb.assemblylinemachines.block.energy.*;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.*;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.*;
import me.haydenb.assemblylinemachines.block.fluid.*;
import me.haydenb.assemblylinemachines.block.fluid.FluidCondensedVoid.FluidCondensedVoidBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidNaphtha.FluidNaphthaBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidOil.FluidOilBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidOilProduct.FluidOilProductBlock;
import me.haydenb.assemblylinemachines.block.machines.crank.*;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockGearbox.*;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleCrankCharger.TESimpleCrankCharger;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleFluidMixer.*;
import me.haydenb.assemblylinemachines.block.machines.crank.BlockSimpleGrinder.*;
import me.haydenb.assemblylinemachines.block.machines.electric.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAlloySmelter.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAutocraftingTable.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFurnace.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricGrinder.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockLumberMill.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockQuarry.*;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockQuarryAddon.BlockFortuneVoidQuarryAddon;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockQuarryAddon.BlockSpeedQuarryAddon;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockToolCharger.TEToolCharger;
import me.haydenb.assemblylinemachines.block.machines.mob.*;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockExperienceHopper.TEExperienceHopper;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockExperienceMill.*;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockInteractor.*;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockPoweredSpawner.*;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockVacuumHopper.TEVacuumHopper;
import me.haydenb.assemblylinemachines.block.machines.oil.*;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockPump.TEPump;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockRefinery.*;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockRefineryAddon.*;
import me.haydenb.assemblylinemachines.block.pipe.*;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.block.storage.BlockBottomlessStorageUnit;
import me.haydenb.assemblylinemachines.block.storage.BlockBottomlessStorageUnit.*;
import me.haydenb.assemblylinemachines.block.storage.BlockFluidTank;
import me.haydenb.assemblylinemachines.block.storage.BlockFluidTank.TEFluidTank;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.item.ItemTiers;
import me.haydenb.assemblylinemachines.item.categories.*;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.item.items.*;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.rendering.PoweredSpawnerTER;
import me.haydenb.assemblylinemachines.rendering.TankTER;
import me.haydenb.assemblylinemachines.util.StateProperties;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Registry {
	
	//REGISTRY
	private static final TreeMap<String, Item> itemRegistry = new TreeMap<>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	private static final HashMap<String, Block> blockRegistry = new HashMap<>();
	
	private static final HashMap<String, TileEntityType<?>> teRegistry = new HashMap<>();
	private static final HashMap<String, ContainerType<?>> containerRegistry = new HashMap<>();
	private static final HashMap<ContainerType<?>, Integer> containerIdRegistry = new HashMap<>();
	
	private static final HashMap<String, Fluid> fluidRegistry = new HashMap<>();
	public static final ModCreativeTab creativeTab = new ModCreativeTab("assemblylinemachines");
	
	//EVENTS
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		createItem("titanium_ingot");
		createItem("titanium_nugget");
		createItem("titanium_blade_piece");
		createItem("titanium_blade", new ItemGrindingBlade(Blades.TITANIUM));
		createItem("pure_gold_blade_piece");
		createItem("pure_gold_blade", new ItemGrindingBlade(Blades.PUREGOLD));
		
		createItem("ground_iron");
		createItem("ground_gold");
		createItem("ground_titanium");
		createItem("ground_coal");
		createItem("ground_charcoal");
		
		createItem("sludge");
		
		createItem("steel_ingot");
		createItem("steel_nugget");
		
		createItem("pure_iron");
		createItem("pure_gold");
		createItem("pure_titanium");
		createItem("pure_steel");
		
		createItem("steel_rod");
		
		createItem("steel_plate");
		createItem("wooden_board");
		createItem("iron_plate");
		createItem("gold_plate");
		createItem("titanium_plate");
		createItem("mystium_plate", new ItemBasicFormattedName(TextFormatting.LIGHT_PURPLE));
		
		createItem("gold_gear");
		createItem("steel_gear");
		createItem("iron_gear");
		createItem("titanium_gear");
		
		createItem("empowered_coal", new ItemGearboxBasicFuel(3200));
		createItem("empowered_fuel", new ItemGearboxBasicFuel(6400));
		
		createItem("gearbox_upgrade_limiter", new ItemUpgrade(false, "Gearbox will only run while needed."));
		createItem("gearbox_upgrade_efficiency", new ItemUpgrade(false, "Gearbox will use less fuel.", "Gearbox will run slower."));
		createItem("gearbox_upgrade_compatability", new ItemUpgrade(false, "Gearbox can use any fuel."));
		
		
		
		createItem("item_pipe_upgrade_stack", new ItemUpgrade(true, "Item Pipe can take larger stacks."));
		createItem("item_pipe_upgrade_filter", new ItemUpgrade(true, "Item Pipe's filter space will grow."));
		createItem("item_pipe_upgrade_redstone", new ItemUpgrade(false, "Item Pipe will gain Redstone control."));
		
		createItem("upgrade_speed", new ItemUpgrade(true, "Device will operate much quicker.", "Device may use more fuel, power, or resources."));
		
		createItem("autocrafting_upgrade_sustained", new ItemUpgrade(false, new String[] {"Autocrafting Table can run without power."}, new String[] {"No other upgrades are accepted.", "Autocrafting Table will run slower."}));
		createItem("autocrafting_upgrade_recipes", new ItemUpgrade(true, "Autocrafting Table will gain more recipes."));
		
		createItem("machine_upgrade_conservation", new ItemUpgrade(true, "Machines may have a chance to not use input."));
		createItem("machine_upgrade_extra", new ItemUpgrade(true, "Machines may have a chance to provide additional output."));
		createItem("machine_upgrade_gas", new ItemUpgrade(false, "Some machines gain the ability to process gas.", "Greatly increases power consumption."));
		
		createItem("experience_mill_upgrade_level", new ItemUpgrade(true, "Experience Mill will perform a higher level enchantment."));
		
		createItem("wooden_stirring_stick", new ItemStirringStick(35, TemperatureResistance.COLD));
		createItem("pure_iron_stirring_stick", new ItemStirringStick(135, TemperatureResistance.HOT));
		createItem("mystium_dowsing_rod", new ItemDowsingRod());
		
		createItem("ground_lapis_lazuli");
		createItem("mystium_blend", new ItemBasicFormattedName(TextFormatting.LIGHT_PURPLE));
		createItem("mystium_ingot", new ItemBasicFormattedName(TextFormatting.LIGHT_PURPLE));
		createItem("corrupted_shard", new ItemCorruptedShard());
		
		createItem("titanium_sword", new SwordItem(ItemTiers.TITANIUM, 2, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_axe", new AxeItem(ItemTiers.TITANIUM, 4, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_pickaxe", new PickaxeItem(ItemTiers.TITANIUM, 0, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_shovel", new ShovelItem(ItemTiers.TITANIUM, 0, -1.3f, new Item.Properties().group(creativeTab)));
		
		createItem("titanium_hoe", new ItemPublicHoe(ItemTiers.TITANIUM, -0.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_helmet", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("titanium_chestplate", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("titanium_leggings", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("titanium_boots", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("titanium_hammer", new ItemHammer(ItemTiers.TITANIUM, 8, -3.2f, new Item.Properties().group(creativeTab)));
		
		createItem("crank_sword", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, null, 3, -1.2f, new Item.Properties().group(creativeTab), 600, SwordItem.class));
		createItem("crank_axe", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.AXE, 5, -3.2f, new Item.Properties().group(creativeTab), 750, AxeItem.class));
		createItem("crank_pickaxe", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.PICKAXE, 0, -1.5f, new Item.Properties().group(creativeTab), 800, PickaxeItem.class));
		createItem("crank_shovel", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.SHOVEL, 0, -1.3f, new Item.Properties().group(creativeTab), 650, ShovelItem.class));
		createItem("crank_hoe", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, null, -999, -0.5f, new Item.Properties().group(creativeTab), 900, ItemPublicHoe.class));
		createItem("crank_hammer", ItemCrankTool.makeCrankTool(ItemTiers.CRANK, null, 11, -3.5f, new Item.Properties().group(creativeTab), 2600, ItemHammer.class));
		
		
		createItem("mystium_sword", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, null, 3, -1.2f, new Item.Properties().group(creativeTab), 100000, SwordItem.class));
		createItem("mystium_axe", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, ToolType.AXE, 5, -3.2f, new Item.Properties().group(creativeTab), 300000, AxeItem.class));
		createItem("mystium_pickaxe", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, ToolType.PICKAXE, 0, -1.5f, new Item.Properties().group(creativeTab), 600000, PickaxeItem.class));
		createItem("mystium_shovel", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, ToolType.SHOVEL, 0, -1.3f, new Item.Properties().group(creativeTab), 300000, ShovelItem.class));
		createItem("mystium_hoe", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, null, -999, -0.5f, new Item.Properties().group(creativeTab), 90000, ItemPublicHoe.class));
		createItem("mystium_hammer", ItemMystiumTool.makePowerTool(ItemTiers.MYSTIUM, null, 11, -3.5f, new Item.Properties().group(creativeTab), 2500000, ItemHammer.class));
		
		createItem("steel_hammer", new ItemHammer(ItemTiers.STEEL, 8, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("steel_sword", new SwordItem(ItemTiers.STEEL, 2, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_axe", new AxeItem(ItemTiers.STEEL, 4, -3.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_pickaxe", new PickaxeItem(ItemTiers.STEEL, 0, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_shovel", new ShovelItem(ItemTiers.STEEL, 0, -1.7f, new Item.Properties().group(creativeTab)));
		createItem("steel_helmet", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("steel_chestplate", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("steel_leggings", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("steel_boots", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("steel_hoe", new ItemPublicHoe(ItemTiers.STEEL, -0.5f, new Item.Properties().group(creativeTab)));
		
		createItem("crank_shaft");
		createItem("convection_component");
		createItem("conduction_component");
		createItem("empowered_conduction_component");
		createItem("basic_battery");
		createItem("temperature_regulator");
		createItem("fluid_regulator");
		createItem("stainless_steel_tank_component");
		createItem("steel_tank_component");
		createItem("pneumatic_device");
		
		createItem("chromium_ingot");
		createItem("chromium_nugget");
		createItem("chromium_plate");
		
		createItem("stainless_steel_plate");
		createItem("energized_gold_ingot");
		createItem("energized_gold_plate");
		
		createItem("plastic_sheet");
		createItem("rubber_sheet");
		createItem("rubber_ball");
		createItem("plastic_ball");
		
		createItem("lock_remover", new ItemLockRemover());
		createItem("key", new ItemKey());
		
		createItem("generator_upgrade_coolant", new ItemUpgrade(false, "Generators will give more power per unit of fuel.", "Requires a secondary coolant supply."));
		
		createItem("polished_rock");
		createItem("mob_crystal", new ItemMobCrystal());
		
		createItem("sawdust");
		createItem("warped_sawdust");
		createItem("crimson_sawdust");
		
		
		
		for(String i : itemRegistry.keySet()) {
			event.getRegistry().register(itemRegistry.get(i));
		}
		
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		createBlock("titanium_ore", Material.ROCK, 3f, 15f, 3, ToolType.PICKAXE, SoundType.STONE);
		createBlock("titanium_block", Material.IRON, 5f, 20f, 3, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("hand_grinder", new BlockHandGrinder());
		createBlock("fluid_bath", new BlockFluidBath());
		
		createBlock("crank", new BlockCrank());
		createBlock("gearbox", new BlockGearbox());
		
		createBlock("simple_crank_charger", new BlockSimpleCrankCharger());
		createBlock("simple_fluid_mixer", new BlockSimpleFluidMixer());
		createBlock("simple_grinder", new BlockSimpleGrinder());
		
		createBlock("item_pipe", new PipeBase<IItemHandler>(() -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Type.ITEM));
		createBlock("fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.BASIC_FLUID));
		createBlock("advanced_fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.ADVANCED_FLUID));
		createBlock("energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.BASIC_POWER));
		
		
		createBlock("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT));
		createBlock("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD));
		
		createBlock("silt", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_brick", Material.ROCK, 4f, 12f, 0, ToolType.PICKAXE, SoundType.STONE);
		createBlock("slab_silt_brick", new SlabBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(4f, 12f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		createBlock("stair_silt_brick", new StairsBlock(() -> Registry.getBlock("smooth_black_granite").getDefaultState(), Block.Properties.create(Material.ROCK).hardnessAndResistance(4f, 12f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		
		createBlock("steel_block", Material.IRON, 7f, 30f, 3, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("black_granite", new BlockBlackGranite());
		createBlock("smooth_black_granite", Material.ROCK, 3f, 9f, 0, ToolType.PICKAXE, SoundType.STONE);
		createBlock("brick_black_granite", Material.ROCK, 3f, 9f, 0, ToolType.PICKAXE, SoundType.STONE);
		createBlock("chiselled_black_granite", Material.ROCK, 3f, 9f, 0, ToolType.PICKAXE, SoundType.STONE);
		createBlock("pillar_black_granite", new RotatedPillarBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 9f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		createBlock("slab_black_granite", new SlabBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 9f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		createBlock("stair_black_granite", new StairsBlock(() -> Registry.getBlock("smooth_black_granite").getDefaultState(), Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 9f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		
		createBlock("silt_iron", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_gold", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_titanium", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		
		createBlock("basic_battery_cell", new BlockBatteryCell(BatteryCellTiers.BASIC));
		createBlock("advanced_battery_cell", new BlockBatteryCell(BatteryCellTiers.ADVANCED));
		createBlock("coal_generator", new BlockCoalGenerator());
		createBlock("crankmill", new BlockCrankmill());
		
		createBlock("chromium_ore", Material.ROCK, 3f, 15f, 3, ToolType.PICKAXE, SoundType.STONE);
		createBlock("chromium_block", Material.IRON, 5f, 20f, 3, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("electric_furnace", new BlockElectricFurnace());
		createBlock("electric_purifier", new BlockElectricPurifier());
		createBlock("electric_grinder", new BlockElectricGrinder());
		createBlock("electric_fluid_mixer", new BlockElectricFluidMixer());
		createBlock("alloy_smelter", new BlockAlloySmelter());
		
		createBlock("autocrafting_table", new BlockAutocraftingTable());
		
		createBlockNoItem("naphtha_fire", new BlockNaphthaFire());
		createItem("naphtha_fire", new BlockItem(Registry.getBlock("naphtha_fire"), new Item.Properties()));
		
		createBlock("pump", new BlockPump());
		createBlock("pumpshaft", new BlockPumpshaft());
		
		createBlockNoItem("mystium_block", Material.IRON, 11f, 80f, 3, ToolType.PICKAXE, SoundType.METAL);
		createItem("mystium_block", new ItemBlockFormattedName(Registry.getBlock("mystium_block"), TextFormatting.LIGHT_PURPLE));
		
		createBlock("mystium_fluid_tank", new BlockFluidTank(250000, TemperatureResistance.HOT));
		
		createBlock("tool_charger", new BlockToolCharger());
		
		createBlock("adv_energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.ADVANCED_POWER));
		createBlock("smoldering_stone", Material.ROCK, 30f, 300f, 3, ToolType.PICKAXE, SoundType.STONE);
		createBlock("naphtha_turbine", new BlockNaphthaTurbine());
		
		createBlock("compressed_crafting_table", Material.WOOD, 2f, 10f, 0, ToolType.AXE, SoundType.WOOD);
		
		createBlock("refinery", new BlockRefinery());
		createBlock("refinery_attachment_separation", new BlockSeparationAddon());
		createBlock("refinery_attachment_addition", new BlockAdditionAddon());
		createBlock("refinery_attachment_halogen", new BlockHalogenAddon());
		createBlock("refinery_attachment_cracking", new BlockCrackingAddon());
		
		createBlock("fluid_router", new BlockFluidRouter());
		createBlock("interactor", new BlockInteractor());
		createBlock("vacuum_hopper", new BlockVacuumHopper());
		
		createBlockNoItem("bottomless_storage_unit", new BlockBottomlessStorageUnit());
		createItem("bottomless_storage_unit", new BlockItemBottomlessStorageUnit(Registry.getBlock("bottomless_storage_unit")));
		
		createBlock("combustion_generator", new BlockFluidGenerator(FluidGeneratorTypes.COMBUSTION));
		createBlock("geothermal_generator", new BlockFluidGenerator(FluidGeneratorTypes.GEOTHERMAL));
		
		createBlock("experience_hopper", new BlockExperienceHopper());
		createBlock("powered_spawner", new BlockPoweredSpawner());
		createBlock("experience_mill", new BlockExperienceMill());
		
		createBlock("quarry", new BlockQuarry());
		createBlock("quarry_speed_addon", new BlockSpeedQuarryAddon());
		createBlock("quarry_fortune_addon", new BlockFortuneVoidQuarryAddon());
		createBlock("quarry_void_addon", new BlockFortuneVoidQuarryAddon());
		
		createBlockNoItem("mystium_farmland", new BlockMystiumFarmland());
		
		createBlock("lumber_mill", new BlockLumberMill());
		
		
		//FLUIDS
		createFluid("oil", new FluidOil(true), new FluidOil(false), new FluidOilBlock(), getBucketItem("oil"));
		createFluid("condensed_void", new FluidCondensedVoid(true), new FluidCondensedVoid(false), new FluidCondensedVoidBlock(), getBucketItem("condensed_void"));
		createFluid("naphtha", new FluidNaphtha(true), new FluidNaphtha(false), new FluidNaphthaBlock(), getBucketItem("naphtha"));
		
		createFluid("gasoline", new FluidOilProduct(getFluidProperties("gasoline", getFluidAttributes("gasoline").temperature(200)), true), new FluidOilProduct(getFluidProperties("gasoline", 
				getFluidAttributes("gasoline").temperature(200)), false), new FluidOilProductBlock(() -> (FlowingFluid)Registry.getFluid("gasoline")), getBucketItem("gasoline"));
		createFluid("diesel", new FluidOilProduct(getFluidProperties("diesel", getFluidAttributes("diesel").temperature(200)), true), new FluidOilProduct(getFluidProperties("diesel", 
				getFluidAttributes("diesel").temperature(200)), false), new FluidOilProductBlock(() -> (FlowingFluid)Registry.getFluid("diesel")), getBucketItem("diesel"));
		createGaseousFluid("ethane", 600);
		createGaseousFluid("propane", 600);
		createGaseousFluid("ethylene", 600);
		createGaseousFluid("propylene", 600);
		createFluid("liquid_experience");
		
		event.getRegistry().registerAll(blockRegistry.values().toArray(new Block[blockRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Fluid> event) {
		
		//All Fluids are handled at bottom of registerBlocks, to handle the _block and _bucket versions of fluids.
		event.getRegistry().registerAll(fluidRegistry.values().toArray(new Fluid[fluidRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		createTileEntity("hand_grinder", TEHandGrinder.class);
		createTileEntity("fluid_bath", TEFluidBath.class);
		
		createTileEntity("simple_fluid_mixer", TESimpleFluidMixer.class);
		createTileEntity("simple_grinder", TESimpleGrinder.class);
		createTileEntity("simple_crank_charger", TESimpleCrankCharger.class);
		
		createTileEntity("gearbox", TEGearbox.class);
		
		createTileEntity("fluid_tank", TEFluidTank.class, blockRegistry.get("wooden_fluid_tank"), blockRegistry.get("steel_fluid_tank"), blockRegistry.get("mystium_fluid_tank"));
		
		createTileEntity("pipe_connector_item", ItemPipeConnectorTileEntity.class, blockRegistry.get("item_pipe"));
		createTileEntity("pipe_connector_fluid", FluidPipeConnectorTileEntity.class, blockRegistry.get("fluid_pipe"), blockRegistry.get("advanced_fluid_pipe"));
		createTileEntity("pipe_connector_energy", EnergyPipeConnectorTileEntity.class, blockRegistry.get("energy_pipe"), blockRegistry.get("adv_energy_pipe"));
		
		createTileEntity("basic_battery_cell", TEBasicBatteryCell.class);
		createTileEntity("advanced_battery_cell", TEAdvancedBatteryCell.class);
		
		createTileEntity("coal_generator", TECoalGenerator.class);
		createTileEntity("crankmill", TECrankmill.class);
		
		createTileEntity("electric_furnace", TEElectricFurnace.class);
		createTileEntity("electric_purifier", TEElectricPurifier.class);
		createTileEntity("electric_grinder", TEElectricGrinder.class);
		createTileEntity("electric_fluid_mixer", TEElectricFluidMixer.class);
		createTileEntity("alloy_smelter", TEAlloySmelter.class);
		
		createTileEntity("autocrafting_table", TEAutocraftingTable.class);
		createTileEntity("pump", TEPump.class);
		createTileEntity("tool_charger", TEToolCharger.class);
		createTileEntity("refinery", TERefinery.class);
		createTileEntity("fluid_router", TEFluidRouter.class);
		createTileEntity("interactor", TEInteractor.class);
		createTileEntity("vacuum_hopper", TEVacuumHopper.class);
		createTileEntity("bottomless_storage_unit", TEBottomlessStorageUnit.class);
		createTileEntity("fluid_generator", TEFluidGenerator.class, blockRegistry.get("geothermal_generator"), blockRegistry.get("combustion_generator"));
		createTileEntity("experience_hopper", TEExperienceHopper.class);
		
		createTileEntity("powered_spawner", TEPoweredSpawner.class);
		createTileEntity("experience_mill", TEExperienceMill.class);
		createTileEntity("quarry", TEQuarry.class);
		createTileEntity("lumber_mill", TELumberMill.class);
		
		event.getRegistry().registerAll(teRegistry.values().toArray(new TileEntityType<?>[teRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
		createContainer("simple_fluid_mixer", 1050, ContainerSimpleFluidMixer.class);
		createContainer("simple_grinder", 1051, ContainerSimpleGrinder.class);
		createContainer("gearbox", 1052, ContainerGearbox.class);
		createContainer("pipe_connector_item", 1053, ItemPipeConnectorContainer.class);
		
		createContainer("coal_generator", 1054, ContainerCoalGenerator.class);
		createContainer("crankmill", 1055, ContainerCrankmill.class);
		createContainer("battery_cell", 1056, ContainerBatteryCell.class);
		
		createContainer("electric_furnace", 1057, ContainerElectricFurnace.class);
		createContainer("electric_purifier", 1058, ContainerElectricPurifier.class);
		createContainer("electric_grinder", 1059, ContainerElectricGrinder.class);
		createContainer("electric_fluid_mixer", 1060, ContainerElectricFluidMixer.class);
		createContainer("alloy_smelter", 1062, ContainerAlloySmelter.class);
		
		createContainer("autocrafting_table", 1061, ContainerAutocraftingTable.class);
		createContainer("refinery", 1063, ContainerRefinery.class);
		createContainer("fluid_router", 1064, ContainerFluidRouter.class);
		createContainer("interactor", 1065, ContainerInteractor.class);
		createContainer("bottomless_storage_unit", 1066, ContainerBottomlessStorageUnit.class);
		createContainer("fluid_generator", 1067, ContainerFluidGenerator.class);
		
		createContainer("powered_spawner", 1068, ContainerPoweredSpawner.class);
		createContainer("experience_mill", 1069, ContainerExperienceMill.class);
		createContainer("quarry", 1070, ContainerQuarry.class);
		createContainer("lumber_mill", 1071, ContainerLumberMill.class);
		
		
		event.getRegistry().registerAll(containerRegistry.values().toArray(new ContainerType<?>[containerRegistry.size()]));
		
	}
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(getBlock("steel_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("wooden_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("mystium_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("autocrafting_table"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("geothermal_generator"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("naphtha_fire"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("powered_spawner"), RenderType.getCutout());
		
		RenderTypeLookup.setRenderLayer(getFluid("naphtha"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("naphtha_flowing"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("diesel"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("diesel_flowing"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("gasoline"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("gasoline_flowing"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("liquid_experience"), RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(getFluid("liquid_experience_flowing"), RenderType.getTranslucent());
		
		ClientRegistry.bindTileEntityRenderer((TileEntityType<TEFluidTank>)getTileEntity("fluid_tank"), TankTER::new);
		ClientRegistry.bindTileEntityRenderer((TileEntityType<TEPoweredSpawner>)getTileEntity("powered_spawner"), PoweredSpawnerTER::new);
		
		registerScreen("simple_fluid_mixer", ContainerSimpleFluidMixer.class, ScreenSimpleFluidMixer.class); 
		registerScreen("simple_grinder", ContainerSimpleGrinder.class, ScreenSimpleGrinder.class);
		registerScreen("gearbox", ContainerGearbox.class, ScreenGearbox.class);
		registerScreen("pipe_connector_item", ItemPipeConnectorContainer.class, ItemPipeConnectorScreen.class);
		
		registerScreen("coal_generator", ContainerCoalGenerator.class, ScreenCoalGenerator.class);
		registerScreen("crankmill", ContainerCrankmill.class, ScreenCrankmill.class);
		registerScreen("battery_cell", ContainerBatteryCell.class, ScreenBatteryCell.class);
		registerScreen("electric_furnace", ContainerElectricFurnace.class, ScreenElectricFurnace.class);
		registerScreen("electric_purifier", ContainerElectricPurifier.class, ScreenElectricPurifier.class);
		
		registerScreen("electric_grinder", ContainerElectricGrinder.class, ScreenElectricGrinder.class);
		registerScreen("electric_fluid_mixer", ContainerElectricFluidMixer.class, ScreenElectricFluidMixer.class);
		registerScreen("autocrafting_table", ContainerAutocraftingTable.class, ScreenAutocraftingTable.class);
		registerScreen("alloy_smelter", ContainerAlloySmelter.class, ScreenAlloySmelter.class);
		registerScreen("refinery", ContainerRefinery.class, ScreenRefinery.class);
		registerScreen("fluid_router", ContainerFluidRouter.class, ScreenFluidRouter.class);
		registerScreen("interactor", ContainerInteractor.class, ScreenInteractor.class);
		registerScreen("bottomless_storage_unit", ContainerBottomlessStorageUnit.class, ScreenBottomlessStorageUnit.class);
		registerScreen("fluid_generator", ContainerFluidGenerator.class, ScreenFluidGenerator.class);
		
		registerScreen("experience_mill", ContainerExperienceMill.class, ScreenExperienceMill.class);
		registerScreen("powered_spawner", ContainerPoweredSpawner.class, ScreenPoweredSpawner.class);
		registerScreen("quarry", ContainerQuarry.class, ScreenQuarry.class);
		
		registerScreen("lumber_mill", ContainerLumberMill.class, ScreenLumberMill.class);
	}
	
	@SubscribeEvent
	public static void registerCrafting(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(GrinderCrafting.GRINDER_RECIPE.toString()), GrinderCrafting.GRINDER_RECIPE);
		event.getRegistry().register(GrinderCrafting.SERIALIZER.setRegistryName("grinder"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(BathCrafting.BATH_RECIPE.toString()), BathCrafting.BATH_RECIPE);
		event.getRegistry().register(BathCrafting.SERIALIZER.setRegistryName("bath"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(PurifierCrafting.PURIFIER_RECIPE.toString()), PurifierCrafting.PURIFIER_RECIPE);
		event.getRegistry().register(PurifierCrafting.SERIALIZER.setRegistryName("purifier"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(AlloyingCrafting.ALLOYING_RECIPE.toString()), AlloyingCrafting.ALLOYING_RECIPE);
		event.getRegistry().register(AlloyingCrafting.SERIALIZER.setRegistryName("alloying"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(FluidInGroundRecipe.FIG_RECIPE.toString()), FluidInGroundRecipe.FIG_RECIPE);
		event.getRegistry().register(FluidInGroundRecipe.SERIALIZER.setRegistryName("fluid_in_ground"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(RefiningCrafting.REFINING_RECIPE.toString()), RefiningCrafting.REFINING_RECIPE);
		event.getRegistry().register(RefiningCrafting.SERIALIZER.setRegistryName("refining"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE.toString()), EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE);
		event.getRegistry().register(EnchantmentBookCrafting.SERIALIZER.setRegistryName("enchantment_book"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(LumberCrafting.LUMBER_RECIPE.toString()), LumberCrafting.LUMBER_RECIPE);
		event.getRegistry().register(LumberCrafting.SERIALIZER.setRegistryName("lumber"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
		
		
		event.getBlockColors().register(new IBlockColor() {

			@Override
			public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int tint) {
				if(reader != null && pos != null) {
					
					if(state.func_235901_b_(BlockFluidBath.STATUS)) {
						if(state.get(BlockFluidBath.STATUS) == BathStatus.SUCCESS) {
							
							TileEntity te = reader.getTileEntity(pos);
							if(te != null && te instanceof TEFluidBath) {
								
								TEFluidBath tef = (TEFluidBath) te;
								if(tef.getFluidColor() >= 0) {
									return tef.getFluidColor();
								}
							}
						}
					}
					
					
					if(state.get(StateProperties.FLUID) == BathCraftingFluids.LAVA) {
						return 0xcb3d07;
					}else {
						return BiomeColors.getWaterColor(reader, pos);
					}
				}
				
				return 0;
			}
		}, getBlock("fluid_bath"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
		event.getItemColors().register(new IItemColor() {
			
			@Override
			public int getColor(ItemStack stack, int index) {
				
				if(stack.hasTag()) {
					EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(stack.getTag().getString("assemblylinemachines:mob")));
					if(entity != null) {
						Integer x = ItemMobCrystal.MOB_COLORS.get(entity);
						if(x != null) {
							return x;
						}
					}
				}
				return 0x7d7d7d;
			}
		}, getItem("mob_crystal"));
	}
	
	@SubscribeEvent
	public static void configEvent(ModConfigEvent event) {
		final ModConfig cfg = event.getConfig();
		if(cfg.getSpec() == ConfigHolder.COMMON_SPEC) {
			ConfigHolder.COMMON.validateConfig();
		}
	}
	
	
	
	
	//===============================================
	
	
	
	//ITEMS
	private static void createItem(String name) {
		
		Item.Properties properties = new Item.Properties().group(creativeTab);
		createItem(name, properties);
	}
	private static void createItem(String name, Item.Properties properties) {
		Item i = new Item(properties);
		createItem(name, i);
	}
	
	private static void createItem(String name, Item item) {
		item.setRegistryName(name);
		itemRegistry.put(name, item);
	}
	
	public static Item getItem(String name) {
		return itemRegistry.get(name);
	}
	
	
	//BLOCKS
	private static void createBlock(String name, Material material, float hardness, float resistance, int harvestLevel, ToolType toolType, SoundType sound) {
		Block.Properties properties = Block.Properties.create(material).hardnessAndResistance(hardness, resistance).harvestLevel(harvestLevel).harvestTool(toolType).sound(sound);
		createBlock(name, properties);
		
	}
	
	private static void createBlock(String name, Block.Properties properties) {
		Block b = new Block(properties);
		createBlock(name, b);
	}
	
	private static void createBlock(String name, Block block) {
		block.setRegistryName(name);
		blockRegistry.put(name, block);
		createItem(name, new BlockItem(block, new Item.Properties().group(creativeTab)));
	}
	
	private static void createBlockNoItem(String name, Material material, float hardness, float resistance, int harvestLevel, ToolType toolType, SoundType sound) {
		Block.Properties properties = Block.Properties.create(material).hardnessAndResistance(hardness, resistance).harvestLevel(harvestLevel).harvestTool(toolType).sound(sound);
		createBlockNoItem(name, properties);
		
	}
	
	private static void createBlockNoItem(String name, Block.Properties properties) {
		Block b = new Block(properties);
		createBlockNoItem(name, b);
	}
	
	private static void createBlockNoItem(String name, Block block) {
		block.setRegistryName(name);
		blockRegistry.put(name, block);
	}
	
	public static Block getBlock(String name) {
		return blockRegistry.get(name);
	}
	
	//FLUIDS
	
	/**
	 * This must be called during Block registry, despite being targeted at fluids. This allows all maps to be populated first.
	 * Will register x, x_flowing, x_block, x_bucket.
	 */
	
	private static void createFluid(String name) {
		createFluid(name, getFluidAttributes(name));
		
	}
	
	private static void createFluid(String name, FluidAttributes.Builder attributes) {
		
		
		
		createFluid(name, attributes, getFluidProperties(name, attributes));
		
	}
	
	private static void createFluid(String name, FluidAttributes.Builder attributes, ForgeFlowingFluid.Properties properties) {
		
		
		
		createFluid(name, getSourceFluid(properties), getFlowingFluid(properties), getFlowingFluidBlock(name), getBucketItem(name));
	}
	
	private static void createFluid(String name, ForgeFlowingFluid source, ForgeFlowingFluid flowing, FlowingFluidBlock block, BucketItem bucket) {
		
		fluidRegistry.put(name, source.setRegistryName(name));
		fluidRegistry.put(name + "_flowing", flowing.setRegistryName(name + "_flowing"));
		blockRegistry.put(name + "_block", block.setRegistryName(name + "_block"));
		itemRegistry.put(name + "_bucket", bucket.setRegistryName(name + "_bucket"));
	}
	
	
	private static void createGaseousFluid(String name, int temp) {
		ForgeFlowingFluid.Properties props = new ForgeFlowingFluid.Properties(() -> fluidRegistry.get(name), () -> fluidRegistry.get(name + "_flowing"), FluidAttributes.builder(new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name),
				new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name)).gaseous().temperature(temp));
		GaseousFluid fl = new GaseousFluid(true, props);
		GaseousFluid gfl = new GaseousFluid(false, props);
		createFluidOnly(name, fl, gfl);
	}
	
	private static void createFluidOnly(String name, ForgeFlowingFluid source, ForgeFlowingFluid flowing) {
		fluidRegistry.put(name, source.setRegistryName(name));
		fluidRegistry.put(name + "_flowing", flowing.setRegistryName(name + "_flowing"));
	}
	
	//FLUID GETTERS
	public static FluidAttributes.Builder getFluidAttributes(String name){
		return FluidAttributes.builder(new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name), new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name + "_flowing"));
	}
	
	public static ForgeFlowingFluid.Properties getFluidProperties(String name, FluidAttributes.Builder attributes){
		return new ForgeFlowingFluid.Properties(() -> fluidRegistry.get(name), () -> fluidRegistry.get(name + "_flowing"), attributes).block(() -> (FlowingFluidBlock) blockRegistry.get(name + "_block")).bucket(() -> itemRegistry.get(name + "_bucket"));
	}
	
	private static ForgeFlowingFluid.Source getSourceFluid(ForgeFlowingFluid.Properties properties){
		return new ForgeFlowingFluid.Source(properties);
	}
	
	private static ForgeFlowingFluid.Flowing getFlowingFluid(ForgeFlowingFluid.Properties properties){
		return new ForgeFlowingFluid.Flowing(properties);
	}
	
	private static FlowingFluidBlock getFlowingFluidBlock(String name) {
		return new FlowingFluidBlock(() -> (FlowingFluid) fluidRegistry.get(name), Block.Properties.create(Material.WATER).hardnessAndResistance(100f).noDrops());
	}
	
	private static BucketItem getBucketItem(String name) {
		return new BucketItem(() -> fluidRegistry.get(name), new Item.Properties().maxStackSize(1).containerItem(Items.BUCKET).group(creativeTab));
	}
	
	public static Fluid getFluid(String name) {
		return fluidRegistry.get(name);
	}
	
	//TILE ENTITIES
	public static TileEntityType<?> getTileEntity(String name){
		return teRegistry.get(name);
	}
	
	@SuppressWarnings("deprecation")
	public static <T extends TileEntity> void createTileEntity(String name, Class<T> clazz, Block... blocks){
		teRegistry.put(name, TileEntityType.Builder.create(() -> {
			T inst;
			try {
				inst = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				inst = null;
				e.printStackTrace();
			}
			return inst;
		}, blocks).build(null).setRegistryName(name));
	
	}
	
	public static <T extends TileEntity> void createTileEntity(String name, Class<T> clazz) {
		createTileEntity(name, clazz, Registry.getBlock(name));
	}
	
	//CONTAINERS, CONTAINER IDS, SCREENS
	public static ContainerType<?> getContainerType(String name){
		return containerRegistry.get(name);
	}
	
	public static Integer getContainerId(String name){
		return containerIdRegistry.get(containerRegistry.get(name));
	}
	
	public static <T extends Container> void createContainer(String name, int id, Class<T> clazz) {
		containerRegistry.put(name, IForgeContainerType.create(new IContainerFactory<T>() {

			@Override
			public T create(int windowId, PlayerInventory inv, PacketBuffer data) {
				try {
					return clazz.getConstructor(int.class, PlayerInventory.class, PacketBuffer.class).newInstance(windowId, inv, data);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					return null;
				}
			}
		}).setRegistryName(name));
		containerIdRegistry.put(containerRegistry.get(name), id);
	}
	
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("unchecked")
	//Use carefully! Unchecked
	public static <T extends Container, F extends Screen & IHasContainer<T>> void registerScreen(String name, Class<T> ctc, Class<F> scc) {
		ScreenManager.registerFactory((ContainerType<T>) getContainerType(name), new IScreenFactory<T, F>() {

			@Override
			public F create(T p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
				try {
					return scc.getConstructor(ctc, PlayerInventory.class, ITextComponent.class).newInstance(p_create_1_, p_create_2_, p_create_3_);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}
}
