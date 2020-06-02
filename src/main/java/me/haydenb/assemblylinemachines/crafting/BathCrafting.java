package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.FluidProperty.Fluids;
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

public class BathCrafting implements IRecipe<IInventory>{

	
	public static final IRecipeType<BathCrafting> BATH_RECIPE = new TypeBathCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	
	private final Ingredient inputa;
	private final Ingredient inputb;
	private final ItemStack output;
	private final Fluids fluid;
	private final int stirs;
	private final ResourceLocation id;
	private final int color;
	private final boolean machineReqd;
	
	public BathCrafting(ResourceLocation id, Ingredient inputa, Ingredient inputb, ItemStack output, int stirs, Fluids fluid, int color, boolean machineReqd) {
		this.inputa = inputa;
		this.inputb = inputb;
		this.output = output;
		this.stirs = stirs;
		this.fluid = fluid;
		this.id = id;
		this.color = color;
		this.machineReqd = machineReqd;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEFluidBath) {
				if(machineReqd == true) {
					return false;
				}
				TEFluidBath finv = (TEFluidBath) inv;
				
				if(inputa.test(finv.getStackInSlot(1))) {
					if(inputb.test(finv.getStackInSlot(2))) {
						return true;
					}
				}
				
				if(inputa.test(finv.getStackInSlot(2))) {
					if(inputb.test(finv.getStackInSlot(1))) {
						return true;
					}
				}
			}else{
				if(inputa.test(inv.getStackInSlot(0))) {
					if(inputb.test(inv.getStackInSlot(1))) {
						return true;
					}
				}
				
				if(inputa.test(inv.getStackInSlot(1))) {
					if(inputb.test(inv.getStackInSlot(0))) {
						return true;
					}
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
		nnl.add(inputa);
		nnl.add(inputb);
		return nnl;
	}
	
	public NonNullList<Ingredient> getIngredientsJEIFormatted(){
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(inputa);
		nnl.add(inputb);
		Item mixerHandler = Registry.getItem("simple_fluid_mixer");
		if(this.machineReqd) {
			nnl.add(Ingredient.fromItems(mixerHandler));
		}else {
			nnl.add(Ingredient.fromItems(mixerHandler, Registry.getItem("fluid_bath")));
		}
		return nnl;
	}

	@Override
	public IRecipeType<?> getType() {
		return BATH_RECIPE;
	}
	
	public Fluids getFluid() {
		return fluid;
	}
	
	public int getStirs() {
		return stirs;
	}
	
	public int getColor() {
		return color;
	}
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<BathCrafting>{

		@Override
		public BathCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input_a"));
				final Ingredient ingredientb = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input_b"));
				
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				final int stirs = JSONUtils.getInt(json, "stirs");
				final Fluids fluid = Fluids.valueOf(JSONUtils.getString(json, "fluid").toUpperCase());
				if(fluid == Fluids.NONE) {
					AssemblyLineMachines.LOGGER.error("Error deserializing Bath Crafting Recipe from JSON: Cannot use 'none' as fluid.");
					return null;
				}
				
				final int color = Integer.parseInt(JSONUtils.getString(json, "mix_color").replace("#", ""), 16);
				final boolean machineReqd;
				if(JSONUtils.hasField(json, "machine_required")) {
					machineReqd = true;
				}else {
					machineReqd = false;
				}
				
				return new BathCrafting(recipeId, ingredienta, ingredientb, output, stirs, fluid, color, machineReqd);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Bath Crafting Recipe from JSON: " + e.getMessage());
				return null;
			}
			
			
		}

		@Override
		public BathCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient inputa = Ingredient.read(buffer);
			final Ingredient inputb = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int stirs = buffer.readInt();
			final Fluids fluid = buffer.readEnumValue(Fluids.class);
			final int color = buffer.readInt();
			final boolean machineReqd = buffer.readBoolean();
			
			return new BathCrafting(recipeId, inputa, inputb, output, stirs, fluid, color, machineReqd);
		}

		@Override
		public void write(PacketBuffer buffer, BathCrafting recipe) {
			recipe.inputa.write(buffer);
			recipe.inputb.write(buffer);
			buffer.writeItemStack(recipe.output);
			buffer.writeInt(recipe.stirs);
			buffer.writeEnumValue(recipe.fluid);
			buffer.writeInt(recipe.color);
			buffer.writeBoolean(recipe.machineReqd);
			
		}
		
	}
	
	public static class TypeBathCrafting implements IRecipeType<BathCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:bath";
		}
	}

	
}
