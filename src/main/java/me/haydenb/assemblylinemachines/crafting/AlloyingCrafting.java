package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.CountIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class AlloyingCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<AlloyingCrafting> ALLOYING_RECIPE = new RecipeType<AlloyingCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:alloying";
		}
	};
	
	public static final AlloyingSerializer SERIALIZER = new AlloyingSerializer();
	
	
	private final Lazy<CountIngredient> parta;
	private final Lazy<CountIngredient> partb;
	private final Lazy<ItemStack> output;
	private final int time;
	private final ResourceLocation id;
	
	public AlloyingCrafting(ResourceLocation id, Lazy<CountIngredient> parta, Lazy<CountIngredient> partb, Lazy<ItemStack> output, int time) {
		this.parta = parta;
		this.partb = partb;
		this.output = output;
		this.time = time;
		
		this.id = id;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(parta.get().isEmpty() || partb.get().isEmpty() || output.get().isEmpty()) return false;
		return ((parta.get().test(inv.getItem(1)) && partb.get().test(inv.getItem(2))) || (partb.get().test(inv.getItem(1)) && parta.get().test(inv.getItem(2))));
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		if(inv instanceof IMachineDataBridge) {
			int shra = 0;
			int shrb = 0;
			if(parta.get().test(inv.getItem(1)) && partb.get().test(inv.getItem(2))) {
				shra = parta.get().getCount();
				shrb = partb.get().getCount();
			}else if(partb.get().test(inv.getItem(1)) && parta.get().test(inv.getItem(2))) {
				shrb = parta.get().getCount();
				shra = partb.get().getCount();
			}else {
				return ItemStack.EMPTY;
			}
			
			inv.getItem(1).shrink(shra);
			inv.getItem(2).shrink(shrb);
			((IMachineDataBridge) inv).setCycles(time / 10f);
		}
		return this.output.get().copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return output.get();
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
		return ALLOYING_RECIPE;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean showInJEI() {
		return !this.parta.get().isEmpty() && !this.partb.get().isEmpty() && !this.output.get().isEmpty();
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(parta.get(), partb.get(), output.get());
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public static class AlloyingSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<AlloyingCrafting>{

		@Override
		public AlloyingCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<CountIngredient> ingredienta = Lazy.of(() -> CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "part_a")));
				Lazy<CountIngredient> ingredientb = Lazy.of(() -> CountIngredient.fromJson(GsonHelper.getAsJsonObject(json, "part_b")));
				Lazy<ItemStack> output = Utils.getTaggedOutputFromJson(GsonHelper.getAsJsonObject(json, "output")).orElseThrow();
				
				int time = GsonHelper.getAsInt(json, "time");
				
				return new AlloyingCrafting(recipeId, ingredienta, ingredientb, output, time);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Alloying recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public AlloyingCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CountIngredient inputa = CountIngredient.fromNetwork(buffer);
			CountIngredient inputb = CountIngredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int time = buffer.readInt();
			
			return new AlloyingCrafting(recipeId, Lazy.of(() -> inputa), Lazy.of(() -> inputb), Lazy.of(() -> output), time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlloyingCrafting recipe) {
			recipe.parta.get().toNetwork(buffer);
			recipe.partb.get().toNetwork(buffer);
			buffer.writeItem(recipe.output.get());
			buffer.writeInt(recipe.time);
			
		}
		
	}
}
