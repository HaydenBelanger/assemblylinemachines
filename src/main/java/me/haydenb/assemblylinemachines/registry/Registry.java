package me.haydenb.assemblylinemachines.registry;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.automation.*;
import me.haydenb.assemblylinemachines.block.automation.BlockAutocraftingTable.*;
import me.haydenb.assemblylinemachines.block.automation.BlockExperienceHopper.TEExperienceHopper;
import me.haydenb.assemblylinemachines.block.automation.BlockInteractor.*;
import me.haydenb.assemblylinemachines.block.automation.BlockPoweredSpawner.*;
import me.haydenb.assemblylinemachines.block.automation.BlockVacuumHopper.TEVacuumHopper;
import me.haydenb.assemblylinemachines.block.chaosplane.*;
import me.haydenb.assemblylinemachines.block.chaosplane.CorruptBlock.*;
import me.haydenb.assemblylinemachines.block.chaosplane.CorruptTallGrassBlock.*;
import me.haydenb.assemblylinemachines.block.energy.*;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.*;
import me.haydenb.assemblylinemachines.block.energy.BlockEntropyReactor.*;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockGearbox.*;
import me.haydenb.assemblylinemachines.block.energy.BlockSimpleCrankCharger.TESimpleCrankCharger;
import me.haydenb.assemblylinemachines.block.energy.BlockToolCharger.TEToolCharger;
import me.haydenb.assemblylinemachines.block.fluid.*;
import me.haydenb.assemblylinemachines.block.fluid.ALMFluid.ALMFluidBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidCondensedVoid.FluidCondensedVoidBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidDarkEnergy.FluidDarkEnergyBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidGlacierWater.FluidGlacierWaterBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidNaphtha.BlockNaphthaFire;
import me.haydenb.assemblylinemachines.block.fluid.FluidNaphtha.FluidNaphthaBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidOil.FluidOilBlock;
import me.haydenb.assemblylinemachines.block.fluid.FluidOilProduct.FluidOilProductBlock;
import me.haydenb.assemblylinemachines.block.fluidutility.*;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockCorruptingBasin.*;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockExperienceSiphon.TEExperienceSiphon;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockFluidRouter.*;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockFluidTank.TEFluidTank;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockRefinery.*;
import me.haydenb.assemblylinemachines.block.fluidutility.BlockRefinery.BlockRefineryAddon.RefineryAddon;
import me.haydenb.assemblylinemachines.block.machines.*;
import me.haydenb.assemblylinemachines.block.machines.BlockAlloySmelter.*;
import me.haydenb.assemblylinemachines.block.machines.BlockElectricFluidMixer.*;
import me.haydenb.assemblylinemachines.block.machines.BlockElectricFurnace.*;
import me.haydenb.assemblylinemachines.block.machines.BlockElectricGrinder.*;
import me.haydenb.assemblylinemachines.block.machines.BlockElectricPurifier.*;
import me.haydenb.assemblylinemachines.block.machines.BlockExperienceMill.*;
import me.haydenb.assemblylinemachines.block.machines.BlockLumberMill.*;
import me.haydenb.assemblylinemachines.block.machines.BlockMetalShaper.*;
import me.haydenb.assemblylinemachines.block.machines.BlockPump.TEPump;
import me.haydenb.assemblylinemachines.block.machines.BlockQuarry.*;
import me.haydenb.assemblylinemachines.block.machines.BlockQuarryAddon.BlockFortuneVoidQuarryAddon;
import me.haydenb.assemblylinemachines.block.machines.BlockQuarryAddon.BlockSpeedQuarryAddon;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleFluidMixer.*;
import me.haydenb.assemblylinemachines.block.machines.BlockSimpleGrinder.*;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite.BlockBlackGranitePillar;
import me.haydenb.assemblylinemachines.block.misc.BlockMystiumFarmland;
import me.haydenb.assemblylinemachines.block.pipe.BlockPipe;
import me.haydenb.assemblylinemachines.block.pipe.PipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.PipeConnectorTileEntity.PipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.PipeConnectorTileEntity.PipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeType;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.TransmissionType;
import me.haydenb.assemblylinemachines.block.rudimentary.BlockFluidBath;
import me.haydenb.assemblylinemachines.block.rudimentary.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.block.rudimentary.BlockHandGrinder;
import me.haydenb.assemblylinemachines.block.rudimentary.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.block.rudimentary.BlockHandGrinder.TEHandGrinder;
import me.haydenb.assemblylinemachines.block.utility.BlockBottomlessStorageUnit;
import me.haydenb.assemblylinemachines.block.utility.BlockBottomlessStorageUnit.*;
import me.haydenb.assemblylinemachines.block.utility.BlockQuantumLink;
import me.haydenb.assemblylinemachines.block.utility.BlockQuantumLink.*;
import me.haydenb.assemblylinemachines.client.ter.*;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.item.*;
import me.haydenb.assemblylinemachines.item.IGearboxFuel.ItemGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemPowerTool.EnchantmentOverclock;
import me.haydenb.assemblylinemachines.item.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.item.ItemTiers.ArmorTiers;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolTiers;
import me.haydenb.assemblylinemachines.plugins.PluginPatchouli;
import me.haydenb.assemblylinemachines.registry.Utils.IToolWithCharge;
import me.haydenb.assemblylinemachines.registry.datagen.*;
import me.haydenb.assemblylinemachines.registry.datagen.TagMaster.DataProviderContainer;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell.EntityCorruptShellRenderFactory;
import me.haydenb.assemblylinemachines.world.effects.*;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
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
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Registry {
	
	//REGISTRY MAPS
	private static final TreeMap<String, Item> MOD_ITEM_REGISTRY = new TreeMap<>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
		
	private static final ConcurrentHashMap<String, Block> MOD_BLOCK_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, BlockEntityType<?>> MOD_BLOCKENTITY_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Pair<MenuType<?>, Integer>> MOD_CONTAINER_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, MobEffect> MOD_EFFECT_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, SoundEvent> MOD_SOUND_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, ForgeFlowingFluid> MOD_FLUID_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Enchantment> MOD_ENCHANTMENT_REGISTRY = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Pair<RecipeType<?>, ForgeRegistryEntry<RecipeSerializer<?>>>> MOD_CRAFTING_REGISTRY = new ConcurrentHashMap<>();
	
	//MOD CREATIVE TAB & DEFAULT PROPERTIES
	public static final ModCreativeTab CREATIVE_TAB = new ModCreativeTab(AssemblyLineMachines.MODID);
	
	//BOTH-SIDED REGISTRATIONS - ITEMS, BLOCKS, TEs, CONTAINERS, ENTITIES, POTIONS, CRAFTING, CARVERS, SOUNDS
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		if(PluginPatchouli.get().isPatchouliInstalled()) {
			createItem("guidebook", new ItemGuidebook());
		}
		createItem("titanium_ingot", "titanium_nugget", "raw_titanium");
		
		createItem("titanium_blade_piece", "pure_gold_blade_piece", "steel_blade_piece");
		createItem("titanium_blade", new Item.Properties().durability(Blade.TITANIUM.uses).tab(Registry.CREATIVE_TAB));
		createItem("pure_gold_blade", new Item.Properties().durability(Blade.PUREGOLD.uses).tab(Registry.CREATIVE_TAB));
		createItem("steel_blade", new Item.Properties().durability(Blade.STEEL.uses).tab(Registry.CREATIVE_TAB));
		
		createItem("ground_iron", "ground_gold", "ground_titanium", "ground_coal", "ground_charcoal", "ground_copper", "ground_lapis_lazuli");
		createItem("sludge");
		
		createItem("steel_ingot", "steel_nugget", "steel_rod");
		
		createItem("pure_iron", "pure_gold", "pure_titanium", "pure_steel", "pure_copper");
		
		createItem("steel_plate", "wooden_board", "iron_plate", "gold_plate", "titanium_plate", "copper_plate", "mystium_plate");
		
		createItem("gold_gear", "steel_gear", "iron_gear", "titanium_gear", "copper_gear");
		
		createItem("empowered_coal", new ItemGearboxFuel(3200));
		createItem("empowered_fuel", new ItemGearboxFuel(6400));
		
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
		
		createItem("wooden_stirring_stick", new ItemStirringStick(TemperatureResistance.COLD, true, 1100));
		createItem("pure_iron_stirring_stick", new ItemStirringStick(TemperatureResistance.HOT, false, 2200));
		createItem("steel_stirring_stick", new ItemStirringStick(TemperatureResistance.HOT, false, 4900));
		
		createItem("mystium_dowsing_rod", new ItemDowsingRod());
		
		createItem("mystium_blend", "mystium_ingot");
		createItem("corrupted_shard", new ItemCorruptedShard());
		
		createItem("titanium_sword", new SwordItem(ToolTiers.TITANIUM, 2, -1.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_axe", new AxeItem(ToolTiers.TITANIUM, 3, -3.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_pickaxe", new PickaxeItem(ToolTiers.TITANIUM, 0, -1.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_shovel", new ShovelItem(ToolTiers.TITANIUM, 0, -1.3f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_hoe", new HoeItem(ToolTiers.TITANIUM, 0, -0.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_hammer", new ItemHammer(ToolTiers.TITANIUM, 8, -3.2f, new Item.Properties().tab(CREATIVE_TAB)));
		
		createItem("crank_sword", new ItemPowerTool<TieredItem>(new SwordItem(ToolTiers.CRANK, 2, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("crank_axe", new ItemPowerTool<TieredItem>(new AxeItem(ToolTiers.CRANK, 3, -3.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("crank_pickaxe", new ItemPowerTool<TieredItem>(new PickaxeItem(ToolTiers.CRANK, 0, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("crank_shovel", new ItemPowerTool<TieredItem>(new ShovelItem(ToolTiers.CRANK, 0, -1.3f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("crank_hoe", new ItemPowerTool<TieredItem>(new HoeItem(ToolTiers.CRANK, 0, -0.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("crank_hammer", new ItemPowerTool<TieredItem>(new ItemHammer(ToolTiers.CRANK, 8, -3.2f, new Item.Properties().tab(CREATIVE_TAB))));
		
		createItem("mystium_sword", new ItemPowerTool<TieredItem>(new SwordItem(ToolTiers.MYSTIUM, 2, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("mystium_axe", new ItemPowerTool<TieredItem>(new AxeItem(ToolTiers.MYSTIUM, 3, -3.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("mystium_pickaxe", new ItemPowerTool<TieredItem>(new PickaxeItem(ToolTiers.MYSTIUM, 0, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("mystium_shovel", new ItemPowerTool<TieredItem>(new ShovelItem(ToolTiers.MYSTIUM, 0, -1.3f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("mystium_hoe", new ItemPowerTool<TieredItem>(new HoeItem(ToolTiers.MYSTIUM, 0, -0.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("mystium_hammer", new ItemPowerTool<TieredItem>(new ItemHammer(ToolTiers.MYSTIUM, 8, -3.2f, new Item.Properties().tab(CREATIVE_TAB))));
		
		createItem("novasteel_sword", new ItemPowerTool<TieredItem>(new SwordItem(ToolTiers.NOVASTEEL, 2, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("novasteel_axe", new ItemPowerTool<TieredItem>(new AxeItem(ToolTiers.NOVASTEEL, 3, -3.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("novasteel_pickaxe", new ItemPowerTool<TieredItem>(new PickaxeItem(ToolTiers.NOVASTEEL, 0, -1.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("novasteel_shovel", new ItemPowerTool<TieredItem>(new ShovelItem(ToolTiers.NOVASTEEL, 0, -1.3f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("novasteel_hoe", new ItemPowerTool<TieredItem>(new HoeItem(ToolTiers.NOVASTEEL, 0, -0.5f, new Item.Properties().tab(CREATIVE_TAB))));
		createItem("novasteel_hammer", new ItemPowerTool<TieredItem>(new ItemHammer(ToolTiers.NOVASTEEL, 8, -3.2f, new Item.Properties().tab(CREATIVE_TAB))));
		
		createItem("steel_sword", new SwordItem(ToolTiers.STEEL, 2, -1.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_axe", new AxeItem(ToolTiers.STEEL, 3, -3.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_pickaxe", new PickaxeItem(ToolTiers.STEEL, 0, -1.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_shovel", new ShovelItem(ToolTiers.STEEL, 0, -1.3f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_hoe", new HoeItem(ToolTiers.STEEL, 0, -0.5f, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_hammer", new ItemHammer(ToolTiers.STEEL, 8, -3.2f, new Item.Properties().tab(CREATIVE_TAB)));
		
		createItem("steel_helmet", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.HEAD, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_chestplate", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.CHEST, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_leggings", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.LEGS, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("steel_boots", new ArmorItem(ArmorTiers.STEEL, EquipmentSlot.FEET, new Item.Properties().tab(CREATIVE_TAB)));
		
		createItem("titanium_helmet", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.HEAD, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_chestplate", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.CHEST, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_leggings", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.LEGS, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("titanium_boots", new ArmorItem(ArmorTiers.TITANIUM, EquipmentSlot.FEET, new Item.Properties().tab(CREATIVE_TAB)));
		
		createItem("crank_shaft", "convection_component", "conduction_component", "empowered_conduction_component", "empowered_convection_component", "basic_battery",
				"temperature_regulator", "fluid_regulator", "stainless_steel_tank_component", "steel_tank_component", "pneumatic_device");
		createItem("chromium_ingot", "chromium_nugget", "chromium_plate", "raw_chromium", "stainless_steel_plate", "ground_chromium");
		createItem("energized_gold_ingot", "energized_gold_plate");
		
		createItem("plastic_sheet", "rubber_sheet", "plastic_ball", "rubber_ball");
		
		createItem("lock_remover", new ItemLockRemover());
		createItem("key", new ItemKey());
		
		createItem("generator_upgrade_coolant", new ItemUpgrade(false, "Generators will give more power per unit of fuel.", "Requires a secondary coolant supply."));
		
		createItem("polished_rock");
		createItem("mob_crystal", new ItemMobCrystal());
		
		createItem("sawdust", "warped_sawdust", "crimson_sawdust", "miniature_black_hole", "singularity");
		
		createItem("fertilizer", new ItemFertilizer(1));
		createItem("enhanced_fertilizer", new ItemFertilizer(3));
		createItem("ultimate_fertilizer", new ItemFertilizer(5));
		createItem("enhanced_battery", "microprocessor", "energy_harness", "attuned_titanium_ingot", "attuned_titanium_plate", "nether_star_shard");
		
		createItem("purifier_upgrade_enhanced", new ItemUpgrade(false, "Purifier can process more recipes.", "Increases power consumption."));
		
		createItem("entropy_reactor_upgrade_capacity", new ItemUpgrade(true, "Entropy Reactor has a higher capacity."));
		createItem("entropy_reactor_upgrade_cycle_delayer", new ItemUpgrade(true, "Entropy Reactor waits longer to clear capacity."));
		createItem("entropy_reactor_upgrade_variety", new ItemUpgrade(false, "Higher Variety has greater performance.", "Lower Variety has worsened performance."));
		createItem("entropy_reactor_upgrade_entropic_harnesser", new ItemUpgrade(false, new String[] {"Most Entropy effects are prevented."}, new String[] {"Frequency & range for Entropy effects is greater.", "Entropy is generated instead of FE."}));
		
		createItem("semi_dense_neutron_matter", new ItemReactorOutput("§7Low-Quality"));
		createItem("quark_matter", new ItemReactorOutput("§9Medium-Quality"));
		createItem("strange_matter", new ItemReactorOutput("§aHigh-Quality"));
		
		createItem("corrupt_shell_spawn_egg", new SpawnEggItem(EntityCorruptShell.CORRUPT_SHELL, 0x005f85, 0x22a1d4, new Item.Properties().tab(CREATIVE_TAB)));
		createItem("reality_crystal");
		createItem("galactic_flesh", new ItemGalacticFlesh());
		
		createItem("electrified_netherite_blend", "ground_netherite");
		
		createItem("chaotic_sawdust", "graphene_rod", "prismatic_dust");
		createItem("chaotic_fertilizer", new ItemFertilizer(10));
		
		createItem("flerovium_gear", "flerovium_ingot", "flerovium_plate", "ground_flerovium", "raw_flerovium", "flerovium_nugget");
		createItem("ground_diamond");
		
		createItem("nova_blend", "novasteel_ingot", "novasteel_plate", "raw_novasteel_compound", "ultimate_battery");
		
		createItem("aefg", new ItemAEFG());
		createItem("chaotic_reduction_goggles", new ItemChaoticReductionGoggles());
		
		createItem("overclocked_convection_component", "overclocked_conduction_component");
		
		createItem("music_disc_assembly_required", new RecordItem(1, () -> Registry.getSound("assembly_required"), new Item.Properties().tab(CREATIVE_TAB).stacksTo(1).rarity(Rarity.RARE)));
		
		for(String i : MOD_ITEM_REGISTRY.keySet()) {
			event.getRegistry().register(MOD_ITEM_REGISTRY.get(i));
		}
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		createBlock("titanium_ore", Material.STONE, 3f, 15f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		createBlock("deepslate_titanium_ore", Material.STONE, 4f, 20f, SoundType.DEEPSLATE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		
		createBlock("titanium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		
		createBlock("hand_grinder", new BlockHandGrinder(), true);
		createBlock("fluid_bath", new BlockFluidBath(), true);
		
		createBlock("crank", new BlockCrank(), true);
		createBlock("gearbox", new BlockGearbox(), true);
		
		createBlock("simple_crank_charger", new BlockSimpleCrankCharger(), true);
		createBlock("simple_fluid_mixer", new BlockSimpleFluidMixer(), true);
		createBlock("simple_grinder", new BlockSimpleGrinder(), true);
		
		//ALL PIPES REGISTERED HERE - Using loop.
		for(Triple<String, PipeType, TransmissionType> pipeData : TransmissionType.getPipeRegistryValues()) {
			createBlock(pipeData.getLeft(), new BlockPipe(pipeData.getRight(), pipeData.getMiddle()), true);
		}
		
		createBlock("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT, 0xff545454), true);
		createBlock("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD, 0xff826a4a), true);
		
		createBlock("silt", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		createBlock("silt_brick", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
		createBlock("slab_silt_brick", new SlabBlock(Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);
		createBlock("stair_silt_brick", new StairBlock(() -> Registry.getBlock("silt_brick").defaultBlockState(), Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);
		
		createBlock("steel_block", Material.METAL, 7f, 30f, SoundType.METAL, false, true);
		
		createBlock("black_granite", new BlockBlackGranite(), true);
		createBlock("smooth_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
		createBlock("brick_black_granite", Material.STONE, 3f, 9f,  SoundType.STONE, false, true);
		createBlock("chiselled_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
		createBlock("pillar_black_granite", new BlockBlackGranitePillar(), true);
		createBlock("slab_black_granite", new SlabBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
		createBlock("stair_black_granite", new StairBlock(() -> Registry.getBlock("smooth_black_granite").defaultBlockState(), Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
		
		createBlock("silt_iron", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		createBlock("silt_gold", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		createBlock("silt_titanium", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		createBlock("silt_copper", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		
		createBlock("basic_battery_cell", new BlockBatteryCell(BatteryCellStats.BASIC), true);
		createBlock("advanced_battery_cell", new BlockBatteryCell(BatteryCellStats.ADVANCED), true);
		createBlock("coal_generator", new BlockCoalGenerator(), true);
		createBlock("crankmill", new BlockCrankmill(), true);
		
		createBlock("chromium_ore", Material.STONE, 3f, 15f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_NETHERITE_TOOL, true);
		createBlock("chromium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		
		createBlock("electric_furnace", new BlockElectricFurnace(), true);
		createBlock("electric_purifier", new BlockElectricPurifier(), true);
		createBlock("electric_grinder", new BlockElectricGrinder(), true);
		createBlock("electric_fluid_mixer", new BlockElectricFluidMixer(), true);
		createBlock("alloy_smelter", new BlockAlloySmelter(), true);
		
		createBlock("autocrafting_table", new BlockAutocraftingTable(), true);
		
		createBlock("naphtha_fire", new BlockNaphthaFire(), false);
		
		createBlock("pump", new BlockPump(), true);
		createBlock("pumpshaft", new BlockPumpshaft(), true);
		
		createBlock("mystium_block", Material.METAL, 11f, 80f, SoundType.METAL, false, true);
		
		createBlock("mystium_fluid_tank", new BlockFluidTank(250000, TemperatureResistance.HOT, 0xff1616b8), true);
		
		createBlock("tool_charger", new BlockToolCharger(), true);
		
		createBlock("smoldering_stone", Material.STONE, 30f, 300f, SoundType.STONE, false, true);
		createBlock("naphtha_turbine", new BlockNaphthaTurbine(), true);
		
		createBlock("compressed_crafting_table", Material.WOOD, 2f, 10f, SoundType.WOOD, false, true);
		
		createBlock("refinery", new BlockRefinery(), true);
		createBlock("refinery_attachment_separation", RefineryAddon.SEPARATION.construct(), true);
		createBlock("refinery_attachment_addition", RefineryAddon.ADDITION.construct(), true);
		createBlock("refinery_attachment_halogen", RefineryAddon.HALOGEN.construct(), true);
		createBlock("refinery_attachment_cracking", RefineryAddon.CRACKING.construct(), true);
		
		createBlock("fluid_router", new BlockFluidRouter(), true);
		createBlock("interactor", new BlockInteractor(), true);
		createBlock("vacuum_hopper", new BlockVacuumHopper(), true);
		
		createBlock("bottomless_storage_unit", new BlockBottomlessStorageUnit(), true);
		
		createBlock("combustion_generator", new BlockFluidGenerator(FluidGeneratorTypes.COMBUSTION), true);
		createBlock("geothermal_generator", new BlockFluidGenerator(FluidGeneratorTypes.GEOTHERMAL), true);
		
		createBlock("experience_hopper", new BlockExperienceHopper(), true);
		createBlock("powered_spawner", new BlockPoweredSpawner(), true);
		createBlock("experience_mill", new BlockExperienceMill(), true);
		
		createBlock("quarry", new BlockQuarry(), true);
		createBlock("quarry_speed_addon", new BlockSpeedQuarryAddon(), true);
		createBlock("quarry_fortune_addon", new BlockFortuneVoidQuarryAddon(), true);
		createBlock("quarry_void_addon", new BlockFortuneVoidQuarryAddon(), true);
		
		createBlock("mystium_farmland", new BlockMystiumFarmland(true), false);
		
		createBlock("lumber_mill", new BlockLumberMill(), true);
		
		createBlock("quantum_link", new BlockQuantumLink(), true);
		createBlock("metal_shaper", new BlockMetalShaper(), true);
		
		createBlock("entropy_reactor_block", new BlockEntropyReactor(), true);
		createBlock("entropy_reactor_core", new BlockEntropyReactorCore(), true);
		createBlock("corrupting_basin", new BlockCorruptingBasin(), true);
		
		createBlock("corrupt_dirt", new CorruptBlock(Block.Properties.of(Material.DIRT).sound(SoundType.GRAVEL), BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_grass", new CorruptBlock(Block.Properties.of(Material.DIRT).sound(SoundType.GRAVEL), BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_DIAMOND_TOOL, true, true), true);
		createBlock("corrupt_sand", new CorruptFallingBlock(0x4287f5, Block.Properties.of(Material.SAND).sound(SoundType.SAND)), true);
		createBlock("corrupt_stone", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_gravel", new CorruptFallingBlock(0x4287f5, Block.Properties.of(Material.SAND).sound(SoundType.GRAVEL)), true);
		createBlock("chaosbark_log", new CorruptBlockWithAxis(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD), BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_DIAMOND_TOOL, false, false), true);
		createBlock("stripped_chaosbark_log", new CorruptBlockWithAxis(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD), BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_DIAMOND_TOOL, false, false), true);
		createBlock("chaosbark_sapling", new CorruptTallGrassBlock.ChaosbarkSaplingBlock(), true);
		createBlock("chaosbark_leaves", new CorruptLeavesBlock(), true);
		createBlock("chaosbark_planks", Material.WOOD, 3f, 9f, SoundType.WOOD, false, BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		createBlock("chaosbark_stairs", new StairBlock(() -> Registry.getBlock("chaosbark_planks").defaultBlockState(), Block.Properties.of(Material.WOOD).strength(3f, 9f).sound(SoundType.WOOD)), true);
		createBlock("chaosbark_slab", new SlabBlock(Block.Properties.of(Material.WOOD).strength(3f, 9f).sound(SoundType.WOOD)), true);
		createBlock("chaosbark_door", new DoorBlock(Block.Properties.of(Material.WOOD).strength(3f, 9f).sound(SoundType.WOOD).noOcclusion()), true);
		createBlock("chaosbark_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD).strength(3f, 9f).sound(SoundType.WOOD).noOcclusion()), true);
		createBlock("chaosbark_fence", new ChaosbarkFenceBlock(), true);
		createBlock("chaosbark_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD).strength(3f, 9f).sound(SoundType.WOOD).noOcclusion()), true);
		
		createBlock("chaosweed", new CorruptTallGrassBlock(), true);
		createBlock("blooming_chaosweed", new CorruptTallGrassBlock(), true);
		createBlock("tall_chaosweed", new CorruptDoubleTallGrassBlock(), true);
		createBlock("tall_blooming_chaosweed", new CorruptDoubleTallGrassBlock(), true);
		createBlock("mandelbloom", new CorruptFlowerBlock(MobEffects.CONFUSION, 15, 0), true);
		createBlock("prism_rose", new CorruptFlowerBlock(MobEffects.GLOWING, 30, 7), true);
		createBlock("bright_prism_rose", new CorruptFlowerBlock(MobEffects.GLOWING, 240, 15), true);
		createBlock("brain_cactus", new BrainCactusBlock(), true);
		
		createBlock("corrupt_coal_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_copper_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_diamond_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_emerald_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_gold_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_iron_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_lapis_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_redstone_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("corrupt_titanium_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
		createBlock("flerovium_ore", new CorruptFallingBlock(0x4287f5, Block.Properties.of(Material.SAND).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(), TagMaster.NEEDS_MYSTIUM_TOOL), true);
		
		createBlock("flerovium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		createBlock("energized_gold_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		createBlock("attuned_titanium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		
		createBlock("novasteel_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
		createBlock("nova_farmland", new BlockMystiumFarmland(false), false);
		
		createBlock("ultimate_battery_cell", new BlockBatteryCell(BatteryCellStats.ULTIMATE), true);
		
		createBlock("attuned_titanium_fluid_tank", new BlockFluidTank(5000000, TemperatureResistance.HOT, 0xffe69ee6), true);
		
		createBlock("novasteel_fluid_tank", new BlockFluidTank(50000000, TemperatureResistance.HOT, 0xff111d63), true);
		createBlock("silt_mystium", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
		createBlock("prism_glass", new Block(Block.Properties.of(Material.GLASS).sound(SoundType.GLASS).lightLevel((state) -> 15).noOcclusion()), true);
		
		createBlock("raw_chromium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_NETHERITE_TOOL, true);
		createBlock("raw_titanium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
		createBlock("raw_flerovium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_MYSTIUM_TOOL, true);
		
		createBlock("experience_siphon", new BlockExperienceSiphon(), true);
		
		registerFluids(null);
		
		event.getRegistry().registerAll(MOD_BLOCK_REGISTRY.values().toArray(new Block[MOD_BLOCK_REGISTRY.size()]));
	}
	
	//Method can be fired with event being null - this happens from the Block registration, to ensure all LiquidBlocks, Buckets, and Fluids get created before their respective registry events.
	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Fluid> event) {
		if(event == null) {
			
			createFluid("oil", new FluidOil(true), new FluidOil(false), new FluidOilBlock(() -> Registry.getFluid("oil")), true);
			createFluid("condensed_void", new FluidCondensedVoid(true), new FluidCondensedVoid(false), new FluidCondensedVoidBlock(() -> Registry.getFluid("condensed_void")), true);
			createFluid("naphtha", new FluidNaphtha(true), new FluidNaphtha(false), new FluidNaphthaBlock(() -> Registry.getFluid("naphtha")), true);
			createFluid("gasoline", new FluidOilProduct("gasoline", true), new FluidOilProduct("gasoline", false), new FluidOilProductBlock(() -> Registry.getFluid("gasoline"), "gasoline"), true);
			createFluid("diesel", new FluidOilProduct("diesel", true), new FluidOilProduct("diesel", false), new FluidOilProductBlock(() -> Registry.getFluid("diesel"), "diesel"), true);
			createFluid("liquid_carbon", new FluidOilProduct("liquid_carbon", true), new FluidOilProduct("liquid_carbon", false), new FluidOilProductBlock(() -> Registry.getFluid("liquid_carbon"), "liquid_carbon"), true);
			createFluid("liquid_experience", 35, false, true, true, 0, 184, 18);
			createFluid("dark_energy", new FluidDarkEnergy(true), new FluidDarkEnergy(false), new FluidDarkEnergyBlock(() -> Registry.getFluid("dark_energy")), true);
			createFluid("glacier_water", new FluidGlacierWater(true), new FluidGlacierWater(false), new FluidGlacierWaterBlock(() -> Registry.getFluid("glacier_water")), true);
			
			createFluid("ethane", 0, true, false, false, 0, 0, 0);
			createFluid("ethylene", 0, true, false, false, 0, 0, 0);
			createFluid("propane", 0, true, false, false, 0, 0, 0);
			createFluid("propylene", 0, true, false, false, 0, 0, 0);
			
		}else {
			event.getRegistry().registerAll(MOD_FLUID_REGISTRY.values().toArray(new Fluid[MOD_FLUID_REGISTRY.size()]));
		}
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		createBlockEntity("hand_grinder", TEHandGrinder.class);
		createBlockEntity("fluid_bath", TEFluidBath.class);
		
		createBlockEntity("simple_fluid_mixer", TESimpleFluidMixer.class);
		createBlockEntity("simple_grinder", TESimpleGrinder.class);
		createBlockEntity("simple_crank_charger", TESimpleCrankCharger.class);
		
		createBlockEntity("gearbox", TEGearbox.class);
		
		createBlockEntity("fluid_tank", TEFluidTank.class, MOD_BLOCK_REGISTRY.get("wooden_fluid_tank"), MOD_BLOCK_REGISTRY.get("steel_fluid_tank"), MOD_BLOCK_REGISTRY.get("mystium_fluid_tank"),
				MOD_BLOCK_REGISTRY.get("attuned_titanium_fluid_tank"), MOD_BLOCK_REGISTRY.get("novasteel_fluid_tank"));
		
		//ALL PIPES CONNECTED TO TE HERE - Using loop.
		ArrayList<Block> pipes = new ArrayList<>();
		for(Triple<String, PipeType, TransmissionType> pipeData : TransmissionType.getPipeRegistryValues()) {
			pipes.add(MOD_BLOCK_REGISTRY.get(pipeData.getLeft()));
		}
		createBlockEntity("pipe_connector", PipeConnectorTileEntity.class, pipes.toArray(new Block[pipes.size()]));
		
		createBlockEntity("battery_cell", TEBatteryCell.class, MOD_BLOCK_REGISTRY.get("basic_battery_cell"), MOD_BLOCK_REGISTRY.get("advanced_battery_cell"), MOD_BLOCK_REGISTRY.get("ultimate_battery_cell"));
		
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
		createBlockEntity("fluid_generator", TEFluidGenerator.class, MOD_BLOCK_REGISTRY.get("geothermal_generator"), MOD_BLOCK_REGISTRY.get("combustion_generator"));
		createBlockEntity("experience_hopper", TEExperienceHopper.class);
		
		createBlockEntity("powered_spawner", TEPoweredSpawner.class);
		createBlockEntity("experience_mill", TEExperienceMill.class);
		createBlockEntity("quarry", TEQuarry.class);
		createBlockEntity("lumber_mill", TELumberMill.class);
		
		createBlockEntity("quantum_link", TEQuantumLink.class);
		
		createBlockEntity("metal_shaper", TEMetalShaper.class);
		
		createBlockEntity("entropy_reactor", TEEntropyReactor.class, MOD_BLOCK_REGISTRY.get("entropy_reactor_block"));
		createBlockEntity("entropy_reactor_slave", TEEntropyReactorSlave.class, MOD_BLOCK_REGISTRY.get("entropy_reactor_block"));
		
		createBlockEntity("corrupting_basin", TECorruptingBasin.class);
		createBlockEntity("experience_siphon", TEExperienceSiphon.class);
		
		event.getRegistry().registerAll(MOD_BLOCKENTITY_REGISTRY.values().toArray(new BlockEntityType<?>[MOD_BLOCKENTITY_REGISTRY.size()]));
	}
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
		createContainer("simple_fluid_mixer", 1050, ContainerSimpleFluidMixer.class);
		createContainer("simple_grinder", 1051, ContainerSimpleGrinder.class);
		createContainer("gearbox", 1052, ContainerGearbox.class);
		createContainer("pipe_connector", 1053, PipeConnectorContainer.class);
		
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
		
		Iterator<Pair<MenuType<?>, Integer>> iter = MOD_CONTAINER_REGISTRY.values().iterator();
		while(iter.hasNext()) event.getRegistry().register(iter.next().getFirst());
		
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
		createEffect("dark_expulsion", new EffectDarkExpulsion());
		
		event.getRegistry().registerAll(MOD_EFFECT_REGISTRY.values().toArray(new MobEffect[MOD_EFFECT_REGISTRY.size()]));
	}
	
	@SubscribeEvent
	public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
		
		createEnchantment("overclock", new EnchantmentOverclock());
		
		event.getRegistry().registerAll(MOD_ENCHANTMENT_REGISTRY.values().toArray(new Enchantment[MOD_ENCHANTMENT_REGISTRY.size()]));
	}
	
	@SubscribeEvent
	public static void registerCrafting(RegistryEvent.Register<RecipeSerializer<?>> event) {
		
		createRecipe(GrinderCrafting.GRINDER_RECIPE, GrinderCrafting.SERIALIZER);
		createRecipe(BathCrafting.BATH_RECIPE, BathCrafting.SERIALIZER);
		createRecipe(PurifierCrafting.PURIFIER_RECIPE, PurifierCrafting.SERIALIZER);
		createRecipe(AlloyingCrafting.ALLOYING_RECIPE, AlloyingCrafting.SERIALIZER);
		createRecipe(FluidInGroundRecipe.FIG_RECIPE, FluidInGroundRecipe.SERIALIZER);
		createRecipe(RefiningCrafting.REFINING_RECIPE, RefiningCrafting.SERIALIZER);
		createRecipe(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE, EnchantmentBookCrafting.SERIALIZER);
		createRecipe(LumberCrafting.LUMBER_RECIPE, LumberCrafting.SERIALIZER);
		createRecipe(MetalCrafting.METAL_RECIPE, MetalCrafting.SERIALIZER);
		createRecipe(EntropyReactorCrafting.ERO_RECIPE, EntropyReactorCrafting.SERIALIZER);
		createRecipe(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE, WorldCorruptionCrafting.SERIALIZER);
		createRecipe(GeneratorFluidCrafting.GENFLUID_RECIPE, GeneratorFluidCrafting.SERIALIZER);
		
		for(String name : MOD_CRAFTING_REGISTRY.keySet()) {
			Pair<RecipeType<?>, ForgeRegistryEntry<RecipeSerializer<?>>> recipe = MOD_CRAFTING_REGISTRY.get(name);
			net.minecraft.core.Registry.register(net.minecraft.core.Registry.RECIPE_TYPE, new ResourceLocation(recipe.getFirst().toString()), recipe.getFirst());
			event.getRegistry().register(recipe.getSecond().setRegistryName(name));
		}
	}
	
	/*
	@SubscribeEvent
	public static void registerCarvers(RegistryEvent.Register<WorldCarver<?>> event) {
		event.getRegistry().register(new ChaosPlaneCarver(CaveCarverConfiguration.CODEC).setRegistryName("chaos_plane_cave"));
		
	}*/
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		MOD_SOUND_REGISTRY.put("corrupt_shell_ambient", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_ambient")).setRegistryName("corrupt_shell_ambient"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_hurt", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_hurt")).setRegistryName("corrupt_shell_hurt"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_death", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_death")).setRegistryName("corrupt_shell_death"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_step", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_step")).setRegistryName("corrupt_shell_step"));
		
		MOD_SOUND_REGISTRY.put("corrupt_shell_cool_ambient", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_cool_ambient")).setRegistryName("corrupt_shell_cool_ambient"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_cool_hurt", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_cool_hurt")).setRegistryName("corrupt_shell_cool_hurt"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_cool_death", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_cool_death")).setRegistryName("corrupt_shell_cool_death"));
		MOD_SOUND_REGISTRY.put("corrupt_shell_cool_step", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_cool_step")).setRegistryName("corrupt_shell_cool_step"));
		
		MOD_SOUND_REGISTRY.put("assembly_required", new SoundEvent(new ResourceLocation(AssemblyLineMachines.MODID, "assembly_required")).setRegistryName("assembly_required"));
		
		event.getRegistry().registerAll(MOD_SOUND_REGISTRY.values().toArray(new SoundEvent[MOD_SOUND_REGISTRY.size()]));
	}
	
	//CLIENT-RELATED SETUP EVENTS - RENDER LAYERS, TERs, SCREENS, ITEM PROPERTIES, TINTING HANDLERS, ENTITY RENDERERS
	
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ItemBlockRenderTypes.setRenderLayer(getBlock("steel_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("wooden_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("mystium_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("attuned_titanium_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("novasteel_fluid_tank"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("autocrafting_table"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("geothermal_generator"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("naphtha_fire"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("powered_spawner"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("entropy_reactor_block"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("chaosbark_leaves"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("chaosbark_sapling"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("chaosweed"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("blooming_chaosweed"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("tall_chaosweed"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("tall_blooming_chaosweed"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("mandelbloom"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("prism_rose"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("brain_cactus"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("bright_prism_rose"), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(getBlock("prism_glass"), RenderType.cutoutMipped());
		
		ItemBlockRenderTypes.setRenderLayer(getFluid("naphtha"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("naphtha_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("diesel"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("diesel_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("gasoline"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("gasoline_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_experience"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_experience_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("dark_energy"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("dark_energy_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_carbon"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("liquid_carbon_flowing"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("glacier_water"), RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(getFluid("glacier_water_flowing"), RenderType.translucent());
		
		BlockEntityRenderers.register((BlockEntityType<TEFluidTank>)getBlockEntity("fluid_tank"), new TankTER());
		BlockEntityRenderers.register((BlockEntityType<TEPoweredSpawner>)getBlockEntity("powered_spawner"), new PoweredSpawnerTER());
		BlockEntityRenderers.register((BlockEntityType<TEQuantumLink>)getBlockEntity("quantum_link"), new QuantumLinkTER());
		
		registerScreen("simple_fluid_mixer", ContainerSimpleFluidMixer.class, ScreenSimpleFluidMixer.class); 
		registerScreen("simple_grinder", ContainerSimpleGrinder.class, ScreenSimpleGrinder.class);
		registerScreen("gearbox", ContainerGearbox.class, ScreenGearbox.class);
		registerScreen("pipe_connector", PipeConnectorContainer.class, PipeConnectorScreen.class);
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
		
		ItemProperties.registerGeneric(new ResourceLocation(AssemblyLineMachines.MODID, "active"), new ClampedItemPropertyFunction() {
			@Override
			public float unclampedCall(ItemStack stack, ClientLevel level, LivingEntity entity, int p_174567_) {
				if(stack.getItem() instanceof IToolWithCharge) {
					return ((IToolWithCharge) stack.getItem()).getActivePropertyState(stack, entity);
				}
				return 0f;
			}
		});
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
	@OnlyIn(Dist.CLIENT)
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(EntityCorruptShell.CORRUPT_SHELL, new EntityCorruptShellRenderFactory());
	}
	
	
	//CONFIG CREATOR/DATA GENERATOR
	
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) throws Exception {
		
		PrintWriter pw = new PrintWriter("logs/almdatagen.log", "UTF-8");
		pw.println("[SYSTEM]: Commencing ALM data generation...");
		
		new DataProviderContainer(pw, event);
		new AutoRecipeGenerator(event, pw);
		new LootTableGenerator(event, pw);
		
		event.getGenerator().run();
		pw.close();
	}
	
	
	//===============================================
	
	//EFFECTS
	private static void createEffect(String name, MobEffect effect) {
		effect.setRegistryName(name);
		MOD_EFFECT_REGISTRY.put(name, effect);
	}
	
	public static MobEffect getEffect(String name) {
		return MOD_EFFECT_REGISTRY.get(name);
	}
	
	//ENCHANTMENTS
	private static void createEnchantment(String name, Enchantment enchantment) {
		enchantment.setRegistryName(name);
		MOD_ENCHANTMENT_REGISTRY.put(name, enchantment);
	}
	
	public static Enchantment getEnchantment(String name) {
		return MOD_ENCHANTMENT_REGISTRY.get(name);
	}
	
	//ITEMS
	private static void createItem(String... names) {
		for(String s : names) {
			createItem(s);
		}
	}
	
	private static void createItem(String name) {
		
		Item.Properties properties = new Item.Properties().tab(CREATIVE_TAB);
		createItem(name, properties);
	}
	
	static void createItem(String name, Item.Properties properties) {
		Item i = new Item(properties);
		createItem(name, i);
	}
	
	static void createItem(String name, Item item) {
		item.setRegistryName(name);
		MOD_ITEM_REGISTRY.put(name, item);
	}
	
	public static Item getItem(String name) {
		return MOD_ITEM_REGISTRY.get(name);
	}
	
	
	//BLOCKS
	private static void createBlock(String name, Material material, float hardness, float resistance, SoundType sound, boolean requireToolToDrop, Named<Block> type, Named<Block> level, boolean item) {
		Block.Properties properties = Block.Properties.of(material).strength(hardness, resistance).sound(sound);
		if(requireToolToDrop) {
			properties = properties.requiresCorrectToolForDrops();
		}
		createBlock(name, properties, type, level, item);
		
	}
	
	private static void createBlock(String name, Material material, float hardness, float resistance, SoundType sound, boolean requireToolToDrop, boolean item) {
		Block.Properties properties = Block.Properties.of(material).strength(hardness, resistance).sound(sound);
		if(requireToolToDrop) {
			properties = properties.requiresCorrectToolForDrops();
		}
		createBlock(name, properties, item);
	}
	
	private static void createBlock(String name, Block.Properties properties, Named<Block> type, Named<Block> level, boolean item) {
		class BlockWithTags extends Block implements TagMaster.IMiningLevelDataGenProvider{

			public BlockWithTags(Properties p) {super(p);}
			@Override
			public Named<Block> getToolType() {return type;}
			@Override
			public Named<Block> getToolLevel() {return level;}
			
		}
		
		createBlock(name, new BlockWithTags(properties), item);
	}
	
	private static void createBlock(String name, Block.Properties properties, boolean item) {
		Block b = new Block(properties);
		createBlock(name, b, item);
	}
	
	static void createBlock(String name, Block block, boolean item) {
		block.setRegistryName(name);
		MOD_BLOCK_REGISTRY.put(name, block);
		if(item) {
			createItem(name, new BlockItem(block, new Item.Properties().tab(CREATIVE_TAB)));
		}
		
	}
	
	public static Block getBlock(String name) {
		return MOD_BLOCK_REGISTRY.get(name);
	}
	
	public static Collection<Block> getAllBlocksUnmodifiable(){
		return Collections.unmodifiableCollection(MOD_BLOCK_REGISTRY.values());
	}
	
	public static List<Block> getAllBlocksModifiable(){
		return new ArrayList<>(MOD_BLOCK_REGISTRY.values());
	}
	
	//FLUIDS
	public static ForgeFlowingFluid getFluid(String name) {
		return MOD_FLUID_REGISTRY.get(name);
	}
	
	public static ForgeFlowingFluid.Properties createFluidProperties(String name, int temperature, boolean gaseous, boolean block, boolean bucket){
		
		ResourceLocation flowName = gaseous ? null : new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name + "_flowing");
		FluidAttributes.Builder attribs = FluidAttributes.builder(new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name), flowName).temperature(temperature);
		if(gaseous) {
			return new ForgeFlowingFluid.Properties(() -> Registry.getFluid(name), null, attribs);
		}else {
			ForgeFlowingFluid.Properties props = new ForgeFlowingFluid.Properties(() -> Registry.getFluid(name), () -> Registry.getFluid(name + "_flowing"), attribs);
			if(block) {
				props.block(() -> (LiquidBlock) Registry.getBlock(name + "_block"));
			}
			if(bucket) {
				props.bucket(() -> (BucketItem) Registry.getItem(name + "_bucket"));
			}
			return props;
		}
	}

	public static void createFluid(String name, int temperature, boolean gaseous, boolean block, boolean bucket, int r, int g, int b) {
		
		if(gaseous) {
			ALMFluid gas = new ALMFluid(createFluidProperties(name, temperature, true, false, false), true, r, g, b);
			gas.setRegistryName(name);
			MOD_FLUID_REGISTRY.put(name, gas);
		}else {
			ForgeFlowingFluid.Properties props = createFluidProperties(name, temperature, gaseous, block, bucket);
			createFluid(name, new ALMFluid(props, true, r, g, b), new ALMFluid(props, false, r, g, b), block, bucket);
		}
	}
	
	public static void createFluid(String name, ALMFluid still, ALMFluid flowing, boolean block, boolean bucket) {
		LiquidBlock lb = null;
		if(block) {
			lb = new ALMFluidBlock(() -> Registry.getFluid(name), ALMFluid.getTag("liquid_experience"), Material.WATER);
		}
		createFluid(name, still, flowing, lb, bucket);
		
	}

	public static void createFluid(String name, ALMFluid still, ALMFluid flowing, LiquidBlock block, boolean bucket) {
		BucketItem bi = bucket ? new BucketItem(() -> Registry.getFluid(name), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.CREATIVE_TAB)) : null;
		if(bi != null) {
			bi.setRegistryName(name + "_bucket");
			MOD_ITEM_REGISTRY.put(name + "_bucket", bi);
		}
		if(block != null) {
			block.setRegistryName(name + "_block");
			MOD_BLOCK_REGISTRY.put(name + "_block", block);
		}
		if(flowing != null) {
			flowing.setRegistryName(name + "_flowing");
			MOD_FLUID_REGISTRY.put(name + "_flowing", flowing);
		}
		if(still != null) {
			still.setRegistryName(name);
			MOD_FLUID_REGISTRY.put(name, still);
		}
		
	}
	
	public static Collection<ForgeFlowingFluid> getAllFluids() {
		return MOD_FLUID_REGISTRY.values();
	}
	
	//TILE ENTITIES
	public static BlockEntityType<?> getBlockEntity(String name){
		return MOD_BLOCKENTITY_REGISTRY.get(name);
	}
	
	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz, Block... blocks){
		MOD_BLOCKENTITY_REGISTRY.put(name, BlockEntityType.Builder.of((pos, state) -> {

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
		return MOD_CONTAINER_REGISTRY.get(name).getFirst();
	}
	
	public static Integer getContainerId(String name){
		return MOD_CONTAINER_REGISTRY.get(name).getSecond();
	}
	
	public static <T extends AbstractContainerMenu> void createContainer(String name, int id, Class<T> clazz) {
		MenuType<?> mt = IForgeMenuType.create(new IContainerFactory<T>() {

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
		}).setRegistryName(name);
		MOD_CONTAINER_REGISTRY.put(name, Pair.of(mt, id));
	}
	
	//SOUNDS
	public static SoundEvent getSound(String name) {
		return MOD_SOUND_REGISTRY.get(name);
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
	
	//CRAFTING
	public static void createRecipe(RecipeType<?> type, ForgeRegistryEntry<RecipeSerializer<?>> serializer) {
		MOD_CRAFTING_REGISTRY.put(type.toString().split(":")[1], Pair.of(type, serializer));
	}
}
