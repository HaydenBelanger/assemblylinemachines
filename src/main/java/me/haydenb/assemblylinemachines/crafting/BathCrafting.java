package me.haydenb.assemblylinemachines.crafting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidBath;
import me.haydenb.assemblylinemachines.block.machines.BlockFluidBath.TEFluidBath;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Utils.IFluidHandlerBypass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BathCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<BathCrafting> BATH_RECIPE = new RecipeType<BathCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:bath";
		}
	};
	
	public static final BathSerializer SERIALIZER = new BathSerializer();
	
	private static final Random RAND = new Random();
	private static final Lazy<List<Item>> ILLEGAL_RECIPE_ITEMS = Lazy.of(() -> Stream.concat(List.of(Registry.getItem("wooden_stirring_stick"), Registry.getItem("pure_iron_stirring_stick"),
			Registry.getItem("steel_stirring_stick")).stream(), BlockFluidBath.VALID_FILL_ITEMS.stream()).collect(Collectors.toList()));
	
	private final Lazy<Ingredient> inputa;
	private final Lazy<Ingredient> inputb;
	private final ItemStack output;
	private final BathCraftingFluids fluid;
	private final int stirs;
	private final ResourceLocation id;
	private final int color;
	private final BathOption type;
	private final BathPercentage percent;
	
	public BathCrafting(ResourceLocation id, Lazy<Ingredient> inputa, Lazy<Ingredient> inputb, ItemStack output, int stirs, BathCraftingFluids fluid, int color, BathOption type, BathPercentage percent) {
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
			
			if(inputa.get().test(finv.getItem(1))) {
				if(inputb.get().test(finv.getItem(2))) {
					return true;
				}
			}
			
			if(inputa.get().test(finv.getItem(2))) {
				if(inputb.get().test(finv.getItem(1))) {
					return true;
				}
			}
		}else if(inv instanceof IMachineDataBridge data){
			if(type == BathOption.BASIN_ONLY) {
				return false;
			}
			int sla = 1;
			int slb = 2;
			if(data.getBlockState().is(Registry.getBlock("kinetic_fluid_mixer"))) {
				sla = 0;
				slb = 1;
			}
			if(inputa.get().test(inv.getItem(sla))) {
				if(inputb.get().test(inv.getItem(slb))) {
					return true;
				}
			}
			
			if(inputa.get().test(inv.getItem(slb))) {
				if(inputb.get().test(inv.getItem(sla))) {
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
			BiFunction<Integer, FluidAction, FluidStack> drain = handler instanceof IFluidHandlerBypass ? (i, a) -> ((IFluidHandlerBypass) handler).drainBypassRestrictions(i, a) : (i, a) -> handler.drain(i, a);
			if(handler == null || handler.getFluidInTank(0).getFluid() != fluid.getAssocFluid() || drain.apply(percent.getMB(), FluidAction.SIMULATE).getAmount() != percent.getMB()) return ItemStack.EMPTY;

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
			
			drain.apply(cons, FluidAction.EXECUTE);
			if(data.getBlockState().is(Registry.getBlock("kinetic_fluid_mixer"))) {
				inv.getItem(0).shrink(1);
				inv.getItem(1).shrink(1);
				data.setCycles((float) stirs * ConfigHolder.getCommonConfig().kineticFluidMixerCycleMultiplier.get().floatValue());
			}else {
				inv.getItem(1).shrink(1);
				inv.getItem(2).shrink(1);
				data.setCycles((float) stirs * 3.6f);
			}
			
			
			
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
	public List<?> getJEIComponents() {
		ArrayList<Item> items = new ArrayList<>();
		switch(this.type) {
		case MIXER_ONLY, ALL:
			items.addAll(List.of(Registry.getItem("kinetic_fluid_mixer"), Registry.getItem("electric_fluid_mixer"), Registry.getItem("mkii_fluid_mixer")));
			if(this.type == BathOption.MIXER_ONLY) break;
		case BASIN_ONLY:
			items.add(Registry.getItem("fluid_bath"));
		}
		
		return List.of(inputa.get(), inputb.get(), Ingredient.of(items.toArray(new Item[items.size()])), new FluidStack(fluid.getAssocFluid(), percent.getMB()), output);
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
	
	public static class BathSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<BathCrafting>{

		@Override
		public BathCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> ingredienta = Lazy.of(() -> {
					Ingredient i = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_a"));
					if(!Collections.disjoint(List.of(i.getItems()), ILLEGAL_RECIPE_ITEMS.get())) throw new IllegalArgumentException(recipeId + " used an illegal item as input_a.");
					return i;
				});
				
				Lazy<Ingredient> ingredientb = Lazy.of(() -> {
					Ingredient i = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_b"));
					if(!Collections.disjoint(List.of(i.getItems()), ILLEGAL_RECIPE_ITEMS.get())) throw new IllegalArgumentException(recipeId + " used an illegal item as input_b.");
					return i;
				});
				
				ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				int stirs = GsonHelper.getAsInt(json, "stirs");
				BathCraftingFluids fluid = BathCraftingFluids.valueOf(GsonHelper.getAsString(json, "fluid").toUpperCase());
				if(fluid == BathCraftingFluids.NONE) {
					throw new IllegalArgumentException("Fluid cannot be 'NONE'.");
				}
				
				int color = Integer.parseInt(GsonHelper.getAsString(json, "mix_color").replace("#", ""), 16);
				BathOption machineReqd;
				if(GsonHelper.isValidNode(json, "mixer_type")) {
					machineReqd = BathOption.valueOf(GsonHelper.getAsString(json, "mixer_type").toUpperCase());
				}else {
					machineReqd = BathOption.ALL;
				}
				
				BathPercentage percent;
				
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
			Ingredient inputa = Ingredient.fromNetwork(buffer);
			Ingredient inputb = Ingredient.fromNetwork(buffer);
			ItemStack output = buffer.readItem();
			int stirs = buffer.readInt();
			BathCraftingFluids fluid = buffer.readEnum(BathCraftingFluids.class);
			int color = buffer.readInt();
			BathOption machineReqd = buffer.readEnum(BathOption.class);
			BathPercentage percent = buffer.readEnum(BathPercentage.class);
			
			return new BathCrafting(recipeId, Lazy.of(() -> inputa), Lazy.of(() -> inputb), output, stirs, fluid, color, machineReqd, percent);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, BathCrafting recipe) {
			recipe.inputa.get().toNetwork(buffer);
			recipe.inputb.get().toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.stirs);
			buffer.writeEnum(recipe.fluid);
			buffer.writeInt(recipe.color);
			buffer.writeEnum(recipe.type);
			buffer.writeEnum(recipe.percent);
			
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
