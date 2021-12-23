package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.*;
import me.haydenb.assemblylinemachines.block.corrupt.*;
import me.haydenb.assemblylinemachines.block.energy.*;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.*;
import me.haydenb.assemblylinemachines.block.energy.BlockEntropyReactor.*;
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
import me.haydenb.assemblylinemachines.block.machines.electric.BlockMetalShaper.*;
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
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockFluidBath;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockHandGrinder;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockHandGrinder.TEHandGrinder;
import me.haydenb.assemblylinemachines.block.pipe.*;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.block.utility.*;
import me.haydenb.assemblylinemachines.block.utility.BlockBottomlessStorageUnit.*;
import me.haydenb.assemblylinemachines.block.utility.BlockCorruptingBasin.*;
import me.haydenb.assemblylinemachines.block.utility.BlockFluidRouter.*;
import me.haydenb.assemblylinemachines.block.utility.BlockFluidTank.BlockItemFluidTank;
import me.haydenb.assemblylinemachines.block.utility.BlockFluidTank.TEFluidTank;
import me.haydenb.assemblylinemachines.block.utility.BlockQuantumLink.*;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.item.ItemTiers.ArmorTiers;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolTiers;
import me.haydenb.assemblylinemachines.item.categories.*;
import me.haydenb.assemblylinemachines.item.categories.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.item.items.*;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.rendering.*;
import me.haydenb.assemblylinemachines.world.*;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell.EntityCorruptShellRender.EntityCorruptShellRenderFactory;
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
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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
	static final TreeMap<String, Item> itemRegistry = new TreeMap<>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	static final HashMap<String, Block> blockRegistry = new HashMap<>();
	
	private static final HashMap<String, TileEntityType<?>> teRegistry = new HashMap<>();
	private static final HashMap<String, ContainerType<?>> containerRegistry = new HashMap<>();
	private static final HashMap<ContainerType<?>, Integer> containerIdRegistry = new HashMap<>();
	private static final HashMap<String, Effect> effectRegistry = new HashMap<>();
	static final HashMap<String, ForgeFlowingFluid> fluidRegistry = new HashMap<>();
	
	public static final ModCreativeTab creativeTab = new ModCreativeTab("assemblylinemachines");
	
	//EVENTS
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		createItem("guidebook", new ItemGuidebook());
		
		createItem("titanium_ingot");
		createItem("titanium_nugget");
		createItem("titanium_blade_piece");
		createItem("titanium_blade", new ItemGrindingBlade(Blades.TITANIUM));
		createItem("pure_gold_blade_piece");
		createItem("pure_gold_blade", new ItemGrindingBlade(Blades.PUREGOLD));
		createItem("steel_blade_piece");
		createItem("steel_blade", new ItemGrindingBlade(Blades.STEEL));
		
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
		
		createItem("wooden_stirring_stick", new ItemStirringStick(TemperatureResistance.COLD, true, 290));
		createItem("pure_iron_stirring_stick", new ItemStirringStick(TemperatureResistance.HOT, false, 765));
		createItem("steel_stirring_stick", new ItemStirringStick(TemperatureResistance.HOT, false, 1690));
		
		createItem("mystium_dowsing_rod", new ItemDowsingRod());
		
		createItem("ground_lapis_lazuli");
		createItem("mystium_blend", new ItemBasicFormattedName(TextFormatting.LIGHT_PURPLE));
		createItem("mystium_ingot", new ItemBasicFormattedName(TextFormatting.LIGHT_PURPLE));
		createItem("corrupted_shard", new ItemCorruptedShard());
		
		createItem("titanium_sword", new SwordItem(ToolTiers.TITANIUM, 2, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_axe", new AxeItem(ToolTiers.TITANIUM, 4, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_pickaxe", new PickaxeItem(ToolTiers.TITANIUM, 0, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_shovel", new ShovelItem(ToolTiers.TITANIUM, 0, -1.3f, new Item.Properties().group(creativeTab)));
		
		createItem("titanium_hoe", new ItemPublicHoe(ToolTiers.TITANIUM, -0.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_helmet", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("titanium_chestplate", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("titanium_leggings", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("titanium_boots", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("titanium_hammer", new ItemHammer(ToolTiers.TITANIUM, 8, -3.2f, new Item.Properties().group(creativeTab)));
		
		createItem("crank_sword", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, null, 3, -1.2f, new Item.Properties().group(creativeTab), 600, SwordItem.class));
		createItem("crank_axe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, ToolType.AXE, 5, -3.2f, new Item.Properties().group(creativeTab), 750, AxeItem.class));
		createItem("crank_pickaxe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, ToolType.PICKAXE, 0, -1.5f, new Item.Properties().group(creativeTab), 800, PickaxeItem.class));
		createItem("crank_shovel", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, ToolType.SHOVEL, 0, -1.3f, new Item.Properties().group(creativeTab), 650, ShovelItem.class));
		createItem("crank_hoe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, null, -999, -0.5f, new Item.Properties().group(creativeTab), 900, ItemPublicHoe.class));
		createItem("crank_hammer", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, null, 11, -3.5f, new Item.Properties().group(creativeTab), 2600, ItemHammer.class));
		
		createItem("mystium_sword", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, null, 3, -1.2f, new Item.Properties().group(creativeTab), 250000, SwordItem.class));
		createItem("mystium_axe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, ToolType.AXE, 5, -3.2f, new Item.Properties().group(creativeTab), 250000, AxeItem.class));
		createItem("mystium_pickaxe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, ToolType.PICKAXE, 0, -1.5f, new Item.Properties().group(creativeTab), 250000, PickaxeItem.class));
		createItem("mystium_shovel", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, ToolType.SHOVEL, 0, -1.3f, new Item.Properties().group(creativeTab), 250000, ShovelItem.class));
		createItem("mystium_hoe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, null, -999, -0.5f, new Item.Properties().group(creativeTab), 250000, ItemPublicHoe.class));
		createItem("mystium_hammer", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, null, 11, -3.5f, new Item.Properties().group(creativeTab), 250000, ItemHammer.class));
		
		createItem("steel_hammer", new ItemHammer(ToolTiers.STEEL, 8, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("steel_sword", new SwordItem(ToolTiers.STEEL, 2, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_axe", new AxeItem(ToolTiers.STEEL, 4, -3.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_pickaxe", new PickaxeItem(ToolTiers.STEEL, 0, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_shovel", new ShovelItem(ToolTiers.STEEL, 0, -1.7f, new Item.Properties().group(creativeTab)));
		createItem("steel_helmet", new ArmorItem(ArmorTiers.STEEL, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("steel_chestplate", new ArmorItem(ArmorTiers.STEEL, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("steel_leggings", new ArmorItem(ArmorTiers.STEEL, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("steel_boots", new ArmorItem(ArmorTiers.STEEL, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("steel_hoe", new ItemPublicHoe(ToolTiers.STEEL, -0.5f, new Item.Properties().group(creativeTab)));
		
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
		
		createItem("miniature_black_hole", new ItemBasicFormattedName(TextFormatting.AQUA));
		createItem("singularity", new ItemBasicFormattedName(TextFormatting.DARK_AQUA));
		createItem("empowered_convection_component");
		
		createItem("fertilizer", new ItemFertilizer(1));
		createItem("enhanced_fertilizer", new ItemFertilizer(3));
		createItem("ultimate_fertilizer", new ItemFertilizer(5));
		createItem("enhanced_battery");
		createItem("microprocessor");
		createItem("energy_harness");
		
		createItem("ground_chromium");
		
		createItem("attuned_titanium_ingot", new ItemBasicFormattedName(TextFormatting.BLUE));
		createItem("attuned_titanium_plate", new ItemBasicFormattedName(TextFormatting.BLUE));
		createItem("purifier_upgrade_enhanced", new ItemUpgrade(false, "Purifier can process more recipes.", "Increases power consumption."));
		
		createItem("nether_star_shard", new ItemBasicFormattedName(TextFormatting.GOLD));
		
		createItem("entropy_reactor_upgrade_capacity", new ItemUpgrade(true, "Entropy Reactor has a higher capacity."));
		createItem("entropy_reactor_upgrade_cycle_delayer", new ItemUpgrade(true, "Entropy Reactor waits longer to clear capacity."));
		createItem("entropy_reactor_upgrade_variety", new ItemUpgrade(false, "Higher Variety has greater performance.", "Lower Variety has worsened performance."));
		
		createItem("poor_strange_matter");
		createItem("strange_matter");
		createItem("rich_strange_matter");
		
		createItem("corrupt_shell_spawn_egg", new SpawnEggItem(EntityCorruptShell.CORRUPT_SHELL, 0x005f85, 0x22a1d4, new Item.Properties().group(creativeTab)));
		createItem("galactic_flesh");
		createItem("reality_crystal");
		
		for(String i : itemRegistry.keySet()) {
			event.getRegistry().register(itemRegistry.get(i));
		}
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		createBlock("titanium_ore", Material.ROCK, 3f, 15f, 4, ToolType.PICKAXE, SoundType.STONE);
		createBlock("titanium_block", Material.IRON, 5f, 20f, 4, ToolType.PICKAXE, SoundType.METAL);
		
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
		
		
		createBlockNoItem("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT));
		createItem("steel_fluid_tank", new BlockItemFluidTank(Registry.getBlock("steel_fluid_tank")));
		createBlockNoItem("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD));
		createItem("wooden_fluid_tank", new BlockItemFluidTank(Registry.getBlock("wooden_fluid_tank")));
		
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
		
		createBlock("pump", new BlockPump());
		createBlock("pumpshaft", new BlockPumpshaft());
		
		createBlockNoItem("mystium_block", Material.IRON, 11f, 80f, 3, ToolType.PICKAXE, SoundType.METAL);
		createItem("mystium_block", new ItemBlockFormattedName(Registry.getBlock("mystium_block"), TextFormatting.LIGHT_PURPLE));
		
		createBlockNoItem("mystium_fluid_tank", new BlockFluidTank(250000, TemperatureResistance.HOT));
		createItem("mystium_fluid_tank", new BlockItemFluidTank(Registry.getBlock("mystium_fluid_tank")));
		
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
		
		createBlock("quantum_link", new BlockQuantumLink());
		createBlock("metal_shaper", new BlockMetalShaper());
		
		createBlock("entropy_reactor_block", new BlockEntropyReactor());
		createBlock("entropy_reactor_core", Material.IRON, 3f, 15f, 0, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("corrupt_dirt", new CorruptBlock(AbstractBlock.Properties.create(Material.EARTH).harvestTool(ToolType.SHOVEL).sound(SoundType.GROUND)));
		createBlock("corrupt_grass", new CorruptGrassBlock());
		createBlock("corrupt_sand", new CorruptSandBlock());
		createBlock("corrupt_stone", new CorruptBlock(AbstractBlock.Properties.create(Material.ROCK).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE)));
		
		createBlock("corrupting_basin", new BlockCorruptingBasin());
		
		//FLUIDS
		FluidRegistration.buildAndRegister("oil", new FluidOil(true), new FluidOil(false), true, new FluidOilBlock());
		FluidRegistration.buildAndRegister("condensed_void", new FluidCondensedVoid(true), new FluidCondensedVoid(false), true, new FluidCondensedVoidBlock());
		FluidRegistration.buildAndRegister("naphtha", new FluidNaphtha(true), new FluidNaphtha(false), true, new FluidNaphthaBlock());
		FluidRegistration.buildAndRegister("gasoline", new FluidOilProduct("gasoline", true), new FluidOilProduct("gasoline", false), true, new FluidOilProductBlock("gasoline"));
		FluidRegistration.buildAndRegister("diesel", new FluidOilProduct("diesel", true), new FluidOilProduct("diesel", false), true, new FluidOilProductBlock("diesel"));
		FluidRegistration.buildAndRegister("liquid_experience", 35, false, true, ALMFluid.LIQUID_EXPERIENCE, Material.WATER);
		
		FluidRegistration.buildAndRegister("ethane", new GaseousFluid("ethane", true, 0x135f0f), null, false, null);
		FluidRegistration.buildAndRegister("ethylene", new GaseousFluid("ethylene", true, 0xb3aac3), null, false, null);
		FluidRegistration.buildAndRegister("propane", new GaseousFluid("propane", true, 0x5f0f1c), null, false, null);
		FluidRegistration.buildAndRegister("propylene", new GaseousFluid("propylene", true, 0x290d55), null, false, null);
		
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
		
		createTileEntity("quantum_link", TEQuantumLink.class);
		
		createTileEntity("metal_shaper", TEMetalShaper.class);
		
		createTileEntity("entropy_reactor", TEEntropyReactor.class, blockRegistry.get("entropy_reactor_block"));
		createTileEntity("entropy_reactor_slave", TEEntropyReactorSlave.class, blockRegistry.get("entropy_reactor_block"));
		
		createTileEntity("corrupting_basin", TECorruptingBasin.class);
		
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
		
		createContainer("quantum_link", 1072, ContainerQuantumLink.class);
		
		createContainer("metal_shaper", 1073, ContainerMetalShaper.class); 
		createContainer("entropy_reactor", 1074, ContainerEntropyReactor.class);
		
		createContainer("corrupting_basin", 1075, ContainerCorruptingBasin.class);
		
		event.getRegistry().registerAll(containerRegistry.values().toArray(new ContainerType<?>[containerRegistry.size()]));
		
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		
		event.getRegistry().register(EntityCorruptShell.CORRUPT_SHELL.setRegistryName("corrupt_shell"));
		EntitySpawnPlacementRegistry.register(EntityCorruptShell.CORRUPT_SHELL, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::canSpawnOn);
		GlobalEntityTypeAttributes.put(EntityCorruptShell.CORRUPT_SHELL, EntityCorruptShell.registerAttributeMap().create());
	}
	
	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Effect> event) {
		
		createEffect("entropy_poisoning", new EffectEntropyPoisoning());
		createEffect("deep_burn", new EffectDeepBurn());
		
		event.getRegistry().registerAll(effectRegistry.values().toArray(new Effect[effectRegistry.size()]));
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
		ClientRegistry.bindTileEntityRenderer((TileEntityType<TEQuantumLink>)getTileEntity("quantum_link"), QuantumLinkTER::new);
		
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
		
		registerScreen("quantum_link", ContainerQuantumLink.class, ScreenQuantumLink.class);
		
		registerScreen("metal_shaper", ContainerMetalShaper.class, ScreenMetalShaper.class);
		
		registerScreen("entropy_reactor", ContainerEntropyReactor.class, ScreenEntropyReactor.class);
		
		registerScreen("corrupting_basin", ContainerCorruptingBasin.class, ScreenCorruptingBasin.class);
		
		((ItemMystiumTool<?>) Registry.getItem("mystium_pickaxe")).connectItemProperties();
		((ItemMystiumTool<?>) Registry.getItem("mystium_axe")).connectItemProperties();
		((ItemMystiumTool<?>) Registry.getItem("mystium_sword")).connectItemProperties();
		((ItemMystiumTool<?>) Registry.getItem("mystium_shovel")).connectItemProperties();
		((ItemMystiumTool<?>) Registry.getItem("mystium_hoe")).connectItemProperties();
		((ItemMystiumTool<?>) Registry.getItem("mystium_hammer")).connectItemProperties();
		
		RenderingRegistry.registerEntityRenderingHandler(EntityCorruptShell.CORRUPT_SHELL, new EntityCorruptShellRenderFactory());
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
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(MetalCrafting.METAL_RECIPE.toString()), MetalCrafting.METAL_RECIPE);
		event.getRegistry().register(MetalCrafting.SERIALIZER.setRegistryName("metal"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
		
		
		event.getBlockColors().register(new IBlockColor() {

			@Override
			public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int tint) {
				if(reader != null && pos != null && reader.getTileEntity(pos) instanceof TEFluidBath) {
					
					TEFluidBath te = (TEFluidBath) reader.getTileEntity(pos);
					
					return te.getFluidColor(reader, pos);
					
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
	
	//EFFECTS
	private static void createEffect(String name, Effect effect) {
		effect.setRegistryName(name);
		effectRegistry.put(name, effect);
	}
	
	public static Effect getEffect(String name) {
		return effectRegistry.get(name);
	}
	
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
	//Fluid Registration is handled in a separate class.
	public static Fluid getFluid(String name) {
		return fluidRegistry.get(name);
	}
	
	public static Collection<ForgeFlowingFluid> getFluids(){
		return fluidRegistry.values();
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
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	//Use carefully! Unchecked
	public static <T extends Container, X extends Screen & IHasContainer<T>> void registerScreen(String name, Class<T> ctc, Class<X> scc) {
		ScreenManager.registerFactory((ContainerType<T>) getContainerType(name), new IScreenFactory<T, X>() {

			@Override
			public X create(T p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
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
