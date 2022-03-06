package me.haydenb.assemblylinemachines.crafting;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidBath;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BathCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<BathCrafting> BATH_RECIPE = new TypeBathCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private static final Random RAND = new Random();
	private static final Supplier<List<Item>> ILLEGAL_RECIPE_ITEMS = Suppliers.memoize(() -> Stream.concat(List.of(Registry.getItem("wooden_stirring_stick"), Registry.getItem("pure_iron_stirring_stick"),
			Registry.getItem("steel_stirring_stick")).stream(), BlockFluidBath.VALID_FILL_ITEMS.stream()).collect(Collectors.toList()));
	
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
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}
	@Override
	public RecipeType<?> getType() {
		return BATH_RECIPE;
	}
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
		if(inv instanceof TEFluidBath) {
			if(type == BathOption.MIXER_ONLY) {
				return false;
			}
			TEFluidBath finv = (TEFluidBath) inv;
			
			if(inputa.test(finv.getItem(1))) {
				if(inputb.test(finv.getItem(2))) {
					return true;
				}
			}
			
			if(inputa.test(finv.getItem(2))) {
				if(inputb.test(finv.getItem(1))) {
					return true;
				}
			}
		}else if(inv instanceof IMachineDataBridge){
			if(type == BathOption.BASIN_ONLY) {
				return false;
			}
			if(inputa.test(inv.getItem(1))) {
				if(inputb.test(inv.getItem(2))) {
					return true;
				}
			}
			
			if(inputa.test(inv.getItem(2))) {
				if(inputb.test(inv.getItem(1))) {
					return true;
				}
			}
		}else {
			if(type == BathOption.BASIN_ONLY) {
				return false;
			}
			if(inputa.test(inv.getItem(0))) {
				if(inputb.test(inv.getItem(1))) {
					return true;
				}
			}
			
			if(inputa.test(inv.getItem(1))) {
				if(inputb.test(inv.getItem(0))) {
					return true;
				}
			}
		}
		return false;
		
	}

	public BathOption getMachineMode() {
		return type;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		if(inv instanceof IMachineDataBridge) {
			IMachineDataBridge data = (IMachineDataBridge) inv;
			IFluidHandler handler = data.getCraftingFluidHandler(Optional.empty());
			if(handler == null || handler.getFluidInTank(0).getFluid() != fluid.getAssocFluid() || handler.drain(percent.getMB(), FluidAction.SIMULATE).getAmount() != percent.getMB()) return ItemStack.EMPTY;
			int rand = RAND.nextInt(9) * data.getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
			int cons = percent.getMB();
			if(rand > 21) {
				cons = 0;
			}else if(rand > 15) {
				cons = (int) Math.round((double) cons * 0.25d);
			}else if(rand > 10) {
				cons = (int) Math.round((double) cons * 0.5d);
			}else if(rand > 5) {
				cons = (int) Math.round((double) cons * 0.75d);
			}
			
			handler.drain(cons, FluidAction.EXECUTE);
			inv.getItem(1).shrink(1);
			inv.getItem(2).shrink(1);
			data.setCycles((float) stirs * 3.6f);
		}
		return this.output.copy();
	}

	@Override
	public ItemStack getResultItem() {
		return this.output.copy();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nnl = NonNullList.create();
		nnl.add(inputa);
		nnl.add(inputb);
		return nnl;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		ArrayList<ItemLike> items = new ArrayList<>();
		switch(this.type) {
		case MIXER_ONLY:
		case ALL:
			items.add(Registry.getItem("simple_fluid_mixer"));
			items.add(Registry.getItem("electric_fluid_mixer"));
			items.add(Registry.getItem("mkii_fluid_mixer"));
			if(this.type == BathOption.MIXER_ONLY) break;
		case BASIN_ONLY:
			items.add(Registry.getItem("fluid_bath"));
			break;
		}
		
		return List.of(inputa, inputb, Ingredient.of(items.toArray(new ItemLike[items.size()])));
	}
	
	@Override
	public List<FluidStack> getJEIFluidInputs() {
		return List.of(new FluidStack(fluid.getAssocFluid(), percent.getMB()));
	}
	
	@Override
	public List<ItemStack> getJEIItemOutputs() {
		return List.of(output);
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
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<BathCrafting>{

		@Override
		public BathCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient ingredienta = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_a"));
				final Ingredient ingredientb = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_b"));
				
				ItemStack[] ingredients = ArrayUtils.addAll(ingredienta.getItems(), ingredientb.getItems());
				List<Item> illegalItems = ILLEGAL_RECIPE_ITEMS.get();
				for(ItemStack is : ingredients) {
					if(illegalItems.contains(is.getItem())) {
						throw new IllegalArgumentException("Recipe used " + is.getItem().getRegistryName().toString() + ", which cannot be used for a Bath recipe.");
					}
				}
				Arrays.asList(ingredienta.getItems(), ingredientb.getItems());
				
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int stirs = GsonHelper.getAsInt(json, "stirs");
				final BathCraftingFluids fluid = BathCraftingFluids.valueOf(GsonHelper.getAsString(json, "fluid").toUpperCase());
				if(fluid == BathCraftingFluids.NONE) {
					throw new IllegalArgumentException("Fluid cannot be 'NONE'.");
				}
				
				final int color = Integer.parseInt(GsonHelper.getAsString(json, "mix_color").replace("#", ""), 16);
				final BathOption machineReqd;
				if(GsonHelper.isValidNode(json, "mixer_type")) {
					machineReqd = BathOption.valueOf(GsonHelper.getAsString(json, "mixer_type").toUpperCase());
				}else {
					machineReqd = BathOption.ALL;
				}
				
				final BathPercentage percent;
				
				if(GsonHelper.isValidNode(json, "drain_percent")) {
					percent = BathPercentage.valueOf(GsonHelper.getAsString(json, "drain_percent").toUpperCase());
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
		public BathCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			final Ingredient inputa = Ingredient.fromNetwork(buffer);
			final Ingredient inputb = Ingredient.fromNetwork(buffer);
			final ItemStack output = buffer.readItem();
			final int stirs = buffer.readInt();
			final BathCraftingFluids fluid = buffer.readEnum(BathCraftingFluids.class);
			final int color = buffer.readInt();
			final BathOption machineReqd = buffer.readEnum(BathOption.class);
			final BathPercentage percent = buffer.readEnum(BathPercentage.class);
			
			return new BathCrafting(recipeId, inputa, inputb, output, stirs, fluid, color, machineReqd, percent);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, BathCrafting recipe) {
			recipe.inputa.toNetwork(buffer);
			recipe.inputb.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.stirs);
			buffer.writeEnum(recipe.fluid);
			buffer.writeInt(recipe.color);
			buffer.writeEnum(recipe.type);
			buffer.writeEnum(recipe.percent);
			
		}
		
	}
	
	public static class TypeBathCrafting implements RecipeType<BathCrafting>{
		
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
		
		public int getMB() {
			return crankmixeruse;
		}
	}
}
