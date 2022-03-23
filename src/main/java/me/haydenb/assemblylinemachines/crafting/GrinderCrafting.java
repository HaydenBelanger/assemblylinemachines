package me.haydenb.assemblylinemachines.crafting;

import java.util.*;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.BlockHandGrinder.Blade;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class GrinderCrafting implements Recipe<Container>, IRecipeCategoryBuilder{

	
	public static final RecipeType<GrinderCrafting> GRINDER_RECIPE = new RecipeType<GrinderCrafting>() {
		@Override
		public String toString() {
			return "assemblylinemachines:grinder";
		}
	};
	
	public static final GrinderSerializer SERIALIZER = new GrinderSerializer();
	
	private static final Random RAND = new Random();
	
	private final Lazy<Ingredient> input;
	private final Lazy<ItemStack> output;

	public final int grinds;
	public final Blade tier;
	private final ResourceLocation id;
	private final boolean machineReqd;
	public final float chanceToDouble;
	
	public GrinderCrafting(ResourceLocation id, Lazy<Ingredient> input, Lazy<ItemStack> output, int grinds, Blade tier, boolean machineReqd, float chanceToDouble) {
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
			if(input.get().test(pinv.getItem(pinv.selected))) {
				return true;
			}
		}else if(inv instanceof BlockEntity be){
			if(be.getLevel().getBlockState(be.getBlockPos()).is(Registry.getBlock("kinetic_grinder"))) {
				Blade blade = Blade.getBladeFromItem(inv.getItem(0).getItem());
				if(blade == null || blade.tier < tier.tier) return false;
			}
			if(input.get().test(inv.getItem(1))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack assemble(Container inv) {
		
		if(inv instanceof IMachineDataBridge data && inv instanceof BlockEntity be) {
			inv.getItem(1).shrink(1);
			if(be.getLevel().getBlockState(be.getBlockPos()).is(Registry.getBlock("kinetic_grinder"))){
				int i = be.getLevel().getRandom().nextInt(0, grinds + 1);
				Blade b = Blade.getBladeFromItem(inv.getItem(0).getItem());
				if(b == null || b.tier < this.tier.tier) return ItemStack.EMPTY;
				inv.getItem(0).setDamageValue(inv.getItem(0).getDamageValue() + i);
				if(inv.getItem(0).getDamageValue() >= inv.getItem(0).getMaxDamage()) inv.getItem(0).shrink(1);
				data.setCycles((float) grinds * ConfigHolder.getCommonConfig().kineticGrinderCycleMultiplier.get().floatValue());
			}else {
				data.setCycles((float) grinds * 2.3f);
			}
		}
		ItemStack stack = output.get().copy();
		if(chanceToDouble != 0f && RAND.nextFloat() <= chanceToDouble) stack.setCount(stack.getCount() * 2);
		return stack;
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
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public List<?> getJEIComponents() {
		Ingredient ing = this.machineReqd ? Ingredient.of(Registry.getItem("kinetic_grinder"), Registry.getItem("electric_grinder")) 
				: Ingredient.of(Registry.getItem("hand_grinder"), Registry.getItem("kinetic_grinder"), Registry.getItem("electric_grinder"));
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(output.get());
		if(chanceToDouble != 0f) stacks.add(new ItemStack(output.get().getItem(), output.get().getCount() * 2));
		
		return List.of(input.get(), ing, Blade.getAllBladesAtMinTier(this.tier.tier), stacks);
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
	
	public static class GrinderSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GrinderCrafting>{

		@Override
		public GrinderCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				Lazy<Ingredient> input = Lazy.of(() -> Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input")));
				Lazy<ItemStack> supplier = Utils.getTaggedOutputFromJson(GsonHelper.getAsJsonObject(json, "output")).orElseThrow();
				
				int grinds = GsonHelper.getAsInt(json, "grinds");
				Blade tier = Blade.valueOf(Blade.class, GsonHelper.getAsString(json, "bladetype"));
				
				if(tier == Blade.NONE) {
					AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: Cannot use 'none' as bladetype.");
					return null;
				}
				boolean machineReqd;
				if(GsonHelper.isValidNode(json, "machine_required") && GsonHelper.getAsBoolean(json, "machine_required")) {
					machineReqd = true;
				}else {
					machineReqd = false;
				}
				
				float chanceToDouble = GsonHelper.isValidNode(json, "chanceToDouble") ? GsonHelper.getAsFloat(json, "chanceToDouble") : 0f;
				
				return new GrinderCrafting(recipeId, input, supplier, grinds, tier, machineReqd, chanceToDouble);
			}catch(Exception e) {
				AssemblyLineMachines.LOGGER.error("Error deserializing Grinder Crafting Recipe from JSON: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			
			
		}

		@Override
		public GrinderCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			ItemStack stack = buffer.readItem();
			int grinds = buffer.readInt();
			Blade tier = buffer.readEnum(Blade.class);
			boolean machineReqd = buffer.readBoolean();
			float chanceToDouble = buffer.readFloat();
			
			return new GrinderCrafting(recipeId, Lazy.of(() -> input), Lazy.of(() -> stack), grinds, tier, machineReqd, chanceToDouble);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, GrinderCrafting recipe) {
			recipe.input.get().toNetwork(buffer);
			buffer.writeItem(recipe.output.get());
			buffer.writeInt(recipe.grinds);
			buffer.writeEnum(recipe.tier);
			buffer.writeBoolean(recipe.machineReqd);
			buffer.writeFloat(recipe.chanceToDouble);
			
		}
		
	}
	
	public static enum SharpenerResult{
		PASS, FAIL_NO_RECIPE, FAIL_INCORRECT_BLADE;
	}

	
}
