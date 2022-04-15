package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LumberCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<LumberCrafting> LUMBER_RECIPE = new RecipeType<LumberCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:lumber";
		}
	};
	
	public static final LumberSerializer SERIALIZER = new LumberSerializer();
	
	private final Lazy<Ingredient> input;
	private final ItemStack outputa;
	public final ItemStack outputb;
	public final float opbchance;
	public final int time;
	private final ResourceLocation id;
	
	public LumberCrafting(ResourceLocation id, Lazy<Ingredient> input, ItemStack outputa, ItemStack outputb, float opbchance, int time) {
		this.id = id;
		this.input = input;
		this.outputa = outputa;
		this.outputb = outputb;
		this.opbchance = opbchance;
		this.time = time;
	}
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
		return input.get().test(inv.getItem(2));
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if(inv instanceof IMachineDataBridge) {
			inv.getItem(2).shrink(1);
			if(!this.outputb.isEmpty()) {
				if(Utils.RAND.nextFloat() < (opbchance * (1f + (0.5f * (float)((IMachineDataBridge) inv).getUpgradeAmount(Upgrades.MACHINE_EXTRA)))))
					((IMachineDataBridge) inv).setSecondaryOutput(this.outputb.copy());
			}
			((IMachineDataBridge) inv).setCycles(time);
		}
		return this.outputa.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return outputa;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<?> getJEIComponents() {
		return List.of(input.get(), outputa, outputb);
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
		return LUMBER_RECIPE;
	}
	
	public static class LumberSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<LumberCrafting>{

		@Override
		public LumberCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> input = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
				ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				ItemStack outputb = ItemStack.EMPTY;
				float opbchance = 0f;
				if(GsonHelper.isValidNode(json, "secondaryoutput")) {
					outputb = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "secondaryoutput"));
					opbchance = GsonHelper.getAsFloat(json, "opbchance");
				}
				int time = GsonHelper.getAsInt(json, "time");
				
				return new LumberCrafting(recipeId, input, output, outputb, opbchance, time);
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public LumberCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			ItemStack opb = buffer.readItem();
			float opbc = buffer.readFloat();
			int time = buffer.readInt();
			
			return new LumberCrafting(recipeId, Lazy.of(() -> input), output, opb, opbc, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, LumberCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeItem(recipe.outputa);
			buffer.writeItem(recipe.outputb);
			buffer.writeFloat(recipe.opbchance);
			buffer.writeInt(recipe.time);
			
		}
		
	}
}
