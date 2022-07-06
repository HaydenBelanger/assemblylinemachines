package me.haydenb.assemblylinemachines.crafting;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.*;
import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.block.machines.BlockExperienceMill.TEExperienceMill;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.utils.CountIngredient;
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

public class EnchantmentBookCrafting implements Recipe<Container>, IRecipeCategoryBuilder{


	public static final RecipeType<EnchantmentBookCrafting> ENCHANTMENT_BOOK_RECIPE = new RecipeType<>() {
		@Override
		public String toString() {
			return "assemblylinemachines:enchantment_book";
		}
	};

	public static final EnchantmentBookSerializer SERIALIZER = new EnchantmentBookSerializer();
	private static final Ingredient BOOK = Ingredient.of(Items.BOOK);

	private final CountIngredient input;
	private final Enchantment enchantment;
	private final int cost;
	private final ResourceLocation id;
	private final LoadingCache<Integer, ItemStack> bookCache;

	public EnchantmentBookCrafting(ResourceLocation id, CountIngredient input, Enchantment enchantment, int cost) {
		this.input = input;
		this.enchantment = enchantment;
		this.id = id;
		this.cost = cost;
		this.bookCache = CacheBuilder.newBuilder().build(CacheLoader.from((key) -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, key))));
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
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
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack assemble(Container inv) {
		int level = 1;
		if(inv instanceof TEExperienceMill te) {
			level = switch(te.getUpgradeAmount(Upgrades.EXP_MILL_LEVEL)) {
			case 3 -> enchantment.getMaxLevel();
			case 2 -> (int) Math.ceil(enchantment.getMaxLevel() / 2d);
			case 1 -> (int) Math.ceil(enchantment.getMaxLevel() / 3d);
			default -> 1;
			};

			float spUp = te.getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
			int cost = Math.round(this.cost * (1f + (0.1f * spUp))) * level;
			int cycles = spUp != 0f ? Math.round((this.cost * (0.75f / spUp)) / 10f) * level : Math.round(this.cost / 10f) * level;

			if(te.tank.getAmount() < cost) return ItemStack.EMPTY;

			int bookSlot = inv.getItem(1).getItem() == Items.BOOK ? 1 : 2;
			int catalystSlot = bookSlot == 2 ? 1 : 2;
			if(inv.getItem(catalystSlot).getCount() < this.input.getCount() * level) return ItemStack.EMPTY;

			te.tank.shrink(cost);
			inv.getItem(bookSlot).shrink(1);
			inv.getItem(catalystSlot).shrink(this.input.getCount() * level);
			te.cycles = cycles;
		}
		try {
			return bookCache.get(level).copy();
		}catch(ExecutionException e) {
			e.printStackTrace();
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		try {
			return bookCache.get(1);
		}catch(ExecutionException e) {
			e.printStackTrace();
			return ItemStack.EMPTY;
		}
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
	public List<?> getJEIComponents() {
		return List.of(input, BOOK, getResultItem());
	}

	@Override
	public RecipeType<?> getType() {
		return ENCHANTMENT_BOOK_RECIPE;
	}

	public static class EnchantmentBookSerializer implements RecipeSerializer<EnchantmentBookCrafting>{

		@Override
		public EnchantmentBookCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				CountIngredient ingredient = CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));

				int cost = GsonHelper.getAsInt(json, "cost");
				Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "enchantment")));

				if(enchantment == null) {
					throw new IllegalArgumentException("This enchantment does not exist.");
				}

				return new EnchantmentBookCrafting(recipeId, ingredient, enchantment, cost);
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}


		}

		@Override
		public EnchantmentBookCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CountIngredient input = CountIngredient.fromNetwork(buffer);
			Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(buffer.readResourceLocation());
			int cost = buffer.readInt();

			return new EnchantmentBookCrafting(recipeId, input, enchantment, cost);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, EnchantmentBookCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(recipe.enchantment));
			buffer.writeInt(recipe.cost);
		}

	}
}
