package me.haydenb.assemblylinemachines.crafting;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.block.machines.BlockRefinery.TERefinery;
import me.haydenb.assemblylinemachines.crafting.RefiningCrafting.RefineryIO.RefineryIOType;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.jei.RecipeCategoryBuilder.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unchecked")
public class RefiningCrafting implements Recipe<Container>, IRecipeCategoryBuilder{


	public static final RecipeType<RefiningCrafting> REFINING_RECIPE = new RecipeType<>() {
		@Override
		public String toString() {
			return "assemblylinemachines:refining";
		}
	};

	public static final RefiningSerializer SERIALIZER = new RefiningSerializer();

	private final Cache<Integer, List<?>> streamCache = CacheBuilder.newBuilder().build();

	public final Block attachmentBlock;
	private final List<RefineryIO> io;
	public final int time;
	private final ResourceLocation id;

	public RefiningCrafting(ResourceLocation id, Block attachmentBlock, List<RefineryIO> io, int time) {

		this.id = id;
		this.attachmentBlock = attachmentBlock;
		this.time = time;
		this.io = io;
	}

	@Override
	public boolean matches(Container inv, Level worldIn) {

		if(inv instanceof TERefinery refinery) {
			if(!refinery.getLevel().getBlockState(refinery.getBlockPos().above()).getBlock().equals(attachmentBlock)) return false;

			for(Ingredient ing : this.getItemIngredients()) {
				if(!ing.test(refinery.getItem(1))) return false;
			}

			for(FluidStack fs : this.getJEIFluidInputs()) {
				if(!fs.isFluidEqual(refinery.tankin) || refinery.tankin.getAmount() < fs.getAmount()) return false;
			}

			return true;
		}
		return false;
	}

	public void performOperations(TERefinery refinery) {
		for(RefineryIO io : getAll(true)) {
			float chance = io.odds;
			chance = switch(refinery.getUpgradeAmount(Upgrades.MACHINE_CONSERVATION)) {
			case 3 -> chance * 2f;
			case 2 -> chance * 1.5f;
			case 1 -> chance;
			default -> chance * 0f;
			};

			switch(io.type) {
			case FLUID:
				int reduceAmt = io.fluid.getAmount();
				if(refinery.getLevel().getRandom().nextFloat() < chance) reduceAmt = Math.round(reduceAmt / 3f);
				refinery.tankin.shrink(reduceAmt);
				break;
			default:
				if(!(refinery.getLevel().getRandom().nextFloat() < chance)) refinery.getItem(1).shrink(1);
			}
		}
	}

