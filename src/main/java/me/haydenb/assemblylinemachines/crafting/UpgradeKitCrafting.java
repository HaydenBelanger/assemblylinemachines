package me.haydenb.assemblylinemachines.crafting;

import java.util.*;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.plugins.jei.IRecipeCategoryBuilder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class UpgradeKitCrafting implements Recipe<Container>, IRecipeCategoryBuilder {

	public static final RecipeType<UpgradeKitCrafting> UPGRADING_RECIPE = new TypeUpgradeKitCrafting();
	public static final Serializer SERIALIZER = new Serializer();
	
	private final Ingredient inputBlock;
	private final UpgradeKit inputUpgradeKit;
	private final Block outputBlock;
	private final Map<Integer, Integer> slotCopying;
	private final ResourceLocation id;
	
	public UpgradeKitCrafting(ResourceLocation id, Ingredient inputBlock, UpgradeKit inputUpgradeKit, Block outputBlock, Map<Integer, Integer> slotCopying) {
		this.inputBlock = inputBlock;
		this.inputUpgradeKit = inputUpgradeKit;
		this.outputBlock = outputBlock;
		this.slotCopying = slotCopying;
		this.id = id;
	}
	
	@Override
	public boolean matches(Container p_44002_, Level p_44003_) {
		return true;
	}

	@Override
	public ItemStack assemble(Container p_44001_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return UPGRADING_RECIPE;
	}
	
	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public static boolean tryUpgrade(BlockPos upgradeBlock, Level level, ItemStack upgradeKit) {
		if(level.isClientSide) throw new IllegalArgumentException("Upgrade attempted from the client side.");
		List<UpgradeKitCrafting> list = level.getRecipeManager().getAllRecipesFor(UPGRADING_RECIPE);
		for(UpgradeKitCrafting recipe : list) {
			if(recipe.inputBlock.test(level.getBlockState(upgradeBlock).getBlock().asItem().getDefaultInstance()) && upgradeKit.is(recipe.inputUpgradeKit.item)) {
				HashMap<Integer, ItemStack> copyItems = new HashMap<>();
				if(!recipe.slotCopying.isEmpty() && level.getBlockEntity(upgradeBlock) instanceof Container) {
					Container container = (Container) level.getBlockEntity(upgradeBlock);
					for(Entry<Integer, Integer> sourceSlot : recipe.slotCopying.entrySet()) {
						copyItems.put(sourceSlot.getValue(), container.getItem(sourceSlot.getKey()));
						container.removeItemNoUpdate(sourceSlot.getKey());
					}
					container.setChanged();
				}
				
				BlockState df = recipe.outputBlock.defaultBlockState();
				DirectionProperty facing = HorizontalDirectionalBlock.FACING;
				if(df.hasProperty(facing) && level.getBlockState(upgradeBlock).hasProperty(facing)) df = df.setValue(facing, level.getBlockState(upgradeBlock).getValue(facing));
				level.setBlockAndUpdate(upgradeBlock, df);
				
				if(!copyItems.isEmpty()) {
					if(level.getBlockEntity(upgradeBlock) instanceof Container) {
						Container newContainer = (Container) level.getBlockEntity(upgradeBlock);
						for(Entry<Integer, ItemStack> destinationSlot : copyItems.entrySet()) {
							newContainer.setItem(destinationSlot.getKey(), destinationSlot.getValue());
						}
						newContainer.setChanged();
					}else {
						NonNullList<ItemStack> nnl = NonNullList.create();
						nnl.addAll(copyItems.values());
						Containers.dropContents(level, upgradeBlock, nnl);
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<UpgradeKitCrafting>{
		
		@Override
		public UpgradeKitCrafting fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient inputBlock = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_block"));
			UpgradeKit upgradeKit = UpgradeKit.valueOf(GsonHelper.getAsString(json, "upgrade_kit"));
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "output_block")));
			Map<Integer, Integer> slotCopy = new HashMap<>();
			if(GsonHelper.isObjectNode(json, "slot_copy")) {
				JsonObject slotJson = GsonHelper.getAsJsonObject(json, "slot_copy");
				slotJson.entrySet().forEach((entry) ->{
					slotCopy.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsInt());
				});
			}
			
			return new UpgradeKitCrafting(recipeId, inputBlock, upgradeKit, block, slotCopy);
		}
		
		@Override
		public UpgradeKitCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient inputBlock = Ingredient.fromNetwork(buffer);
			UpgradeKit upgradeKit = buffer.readEnum(UpgradeKit.class);
			Block block = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
			Map<Integer, Integer> slotCopy = buffer.readMap((buf) -> buf.readInt(), (buf) -> buf.readInt());
			return new UpgradeKitCrafting(recipeId, inputBlock, upgradeKit, block, slotCopy);
		}
		
		@Override
		public void toNetwork(FriendlyByteBuf buffer, UpgradeKitCrafting recipe) {
			recipe.inputBlock.toNetwork(buffer);
			buffer.writeEnum(recipe.inputUpgradeKit);
			buffer.writeResourceLocation(recipe.outputBlock.getRegistryName());
			buffer.writeMap(recipe.slotCopying, (bufferkey, key) -> bufferkey.writeInt(key), (buffervalue, value) -> buffervalue.writeInt(value));
		}
		
	}
	
	public static enum UpgradeKit{
		MKII(Registry.getItem("mkii_upgrade_kit"));
		
		private final Item item;
		
		UpgradeKit(Item item){
			this.item = item;
		}
	}
	
	public static class TypeUpgradeKitCrafting implements RecipeType<UpgradeKitCrafting>{
		
		@Override
		public String toString() {
			return "assemblylinemachines:upgrade_kit";
		}
	}
}
