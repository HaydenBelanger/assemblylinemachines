package me.haydenb.assemblylinemachines.block.automation;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import com.mojang.math.Vector3f;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;

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

	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.EAST, Direction.SOUTH, SHAPE_E);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.EAST, Direction.WEST, SHAPE_E);
	private static final VoxelShape SHAPE_N = Utils.rotateShape(Direction.EAST, Direction.NORTH, SHAPE_E);

	public BlockVacuumHopper() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
	}
	
	
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return Registry.getBlockEntity("vacuum_hopper").create(pPos, pState);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return new BlockEntityTicker<T>() {

			@Override
			public void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
				if(blockEntity instanceof ALMTicker) {
					((ALMTicker<?>) blockEntity).tick();
				}else if(blockEntity instanceof BlockEntityTicker) {
					((BlockEntityTicker<T>) blockEntity).tick(level, pos, state, blockEntity);
				}
				
			}
		};
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
		
		private IItemHandler handler = null;

		@Override
		public void tick() {
			if (!level.isClientSide) {
				if (timer++ == 10) {
					timer = 0;

					if (bb == null) {
						bb = new AABB(this.getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING_HOPPER).getOpposite(), 3)).inflate(2);
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
					if(handler == null) {
						Direction d = this.getBlockState().getValue(HopperBlock.FACING);
						if(this.getLevel().getBlockEntity(this.getBlockPos().relative(d)) != null) {
							LazyOptional<IItemHandler> lO = this.getLevel().getBlockEntity(this.getBlockPos().relative(d)).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
							if(lO.orElse(null) != null) {
								handler = lO.orElse(null);
								lO.addListener((lOX) -> handler = null);
							}
						}
						
					}
					if(handler != null) {
						ListIterator<ItemStack> iter = this.getItems().listIterator();
						
						while(iter.hasNext()) {
							ItemStack item = iter.next();
							for(int i = 0; i < handler.getSlots(); i++) {
								if(item.isEmpty()) {
									break;
								}
								item = handler.insertItem(i, item, false);
							}
							iter.set(item);
						}
						
					}
				}
				
			}

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
			PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), pd);
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
