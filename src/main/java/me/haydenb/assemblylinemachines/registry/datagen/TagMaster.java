package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.data.tags.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class TagMaster {

	//Below is all in reference to Mining Level and Tool Type, referenced by the block data generator.
	public static final Named<Block> NEEDS_NETHERITE_TOOL = ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, new ResourceLocation("forge", "needs_netherite_tool"));
	public static final Named<Block> NEEDS_MYSTIUM_TOOL = ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "needs_mystium_tool"));
	
	
	private static final HashMap<Material, Optional<Named<Block>>> MATERIAL_TOOL = new HashMap<>();
	static {
		MATERIAL_TOOL.put(Material.AMETHYST, Optional.of(BlockTags.MINEABLE_WITH_PICKAXE));
		MATERIAL_TOOL.put(Material.DIRT, Optional.of(BlockTags.MINEABLE_WITH_SHOVEL));
		MATERIAL_TOOL.put(Material.GRASS, Optional.of(BlockTags.MINEABLE_WITH_SHOVEL));
		MATERIAL_TOOL.put(Material.HEAVY_METAL, Optional.of(BlockTags.MINEABLE_WITH_PICKAXE));
		MATERIAL_TOOL.put(Material.METAL, Optional.of(BlockTags.MINEABLE_WITH_PICKAXE));
		MATERIAL_TOOL.put(Material.NETHER_WOOD, Optional.of(BlockTags.MINEABLE_WITH_AXE));
		MATERIAL_TOOL.put(Material.SAND, Optional.of(BlockTags.MINEABLE_WITH_SHOVEL));
		MATERIAL_TOOL.put(Material.SNOW, Optional.of(BlockTags.MINEABLE_WITH_SHOVEL));
		MATERIAL_TOOL.put(Material.STONE, Optional.of(BlockTags.MINEABLE_WITH_PICKAXE));
		MATERIAL_TOOL.put(Material.WOOD, Optional.of(BlockTags.MINEABLE_WITH_AXE));
		MATERIAL_TOOL.put(Material.CLAY, Optional.of(BlockTags.MINEABLE_WITH_SHOVEL));
		MATERIAL_TOOL.put(Material.GLASS, Optional.empty());
		MATERIAL_TOOL.put(Material.PLANT, Optional.empty());
		MATERIAL_TOOL.put(Material.REPLACEABLE_PLANT, Optional.empty());
		MATERIAL_TOOL.put(Material.CACTUS, Optional.empty());
		MATERIAL_TOOL.put(Material.FIRE, Optional.empty());
	}
	
	//This is for general tagging.
	private static final ListMultimap<WrappedNamed, String> TAG_SINGLE_MASTER = ArrayListMultimap.create();
	private static final ListMultimap<WrappedNamed, Pair<String, String>> TAG_GROUP_MASTER = ArrayListMultimap.create();
	static {
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/gears/all"), List.of(Pair.of("assemblylinemachines", "crafting/gears/industrial"), Pair.of("assemblylinemachines", "crafting/gears/precious")));
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/gears/industrial"), List.of(Pair.of("forge", "gears/copper"), Pair.of("forge", "gears/iron"), Pair.of("forge", "gears/steel")));
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/gears/precious"), List.of(Pair.of("forge", "gears/gold"), Pair.of("forge", "gears/titanium"), Pair.of("forge", "gears/flerovium")));
		
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/entropy_reactor_outputs"), List.of("semi_dense_neutron_matter", "quark_matter", "strange_matter"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/hammers"), List.of("steel_hammer", "crank_hammer", "titanium_hammer", "mystium_hammer", "novasteel_hammer"));
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/organics"), List.of(Pair.of("minecraft", "flowers"), Pair.of("minecraft", "leaves"), Pair.of("minecraft", "saplings")));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/sawdust"), List.of("sawdust", "warped_sawdust", "crimson_sawdust", "chaotic_sawdust"));
		
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/carbon_small"), List.of("minecraft:coal", "minecraft:charcoal", "ground_coal", "ground_charcoal"));
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/carbon_small"), List.of(Pair.of("minecraft", "coal_ores")));
		TAG_GROUP_MASTER.putAll(wrap(TagType.ITEM, "crafting/carbon_large"), List.of(Pair.of("forge", "storage_blocks/coal")));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/prism_roses"), List.of("prism_rose", "bright_prism_rose"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/mushroom_blocks"), List.of("minecraft:brown_mushroom_block", "minecraft:red_mushroom_block", "minecraft:mushroom_stem"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "world/gas_flammable"), List.of("naphtha_fire", "naphtha_block", "minecraft:torch", "minecraft:redstone_torch", "minecraft:soul_torch"));
		TAG_GROUP_MASTER.putAll(wrap(TagType.BLOCK, "world/gas_flammable"), List.of(Pair.of("minecraft", "campfires"), Pair.of("minecraft", "fire"), Pair.of("minecraft", "strider_warm_blocks")));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.ITEM, "crafting/shulker_boxes"), List.of("minecraft:shulker_box", "minecraft:black_shulker_box", "minecraft:blue_shulker_box", "minecraft:brown_shulker_box",
				"minecraft:cyan_shulker_box", "minecraft:gray_shulker_box", "minecraft:green_shulker_box", "minecraft:light_blue_shulker_box", "minecraft:light_gray_shulker_box", "minecraft:lime_shulker_box", "minecraft:magenta_shulker_box",
				"minecraft:orange_shulker_box", "minecraft:pink_shulker_box", "minecraft:purple_shulker_box", "minecraft:red_shulker_box", "minecraft:white_shulker_box", "minecraft:yellow_shulker_box"));
		
		TAG_GROUP_MASTER.putAll(wrap(TagType.BLOCK, "world/mystium_axe_mineable"), List.of(Pair.of("minecraft", "logs")));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "world/naphtha_fireproof"), List.of("smoldering_stone"));
		
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "dragon_immune"), List.of("smoldering_stone"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "wither_immune"), List.of("smoldering_stone"));
		
		TAG_GROUP_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "logs").copy(), List.of(Pair.of("assemblylinemachines", "chaosbark_logs")));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "leaves").copy(), List.of("chaosbark_leaves"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "chaosbark_logs").copy(), List.of("chaosbark_log", "stripped_chaosbark_log"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "saplings").copy(), List.of("chaosbark_sapling"));
		TAG_SINGLE_MASTER.putAll(wrap(TagType.BLOCK, "minecraft", "flowers").copy(), List.of("prism_rose"));
		
		ListMultimap<String, Pair<String, String>> forgeTags = ArrayListMultimap.create();
		forgeTags.putAll("dusts", List.of(Pair.of("charcoal", "ground_charcoal"), Pair.of("coal", "ground_coal"), 
				Pair.of("copper", "ground_copper"), Pair.of("electrified_netherite", "electrified_netherite_blend"), 
				Pair.of("gold", "ground_gold"), Pair.of("iron", "ground_iron"), Pair.of("lapis", "ground_lapis_lazuli"), 
				Pair.of("mystium", "mystium_blend"), Pair.of("netherite", "ground_netherite"), Pair.of("titanium", "ground_titanium"),
				Pair.of("flerovium", "ground_flerovium"), Pair.of("diamond", "ground_diamond"), Pair.of("novasteel", "nova_blend"), Pair.of("prismatic", "prismatic_dust"),
				Pair.of("electrified_netherite", "electrified_netherite_blend"), Pair.of("sawdust", "sawdust;warped_sawdust;crimson_sawdust;chaotic_sawdust"), Pair.of("chromium", "ground_chromium")));
		forgeTags.putAll("gears", List.of(Pair.of("copper", "copper_gear"), Pair.of("gold", "gold_gear"), Pair.of("iron", "iron_gear"),
				Pair.of("steel", "steel_gear"), Pair.of("titanium", "titanium_gear"), Pair.of("flerovium", "flerovium_gear")));
		forgeTags.putAll("ingots", List.of(Pair.of("attuned_titanium", "attuned_titanium_ingot"), Pair.of("chromium", "chromium_ingot"),
				Pair.of("energized_gold", "energized_gold_ingot"), Pair.of("mystium", "mystium_ingot"), Pair.of("pure_copper", "pure_copper"), Pair.of("pure_gold", "pure_gold"),
				Pair.of("pure_iron", "pure_iron"), Pair.of("pure_steel", "pure_steel"), Pair.of("pure_titanium", "pure_titanium"), Pair.of("steel", "steel_ingot"), 
				Pair.of("titanium", "titanium_ingot"), Pair.of("flerovium", "flerovium_ingot"), Pair.of("raw_novasteel", "raw_novasteel_compound"), Pair.of("novasteel", "novasteel_ingot")));
		forgeTags.putAll("nuggets", List.of(Pair.of("chromium", "chromium_nugget"), Pair.of("steel", "steel_nugget"), Pair.of("titanium", "titanium_nugget"), Pair.of("flerovium", "flerovium_nugget")));
		forgeTags.putAll("plates", List.of(Pair.of("attuned_titanium", "attuned_titanium_plate"), Pair.of("chromium", "chromium_plate"), Pair.of("copper", "copper_plate"), 
				Pair.of("energized_gold", "energized_gold_plate"), Pair.of("gold", "gold_plate"), Pair.of("iron", "iron_plate"), Pair.of("mystium", "mystium_plate"),
				Pair.of("stainless_steel", "stainless_steel_plate"), Pair.of("steel", "steel_plate"), Pair.of("titanium", "titanium_plate"), Pair.of("flerovium", "flerovium_plate"),
				Pair.of("novasteel", "novasteel_plate")));
		forgeTags.putAll("rods", List.of(Pair.of("steel", "steel_rod"), Pair.of("graphene", "graphene_rod")));
		forgeTags.putAll("sheets", List.of(Pair.of("plastic", "plastic_sheet"), Pair.of("rubber", "rubber_sheet")));
		forgeTags.putAll("ores", List.of(Pair.of("chromium", "chromium_ore"), Pair.of("titanium", "titanium_ore;deepslate_titanium_ore"), Pair.of("flerovium", "flerovium_ore"),
				Pair.of("corrupt_coal", "corrupt_coal_ore"), Pair.of("corrupt_copper", "corrupt_copper_ore"), Pair.of("corrupt_diamond", "corrupt_diamond_ore"), Pair.of("corrupt_gold", "corrupt_gold_ore"), Pair.of("corrupt_iron", "corrupt_iron_ore"),
				Pair.of("corrupt_lapis", "corrupt_lapis_ore"), Pair.of("corrupt_redstone", "corrupt_redstone_ore"), Pair.of("corrupt_emerald", "corrupt_emerald_ore"), Pair.of("corrupt_titanium", "corrupt_titanium_ore")));
		forgeTags.putAll("storage_blocks", List.of(Pair.of("chromium", "chromium_block"), Pair.of("titanium", "titanium_block"), Pair.of("mystium", "mystium_block"), Pair.of("steel", "steel_block"),
				Pair.of("flerovium", "flerovium_block"), Pair.of("attuned_titanium", "attuned_titanium_block"), Pair.of("energized_gold", "energized_gold_block"), Pair.of("novasteel", "novasteel_block"), 
				Pair.of("raw_chromium", "raw_chromium_block"), Pair.of("raw_titanium", "raw_titanium_block"), Pair.of("raw_flerovium", "raw_flerovium_block")));
		forgeTags.putAll("raw_materials", List.of(Pair.of("chromium", "raw_chromium"), Pair.of("titanium", "raw_titanium"), Pair.of("flerovium", "raw_flerovium")));
		
		//Blocks get special treatment since they need to have both a block and item tag
		List<String> forgeBlockTypes = List.of("ores", "storage_blocks");
		for(String type : forgeTags.keySet()) {
			
			boolean isBlock = forgeBlockTypes.contains(type);
			TagType tt = isBlock ? TagType.BLOCK : TagType.ITEM;
			for(Pair<String, String> keys : forgeTags.get(type)) {
				for(String substr : keys.getSecond().split(";")) {
					TAG_SINGLE_MASTER.put(wrap(tt, "forge", type + "/" + keys.getFirst()).copy(isBlock), substr);
				}
				TAG_GROUP_MASTER.put(wrap(tt, "forge", type), Pair.of("forge", type + "/" + keys.getFirst()));
				if(isBlock) {
					TAG_GROUP_MASTER.put(wrap(TagType.ITEM, "forge", type), Pair.of("forge", type + "/" + keys.getFirst()));
				}
			}
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	static <T> void tagAllInMaster(DataProviderContainer provider, TagType type) {
		for(WrappedNamed wn : Stream.concat(TAG_SINGLE_MASTER.keySet().stream(), TAG_GROUP_MASTER.keySet().stream()).collect(Collectors.toSet())) {
			for(Object x : Stream.concat(TAG_SINGLE_MASTER.get(wn).stream(), TAG_GROUP_MASTER.get(wn).stream()).collect(Collectors.toList())) {
				x = x instanceof Pair ? new ResourceLocation(((Pair<String, String>) x).getFirst(), ((Pair<String, String>) x).getSecond()) : x;
				if(x instanceof String) {
					String[] split = ((String) x).split(":");
					if(split.length == 1) {
						split = new String[] {AssemblyLineMachines.MODID, split[0]};
					}
					x = wn.type == TagType.BLOCK ? ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0], split[1])) : ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
				}
				switch(wn.type){
				case BLOCK:
					if(type == wn.type) provider.blockProvider.passback(x, (Named<Block>) wn.named);
					if(wn.copy && type == TagType.ITEM) provider.itemProvider.passback((Named<Block>) wn.named, wn.namedItemCopy.orElseThrow());
					break;
				case ITEM:
					if(type == wn.type) provider.itemProvider.passback(x, (Named<Item>) wn.named);
				}
			}
		}
	}
	
	private static WrappedNamed wrap(TagType type, String modid, String path) {
		IForgeRegistry<?> reg = null;
		switch(type) {
		case BLOCK:
			reg = ForgeRegistries.BLOCKS;
			break;
		case ITEM:
			reg = ForgeRegistries.ITEMS;
			break;
		}
		return new WrappedNamed(ForgeTagHandler.makeWrapperTag(reg, new ResourceLocation(modid, path)), type);
	}
	
	private static WrappedNamed wrap(TagType type, String path) {
		return wrap(type, AssemblyLineMachines.MODID, path);
	}
	
	//WrappedNamed, used to store the type of tag as stored above.
	private static class WrappedNamed{
		private final TagType type;
		private final Named<?> named;
		private boolean copy;
		private Optional<Named<Item>> namedItemCopy;
		
		private WrappedNamed(Named<?> named, TagType type) {
			this.named = named;
			this.type = type;
			this.copy = false;
			this.namedItemCopy = Optional.empty();
		}
		
		private WrappedNamed copy() {
			if(this.type == TagType.ITEM) {
				throw new IllegalArgumentException("Cannot perform copy on a TagType.ITEM!");
			}
			copy = true;
			namedItemCopy = Optional.of(ForgeTagHandler.makeWrapperTag(ForgeRegistries.ITEMS, named.getName()));
			return this;
		}
		
		private WrappedNamed copy(boolean copy) {
			if(copy) return this.copy();
			return this;
		}
		
		@Override
		public String toString() {
			return named.getName().toString();
		}
	}
	
	//DataProviderContainer, stores the 3 providers so that all can be referenced, and handles logging.
	public static class DataProviderContainer{
		
		private final BlockDataProvider blockProvider;
		private final ItemDataProvider itemProvider;
		private final GatherDataEvent event;
		private final DataProviderContainer container;
		private final PrintWriter writer;
		
		public DataProviderContainer(PrintWriter pw, GatherDataEvent event) throws Exception {
			this.writer = pw;
			this.event = event;
			this.container = this;
			
			this.blockProvider = new BlockDataProvider();
			this.itemProvider = new ItemDataProvider();
			new FluidDataProvider();
		}
		
		//BlockDataProvider, runs tagAllInMaster as well as generates its own data based on mining level and tool type.
		public class BlockDataProvider extends BlockTagsProvider{
			
			public BlockDataProvider() {
				super(event.getGenerator(), AssemblyLineMachines.MODID, event.getExistingFileHelper());
				event.getGenerator().addProvider(this);
			}
			
			@Override
			protected void addTags() {
				writer.println("[SYSTEM]: Starting general block tagging...");
				TagMaster.tagAllInMaster(container, TagType.BLOCK);
				
				writer.println("[SYSTEM]: Starting Mining Level and Tool Type tagging...");
				for(Block b : Registry.getAllBlocksUnmodifiable()) {
					Optional<Pair<Optional<Named<Block>>, Optional<Named<Block>>>> result = getTagsForMiningLevel(b);
					if(result == null) {
						writer.println("[WARNING]: For " + b.getRegistryName() + ", a default tool was not exposed.");
					}else {
						if(result.isPresent()) {
							Pair<Optional<Named<Block>>, Optional<Named<Block>>> r2 = result.orElseThrow();
							if(r2.getFirst().isPresent()) {
								this.passback(b, r2.getFirst().orElseThrow());
							}
							if(r2.getSecond().isPresent()) {
								this.passback(b, r2.getSecond().orElseThrow());
							}
						}
					}
				}
			}
			
			public Optional<Pair<Optional<Named<Block>>, Optional<Named<Block>>>> getTagsForMiningLevel(Block b){
				if(b instanceof IMiningLevelDataGenProvider) {
					IMiningLevelDataGenProvider provider = (IMiningLevelDataGenProvider) b;
					return Optional.of(Pair.of(provider.getToolTypeOpt(), provider.getToolLevelOpt()));
				}else if(b instanceof LiquidBlock) {
					return Optional.empty();
				}else {
					
					Optional<Named<Block>> onb = MATERIAL_TOOL.get(b.defaultBlockState().getMaterial());
					return onb == null ? null : Optional.of(Pair.of(onb, Optional.empty()));
				}
			}
			
			public void passback(Object resource, Named<Block> block){
				if(resource == null) throwException(block);
				if(resource instanceof ResourceLocation) {
					this.tag(block).addTag(ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, (ResourceLocation) resource));
				}else if(resource instanceof Block) {
					this.tag(block).add((Block) resource);
				}else {
					throwException(block);
				}
				
				log(resource, block, TagType.BLOCK.toString());
			}
			
			
		}
		
		//ItemDataProvider, runs tagAllInMaster.
		public class ItemDataProvider extends ItemTagsProvider{
			
			public ItemDataProvider() {
				super(event.getGenerator(), blockProvider, AssemblyLineMachines.MODID, event.getExistingFileHelper());
				event.getGenerator().addProvider(this);
			}
			
			@Override
			protected void addTags() {
				writer.println("[SYSTEM]: Starting general item tagging...");
				TagMaster.tagAllInMaster(container, TagType.ITEM);
			}
			
			@SuppressWarnings("unchecked")
			public void passback(Object resource, Named<Item> item){
				if(resource == null) throwException(item);
				if(resource instanceof ResourceLocation) {
					this.tag(item).addTag(ForgeTagHandler.makeWrapperTag(ForgeRegistries.ITEMS, (ResourceLocation) resource));
				}else if(resource instanceof Item) {
					this.tag(item).add((Item) resource);
				}else if(resource instanceof Named) {
					this.copy((Named<Block>) resource, item);
				}else {
					throwException(item);
				}
				
				log(resource, item, TagType.ITEM.toString());
				
			}
			
		}
		
		//FluidDataProvider, does its own thing.
		public class FluidDataProvider extends FluidTagsProvider{

			public FluidDataProvider() {
				super(event.getGenerator(), AssemblyLineMachines.MODID, event.getExistingFileHelper());
				event.getGenerator().addProvider(this);
			}
			
			@Override
			protected void addTags() {
				writer.println("[SYSTEM]: Starting fluid tagging...");
				for(Fluid f : Registry.getAllFluids()) {
					if(f.isSource(f.defaultFluidState()) && Registry.getBlock(f.getRegistryName().getPath() + "_block") != null) {
						writer.println("[FLUID]: Created tag for " + f.getRegistryName() + ".");
						Named<Fluid> named = ForgeTagHandler.makeWrapperTag(ForgeRegistries.FLUIDS, new ResourceLocation(AssemblyLineMachines.MODID, f.getRegistryName().getPath()));
						this.tag(named).add(f);
						Fluid flowing = Registry.getFluid(f.getRegistryName().getPath() + "_flowing");
						if(flowing != null) this.tag(named).add(flowing);
					}
				}
				
			}
			
		}
		
		private void throwException(Named<?> named) {
			throw new IllegalArgumentException("Attempt to tag " + named.getName().toString() + " failed: Resource is either null or not any appropriate Object type for the TagType.");
		}
		
		private void log(Object resource, Named<?> named, String descr) {
			String objDesc;
			if(resource instanceof Named) {
				objDesc = ((Named<?>) resource).getName().toString();
			}else {
				objDesc = resource.toString();
			}
			
			writer.println("[" + descr + "]: " + objDesc + " tagged to " + named.getName().toString() + ".");
			
		}
	}
	
	public static enum TagType{
		BLOCK, ITEM;
	}

	//IMiningLevelDataGenProvider, Blocks can implement this to specify the mining level and tool type.
	public static interface IMiningLevelDataGenProvider {
	
		public Named<Block> getToolType();
		
		public Named<Block> getToolLevel();
		
		default public Optional<Named<Block>> getToolTypeOpt(){
			return this.getToolType() != null ? Optional.of(this.getToolType()) : Optional.empty();
		}
		
		default public Optional<Named<Block>> getToolLevelOpt(){
			return this.getToolLevel() != null ? Optional.of(this.getToolLevel()) : Optional.empty();
		}
	}

	
}
