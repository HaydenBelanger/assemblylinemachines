package me.haydenb.assemblylinemachines.registry;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.energy.*;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCoalGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockCrankmill.*;
import me.haydenb.assemblylinemachines.block.energy.BlockEntropyReactor.*;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.*;
import me.haydenb.assemblylinemachines.block.energy.BlockGearbox.*;
import me.haydenb.assemblylinemachines.block.energy.BlockSimpleCrankCharger.TESimpleCrankCharger;
import me.haydenb.assemblylinemachines.block.energy.BlockToolCharger.TEToolCharger;
import me.haydenb.assemblylinemachines.block.fluids.*;
import me.haydenb.assemblylinemachines.block.fluids.FluidCondensedVoid.FluidCondensedVoidBlock;
import me.haydenb.assemblylinemachines.block.fluids.FluidNaphtha.BlockNaphthaFire;
import me.haydenb.assemblylinemachines.block.fluids.SplitFluid.GasFluid;
import me.haydenb.assemblylinemachines.block.fluids.SplitFluid.SpecialRenderFluidType;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.*;
import me.haydenb.assemblylinemachines.block.machines.BlockAutocraftingTable.*;
import me.haydenb.assemblylinemachines.block.machines.BlockBottomlessStorageUnit.*;
import me.haydenb.assemblylinemachines.block.machines.BlockCorruptingBasin.*;
import me.haydenb.assemblylinemachines.block.machines.BlockExperienceHopper.TEExperienceHopper;
import me.haydenb.assemblylinemachines.block.machines.BlockExperienceMill.*;
import me.haydenb.assemblylinemachines.block.machines.BlockExperienceSiphon.TEExperienceSiphon;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidRouter.*;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidTank.TEFluidTank;
import me.haydenb.assemblylinemachines.block.machines.BlockGreenhouse.*;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.TEHandGrinder;
import me.haydenb.assemblylinemachines.block.machines.BlockInteractor.*;
import me.haydenb.assemblylinemachines.block.machines.BlockOmnivoid.*;
import me.haydenb.assemblylinemachines.block.machines.BlockPoweredSpawner.*;
import me.haydenb.assemblylinemachines.block.machines.BlockPump.BlockPumpshaft;
import me.haydenb.assemblylinemachines.block.machines.BlockPump.TEPump;
import me.haydenb.assemblylinemachines.block.machines.BlockQuantumLink.*;
import me.haydenb.assemblylinemachines.block.machines.BlockQuarry.*;
import me.haydenb.assemblylinemachines.block.machines.BlockQuarryAddon.QuarryAddonShapes;
import me.haydenb.assemblylinemachines.block.machines.BlockRefinery.*;
import me.haydenb.assemblylinemachines.block.machines.BlockRefinery.BlockRefineryAddon.RefineryAddon;
import me.haydenb.assemblylinemachines.block.machines.BlockVacuumHopper.TEVacuumHopper;
import me.haydenb.assemblylinemachines.block.misc.*;
import me.haydenb.assemblylinemachines.block.misc.CorruptBlock.*;
import me.haydenb.assemblylinemachines.block.misc.CorruptTallGrassBlock.*;
import me.haydenb.assemblylinemachines.block.pipes.BlockPipe;
import me.haydenb.assemblylinemachines.block.pipes.PipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipes.PipeConnectorTileEntity.PipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipes.PipeConnectorTileEntity.PipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipes.PipeProperties.TransmissionType;
import me.haydenb.assemblylinemachines.client.ter.*;
import me.haydenb.assemblylinemachines.crafting.*;
import me.haydenb.assemblylinemachines.item.*;
import me.haydenb.assemblylinemachines.item.IGearboxFuel.ItemGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.item.ItemWrenchOMatic.EnchantmentEngineersFury;
import me.haydenb.assemblylinemachines.item.powertools.*;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge.EnchantmentOverclock;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge.PowerToolType;
import me.haydenb.assemblylinemachines.registry.config.ConfigCondition.ConfigConditionSerializer;
import me.haydenb.assemblylinemachines.registry.config.ConfigMatchTest;
import me.haydenb.assemblylinemachines.registry.datagen.*;
import me.haydenb.assemblylinemachines.registry.datagen.TagMaster.DataProviderContainer;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import me.haydenb.assemblylinemachines.registry.utils.WhitelistBiomeFilter;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell;
import me.haydenb.assemblylinemachines.world.EntityCorruptShell.EntityCorruptShellRenderFactory;
import me.haydenb.assemblylinemachines.world.ModCommand.FluidArgument;
import me.haydenb.assemblylinemachines.world.RawFeatureDeserializer;
import me.haydenb.assemblylinemachines.world.chaosplane.ChaosPlaneCarver;
import me.haydenb.assemblylinemachines.world.effects.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.commands.synchronization.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.material.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.*;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.data.event.GatherDataEvent.DataGeneratorConfig;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.*;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class Registry {

	//MAP REGISTRIES
	private static final TreeMap<String, Item> ITEMS = new TreeMap<>((o1, o2) -> o1.compareTo(o2));
	private static final ConcurrentHashMap<String, Block> BLOCKS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, BlockEntityType<?>> BLOCK_ENTITIES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Pair<MenuType<?>, Integer>> CONTAINERS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, RecipeSerializer<?>> RECIPE_SERIALIZERS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, RecipeType<?>> RECIPE_TYPES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Fluid> FLUIDS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, FluidType> FLUID_TYPES = new ConcurrentHashMap<>();

	//DEFERRED REGISTRIES
	private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Keys.MOB_EFFECTS, AssemblyLineMachines.MODID);
	private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Keys.ENCHANTMENTS, AssemblyLineMachines.MODID);
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Keys.SOUND_EVENTS, AssemblyLineMachines.MODID);
	private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Keys.ENTITY_TYPES, AssemblyLineMachines.MODID);
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, AssemblyLineMachines.MODID);
	private static final DeferredRegister<RuleTestType<?>> RULE_TESTS = DeferredRegister.create(Registries.RULE_TEST, AssemblyLineMachines.MODID);
	private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, AssemblyLineMachines.MODID);
	private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(Keys.BIOME_MODIFIER_SERIALIZERS, AssemblyLineMachines.MODID);

	//EFFECTS
	public static final RegistryObject<MobEffect> ENTROPY_POISONING = EFFECTS.register("entropy_poisoning", () -> new EffectEntropyPoisoning());
	public static final RegistryObject<MobEffect> DEEP_BURN = EFFECTS.register("deep_burn", () -> new EffectDeepBurn());
	public static final RegistryObject<MobEffect> DARK_EXPULSION = EFFECTS.register("dark_expulsion", () -> new EffectDarkExpulsion());

	//ENCHANTMENTS
	public static final RegistryObject<Enchantment> OVERCLOCK = ENCHANTMENTS.register("overclock", () -> new EnchantmentOverclock());
	public static final RegistryObject<Enchantment> ENGINEERS_FURY = ENCHANTMENTS.register("engineers_fury", () -> new EnchantmentEngineersFury());

	//SOUND EVENTS
	public static final RegistryObject<SoundEvent> CORRUPT_SHELL_AMBIENT = SOUNDS.register("corrupt_shell_ambient", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_ambient")));
	public static final RegistryObject<SoundEvent> CORRUPT_SHELL_HURT = SOUNDS.register("corrupt_shell_hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_hurt")));
	public static final RegistryObject<SoundEvent> CORRUPT_SHELL_DEATH = SOUNDS.register("corrupt_shell_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_death")));
	public static final RegistryObject<SoundEvent> CORRUPT_SHELL_STEP = SOUNDS.register("corrupt_shell_step", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell_step")));
	public static final RegistryObject<SoundEvent> ASSEMBLY_REQUIRED = SOUNDS.register("assembly_required", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AssemblyLineMachines.MODID, "assembly_required")));

	//MISC
	public static final RegistryObject<EntityType<EntityCorruptShell>> CORRUPT_SHELL = ENTITIES.register("corrupt_shell", () -> EntityType.Builder.of(EntityCorruptShell::new, MobCategory.MONSTER).build(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell").toString()));
	public static final RegistryObject<SingletonArgumentInfo<FluidArgument>> FLUID_ARGUMENT = ARGUMENT_TYPES.register("fluid", () -> ArgumentTypeInfos.registerByClass(FluidArgument.class, SingletonArgumentInfo.contextFree(() -> new FluidArgument())));
	public static final RegistryObject<RuleTestType<ConfigMatchTest>> CONFIG_RULE_TEST = RULE_TESTS.register("config", () -> () -> ConfigMatchTest.CODEC);
	public static final RegistryObject<PlacementModifierType<WhitelistBiomeFilter>> WHITELIST_BIOME_FILTER = PLACEMENT_MODIFIERS.register("whitelist_biome", () -> () -> WhitelistBiomeFilter.CODEC);
	public static final RegistryObject<Codec<? extends BiomeModifier>> RAW_FEATURE_MODIFIER = BIOME_MODIFIERS.register("raw_features", () -> RawFeatureDeserializer.CODEC);

	//REGISTER DEFERRED REGISTRIES
	public static void registerDeferredRegistries() {
		Stream.of(Registry.class.getDeclaredFields()).filter((f) -> f.getType().equals(DeferredRegister.class)).forEach((f) -> {
			try {
				((DeferredRegister<?>) f.get(null)).register(FMLJavaModLoadingContext.get().getModEventBus());
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	@SubscribeEvent
	public static void register(RegisterEvent event) {

		//ITEMS
		event.register(Keys.ITEMS, (h) -> {
			
			createItem("titanium_ingot", "titanium_nugget", "raw_titanium");

			createItem("titanium_blade_piece", "pure_gold_blade_piece", "steel_blade_piece");
			createItem("titanium_blade", new Item.Properties().durability(Blade.TITANIUM.uses));
			createItem("pure_gold_blade", new Item.Properties().durability(Blade.PUREGOLD.uses));
			createItem("steel_blade", new Item.Properties().durability(Blade.STEEL.uses));

			createItem("ground_iron", "ground_gold", "ground_titanium", "ground_coal", "ground_charcoal", "ground_copper", "ground_lapis_lazuli");
			createItem("sludge");

			createItem("steel_ingot", "steel_nugget", "steel_rod");

			createItem("pure_iron_ingot", "pure_gold_ingot", "pure_titanium_ingot", "pure_steel_ingot", "pure_copper_ingot");

			createItem("pure_steel_plate", "wooden_board", "pure_iron_plate", "pure_gold_plate", "pure_titanium_plate", "pure_copper_plate", "mystium_plate");

			createItem("pure_gold_gear", "pure_steel_gear", "pure_iron_gear", "pure_titanium_gear", "pure_copper_gear");

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

			createItem("titanium_sword", new SwordItem(ItemTiers.TITANIUM.getItemTier(), 2, -1.5f, new Item.Properties()));
			createItem("titanium_axe", new AxeItem(ItemTiers.TITANIUM.getItemTier(), 3, -3.5f, new Item.Properties()));
			createItem("titanium_pickaxe", new PickaxeItem(ItemTiers.TITANIUM.getItemTier(), 0, -1.5f, new Item.Properties()));
			createItem("titanium_shovel", new ShovelItem(ItemTiers.TITANIUM.getItemTier(), 0, -1.3f, new Item.Properties()));
			createItem("titanium_hoe", new HoeItem(ItemTiers.TITANIUM.getItemTier(), 0, -0.5f, new Item.Properties()));
			createItem("titanium_hammer", new ItemHammer(ItemTiers.TITANIUM.getItemTier(), 8, -3.2f, new Item.Properties()));

			createItem("crank_sword", new ItemPowerSword(ItemTiers.CRANK, new Item.Properties()));
			createItem("crank_axe", new ItemPowerAxe(ItemTiers.CRANK, new Item.Properties()));
			createItem("crank_pickaxe", new ItemPowerPickaxe(ItemTiers.CRANK, new Item.Properties()));
			createItem("crank_shovel", new ItemPowerShovel(ItemTiers.CRANK, new Item.Properties()));
			createItem("crank_hoe", new ItemPowerHoe(ItemTiers.CRANK, new Item.Properties()));
			createItem("crank_hammer", new ItemPowerHammer(ItemTiers.CRANK, new Item.Properties()));

			createItem("mystium_sword", new ItemPowerSword(ItemTiers.MYSTIUM, new Item.Properties()));
			createItem("mystium_axe", new ItemPowerAxe(ItemTiers.MYSTIUM, new Item.Properties()));
			createItem("mystium_pickaxe", new ItemPowerPickaxe(ItemTiers.MYSTIUM, new Item.Properties()));
			createItem("mystium_shovel", new ItemPowerShovel(ItemTiers.MYSTIUM, new Item.Properties()));
			createItem("mystium_hoe", new ItemPowerHoe(ItemTiers.MYSTIUM, new Item.Properties()));
			createItem("mystium_hammer", new ItemPowerHammer(ItemTiers.MYSTIUM, new Item.Properties()));

			createItem("novasteel_sword", new ItemPowerSword(ItemTiers.NOVASTEEL, new Item.Properties()));
			createItem("novasteel_axe", new ItemPowerAxe(ItemTiers.NOVASTEEL, new Item.Properties()));
			createItem("novasteel_pickaxe", new ItemPowerPickaxe(ItemTiers.NOVASTEEL, new Item.Properties()));
			createItem("novasteel_shovel", new ItemPowerShovel(ItemTiers.NOVASTEEL, new Item.Properties()));
			createItem("novasteel_hoe", new ItemPowerHoe(ItemTiers.NOVASTEEL, new Item.Properties()));
			createItem("novasteel_hammer", new ItemPowerHammer(ItemTiers.NOVASTEEL, new Item.Properties()));

			createItem("steel_sword", new SwordItem(ItemTiers.STEEL.getItemTier(), 2, -1.5f, new Item.Properties()));
			createItem("steel_axe", new AxeItem(ItemTiers.STEEL.getItemTier(), 3, -3.5f, new Item.Properties()));
			createItem("steel_pickaxe", new PickaxeItem(ItemTiers.STEEL.getItemTier(), 0, -1.5f, new Item.Properties()));
			createItem("steel_shovel", new ShovelItem(ItemTiers.STEEL.getItemTier(), 0, -1.3f, new Item.Properties()));
			createItem("steel_hoe", new HoeItem(ItemTiers.STEEL.getItemTier(), 0, -0.5f, new Item.Properties()));
			createItem("steel_hammer", new ItemHammer(ItemTiers.STEEL.getItemTier(), 8, -3.2f, new Item.Properties()));

			createItem("steel_helmet", new ArmorItem(ItemTiers.STEEL.getArmorTier(), EquipmentSlot.HEAD, new Item.Properties()));
			createItem("steel_chestplate", new ArmorItem(ItemTiers.STEEL.getArmorTier(), EquipmentSlot.CHEST, new Item.Properties()));
			createItem("steel_leggings", new ArmorItem(ItemTiers.STEEL.getArmorTier(), EquipmentSlot.LEGS, new Item.Properties()));
			createItem("steel_boots", new ArmorItem(ItemTiers.STEEL.getArmorTier(), EquipmentSlot.FEET, new Item.Properties()));

			createItem("titanium_helmet", new ArmorItem(ItemTiers.TITANIUM.getArmorTier(), EquipmentSlot.HEAD, new Item.Properties()));
			createItem("titanium_chestplate", new ArmorItem(ItemTiers.TITANIUM.getArmorTier(), EquipmentSlot.CHEST, new Item.Properties()));
			createItem("titanium_leggings", new ArmorItem(ItemTiers.TITANIUM.getArmorTier(), EquipmentSlot.LEGS, new Item.Properties()));
			createItem("titanium_boots", new ArmorItem(ItemTiers.TITANIUM.getArmorTier(), EquipmentSlot.FEET, new Item.Properties()));

			createItem("crank_shaft", "convection_component", "conduction_component", "empowered_conduction_component", "empowered_convection_component", "basic_battery",
					"temperature_regulator", "fluid_regulator", "stainless_steel_tank_component", "steel_tank_component", "pneumatic_device", "basic_pneumatic_device");
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

			createItem("semi_dense_neutron_matter", new ItemReactorOutput(Component.literal("Low-Quality").withStyle(ChatFormatting.GRAY)));
			createItem("quark_matter", new ItemReactorOutput(Component.literal("Medium-Quality").withStyle(ChatFormatting.BLUE)));
			createItem("strange_matter", new ItemReactorOutput(Component.literal("High-Quality").withStyle(ChatFormatting.GREEN)));

			createItem("corrupt_shell_spawn_egg", new ForgeSpawnEggItem(() -> CORRUPT_SHELL.get(), 0x005f85, 0x22a1d4, new Item.Properties()));
			createItem("reality_crystal");
			createItem("galactic_flesh", new ItemGalacticFlesh());

			createItem("energizing_blend", "energizing_compound", "balanced_blend");
			createItem("electrified_netherite_blend", "ground_netherite");

			createItem("chaotic_sawdust", "graphene_rod", "prismatic_dust");
			createItem("chaotic_fertilizer", new ItemFertilizer(10));

			createItem("flerovium_gear", "flerovium_ingot", "flerovium_plate", "ground_flerovium", "raw_flerovium", "flerovium_nugget");
			createItem("ground_diamond", "ground_emerald");

			createItem("nova_blend", "novasteel_ingot", "novasteel_plate", "raw_novasteel_compound", "ultimate_battery");

			createItem("aefg", new ItemAEFG());
			createItem("chaotic_reduction_goggles", new ItemChaoticReductionGoggles());

			createItem("overclocked_convection_component", "overclocked_conduction_component");

			createItem("music_disc_assembly_required", new RecordItem(1, () -> ASSEMBLY_REQUIRED.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 5100));

			createItem("mkii_upgrade_kit", new ItemUpgradeKit());
			createItem("electric_upgrade_kit", new ItemUpgradeKit());

			createItem("wrench_o_matic", new ItemWrenchOMatic());

			createItem("mystium_helmet", new ItemPowerArmor(ItemTiers.MYSTIUM.getArmorTier(), EquipmentSlot.HEAD, new Item.Properties()));
			createItem("mystium_chestplate", new ItemPowerArmor(ItemTiers.MYSTIUM.getArmorTier(), EquipmentSlot.CHEST, new Item.Properties()));
			createItem("mystium_leggings", new ItemPowerArmor(ItemTiers.MYSTIUM.getArmorTier(), EquipmentSlot.LEGS, new Item.Properties()));
			createItem("mystium_boots", new ItemPowerArmor(ItemTiers.MYSTIUM.getArmorTier(), EquipmentSlot.FEET, new Item.Properties()));
			createItem("enhanced_mystium_chestplate", new ItemPowerArmor(PowerToolType.ENHANCED_MYSTIUM, ItemTiers.MYSTIUM.getArmorTier(), EquipmentSlot.CHEST, new Item.Properties().rarity(Rarity.EPIC)));

			createItem("mystium_armor_plating", "mystium_crystal", "mystium_flight_harness");

			createItem("greenhouse_upgrade_arborists_specialization", new ItemUpgrade(false, "Greenhouse can support Saplings."));
			createItem("greenhouse_upgrade_florists_specialization", new ItemUpgrade(false, "Greenhouse can support Flowers."));
			createItem("greenhouse_upgrade_interdimensional_specialization", new ItemUpgrade(false, "Greenhouse can support otherworldly crops."));
			createItem("greenhouse_upgrade_internal_lamp", new ItemUpgrade(false, new String[]{"Allows sunlight crops to grow in dark environments.", "Increases speed of operation."}, new String[] {"Increases power consumption.", "Becomes too bright to sustain darkness crops."}));
			createItem("greenhouse_upgrade_blackout_glass", new ItemUpgrade(false, new String[]{"Allows darkness crops to grow in light environments.", "Increases speed of operation."}, new String[]{"Increases power consumption.", "Won't allow sunlight crops to get enough sun."}));

			createItem("spores", new ItemSpores(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "spores_plantable")), Blocks.MYCELIUM));
			createItem("forest_fungus", new ItemSpores(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "forest_fungus_plantable")), Blocks.PODZOL));
			createItem("grass_seeds", new ItemSpores(TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "grass_seeds_plantable")), Blocks.GRASS_BLOCK));

			createItem("gear_mold", new Item.Properties().stacksTo(1));
			createItem("plate_mold", new Item.Properties().stacksTo(1));
			createItem("rod_mold", new Item.Properties().stacksTo(1));

			createItem("chromium_rod", "copper_rod", "gold_rod", "iron_rod", "mystium_gear", "ground_steel", "mystium_rod", "novasteel_gear", "novasteel_rod", "titanium_rod", "ground_amethyst",
					"graphene_gear", "graphene_ingot", "graphene_plate", "attuned_titanium_rod", "attuned_titanium_gear");
			createItem("quantum_fuel", new ItemGearboxFuel(12800, Rarity.RARE));
			createItem("glacier_sauce", new Item.Properties().rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder().effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600), 1f).nutrition(1).fast().saturationMod(2.4f).alwaysEat().build()));
			createItem("internal_water_generator", new ItemInternalWaterGenerator());
			createItem("creative_upgrade_kit", new ItemCreativeUpgradeKit());

			ITEMS.forEach((k, v) -> h.register(k, v));
		});

		//BLOCKS
		event.register(Keys.BLOCKS, (h) -> {
			TransmissionType.getPipeRegistryValues().forEach((pipeData) -> createBlock(pipeData.getLeft(), new BlockPipe(pipeData.getRight(), pipeData.getMiddle()), true));
			BlockCorruptOres.createCorruptOres();
			Utils.getAllMethods(BlockMachines.class, Block.class, null).forEach((name, block) -> createBlock(name, block, true));
			
			createBlock("titanium_ore", Material.STONE, 3f, 15f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
			createBlock("deepslate_titanium_ore", Material.STONE, 4f, 20f, SoundType.DEEPSLATE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);

			createBlock("titanium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);

			createBlock("hand_grinder", new BlockHandGrinder(), true);
			createBlock("fluid_bath", new BlockFluidBath(), true);

			createBlock("crank", new BlockCrank(), true);
			createBlock("gearbox", new BlockGearbox(), true);

			createBlock("simple_crank_charger", new BlockSimpleCrankCharger(), true);

			createBlock("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT, 0xff545454), true);
			createBlock("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD, 0xff826a4a), true);

			createBlock("silt", Material.CLAY, 1f, 2f, SoundType.GRAVEL, false, true);
			createBlock("silt_brick", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
			createBlock("large_silt_brick", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
			createBlock("painted_silt", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
			createBlock("silt_tile", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
			createBlock("smooth_silt", Material.STONE, 4f, 12f, SoundType.STONE, false, true);
			createBlock("silt_pillar", new BlockFancyPillar(Material.STONE, 4f, 12f, SoundType.STONE), true);
			createBlock("slab_silt_brick", new SlabBlock(Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);
			createBlock("stair_silt_brick", new StairBlock(() -> Registry.getBlock("silt_brick").defaultBlockState(), Block.Properties.of(Material.STONE).strength(4f, 12f).sound(SoundType.STONE)), true);

			createBlock("steel_block", Material.METAL, 7f, 30f, SoundType.METAL, false, true);

			createBlock("black_granite", new BlockBlackGranite(), true);
			createBlock("smooth_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("brick_black_granite", Material.STONE, 3f, 9f,  SoundType.STONE, false, true);
			createBlock("runed_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("arcane_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("circuit_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("tile_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("etched_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("large_brick_black_granite", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("pillar_black_granite", new BlockFancyPillar(Material.STONE, 3f, 9f, SoundType.STONE), true);
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
			createBlock("experience_mill", BlockExperienceMill.experienceMill(), true);

			createBlock("quarry", new BlockQuarry(), true);
			createBlock("quarry_speed_addon", new BlockQuarryAddon(QuarryAddonShapes.SPEED), true);
			createBlock("quarry_fortune_addon", new BlockQuarryAddon(QuarryAddonShapes.FORTUNE), true);
			createBlock("quarry_void_addon", new BlockQuarryAddon(QuarryAddonShapes.VOID), true);

			createBlock("mystium_farmland", new BlockMystiumFarmland(true), false);

			createBlock("quantum_link", new BlockQuantumLink(), true);

			createBlock("entropy_reactor_block", new BlockEntropyReactor(), true);
			createBlock("entropy_reactor_core", new BlockEntropyReactorCore(), true);
			createBlock("corrupting_basin", new BlockCorruptingBasin(), true);

			createBlock("corrupt_dirt", new CorruptBlock(Block.Properties.of(Material.DIRT).sound(SoundType.GRAVEL).strength(3f, 9f), BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_DIAMOND_TOOL), true);
			createBlock("corrupt_grass", new CorruptBlock(Block.Properties.of(Material.DIRT).sound(SoundType.GRAVEL).strength(3f, 9f), BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.NEEDS_DIAMOND_TOOL, true, true), true);
			createBlock("corrupt_sand", new CorruptFallingBlock(0x4287f5, Block.Properties.of(Material.SAND).strength(3f, 9f).sound(SoundType.SAND)), true);
			createBlock("corrupt_stone", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 9f), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
			createBlock("corrupt_gravel", new CorruptFallingBlock(0x4287f5, Block.Properties.of(Material.SAND).sound(SoundType.GRAVEL).strength(3f, 9f)), true);
			createBlock("corrupt_bedrock", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(-1f, 3600000f).noLootTable().isValidSpawn(Blocks::never), null, null), true);
			createBlock("corrupt_sandstone", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 9f), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
			createBlock("corrupt_basalt", new CorruptBlockWithAxis(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 15f), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, false, true), true);
			createBlock("flerovium_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 9f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_MYSTIUM_TOOL), true);
			createBlock("corrupt_basalt_empowered_coal_ore", new CorruptBlockWithAxis(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 15f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, false, true), true);

			createBlock("chaosbark_log", new CorruptBlockWithAxis(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(3f, 9f), BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_DIAMOND_TOOL, false, false), true);
			createBlock("stripped_chaosbark_log", new CorruptBlockWithAxis(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(3f, 9f), BlockTags.MINEABLE_WITH_AXE, BlockTags.NEEDS_DIAMOND_TOOL, false, false), true);
			createBlock("chaosbark_leaves", new CorruptLeavesBlock(Block.Properties.of(Material.LEAVES).sound(SoundType.GRASS).randomTicks().noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never).strength(3f, 9f)), true);

			createBlock("chaosbark_planks", Material.WOOD, 2f, 6f, SoundType.WOOD, false, true);
			createBlock("chaosbark_stairs", new StairBlock(() -> Registry.getBlock("chaosbark_planks").defaultBlockState(), Block.Properties.of(Material.WOOD).strength(2f, 6f).sound(SoundType.WOOD)), true);
			createBlock("chaosbark_slab", new SlabBlock(Block.Properties.of(Material.WOOD).strength(2f, 6f).sound(SoundType.WOOD)), true);
			
			createBlock("chaosbark_door", new DoorBlock(Block.Properties.of(Material.WOOD).strength(2f, 6f).sound(SoundType.WOOD).noOcclusion(), SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN), true);
			createBlock("chaosbark_trapdoor", new TrapDoorBlock(Block.Properties.of(Material.WOOD).strength(2f, 6f).sound(SoundType.WOOD).noOcclusion(), SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN), true);
			createBlock("chaosbark_fence", new ChaosbarkFenceBlock(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).noOcclusion().strength(2f, 6f)), true);
			createBlock("chaosbark_fence_gate", new FenceGateBlock(Block.Properties.of(Material.WOOD).strength(2f, 6f).sound(SoundType.WOOD).noOcclusion(), SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN), true);

			createBlock("smooth_corrupt_stone", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("large_corrupt_stone_bricks", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("corrupt_stone_conduit_node", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("corrupt_stone_conduit", new RotatedPillarBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
			createBlock("corrupt_stone_conduit_strikethrough_tile", new RotatedPillarBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
			createBlock("corrupt_stone_jewelled_strikethrough_tile", new RotatedPillarBlock(Block.Properties.of(Material.STONE).strength(3f, 9f).sound(SoundType.STONE)), true);
			createBlock("corrupt_stone_panel", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("corrupt_stone_tile", Material.STONE, 3f, 9f, SoundType.STONE, false, true);
			createBlock("corrupt_stone_pillar", new BlockFancyPillar(Material.STONE, 3f, 9f, SoundType.STONE), true);
			
			createBlock("chaosbark_sapling", new BlockModdedSapling(new ResourceLocation(AssemblyLineMachines.MODID, "chaosbark/chaosbark_random"), CorruptTallGrassBlock.CORRUPT_GRASS), true);
			createBlock("chaosweed", new CorruptTallGrassBlock(), true);
			createBlock("blooming_chaosweed", new CorruptTallGrassBlock(), true);
			createBlock("tall_chaosweed", new CorruptDoubleTallGrassBlock(), true);
			createBlock("tall_blooming_chaosweed", new CorruptDoubleTallGrassBlock(), true);
			createBlock("mandelbloom", new CorruptFlowerBlock(() -> MobEffects.CONFUSION, 15, 0), true);
			createBlock("prism_rose", new CorruptFlowerBlock(() -> MobEffects.GLOWING, 30, 7), true);
			createBlock("bright_prism_rose", new CorruptFlowerBlock(() -> MobEffects.GLOWING, 240, 15), true);
			createBlock("brain_cactus", new BrainCactusBlock(Block.Properties.of(Material.CACTUS).strength(3f, 9f).randomTicks().sound(SoundType.WOOL)), true);

			createBlock("flerovium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
			createBlock("energized_gold_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
			createBlock("attuned_titanium_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);

			createBlock("novasteel_block", Material.METAL, 5f, 20f, SoundType.METAL, false, true);
			createBlock("nova_farmland", new BlockMystiumFarmland(false), false);

			createBlock("ultimate_battery_cell", new BlockBatteryCell(BatteryCellStats.ULTIMATE), true);

			createBlock("attuned_titanium_fluid_tank", new BlockFluidTank(5000000, TemperatureResistance.HOT, 0xffe69ee6), true);

			createBlock("novasteel_fluid_tank", new BlockFluidTank(50000000, TemperatureResistance.HOT, 0xff111d63), true);
			createBlock("prism_glass", new Block(Block.Properties.of(Material.GLASS).sound(SoundType.GLASS).lightLevel((state) -> 15).noOcclusion()), true);

			createBlock("raw_chromium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_NETHERITE_TOOL, true);
			createBlock("raw_titanium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, true);
			createBlock("raw_flerovium_block", Material.STONE, 5f, 6f, SoundType.STONE, true, BlockTags.MINEABLE_WITH_PICKAXE, TagMaster.NEEDS_MYSTIUM_TOOL, true);

			createBlock("experience_siphon", new BlockExperienceSiphon(), true);
			createBlock("omnivoid", new BlockOmnivoid(), true);
			createBlock("greenhouse", new BlockGreenhouse(), true);

			createBlock("cocoa_sapling", new BlockModdedSapling(new ResourceLocation(AssemblyLineMachines.MODID, "cocoa_tree"), PlantType.PLAINS), true);
			createBlock("cocoa_leaves", new LeavesBlock(Block.Properties.of(Material.LEAVES).sound(SoundType.GRASS).randomTicks().noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never)), true);

			BLOCKS.forEach((k, v) -> h.register(k, v));
		});
		
		event.register(Keys.FLUIDS, (h) -> {
			createFluid("oil", (b) -> new SplitFluid(b, 5, basicFFFProperties("oil").levelDecreasePerBlock(2).tickRate(25)), Either.left(SplitFluid.effectLiquidBlock("oil", () -> List.of(new MobEffectInstance(MobEffects.BLINDNESS, 60), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3)))), true, FluidType.Properties.create().canSwim(false).temperature(400), (f) -> f.color(0, 0, 0).fog(6f));
			createFluid("naphtha", FluidNaphtha::new, Either.left(SplitFluid.effectLiquidBlock("naphtha", () -> List.of(new MobEffectInstance(DEEP_BURN.get(), 300, 0)), Block.Properties.of(Material.LAVA).lightLevel((state) -> 11).noLootTable())), true, FluidType.Properties.create().temperature(2200).canDrown(false), (f) -> f.color(222, 79, 22).fog(11f));
			createFluid("condensed_void", FluidCondensedVoid::new, Either.left(new FluidCondensedVoidBlock()), true, FluidType.Properties.create().temperature(-200).canSwim(false).canPushEntity(false).canDrown(false).viscosity(9000).motionScale(0.0005).fallDistanceModifier(0.25f), (f) -> f.color(0, 0, 0).fog(1f));
			createFluid("gasoline", (b) -> new FluidGasLike(b, basicFFFProperties("gasoline")), Either.left(SplitFluid.effectLiquidBlock("gasoline", () -> List.of(new MobEffectInstance(MobEffects.POISON, 60, 2), new MobEffectInstance(MobEffects.HUNGER, 60, 3)))), true, FluidType.Properties.create().canSwim(false).temperature(350).viscosity(2500), (f) -> f.color(122, 104, 0).fog(18f));
			createFluid("diesel", (b) -> new FluidGasLike(b, basicFFFProperties("diesel")), Either.left(SplitFluid.effectLiquidBlock("diesel", () -> List.of(new MobEffectInstance(MobEffects.POISON, 60, 2), new MobEffectInstance(MobEffects.HUNGER, 60, 3)))), true, FluidType.Properties.create().canSwim(false).temperature(350).viscosity(2500), (f) -> f.color(82, 69, 0).fog(15f));
			createFluid("liquid_carbon", (b) -> new FluidGasLike(b, basicFFFProperties("liquid_carbon")), Either.left(SplitFluid.effectLiquidBlock("liquid_carbon", () -> List.of(new MobEffectInstance(MobEffects.POISON, 60, 2), new MobEffectInstance(MobEffects.HUNGER, 60, 3)))), true, FluidType.Properties.create().canSwim(false).temperature(350).viscosity(2500), (f) -> f.color(110, 110, 110).fog(8f));
			createFluid("dark_energy", (b) -> new SplitFluid(b, basicFFFProperties("dark_energy").tickRate(3)), Either.left(SplitFluid.effectLiquidBlock("dark_energy", () -> List.of(new MobEffectInstance(DARK_EXPULSION.get(), 129, 0, false, false, true)))), true, FluidType.Properties.create().temperature(-100).canSwim(false).canDrown(false).canPushEntity(false), (f) -> f.color(0, 0, 0).fog(() -> ItemChaoticReductionGoggles.modifyFogColor()));
			createFluid("liquid_experience", (b) -> new SplitFluid(b, basicFFFProperties("liquid_experience")), Either.right(true), true, FluidType.Properties.create().temperature(35), (f) -> f.color(0, 184, 18).fog(24f));
			createFluid("glacier_water", FluidGlacierWater::new, Either.right(true), true, FluidType.Properties.create().temperature(-100).motionScale(0.00025).canSwim(false).canPushEntity(false), (f) -> f.color(114, 154, 219).fog(11.5f));
			createFluid("propane", Optional.of(new GasFluid("propane")), Optional.empty(), Either.right(false), false, FluidType.Properties.create().temperature(0).density(-100), (f) -> f.color(95, 15, 28));
			createFluid("propylene", Optional.of(new GasFluid("propylene")), Optional.empty(), Either.right(false), false, FluidType.Properties.create().temperature(0).density(-100), (f) -> f.color(40, 13, 85));
			createFluid("ethane", Optional.of(new GasFluid("ethane")), Optional.empty(), Either.right(false), false, FluidType.Properties.create().temperature(0).density(-100), (f) -> f.color(19, 95, 15));
			createFluid("ethylene", Optional.of(new GasFluid("ethylene")), Optional.empty(), Either.right(false), false, FluidType.Properties.create().temperature(0).density(-100), (f) -> f.color(179, 170, 195));
			
			FLUIDS.forEach((k, v) -> h.register(k, v));
		});

		//BLOCK ENTITIES
		event.register(Keys.BLOCK_ENTITY_TYPES, (h) -> {
			Utils.getAllMethods(BlockMachines.class, BlockEntityType.class, "Entity").forEach(Registry::createBlockEntity);
			createBlockEntity("pipe_connector", PipeConnectorTileEntity.class, Lists.transform(TransmissionType.getPipeRegistryValues(), (t) -> BLOCKS.get(t.getLeft())));
			createBlockEntity("hand_grinder", TEHandGrinder.class);
			createBlockEntity("fluid_bath", TEFluidBath.class);
			createBlockEntity("simple_crank_charger", TESimpleCrankCharger.class);
			createBlockEntity("gearbox", TEGearbox.class);
			createBlockEntity("fluid_tank", TEFluidTank.class, getBlock("wooden_fluid_tank"), getBlock("steel_fluid_tank"), getBlock("mystium_fluid_tank"), getBlock("attuned_titanium_fluid_tank"), getBlock("novasteel_fluid_tank"));
			createBlockEntity("battery_cell", TEBatteryCell.class, getBlock("basic_battery_cell"), getBlock("advanced_battery_cell"), getBlock("ultimate_battery_cell"));
			createBlockEntity("coal_generator", TECoalGenerator.class);
			createBlockEntity("crankmill", TECrankmill.class);
			createBlockEntity("autocrafting_table", TEAutocraftingTable.class);
			createBlockEntity("pump", TEPump.class);
			createBlockEntity("tool_charger", TEToolCharger.class);
			createBlockEntity("refinery", TERefinery.class);
			createBlockEntity("fluid_router", TEFluidRouter.class);
			createBlockEntity("interactor", TEInteractor.class);
			createBlockEntity("vacuum_hopper", TEVacuumHopper.class);
			createBlockEntity("bottomless_storage_unit", TEBottomlessStorageUnit.class);
			createBlockEntity("fluid_generator", TEFluidGenerator.class, getBlock("geothermal_generator"), getBlock("combustion_generator"));
			createBlockEntity("experience_hopper", TEExperienceHopper.class);
			createBlockEntity("powered_spawner", TEPoweredSpawner.class);
			createBlockEntity("experience_mill", TEExperienceMill.class);
			createBlockEntity("quarry", TEQuarry.class);
			createBlockEntity("quantum_link", TEQuantumLink.class);
			createBlockEntity("entropy_reactor", TEEntropyReactor.class, getBlock("entropy_reactor_block"));
			createBlockEntity("entropy_reactor_slave", TEEntropyReactorSlave.class, getBlock("entropy_reactor_block"));
			createBlockEntity("corrupting_basin", TECorruptingBasin.class);
			createBlockEntity("experience_siphon", TEExperienceSiphon.class);
			createBlockEntity("omnivoid", TEOmnivoid.class);
			createBlockEntity("greenhouse", TEGreenhouse.class);

			BLOCK_ENTITIES.forEach((k, v) -> h.register(k, v));
		});

		//CONTAINERS
		event.register(Keys.MENU_TYPES, (h) -> {
			Utils.getAllMethods(BlockMachines.class, MenuType.class, "Container").forEach(Registry::createContainer);
			createContainer("gearbox", ContainerGearbox.class);
			createContainer("pipe_connector", PipeConnectorContainer.class);
			createContainer("coal_generator", ContainerCoalGenerator.class);
			createContainer("crankmill", ContainerCrankmill.class);
			createContainer("battery_cell", ContainerBatteryCell.class);
			createContainer("autocrafting_table", ContainerAutocraftingTable.class);
			createContainer("refinery", ContainerRefinery.class);
			createContainer("fluid_router", ContainerFluidRouter.class);
			createContainer("interactor", ContainerInteractor.class);
			createContainer("bottomless_storage_unit", ContainerBottomlessStorageUnit.class);
			createContainer("fluid_generator", ContainerFluidGenerator.class);
			createContainer("powered_spawner", ContainerPoweredSpawner.class);
			createContainer("experience_mill", ContainerExperienceMill.class);
			createContainer("quarry", ContainerQuarry.class);
			createContainer("quantum_link", ContainerQuantumLink.class);
			createContainer("entropy_reactor", ContainerEntropyReactor.class);
			createContainer("corrupting_basin", ContainerCorruptingBasin.class);
			createContainer("omnivoid", ContainerOmnivoid.class);
			createContainer("greenhouse", ContainerGreenhouse.class);

			CONTAINERS.forEach((k, v) -> h.register(k, v.getFirst()));
		});

		//CRAFTING
		event.register(Keys.RECIPE_TYPES, (h) -> {
			
			createRecipe(GrinderCrafting.GRINDER_RECIPE, GrinderCrafting.SERIALIZER);
			createRecipe(BathCrafting.BATH_RECIPE, BathCrafting.SERIALIZER);
			createRecipe(PurifierCrafting.PURIFIER_RECIPE, PurifierCrafting.SERIALIZER);
			createRecipe(AlloyingCrafting.ALLOYING_RECIPE, AlloyingCrafting.SERIALIZER);
			createRecipe(FluidInGroundRecipe.FIG_RECIPE, FluidInGroundRecipe.SERIALIZER);
			createRecipe(RefiningCrafting.REFINING_RECIPE, RefiningCrafting.SERIALIZER);
			createRecipe(EnchantmentBookCrafting.ENCHANTMENT_BOOK_RECIPE, EnchantmentBookCrafting.SERIALIZER);
			createRecipe(LumberCrafting.LUMBER_RECIPE, LumberCrafting.SERIALIZER);
			createRecipe(PneumaticCrafting.PNEUMATIC_RECIPE, PneumaticCrafting.SERIALIZER);
			createRecipe(EntropyReactorCrafting.ERO_RECIPE, EntropyReactorCrafting.SERIALIZER);
			createRecipe(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE, WorldCorruptionCrafting.SERIALIZER);
			createRecipe(GeneratorFluidCrafting.GENFLUID_RECIPE, GeneratorFluidCrafting.SERIALIZER);
			createRecipe(UpgradeKitCrafting.UPGRADING_RECIPE, UpgradeKitCrafting.SERIALIZER);
			createRecipe(GreenhouseCrafting.GREENHOUSE_RECIPE, GreenhouseCrafting.SERIALIZER);
			createRecipe(GreenhouseFertilizerCrafting.FERTILIZER_RECIPE, GreenhouseFertilizerCrafting.SERIALIZER);
			
			RECIPE_TYPES.forEach((k, v) -> h.register(k, v));
		});
		
		event.register(Keys.RECIPE_SERIALIZERS, (h) -> {
			RECIPE_SERIALIZERS.forEach((k, v) -> h.register(k, v));
		});

		event.register(Keys.FLUID_TYPES, (h) -> FLUID_TYPES.forEach((k, v) -> h.register(k, v)));
		event.register(Keys.WORLD_CARVERS, (h) -> h.register("chaos_plane_carver", new ChaosPlaneCarver()));
	}

	//ENTITIES
	@SubscribeEvent
	public static void registerEntAttributes(EntityAttributeCreationEvent event) {
		event.put(CORRUPT_SHELL.get(), EntityCorruptShell.registerAttributeMap().build());
	}

	public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
		event.register(CORRUPT_SHELL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMobSpawnRules, Operation.AND);
	}
	
	//COMMON SETUP
	@SubscribeEvent
	public static void common(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			craftingConditions();
		});
	}

	//CREATIVE TAB
	@SubscribeEvent
	public static void makeTab(CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation(AssemblyLineMachines.MODID, "creative_tab"), builder -> 
				builder.title(Component.translatable("item_group." + AssemblyLineMachines.MODID + ".creative_tab"))
				.icon(() -> new ItemStack(Registry.getItem("pure_steel_gear")))
				.displayItems((flags, populator, hasPermissions) -> {
					ArrayList<Item> items = new ArrayList<>(getAllItems());
					items.sort(new Comparator<Item>() {

						@Override
						public int compare(Item i1, Item i2) {
							if(i1 instanceof BlockItem && !(i2 instanceof BlockItem)) {
								return -1;
							}else if(i2 instanceof BlockItem && !(i1 instanceof BlockItem)) {
								return 1;
							}else {
								return i1.getName(i1.getDefaultInstance()).getContents().toString().compareTo(i2.getName(i2.getDefaultInstance()).getContents().toString());

							}
						}
					});
					
					for(Item i : items) {
						populator.accept(i);
					}
				})
				.withBackgroundLocation(new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/creative/page.png"))
				.withTabsImage(new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/creative/tabs.png"))
				);
	}
	
	//CRAFTING CONDITIONS
	private static void craftingConditions() {
		CraftingHelper.register(ConfigConditionSerializer.INSTANCE);
	}

	//CLIENT SETUP
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		FLUIDS.values().forEach((f) -> ItemBlockRenderTypes.setRenderLayer(f, RenderType.translucent()));
		
		BlockEntityRenderers.register((BlockEntityType<TEFluidTank>)getBlockEntity("fluid_tank"), new TankTER());
		BlockEntityRenderers.register((BlockEntityType<TEPoweredSpawner>)getBlockEntity("powered_spawner"), new PoweredSpawnerTER());
		BlockEntityRenderers.register((BlockEntityType<TEQuantumLink>)getBlockEntity("quantum_link"), new QuantumLinkTER());

		Utils.invokeAllMethods(BlockMachines.class, Void.TYPE);
		registerScreen("gearbox", ContainerGearbox.class, ScreenGearbox.class);
		registerScreen("pipe_connector", PipeConnectorContainer.class, PipeConnectorScreen.class);
		registerScreen("coal_generator", ContainerCoalGenerator.class, ScreenCoalGenerator.class);
		registerScreen("crankmill", ContainerCrankmill.class, ScreenCrankmill.class);
		registerScreen("battery_cell", ContainerBatteryCell.class, ScreenBatteryCell.class);
		registerScreen("autocrafting_table", ContainerAutocraftingTable.class, ScreenAutocraftingTable.class);
		registerScreen("refinery", ContainerRefinery.class, ScreenRefinery.class);
		registerScreen("fluid_router", ContainerFluidRouter.class, ScreenFluidRouter.class);
		registerScreen("interactor", ContainerInteractor.class, ScreenInteractor.class);
		registerScreen("bottomless_storage_unit", ContainerBottomlessStorageUnit.class, ScreenBottomlessStorageUnit.class);
		registerScreen("fluid_generator", ContainerFluidGenerator.class, ScreenFluidGenerator.class);
		registerScreen("experience_mill", ContainerExperienceMill.class, ScreenExperienceMill.class);
		registerScreen("powered_spawner", ContainerPoweredSpawner.class, ScreenPoweredSpawner.class);
		registerScreen("quarry", ContainerQuarry.class, ScreenQuarry.class);
		registerScreen("quantum_link", ContainerQuantumLink.class, ScreenQuantumLink.class);
		registerScreen("entropy_reactor", ContainerEntropyReactor.class, ScreenEntropyReactor.class);
		registerScreen("corrupting_basin", ContainerCorruptingBasin.class, ScreenCorruptingBasin.class);
		registerScreen("omnivoid", ContainerOmnivoid.class, ScreenOmnivoid.class);
		registerScreen("greenhouse", ContainerGreenhouse.class, ScreenGreenhouse.class);
		

		ItemProperties.registerGeneric(new ResourceLocation(AssemblyLineMachines.MODID, "active"), (stack, level, entity, seed) -> stack.getItem() instanceof IToolWithCharge charge ? charge.getActivePropertyState(stack, entity) : 0f);
		ItemProperties.register(getItem("wrench_o_matic"), new ResourceLocation(AssemblyLineMachines.MODID, "wrenchmode"), (stack, level, entity, seed) -> stack.getOrCreateTag().getInt("assemblylinemachines:wrenchmode") /2f);
	}

	//BLOCK COLORS
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
		event.register((state, reader, pos, tint) -> reader.getBlockEntity(pos) instanceof TEGreenhouse greenhouse ? greenhouse.getBlockState().getValue(BlockGreenhouse.SPROUT).getTint(greenhouse.getItem(1).getItem(), tint) : 0, getBlock("greenhouse"));
		event.register((state, reader, pos, tint) -> reader.getBlockTint(pos, BiomeColors.FOLIAGE_COLOR_RESOLVER), getBlock("cocoa_leaves"));
		event.register((state, reader, pos, tint) -> {
			if(reader.getBlockEntity(pos) instanceof IMachineDataBridge bridge) {
				BathCrafting bc = (BathCrafting) bridge.getCurrentRecipe();
				if(bc != null) return bc.getFluidTextureColor();
			}
			return 0;
		}, getBlock("kinetic_fluid_mixer"), getBlock("electric_fluid_mixer"), getBlock("mkii_fluid_mixer"));
		event.register((state, reader, pos, tint) -> {
			if(reader.getBlockEntity(pos) instanceof TEFluidBath bath) {
				if(bath.getRecipe() != null) {
					return bath.getRecipe().getOutputTextureColor();
				}else {
					return switch(bath.getBlockState().getValue(BlockFluidBath.FLUID)) {
					case LAVA -> 0xcb3d07;
					case WATER -> BiomeColors.getAverageWaterColor(reader, pos);
					case NONE -> 0;
				};}
			}
			return 0;
		}, getBlock("fluid_bath"));
	}

	//ITEM COLORS
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
		event.register((stack, index) -> {
			if(stack.hasTag()) {
				EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(stack.getTag().getString("assemblylinemachines:mob")));
				if(entity != null) return ItemMobCrystal.MOB_COLORS.getUnchecked(entity);
			}
			return 0x7d7d7d;
		}, getItem("mob_crystal"));
	}

	//ENTITY RENDERERS
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerEntityRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(CORRUPT_SHELL.get(), new EntityCorruptShellRenderFactory());
	}

	//DATAGEN
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) throws Exception {

		craftingConditions();
		
		Field f = ObfuscationReflectionHelper.findField(GatherDataEvent.class, "config");
		f.trySetAccessible();
		Collection<Path> inputs = ((DataGeneratorConfig) f.get(event)).getInputs();
		
		PrintWriter pw = new PrintWriter("logs/almdatagen.log", "UTF-8");
		pw.println("[DATAGEN SYSTEM - INFO]: Commencing ALM data generation...");
		
		if(event.includeServer()) {
			new DataProviderContainer(pw, event);
			new AutoRecipeGenerator(event, pw, inputs);
			new LootTableGenerator(event, pw);
		}
		if(event.includeClient()) {
			new ItemModelGenerator(event, pw, inputs);
		}
		
		event.getGenerator().run();
		pw.close();
	}


	//===============================================

	//ITEMS
	private static void createItem(String... names) {
		for(String s : names) {
			createItem(s);
		}
	}

	private static void createItem(String name) {

		Item.Properties properties = new Item.Properties();
		createItem(name, properties);
	}

	static void createItem(String name, Item.Properties properties) {
		Item i = new Item(properties);
		createItem(name, i);
	}

	static void createItem(String name, Item item) {
		ITEMS.put(name, item);
	}

	public static Item getItem(String name) {
		return ITEMS.get(name);
	}

	public static Collection<Item> getAllItems(){
		return Collections.unmodifiableCollection(ITEMS.values());
	}

	//BLOCKS
	private static void createBlock(String name, Material material, float hardness, float resistance, SoundType sound, boolean requireToolToDrop, TagKey<Block> type, TagKey<Block> level, boolean item) {
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

	private static void createBlock(String name, Block.Properties properties, TagKey<Block> type, TagKey<Block> level, boolean item) {
		class BlockWithTags extends Block implements TagMaster.IMiningLevelDataGenProvider{

			public BlockWithTags(Properties p) {super(p);}
			@Override
			public TagKey<Block> getToolType() {return type;}
			@Override
			public TagKey<Block> getToolLevel() {return level;}

		}

		createBlock(name, new BlockWithTags(properties), item);
	}

	private static void createBlock(String name, Block.Properties properties, boolean item) {
		Block b = new Block(properties);
		createBlock(name, b, item);
	}

	public static void createBlock(String name, Block block, boolean item) {
		BLOCKS.put(name, block);
		if(item) {
			createItem(name, new BlockItem(block, new Item.Properties()));
		}

	}

	public static Block getBlock(String name) {
		return BLOCKS.get(name);
	}

	public static Collection<Block> getAllBlocks(){
		return Collections.unmodifiableCollection(BLOCKS.values());
	}

	//FLUIDS

	public static Fluid getFluid(String name) {
		return FLUIDS.get(name);
	}

	public static FluidType getFluidType(String name) {
		return FLUID_TYPES.get(name);
	}

	public static Set<Entry<String, Fluid>> getAllFluids(){
		return Collections.unmodifiableSet(FLUIDS.entrySet());
	}

	public static ForgeFlowingFluid.Properties basicFFFProperties(String name){
		return new ForgeFlowingFluid.Properties(() -> Registry.FLUID_TYPES.get(name), () -> Registry.FLUIDS.get(name), () -> Registry.FLUIDS.get(name + "_flowing")).block(() -> (LiquidBlock) BLOCKS.get(name + "_block")).bucket(() -> ITEMS.get(name + "_bucket"));
	}

	public static void createFluid(String name, Function<Boolean, Fluid> fluidFunction, Either<LiquidBlock, Boolean> block, boolean bucket, FluidType.Properties typeProperties) {
		createFluid(name, Optional.of(fluidFunction.apply(true)), Optional.of(fluidFunction.apply(false)), block, bucket, typeProperties);
	} 

	public static void createFluid(String name, Optional<Fluid> still, Optional<Fluid> flowing, Either<LiquidBlock, Boolean> block, boolean bucket, FluidType.Properties typeProperties) {
		createFluid(name, still, flowing, block, bucket, typeProperties, null);
	}

	public static void createFluid(String name, Function<Boolean, Fluid> fluidFunction, Either<LiquidBlock, Boolean> block, boolean bucket, FluidType.Properties typeProperties, Function<SpecialRenderFluidType, SpecialRenderFluidType> typeModifier) {
		createFluid(name, Optional.of(fluidFunction.apply(true)), Optional.of(fluidFunction.apply(false)), block, bucket, typeProperties, typeModifier);
	}

	public static void createFluid(String name, Optional<Fluid> still, Optional<Fluid> flowing, Either<LiquidBlock, Boolean> block, boolean bucket, FluidType.Properties typeProperties, Function<SpecialRenderFluidType, SpecialRenderFluidType> typeModifier) {
		SpecialRenderFluidType srft = new SpecialRenderFluidType(typeProperties.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
				.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.LAVA_EXTINGUISH), name, flowing.isPresent());
		if(typeModifier != null) srft = typeModifier.apply(srft);
		FLUID_TYPES.put(name, srft);

		if(still.isPresent()) FLUIDS.put(name, still.get());
		if(flowing.isPresent()) FLUIDS.put(name + "_flowing", flowing.get());

		if(block.left().isPresent() || block.right().get() == true) {
			LiquidBlock regBlock = block.left().isPresent() ? block.left().get() : new LiquidBlock(() -> (FlowingFluid) FLUIDS.get(name), Block.Properties.of(Material.WATER).noLootTable());
			BLOCKS.put(name + "_block", regBlock);
		}

		if(bucket) ITEMS.put(name + "_bucket", new BucketItem(() -> FLUIDS.get(name), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
	}

	//TILE ENTITIES
	public static BlockEntityType<?> getBlockEntity(String name){
		return BLOCK_ENTITIES.get(name);
	}

	public static void createBlockEntity(String name, BlockEntityType<?> type) {
		BLOCK_ENTITIES.put(name, type);
	}

	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz, List<Block> blocks) {
		createBlockEntity(name, clazz, blocks.toArray(new Block[blocks.size()]));
	}

	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz, Block... blocks){
		BLOCK_ENTITIES.put(name, BlockEntityType.Builder.of((pos, state) -> {

			T inst;
			try {
				inst = clazz.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				inst = null;
				e.printStackTrace();
			}
			return inst;
		}, blocks).build(null));
	}

	public static <T extends BlockEntity> void createBlockEntity(String name, Class<T> clazz) {
		createBlockEntity(name, clazz, Registry.getBlock(name));
	}

	//CONTAINERS, CONTAINER IDS, SCREENS
	public static MenuType<?> getContainerType(String name){
		return CONTAINERS.get(name).getFirst();
	}

	public static Integer getContainerId(String name){
		return CONTAINERS.get(name).getSecond();
	}

	public static <T extends AbstractContainerMenu> void createContainer(String name, Class<T> clazz) {
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
		});

		createContainer(name, mt);
	}

	public static void createContainer(String name, MenuType<?> menu) {

		int id = CONTAINERS.entrySet().stream().max((entry1, entry2) -> entry1.getValue().getSecond() > entry2.getValue().getSecond() ? 1 : -1).map((o) -> o.getValue().getSecond() + 1).orElse(1000);
		CONTAINERS.put(name, Pair.of(menu, id));
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
	public static void createRecipe(RecipeType<?> type, RecipeSerializer<?> serializer) {
		RECIPE_TYPES.put(type.toString().split(":")[1], type);
		RECIPE_SERIALIZERS.put(type.toString().split(":")[1], serializer);
	}
}
