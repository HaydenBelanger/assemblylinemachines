package me.haydenb.assemblylinemachines.block.pipe;

import java.util.ArrayList;
import java.util.TreeMap;

import com.google.common.base.Supplier;

import me.haydenb.assemblylinemachines.block.pipe.PipeProperties.PipeConnOptions;
import me.haydenb.assemblylinemachines.misc.Utils;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.network.NetworkHooks;

public class PipeBase<T> extends Block {

	private static final VoxelShape SHAPE_BASE = Block.makeCuboidShape(4, 4, 4, 12, 12, 12);
	private static final VoxelShape SHAPE_CARDINAL = Block.makeCuboidShape(0, 4, 4, 4, 12, 12);
	private static final VoxelShape SHAPE_UP = Block.makeCuboidShape(4, 12, 4, 12, 16, 12);
	private static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(4, 0, 4, 12, 4, 12);

	private static final VoxelShape SHAPE_CONN_CARDINAL = Block.makeCuboidShape(3, 3, 0, 13, 13, 4);
	private static final VoxelShape SHAPE_CONN_UP = Block.makeCuboidShape(3, 12, 3, 13, 16, 13);
	private static final VoxelShape SHAPE_CONN_DOWN = Block.makeCuboidShape(3, 0, 3, 13, 4, 13);

	private final Supplier<Capability<T>> cap;
	private final Type type;

