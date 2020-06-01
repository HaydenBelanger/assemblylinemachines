package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
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
import me.haydenb.assemblylinemachines.block.BlockSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.BlockSimpleFluidMixer.ContainerSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.BlockSimpleFluidMixer.ScreenSimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.BlockSimpleFluidMixer.TESimpleFluidMixer;
import me.haydenb.assemblylinemachines.block.BlockSimpleGrinder;
import me.haydenb.assemblylinemachines.block.BlockSimpleGrinder.ContainerSimpleGrinder;
import me.haydenb.assemblylinemachines.block.BlockSimpleGrinder.ScreenSimpleGrinder;
import me.haydenb.assemblylinemachines.block.BlockSimpleGrinder.TESimpleGrinder;
import me.haydenb.assemblylinemachines.block.pipe.EnergyPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.FluidPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorContainer;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity.ItemPipeConnectorScreen;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.ItemGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemGearboxUpgrade;
import me.haydenb.assemblylinemachines.item.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.item.ToolStirringStick;
import me.haydenb.assemblylinemachines.item.ToolStirringStick.TemperatureResistance;
import me.haydenb.assemblylinemachines.misc.CreativeTab;
import me.haydenb.assemblylinemachines.misc.FluidProperty;
import me.haydenb.assemblylinemachines.misc.FluidProperty.Fluids;
import me.haydenb.assemblylinemachines.misc.ItemTiers;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
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
	private static final HashMap<String, Item> itemRegistry = new HashMap<>();
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
		
		createItem("steel_plate");
		createItem("steel_rod");
		createItem("wooden_board");
		
		createItem("empowered_coal", new ItemGearboxFuel(3200));
		createItem("gearbox_upgrade_limiter", new ItemGearboxUpgrade("Gearbox will only burn fuel while action is required."));
		createItem("gearbox_upgrade_efficiency", new ItemGearboxUpgrade("Gearbox will use greatly less fuel.", "Gearbox will run half as quickly."));
		createItem("gearbox_upgrade_speed", new ItemGearboxUpgrade("Gearbox will run much faster.", "Gearbox will use twice as much fuel."));
		createItem("gearbox_upgrade_compatability", new ItemGearboxUpgrade("Gearbox can use any burnable fuel."));
		
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
		
		event.getRegistry().registerAll(itemRegistry.values().toArray(new Item[itemRegistry.size()]));
		
		
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		createBlock("titanium_ore", Material.ROCK, 3f, 15f, 3, ToolType.PICKAXE, SoundType.STONE);
		createBlock("titanium_block", Material.IRON, 5f, 20f, 3, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("hand_grinder", new BlockHandGrinder());
		createBlock("fluid_bath", new BlockFluidBath());
		
		createBlock("crank", new BlockCrank());
		createBlock("gearbox", new BlockGearbox());
		createBlock("simple_fluid_mixer", new BlockSimpleFluidMixer());
		createBlock("simple_grinder", new BlockSimpleGrinder());
		
		createBlock("item_pipe", new PipeBase<IItemHandler>(() -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Type.ITEM));
		createBlock("fluid_pipe", new PipeBase<IFluidHandler>(() -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Type.FLUID));
		createBlock("energy_pipe", new PipeBase<IEnergyStorage>(() -> CapabilityEnergy.ENERGY, Type.POWER));
		
		createBlock("steel_fluid_tank", new BlockFluidTank(20000, TemperatureResistance.HOT));
		createBlock("wooden_fluid_tank", new BlockFluidTank(6000, TemperatureResistance.COLD));
		
		createBlock("silt", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_brick", Material.ROCK, 4f, 12f, 0, ToolType.PICKAXE, SoundType.STONE);
		createBlock("steel_block", Material.IRON, 7f, 30f, 3, ToolType.PICKAXE, SoundType.METAL);
		
		createBlock("silt_iron", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_gold", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		createBlock("silt_titanium", Material.CLAY, 1f, 2f, 0, ToolType.SHOVEL, SoundType.GROUND);
		
		event.getRegistry().registerAll(blockRegistry.values().toArray(new Block[blockRegistry.size()]));
	}
	
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		createTileEntity("hand_grinder", TEHandGrinder.class);
		createTileEntity("fluid_bath", TEFluidBath.class);
		createTileEntity("simple_fluid_mixer", TESimpleFluidMixer.class);
		createTileEntity("simple_grinder", TESimpleGrinder.class);
		createTileEntity("fluid_tank", TEFluidTank.class, blockRegistry.get("wooden_fluid_tank"), blockRegistry.get("steel_fluid_tank"));
		createTileEntity("gearbox", TEGearbox.class);
		createTileEntity("pipe_connector_item", ItemPipeConnectorTileEntity.class, blockRegistry.get("item_pipe"));
		createTileEntity("fluid_connector_item", FluidPipeConnectorTileEntity.class, blockRegistry.get("fluid_pipe"));
		createTileEntity("energy_connector_item", EnergyPipeConnectorTileEntity.class, blockRegistry.get("energy_pipe"));
		
		event.getRegistry().registerAll(teRegistry.values().toArray(new TileEntityType<?>[teRegistry.size()]));
	}
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
		createContainer("simple_fluid_mixer", 1050, ContainerSimpleFluidMixer.class);
		createContainer("simple_grinder", 1051, ContainerSimpleGrinder.class);
		createContainer("gearbox", 1052, ContainerGearbox.class);
		createContainer("pipe_connector_item", 1053, ItemPipeConnectorContainer.class);
		
		event.getRegistry().registerAll(containerRegistry.values().toArray(new ContainerType<?>[containerRegistry.size()]));
		
	}
	
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(getBlock("steel_fluid_tank"), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(getBlock("wooden_fluid_tank"), RenderType.getCutout());
		
		registerScreen("simple_fluid_mixer", ContainerSimpleFluidMixer.class, ScreenSimpleFluidMixer.class);
		registerScreen("simple_grinder", ContainerSimpleGrinder.class, ScreenSimpleGrinder.class);
		registerScreen("gearbox", ContainerGearbox.class, ScreenGearbox.class);
		registerScreen("pipe_connector_item", ItemPipeConnectorContainer.class, ItemPipeConnectorScreen.class);
	}
	
	@SubscribeEvent
	public static void registerCrafting(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(GrinderCrafting.GRINDER_RECIPE.toString()), GrinderCrafting.GRINDER_RECIPE);
		event.getRegistry().register(GrinderCrafting.SERIALIZER.setRegistryName("grinder"));
		
		net.minecraft.util.registry.Registry.register(net.minecraft.util.registry.Registry.RECIPE_TYPE, new ResourceLocation(BathCrafting.BATH_RECIPE.toString()), BathCrafting.BATH_RECIPE);
		event.getRegistry().register(BathCrafting.SERIALIZER.setRegistryName("bath"));
		
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
