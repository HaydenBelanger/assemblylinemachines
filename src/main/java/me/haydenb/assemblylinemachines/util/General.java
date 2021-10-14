package me.haydenb.assemblylinemachines.util;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.IItemHandler;

public class General {

	public static final Random RAND = new Random();
	private static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> tillables = null;

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
	
	
	public static interface IPoweredTool{
		public int getMaxPower();
	}
	
	public static <H> H getCapabilityFromDirection(BlockEntity pte, String fieldName, Direction dir, Capability<H> capType) {
		BlockEntity te = pte.getLevel().getBlockEntity(pte.getBlockPos().relative(dir));
		
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
	
	@SuppressWarnings("unchecked")
	public static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> getTillableMap(){
		try {
			if(tillables == null) {
				Field map = HoeItem.class.getDeclaredField("TILLABLES");
				map.setAccessible(true);
				tillables = (Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>>) map.get(null);
			}
			
			return tillables;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void setFluidField(Block instance, FlowingFluid fluid) throws Exception {
		if(instance instanceof LiquidBlock) {
			LiquidBlock liquidBlock = (LiquidBlock) instance;
			liquidBlock.fluid = fluid;
		}else {
			throw new IllegalArgumentException("Block passed to LiquidBlock hack is not a LiquidBlock.");
		}
	}
	
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Font pFont, Component pText, int pX, int pY, int pColor) {
	      FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
	      pFont.draw(pPoseStack, formattedcharsequence, (float)(pX - pFont.width(formattedcharsequence) / 2), (float)pY, pColor);
	   }
}
