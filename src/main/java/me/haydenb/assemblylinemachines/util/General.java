package me.haydenb.assemblylinemachines.util;

import java.util.Objects;
import java.util.Random;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.IItemHandler;

public class General {

	public static final Random RAND = new Random();

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
	
	
	public static interface IPoweredTool{
		public int getMaxPower();
	}
	
	public static <H> H getCapabilityFromDirection(TileEntity pte, String fieldName, Direction dir, Capability<H> capType) {
		TileEntity te = pte.getWorld().getTileEntity(pte.getPos().offset(dir));
		
		if(te != null) {
			LazyOptional<H> cap = te.getCapability(capType, dir.getOpposite());
			H output = cap.orElse(null);
			if(output != null) {
				cap.addListener(new NonNullConsumer<LazyOptional<H>>() {
					@Override
					public void accept(LazyOptional<H> t) {
						try {
							if(pte != null) {
								pte.getClass().getField(fieldName).set(pte, null);
							}
						} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
							e.printStackTrace();
						}
					}
				});
				
				return output;
			}
		}
		
		return null;
	}
}
