package me.haydenb.assemblylinemachines.crafting;

import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PurifierCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<PurifierCrafting> PURIFIER_RECIPE = new RecipeType<PurifierCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:purifier";
		}
	};
	
	public static final PurifierSerializer SERIALIZER = new PurifierSerializer();
	
	private static final Random RAND = new Random();
	
	
	private final Lazy<Ingredient> parta;
	private final Lazy<Ingredient> partb;
	public final Lazy<Ingredient> tobepurified;
	private final ItemStack output;
	private final int time;
	private final ResourceLocation id;
	
	public PurifierCrafting(ResourceLocation id, Lazy<Ingredient> parta, Lazy<Ingredient> partb, Lazy<Ingredient> tobepurified, ItemStack output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.tobepurified = tobepurified;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if((parta.get().test(inv.getItem(1)) && partb.get().test(inv.getItem(2))) || (partb.get().test(inv.getItem(1)) && parta.get().test(inv.getItem(2)))) {
			if(tobepurified.get().test(inv.getItem(3))) {
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
		if((parta.get().test(new ItemStack(Items.SAND)) && partb.get().test(new ItemStack(Items.GRAVEL))) || (parta.get().test(new ItemStack(Items.GRAVEL)) && partb.get().test(new ItemStack(Items.SAND)))) {
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
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(parta.get(), partb.get(), tobepurified.get(), output);
	}
	
	public static class PurifierSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<PurifierCrafting>{

		@Override
		public PurifierCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> ingredienta = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a")));
				Lazy<Ingredient> ingredientb = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b")));
				Lazy<Ingredient> tobepurified = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tobepurified")));
				
				ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				int time = GsonHelper.getAsInt(json, "time");
				
				return new PurifierCrafting(recipeId, ingredienta, ingredientb, tobepurified, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Purifier recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public PurifierCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient inputa = Ingredient.fromNetwork(buffer);
			Ingredient inputb = Ingredient.fromNetwork(buffer);
			Ingredient inputc = Ingredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int time = buffer.readInt();
			
			return new PurifierCrafting(recipeId, Lazy.of(() -> inputa), Lazy.of(() -> inputb), Lazy.of(() -> inputc), output, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PurifierCrafting recipe) {
			recipe.parta.get().toNetwork(buffer);
			recipe.partb.get().toNetwork(buffer);
			recipe.tobepurified.get().toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.time);
			
		}
		
	}
}
