package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockHandGrinder.Blades;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GrinderCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<GrinderCrafting> GRINDER_RECIPE = new TypeGrinderCrafting();
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
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof PlayerInventory) {
				if(machineReqd == true) {
					return false;
				}
				PlayerInventory pinv = (PlayerInventory) inv;
				if(input.test(pinv.getStackInSlot(pinv.currentItem))) {
					return true;
				}
			}else {
				if(input.test(inv.getStackInSlot(1))) {
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
	public ItemStack getCraftingResult(IInventory inv) {
		return this.output.copy();
	}

	@Override
	public boolean canFit(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return output;
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
			nnl.add(Ingredient.fromItems(grinderHandler, electricGrinder));
		}else {
			nnl.add(Ingredient.fromItems(grinderHandler, electricGrinder, Registry.getItem("hand_grinder")));
		}
		nnl.add(Blades.getAllBladesAtMinTier(getBlade().tier));
		return nnl;
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
	public IRecipeType<?> getType() {
		return GRINDER_RECIPE;
	}
	
	public Blades getBlade() {
		return tier;
	}
	
	public int getGrinds() {
		return grinds;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<GrinderCrafting>{

		@Override
		public GrinderCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				final int grinds = JSONUtils.getInt(json, "grinds");
				final Blades tier = Blades.valueOf(JSONUtils.getString(json, "bladetype"));
				
				if(tier == Blades.none) {
					AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: Cannot use 'none' as bladetype.");
					return null;
				}
				final boolean machineReqd;
				if(JSONUtils.hasField(json, "machine_required")) {
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
		public GrinderCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient input = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int grinds = buffer.readInt();
			final Blades tier = buffer.readEnumValue(Blades.class);
			final boolean machineReqd = buffer.readBoolean();
			
			return new GrinderCrafting(recipeId, input, output, grinds, tier, machineReqd);
		}

		@Override
		public void write(PacketBuffer buffer, GrinderCrafting recipe) {
			recipe.input.write(buffer);
			buffer.writeItemStack(recipe.output);
			buffer.writeInt(recipe.grinds);
			buffer.writeEnumValue(recipe.tier);
			buffer.writeBoolean(recipe.machineReqd);
			
		}
		
	}
	
	public static class TypeGrinderCrafting implements IRecipeType<GrinderCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:grinder";
		}
	}
	
	public static enum SharpenerResult{
		PASS, FAIL_NO_RECIPE, FAIL_INCORRECT_BLADE;
	}

	
}
