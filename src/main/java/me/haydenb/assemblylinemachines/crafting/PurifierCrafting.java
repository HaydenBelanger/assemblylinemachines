package me.haydenb.assemblylinemachines.crafting;

import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PurifierCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<PurifierCrafting> PURIFIER_RECIPE = new TypePurifierCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private static final Random RAND = new Random();
	
	
	private final Ingredient parta;
	private final Ingredient partb;
	private final Ingredient tobepurified;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public PurifierCrafting(ResourceLocation id, Ingredient parta, Ingredient partb, Ingredient tobepurified, ItemStack output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.tobepurified = tobepurified;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if((parta.test(inv.getItem(1)) && partb.test(inv.getItem(2))) || (partb.test(inv.getItem(1)) && parta.test(inv.getItem(2)))) {
			if(tobepurified.test(inv.getItem(3))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if(inv instanceof IMachineDataBridge) {
			IMachineDataBridge data = (IMachineDataBridge) inv;
			boolean requiresUpgrade = this.requiresUpgrade();
			if(requiresUpgrade && data.getUpgradeAmount(Upgrades.PURIFIER_EXPANDED) == 0) return ItemStack.EMPTY;
			int conservationCount = requiresUpgrade ? 0 : data.getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
			if(RAND.nextInt(10) * conservationCount < 10) inv.getItem(1).shrink(1);
			if(RAND.nextInt(10) * conservationCount < 10) inv.getItem(2).shrink(1);
			data.setCycles(requiresUpgrade ? time / 8f : time / 10f);
			
			inv.getItem(3).shrink(1);
		}
		return this.output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output;
	}

	
	public boolean requiresUpgrade() {
		if((parta.test(new ItemStack(Items.SAND)) && partb.test(new ItemStack(Items.GRAVEL))) || (parta.test(new ItemStack(Items.GRAVEL)) && partb.test(new ItemStack(Items.SAND)))) {
			return false;
		}
		
		return true;
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
	public RecipeType<?> getType() {
		return PURIFIER_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(parta);
		nnl.add(partb);
		nnl.add(tobepurified);
		
		return nnl;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		return List.of(parta, partb, tobepurified);
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return List.of(output);
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PurifierCrafting>{

		@Override
		public PurifierCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a"));
				final Ingredient ingredientb = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b"));
				final Ingredient tobepurified = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tobepurified"));
				
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int time = GsonHelper.getAsInt(json, "time");
				
				return new PurifierCrafting(recipeId, ingredienta, ingredientb, tobepurified, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Purifier recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public PurifierCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient inputa = Ingredient.fromNetwork(buffer);
			final Ingredient inputb = Ingredient.fromNetwork(buffer);
			final Ingredient inputc = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int time = buffer.readInt();
			
			return new PurifierCrafting(recipeId, inputa, inputb, inputc, output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PurifierCrafting recipe) {
			recipe.parta.toNetwork(buffer);
			recipe.partb.toNetwork(buffer);
			recipe.tobepurified.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
	
	public static class TypePurifierCrafting implements RecipeType<PurifierCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:purifier";
		}
	}

	
}
