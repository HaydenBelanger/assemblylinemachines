package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.crafting.MetalCrafting;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoRecipeGenerator extends RecipeProvider {
	
	private final PrintWriter writer;
	private final Collection<Path> inputFolders;
	
	public AutoRecipeGenerator(GatherDataEvent event, PrintWriter openPw) {
		super(event.getGenerator());
		
		System.out.println(event.getGenerator().getOutputFolder().toAbsolutePath().toString());
		this.inputFolders = event.getGenerator().getInputFolders();
		this.writer = openPw;
		event.getGenerator().addProvider(this);
	}
	
	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
		
		
		
		if(inputFolders.size() == 0) {
			writer.println("[WARNING]: Datagen was not provided with any input folders, so no Blasting to Furnace auto-recipes will be generated.");
		}else {
			writer.println("[SYSTEM]: Starting Blasting to Furnace recipe auto-conversion with " + inputFolders.size() + " input folder(s)...");
			for(Path p : inputFolders) {
				File f = new File(p.toString() + "/data/" + AssemblyLineMachines.MODID.toLowerCase() + "/recipes/blasting/");
				writer.println("[SYSTEM]: Using input directory \"" + f.getPath() + "\"...");
				this.concursivelyCopyRecipe(consumer, f.getPath());
			}
		}
		
		writer.println("[SYSTEM]: Starting auto-generation of metal-related recipes...");
		
		for(MetalRecipeGeneration mrg : MetalRecipeGeneration.values()) {
			writer.println(mrg.generateRecipes(consumer));
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
						writer.println("[FURNACE RECIPES]: Copied blasting recipe " + rlPath + ".");
					}catch(Exception e) {
						
						writer.println("[WARNING]: Generating recipe for " + file.getName() + " failed. View console for more info.");
						e.printStackTrace();
					}
					
				}else if(file.isDirectory()) {
					concursivelyCopyRecipe(consumer, file.getAbsolutePath());
				}
			}
		}
	}
	
	private static enum MetalRecipeGeneration {
		
		COPPER(false, false, false, false, Triple.of(true, true, 3), Triple.of(true, false, 5)), IRON(false, false, false, false, Triple.of(true, true, 3), Triple.of(true, false, 4)), GOLD(false, false, false, false, Triple.of(true, true, 3), Triple.of(true, false, 1)),
		TITANIUM(true, true, true, true, Triple.of(true, true, 5), Triple.of(true, false, 2)), STEEL(true, true, true, true, Triple.of(true, true, 8), Triple.of(true, false, 3)), CHROMIUM(true, true, false, false, Triple.of(true, false, 3), Triple.of(false, false, 0)),
		ATTUNED_TITANIUM(true, false, false, false, Triple.of(true, false, 1), Triple.of(false, false, 0)), PLASTIC(3), RUBBER(4),
		MYSTIUM(true, false, true, false, Triple.of(true, false, 2), Triple.of(false, false, 0), getItemTagIngredient("forge", "rods/steel")), FLEROVIUM(true, true, false, false, Triple.of(true, false, 4), Triple.of(true, true, 1)), 
		ENERGIZED_GOLD(true, false, false, false, Triple.of(true, false, 4), Triple.of(false, false, 0)), RAW_CHROMIUM(() -> Ingredient.of(Registry.getItem("raw_chromium")), null, false),
		NOVASTEEL(true, false, true, false, Triple.of(true, false, 1), Triple.of(false, false, 0), getItemTagIngredient("forge", "rods/graphene")), 
		CRANK(false, false, true, false, Triple.of(false, false, 0), Triple.of(false, false, 0), getItemTagIngredient(AssemblyLineMachines.MODID, "crafting/gears/precious"), getItemTagIngredient("forge", "rods/steel")),
		CRAFTING_TABLE(() -> Ingredient.of(Blocks.CRAFTING_TABLE), () -> Ingredient.of(Registry.getBlock("compressed_crafting_table")), false), RAW_TITANIUM(() -> Ingredient.of(Registry.getItem("raw_titanium")), null, false),
		RAW_FLEROVIUM(() -> Ingredient.of(Registry.getItem("raw_flerovium")), null, false);
		
		private static final HashMap<String, List<String>> ARMOR_PATTERNS = new HashMap<>();
		private static final HashMap<String, List<String>> TOOL_PATTERNS = new HashMap<>();
		
		private static final List<String> BLOCK_PATTERN = List.of("XXX", "XXX", "XXX");
		private static final List<String> GEAR_PATTERN = List.of(" X ", "XYX", " X ");
		
		static {
			ARMOR_PATTERNS.put("helmet", List.of("XXX", "X X"));
			ARMOR_PATTERNS.put("chestplate", List.of("X X", "XXX", "XXX"));
			ARMOR_PATTERNS.put("leggings", List.of("XXX", "X X", "X X"));
			ARMOR_PATTERNS.put("boots", List.of("X X", "X X"));
			
			TOOL_PATTERNS.put("pickaxe", List.of("XXX", " Y ", " Y "));
			TOOL_PATTERNS.put("axe", List.of("XX", "XY", " Y"));
			TOOL_PATTERNS.put("sword", List.of("X", "X", "Y"));
			TOOL_PATTERNS.put("shovel", List.of("X", "Y", "Y"));
			TOOL_PATTERNS.put("hoe", List.of("XX", " Y", " Y"));
			TOOL_PATTERNS.put("hammer", List.of(" X ", " YX", "Y  "));
			
		}
		
		private final boolean hasStorageBlock;
		private final boolean hasNugget;
		private final boolean hasTools;
		private final boolean hasArmor;
		private final Triple<Boolean, Boolean, Integer> hasPlate;
		private final Triple<Boolean, Boolean, Integer> hasGear;
		private final Supplier<Ingredient> toolMaterial;
		private final Supplier<Ingredient> toolRod;
		private final Supplier<Ingredient> specialStorageBlock;
		private final boolean addIngot;
		private final boolean isSheetInsteadOfPlate;
		
		MetalRecipeGeneration(boolean hasStorageBlock, boolean hasNugget, boolean hasTools, boolean hasArmor, Triple<Boolean, Boolean, Integer> hasPlate, Triple<Boolean, Boolean, Integer> hasGear){
			
			this(hasStorageBlock, hasNugget, hasTools, hasArmor, hasPlate, hasGear, null, null);
		}
		
		MetalRecipeGeneration(boolean hasStorageBlock, boolean hasNugget, boolean hasTools, boolean hasArmor, Triple<Boolean, Boolean, Integer> hasPlate, Triple<Boolean, Boolean, Integer> hasGear, Supplier<Ingredient> toolRod){
			this(hasStorageBlock, hasNugget, hasTools, hasArmor, hasPlate, hasGear, null, toolRod);
		}
		
		MetalRecipeGeneration(boolean hasStorageBlock, boolean hasNugget, boolean hasTools, boolean hasArmor, Triple<Boolean, Boolean, Integer> hasPlate, Triple<Boolean, Boolean, Integer> hasGear, Supplier<Ingredient> toolMaterial, Supplier<Ingredient> toolRod){
			this.hasStorageBlock = hasStorageBlock;
			this.hasNugget = hasNugget;
			this.hasTools = hasTools;
			this.hasArmor = hasArmor;
			this.hasPlate = hasPlate;
			this.hasGear = hasGear;
			this.toolMaterial = toolMaterial != null ? toolMaterial : getItemTagIngredient("forge", "ingots/" + this.toString().toLowerCase());
			this.toolRod = toolRod != null ? toolRod : getItemTagIngredient("forge", "rods/wooden");
			this.specialStorageBlock = null;
			this.addIngot = true;
			this.isSheetInsteadOfPlate = false;
		}
		
		MetalRecipeGeneration(Supplier<Ingredient> toolMaterial, Supplier<Ingredient> specialStorageBlock, boolean addIngot){
			this.hasStorageBlock = true;
			this.hasNugget = this.hasTools = this.hasArmor = this.isSheetInsteadOfPlate = false;
			this.hasPlate = this.hasGear = Triple.of(false, false, 0);
			this.toolMaterial = this.toolRod = toolMaterial;
			this.specialStorageBlock = specialStorageBlock;
			this.addIngot = addIngot;
		}
		
		MetalRecipeGeneration(int plateCount){
			this.hasStorageBlock = this.hasNugget = this.hasTools = this.hasArmor = false;
			this.hasPlate = Triple.of(true, false, plateCount);
			this.hasGear = Triple.of(false, false, 0);
			this.toolMaterial = this.toolRod = () -> Ingredient.of(Registry.getItem(this.toString().toLowerCase() + "_ball"));
			this.isSheetInsteadOfPlate = this.addIngot = true;
			this.specialStorageBlock = null;
		}
		
		public String generateRecipes(Consumer<FinishedRecipe> consumer) {
			String typeName = this.toString().toLowerCase();
			String logText = "";
			
			if(this.hasArmor) {
				for(String piece : ARMOR_PATTERNS.keySet()) {
					String pieceResultName = typeName + "_" + piece;
					consumer.accept(new AdvancementlessResult(getRecipeLoc("tools/" + typeName + "/" + pieceResultName), Registry.getItem(pieceResultName), 1, ARMOR_PATTERNS.get(piece), getShapedKey(this.toolMaterial)));
				}
				logText = logText + "Armor, ";
			}
			if(this.hasTools) {
				for(String piece : TOOL_PATTERNS.keySet()) {
					String pieceResultName = typeName + "_" + piece;
					consumer.accept(new AdvancementlessResult(getRecipeLoc("tools/" + typeName + "/" + pieceResultName), Registry.getItem(pieceResultName), 1, TOOL_PATTERNS.get(piece), getShapedKey(this.toolMaterial, this.toolRod)));
				}
				logText = logText + "Tools, ";
			}
			if(this.hasStorageBlock) {
				Item resultBlock = specialStorageBlock == null ? Registry.getItem(typeName + "_block") : specialStorageBlock.get().getItems()[0].getItem();
				Item resultIngot = specialStorageBlock == null ? addIngot ? Registry.getItem(typeName + "_ingot") : Registry.getItem(typeName) : toolMaterial.get().getItems()[0].getItem();
				Ingredient inputBlock = specialStorageBlock == null ? getItemTagIngredient("forge", "storage_blocks/" + typeName).get() : specialStorageBlock.get();
				consumer.accept(new AdvancementlessResult(getRecipeLoc("conversion/ingot_to_block/" + typeName), resultBlock, 1, BLOCK_PATTERN, getShapedKey(this.toolMaterial)));
				consumer.accept(new AdvancementlessResult(getRecipeLoc("conversion/block_to_ingot/" + typeName), resultIngot, 9, List.of(inputBlock)));
				logText = logText + "Storage Block, ";
			}
			if(this.hasNugget) {
				consumer.accept(new AdvancementlessResult(getRecipeLoc("conversion/nugget_to_ingot/" + typeName), Registry.getItem(typeName + "_ingot"), 1, BLOCK_PATTERN, getShapedKey(getItemTagIngredient("forge", "nuggets/" + typeName))));
				consumer.accept(new AdvancementlessResult(getRecipeLoc("conversion/ingot_to_nugget/" + typeName), Registry.getItem(typeName + "_nugget"), 9, List.of(this.toolMaterial.get())));
				logText = logText + "Nugget, ";
			}
			if(this.hasPlate.getLeft()) {
				Ingredient metalInput = this.hasPlate.getMiddle() ? getItemTagIngredient("forge", "ingots/pure_" + typeName).get() : toolMaterial.get();
				String type = this.isSheetInsteadOfPlate ? "_sheet" : "_plate";
				consumer.accept(new AdvancementlessResult(getRecipeLoc("plates/" + typeName + type), Registry.getItem(typeName + type), this.hasPlate.getRight(), List.of(metalInput, getItemTagIngredient(AssemblyLineMachines.MODID, "crafting/hammers").get())));
				consumer.accept(new MetalShaperResult(getRecipeLoc("metalshaper", typeName + type), metalInput, new ItemStack(Registry.getItem(typeName + type), this.hasPlate.getRight()), 6));
				logText = logText + "Plate, ";
			}
			if(this.hasGear.getLeft()) {
				Supplier<Ingredient> stickType = this.hasGear.getMiddle() ? getItemTagIngredient("forge", "rods/steel") : getItemTagIngredient("forge", "rods/wooden");
				consumer.accept(new AdvancementlessResult(getRecipeLoc("gears/" + typeName + "_gear"), Registry.getItem(typeName + "_gear"), this.hasGear.getRight(), GEAR_PATTERN, getShapedKey(getItemTagIngredient("forge", "plates/" + typeName), stickType)));
			}
			
			if(!logText.isEmpty()) {
				return "[METAL RECIPES]: Generated " + typeName + " recipes: " + logText.substring(0, logText.length() - 2) + ".";
			}
			return "[WARNING]: No recipes generated for " + typeName + ".";
		}
		
		private ResourceLocation getRecipeLoc(String fileName){
			return this.getRecipeLoc("crafting", fileName);
		}
		
		private ResourceLocation getRecipeLoc(String mainPath, String fileName) {
			return new ResourceLocation(AssemblyLineMachines.MODID, mainPath + "/" + fileName);
		}
		
		@SafeVarargs
		private HashMap<Character, Ingredient> getShapedKey(Supplier<Ingredient>... ingredients){
			HashMap<Character, Ingredient> map = new HashMap<>();
			
			if(ingredients.length > 0) {
				map.put('X', ingredients[0].get());
			}
			if(ingredients.length > 1) {
				map.put('Y', ingredients[1].get());
			}
			return map;
		}
		
		private static Supplier<Ingredient> getItemTagIngredient(String modid, String path){
			return () -> Ingredient.of(ForgeTagHandler.makeWrapperTag(ForgeRegistries.ITEMS, new ResourceLocation(modid, path)));
		}
	}
	
	public static class AdvancementlessResult implements FinishedRecipe{

		private final FinishedRecipe recipe;
		
		public AdvancementlessResult(ResourceLocation rl, Ingredient input, Item result, float experience, int processingTime) {
			this.recipe = new SimpleCookingRecipeBuilder.Result(rl, "", input, result, experience, processingTime, null, null, RecipeSerializer.SMELTING_RECIPE);
		}
		
		public AdvancementlessResult(ResourceLocation rl, Item result, int numberOfResult, List<String> pattern, Map<Character, Ingredient> key) {
			this.recipe = new ShapedRecipeBuilder.Result(rl, result, numberOfResult, "", pattern, key, null, null);
		}
		
		public AdvancementlessResult(ResourceLocation rl, Item result, int numberOfResult, List<Ingredient> ingredients) {
			this.recipe = new ShapelessRecipeBuilder.Result(rl, result, numberOfResult, "", ingredients, null, null);
		}
		
		@Override
		public void serializeRecipeData(JsonObject json) {
			recipe.serializeRecipeData(json);
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
	
	public static class MetalShaperResult implements FinishedRecipe{

		private final ResourceLocation rl;
		private final Ingredient input;
		private final ItemStack result;
		private final int time;
		
		public MetalShaperResult(ResourceLocation rl, Ingredient input, ItemStack result, int time) {
			this.rl = rl;
			this.input = input;
			this.result = result;
			this.time = time;
		}
		
		@Override
		public void serializeRecipeData(JsonObject json) {
			
			json.add("input", input.toJson());
			
			JsonObject outputJson = new JsonObject();
			outputJson.addProperty("item", result.getItem().getRegistryName().toString());
			if(result.getCount() > 1) {
				outputJson.addProperty("count", result.getCount());
			}
			json.add("output", outputJson);
			json.addProperty("time", time);
		}

		@Override
		public ResourceLocation getId() {
			return rl;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return MetalCrafting.SERIALIZER;
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
