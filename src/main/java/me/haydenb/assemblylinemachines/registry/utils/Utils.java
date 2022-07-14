package me.haydenb.assemblylinemachines.registry.utils;

import java.util.*;
import java.util.function.BiFunction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;

import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.tags.ITag;

public class Utils {

	public static final Gson GSON = new Gson();
	
	public static final Direction[] CARDINAL_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	public static final Random RAND = new Random();
	
	public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

		int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
					Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = Shapes.empty();
		}

		return buffer[0];
	}

	public static <T extends BlockEntity> T getBlockEntity(final Inventory pInv, final FriendlyByteBuf data,
			Class<T> clazz) {
		Objects.requireNonNull(pInv, "This object cannot be null.");
		Objects.requireNonNull(data, "This object cannot be null.");

		BlockEntity posEntity = pInv.player.getCommandSenderWorld().getBlockEntity(data.readBlockPos());

		return clazz.cast(posEntity);
	}
	
	public static RandomizableContainerBlockEntity getBlockEntity(Inventory pInv, FriendlyByteBuf data) {
		return getBlockEntity(pInv, data, RandomizableContainerBlockEntity.class);
	}

	public static void spawnItem(ItemStack stack, BlockPos pos, Level world) {
		ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
		world.addFreshEntity(ent);
	}

	/**
	 * Attempts to deposit into all slots in a handler. Returns the remainder.
	 */
	public static ItemStack attemptDepositIntoAllSlots(ItemStack stack, IItemHandler handler) {
		for(int i = 0; i < handler.getSlots(); i++) {
			if(stack.isEmpty()) {
				break;
			}
			stack = handler.insertItem(i, stack, false);
		}
		return stack;
	}

	public static <T extends Recipe<Container>> BiFunction<BlockEntity, Container, Optional<Recipe<Container>>> recipeFunction(RecipeType<T> recipeType){
		
		return new BiFunction<>() {
			@SuppressWarnings("unchecked")
			@Override
			public Optional<Recipe<Container>> apply(BlockEntity entity, Container container) {
				return (Optional<Recipe<Container>>) entity.getLevel().getRecipeManager().getRecipeFor(recipeType, container, entity.getLevel());
			}
		};
	}
	
	public static PredicateLazy<ItemStack> getItemStackWithTag(JsonObject json){
		return PredicateLazy.of(() -> {
			if(GsonHelper.isValidNode(json, "item")) {
				return ShapedRecipe.itemStackFromJson(json);
			}else if(GsonHelper.isValidNode(json, "tag")) {
				ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Keys.ITEMS, new ResourceLocation(GsonHelper.getAsString(json, "tag"))));
				int count = GsonHelper.isValidNode(json, "count") ? GsonHelper.getAsInt(json, "count") : 1;
				if(tag.isEmpty()) return ItemStack.EMPTY;
				List<Item> values = new ArrayList<>(tag.stream().toList());
				String preferredModid = ALMConfig.getCommonConfig().preferredModid().get();
				if(!preferredModid.isBlank() && ModList.get().isLoaded(preferredModid)) {
					Optional<Item> optionalItem = values.stream().filter((item) -> item.getRegistryName().getNamespace().equalsIgnoreCase(preferredModid)).sorted((a, b) -> a.getRegistryName().toString().compareToIgnoreCase(b.getRegistryName().toString())).findFirst();
					if(optionalItem.isPresent()) values = new ArrayList<>(List.of(optionalItem.get()));
				}
				
				Collections.sort(values, (a, b) -> a.getRegistryName().toString().compareToIgnoreCase(b.getRegistryName().toString()));
				return new ItemStack(values.get(0), count);
			}
			throw new IllegalArgumentException("Unknown field in ItemStack Tag.");
		}, (is) -> !is.isEmpty());
	}
	
	public static boolean containsArgument(CommandContext<CommandSourceStack> context, String argument) {
		try {
			context.getArgument(argument, Object.class);
			return true;
		}catch(IllegalArgumentException e) {
			return false;
		}
		
	}
}
