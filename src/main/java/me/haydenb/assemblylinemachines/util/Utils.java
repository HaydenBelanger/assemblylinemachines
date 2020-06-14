package me.haydenb.assemblylinemachines.util;

import java.util.Map.Entry;
import java.util.function.Supplier;
import java.text.DecimalFormat;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Utils {

	public static final BooleanProperty MACHINE_ACTIVE = BooleanProperty.create("active");
	public static final IntegerProperty BATTERY_PERCENT_STATE = IntegerProperty.create("fullness", 0, 4);

	public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[] { shape, VoxelShapes.empty() };

		int times = (to.getHorizontalIndex() - from.getHorizontalIndex() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1],
					VoxelShapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = VoxelShapes.empty();
		}

		return buffer[0];
	}

	public static class Pair<X, Y> {
		public X x;
		public Y y;

		public Pair(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}

	public static class Triplet<X, Y, Z> {
		public X x;
		public Y y;
		public Z z;

		public Triplet(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static <T extends TileEntity> T getTileEntity(final PlayerInventory pInv, final PacketBuffer data,
			Class<T> clazz) {
		Objects.requireNonNull(pInv, "This object cannot be null.");
		Objects.requireNonNull(data, "This object cannot be null.");

		TileEntity posEntity = pInv.player.world.getTileEntity(data.readBlockPos());

		return clazz.cast(posEntity);
	}

	public static void spawnItem(ItemStack stack, BlockPos pos, World world) {
		ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
		world.addEntity(ent);
	}

	public static void spawnItem(ItemStack stack, BlockPos pos, IWorld world) {
		ItemEntity ent = new ItemEntity(world.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
		world.addEntity(ent);
	}

	private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
	static {
		suffixes.put(1_000L, "K");
		suffixes.put(1_000_000L, "M");
		suffixes.put(1_000_000_000L, "G");
		suffixes.put(1_000_000_000_000L, "T");
		suffixes.put(1_000_000_000_000_000L, "P");
		suffixes.put(1_000_000_000_000_000_000L, "E");
	}

	public static String format(long value) {
		// Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE)
			return format(Long.MIN_VALUE + 1);
		if (value < 0)
			return "-" + format(-value);
		if (value < 1000)
			return Long.toString(value); // deal with easy case

		Entry<Long, String> e = suffixes.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); // the number part of the output times 10
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}

	public static class SupplierWrapper {

		private final String trueText;
		private final String falseText;
		public final Supplier<Boolean> supplier;

		public SupplierWrapper(String trueText, String falseText, Supplier<Boolean> supplier) {
			this.trueText = trueText;
			this.falseText = falseText;
			this.supplier = supplier;

		}

		public String getTextFromSupplier() {
			if (supplier.get()) {
				return trueText;
			} else {
				return falseText;
			}
		}

	}

	public static final DecimalFormat FORMAT = new DecimalFormat("###,###,###,###,###");

	public static final DecimalFormat FEPT_FORMAT = new DecimalFormat("###,##0.#");

	public static class SimpleButton extends Button {
		public final int blitx;
		public final int blity;
		
		public final int sizex;
		public final int sizey;

		public SimpleButton(int widthIn, int heightIn, int blitx, int blity, int sizex, int sizey, String text, IPressable onPress) {
			super(widthIn, heightIn, sizex, sizey, text, onPress);
			this.blitx = blitx;
			this.blity = blity;
			this.sizex = sizex;
			this.sizey = sizey;
		}

		public SimpleButton(int widthIn, int heightIn, String text, IPressable onPress) {
			this(widthIn, heightIn, 0, 0, text, onPress);
		}
		
		public SimpleButton(int widthIn, int heightIn, int blitx, int blity, String text, IPressable onPress) {
			this(widthIn, heightIn, blitx, blity, 8, 8, text, onPress);
		}

		@Override
		public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		}

	}
}
