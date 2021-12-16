package me.haydenb.assemblylinemachines.crafting;

import java.util.List;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidInGroundRecipe implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<FluidInGroundRecipe> FIG_RECIPE = new TypeFluidInGroundRecipe();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Fluid fluid;
	private final ResourceLocation id;
	private int odds;
	private int minAmount;
	private int maxAmount;
	private FluidInGroundCriteria figc;
	
	public FluidInGroundRecipe(ResourceLocation id, Fluid fluid, int odds, int minAmount, int maxAmount, FluidInGroundCriteria figc) {
		
		this.fluid = fluid;
		this.id = id;
		this.odds = odds;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.figc = figc;
	}
	
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
		
		return true;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
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
		return FIG_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		return List.of(Ingredient.of(Registry.getItem("pump"), Registry.getItem("pumpshaft")));
	}
	
	@Override
	public List<FluidStack> getJEIFluidOutputs() {
		return List.of(new FluidStack(fluid, 1000));
	}
	
	public int getChance() {
		return odds;
	}
	
	public FluidInGroundCriteria getCriteria() {
		return figc;
	}
	
	public int getMinimum() {
		return minAmount;
	}
	
	public int getMaximum() {
		return maxAmount;
	}
	
	public Fluid getFluid() {
		return fluid;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<FluidInGroundRecipe>{

		@Override
		public FluidInGroundRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
				if(f == null) {
					throw new IllegalArgumentException("Could not find this fluid.");
				}
				
				int odds = GsonHelper.getAsInt(json, "chance") - 1;
				if(odds < 0 || odds > 99) {
					throw new IllegalArgumentException("Chance must be more than 0 and less than 101.");
				}
				int min = GsonHelper.getAsInt(json, "min");
				int max = GsonHelper.getAsInt(json, "max");
				FluidInGroundCriteria figc = FluidInGroundCriteria.valueOf(GsonHelper.getAsString(json, "criteria_set").toUpperCase());
				return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Fluid-In-Ground Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public FluidInGroundRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Fluid f = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
			int odds = buffer.readInt();
			int min = buffer.readInt();
			int max = buffer.readInt();
			FluidInGroundCriteria figc = buffer.readEnum(FluidInGroundCriteria.class);
			
			return new FluidInGroundRecipe(recipeId, f, odds, min, max, figc);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FluidInGroundRecipe recipe) {
			buffer.writeResourceLocation(recipe.getFluid().getRegistryName());
			buffer.writeInt(recipe.getChance());
			buffer.writeInt(recipe.getMinimum());
			buffer.writeInt(recipe.getMaximum());
			buffer.writeEnum(recipe.getCriteria());
		}
		
	}
	
	public static class TypeFluidInGroundRecipe implements RecipeType<FluidInGroundRecipe>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:fluid_in_ground";
		}
		
	}
	
	public static enum FluidInGroundCriteria{
		OVERWORLD_PREFCOLD(34, 99, true, new TextComponent("�9Favors very cold biomes.")), OVERWORLD_PREFHOT(102, 99, true, new TextComponent("�cFavors very hot biomes.")), 
		OVERWORLD_ONLYCOLD(34, 99, true, new TextComponent("�9Only in very cold biomes.")), OVERWORLD_ONLYHOT(102, 99, true, new TextComponent("�cOnly in very hot biomes.")),
		OVERWORLD_ANY(0, 133, false, new TextComponent("�2Found in The Overworld.")), NETHER(0, 99, false, new TextComponent("�4Found in The Nether.")), 
		END(68, 99, false, new TextComponent("�5Found in The End.")), CHAOS_PLANE(34, 133, false, new TextComponent("�dFound in the Chaos Plane."));
		
		private final int jeiBlitX, jeiBlitY;
		private final TextComponent descriptor;
		private final boolean isOverworld;
		
		FluidInGroundCriteria(int jeiBlitX, int jeiBlitY, boolean isOverworld, TextComponent descriptor){
			this.jeiBlitX = jeiBlitX;
			this.jeiBlitY = jeiBlitY;
			this.descriptor = descriptor;
			this.isOverworld = isOverworld;
		}
		public int getJeiBlitX() {
			return jeiBlitX;
		}
		
		public int getJeiBlitY() {
			return jeiBlitY;
		}
		
		public List<Component> getTooltip(Component fluidDisplayName, int chanceToGenerate){
			
			if(isOverworld) {
				return List.of(fluidDisplayName, OVERWORLD_ANY.descriptor, this.descriptor, new TextComponent("�e" + chanceToGenerate + "% chance to generate."));
			}else {
				return List.of(fluidDisplayName, this.descriptor, new TextComponent("�e" + chanceToGenerate + "% chance to generate."));
			}
		}
	}
	
}
