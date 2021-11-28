package me.haydenb.assemblylinemachines.block.pipe;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.*;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class BlockPipe extends Block implements EntityBlock {

	private static final ArrayListMultimap<String, VoxelShape> SHAPE_MAP = ArrayListMultimap.create();
	static {
		SHAPE_MAP.put("BASE", Block.box(4, 4, 4, 12, 12, 12));
		SHAPE_MAP.putAll("UP", List.of(Block.box(4, 12, 4, 12, 16, 12), Block.box(3, 12, 3, 13, 16, 13)));
		SHAPE_MAP.putAll("DOWN", List.of(Block.box(4, 0, 4, 12, 4, 12), Block.box(3, 0, 3, 13, 4, 13)));
		SHAPE_MAP.putAll("CARDINAL", List.of(Block.box(0, 4, 4, 4, 12, 12), Block.box(3, 3, 0, 13, 13, 4)));
	}
	
	final TransmissionType transType;
	final PipeType pipeType;
	
	public BlockPipe(TransmissionType transType, PipeType pipeType) {
		super(Block.Properties.of(Material.METAL).strength(1f, 2f).sound(SoundType.METAL));
		this.transType = transType;
		this.pipeType = pipeType;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		for (EnumProperty<PipeConnOptions> b : PipeProperties.DIRECTION_BOOL.values()) {
			builder.add(b);
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getBlockEntity(pos) instanceof PipeConnectorTileEntity) {
				PipeConnectorTileEntity tefm = (PipeConnectorTileEntity) worldIn.getBlockEntity(pos);
				for (int i = 0; i < 9; i++) {
					tefm.setItem(i, ItemStack.EMPTY);
				}
				Containers.dropContents(worldIn, pos, tefm.getItems());
				worldIn.removeBlockEntity(pos);
				
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand handIn, BlockHitResult hit) {
		if (!world.isClientSide && handIn.equals(InteractionHand.MAIN_HAND)) {
			if(!(player.getMainHandItem().getItem() instanceof BlockItem) || !(((BlockItem) player.getMainHandItem().getItem()).getBlock() instanceof BlockPipe)) {
				for (Direction d : Direction.values()) {
					if (world.getBlockState(pos).getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
						try {
							NetworkHooks.openGui((ServerPlayer) player, (PipeConnectorTileEntity) world.getBlockEntity(pos), buf -> buf.writeBlockPos(pos));
						}catch(NullPointerException e) {}
						return InteractionResult.CONSUME;
					}	
				}
			}
		}
		return InteractionResult.PASS;
		
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		VoxelShape rt = SHAPE_MAP.get("BASE").get(0);
		for (Direction d : Direction.values()) {
			int vx = -1;
			switch(state.getValue(PipeProperties.DIRECTION_BOOL.get(d))) {
			case CONNECTOR:
				vx = 1;
				break;
			case PIPE:
				vx = 0;
				break;
			default:
				break;
			}
			if(vx != -1) {
				VoxelShape combineShape;
				switch(d) {
				case DOWN:
				case UP:
					combineShape = SHAPE_MAP.get(d.toString().toUpperCase()).get(vx);
					break;
				default:
					Direction rtd = vx == 1 ? Direction.NORTH : Direction.WEST;
					combineShape = Utils.rotateShape(rtd, d, SHAPE_MAP.get("CARDINAL").get(vx));
					break;
				}
				rt = Shapes.join(rt, combineShape, BooleanOp.OR);	
			}
		}
		return rt;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.updateConnections(this.defaultBlockState(), Direction.values(), context.getLevel(), context.getClickedPos(), context.getPlayer(), context.getClickedFace());
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		return this.updateConnections(stateIn, new Direction[]{facing}, (Level) worldIn, currentPos, null, null);
	}
	
	private BlockState updateConnections(BlockState state, Direction[] toUpdate, Level world, BlockPos currentPos, Player playerForConnector, Direction clickedFace) {
		for(Direction d : toUpdate) {
			PipeConnOptions pco;
			boolean set = true;
			if(world.getBlockState(currentPos.relative(d)).getBlock() instanceof BlockPipe) {
				BlockPipe bp = (BlockPipe) world.getBlockState(currentPos.relative(d)).getBlock();
				pco = (bp.transType == this.transType && bp.pipeType == this.pipeType) ? PipeConnOptions.PIPE : PipeConnOptions.NONE;
			}else {
				
				pco = this.transType.hasCapability(d.getOpposite(), world.getBlockEntity(currentPos.relative(d))) && playerForConnector != null && clickedFace != null && clickedFace == d.getOpposite() ? PipeConnOptions.CONNECTOR : PipeConnOptions.NONE;
				if(pco == PipeConnOptions.CONNECTOR && playerForConnector != null && !playerForConnector.isShiftKeyDown()) pco = PipeConnOptions.NONE;
				if(state.getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {
					set = false;
					if(pco == PipeConnOptions.NONE && playerForConnector == null && world.getBlockEntity(currentPos.relative(d)) == null) set = true;
					if(set == true) world.removeBlockEntity(currentPos);
				}
			}
			if(set) state = state.setValue(PipeProperties.DIRECTION_BOOL.get(d), pco);
		}
		return state;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		for(EnumProperty<PipeConnOptions> ep : PipeProperties.DIRECTION_BOOL.values()) {
			if(state.getValue(ep) == PipeConnOptions.CONNECTOR) {
				return Registry.getBlockEntity("pipe_connector").create(pos, state);
			}
		}
		return null;
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return new BlockEntityTicker<T>() {

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

}
