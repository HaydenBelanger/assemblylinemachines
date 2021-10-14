package me.haydenb.assemblylinemachines.block.pipe;

import com.google.common.base.Supplier;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.pipe.PipeBase.Type.MainType;
import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class PipeBase<T> extends Block implements EntityBlock {

	private static final VoxelShape SHAPE_BASE = Block.box(4, 4, 4, 12, 12, 12);
	private static final VoxelShape SHAPE_CARDINAL = Block.box(0, 4, 4, 4, 12, 12);
	private static final VoxelShape SHAPE_UP = Block.box(4, 12, 4, 12, 16, 12);
	private static final VoxelShape SHAPE_DOWN = Block.box(4, 0, 4, 12, 4, 12);

	private static final VoxelShape SHAPE_CONN_CARDINAL = Block.box(3, 3, 0, 13, 13, 4);
	private static final VoxelShape SHAPE_CONN_UP = Block.box(3, 12, 3, 13, 16, 13);
	private static final VoxelShape SHAPE_CONN_DOWN = Block.box(3, 0, 3, 13, 4, 13);

	private final Supplier<Capability<T>> cap;
	public final Type type;

	public PipeBase(Supplier<Capability<T>> cap, Type type) {
		super(Block.Properties.of(Material.METAL).strength(1f, 2f).sound(SoundType.METAL));
		this.cap = cap;
		this.type = type;
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
			if(worldIn.getBlockEntity(pos) instanceof ItemPipeConnectorTileEntity) {
				ItemPipeConnectorTileEntity tefm = (ItemPipeConnectorTileEntity) worldIn.getBlockEntity(pos);
				for (int i = 0; i < 9; i++) {
					tefm.setItem(i, ItemStack.EMPTY);
				}
				Containers.dropContents(worldIn, pos, tefm.getItems());
				worldIn.removeBlockEntity(pos);
				
			}else if(worldIn.getBlockEntity(pos) instanceof FluidPipeConnectorTileEntity || worldIn.getBlockEntity(pos) instanceof EnergyPipeConnectorTileEntity) {
				worldIn.removeBlockEntity(pos);
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if (!world.isClientSide) {
			if (handIn.equals(InteractionHand.MAIN_HAND)) {
				if(!(player.getMainHandItem().getItem() instanceof BlockItem) || !(((BlockItem) player.getMainHandItem().getItem()).getBlock() instanceof PipeBase<?>)) {
					for (Direction d : Direction.values()) {
						if (world.getBlockState(pos)
								.getValue(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {

							BlockEntity te = world.getBlockEntity(pos);
							if(te instanceof ItemPipeConnectorTileEntity) {
								try {
									NetworkHooks.openGui((ServerPlayer) player, (ItemPipeConnectorTileEntity) te, buf -> buf.writeBlockPos(pos));
								}catch(NullPointerException e) {}
								return InteractionResult.CONSUME;
							}else if(te instanceof FluidPipeConnectorTileEntity) {
								FluidPipeConnectorTileEntity fpcte = (FluidPipeConnectorTileEntity) te;
								fpcte.outputMode = !fpcte.outputMode;
								fpcte.sendUpdates();
								if(fpcte.outputMode) {
									player.displayClientMessage(new TextComponent("Set Connector to Output Mode."), true);
								}else {
									player.displayClientMessage(new TextComponent("Set Connector to Input Mode."), true);
								}
							}else if(te instanceof EnergyPipeConnectorTileEntity) {
								EnergyPipeConnectorTileEntity fpcte = (EnergyPipeConnectorTileEntity) te;
								fpcte.outputMode = !fpcte.outputMode;
								fpcte.sendUpdates();
								if(fpcte.outputMode) {
									player.displayClientMessage(new TextComponent("Set Connector to Output Mode."), true);
								}else {
									player.displayClientMessage(new TextComponent("Set Connector to Input Mode."), true);
								}
							}
						}
					}
				}
			}
		}
		return InteractionResult.PASS;
		
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		VoxelShape rt = SHAPE_BASE;
		for (Direction d : Direction.values()) {
			PipeConnOptions pco = state.getValue(PipeProperties.DIRECTION_BOOL.get(d));
			if (pco != PipeConnOptions.NONE) {
				switch (d) {
				case UP:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = Shapes.join(rt, SHAPE_CONN_UP, BooleanOp.OR);
					} else {
						rt = Shapes.join(rt, SHAPE_UP, BooleanOp.OR);
					}

					break;
				case DOWN:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = Shapes.join(rt, SHAPE_CONN_DOWN, BooleanOp.OR);
					} else {
						rt = Shapes.join(rt, SHAPE_DOWN, BooleanOp.OR);
					}

					break;
				default:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = Shapes.join(rt,
								General.rotateShape(Direction.NORTH, d, SHAPE_CONN_CARDINAL), BooleanOp.OR);
					} else {
						rt = Shapes.join(rt, General.rotateShape(Direction.WEST, d, SHAPE_CARDINAL),
								BooleanOp.OR);
					}

				}

			}
		}

		return rt;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState bs = this.defaultBlockState();
		for (Direction d : Direction.values()) {
			Block b = context.getLevel().getBlockState(context.getClickedPos().relative(d)).getBlock();
			if (b instanceof PipeBase) {
				PipeBase<?> pb = (PipeBase<?>) b;
				if (this.type == pb.type) {
					bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.PIPE);
				} else {
					bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
				}
			} else {
				BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos().relative(d));
				if (te != null) {
					if (te.getCapability(cap.get(), d.getOpposite()).orElse(null) != null) {
						if (context.getClickedFace().getOpposite() == d && context.getPlayer().isShiftKeyDown()) {
							bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.CONNECTOR);
						} else {
							bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
						}

					} else {
						bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
					}
				} else {
					bs = bs.setValue(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
				}
			}
		}

		return bs;
	}

	
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		Level world = (Level) worldIn;
		if (world.getBlockState(currentPos.relative(facing)).getBlock() instanceof PipeBase) {
			PipeBase<?> pb = (PipeBase<?>) world.getBlockState(currentPos.relative(facing)).getBlock();
			if (pb.type == this.type) {
				if(stateIn.getValue(PipeProperties.DIRECTION_BOOL.get(facing)) != PipeConnOptions.PIPE) {
					return stateIn.setValue(PipeProperties.DIRECTION_BOOL.get(facing), PipeConnOptions.PIPE);
				}else {
					return stateIn;
				}
			}
		}

		if(stateIn.getValue(PipeProperties.DIRECTION_BOOL.get(facing)) == PipeConnOptions.CONNECTOR) {
			
			BlockEntity te = world.getBlockEntity(currentPos.relative(facing));
			if (te != null) {
				if (te.getCapability(cap.get(), facing.getOpposite()).orElse(null) != null) {
					return stateIn;

				}
			}
		}
		
		if(stateIn.getValue(PipeProperties.DIRECTION_BOOL.get(facing)) != PipeConnOptions.NONE) {
			
			if(stateIn.getValue(PipeProperties.DIRECTION_BOOL.get(facing)) == PipeConnOptions.CONNECTOR && world.getBlockEntity(currentPos.relative(facing)) == null) {
				world.removeBlockEntity(currentPos);
			}
			
			return stateIn.setValue(PipeProperties.DIRECTION_BOOL.get(facing), PipeConnOptions.NONE);
		}else {
			return stateIn;
		}
	}
	
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if(!worldIn.isClientSide) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te != null && te instanceof ItemPipeConnectorTileEntity) {
				ItemPipeConnectorTileEntity ipcte = (ItemPipeConnectorTileEntity) te;
				if(ipcte.isRedstoneActive() && worldIn.hasNeighborSignal(pos)) {
					ipcte.setRedstoneActive(true);
					ipcte.sendUpdates();
				}else {
					ipcte.setRedstoneActive(false);
					ipcte.sendUpdates();
				}
			}
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		for(EnumProperty<PipeConnOptions> ep : PipeProperties.DIRECTION_BOOL.values()) {
			if(state.getValue(ep) == PipeConnOptions.CONNECTOR) {
				if(type.getMainType() == MainType.ITEM) {
					
					return Registry.getBlockEntity("pipe_connector_item").create(pos, state);
				}else if(type.getMainType() == MainType.FLUID) {
					return Registry.getBlockEntity("pipe_connector_fluid").create(pos, state);
				}else if(type.getMainType() == MainType.POWER){
					
					return Registry.getBlockEntity("pipe_connector_energy").create(pos, state);
				}else {
					throw new IllegalArgumentException("Invalid MainType found. No associated Tile Entity available.");
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("hiding")
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

	public static enum Type {
		BASIC_POWER(MainType.POWER), ADVANCED_POWER(MainType.POWER), BASIC_FLUID(MainType.FLUID), ADVANCED_FLUID(MainType.FLUID), ITEM(MainType.ITEM);
		
		
		private MainType category;
		Type(MainType category){
			this.category = category;
		}
		
		
		
		public static enum MainType{
			POWER, FLUID, ITEM;
		}
		
		public MainType getMainType() {
			return category;
		}
	}

}
