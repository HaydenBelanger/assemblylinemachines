package me.haydenb.assemblylinemachines.block.helpers;

import java.util.function.Consumer;

import com.google.common.cache.*;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;

public abstract class BlockTileEntity extends Block implements EntityBlock{

	private final LoadingCache<Direction, VoxelShape> shapes;
	private final VoxelShape defaultShape;
	private final boolean shouldRotate;
	private final Direction defaultDirection;
	
	private final String teName;

	private static final VoxelShape NO_SHAPE_CUBE = Shapes.block();

	public BlockTileEntity(Properties properties, String teName, VoxelShape shape, boolean shouldRotate, Direction defaultDirection) {
		super(properties);
		this.teName = teName;
		this.defaultShape = shape;
		this.defaultDirection = defaultDirection;
		this.shouldRotate = shouldRotate;

		this.shapes = shape != null && shouldRotate && defaultDirection != null ? CacheBuilder.newBuilder().build(CacheLoader.from((dir) -> Utils.rotateShape(defaultDirection, dir, defaultShape))) : null;
	}

	public BlockTileEntity(Properties properties, String teName, VoxelShape shape) {
		this(properties, teName, shape, true, Direction.NORTH);
	}

	public BlockTileEntity(Properties properties, String teName) {
		this(properties, teName, null, false, null);
	}





	//GROUPED - return for newBlockEntity
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return bteExtendBlockEntity(pPos, pState);
	}

	public abstract BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState);

	public BlockEntity bteDefaultReturnBlockEntity(BlockPos pPos, BlockState pState) {
		return Registry.getBlockEntity(teName).create(pPos, pState);
	}

	//GROUPED - return for getTicker
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return bteExtendTicker(level, state, blockEntityType);
	}

	public abstract <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType);

	public <T extends BlockEntity> BlockEntityTicker<T> bteDefaultReturnTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType){
		return new BlockEntityTicker<>() {

			@SuppressWarnings("unchecked")
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



	public abstract InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player);

	public abstract InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player);
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {

		if(hand.equals(InteractionHand.MAIN_HAND)) {
			if(!worldIn.isClientSide) {
				return blockRightClickServer(state, worldIn, pos, player);
			}else {
				return blockRightClickClient(state, worldIn, pos, player);
			}
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return defaultShape != null ? shouldRotate && defaultDirection != null && state.hasProperty(HorizontalDirectionalBlock.FACING) ? shapes.getUnchecked(state.getValue(HorizontalDirectionalBlock.FACING)) : defaultShape : NO_SHAPE_CUBE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if(state.hasProperty(HorizontalDirectionalBlock.FACING)) state = state.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
		return state;
	}

	public static class BlockScreenBlockEntity<T extends AbstractMachine<?>> extends BlockTileEntity{

		private final Class<? extends T> clazz;
		public BlockScreenBlockEntity(Properties properties, String teName, VoxelShape shape, boolean shouldRotate,
				Direction defaultDirection, Class<? extends T> clazz) {
			super(properties, teName, shape, shouldRotate, defaultDirection);
			this.clazz = clazz;
		}

		public BlockScreenBlockEntity(Properties properties, String teName, Class<T> clazz) {
			super(properties, teName);
			this.clazz = clazz;
		}

		@Override
		public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos,
				Player player) {
			BlockEntity te = world.getBlockEntity(pos);
			if(clazz.isInstance(te)) {

				try {
					NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) clazz.cast(te), new Consumer<FriendlyByteBuf>() {

						@Override
						public void accept(FriendlyByteBuf t) {
							t.writeBlockPos(pos);

						}
					});
				}catch(NullPointerException e) {}

			}
			return InteractionResult.CONSUME;

		}

		@Override
		public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos,
				Player player) {
			return InteractionResult.CONSUME;
		}

		@Override
		public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
			if(state.getBlock() != newState.getBlock()) {
				BlockEntity te = worldIn.getBlockEntity(pos);
				if(clazz.isInstance(te)) {
					T v = clazz.cast(te);
					Containers.dropContents(worldIn, pos, v.getItems());
					worldIn.removeBlockEntity(pos);
				}
			}
		}

		@Override
		public BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState) {
			return bteDefaultReturnBlockEntity(pPos, pState);
		}

		@SuppressWarnings("hiding")
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
			return bteDefaultReturnTicker(level, state, blockEntityType);
		}

	}
}
