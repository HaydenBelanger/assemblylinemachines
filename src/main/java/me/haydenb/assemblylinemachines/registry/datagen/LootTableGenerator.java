package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.OreManager.CorruptOres;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class LootTableGenerator extends LootTableProvider {

	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> providers = ImmutableList.of(Pair.of(GeneratedBlockLoot::new, LootContextParamSets.BLOCK));
	private final Collection<Path> inputFolders;
	private final PrintWriter pw;
	
	public LootTableGenerator(GatherDataEvent event, PrintWriter pw) {
		super(event.getGenerator());
		event.getGenerator().addProvider(this);
		this.inputFolders = event.getGenerator().getInputFolders();
		this.pw = pw;
	}
	
	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootContextParamSet>> getTables() {
		return providers;
	}
	
	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((rl, lootTable) ->{
			LootTables.validate(validationtracker, rl, lootTable);
		});
	}
	
	private class GeneratedBlockLoot extends BlockLoot{
		
		private static final List<String> BLOCK_ONLY_EXCEPTIONS = List.of("mystium_farmland", "nova_farmland");
		
		private final List<String> existingLootTables = getExistingLootTables();
		
		private static final HashMap<String, Triple<Supplier<Item>, Float, Float>> ORE_DROPS = new HashMap<>();
		static {
			CorruptOres.oreDropMap(ORE_DROPS);
			ORE_DROPS.put("deepslate_titanium_ore", Triple.of(() -> Registry.getItem("raw_titanium"), -1f, -1f));
			ORE_DROPS.put("titanium_ore", Triple.of(() -> Registry.getItem("raw_titanium"), -1f, -1f));
			ORE_DROPS.put("chromium_ore", Triple.of(() -> Registry.getItem("raw_chromium"), -1f, -1f));
			ORE_DROPS.put("flerovium_ore", Triple.of(() -> Registry.getItem("raw_flerovium"), -1f, -1f));
		}
		
		private static final HashMap<String, Block> LEAVES_SAPLINGS = new HashMap<>();
		static {
			LEAVES_SAPLINGS.put("chaosbark_leaves", Registry.getBlock("chaosbark_sapling"));
			LEAVES_SAPLINGS.put("cocoa_leaves", Registry.getBlock("cocoa_sapling"));
		}
		
		private static final HashMap<String, List<String>> NBT_COPYING_KEYS = new HashMap<>();
		static {
			for(String str : new String[] {"novasteel", "attuned_titanium", "mystium", "steel", "wooden"}) {
				NBT_COPYING_KEYS.put(str + "_fluid_tank", List.of("fluidstack", "upgraded"));
			}
			for(String str : new String[] {"basic", "advanced", "ultimate"}) {
				NBT_COPYING_KEYS.put(str + "_battery_cell", List.of("stored", "fptout", "in", "creative"));
			}
			NBT_COPYING_KEYS.put("bottomless_storage_unit", List.of("stored", "storeditem", "creative"));
			
			NBT_COPYING_KEYS.replaceAll(new BiFunction<String, List<String>, List<String>>() {
				@Override
				public List<String> apply(String t, List<String> u) {
					List<String> nl = new ArrayList<>();
					for(String str : u) {
						nl.add(AssemblyLineMachines.MODID + ":" + str);
					}
					return nl;
				}
			});
			
		}
		
		
		
		@Override
		protected void addTables() {
			pw.println("[LOOT TABLES - INFO]: Starting block loot table generation...");
			
			Iterator<Block> knownBlocks = getKnownBlocks().iterator();
			
			int skipped = 0;
			for(@SuppressWarnings("unused") String str : existingLootTables) {
				skipped++;
			}
			
			int i = 0;
			while(knownBlocks.hasNext()) {
				Block b = knownBlocks.next();
				switch(b.getRegistryName().getPath()) {
				
				case "titanium_ore", "deepslate_titanium_ore",
						"chromium_ore", "flerovium_ore" -> 
					//Ore loot tables - drops Raw Ore.
					this.add(b, (bl) -> createOreDrop(bl, ORE_DROPS.get(bl.getRegistryName().getPath()).getLeft().get()));
				
				case "mystium_farmland", "nova_farmland" -> 
					//Farmland loot tables - drops Dirt.
					this.dropOther(b, Blocks.DIRT);
					
				case "novasteel_fluid_tank", "attuned_titanium_fluid_tank", "mystium_fluid_tank",
				"steel_fluid_tank", "wooden_fluid_tank", "basic_battery_cell", "advanced_battery_cell",
				"ultimate_battery_cell", "bottomless_storage_unit" -> {
					//NBT-copying loot tables - copys required NBT data from BlockEntity to Item.
					CopyNbtFunction.Builder nbtFunction = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
					for(String key : NBT_COPYING_KEYS.get(b.getRegistryName().getPath())) {
						nbtFunction = nbtFunction.copy(key, key);
					}
					this.add(b, createSingleItemTable(b).apply(nbtFunction));
				}
					
				case "corrupt_coal_ore", "corrupt_copper_ore", "corrupt_diamond_ore",
				"corrupt_emerald_ore", "corrupt_gold_ore", "corrupt_iron_ore", "corrupt_lapis_ore",
				"corrupt_redstone_ore", "corrupt_titanium_ore", "corrupt_basalt_coal_ore",
				"corrupt_basalt_copper_ore", "corrupt_basalt_diamond_ore", "corrupt_basalt_emerald_ore",
				"corrupt_basalt_gold_ore", "corrupt_basalt_iron_ore", "corrupt_basalt_lapis_ore",
				"corrupt_basalt_redstone_ore", "corrupt_basalt_titanium_ore" -> {
					Triple<Supplier<Item>, Float, Float> stats = ORE_DROPS.get(b.getRegistryName().getPath());
					this.add(b, createDynamicOreDrops(b, stats.getLeft().get(), stats.getMiddle(), stats.getRight()));
				}
				
				case "prism_glass" ->
					//Silk Touch loot tables - only drops when Silk Touch is used.
					this.dropWhenSilkTouch(b);
					
				case "chaosweed", "blooming_chaosweed" ->
					//Short plant loot tables - drops when shears are used.
					this.add(b, BlockLoot::createShearsOnlyDrop);
				
				case "tall_chaosweed", "tall_blooming_chaosweed" -> 
					//Tall plant loot tables - drops 2x when shears are used from one part of plant.
					this.add(b, BlockLoot::createDoublePlantShearsDrop);
				
				case "chaosbark_door" ->
					//Door loot tables - only drops from one segment of door.
					this.add(b, BlockLoot::createDoorTable);
				
				case "slab_black_granite", "slab_silt_brick", "chaosbark_slab" ->
					//Slab loot tables - if double slab, drops 2.
					this.add(b, BlockLoot::createSlabItemTable);
				
				case "chaosbark_leaves", "cocoa_leaves" ->
					//Leaves loot tables - occasionally drops Saplings, and leaves if shears are used.
					this.add(b, (bl) -> createLeavesDrops(bl, LEAVES_SAPLINGS.get(b.getRegistryName().getPath()), new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F}));
					
				default -> {
					if(!b.asItem().equals(Items.AIR)) {
						this.dropSelf(b);
					}
				}
					
				}
				i++;
			}
			
			pw.println("[LOOT TABLES - INFO]: Processed and generated loot tables for " + i + " block(s).");
			if(skipped != 0) pw.println("[LOOT TABLES - INFO]: Skipped " + skipped + " block(s) which had an existing loot table file in an input directory.");
			List<Block> skippedBlks = excludedBlocks();
			if(!skippedBlks.isEmpty()) {
				int fluidSkips = 0;
				int emptySkips = 0;
				List<Block> otherSkips = new ArrayList<>();
				for(Block b : skippedBlks) {
					if(b instanceof LiquidBlock) {
						fluidSkips++;
					}else if(b.getLootTable().equals(BuiltInLootTables.EMPTY)) {
						emptySkips++;
					}else {
						otherSkips.add(b);
					}
				}
				if(fluidSkips != 0) pw.println("[LOOT TABLES - INFO]: Skipped " + fluidSkips + " block(s) which are LiquidBlocks and expected to be skipped as a result.");
				if(emptySkips != 0) pw.println("[LOOT TABLES - INFO]: Skipped " + emptySkips + " block(s) which are set as noDrops in their Block.Properties.");
				if(!otherSkips.isEmpty()) {
					String blocks = "";
					for(Block b : otherSkips) {
						blocks = blocks + b.getRegistryName().toString() + ", ";
					}
					blocks = blocks.substring(0, blocks.length() - 2);
					pw.println("[LOOT TABLES - WARNING]: There were blocks skipped which were skipped for not having an associated BlockItem, and not marked as having noDrops or given an alternate drop: " + blocks);
				}
			}
		}
		
		@Override
		protected Iterable<Block> getKnownBlocks() {
			List<Block> recipesToApply = Registry.getAllBlocksModifiable();
			recipesToApply.removeIf((b) -> b.asItem().equals(Items.AIR) && !BLOCK_ONLY_EXCEPTIONS.contains(b.getRegistryName().getPath()));
			recipesToApply.removeIf((b) -> existingLootTables.contains(b.getRegistryName().getPath()));
			recipesToApply.removeIf((b) -> b.getLootTable().equals(BuiltInLootTables.EMPTY));
			
			return recipesToApply;
		}
		
		private List<Block> excludedBlocks(){
			return Registry.getAllBlocksUnmodifiable().stream().filter((b) -> (b.asItem().equals(Items.AIR) && !BLOCK_ONLY_EXCEPTIONS.contains(b.getRegistryName().getPath())) || b.getLootTable().equals(BuiltInLootTables.EMPTY)).toList();
		}
		
		private List<String> getExistingLootTables(){
			List<File> files = new ArrayList<>();
			for(Path p : inputFolders) {
				File f = new File(p.toString() + "/data/" + AssemblyLineMachines.MODID + "/loot_tables/blocks/");
				files.addAll(Lists.newArrayList(f.listFiles()));
			}
			return Lists.transform(files, (f) -> FilenameUtils.getBaseName(f.getPath()));
		}
		
		private static LootTable.Builder createDynamicOreDrops(Block block, ItemLike resultItem, float min, float max){
			return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(resultItem).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));
		}
		
	}
}