	public PipeBase(Supplier<Capability<T>> cap, Type type) {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1f, 2f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		this.cap = cap;
		this.type = type;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		for (EnumProperty<PipeConnOptions> b : PipeProperties.DIRECTION_BOOL.values()) {
			builder.add(b);
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof IPipeConnector) {
				worldIn.removeTileEntity(pos);
			}
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (!world.isRemote) {
			if (handIn == Hand.MAIN_HAND) {
				for (Direction d : Direction.values()) {
					if (world.getBlockState(pos)
							.get(PipeProperties.DIRECTION_BOOL.get(d)) == PipeConnOptions.CONNECTOR) {

						TileEntity te = world.getTileEntity(pos);
						if(te != null && te instanceof ItemPipeConnectorTileEntity) {
							NetworkHooks.openGui((ServerPlayerEntity) player, (ItemPipeConnectorTileEntity) te, buf -> buf.writeBlockPos(pos));
						}
					}
				}
			}
		}
		return ActionResultType.PASS;
		
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape rt = SHAPE_BASE;
		for (Direction d : Direction.values()) {
			PipeConnOptions pco = state.get(PipeProperties.DIRECTION_BOOL.get(d));
			if (pco != PipeConnOptions.NONE) {
				switch (d) {
				case UP:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = VoxelShapes.combineAndSimplify(rt, SHAPE_CONN_UP, IBooleanFunction.OR);
					} else {
						rt = VoxelShapes.combineAndSimplify(rt, SHAPE_UP, IBooleanFunction.OR);
					}

					break;
				case DOWN:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = VoxelShapes.combineAndSimplify(rt, SHAPE_CONN_DOWN, IBooleanFunction.OR);
					} else {
						rt = VoxelShapes.combineAndSimplify(rt, SHAPE_DOWN, IBooleanFunction.OR);
					}

					break;
				default:
					if (pco == PipeConnOptions.CONNECTOR) {
						rt = VoxelShapes.combineAndSimplify(rt,
								Utils.rotateShape(Direction.NORTH, d, SHAPE_CONN_CARDINAL), IBooleanFunction.OR);
					} else {
						rt = VoxelShapes.combineAndSimplify(rt, Utils.rotateShape(Direction.WEST, d, SHAPE_CARDINAL),
								IBooleanFunction.OR);
					}

				}

			}
		}

		return rt;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState bs = this.getDefaultState();
		for (Direction d : Direction.values()) {
			Block b = context.getWorld().getBlockState(context.getPos().offset(d)).getBlock();
			if (b instanceof PipeBase) {
				PipeBase<?> pb = (PipeBase<?>) b;
				if (this.type == pb.type) {
					bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.PIPE);
				} else {
					bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
				}
			} else {
				TileEntity te = context.getWorld().getTileEntity(context.getPos().offset(d));
				if (te != null) {
					if (te.getCapability(cap.get(), d.getOpposite()).orElse(null) != null) {
						if (context.getFace().getOpposite() == d && context.getPlayer().isSneaking()) {
							bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.CONNECTOR);
						} else {
							bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
						}

					} else {
						bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
					}
				} else {
					bs = bs.with(PipeProperties.DIRECTION_BOOL.get(d), PipeConnOptions.NONE);
				}
			}
		}

		return bs;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		World world = worldIn.getWorld();
		if (world.getBlockState(currentPos.offset(facing)).getBlock() instanceof PipeBase) {
			PipeBase<?> pb = (PipeBase<?>) world.getBlockState(currentPos.offset(facing)).getBlock();
			if (pb.type == this.type) {
				if(stateIn.get(PipeProperties.DIRECTION_BOOL.get(facing)) != PipeConnOptions.PIPE) {
					if(!world.isRemote) {
						updateAllAlongPath(world, currentPos, new ArrayList<>(), new ArrayList<>());
					}
					return stateIn.with(PipeProperties.DIRECTION_BOOL.get(facing), PipeConnOptions.PIPE);
				}else {
					return stateIn;
				}
			}
		}

		if(stateIn.get(PipeProperties.DIRECTION_BOOL.get(facing)) != PipeConnOptions.NONE) {
			if(!world.isRemote) {
				updateAllAlongPath(world, currentPos, new ArrayList<>(), new ArrayList<>());
			}
			if(world.getTileEntity(currentPos) != null) {
				world.removeTileEntity(currentPos);
			}
			
			return stateIn.with(PipeProperties.DIRECTION_BOOL.get(facing), PipeConnOptions.NONE);
		}else {
			return stateIn;
		}
	}

	public <M> void pathToNearest(World world, BlockPos curPos, ArrayList<BlockPos> checked, BlockPos initial, int distance, TreeMap<Target, IPipeConnector> foundTileEntities) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.get(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !initial.equals(curPos)) {
				System.out.println(initial + " = " + curPos);
				TileEntity te = world.getTileEntity(curPos);
				if(te != null && te instanceof IPipeConnector) {
					IPipeConnector ipc = (IPipeConnector) te;
					foundTileEntities.put(new Target(distance, ipc.getPriority()), ipc);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.offset(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof PipeBase) {
						PipeBase<?> t = (PipeBase<?>) world.getBlockState(targPos).getBlock();
						if (t.type == this.type) {
							distance++;
							t.pathToNearest(world, targPos, checked, initial, distance, foundTileEntities);
						}

					}
				}

			}
		}
	}
	
	public void updateAllAlongPath(World world, BlockPos curPos, ArrayList<BlockPos> checked, ArrayList<BlockPos> updated) {
		BlockState bs = world.getBlockState(curPos);
		for (Direction k : Direction.values()) {
			PipeConnOptions pco = bs.get(PipeProperties.DIRECTION_BOOL.get(k));
			if(pco == PipeConnOptions.CONNECTOR && !updated.contains(curPos)) {
				updated.add(curPos);
				TileEntity te = world.getTileEntity(curPos);
				if(te != null && te instanceof IPipeConnector) {
					((IPipeConnector) te).updateTargets(this);
				}
				
			}else if (pco == PipeConnOptions.PIPE) {
				BlockPos targPos = curPos.offset(k);
				if (!checked.contains(targPos)) {
					checked.add(targPos);
					if (world.getBlockState(targPos).getBlock() instanceof PipeBase) {
						PipeBase<?> t = (PipeBase<?>) world.getBlockState(targPos).getBlock();
						if (t.type == this.type) {
							t.updateAllAlongPath(world, targPos, checked, updated);
						}

					}
				}

			}
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		for(EnumProperty<PipeConnOptions> ep : PipeProperties.DIRECTION_BOOL.values()) {
			if(state.get(ep) == PipeConnOptions.CONNECTOR) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if(type == Type.ITEM) {
			return Registry.getTileEntity("pipe_connector_item").create();
		}else if(type == Type.FLUID) {
			return Registry.getTileEntity("pipe_connector_fluid").create();
		}else {
			return Registry.getTileEntity("pipe_connector_energy").create();
		}
		
	}

	public static enum Type {
		POWER, FLUID, ITEM;
	}
	
	public static class Target{
		
		public final int distance;
		public final Integer priority;
		
		Target(int distance, int priority) {
			this.distance = distance;
			this.priority = priority;
		}
	}

}
