package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockBlackGranite;
import me.haydenb.assemblylinemachines.block.BlockCrank;
import me.haydenb.assemblylinemachines.block.BlockFluidBath;
import me.haydenb.assemblylinemachines.block.BlockFluidBath.BathStatus;
import me.haydenb.assemblylinemachines.block.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.block.BlockFluidTank;
import me.haydenb.assemblylinemachines.block.BlockFluidTank.TEFluidTank;
import me.haydenb.assemblylinemachines.block.BlockGearbox;
import me.haydenb.assemblylinemachines.block.BlockGearbox.ContainerGearbox;
import me.haydenb.assemblylinemachines.block.BlockGearbox.ScreenGearbox;
import me.haydenb.assemblylinemachines.block.BlockGearbox.TEGearbox;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder.TEHandGrinder;
import me.haydenb.assemblylinemachines.block.energy.BlockBasicBatteryCell;
import me.haydenb.assemblylinemachines.block.energy.BlockBasicBatteryCell.ContainerBasicBatteryCell;
import me.haydenb.assemblylinemachines.block.energy.BlockBasicBatteryCell.ScreenBasicBatteryCell;
import me.haydenb.assemblylinemachines.block.energy.BlockBasicBatteryCell.TEBasicBatteryCell;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.ContainerCoalGenerator;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.ScreenCoalGenerator;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.TECoalGenerator;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.ContainerCrankmill;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.ScreenCrankmill;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.TECrankmill;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleCrankCharger;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleCrankCharger.TESimpleCrankCharger;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleFluidMixer.ContainerSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleFluidMixer.ScreenSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleFluidMixer.TESimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleGrinder;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleGrinder.ContainerSimpleGrinder;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleGrinder.ScreenSimpleGrinder;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleGrinder.TESimpleGrinder;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAutocraftingTable;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer.ContainerElectricFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer.ScreenElectricFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer.TEElectricFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFurnace;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFurnace.ContainerElectricFurnace;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFurnace.ScreenElectricFurnace;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFurnace.TEElectricFurnace;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricGrinder;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricGrinder.ContainerElectricGrinder;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricGrinder.ScreenElectricGrinder;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricGrinder.TEElectricGrinder;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAutocraftingTable.ContainerAutocraftingTable;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAutocraftingTable.ScreenAutocraftingTable;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockAutocraftingTable.TEAutocraftingTable;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier.ContainerElectricPurifier;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier.ScreenElectricPurifier;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricPurifier.TEElectricPurifier;
import me.haydenb.assemblylinemachines.block.pipe.EnergyPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.FluidPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.crafting.PurifierCrafting;
import me.haydenb.assemblylinemachines.item.CrankTool;
import me.haydenb.assemblylinemachines.item.ItemGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.item.ItemHammer;
import me.haydenb.assemblylinemachines.item.ItemTiers;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ToolStirringStick;
import me.haydenb.assemblylinemachines.item.ToolStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.util.CreativeTab;
import me.haydenb.assemblylinemachines.util.FluidProperty;
import me.haydenb.assemblylinemachines.util.FluidProperty.Fluids;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ILightReader;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
	public static final CreativeTab creativeTab = new CreativeTab("assemblylinemachines");
	
	//EVENTS
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		createItem("titanium_ingot");
		createItem("titanium_nugget");
		createItem("titanium_blade_piece");
		createItem("titanium_blade", new ItemGrindingBlade(Blades.titanium));
		createItem("pure_gold_blade_piece");
		createItem("pure_gold_blade", new ItemGrindingBlade(Blades.puregold));
		
		createItem("ground_iron");
		createItem("ground_gold");
		createItem("ground_titanium");
		createItem("ground_coal");
		
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
		
		createItem("gold_gear");
		createItem("steel_gear");
		createItem("iron_gear");
		createItem("titanium_gear");
		
		createItem("empowered_coal", new ItemGearboxFuel(3200));
		createItem("gearbox_upgrade_limiter", new ItemUpgrade(false, "Gearbox will only burn fuel while action is required."));
		createItem("gearbox_upgrade_efficiency", new ItemUpgrade(false, "Gearbox will use greatly less fuel.", "Gearbox will run half as quickly."));
		createItem("gearbox_upgrade_compatability", new ItemUpgrade(false, "Gearbox can use any burnable fuel."));
		
		
		createItem("item_pipe_upgrade_stack", new ItemUpgrade(true, "Item Pipe will take larger stacks."));
		createItem("item_pipe_upgrade_filter", new ItemUpgrade(true, "Item Pipe's filter space will grow."));
		createItem("item_pipe_upgrade_redstone", new ItemUpgrade(false, "Item Pipe will get Redstone control."));
		
		createItem("upgrade_speed", new ItemUpgrade(true, "Device will operate much quicker.", "Device may use more fuel or power."));
		
		createItem("autocrafting_upgrade_sustained", new ItemUpgrade(true, new String[] {"Autocrafting Table won't use power."}, new String[] {"No other upgrades will function.", "Autocrafting Table will run slower."}));
		createItem("autocrafting_upgrade_recipes", new ItemUpgrade(true, "The Autocrafting Table will gain extra recipes."));
		
		createItem("machine_upgrade_conservation", new ItemUpgrade(true, "Certain Machines have chance to not use item."));
		createItem("wooden_stirring_stick", new ToolStirringStick(35, TemperatureResistance.COLD));
		createItem("pure_iron_stirring_stick", new ToolStirringStick(135, TemperatureResistance.HOT));
		
		
		
		createItem("titanium_sword", new SwordItem(ItemTiers.TITANIUM, 2, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_axe", new AxeItem(ItemTiers.TITANIUM, 4, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_pickaxe", new PickaxeItem(ItemTiers.TITANIUM, 0, -1.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_shovel", new ShovelItem(ItemTiers.TITANIUM, 0, -1.3f, new Item.Properties().group(creativeTab)));
		createItem("titanium_hoe", new HoeItem(ItemTiers.TITANIUM, -0.5f, new Item.Properties().group(creativeTab)));
		createItem("titanium_helmet", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("titanium_chestplate", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("titanium_leggings", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("titanium_boots", new ArmorItem(ItemTiers.TITANIUM, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("titanium_hammer", new ItemHammer(ItemTiers.TITANIUM, 8, -3.2f, new Item.Properties().group(creativeTab)));
		
		createItem("crank_sword", CrankTool.makeCrankTool(ItemTiers.CRANK, null, 3, -1.2f, new Item.Properties().group(creativeTab), 600, SwordItem.class));
		createItem("crank_axe", CrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.AXE, 5, -3.2f, new Item.Properties().group(creativeTab), 750, AxeItem.class));
		createItem("crank_pickaxe", CrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.PICKAXE, 0, -1.5f, new Item.Properties().group(creativeTab), 800, PickaxeItem.class));
		createItem("crank_shovel", CrankTool.makeCrankTool(ItemTiers.CRANK, ToolType.SHOVEL, 0, -1.3f, new Item.Properties().group(creativeTab), 650, ShovelItem.class));
		createItem("crank_hoe", CrankTool.makeCrankTool(ItemTiers.CRANK, null, -999, -0.5f, new Item.Properties().group(creativeTab), 900, HoeItem.class));
		createItem("crank_hammer", CrankTool.makeCrankTool(ItemTiers.CRANK, null, 11, -3.5f, new Item.Properties().group(creativeTab), 2600, ItemHammer.class));
		
		createItem("steel_hammer", new ItemHammer(ItemTiers.STEEL, 8, -3.5f, new Item.Properties().group(creativeTab)));
		createItem("steel_sword", new SwordItem(ItemTiers.STEEL, 2, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_axe", new AxeItem(ItemTiers.STEEL, 4, -3.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_pickaxe", new PickaxeItem(ItemTiers.STEEL, 0, -1.9f, new Item.Properties().group(creativeTab)));
		createItem("steel_shovel", new ShovelItem(ItemTiers.STEEL, 0, -1.7f, new Item.Properties().group(creativeTab)));
		createItem("steel_helmet", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.HEAD, new Item.Properties().group(creativeTab)));
		createItem("steel_chestplate", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.CHEST, new Item.Properties().group(creativeTab)));
		createItem("steel_leggings", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.LEGS, new Item.Properties().group(creativeTab)));
		createItem("steel_boots", new ArmorItem(ItemTiers.STEEL, EquipmentSlotType.FEET, new Item.Properties().group(creativeTab)));
		createItem("steel_hoe", new HoeItem(ItemTiers.STEEL, -0.5f, new Item.Properties().group(creativeTab)));
		
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
		createBlock("fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.FLUID));
		createBlock("energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.POWER));
		
		
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
		
		createBlock("basic_battery_cell", new BlockBasicBatteryCell());
		createBlock("coal_generator", new BlockCoalGenerator());
		createBlock("crankmill", new BlockCrankmill());
		
		createBlock("electric_furnace", new BlockElectricFurnace());
		createBlock("electric_purifier", new BlockElectricPurifier());
		createBlock("electric_grinder", new BlockElectricGrinder());
		createBlock("electric_fluid_mixer", new BlockElectricFluidMixer());
		
		createBlock("autocrafting_table", new BlockAutocraftingTable());
		
		event.getRegistry().registerAll(blockRegistry.values().toArray(new Block[blockRegistry.size()]));
	}
	
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		createTileEntity("hand_grinder", TEHandGrinder.class);
		createTileEntity("fluid_bath", TEFluidBath.class);
		
		createTileEntity("simple_fluid_mixer", TESimpleFluidMixer.class);
		createTileEntity("simple_grinder", TESimpleGrinder.class);
		createTileEntity("simple_crank_charger", TESimpleCrankCharger.class);
		
		createTileEntity("gearbox", TEGearbox.class);
		
		createTileEntity("fluid_tank", TEFluidTank.class, blockRegistry.get("wooden_fluid_tank"), blockRegistry.get("steel_fluid_tank"));
		
		createTileEntity("pipe_connector_item", ItemPipeConnectorTileEntity.class, blockRegistry.get("item_pipe"));
		createTileEntity("pipe_connector_fluid", FluidPipeConnectorTileEntity.class, blockRegistry.get("fluid_pipe"));
		createTileEntity("pipe_connector_energy", EnergyPipeConnectorTileEntity.class, blockRegistry.get("energy_pipe"));
		
		createTileEntity("basic_battery_cell", TEBasicBatteryCell.class);
		createTileEntity("coal_generator", TECoalGenerator.class);
		createTileEntity("crankmill", TECrankmill.class);
		
		createTileEntity("electric_furnace", TEElectricFurnace.class);
		createTileEntity("electric_purifier", TEElectricPurifier.class);
		createTileEntity("electric_grinder", TEElectricGrinder.class);
		createTileEntity("electric_fluid_mixer", TEElectricFluidMixer.class);
		
		createTileEntity("autocrafting_table", TEAutocraftingTable.class);
		
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
		createContainer("basic_battery_cell", 1056, ContainerBasicBatteryCell.class);
		
		createContainer("electric_furnace", 1057, ContainerElectricFurnace.class);
		createContainer("electric_purifier", 1058, ContainerElectricPurifier.class);
		createContainer("electric_grinder", 1059, ContainerElectricGrinder.class);
		createContainer("electric_fluid_mixer", 1060, ContainerElectricFluidMixer.class);
		
		createContainer("autocrafting_table", 1061, ContainerAutocraftingTable.class);
		
		event.getRegistry().registerAll(containerRegistry.values().toArray(new ContainerType<?>[containerRegistry.size()]));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(getBlock("steel_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("wooden_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("autocrafting_table"), RenderType.getCutout());
		
		registerScreen("simple_fluid_mixer", ContainerSimpleFluidMixer.class, ScreenSimpleFluidMixer.class); 
		registerScreen("simple_grinder", ContainerSimpleGrinder.class, ScreenSimpleGrinder.class);
		registerScreen("gearbox", ContainerGearbox.class, ScreenGearbox.class);
		registerScreen("pipe_connector_item", ItemPipeConnectorContainer.class, ItemPipeConnectorScreen.class);
		
		registerScreen("coal_generator", ContainerCoalGenerator.class, ScreenCoalGenerator.class);
		registerScreen("crankmill", ContainerCrankmill.class, ScreenCrankmill.class);
		registerScreen("basic_battery_cell", ContainerBasicBatteryCell.class, ScreenBasicBatteryCell.class);
		registerScreen("electric_furnace", ContainerElectricFurnace.class, ScreenElectricFurnace.class);
		registerScreen("electric_purifier", ContainerElectricPurifier.class, ScreenElectricPurifier.class);
		
		registerScreen("electric_grinder", ContainerElectricGrinder.class, ScreenElectricGrinder.class);
		registerScreen("electric_fluid_mixer", ContainerElectricFluidMixer.class, ScreenElectricFluidMixer.class);
		registerScreen("autocrafting_table", ContainerAutocraftingTable.class, ScreenAutocraftingTable.class);
	}
	
	@SubscribeEvent
	public static void registerCrafting(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(GrinderCrafting.GRINDER_RECIPE.toString()), GrinderCrafting.GRINDER_RECIPE);
		event.getRegistry().register(GrinderCrafting.SERIALIZER.setRegistryName("grinder"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(BathCrafting.BATH_RECIPE.toString()), BathCrafting.BATH_RECIPE);
		event.getRegistry().register(BathCrafting.SERIALIZER.setRegistryName("bath"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(PurifierCrafting.PURIFIER_RECIPE.toString()), PurifierCrafting.PURIFIER_RECIPE);
		event.getRegistry().register(PurifierCrafting.SERIALIZER.setRegistryName("purifier"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
		
		
		event.getBlockColors().register(new IBlockColor() {
			
			@Override
			public int getColor(BlockState state, ILightReader reader, BlockPos pos, int tint) {
				if(reader != null && pos != null) {
					
					
					if(state.has(BlockFluidBath.STATUS)) {
						if(state.get(BlockFluidBath.STATUS) == BathStatus.success) {
							
							TileEntity te = reader.getTileEntity(pos);
							if(te != null && te instanceof TEFluidBath) {
								
								TEFluidBath tef = (TEFluidBath) te;
								if(tef.getFluidColor() >= 0) {
									return tef.getFluidColor();
								}
							}
						}
					}
					
					
					if(state.get(FluidProperty.FLUID) == Fluids.LAVA) {
						return 0xcb3d07;
					}else {
						return BiomeColors.getWaterColor(reader, pos);
					}
				}
				
				return 0;
			}
		}, getBlock("fluid_bath"), getBlock("steel_fluid_tank"), getBlock("wooden_fluid_tank"));
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
	
	public static Block getBlock(String name) {
		return blockRegistry.get(name);
	}
	
	//TILE ENTITIES
	public static TileEntityType<?> getTileEntity(String name){
		return teRegistry.get(name);
	}
	
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
