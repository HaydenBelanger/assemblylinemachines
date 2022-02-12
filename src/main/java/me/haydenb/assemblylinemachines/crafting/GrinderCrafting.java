package me.haydenb.assemblylinemachines.crafting;

import java.util.*;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GrinderCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<GrinderCrafting> GRINDER_RECIPE = new TypeGrinderCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private static final Random RAND = new Random();
	
	private final Ingredient input;
	private final ItemStack output;
	private final int grinds;
	private final Blade tier;
	private final ResourceLocation id;
	private final boolean machineReqd;
	private final float chanceToDouble;
	
	public GrinderCrafting(ResourceLocation id, Ingredient input, ItemStack output, int grinds, Blade tier, boolean machineReqd, float chanceToDouble) {
		this.input = input;
		this.output = output;
		this.grinds = grinds;
		this.tier = tier;
		this.id = id;
		this.machineReqd = machineReqd;
		this.chanceToDouble = chanceToDouble;
	}
	
	@Override
	public boolean matches(Container inv, Level worldIn) {
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
	}

	public boolean getMachineReqd() {
		return machineReqd;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		if(inv instanceof IMachineDataBridge) {
			inv.getItem(1).shrink(1);
			((IMachineDataBridge) inv).setCycles(grinds * 2.3f);
		}
		ItemStack stack = this.output.copy();
		if(chanceToDouble != 0f && RAND.nextFloat() <= chanceToDouble) stack.setCount(stack.getCount() * 2);
		return stack;
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
	
	@Override
	public List<Ingredient> getJEIItemIngredients() {
		Ingredient ing = this.getMachineReqd() ? Ingredient.of(Registry.getItem("simple_grinder"), Registry.getItem("electric_grinder")) 
				: Ingredient.of(Registry.getItem("hand_grinder"), Registry.getItem("simple_grinder"), Registry.getItem("electric_grinder"));
		return List.of(input, ing, Blade.getAllBladesAtMinTier(getBlade().tier));
	}
	
	@Override
	public List<List<ItemStack>> getJEIItemOutputLists() {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(output);
		if(chanceToDouble != 0f) stacks.add(new ItemStack(output.getItem(), output.getCount() * 2));
		return List.of(stacks);
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
	
	public Blade getBlade() {
		return tier;
	}
	
	public int getGrinds() {
		return grinds;
	}
	
	public float getChanceToDouble() {
		return chanceToDouble;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GrinderCrafting>{

		@Override
		public GrinderCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
				final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
				final int grinds = GsonHelper.getAsInt(json, "grinds");
				final Blade tier = Blade.valueOf(Blade.class, GsonHelper.getAsString(json, "bladetype"));
				
				if(tier == Blade.NONE) {
					AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: Cannot use 'none' as bladetype.");
					return null;
				}
				final boolean machineReqd;
				if(GsonHelper.isValidNode(json, "machine_required") && GsonHelper.getAsBoolean(json, "machine_required")) {
					machineReqd = true;
				}else {
					machineReqd = false;
				}
				
				final float chanceToDouble = GsonHelper.isValidNode(json, "chanceToDouble") ? GsonHelper.getAsFloat(json, "chanceToDouble") : 0f;
				
				return new GrinderCrafting(recipeId, input, output, grinds, tier, machineReqd, chanceToDouble);
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
			final Blade tier = buffer.readEnum(Blade.class);
			final boolean machineReqd = buffer.readBoolean();
			final float chanceToDouble = buffer.readFloat();
			
			return new GrinderCrafting(recipeId, input, output, grinds, tier, machineReqd, chanceToDouble);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, GrinderCrafting recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeInt(recipe.grinds);
			buffer.writeEnum(recipe.tier);
			buffer.writeBoolean(recipe.machineReqd);
			buffer.writeFloat(recipe.chanceToDouble);
			
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
