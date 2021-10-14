package me.haydenb.assemblylinemachines.block.machines.mob;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.math.Vector3f;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class BlockVacuumHopper extends HopperBlock {

	private static final VoxelShape SHAPE_UD = Stream.of(Block.box(5, 0, 5, 11, 4, 11), Block.box(4, 4, 4, 12, 8, 12),
			Block.box(3, 8, 3, 13, 12, 13), Block.box(2, 10, 7, 3, 13, 9), Block.box(13, 10, 7, 14, 13, 9),
			Block.box(3, 12, 7, 4, 13, 9), Block.box(12, 12, 7, 13, 13, 9), Block.box(7, 10, 13, 9, 13, 14),
			Block.box(7, 12, 12, 9, 13, 13), Block.box(7, 10, 2, 9, 13, 3), Block.box(7, 12, 3, 9, 13, 4)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	private static final VoxelShape SHAPE_E = Stream.of(Block.box(12, 5, 5, 16, 11, 11), Block.box(8, 4, 4, 12, 12, 12),
			Block.box(4, 3, 3, 8, 13, 13), Block.box(3, 2, 7, 6, 3, 9), Block.box(3, 13, 7, 6, 14, 9), Block.box(3, 3, 7, 4, 4, 9),
			Block.box(3, 12, 7, 4, 13, 9), Block.box(3, 7, 13, 6, 9, 14), Block.box(3, 7, 12, 4, 9, 13),
			Block.box(3, 7, 2, 6, 9, 3), Block.box(3, 7, 3, 4, 9, 4)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.EAST, Direction.SOUTH, SHAPE_E);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.EAST, Direction.WEST, SHAPE_E);
	private static final VoxelShape SHAPE_N = General.rotateShape(Direction.EAST, Direction.NORTH, SHAPE_E);
	private BlockEntity entityForTicker = null;

	public BlockVacuumHopper() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
	}
	
	
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		entityForTicker = Registry.getBlockEntity("vacuum_hopper").create(pPos, pState);
		return entityForTicker;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		if(entityForTicker != null && entityForTicker instanceof BlockEntityTicker) {
			return (BlockEntityTicker<T>) entityForTicker;
		}
		return null;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction d = state.getValue(BlockStateProperties.FACING_HOPPER);
		if (d == Direction.WEST) {
			return SHAPE_W;
		} else if (d == Direction.SOUTH) {
			return SHAPE_S;
		} else if (d == Direction.EAST) {
			return SHAPE_E;
		} else if (d == Direction.NORTH) {
			return SHAPE_N;
		} else {
			return SHAPE_UD;
		}
	}

	public static class TEVacuumHopper extends HopperBlockEntity implements ALMTicker<TEVacuumHopper> {

		public TEVacuumHopper(BlockPos pWorldPosition, BlockState pBlockState) {
			super(pWorldPosition, pBlockState);
		}

		private int timer = 0;
		private int sTimer = 0;
		private AABB bb = null;

		@Override
		public void tick() {
			if (!level.isClientSide) {
				if (timer++ == 10) {
					timer = 0;

					if (bb == null) {
						bb = new AABB(this.getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING_HOPPER).getOpposite(), 2)).inflate(1);
					}

					List<ItemEntity> el = this.getLevel().getEntitiesOfClass(ItemEntity.class, bb);
					for (ItemEntity entity : el) {

						double x = entity.getX();
						double y = entity.getY();
						double z = entity.getZ();
						if (addItem(this, entity)) {
							this.setChanged();
							spawnTeleparticles(x, y, z, this.getLevel().getChunkAt(entity.blockPosition()));
							break;
						}

					}

				}
				if(sTimer++ == 4) {
					sTimer = 0;
					if (transferItemsOut(this)) {
						this.setChanged();
					}
				}
				
			}

		}

		
		//Below four methods copied from HopperBlockEntity.class as static
		private static boolean transferItemsOut(TEVacuumHopper tevh) {
			if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(tevh))
				return true;
			Container iinventory = getInventoryForHopperTransfer(tevh);
			if (iinventory == null) {
				return false;
			} else {
				Direction direction = tevh.getBlockState().getValue(HopperBlock.FACING).getOpposite();
				if (TEVacuumHopper.isInventoryFull(iinventory, direction)) {
					return false;
				} else {
					for (int i = 0; i < tevh.getContainerSize(); ++i) {
						if (!tevh.getItem(i).isEmpty()) {
							ItemStack itemstack = tevh.getItem(i).copy();
							ItemStack itemstack1 = TEVacuumHopper.addItem(tevh, iinventory, tevh.removeItem(i, 1), direction);
							if (itemstack1.isEmpty()) {
								iinventory.setChanged();
								return true;
							}

							tevh.setItem(i, itemstack);
						}
					}

					return false;
				}
			}
		}

		private static Container getInventoryForHopperTransfer(TEVacuumHopper tevh) {
			Direction direction = tevh.getBlockState().getValue(HopperBlock.FACING);
			return HopperBlockEntity.getContainerAt(tevh.getLevel(), tevh.getBlockPos().relative(direction));
		}

		private static boolean isInventoryFull(Container inventoryIn, Direction side) {
			return iterateSlotsForCompat(inventoryIn, side).allMatch((p_213970_1_) -> {
				ItemStack itemstack = inventoryIn.getItem(p_213970_1_);
				return itemstack.getCount() >= itemstack.getMaxStackSize();
			});
		}
		
		private static IntStream iterateSlotsForCompat(Container p_213972_0_, Direction p_213972_1_) {
			return p_213972_0_ instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer) p_213972_0_).getSlotsForFace(p_213972_1_))
					: IntStream.range(0, p_213972_0_.getContainerSize());
		}

		@Override
		public BlockEntityType<?> getType() {
			return Registry.getBlockEntity("vacuum_hopper");
		}

		@Override
		public Component getName() {
			return new TranslatableComponent(Registry.getBlock("vacuum_hopper").getDescriptionId());
		}

		private static void spawnTeleparticles(double x, double y, double z, LevelChunk ch) {
			PacketData pd = new PacketData("vacuum_hopper_particles");
			pd.writeDouble("x", x);
			pd.writeDouble("y", y);
			pd.writeDouble("z", z);
			HashPacketImpl.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), pd);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void spawnTeleparticles(PacketData pd) {
		Minecraft mc = Minecraft.getInstance();
		for (int i = 0; i < 4; i++) {
			mc.player.getCommandSenderWorld().addParticle(new DustParticleOptions(new Vector3f(0.8f, 0.223f, 0.792f), 1f), true, pd.get("x", Double.class), pd.get("y", Double.class) + 0.1, pd.get("z", Double.class), 0, 0, 0);
		}
	}

}
