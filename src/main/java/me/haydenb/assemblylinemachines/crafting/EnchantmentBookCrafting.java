package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.mob.BlockExperienceMill.TEExperienceMill;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EnchantmentBookCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<EnchantmentBookCrafting> ENCHANTMENT_BOOK_RECIPE = new TypeEnchantmentBookCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	private static final Ingredient BOOK = Ingredient.fromItems(Items.BOOK);
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
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEExperienceMill) {
				TEExperienceMill finv = (TEExperienceMill) inv;
				
				if(input.test(finv.getStackInSlot(1))) {
					if(BOOK.test(finv.getStackInSlot(2))) {
						return true;
					}
				}
				
				if(input.test(finv.getStackInSlot(2))) {
					if(BOOK.test(finv.getStackInSlot(1))) {
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
	public boolean isDynamic() {
		return true;
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inv) {
		
		return getRecipeOutput();
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
		
		return Pair.of(EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchantment, mx)), mx);
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		
		return EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(enchantment, 1));
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
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
	public IRecipeType<?> getType() {
		return ENCHANTMENT_BOOK_RECIPE;
	}
	
	public int getCost() {
		return cost;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EnchantmentBookCrafting>{

		@Override
		public EnchantmentBookCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredient = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
				
				if(ingredient.test(BOOK_STACK)) {
					throw new IllegalArgumentException("An Enchantment Book recipe cannot contain Book.");
				}
				int amt = JSONUtils.getInt(json, "amount");
				int cost = JSONUtils.getInt(json, "cost");
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(JSONUtils.getString(json, "enchantment")));
				
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
		public EnchantmentBookCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient input = Ingredient.read(buffer);
			final Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
			final int cost = buffer.readInt();
			final int amount = buffer.readInt();
			
			return new EnchantmentBookCrafting(recipeId, input, enchantment, cost, amount);
		}

		@Override
		public void write(PacketBuffer buffer, EnchantmentBookCrafting recipe) {
			recipe.input.write(buffer);
			buffer.writeResourceLocation(recipe.enchantment.getRegistryName());
			buffer.writeInt(recipe.cost);
			buffer.writeInt(recipe.amount);
		}
		
	}
	
	public static class TypeEnchantmentBookCrafting implements IRecipeType<EnchantmentBookCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:enchantment_book";
		}
	}

	
}
