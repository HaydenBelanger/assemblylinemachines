package me.haydenb.assemblylinemachines.crafting;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockElectricFluidMixer.TEElectricFluidMixer;
import me.haydenb.assemblylinemachines.block.machines.primitive.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
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
	private final BathCraftingFluids fluid;
	private final int stirs;
	private final ResourceLocation id;
	private final int color;
	private final BathOption type;
	private final BathPercentage percent;
	
	public BathCrafting(ResourceLocation id, Ingredient inputa, Ingredient inputb, ItemStack output, int stirs, BathCraftingFluids fluid, int color, BathOption type, BathPercentage percent) {
		this.inputa = inputa;
		this.inputb = inputb;
		this.output = output;
		this.stirs = stirs;
		this.fluid = fluid;
		this.id = id;
		this.color = color;
		this.type = type;
		this.percent = percent;
	}
	@Override
	public boolean matches(IInventory inv, World worldIn) {
		if(inv != null) {
			if(inv instanceof TEFluidBath) {
				if(type == BathOption.MIXER_ONLY) {
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
			}else if(inv instanceof TEElectricFluidMixer){
				if(type == BathOption.BASIN_ONLY) {
					return false;
				}
				if(inputa.test(inv.getStackInSlot(1))) {
					if(inputb.test(inv.getStackInSlot(2))) {
						return true;
					}
				}
				
				if(inputa.test(inv.getStackInSlot(2))) {
					if(inputb.test(inv.getStackInSlot(1))) {
						return true;
					}
				}
			}else {
				if(type == BathOption.BASIN_ONLY) {
					return false;
				}
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

	public BathOption getMachineMode() {
		return type;
	}
	
	@Override
	public boolean isDynamic() {
		return true;
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
		Item electricMixer = Registry.getItem("electric_fluid_mixer");
		Item basin = Registry.getItem("fluid_bath");
		if(this.type == BathOption.MIXER_ONLY) {
			
			if(fluid.isElectricMixerOnly()) {
				nnl.add(Ingredient.fromItems(electricMixer));
			}else {
				nnl.add(Ingredient.fromItems(mixerHandler, electricMixer));
			}
			
		}else if(this.type == BathOption.BASIN_ONLY){
			nnl.add(Ingredient.fromItems(basin));
		}else {
			nnl.add(Ingredient.fromItems(mixerHandler, electricMixer, basin));
		}
		return nnl;
	}

	@Override
	public IRecipeType<?> getType() {
		return BATH_RECIPE;
	}
	
	public BathCraftingFluids getFluid() {
		return fluid;
	}
	
	public int getStirs() {
		return stirs;
	}
	
	public int getColor() {
		return color;
	}
	
	public BathPercentage getPercentage() {
		return percent;
	}
	
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<BathCrafting>{

		@Override
		public BathCrafting read(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input_a"));
				final Ingredient ingredientb = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input_b"));
				
				final ItemStack output = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "output"));
				final int stirs = JSONUtils.getInt(json, "stirs");
				final BathCraftingFluids fluid = BathCraftingFluids.valueOf(JSONUtils.getString(json, "fluid").toUpperCase());
				if(fluid == BathCraftingFluids.NONE) {
					throw new IllegalArgumentException("Fluid cannot be 'NONE'.");
				}
				
				final int color = Integer.parseInt(JSONUtils.getString(json, "mix_color").replace("#", ""), 16);
				final BathOption machineReqd;
				if(JSONUtils.hasField(json, "mixer_type")) {
					machineReqd = BathOption.valueOf(JSONUtils.getString(json, "mixer_type").toUpperCase());
				}else {
					machineReqd = BathOption.ALL;
				}
				
				final BathPercentage percent;
				
				if(JSONUtils.hasField(json, "drain_percent")) {
					percent = BathPercentage.valueOf(JSONUtils.getString(json, "drain_percent").toUpperCase());
				}else {
					percent = BathPercentage.FULL;
				}
				
				return new BathCrafting(recipeId, ingredienta, ingredientb, output, stirs, fluid, color, machineReqd, percent);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Bath Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public BathCrafting read(ResourceLocation recipeId, PacketBuffer buffer) {
			final Ingredient inputa = Ingredient.read(buffer);
			final Ingredient inputb = Ingredient.read(buffer);
			final ItemStack output = buffer.readItemStack();
			final int stirs = buffer.readInt();
			final BathCraftingFluids fluid = buffer.readEnumValue(BathCraftingFluids.class);
			final int color = buffer.readInt();
			final BathOption machineReqd = buffer.readEnumValue(BathOption.class);
			final BathPercentage percent = buffer.readEnumValue(BathPercentage.class);
			
			return new BathCrafting(recipeId, inputa, inputb, output, stirs, fluid, color, machineReqd, percent);
		}

		@Override
		public void write(PacketBuffer buffer, BathCrafting recipe) {
			recipe.inputa.write(buffer);
			recipe.inputb.write(buffer);
			buffer.writeItemStack(recipe.output);
			buffer.writeInt(recipe.stirs);
			buffer.writeEnumValue(recipe.fluid);
			buffer.writeInt(recipe.color);
			buffer.writeEnumValue(recipe.type);
			buffer.writeEnumValue(recipe.percent);
			
		}
		
	}
	
	public static class TypeBathCrafting implements IRecipeType<BathCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:bath";
		}
	}

	public static enum BathOption{
		ALL, BASIN_ONLY, MIXER_ONLY;
	}
	
	public static enum BathPercentage{
		FULL(4, 1000), HALF(2, 500), QUARTER(1, 250);
		
		final int drop;
		final int crankmixeruse;
		BathPercentage(int drop, int use){
			this.drop = drop;
			this.crankmixeruse = use;
		}
		
		public int getDrop() {
			return drop;
		}
		
		public int getCrankUse() {
			return crankmixeruse;
		}
	}
}
