package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.crafting.AlloyingCrafting.AlloyResult;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting.GrinderResult;
import me.haydenb.assemblylinemachines.crafting.PneumaticCrafting.Mold;
import me.haydenb.assemblylinemachines.crafting.PneumaticCrafting.PneumaticResult;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.config.ConfigCondition;
import me.haydenb.assemblylinemachines.registry.utils.CountIngredient;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class AutoRecipeGenerator extends RecipeProvider {

	private static final LinkedHashMap<String, List<String>> EQUIPMENT_PATTERNS = new LinkedHashMap<>();
	static {
		EQUIPMENT_PATTERNS.put("helmet", List.of("AAA", "A A"));
		EQUIPMENT_PATTERNS.put("chestplate", List.of("A A", "AAA", "AAA"));
		EQUIPMENT_PATTERNS.put("leggings", List.of("AAA", "A A", "A A"));
		EQUIPMENT_PATTERNS.put("boots", List.of("A A", "A A"));
		EQUIPMENT_PATTERNS.put("pickaxe", List.of("AAA", " B ", " B "));
		EQUIPMENT_PATTERNS.put("axe", List.of("AA", "AB", " B"));
		EQUIPMENT_PATTERNS.put("sword", List.of("A", "A", "B"));
		EQUIPMENT_PATTERNS.put("shovel", List.of("A", "B", "B"));
		EQUIPMENT_PATTERNS.put("hoe", List.of("AA", " B", " B"));
		EQUIPMENT_PATTERNS.put("hammer", List.of(" A ", " BA", "B  "));
	}
	private static final List<String> BLOCK_PATTERN = List.of("AAA", "AAA", "AAA");
	private static final List<String> GEAR_PATTERN = List.of(" A ", "ABA", " A ");


	private final PrintWriter writer;
	private final Collection<Path> inputFolders;

	public AutoRecipeGenerator(GatherDataEvent event, PrintWriter openPw) {
		super(event.getGenerator());

		this.inputFolders = event.getGenerator().getInputFolders();
		this.writer = openPw;
		event.getGenerator().addProvider(true, this);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

		if(inputFolders.size() == 0) {
			writer.println("[FURNACE RECIPES - WARNING]: Datagen was not provided with any input folders, so no Blasting to Furnace auto-recipes will be generated.");
		}else {
			writer.println("[FURNACE RECIPES - INFO]: Starting Blasting to Furnace recipe auto-conversion with " + inputFolders.size() + " input folder(s)...");
			for(Path p : inputFolders) {
				File f = new File(p.toString() + "/data/" + AssemblyLineMachines.MODID.toLowerCase() + "/recipes/blasting/");
				writer.println("[FURNACE RECIPES - INFO]: Using input directory \"" + f.getPath() + "\"...");
				this.concursivelyCopyRecipe(consumer, f.getPath());
			}
		}

		writer.println("[FURNACE RECIPES - INFO]: Finished generating Blasting to Furnace recipe copy(s).");
		writer.println("[METAL RECIPES - INFO]: Starting generation of general metal-related Crafting, Grinding, and Compressing recipes...");

		ArrayList<FinishedRecipe> recipes = new ArrayList<>();

		//IMC
		new Builder(recipes, "constantan", true).alloy("copper", 1, "nickel", 1, 2).imcOptions();
		new Builder(recipes, "electrum", true).alloy("gold", 1, "silver", 1, 2).imcOptions();
		new Builder(recipes, "bronze", true).alloy("copper", 3, "tin", 1, 4).imcOptions();
		new Builder(recipes, "brass", true).alloy("copper", 1, "zinc", 1, 2).imcOptions();
		new Builder(recipes, "invar", true).alloy("iron", 2, "nickel", 1, 3).imcOptions();
		new Builder(recipes, "rose_gold", true).alloy("copper", 3, "gold", 1, 4).imcOptions();
		new Builder(recipes, "manyullyn", true).alloy(Ingredient.of(getNamed("forge", "ingots/cobalt")), 3, Ingredient.of(Items.NETHERITE_SCRAP), 1, 4).imcOptions();
		new Builder(recipes, "aluminum", true).imcOptions();
		new Builder(recipes, "lead", true).imcOptions();
		new Builder(recipes, "silver", true).imcOptions();
		new Builder(recipes, "nickel", true).imcOptions();
		new Builder(recipes, "uranium", true).imcOptions();
		new Builder(recipes, "tin", true).imcOptions();
		new Builder(recipes, "zinc", true).imcOptions();
		new Builder(recipes, "platinum", true).imcOptions();
		new Builder(recipes, "tungsten", true).imcOptions();
		new Builder(recipes, "osmium", true).imcOptions();
		new Builder(recipes, "cobalt", true).imcOptions();
		new Builder(recipes, "ardite", true).imcOptions();

		//Builtin
		new Builder(recipes, "raw_novasteel").alloy("flerovium", 1, "attuned_titanium", 1, 2);
		new Builder(recipes, "energized_gold").alloy(Pair.of("dusts", "redstone"), 3, Pair.of("ingots", "pure_gold"), 1, 1).storageBlock(false).plate(4, RecipeState.CONDITIONAL);
		new Builder(recipes, "coal").alternateInput(Ingredient.of(Items.COAL)).grinder(true, false, false, Blade.PUREGOLD, 4).storageBlock(true);
		new Builder(recipes, "diamond").alternateInput(Ingredient.of(getNamed("forge", "gems/diamond"))).grinder(true, false, false, Blade.PUREGOLD, 2).storageBlock(true);
		new Builder(recipes, "lapis").alternateInput(Ingredient.of(getNamed("forge", "gems/lapis"))).grinder(true, true, false, Blade.TITANIUM, 8).storageBlock(true);
		new Builder(recipes, "redstone").alternateInput(null).grinder(true, false, false, Blade.TITANIUM, 10).alternateInput(Ingredient.of(getNamed("forge", "dusts/redstone"))).storageBlock(true);
		new Builder(recipes, "emerald").alternateInput(Ingredient.of(getNamed("forge", "gems/emerald"))).grinder(true, false, false, Blade.PUREGOLD, 2).storageBlock(true);
		new Builder(recipes, "flerovium").grinder(false, true).storageBlock(false).nugget(false).plate(4, RecipeState.CONDITIONAL).gear(1, RecipeState.CONDITIONAL);
		new Builder(recipes, "chromium").grinder(false, true).storageBlock(false).nugget(false).plate(3, RecipeState.CONDITIONAL).rod();
		new Builder(recipes, "copper").grinder(true, false).storageBlock(true).nugget(true).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		new Builder(recipes, "iron").grinder(true, false).storageBlock(true).nugget(true).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		new Builder(recipes, "gold").grinder(true, false).storageBlock(true).nugget(true).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		new Builder(recipes, "titanium").grinder(true, false).armor().tools().storageBlock(false).nugget(false).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		new Builder(recipes, "steel").grinder().armor().tools().storageBlock(false).nugget(false).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		new Builder(recipes, "attuned_titanium").plate(3, RecipeState.CONDITIONAL).storageBlock(false).gear(1, RecipeState.CONDITIONAL).rod();
		new Builder(recipes, "amethyst").alternateInput(Ingredient.of(getNamed("forge", "gems/amethyst"))).grinder().storageBlock(true);
		new Builder(recipes, "crank").alternateInput(Ingredient.of(getNamed(AssemblyLineMachines.MODID, "precious_gears"))).alternateToolRod(Ingredient.of(getNamed("forge", "rods/steel"))).tools();
		new Builder(recipes, "crafting_table").storageBlockResult(Registry.getItem("compressed_crafting_table"), Items.CRAFTING_TABLE).storageBlock(false);
		new Builder(recipes, "raw_chromium").storageBlockResult(Registry.getItem("raw_chromium_block"), Registry.getItem("raw_chromium")).alternateInput(Ingredient.of(getNamed("forge", "raw_materials/chromium"))).storageBlock(false);
		new Builder(recipes, "raw_titanium").storageBlockResult(Registry.getItem("raw_titanium_block"), Registry.getItem("raw_titanium")).alternateInput(Ingredient.of(getNamed("forge", "raw_materials/titanium"))).storageBlock(false);
		new Builder(recipes, "raw_flerovium").storageBlockResult(Registry.getItem("raw_flerovium_block"), Registry.getItem("raw_flerovium")).alternateInput(Ingredient.of(getNamed("forge", "raw_materials/flerovium"))).storageBlock(false);
		new Builder(recipes, "pure_copper").plate(3, RecipeState.TRUE).gear(5, RecipeState.TRUE);
		new Builder(recipes, "pure_iron").plate(3, RecipeState.TRUE).gear(4, RecipeState.TRUE);
		new Builder(recipes, "pure_gold").plate(3, RecipeState.TRUE).gear(1, RecipeState.TRUE);
		new Builder(recipes, "pure_titanium").plate(5, RecipeState.TRUE).gear(2, RecipeState.TRUE);
		new Builder(recipes, "pure_steel").plate(8, RecipeState.TRUE).gear(3, RecipeState.TRUE);
		new Builder(recipes, "graphene").plate(1, RecipeState.CONDITIONAL).gear(1, RecipeState.CONDITIONAL).rod();
		new Builder(recipes, "plastic").alternateInput(Ingredient.of(Registry.getItem("plastic_ball"))).plate(3, true, RecipeState.CONDITIONAL);
		new Builder(recipes, "rubber").alternateInput(Ingredient.of(Registry.getItem("rubber_ball"))).plate(4, true, RecipeState.CONDITIONAL);
		new Builder(recipes, "novasteel").alloy(Pair.of("ingots", "raw_novasteel"), 1, Pair.of("dusts", "novasteel"), 1, 1).alternateToolRod(Ingredient.of(getNamed("forge", "rods/graphene")))
			.tools().storageBlock(false).plate(1, RecipeState.CONDITIONAL).gear(1, RecipeState.CONDITIONAL).rod();
		new Builder(recipes, "mystium").alloy(Pair.of("dusts", "mystium"), 1, Pair.of("dusts", "electrified_netherite"), 1, 1)
			.armor(() -> {
				LinkedHashMap<String, List<String>> patterns = new LinkedHashMap<>();
				patterns.put("helmet", List.of("BCB", "A A"));
				patterns.put("chestplate", List.of("B B", "BCB", "AAA"));
				patterns.put("leggings", List.of("AAA", "B B", "B B"));
				patterns.put("boots", List.of("A A", "B B"));
				return patterns;
			}, (s) -> {
				Ingredient[] ingredients;
				if(s.equals("helmet") || s.equals("chestplate")) {
					ingredients = new Ingredient[3];
					ingredients[2] = Ingredient.of(Registry.getItem("mystium_crystal"));
				}else {
					ingredients = new Ingredient[2];
				}
				ingredients[0] = Ingredient.of(getNamed("forge", "plates/pure_steel"));
				ingredients[1] = Ingredient.of(Registry.getItem("mystium_armor_plating"));
				return ingredients;
			}).alternateToolRod(Ingredient.of(getNamed("forge", "rods/steel"))).tools().storageBlock(false).plate(2, RecipeState.CONDITIONAL).gear(1, RecipeState.CONDITIONAL).rod();

		recipes.forEach((fr) -> consumer.accept(fr));

		writer.println("[METAL RECIPES - INFO]: Generated " + recipes.size() + " Metal recipe(s).");
	}

	private static class Builder{

		private final ArrayList<FinishedRecipe> recipes;
		private final String name;
		private final boolean modCompat;
		private Supplier<Ingredient> input;
		private Supplier<Ingredient> toolRod;
		private Supplier<Pair<Item, Item>> storageBlockResult;

		private Builder(ArrayList<FinishedRecipe> recipes, String name, boolean modCompat) {
			this.recipes = recipes;
			this.name = name;
			this.modCompat = modCompat;
			this.input = () -> Ingredient.of(getNamed("forge", "ingots/" + name));
			this.toolRod = () -> Ingredient.of(getNamed("forge", "rods/wooden"));
			this.storageBlockResult = () -> Pair.of(Registry.getItem(name + "_block"), Registry.getItem(name + "_ingot"));
		}

		private Builder(ArrayList<FinishedRecipe> recipes, String name) {
			this(recipes, name, false);
		}

		private Builder alternateInput(Ingredient input) {
			this.input = () -> input;
			return this;
		}

		private Builder alternateToolRod(Ingredient toolRod) {
			this.toolRod = () -> toolRod;
			return this;
		}

		private Builder storageBlockResult(Item resultBlock, Item resultIngot) {
			this.storageBlockResult = () -> Pair.of(resultBlock, resultIngot);
			return this.alternateInput(Ingredient.of(resultIngot));
		}

		private Builder imcOptions() {
			return this.grinder().storageBlock(true).nugget(true).plate(1, RecipeState.IMC).gear(1, RecipeState.IMC).rod();
		}

		private Builder alloy(String inputA, int inputACount, String inputB, int inputBCount, int outputCount) {
			return this.alloy(Pair.of("ingots", inputA), inputACount, Pair.of("ingots", inputB), inputBCount, outputCount);
		}

		private Builder alloy(Pair<String, String> inputA, int inputACount, Pair<String, String> inputB, int inputBCount, int outputCount) {
			return this.alloy(Ingredient.of(getNamed("forge", inputA.getKey() + "/" + inputA.getValue())), inputACount, Ingredient.of(getNamed("forge", inputB.getKey() + "/" + inputB.getValue())), inputBCount, outputCount);
		}

		private Builder alloy(Ingredient inputA, int inputACount, Ingredient inputB, int inputBCount, int outputCount) {
			recipes.add(new AlloyResult(getRecipeLoc("alloying", name), CountIngredient.of(inputA, inputACount), CountIngredient.of(inputB, inputBCount), getNamed("forge", "ingots/" + name), outputCount).addIMCIfTrue(modCompat));
			return this;
		}

		private Builder grinder() {
			return this.grinder(false, false);
		}

		private Builder grinder(boolean corruptOre, boolean machineRequired) {
			return this.grinder(corruptOre, machineRequired, true, Blade.TITANIUM, 2);
		}

		private Builder grinder(boolean corruptOre, boolean machineRequired, boolean hasRaw, Blade bladeType, int countFromOre) {
			recipes.add(new GrinderResult(getRecipeLoc("grinder", "block_ores/" + name), Ingredient.of(getNamed("forge", "ores/" + name)), getNamed("forge", "dusts/" + name), countFromOre, 10, 0f, machineRequired, bladeType).addIMCIfTrue(modCompat));
			if(hasRaw) {
				recipes.add(new GrinderResult(getRecipeLoc("grinder", "raw_ores/" + name), Ingredient.of(getNamed("forge", "raw_materials/" + name)), getNamed("forge", "dusts/" + name), 1, 5, 0.25f, machineRequired, bladeType).addIMCIfTrue(modCompat));
				recipes.add(new GrinderResult(getRecipeLoc("grinder", "raw_ore_blocks/" + name), Ingredient.of(getNamed("forge", "storage_blocks/raw_" + name)), getNamed("forge", "dusts/" + name), 9, 10, 0.25f, machineRequired, bladeType).addIMCIfTrue(modCompat));
			}
			if(input.get() != null) recipes.add(new GrinderResult(getRecipeLoc("grinder", "ingots/" + name), input.get(), getNamed("forge", "dusts/" + name), 1, 4, 0f, machineRequired, bladeType).addIMCIfTrue(modCompat));
			if(corruptOre) recipes.add(new GrinderResult(getRecipeLoc("grinder", "block_ores/corrupt_" + name), Ingredient.of(getNamed("forge", "ores/corrupt_" + name)), getNamed("forge", "dusts/" + name), Math.round(countFromOre * 1.5f), 15, 0f, true, Blade.TITANIUM));
			return this;
		}

		private Builder armor() {
			return this.armor(() -> EQUIPMENT_PATTERNS, (s) -> new Ingredient[] {this.input.get()});
		}

		private Builder armor(Supplier<LinkedHashMap<String, List<String>>> patterns, Function<String, Ingredient[]> ingredients) {
			for(String slot : patterns.get().keySet().stream().limit(4).toList()) {
				recipes.add(new AdvancementlessResult(getRecipeLoc("tools/" + name + "/" + name + "_" + slot), Registry.getItem(name + "_" + slot), 1, patterns.get().get(slot), getShapedKey(ingredients.apply(slot))));
			}
			return this;
		}

		private Builder tools() {
			for(String tool : EQUIPMENT_PATTERNS.keySet().stream().skip(4).toList()) {
				recipes.add(new AdvancementlessResult(getRecipeLoc("tools/" + name + "/" + name + "_" + tool), Registry.getItem(name + "_" + tool), 1, EQUIPMENT_PATTERNS.get(tool), getShapedKey(this.input.get(), this.toolRod.get())));
			}
			return this;
		}

		private Builder storageBlock(boolean metalShaperOnly) {
			recipes.add(new PneumaticResult(getRecipeLoc("pneumatic", "ingot_to_block/" + name), CountIngredient.of(input.get(), 9), getNamed("forge", "storage_blocks/" + name), 1, Mold.NONE, 9).addIMCIfTrue(modCompat, "compressorStorageBlockIMC"));
			if(!metalShaperOnly) {
				recipes.add(new AdvancementlessResult(getRecipeLoc("ingot_to_block/" + name), storageBlockResult.get().getKey(), 1, BLOCK_PATTERN, getShapedKey(this.input.get())));
				recipes.add(new AdvancementlessResult(getRecipeLoc("block_to_ingot/" + name), storageBlockResult.get().getValue(), 9, List.of(Ingredient.of(getNamed("forge", "storage_blocks/" + name)))));
			}
			return this;
		}

		private Builder nugget(boolean metalShaperOnly) {
			recipes.add(new PneumaticResult(getRecipeLoc("pneumatic", "nugget_to_ingot/" + name), CountIngredient.of(Ingredient.of(getNamed("forge", "nuggets/" + name)), 9), getNamed("forge", "ingots/" + name), 1, Mold.NONE, 5).addIMCIfTrue(modCompat, "compressorNuggetIMC"));
			if(!metalShaperOnly) {
				recipes.add(new AdvancementlessResult(getRecipeLoc("nugget_to_ingot/" + name), storageBlockResult.get().getValue(), 1, BLOCK_PATTERN, getShapedKey(Ingredient.of(getNamed("forge", "nuggets/" + name)))));
				recipes.add(new AdvancementlessResult(getRecipeLoc("ingot_to_nugget/" + name), Registry.getItem(name + "_nugget"), 9, List.of(this.input.get())));
			}
			return this;
		}

		private Builder plate(int plateCount, RecipeState state) {
			return this.plate(plateCount, false, state);
		}

		private Builder plate(int plateCount, boolean isSheet, RecipeState state) {
			String category = isSheet ? "sheet" : "plate";
			recipes.add(new PneumaticResult(getRecipeLoc("pneumatic/plates", name), CountIngredient.of(this.input.get()), getNamed("forge", category + "s/" + name), plateCount, Mold.PLATE, 7).addIMCIfTrue(state == RecipeState.IMC || modCompat, "compressorPlateIMC"));
			if(state != RecipeState.IMC) {
				AdvancementlessResult ar = new AdvancementlessResult(getRecipeLoc("plates/" + name), Registry.getItem(name + "_" + category), plateCount, List.of(this.input.get(), Ingredient.of(getNamed(AssemblyLineMachines.MODID, "hammers"))));
				if(state == RecipeState.CONDITIONAL) ar = ar.configCondition("lateGamePlatesRequireCompressor", false);
				recipes.add(ar);
			}
			return this;
		}

		private Builder gear(int gearCount, RecipeState state) {
			recipes.add(new PneumaticResult(getRecipeLoc("pneumatic/gears", name), CountIngredient.of(Ingredient.of(getNamed("forge", "plates/" + name)), 4), getNamed("forge", "gears/" + name), gearCount, Mold.GEAR, 9).addIMCIfTrue(state == RecipeState.IMC || modCompat, "compressorGearIMC"));
			if(state != RecipeState.IMC) {
				AdvancementlessResult ar = new AdvancementlessResult(getRecipeLoc("gears/" + name), Registry.getItem(name + "_gear"), 1, GEAR_PATTERN, getShapedKey(Ingredient.of(getNamed("forge", "plates/" + name)), Ingredient.of(getNamed("forge", "rods/wooden"))));
				if(state == RecipeState.CONDITIONAL) ar = ar.configCondition("lateGameGearsRequireCompressor", false);
				recipes.add(ar);
			}
			return this;
		}

		private Builder rod() {
			recipes.add(new PneumaticResult(getRecipeLoc("pneumatic/rods", name), CountIngredient.of(this.input.get()), getNamed("forge", "rods/" + name), 1, Mold.ROD, 8).addIMCIfTrue(modCompat, "compressorRodIMC"));
			return this;
		}
	}

	private void concursivelyCopyRecipe(Consumer<FinishedRecipe> consumer, String directoryName) {
		File currentDir = new File(directoryName);

		File[] fileList = currentDir.listFiles();
		if(fileList != null) {
			for(File file : fileList) {
				if(file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {

					try {
						String unixPath = FilenameUtils.separatorsToUnix(file.getPath());
						String[] pathParts = unixPath.split("blasting/");
						if(pathParts.length != 2) {

							throw new IOException("Splitting by \"blasting\" results in more or less than 2 segments of pathname.");
						}
						String rlPath = FilenameUtils.removeExtension(pathParts[1]);
						JsonObject json = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
						BlastingRecipe recipe = RecipeSerializer.BLASTING_RECIPE.fromJson(new ResourceLocation(AssemblyLineMachines.MODID, "blasting/" + rlPath), json);

						consumer.accept(new AdvancementlessResult(new ResourceLocation(AssemblyLineMachines.MODID, "smelting/" + rlPath), recipe.getIngredients().get(0), recipe.assemble(null).getItem(), recipe.getExperience(), recipe.getCookingTime() * 2));
						writer.println("[FURNACE RECIPES - INFO]: Copied blasting recipe " + rlPath + ".");
					}catch(Exception e) {

						writer.println("[FURNACE RECIPES - WARNING]: Generating recipe for " + file.getName() + " failed. View console for more info.");
						e.printStackTrace();
					}

				}else if(file.isDirectory()) {
					concursivelyCopyRecipe(consumer, file.getAbsolutePath());
				}
			}
		}
	}

	private static HashMap<Character, Ingredient> getShapedKey(Ingredient... ingredients){
		HashMap<Character, Ingredient> map = new HashMap<>();

		for(int i = 0; i < ingredients.length; i++) map.put(FormattingHelper.getCharForNumber(i + 1), ingredients[i]);
		return map;
	}

	private static ResourceLocation getRecipeLoc(String fileName){
		return getRecipeLoc("crafting", fileName);
	}

	private static ResourceLocation getRecipeLoc(String mainPath, String fileName) {
		return new ResourceLocation(AssemblyLineMachines.MODID, mainPath + "/" + fileName);
	}

	private static TagKey<Item> getNamed(String modid, String path){
		return TagKey.create(Keys.ITEMS, new ResourceLocation(modid, path));
	}

	private static enum RecipeState{
		TRUE, CONDITIONAL, IMC;
	}

	public static class AdvancementlessResult implements FinishedRecipe{

		private final FinishedRecipe recipe;
		private ConfigCondition condition = null;

		public AdvancementlessResult(ResourceLocation rl, Ingredient input, Item result, float experience, int processingTime) {
			this.recipe = new SimpleCookingRecipeBuilder.Result(rl, "", input, result, experience, processingTime, null, null, RecipeSerializer.SMELTING_RECIPE);
		}

		public AdvancementlessResult(ResourceLocation rl, Item result, int numberOfResult, List<String> pattern, Map<Character, Ingredient> key) {
			this.recipe = new ShapedRecipeBuilder.Result(rl, result, numberOfResult, "", pattern, key, null, null);
		}

		public AdvancementlessResult(ResourceLocation rl, Item result, int numberOfResult, List<Ingredient> ingredients) {
			this.recipe = new ShapelessRecipeBuilder.Result(rl, result, numberOfResult, "", ingredients, null, null);
		}

		public AdvancementlessResult configCondition(String fieldName, boolean enableOn) {
			this.condition = new ConfigCondition(fieldName, enableOn);
			return this;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			recipe.serializeRecipeData(json);
			if(condition != null) {
				JsonArray conditions = new JsonArray();
				conditions.add(CraftingHelper.serialize(condition));
				json.add("conditions", conditions);
			}
		}

		@Override
		public ResourceLocation getId() {
			return recipe.getId();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return recipe.getType();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}

	}
}
