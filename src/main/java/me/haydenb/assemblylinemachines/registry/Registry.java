package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.*;
import me.haydenb.assemblylinemachines.block.corrupt.*;
import me.haydenb.assemblylinemachines.block.energy.*;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.*;
import me.haydenb.assemblylinemachines.block.energy.BlockEntropyReactor.*;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.*;
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
import me.haydenb.assemblylinemachines.registry.datagen.BasicHarvestableTaggedBlock;
import me.haydenb.assemblylinemachines.registry.datagen.MineableBlockProvider;
import me.haydenb.assemblylinemachines.world.*;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell.EntityCorruptShellRenderFactory;
import me.haydenb.assemblylinemachines.world.rendering.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
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
	
	public static final HashMap<String, Block> blockRegistry = new HashMap<>();
	
	private static final HashMap<String, BlockEntityType<?>> teRegistry = new HashMap<>();
	private static final HashMap<String, MenuType<?>> containerRegistry = new HashMap<>();
	private static final HashMap<MenuType<?>, Integer> containerIdRegistry = new HashMap<>();
	private static final HashMap<String, MobEffect> effectRegistry = new HashMap<>();
	
	public static final ModCreativeTab creativeTab = new ModCreativeTab("assemblylinemachines");
	
	//EVENTS
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		//DISABLED DUE TO PATCHOULI PLUGIN NON-UPDATE - createItem("guidebook", new ItemGuidebook());
		
		createItem("titanium_ingot");
		createItem("titanium_nugget");
		createItem("raw_titanium");
		
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
		createItem("ground_copper");
		
		createItem("sludge");
		
		createItem("steel_ingot");
		createItem("steel_nugget");
		
		createItem("pure_iron");
		createItem("pure_gold");
		createItem("pure_titanium");
		createItem("pure_steel");
		createItem("pure_copper");
		
		createItem("steel_rod");
		
		createItem("steel_plate");
		createItem("wooden_board");
		createItem("iron_plate");
		createItem("gold_plate");
		createItem("titanium_plate");
		createItem("copper_plate");
		createItem("mystium_plate", new ItemBasicFormattedName(ChatFormatting.LIGHT_PURPLE));
		
		createItem("gold_gear");
		createItem("steel_gear");
		createItem("iron_gear");
		createItem("titanium_gear");
		createItem("copper_gear");
		
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
		createItem("mystium_blend", new ItemBasicFormattedName(ChatFormatting.LIGHT_PURPLE));
		createItem("mystium_ingot", new ItemBasicFormattedName(ChatFormatting.LIGHT_PURPLE));
		createItem("corrupted_shard", new ItemCorruptedShard());
		
		createItem("titanium_sword", new SwordItem(ToolTiers.TITANIUM, 2, -1.5f, new Item.Properties().tab(creativeTab)));
		createItem("titanium_axe", new AxeItem(ToolTiers.TITANIUM, 3, -3.5f, new Item.Properties().tab(creativeTab)));
		createItem("titanium_pickaxe", new PickaxeItem(ToolTiers.TITANIUM, 0, -1.5f, new Item.Properties().tab(creativeTab)));
		createItem("titanium_shovel", new ShovelItem(ToolTiers.TITANIUM, 0, -1.3f, new Item.Properties().tab(creativeTab)));
		createItem("titanium_hoe", new HoeItem(ToolTiers.TITANIUM, 0, -0.5f, new Item.Properties().tab(creativeTab)));
		createItem("titanium_hammer", new ItemHammer(ToolTiers.TITANIUM, 8, -3.2f, new Item.Properties().tab(creativeTab)));
		
		int crankcapacity = ConfigHolder.COMMON.crankToolMaxCranks.get();
		createItem("crank_sword", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 2, -1.5f, new Item.Properties().tab(creativeTab), crankcapacity, SwordItem.class));
		createItem("crank_axe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 3, -3.5f, new Item.Properties().tab(creativeTab), crankcapacity, AxeItem.class));
		createItem("crank_pickaxe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 0, -1.5f, new Item.Properties().tab(creativeTab), crankcapacity, PickaxeItem.class));
		createItem("crank_shovel", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 0, -1.3f, new Item.Properties().tab(creativeTab), crankcapacity, ShovelItem.class));
		createItem("crank_hoe", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 0, -0.5f, new Item.Properties().tab(creativeTab), crankcapacity, HoeItem.class));
		createItem("crank_hammer", ItemCrankTool.makeCrankTool(ToolTiers.CRANK, 8, -3.2f, new Item.Properties().tab(creativeTab), crankcapacity, ItemHammer.class));
		
		int fecapacity = ConfigHolder.COMMON.mystiumToolMaxFE.get();
		createItem("mystium_sword", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 2, -1.5f, new Item.Properties().tab(creativeTab), fecapacity, SwordItem.class));
		createItem("mystium_axe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 3, -3.5f, new Item.Properties().tab(creativeTab), fecapacity, AxeItem.class));
		createItem("mystium_pickaxe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 0, -1.5f, new Item.Properties().tab(creativeTab), fecapacity, PickaxeItem.class));
		createItem("mystium_shovel", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 0, -1.3f, new Item.Properties().tab(creativeTab), fecapacity, ShovelItem.class));
		createItem("mystium_hoe", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 0, -0.5f, new Item.Properties().tab(creativeTab), fecapacity, HoeItem.class));
		createItem("mystium_hammer", ItemMystiumTool.makePowerTool(ToolTiers.MYSTIUM, 8, -3.2f, new Item.Properties().tab(creativeTab), fecapacity, ItemHammer.class));
		
		createItem("steel_sword", new SwordItem(ToolTiers.STEEL, 2, -1.5f, new Item.Properties().tab(creativeTab)));
		createItem("steel_axe", new AxeItem(ToolTiers.STEEL, 3, -3.5f, new Item.Properties().tab(creativeTab)));
		createItem("steel_pickaxe", new PickaxeItem(ToolTiers.STEEL, 0, -1.5f, new Item.Properties().tab(creativeTab)));
		createItem("steel_shovel", new ShovelItem(ToolTiers.STEEL, 0, -1.3f, new Item.Properties().tab(creativeTab)));
		createItem("steel_hoe", new HoeItem(ToolTiers.STEEL, 0, -0.5f, new Item.Properties().tab(creativeTab)));
		createItem("steel_hammer", new ItemHammer(ToolTiers.STEEL, 8, -3.2f, new Item.Properties().tab(creativeTab)));
		
		createItem("steel_helmet", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.HEAD, new Item.Properties().tab(creativeTab)));
		createItem("steel_chestplate", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.CHEST, new Item.Properties().tab(creativeTab)));
		createItem("steel_leggings", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.LEGS, new Item.Properties().tab(creativeTab)));
		createItem("steel_boots", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.FEET, new Item.Properties().tab(creativeTab)));
		
		createItem("titanium_helmet", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.HEAD, new Item.Properties().tab(creativeTab)));
		createItem("titanium_chestplate", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.CHEST, new Item.Properties().tab(creativeTab)));
		createItem("titanium_leggings", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.LEGS, new Item.Properties().tab(creativeTab)));
		createItem("titanium_boots", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.FEET, new Item.Properties().tab(creativeTab)));
		
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
		createItem("raw_chromium");
		
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
		
		createItem("miniature_black_hole", new ItemBasicFormattedName(ChatFormatting.AQUA));
		createItem("singularity", new ItemBasicFormattedName(ChatFormatting.DARK_AQUA));
		createItem("empowered_convection_component");
		
		createItem("fertilizer", new ItemFertilizer(1));
		createItem("enhanced_fertilizer", new ItemFertilizer(3));
		createItem("ultimate_fertilizer", new ItemFertilizer(5));
		createItem("enhanced_battery");
		createItem("microprocessor");
		createItem("energy_harness");
		
		createItem("ground_chromium");
		
		createItem("attuned_titanium_ingot", new ItemBasicFormattedName(ChatFormatting.BLUE));
		createItem("attuned_titanium_plate", new ItemBasicFormattedName(ChatFormatting.BLUE));
		createItem("purifier_upgrade_enhanced", new ItemUpgrade(false, "Purifier can process more recipes.", "Increases power consumption."));
		
		createItem("nether_star_shard", new ItemBasicFormattedName(ChatFormatting.GOLD));
		
		createItem("entropy_reactor_upgrade_capacity", new ItemUpgrade(true, "Entropy Reactor has a higher capacity."));
		createItem("entropy_reactor_upgrade_cycle_delayer", new ItemUpgrade(true, "Entropy Reactor waits longer to clear capacity."));
		createItem("entropy_reactor_upgrade_variety", new ItemUpgrade(false, "Higher Variety has greater performance.", "Lower Variety has worsened performance."));
		
		createItem("semi_dense_neutron_matter", new ItemReactorOutput("§7Low-Quality"));
		createItem("quark_matter", new ItemReactorOutput("§9Medium-Quality"));
		createItem("strange_matter", new ItemReactorOutput("§aHigh-Quality"));
		
		createItem("corrupt_shell_spawn_egg", new SpawnEggItem(EntityCorruptShell.CORRUPT_SHELL, 0x005f85, 0x22a1d4, new Item.Properties().tab(creativeTab)));
		createItem("galactic_flesh");
		createItem("reality_crystal");
		
		createItem("electrified_netherite_blend");
		createItem("ground_netherite");
		
		//1.17.1 CHANGE: Fluid registration is segmented due to build issues. See FluidRegistry.class.
		FluidRegistry.registerBuckets();
		
		for(String i : itemRegistry.keySet()) {
			event.getRegistry().register(itemRegistry.get(i));
		}
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		createBlock("titanium_ore", Material.STONE, 3f, 15f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		createBlock("deepslate_titanium_ore", Material.STONE, 4f, 20f, SoundType.DEEPSLATE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		
		createBlock("titanium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		
		createBlock("hand_grinder", new BlockHandGrinder(), true);
		createBlock("fluid_bath", new BlockFluidBath(), true);
		
		createBlock("crank", new BlockCrank(), true);
		createBlock("gearbox", new BlockGearbox(), true);
		
		createBlock("simple_crank_charger", new BlockSimpleCrankCharger(), true);
		createBlock("simple_fluid_mixer", new BlockSimpleFluidMixer(), true);
		createBlock("simple_grinder", new BlockSimpleGrinder(), true);
		
		createBlock("item_pipe", new PipeBase<IItemHandler>(() -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Type.ITEM), true);
		createBlock("fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.BASIC_FLUID), true);
		createBlock("advanced_fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.ADVANCED_FLUID), true);
		createBlock("energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.BASIC_POWER), true);
		
		createBlock("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT), false);
		createItem("steel_fluid_tank", new BlockItemFluidTank(Registry.getBlock("steel_fluid_tank")));
		createBlock("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD), false);
		createItem("wooden_fluid_tank", new BlockItemFluidTank(Registry.getBlock("wooden_fluid_tank")));
		
		createBlock("silt", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("silt_brick", Material.STONE, 4f, 12f, SoundType.STONE, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("slab_silt_brick", new SlabBlock(Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);
		createBlock("stair_silt_brick", new StairBlock(new Supplier<BlockState>() {

			@Override
			public BlockState get() {
				return Registry.getBlock("silt_brick").defaultBlockState();
			}
			
		}, Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);
		
		createBlock("steel_block", Material.METAL, 7f, 30f, SoundType.METAL, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		
		createBlock("black_granite", new BlockBlackGranite(), true);
		createBlock("smooth_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("brick_black_granite", Material.STONE, 3f, 9f,  SoundType.STONE, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("chiselled_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("pillar_black_granite", new RotatedPillarBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
		createBlock("slab_black_granite", new SlabBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
		createBlock("stair_black_granite", new StairBlock(new Supplier<BlockState>() {

			@Override
			public BlockState get() {
				return Registry.getBlock("smooth_black_granite").defaultBlockState();
			}
			
		}, Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
		
		createBlock("silt_iron", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("silt_gold", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("silt_titanium", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("silt_copper", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_STONE_TOOL, true);
		
		createBlock("basic_battery_cell", new BlockBatteryCell(BatteryCellTiers.BASIC), true);
		createBlock("advanced_battery_cell", new BlockBatteryCell(BatteryCellTiers.ADVANCED), true);
		createBlock("coal_generator", new BlockCoalGenerator(), true);
		createBlock("crankmill", new BlockCrankmill(), true);
		
		createBlock("chromium_ore", Material.STONE, 3f, 15f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, MineableBlockProvider.NEEDS_NETHERITE_TOOL, true);
		createBlock("chromium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		
		createBlock("electric_furnace", new BlockElectricFurnace(), true);
		createBlock("electric_purifier", new BlockElectricPurifier(), true);
		createBlock("electric_grinder", new BlockElectricGrinder(), true);
		createBlock("electric_fluid_mixer", new BlockElectricFluidMixer(), true);
		createBlock("alloy_smelter", new BlockAlloySmelter(), true);
		
		createBlock("autocrafting_table", new BlockAutocraftingTable(), true);
		
		createBlock("naphtha_fire", new BlockNaphthaFire(), false);
		
		createBlock("pump", new BlockPump(), true);
		createBlock("pumpshaft", new BlockPumpshaft(), true);
		
		createBlock("mystium_block", Material.METAL, 11f, 80f, SoundType.METAL, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, false);
		createItem("mystium_block", new ItemBlockFormattedName(Registry.getBlock("mystium_block"), ChatFormatting.LIGHT_PURPLE));
		
		createBlock("mystium_fluid_tank", new BlockFluidTank(250000, TemperatureResistance.HOT), false);
		createItem("mystium_fluid_tank", new BlockItemFluidTank(Registry.getBlock("mystium_fluid_tank")));
		
		createBlock("tool_charger", new BlockToolCharger(), true);
		
		createBlock("adv_energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.ADVANCED_POWER), true);
		createBlock("smoldering_stone", Material.STONE, 30f, 300f, SoundType.STONE, false, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, true);
		createBlock("naphtha_turbine", new BlockNaphthaTurbine(), true);
		
		createBlock("compressed_crafting_table", Material.WOOD, 2f, 10f, SoundType.WOOD, false, BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_STONE_TOOL, true);
		
		createBlock("refinery", new BlockRefinery(), true);
		createBlock("refinery_attachment_separation", new BlockSeparationAddon(), true);
		createBlock("refinery_attachment_addition", new BlockAdditionAddon(), true);
		createBlock("refinery_attachment_halogen", new BlockHalogenAddon(), true);
		createBlock("refinery_attachment_cracking", new BlockCrackingAddon(), true);
		
		createBlock("fluid_router", new BlockFluidRouter(), true);
		createBlock("interactor", new BlockInteractor(), true);
		createBlock("vacuum_hopper", new BlockVacuumHopper(), true);
		
		createBlock("bottomless_storage_unit", new BlockBottomlessStorageUnit(), false);
		createItem("bottomless_storage_unit", new BlockItemBottomlessStorageUnit(Registry.getBlock("bottomless_storage_unit")));
		
		createBlock("combustion_generator", new BlockFluidGenerator(FluidGeneratorTypes.COMBUSTION), true);
		createBlock("geothermal_generator", new BlockFluidGenerator(FluidGeneratorTypes.GEOTHERMAL), true);
		
		createBlock("experience_hopper", new BlockExperienceHopper(), true);
		createBlock("powered_spawner", new BlockPoweredSpawner(), true);
		createBlock("experience_mill", new BlockExperienceMill(), true);
		
		createBlock("quarry", new BlockQuarry(), true);
		createBlock("quarry_speed_addon", new BlockSpeedQuarryAddon(), true);
		createBlock("quarry_fortune_addon", new BlockFortuneVoidQuarryAddon(), true);
		createBlock("quarry_void_addon", new BlockFortuneVoidQuarryAddon(), true);
		
		createBlock("mystium_farmland", new BlockMystiumFarmland(), false);
		
		createBlock("lumber_mill", new BlockLumberMill(), true);
		
		createBlock("quantum_link", new BlockQuantumLink(), true);
		createBlock("metal_shaper", new BlockMetalShaper(), true);
		
		createBlock("entropy_reactor_block", new BlockEntropyReactor(), true);
		createBlock("entropy_reactor_core", new BlockEntropyReactorCore(), true);
		
		createBlock("corrupt_dirt", new CorruptBlock(Block.Properties.of(Material.DIRT).sound(SoundType.GRAVEL), BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_grass", new CorruptGrassBlock(), true);
		createBlock("corrupt_sand", new CorruptSandBlock(), true);
		createBlock("corrupt_stone", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		
		createBlock("corrupting_basin", new BlockCorruptingBasin(), true);
		
		//1.17.1 CHANGE: Fluid registration is segmented due to build issues. See FluidRegistry.class.
		FluidRegistry.registerFluidBlocks();
		
		event.getRegistry().registerAll(blockRegistry.values().toArray(new Block[blockRegistry.size()]));
	}
	
	/*
	1.17.1 CHANGE: Due to issues related to building fluids, fluid registration is handled through a DeferredRegister in FluidRegistry.class.
	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Fluid> event) {
		
		//FLUIDS - COPIED FROM BOTTOM OF #registerBlocks()
		//FluidRegistration.buildAndRegister("oil", new FluidOil(true), new FluidOil(false), true, new FluidOilBlock(new FluidOil(true)));
		//FluidRegistration.buildAndRegister("condensed_void", new FluidCondensedVoid(true), new FluidCondensedVoid(false), true, new FluidCondensedVoidBlock(new FluidCondensedVoid(true)));
		//FluidRegistration.buildAndRegister("naphtha", new FluidNaphtha(true), new FluidNaphtha(false), true, new FluidNaphthaBlock(new FluidNaphtha(true)));
		//FluidRegistration.buildAndRegister("gasoline", new FluidOilProduct("gasoline", true), new FluidOilProduct("gasoline", false), true, new FluidOilProductBlock(new FluidOilProduct("gasoline", true)));
		//FluidRegistration.bildAndRegister("diesel", new FluidOilProduct("diesel", true), new FluidOilProduct("diesel", false), true, new FluidOilProductBlock(new FluidOilProduct("diesel", true)));
		//FluidRegistration.buildAndRegister("liquid_experience", 35, false, true, ALMFluid.LIQUID_EXPERIENCE, Material.WATER);
		
		//FluidRegistration.buildAndRegister("ethane", new GaseousFluid("ethane", true, 0x135f0f), null, false, null);
		//FluidRegistration.buildAndRegister("ethylene", new GaseousFluid("ethylene", true, 0xb3aac3), null, false, null);
		//FluidRegistration.buildAndRegister("propane", new GaseousFluid("propane", true, 0x5f0f1c), null, false, null);
		//FluidRegistration.buildAndRegister("propylene", new GaseousFluid("propylene", true, 0x290d55), null, false, null);
		 * 
		//All Fluids are handled at bottom of registerBlocks, to handle the _block and _bucket versions of fluids.
		event.getRegistry().registerAll(fluidRegistry.values().toArray(new Fluid[fluidRegistry.size()]));
	}
	*/
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		createBlockEntity("hand_grinder", TEHandGrinder.class);
		createBlockEntity("fluid_bath", TEFluidBath.class);
		
		createBlockEntity("simple_fluid_mixer", TESimpleFluidMixer.class);
		createBlockEntity("simple_grinder", TESimpleGrinder.class);
		createBlockEntity("simple_crank_charger", TESimpleCrankCharger.class);
		
		createBlockEntity("gearbox", TEGearbox.class);
		
		createBlockEntity("fluid_tank", TEFluidTank.class, blockRegistry.get("wooden_fluid_tank"), blockRegistry.get("steel_fluid_tank"), blockRegistry.get("mystium_fluid_tank"));
		
		createBlockEntity("pipe_connector_item", ItemPipeConnectorTileEntity.class, blockRegistry.get("item_pipe"));
		createBlockEntity("pipe_connector_fluid", FluidPipeConnectorTileEntity.class, blockRegistry.get("fluid_pipe"), blockRegistry.get("advanced_fluid_pipe"));
		createBlockEntity("pipe_connector_energy", EnergyPipeConnectorTileEntity.class, blockRegistry.get("energy_pipe"), blockRegistry.get("adv_energy_pipe"));
		
		createBlockEntity("basic_battery_cell", TEBasicBatteryCell.class);
		createBlockEntity("advanced_battery_cell", TEAdvancedBatteryCell.class);
		
		createBlockEntity("coal_generator", TECoalGenerator.class);
		createBlockEntity("crankmill", TECrankmill.class);
		
		createBlockEntity("electric_furnace", TEElectricFurnace.class);
		createBlockEntity("electric_purifier", TEElectricPurifier.class);
		createBlockEntity("electric_grinder", TEElectricGrinder.class);
		createBlockEntity("electric_fluid_mixer", TEElectricFluidMixer.class);
		createBlockEntity("alloy_smelter", TEAlloySmelter.class);
		
		createBlockEntity("autocrafting_table", TEAutocraftingTable.class);
		createBlockEntity("pump", TEPump.class);
		createBlockEntity("tool_charger", TEToolCharger.class);
		createBlockEntity("refinery", TERefinery.class);
		createBlockEntity("fluid_router", TEFluidRouter.class);
		createBlockEntity("interactor", TEInteractor.class);
		createBlockEntity("vacuum_hopper", TEVacuumHopper.class);
		createBlockEntity("bottomless_storage_unit", TEBottomlessStorageUnit.class);
		createBlockEntity("fluid_generator", TEFluidGenerator.class, blockRegistry.get("geothermal_generator"), blockRegistry.get("combustion_generator"));
		createBlockEntity("experience_hopper", TEExperienceHopper.class);
		
		createBlockEntity("powered_spawner", TEPoweredSpawner.class);
		createBlockEntity("experience_mill", TEExperienceMill.class);
		createBlockEntity("quarry", TEQuarry.class);
		createBlockEntity("lumber_mill", TELumberMill.class);
		
		createBlockEntity("quantum_link", TEQuantumLink.class);
		
		createBlockEntity("metal_shaper", TEMetalShaper.class);
		
		createBlockEntity("entropy_reactor", TEEntropyReactor.class, blockRegistry.get("entropy_reactor_block"));
		createBlockEntity("entropy_reactor_slave", TEEntropyReactorSlave.class, blockRegistry.get("entropy_reactor_block"));
		
		createBlockEntity("corrupting_basin", TECorruptingBasin.class);
		
		event.getRegistry().registerAll(teRegistry.values().toArray(new BlockEntityType<?>[teRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
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
		
		event.getRegistry().registerAll(containerRegistry.values().toArray(new MenuType<?>[containerRegistry.size()]));
		
	}
	
	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		
		event.getRegistry().register(EntityCorruptShell.CORRUPT_SHELL.setRegistryName("corrupt_shell"));
		SpawnPlacements.register(EntityCorruptShell.CORRUPT_SHELL, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMobSpawnRules);
	}
	
	@SubscribeEvent
	public static void registerEntAttributes(EntityAttributeCreationEvent event) {
		
		event.put(EntityCorruptShell.CORRUPT_SHELL, EntityCorruptShell.registerAttributeMap().build());
	}
	
	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<MobEffect> event) {
		
		createEffect("entropy_poisoning", new EffectEntropyPoisoning());
		createEffect("deep_burn", new EffectDeepBurn());
		
		event.getRegistry().registerAll(effectRegistry.values().toArray(new MobEffect[effectRegistry.size()]));
	}
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(getBlock("steel_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("wooden_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("mystium_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("autocrafting_table"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("geothermal_generator"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("naphtha_fire"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("powered_spawner"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("entropy_reactor_block"), RenderType.cutout());
		
		ItemBlockRenderTypes.setRenderLayer(getFluid("naphtha"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("naphtha_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("diesel"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("diesel_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("gasoline"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("gasoline_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_experience"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_experience_flowing"), RenderType.translucent());
		
		
		BlockEntityRenderers.register((BlockEntityType<TEFluidTank>)getBlockEntity("fluid_tank"), new TankTER());
		BlockEntityRenderers.register((BlockEntityType<TEPoweredSpawner>)getBlockEntity("powered_spawner"), new PoweredSpawnerTER());
		BlockEntityRenderers.register((BlockEntityType<TEQuantumLink>)getBlockEntity("quantum_link"), new QuantumLinkTER());
		
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
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(EntityCorruptShell.CORRUPT_SHELL, new EntityCorruptShellRenderFactory());
	}
	
	@SubscribeEvent
	public static void registerCrafting(RegistryEvent.Register<RecipeSerializer<?>> event) {
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(GrinderCrafting.GRINDER_RECIPE.toString()), GrinderCrafting.GRINDER_RECIPE);
		event.getRegistry().register(GrinderCrafting.SERIALIZER.setRegistryName("grinder"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(BathCrafting.BATH_RECIPE.toString()), BathCrafting.BATH_RECIPE);
		event.getRegistry().register(BathCrafting.SERIALIZER.setRegistryName("bath"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(PurifierCrafting.PURIFIER_RECIPE.toString()), PurifierCrafting.PURIFIER_RECIPE);
		event.getRegistry().register(PurifierCrafting.SERIALIZER.setRegistryName("purifier"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(AlloyingCrafting.ALLOYING_RECIPE.toString()), AlloyingCrafting.ALLOYING_RECIPE);
		event.getRegistry().register(AlloyingCrafting.SERIALIZER.setRegistryName("alloying"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(FluidInGroundRecipe.FIG_RECIPE.toString()), FluidInGroundRecipe.FIG_RECIPE);
		event.getRegistry().register(FluidInGroundRecipe.SERIALIZER.setRegistryName("fluid_in_ground"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(RefiningCrafting.REFINING_RECIPE.toString()), RefiningCrafting.REFINING_RECIPE);
		event.getRegistry().register(RefiningCrafting.SERIALIZER.setRegistryName("refining"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE.toString()), EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE);
		event.getRegistry().register(EnchantmentBookCrafting.SERIALIZER.setRegistryName("enchantment_book"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(LumberCrafting.LUMBER_RECIPE.toString()), LumberCrafting.LUMBER_RECIPE);
		event.getRegistry().register(LumberCrafting.SERIALIZER.setRegistryName("lumber"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(MetalCrafting.METAL_RECIPE.toString()), MetalCrafting.METAL_RECIPE);
		event.getRegistry().register(MetalCrafting.SERIALIZER.setRegistryName("metal"));
		
		net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(EntropyReactorCrafting.ERO_RECIPE.toString()), EntropyReactorCrafting.ERO_RECIPE);
		event.getRegistry().register(EntropyReactorCrafting.SERIALIZER.setRegistryName("entropy_reactor"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
		
		
		event.getBlockColors().register(new BlockColor() {

			@Override
			public int getColor(BlockState state, BlockAndTintGetter reader, BlockPos pos, int tint) {
				if(reader != null && pos != null && reader.getBlockEntity(pos) instanceof TEFluidBath) {
					
					TEFluidBath te = (TEFluidBath) reader.getBlockEntity(pos);
					
					return te.getFluidColor(reader, pos);
					
				}
				
				return 0;
			}
		}, getBlock("fluid_bath"));
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
		event.getItemColors().register(new ItemColor() {
			
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
	private static void createEffect(String name, MobEffect effect) {
		effect.setRegistryName(name);
		effectRegistry.put(name, effect);
	}
	
	public static MobEffect getEffect(String name) {
		return effectRegistry.get(name);
	}
	
	//ITEMS
	private static void createItem(String name) {
		
		Item.Properties properties = new Item.Properties().tab(creativeTab);
		createItem(name, properties);
	}
	static void createItem(String name, Item.Properties properties) {
		Item i = new Item(properties);
		createItem(name, i);
	}
	
	static void createItem(String name, Item item) {
		item.setRegistryName(name);
		itemRegistry.put(name, item);
	}
	
	public static Item getItem(String name) {
		return itemRegistry.get(name);
	}
	
	
	//BLOCKS
	private static void createBlock(String name, Material material, float hardness, float resistance, SoundType sound, boolean requireToolToDrop, Named<Block> type, Named<Block> level, boolean item) {
		Block.Properties properties = Block.Properties.of(material).strength(hardness, resistance).sound(sound);
		if(requireToolToDrop) {
			properties = properties.requiresCorrectToolForDrops();
		}
		createBlock(name, properties, type, level, item);
		
	}
	
	private static void createBlock(String name, Block.Properties properties, Named<Block> type, Named<Block> level, boolean item) {
		Block b;
		if(type != null && level != null) {
			b = new BasicHarvestableTaggedBlock(properties, type, level);
		}else {
			b = new Block(properties);
		}
		createBlock(name, b, item);
	}
	
	static void createBlock(String name, Block block, boolean item) {
		block.setRegistryName(name);
		blockRegistry.put(name, block);
		if(item) {
			createItem(name, new BlockItem(block, new Item.Properties().tab(creativeTab)));
		}
		
	}
	
	public static Block getBlock(String name) {
		return blockRegistry.get(name);
	}
	
	
	//FLUIDS
	//Fluid Registration is handled in a separate class.
	public static Fluid getFluid(String name) {
		return ForgeRegistries.FLUIDS.getValue(new ResourceLocation("assemblylinemachines", name));
	}
	
	//TILE ENTITIES
	public static BlockEntityType<?> getBlockEntity(String name){
		return teRegistry.get(name);
	}
	
	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz, Block... blocks){
		teRegistry.put(name, BlockEntityType.Builder.of((pos, state) -> {

			T inst;
			try {
				inst = clazz.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				inst = null;
				e.printStackTrace();
			}
			return inst;
		}, blocks).build(null).setRegistryName(name));
	
	}
	
	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz) {
		createBlockEntity(name, clazz, Registry.getBlock(name));
	}
	
	//CONTAINERS, CONTAINER IDS, SCREENS
	public static MenuType<?> getContainerType(String name){
		return containerRegistry.get(name);
	}
	
	public static Integer getContainerId(String name){
		return containerIdRegistry.get(containerRegistry.get(name));
	}
	
	public static <T extends AbstractContainerMenu> void createContainer(String name, int id, Class<T> clazz) {
		containerRegistry.put(name, IForgeContainerType.create(new IContainerFactory<T>() {

			@Override
			public T create(int windowId, Inventory inv, FriendlyByteBuf data) {
				try {
					return clazz.getConstructor(int.class, Inventory.class, FriendlyByteBuf.class).newInstance(windowId, inv, data);
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
	public static <T extends AbstractContainerMenu, X extends Screen & MenuAccess<T>> void registerScreen(String name, Class<T> ctc, Class<X> scc) {
		MenuScreens.register((MenuType<T>) getContainerType(name), new MenuScreens.ScreenConstructor<T, X>() {

			@Override
			public X create(T p_create_1_, Inventory p_create_2_, Component p_create_3_) {
				try {
					return scc.getConstructor(ctc, Inventory.class, Component.class).newInstance(p_create_1_, p_create_2_, p_create_3_);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}
}
