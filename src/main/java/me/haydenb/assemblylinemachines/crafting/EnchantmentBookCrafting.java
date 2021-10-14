package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockExperienceMill.TEExperienceMill;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EnchantmentBookCrafting implements Recipe<Container>{

	
	public static final RecipeType<EnchantmentBookCrafting> ENCHANTMENT_BOOK_RECIPE = new TypeEnchantmentBookCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	private static final Ingredient BOOK = Ingredient.of(Items.BOOK);
	private static final ItemStack BOOK_STACK = new ItemStack(Items.BOOK);
	
	private final Ingredient input;
	private final Enchantment enchantment;
	private final int cost;
	private final int amount;
	private final ResourceLocation id;
	
	public EnchantmentBookCrafting(ResourceLocation id, Ingredient input, Enchantment enchantment, int cost, int amount) {
		this.input = input;
		this.enchantment = enchantment;
		this.id = id;
		this.cost = cost;
		this.amount = amount;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(inv != null) {
			if(inv instanceof TEExperienceMill) {
				
				if(input.test(inv.getItem(1))) {
					if(BOOK.test(inv.getItem(2))) {
						return true;
					}
				}
				
				if(input.test(inv.getItem(2))) {
					if(BOOK.test(inv.getItem(1))) {
						return true;
					}
				}
			}
			return false;
		}else {
			return true;
		}
		
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		return getResultItem();
	}
	
	public Pair<ItemStack, Integer> getLeveledBookCraftingResult(TEExperienceMill te){
		int mx = 1;
		switch(te.getUpgradeAmount(Upgrades.EXP_MILL_LEVEL)) {
		case 3:
			mx = enchantment.getMaxLevel();
			break;
		case 2:
			mx = (int) Math.ceil((double) enchantment.getMaxLevel() / 2d);
			break;
		case 1:
			mx = (int) Math.ceil((double) enchantment.getMaxLevel() / 3d);
			break;
		}
		
		return Pair.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, mx)), mx);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		
		return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, 1));
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
	
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(BOOK);
		nnl.add(input);
		return nnl;
	}

	@Override
	public RecipeType<?> getType() {
		return ENCHANTMENT_BOOK_RECIPE;
	}
	
	public int getCost() {
		return cost;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EnchantmentBookCrafting>{

		@Override
		public EnchantmentBookCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				
				if(ingredient.test(BOOK_STACK)) {
					throw new IllegalArgumentException("An Enchantment Book recipe cannot contain Book.");
				}
				int amt = GsonHelper.getAsInt(json, "amount");
				int cost = GsonHelper.getAsInt(json, "cost");
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "enchantment")));
				
				if(enchantment == null) {
					throw new IllegalArgumentException("This enchantment does not exist.");
				}
				
				return new EnchantmentBookCrafting(recipeId, ingredient, enchantment, cost, amt);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Enchantment Book Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public EnchantmentBookCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient input = Ingredient.fromNetwork(buffer);
			final Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
			final int cost = buffer.readInt();
			final int amount = buffer.readInt();
			
			return new EnchantmentBookCrafting(recipeId, input, enchantment, cost, amount);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, EnchantmentBookCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeResourceLocation(recipe.enchantment.getRegistryName());
			buffer.writeInt(recipe.cost);
			buffer.writeInt(recipe.amount);
		}
		
	}
	
	public static class TypeEnchantmentBookCrafting implements RecipeType<EnchantmentBookCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:enchantment_book";
		}
	}

	
}
