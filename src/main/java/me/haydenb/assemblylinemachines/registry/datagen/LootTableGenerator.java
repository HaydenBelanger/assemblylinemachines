package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.misc.BlockBlackGranite;
import me.haydenb.assemblylinemachines.block.misc.BlockCorruptOres;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.CompoundTagBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

public class LootTableGenerator extends LootTableProvider {

	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> providers = ImmutableList.of(Pair.of(GeneratedBlockLoot::new, LootContextParamSets.BLOCK));
	private final PrintWriter pw;

	public LootTableGenerator(GatherDataEvent event, PrintWriter pw) {
		super(event.getGenerator());
		event.getGenerator().addProvider(true, this);
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

		@Override
		protected void addTables() {
			pw.println("[LOOT TABLES - INFO]: Starting block loot table generation...");

			this.add(Registry.getBlock("titanium_ore"), (b) -> createOreDrop(b, Registry.getItem("raw_titanium")));
			this.add(Registry.getBlock("deepslate_titanium_ore"), (b) -> createOreDrop(b, Registry.getItem("raw_titanium")));
			this.add(Registry.getBlock("chromium_ore"), (b) -> createOreDrop(b, Registry.getItem("raw_chromium")));
			this.add(Registry.getBlock("flerovium_ore"), (b) -> createOreDrop(b, Registry.getItem("raw_flerovium")));
			this.add(Registry.getBlock("corrupt_basalt_empowered_coal_ore"), (b) -> createOreDrop(b, Registry.getItem("empowered_coal")));

			this.dropOther(Registry.getBlock("mystium_farmland"), Blocks.DIRT);
			this.dropOther(Registry.getBlock("nova_farmland"), Blocks.DIRT);

			List.of("novasteel_fluid_tank", "attuned_titanium_fluid_tank", "mystium_fluid_tank", "steel_fluid_tank", "wooden_fluid_tank").forEach((s) -> this.nbtCopyDrop(Registry.getBlock(s), "upgraded", "fluidstack"));
			List.of("basic_battery_cell", "advanced_battery_cell", "ultimate_battery_cell").forEach((s) -> this.nbtCopyDrop(Registry.getBlock(s), "stored", "fptout", "in", "creative"));
			this.nbtCopyDrop(Registry.getBlock("bottomless_storage_unit"), "stored", "storeditem", "creative");

			Stream.of(BlockCorruptOres.values()).forEach((co) -> {
				String name = co.toString().toLowerCase();
				List.of("corrupt_" + name + "_ore", "corrupt_basalt_" + name + "_ore").forEach((s) -> dynamicOreDrops(Registry.getBlock(s), co.itemDrop.get(), co.minDrop, co.maxDrop));
			});

			this.add(Registry.getBlock("black_granite"), (b) -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).add(LootItem.lootTableItem(b).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockBlackGranite.NATURAL_GRANITE, false))).otherwise(LootItem.lootTableItem(b).when(MatchTool.toolMatches(ItemPredicate.Builder.item().hasNbt(CompoundTagBuilder.builder().bool("canbreakblackgranite", true).build())))))));

			this.dropWhenSilkTouch(Registry.getBlock("prism_glass"));

			this.add(Registry.getBlock("chaosweed"), BlockLoot::createShearsOnlyDrop);
			this.add(Registry.getBlock("blooming_chaosweed"), BlockLoot::createShearsOnlyDrop);
			this.add(Registry.getBlock("tall_chaosweed"), BlockLoot::createDoublePlantShearsDrop);
			this.add(Registry.getBlock("tall_blooming_chaosweed"), BlockLoot::createDoublePlantShearsDrop);

			this.add(Registry.getBlock("chaosbark_door"), BlockLoot::createDoorTable);

			this.add(Registry.getBlock("slab_black_granite"), BlockLoot::createSlabItemTable);
			this.add(Registry.getBlock("slab_silt_brick"), BlockLoot::createSlabItemTable);
			this.add(Registry.getBlock("chaosbark_slab"), BlockLoot::createSlabItemTable);

			this.add(Registry.getBlock("chaosbark_leaves"), (b) -> createLeavesDrops(b, Registry.getBlock("chaosbark_sapling"), 0.05f, 0.0625f, 0.083333336f, 0.1f));
			this.leavesWithAdditionalDrop(Registry.getBlock("cocoa_leaves"), Registry.getBlock("cocoa_sapling"), Items.COCOA_BEANS, 0.05f, 0.0625f, 0.083333336f, 0.1f);

			for(Block b : getKnownBlocks()) if(!this.map.containsKey(b.getLootTable()) && !b.getLootTable().equals(BuiltInLootTables.EMPTY) && !b.asItem().equals(Items.AIR)) this.dropSelf(b);

			pw.println("[LOOT TABLES - INFO]: Successfully generated " + this.map.size() + " loot table(s).");
		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return Registry.getAllBlocks();
		}

		private void dynamicOreDrops(Block block, ItemLike resultItem, float min, float max){
			this.add(block, createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(resultItem).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))));
		}

		private void nbtCopyDrop(Block block, String... keys){
			CopyNbtFunction.Builder builder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
			Stream.of(keys).map((k) -> "assemblylinemachines:" + k).forEach((k) -> builder.copy(k, k));
			this.add(block, createSingleItemTable(block).apply(builder));
		}

		private void leavesWithAdditionalDrop(Block leaves, Block sapling, ItemLike additional, float... chances) {
			this.add(leaves, createLeavesDrops(leaves, sapling, chances).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(applyExplosionCondition(leaves, LootItem.lootTableItem(additional)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, chances)))));
		}
	}
}