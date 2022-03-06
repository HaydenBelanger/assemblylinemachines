package me.haydenb.assemblylinemachines.registry;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class Utils {

	public static final Direction[] CARDINAL_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	public static final BooleanProperty PURIFIER_STATES = BooleanProperty.create("enhanced");
	
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
	
	private static final Cache<String, Item> CACHED_SORTED_TAGS = CacheBuilder.newBuilder().build();
	
	/**
	 * Supplier comes pre-memoized.
	 */
	public static Supplier<ItemStack> getPreferredOrAlphabeticSupplier(Named<Item> tag, int count){
		return Suppliers.memoize(() -> {
			try {
				Item preferredResult = CACHED_SORTED_TAGS.get(tag.getName().toString(), () ->{
					List<Item> values = new ArrayList<Item>(tag.getValues());
					String preferredModid = ConfigHolder.getCommonConfig().preferredModid.get();
					if(!preferredModid.isBlank() && ModList.get().isLoaded(preferredModid)) {
						Optional<Item> optionalItem = values.stream().filter((item) -> item.getRegistryName().getNamespace().equalsIgnoreCase(preferredModid)).sorted((a, b) -> a.getRegistryName().toString().compareToIgnoreCase(b.getRegistryName().toString())).findFirst();
						if(optionalItem.isPresent()) return optionalItem.get();
					}
					
					Collections.sort(values, (a, b) -> a.getRegistryName().toString().compareToIgnoreCase(b.getRegistryName().toString()));
					return values.get(0);
				});
				
				return new ItemStack(preferredResult, count);
			}catch(ExecutionException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	public static class PhasedMap<K, V> extends ConcurrentHashMap<K, V>{

		private static final long serialVersionUID = -1346052825976440901L;
		
		private final Phases phase;
		
		public PhasedMap(Phases phase) {
			this.phase = phase;
		}
		
		public void add() {
			List<Method> methods = MethodUtils.getMethodsListWithAnnotation(BlockMachines.class, RegisterableMachine.class);
			methods.removeIf((m) -> m.getAnnotation(RegisterableMachine.class).phase() != phase);
			Consumer<Method> typedConsumer = getTypedConsumer(phase);
			methods.forEach((m) -> typedConsumer.accept(m));
		}
		
		private static Consumer<Method> getTypedConsumer(Phases phase){
			return switch(phase) {
			case BLOCK -> (m) -> {
				try {
					Registry.createBlock(m.getAnnotation(RegisterableMachine.class).blockName(), (Block) m.invoke(null), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			case BLOCK_ENTITY -> (m) -> {
				try {
					Registry.createBlockEntity(m.getAnnotation(RegisterableMachine.class).blockName(), (BlockEntityType<?>) m.invoke(null));
				}catch(Exception e) {
					e.printStackTrace();
				}
			};
			case CONTAINER -> (m) -> {
				try {
					Registry.createContainer(m.getAnnotation(RegisterableMachine.class).blockName(), (MenuType<?>) m.invoke(null));
				}catch(Exception e) {
					e.printStackTrace();
				}
			};
			case SCREEN -> (m) -> {
				try {
					m.invoke(null);
				}catch(Exception e) {
					e.printStackTrace();
				}
				
			};
			};
			
		}
	}

	public static int breakAndBreakConnected(Level world, BlockState origState, int ctx, int cmax, BlockPos posx, LivingEntity player) {
		world.destroyBlock(posx, true, player);

		int cost = 2;
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(posx.below().north().west(), posx.above().south().east()).iterator();

		while(iter.hasNext()) {
			BlockPos posq = iter.next();

			BlockState bs = world.getBlockState(posq);
			if(bs.getBlock() == origState.getBlock() && ctx <= cmax) {
				ctx++;
				cost = cost + breakAndBreakConnected(world, origState, ctx, cmax, posq, player);
			}
		}

		return cost;
	}

	public static <H> H getCapabilityFromDirection(BlockEntity pte, NonNullConsumer<LazyOptional<H>> consumer, Direction dir, Capability<H> capType) {
		BlockEntity te = pte.getLevel().getBlockEntity(pte.getBlockPos().relative(dir));

		if(te != null) {
			LazyOptional<H> cap = te.getCapability(capType, dir.getOpposite());
			H output = cap.orElse(null);
			if(output != null) {
				cap.addListener(consumer);

				return output;
			}
		}

		return null;
	}

	public static <T> TagKey<T> getTagKey(ResourceKey<net.minecraft.core.Registry<T>> key, ResourceLocation tag){
		return TagKey.create(key, tag);
	}
	
	public static boolean isInTag(BlockState block, String minecraftTag) {
		return isInTag(block, new ResourceLocation("minecraft", minecraftTag));
	}
	public static boolean isInTag(BlockState block, ResourceLocation tag) {
		return block.is(getTagKey(Keys.BLOCKS, tag));
	}
	
	public static boolean isInAnyTag(BlockState block, String... minecraftTags) {
		return isInAnyTag(block, Utils.copy(minecraftTags, ResourceLocation.class, (i) -> new ResourceLocation("minecraft", i)));
	}
	public static boolean isInAnyTag(BlockState block, ResourceLocation... tags) {
		for(ResourceLocation tag : tags) if(!isInTag(block, tag)) return false;
		return true;
	}
	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Font pFont, Component pText, int pX, int pY, int pColor) {
		FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
		pFont.draw(pPoseStack, formattedcharsequence, (float)(pX - pFont.width(formattedcharsequence) / 2), (float)pY, pColor);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Component pText, int pX, int pY) {
		Minecraft mc = Minecraft.getInstance();
		drawCenteredStringWithoutShadow(pPoseStack, mc.font, pText, pX, pY, 4210752);
	}
	
	@SuppressWarnings("unchecked")
	public static <O, I> O[] copy(I[] input, Class<O> outputClass, Function<I, O> converter) {
		O[] output = (O[]) Array.newInstance(outputClass, input.length);
		for(int i = 0; i < input.length; i++) {
			output[i] = converter.apply(input[i]);
		}
		return output;
	}
	
	public static class Formatting {

		private static final NavigableMap<Long, String> SUFFIX = new TreeMap<>();
		
		public static final DecimalFormat GENERAL_FORMAT = new DecimalFormat("###,###,###,###,###,###,###");
		public static final DecimalFormat FEPT_FORMAT = new DecimalFormat("###,##0.#");
		
		
		static {
			SUFFIX.put(1_000L, "K");
			SUFFIX.put(1_000_000L, "M");
			SUFFIX.put(1_000_000_000L, "G");
			SUFFIX.put(1_000_000_000_000L, "T");
			SUFFIX.put(1_000_000_000_000_000L, "P");
			SUFFIX.put(1_000_000_000_000_000_000L, "E");
		}
		
		public static String formatToSuffix(long value) {
			// Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
			if (value == Long.MIN_VALUE)
				return formatToSuffix(Long.MIN_VALUE + 1);
			if (value < 0)
				return "-" + formatToSuffix(-value);
			if (value < 1000)
				return Long.toString(value); // deal with easy case

			Entry<Long, String> e = SUFFIX.floorEntry(value);
			Long divideBy = e.getKey();
			String suffix = e.getValue();

			long truncated = value / (divideBy / 10); // the number part of the output times 10
			boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
			return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
		}
	}
	
	public static class MathHelper {
		
		
		//Claimed from ItemRenderer#renderItemOverlayIntoGUI, modified.
		@OnlyIn(Dist.CLIENT)
		public static void renderItemSlotBoundScaledText(Font fr, int xpos, int ypos, float scale, String text) {
			
			xpos = Math.round(xpos * (1.0f / scale));
			ypos = Math.round(ypos * (1.0f / scale));
			PoseStack mx = new PoseStack();
			
			mx.translate(0.0D, 0.0D, 200D);
			mx.scale(scale, scale, scale);
			BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			fr.drawInBatch(text, (float) (xpos + 19 - 2 - fr.width(text)), (float) (ypos + 6 + 3), 16777215, true, mx.last().pose(),
					irendertypebuffer$impl, false, 0, 15728880);
			
			irendertypebuffer$impl.endBatch();
		}
		
		@OnlyIn(Dist.CLIENT)
		public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text, boolean dropShadow, int color) {
			
			xpos = Math.round(xpos * (1.0f / scale));
			ypos = Math.round(ypos * (1.0f / scale));
			PoseStack mx = new PoseStack();
			
			mx.translate(0.0D, 0.0D, 200D);
			mx.scale(scale, scale, scale);
			BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			fr.drawInBatch(text, (float) xpos, ypos, color, dropShadow, mx.last().pose(),
					irendertypebuffer$impl, false, 0, 15728880);
			irendertypebuffer$impl.endBatch();
		}
		
		@OnlyIn(Dist.CLIENT)
		public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text) {
			
			renderScaledText(fr, xpos, ypos, scale, text, false, 0xffffff);
		}
		
		public static boolean isMouseBetween(int globalX, int globalY, int mouseX, int mouseY, int minX, int minY, int maxX, int maxY) {
			return mouseX >= globalX + minX && mouseX <= globalX + maxX && mouseY >= globalY + minY && mouseY <= globalY + maxY;
		}
		
		public static int multiplyARGBColor(int argb, float multiplier) {
			int[] argbSplit = new int[] {ARGB32.alpha(argb), ARGB32.red(argb), ARGB32.green(argb), ARGB32.blue(argb)};
			
			for(int i = 0; i < argbSplit.length; i++) {
				argbSplit[i] = Math.round((float) argbSplit[i] * multiplier);
			}
			
			return ARGB32.color(argbSplit[0], argbSplit[1], argbSplit[2], argbSplit[3]);
		}
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class TrueFalseButton extends Button {

		public final int blitx;
		public final int blity;
		
		private final TrueFalseButtonSupplier supplier;
		
		public TrueFalseButton(int x, int y, int blitx, int blity, int width, int height, TrueFalseButtonSupplier supplier, OnPress onPress) {
			super(x, y, width, height, new TextComponent(""), onPress, new OnTooltip() {
				@Override
				public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
					Minecraft minecraft = Minecraft.getInstance();
					if(supplier.getTrueText() != null && supplier.getFalseText() != null && supplier.get()) {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(supplier.getTrueText())), mouseX, mouseY);
					}else {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(supplier.getFalseText())), mouseX, mouseY);
					}
					
				}
			});
			this.blitx = blitx;
			this.blity = blity;
			this.supplier = supplier;
			
		}
		
		public TrueFalseButton(int x, int y, int width, int height, String text, OnPress onPress) {
			super(x, y, width, height, new TextComponent(""), onPress, new OnTooltip() {
				@Override
				public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
					Minecraft minecraft = Minecraft.getInstance();
					if(text != null) {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(text)), mouseX, mouseY);
					}
					
				}
			});
			this.blitx = 0;
			this.blity = 0;
			this.supplier = null;
			
		}
		
		@Override
		public void renderButton(PoseStack mx, int mouseX, int mouseY, float partialTicks) {
			if(this.isHoveredOrFocused()) {
				this.renderToolTip(mx, mouseX, mouseY);
			}
			
		}
		
		public int[] getBlitData() {
			return new int[]{x, y, blitx, blity, width, height};
		}
		
		public boolean getSupplierOutput() {
			if(supplier == null) {
				return false;
			}
			return supplier.get();
		}
		
		public static class TrueFalseButtonSupplier {
			private final String trueText;
			private final String falseText;
			
			private final Supplier<Boolean> supplier;

			public TrueFalseButtonSupplier(String trueText, String falseText, Supplier<Boolean> supplier) {
				this.trueText = trueText;
				this.falseText = falseText;
				this.supplier = supplier;
			}
			
			public boolean get() {
				return supplier != null ? supplier.get() : false;
			}
			
			public String getTrueText() {
				return trueText;
			}
			
			public String getFalseText() {
				return falseText;
			}
			
			@Deprecated
			public String getTextFromSupplier() {
				if(get()) {
					return trueText;
				}else {
					return falseText;
				}
			}
		}
	}
}
