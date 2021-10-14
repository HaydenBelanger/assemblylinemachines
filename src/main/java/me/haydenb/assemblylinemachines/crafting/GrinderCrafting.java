package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GrinderCrafting implements Recipe<Container>{

	
	public static final RecipeType<GrinderCrafting> GRINDER_RECIPE = new TypeGrinderCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Ingredient input;
	private final ItemStack output;
	private final int grinds;
	private final Blades tier;
	private final ResourceLocation id;
	private final boolean machineReqd;
	
	public GrinderCrafting(ResourceLocation id, Ingredient input, ItemStack output, int grinds, Blades tier, boolean machineReqd) {
		this.input = input;
		this.output = output;
		this.grinds = grinds;
		this.tier = tier;
		this.id = id;
		this.machineReqd = machineReqd;
	}
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(inv != null) {
			if(inv instanceof Inventory) {
				if(machineReqd == true) {
					return false;
				}
				Inventory pinv = (Inventory) inv;
				if(input.test(pinv.getItem(pinv.selected))) {
					return true;
				}
			}else {
				if(input.test(inv.getItem(1))) {
					return true;
				}
			}
			return false;
		}else {
			return true;
		}
	}

	public boolean getMachineReqd() {
		return machineReqd;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
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
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(input);
		return nnl;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted(){
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(input);
		Item grinderHandler = Registry.getItem("simple_grinder");
		Item electricGrinder = Registry.getItem("electric_grinder");
		if(this.machineReqd) {
			nnl.add(Ingredient.of(grinderHandler, electricGrinder));
		}else {
			nnl.add(Ingredient.of(grinderHandler, electricGrinder, Registry.getItem("hand_grinder")));
		}
		nnl.add(Blades.getAllBladesAtMinTier(getBlade().tier));
		return nnl;
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
		return GRINDER_RECIPE;
	}
	
	public Blades getBlade() {
		return tier;
	}
	
	public int getGrinds() {
		return grinds;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GrinderCrafting>{

		@Override
		public GrinderCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int grinds = GsonHelper.getAsInt(json, "grinds");
				final Blades tier = Blades.valueOf(Blades.class, GsonHelper.getAsString(json, "bladetype"));
				
				if(tier == Blades.NONE) {
					AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: Cannot use 'none' as bladetype.");
					return null;
				}
				final boolean machineReqd;
				if(GsonHelper.isValidNode(json, "machine_required") && GsonHelper.getAsBoolean(json, "machine_required")) {
					machineReqd = true;
				}else {
					machineReqd = false;
				}
				
				return new GrinderCrafting(recipeId, input, output, grinds, tier, machineReqd);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public GrinderCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient input = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int grinds = buffer.readInt();
			final Blades tier = buffer.readEnum(Blades.class);
			final boolean machineReqd = buffer.readBoolean();
			
			return new GrinderCrafting(recipeId, input, output, grinds, tier, machineReqd);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, GrinderCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.grinds);
			buffer.writeEnum(recipe.tier);
			buffer.writeBoolean(recipe.machineReqd);
			
		}
		
	}
	
	public static class TypeGrinderCrafting implements RecipeType<GrinderCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:grinder";
		}
	}
	
	public static enum SharpenerResult{
		PASS, FAIL_NO_RECIPE, FAIL_INCORRECT_BLADE;
	}

	
}
