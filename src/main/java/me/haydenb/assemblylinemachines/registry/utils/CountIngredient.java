package me.haydenb.assemblylinemachines.registry.utils;

import java.util.*;

import org.apache.commons.lang3.Validate;

import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries.Keys;

/**
 * The CountIngredient is a special kind of ingredient which supports addition of a count field.
 * This also includes lazy-wrapping to support new 1.18.2 delayed tag setup, as well as empty-tag validation from JSON for
 * inter-mod compat recipes.
 * @author Hayden Belanger
 *
 */
public class CountIngredient {
	
	private final int count;
	private final Lazy<Ingredient> ingredient;
	
	private CountIngredient(Lazy<Ingredient> ingredient, int count) {
		Validate.isTrue(count > 0, "count must be greater than 0 for CountIngredients.");
		this.count = count;
		this.ingredient = ingredient;
	}
	
	public boolean test(ItemStack stack) {
		return test(stack, false);
	}
	
	public boolean test(ItemStack stack, boolean ignoreCount) {
		if(!ingredient.get().test(stack)) return false;
		return ignoreCount || stack.getCount() >= count;
	}
	
	public int getCount() {
		return count;
	}
	
	public boolean isEmpty() {
		return ingredient.get().isEmpty();
	}
	
	public List<ItemStack> getCountModifiedItemStacks(){
		return Arrays.asList(ingredient.get().getItems()).stream().map((is) -> {
			ItemStack cs = is.copy();
			cs.setCount(count);
			return cs;
		}).toList();
	}
	
	public static CountIngredient fromNetwork(FriendlyByteBuf buffer) {
		Ingredient ingredient = Ingredient.fromNetwork(buffer);
		int count = buffer.readInt();
		return new CountIngredient(Lazy.of(() -> ingredient), count);
	}
	
	public void toNetwork(FriendlyByteBuf buffer) {
		ingredient.get().toNetwork(buffer);
		buffer.writeInt(count);
	}
	
	public static CountIngredient fromJson(JsonObject json) {
		Lazy<Ingredient> ingredient = getValidatedIngredient(json);
		int count = GsonHelper.isValidNode(json, "count") ? GsonHelper.getAsInt(json, "count") : 1;
		return new CountIngredient(ingredient, count);
	}
	
	public JsonObject toJson() {
		JsonObject ingJson = ingredient.get().toJson().getAsJsonObject();
		if(count > 1) {
			ingJson.addProperty("count", count);
		}
		return ingJson;
	}
	
	public static CountIngredient of(Ingredient ingredient) {
		return of(ingredient, 1);
	}
	
	public static CountIngredient of(Ingredient ingredient, int count) {
		return new CountIngredient(Lazy.of(() -> ingredient), count);
	}
	
	@SuppressWarnings("deprecation")
	public static Lazy<Ingredient> getValidatedIngredient(JsonObject json){
		return Lazy.of(() -> {
				if(GsonHelper.isValidNode(json, "tag")) {
					TagKey<Item> tag = TagKey.create(Keys.ITEMS, new ResourceLocation(GsonHelper.getAsString(json, "tag")));
					List<Holder<Item>> values = new ArrayList<>();
					Registry.ITEM.getTagOrEmpty(tag).forEach(values::add);
					if(values.isEmpty()) return Ingredient.EMPTY;
				}
				return Ingredient.fromJson(json);
		});
	}
}