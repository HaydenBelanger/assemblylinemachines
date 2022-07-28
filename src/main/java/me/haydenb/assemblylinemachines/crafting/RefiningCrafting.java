package me.haydenb.assemblylinemachines.crafting;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.cache.*;
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

	public final Block attachmentBlock;
	private final List<RefineryIO> io;
	public final int time;
	private final ResourceLocation id;
	private final LoadingCache<Integer, List<?>> streamCache;

	public RefiningCrafting(ResourceLocation id, Block attachmentBlock, List<RefineryIO> io, int time) {

		this.id = id;
		this.attachmentBlock = attachmentBlock;
		this.time = time;
		this.io = io;
		
		//Depending on the specified operation, performs different responsive operations.
		this.streamCache = CacheBuilder.newBuilder().build(CacheLoader.from((op) -> {
			return switch (op) {
			case 0 -> io.stream().filter((rio) -> rio.isInput && !rio.fluid.isEmpty()).map((rio) -> rio.fluid).toList();
			case 1 -> Stream.concat(get(RefData.CRAFTING_STATIONS).stream(), get(RefData.ITEM_INGREDIENTS).stream()).toList();
			case 2 -> io.stream().filter((rio) -> rio.isInput && !rio.input.get().isEmpty()).map((rio) -> rio.input.get()).toList();
			case 3 -> List.of(Ingredient.of(attachmentBlock), Ingredient.of(Registry.getBlock("refinery")));
			case 4 -> io.stream().filter((rio) -> !rio.isInput && !rio.fluid.isEmpty()).map((rio) -> rio.fluid).toList();
			case 5 -> io.stream().filter((rio) -> !rio.isInput && !rio.output.isEmpty()).map((rio) -> rio.output).toList();
			case 6 -> io.stream().filter((rio) -> rio.isInput).toList();
			case 7 -> io.stream().filter((rio) -> !rio.isInput).toList();
			case 8 -> {
				ArrayList<Object> inputs = new ArrayList<>(Stream.of(get(RefData.JEI_ITEM_INGREDIENTS), get(RefData.JEI_FLUID_INPUTS)).flatMap(Collection::stream).toList());
				while(inputs.size() < 4) {
					inputs.add(Ingredient.EMPTY);
				}

				ArrayList<Object> outputs = new ArrayList<>(Stream.of(inputs, get(RefData.JEI_ITEM_OUTPUTS), get(RefData.JEI_FLUID_OUTPUTS)).flatMap(Collection::stream).toList());
				while(outputs.size() < 7) {
					outputs.add(Ingredient.EMPTY);
				}

				yield outputs;
			}
			default -> List.of();
			};
		}));
	}

	@Override
	public boolean matches(Container inv, Level worldIn) {

		if(inv instanceof TERefinery refinery) {
			if(!refinery.getLevel().getBlockState(refinery.getBlockPos().above()).getBlock().equals(attachmentBlock)) return false;

			for(Ingredient ing : get(RefData.ITEM_INGREDIENTS)) {
				if(!ing.test(refinery.getItem(1))) return false;
			}

			for(FluidStack fs : get(RefData.JEI_FLUID_INPUTS)) {
				if(!fs.isFluidEqual(refinery.tankin) || refinery.tankin.getAmount() < fs.getAmount()) return false;
			}

			return true;
		}
		return false;
	}

	public void performOperations(TERefinery refinery) {
		for(RefineryIO io : get(RefData.ALL_INPUTS)) {
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

		for(RefineryIO io : get(RefData.ALL_OUTPUTS)) {
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

	@Override
	public List<?> getJEIComponents() {
		return this.get(RefData.JEI_COMPONENTS);
	}
	
	public <T> List<T> get(RefData<T> operation){
		return operation.get(this);
	}
	
	public static class RefData<T>{
		
		public static final RefData<FluidStack> JEI_FLUID_INPUTS = new RefData<>(0);
		public static final RefData<Ingredient> JEI_ITEM_INGREDIENTS = new RefData<>(1);
		public static final RefData<Ingredient> ITEM_INGREDIENTS = new RefData<>(2);
		public static final RefData<Ingredient> CRAFTING_STATIONS = new RefData<>(3);
		public static final RefData<FluidStack> JEI_FLUID_OUTPUTS = new RefData<>(4);
		public static final RefData<ItemStack> JEI_ITEM_OUTPUTS = new RefData<>(5);
		public static final RefData<RefineryIO> ALL_INPUTS = new RefData<>(6);
		public static final RefData<RefineryIO> ALL_OUTPUTS = new RefData<>(7);
		public static final RefData<?> JEI_COMPONENTS = new RefData<>(8);
		
		private final int operation;
		
		private RefData(int operation){
			this.operation = operation;
		}
		
		private List<T> get(RefiningCrafting recipe){
			return (List<T>) recipe.streamCache.getUnchecked(operation);
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