	public boolean performOutputs(TERefinery refinery) {
		ArrayList<Consumer<Void>> actions = new ArrayList<>();
		boolean useFirstTank = true;

		for(RefineryIO io : getAll(false)) {
			float chance = io.odds;
			chance = switch(refinery.getUpgradeAmount(Upgrades.MACHINE_EXTRA)) {
			case 3 -> chance * 2f;
			case 2 -> chance * 1.5f;
			case 1 -> chance;
			default -> chance * 0f;
			};

			switch(io.type) {
			case FLUID:
				FluidStack tank = useFirstTank ? refinery.tankouta : refinery.tankoutb;
				int fillAmt = io.fluid.getAmount();
				if(refinery.getLevel().getRandom().nextFloat() < chance) fillAmt = Math.round(fillAmt * 1.5f);
				final int finalFillAmt = fillAmt;

				if(tank.isEmpty()) {
					final boolean consumerTank = useFirstTank;

					actions.add((v) -> {
						if(consumerTank) {
							refinery.tankouta = new FluidStack(io.fluid.getFluid(), finalFillAmt);
						}else {
							refinery.tankoutb = new FluidStack(io.fluid.getFluid(), finalFillAmt);
						}
					});
				}else if(tank.isFluidEqual(io.fluid) && tank.getAmount() + fillAmt <= 4000) {
					actions.add((v) -> tank.grow(finalFillAmt));
				}else {
					return false;
				}
				useFirstTank = false;
				break;
			default:
				int count = io.output.getCount();
				if(refinery.getLevel().getRandom().nextFloat() < chance) count = Math.round(count * 1.5f);
				final int finalCount = count;

				if(refinery.getItem(0).isEmpty()) {
					actions.add((v) -> refinery.setItem(0, new ItemStack(io.output.getItem(), finalCount)));
				}else if(ItemHandlerHelper.canItemStacksStack(refinery.getItem(0), io.output) &&
						refinery.getItem(0).getCount() + count <= refinery.getItem(0).getMaxStackSize()) {
					actions.add((v) -> refinery.getItem(0).grow(finalCount));
				}else {
					return false;
				}
			}
		}

		for(Consumer<Void> action : actions) action.accept(null);
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
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeType<?> getType() {
		return REFINING_RECIPE;
	}

	public List<FluidStack> getJEIFluidInputs() {
		try {
			return (List<FluidStack>) streamCache.get(0, () -> io.stream().filter((rio) -> rio.isInput && !rio.fluid.isEmpty()).map((rio) -> rio.fluid).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<Ingredient> getJEIItemIngredients() {
		try {
			return (List<Ingredient>) streamCache.get(1, () -> Stream.concat(getCraftingStationIngredients().stream(), getItemIngredients().stream()).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<Ingredient> getItemIngredients(){
		try {
			return (List<Ingredient>) streamCache.get(2, () -> io.stream().filter((rio) -> rio.isInput && !rio.input.get().isEmpty()).map((rio) -> rio.input.get()).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<Ingredient> getCraftingStationIngredients(){
		try {
			return (List<Ingredient>) streamCache.get(3, () -> List.of(Ingredient.of(attachmentBlock), Ingredient.of(Registry.getBlock("refinery"))));
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<FluidStack> getJEIFluidOutputs() {
		try {
			return (List<FluidStack>) streamCache.get(4, () -> io.stream().filter((rio) -> !rio.isInput && !rio.fluid.isEmpty()).map((rio) -> rio.fluid).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<ItemStack> getJEIItemOutputs() {
		try {
			return (List<ItemStack>) streamCache.get(5, () -> io.stream().filter((rio) -> !rio.isInput && !rio.output.isEmpty()).map((rio) -> rio.output).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public List<RefineryIO> getAll(boolean inputs){
		try {
			return (List<RefineryIO>) streamCache.get(inputs ? 6 : 7, () -> io.stream().filter((rio) -> rio.isInput == inputs).toList());
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	@Override
	public List<?> getJEIComponents() {
		try {
			return streamCache.get(8, () -> {
				ArrayList<Object> inputs = new ArrayList<>(Stream.of(getJEIItemIngredients(), getJEIFluidInputs()).flatMap(Collection::stream).toList());
				while(inputs.size() < 4) {
					inputs.add(Ingredient.EMPTY);
				}

				ArrayList<Object> outputs = new ArrayList<>(Stream.of(inputs, getJEIItemOutputs(), getJEIFluidOutputs()).flatMap(Collection::stream).toList());
				while(outputs.size() < 7) {
					outputs.add(Ingredient.EMPTY);
				}

				return outputs;
			});
		}catch(ExecutionException e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public static class RefiningSerializer implements RecipeSerializer<RefiningCrafting>{

		@Override
		public RefiningCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			try {
				ArrayList<RefineryIO> allIO = new ArrayList<>();
				for(String subNode : RefineryIO.IO_PROCESSOR.keySet()) {
					if(GsonHelper.isValidNode(json, subNode)) allIO.add(RefineryIO.IO_PROCESSOR.get(subNode).apply(GsonHelper.getAsJsonObject(json, subNode)));
				}
				if(!allIO.stream().anyMatch((io) -> io.isInput)) throw new IllegalArgumentException("At least one input must be set.");
				if(!allIO.stream().anyMatch((io) -> !io.isInput)) throw new IllegalArgumentException("At least one output must be set.");

				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "attachment")));
				int time = GsonHelper.getAsInt(json, "proc_time");

				return new RefiningCrafting(recipeId, block, allIO, time);
			}catch(Exception e) {
				e.printStackTrace();
				return null;
			}


		}

		@Override
		public RefiningCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {

			Block attachment = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
			int time = buffer.readInt();
			List<RefineryIO> io = buffer.readList((buf) -> {
				RefineryIOType type = buf.readEnum(RefineryIOType.class);
				boolean isInput = buf.readBoolean();
				float odds = buf.readFloat();
				FluidStack fluid = type == RefineryIOType.FLUID ? buf.readFluidStack() : FluidStack.EMPTY;
				ItemStack output = type == RefineryIOType.ITEMSTACK ? buf.readItem() : ItemStack.EMPTY;
				Ingredient inputIng = type == RefineryIOType.INGREDIENT ? Ingredient.fromNetwork(buf) : Ingredient.EMPTY;
				Lazy<Ingredient> input = Lazy.of(() -> inputIng);
				return new RefineryIO(input, output, fluid, isInput, odds, type);
			});

			return new RefiningCrafting(recipeId, attachment, io, time);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, RefiningCrafting recipe) {

			buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(recipe.attachmentBlock));
			buffer.writeInt(recipe.time);
			buffer.writeCollection(recipe.io, (buf, io) -> {
				buf.writeEnum(io.type);
				buf.writeBoolean(io.isInput);
				buf.writeFloat(io.odds);
				switch(io.type) {
				case FLUID -> buf.writeFluidStack(io.fluid);
				case ITEMSTACK -> buf.writeItemStack(io.output, true);
				case INGREDIENT -> io.input.get().toNetwork(buf);
				}
			});
		}

	}

	public static class RefineryIO{

		private final Lazy<Ingredient> input;
		private final ItemStack output;
		private final FluidStack fluid;
		private final boolean isInput;
		private final float odds;
		private final RefineryIOType type;

		private static final HashMap<String, Function<JsonObject, RefineryIO>> IO_PROCESSOR = new HashMap<>();
		static {
			IO_PROCESSOR.put("input_fluid", (json) -> new RefineryIO(jsonFluidStack(json), true, Optional.ofNullable(json.get("upgrade_multiply_chance")).map((j2) -> j2.getAsFloat()).orElse(0f)));
			IO_PROCESSOR.put("input_item", (json) -> new RefineryIO(Lazy.of(() -> Ingredient.fromJson(json)), Optional.ofNullable(json.get("upgrade_multiply_chance")).map((j2) -> j2.getAsFloat()).orElse(0f)));
			IO_PROCESSOR.put("output_item", (json) -> new RefineryIO(ShapedRecipe.itemStackFromJson(json), Optional.ofNullable(json.get("upgrade_multiply_chance")).map((j2) -> j2.getAsFloat()).orElse(0f)));

			Function<JsonObject, RefineryIO> foJSON = (json) -> new RefineryIO(jsonFluidStack(json), false, Optional.ofNullable(json.get("upgrade_multiply_chance")).map((j2) -> j2.getAsFloat()).orElse(0f));
			IO_PROCESSOR.put("output_fluid_a", foJSON);
			IO_PROCESSOR.put("output_fluid_b", foJSON);
		}

		public RefineryIO(Lazy<Ingredient> input, float odds) {
			this(input, ItemStack.EMPTY, FluidStack.EMPTY, true, odds, RefineryIOType.INGREDIENT);
		}

		public RefineryIO(ItemStack output, float odds) {
			this(Lazy.of(() -> Ingredient.EMPTY), output, FluidStack.EMPTY, false, odds, RefineryIOType.ITEMSTACK);
		}

		public RefineryIO(FluidStack fluid, boolean input, float odds) {
			this(Lazy.of(() -> Ingredient.EMPTY), ItemStack.EMPTY, fluid, input, odds, RefineryIOType.FLUID);
		}

		private RefineryIO(Lazy<Ingredient> input, ItemStack output, FluidStack fluid, boolean isInput, float odds, RefineryIOType type) {
			this.input = input;
			this.output = output;
			this.fluid = fluid;
			this.isInput = isInput;
			this.odds = odds;
			this.type = type;
		}

		private static FluidStack jsonFluidStack(JsonObject json) {
			return new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid"))), GsonHelper.getAsInt(json, "amount"));
		}

		public static enum RefineryIOType{
			FLUID, INGREDIENT, ITEMSTACK;
		}
	}

}
