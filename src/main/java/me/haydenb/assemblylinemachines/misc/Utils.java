package me.haydenb.assemblylinemachines.misc;

import java.util.Objects;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Utils {

	public static final BooleanProperty MACHINE_ACTIVE = BooleanProperty.create("active");
	
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
	
	public static class Pair<X, Y>{
		public X x;
		public Y y;
		public Pair(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public static class Triplet<X, Y, Z>{
		public X x;
		public Y y;
		public Z z;
		public Triplet(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static <T extends TileEntity> T getTileEntity(final PlayerInventory pInv, final PacketBuffer data, Class<T> clazz) {
		Objects.requireNonNull(pInv, "This object cannot be null.");
		Objects.requireNonNull(data, "This object cannot be null.");
		
		TileEntity posEntity = pInv.player.world.getTileEntity(data.readBlockPos());
		
		
		
		return clazz.cast(posEntity);
	}
	
	public static void spawnItem(ItemStack stack, BlockPos pos, World world) {
		ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, stack);
		world.addEntity(ent);
	}
	
	public static void spawnItem(ItemStack stack, BlockPos pos, IWorld world) {
		ItemEntity ent = new ItemEntity(world.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, stack);
		world.addEntity(ent);
	}
}
