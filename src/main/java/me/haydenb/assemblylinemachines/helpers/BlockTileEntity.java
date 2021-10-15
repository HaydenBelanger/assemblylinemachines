package me.haydenb.assemblylinemachines.helpers;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class BlockTileEntity extends Block{

	private final String teName;
	private final VoxelShape shape;
	private final boolean shouldRotate;
	private final Direction dir;
	
	private static final VoxelShape NO_SHAPE_CUBE = VoxelShapes.fullCube();
	public BlockTileEntity(Properties properties, String teName) {
		super(properties);
		this.teName = teName;
		this.shape = null;
		this.shouldRotate = false;
		this.dir = null;
	}
	
	public BlockTileEntity(Properties properties, String teName, VoxelShape shape, boolean shouldRotate, Direction defaultDirection) {
		super(properties);
		this.teName = teName;
		this.shape = shape;
		this.shouldRotate = shouldRotate;
		this.dir = defaultDirection;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.getTileEntity(teName).create();
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	public abstract ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player);
	
	public abstract ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player);
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand hand, BlockRayTraceResult hit) {
		
		if(hand.equals(Hand.MAIN_HAND)) {
			if(!worldIn.isRemote) {
				return blockRightClickServer(state, worldIn, pos, player);
			}else {
				return blockRightClickClient(state, worldIn, pos, player);
			}
		}
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if(shape != null) {
			if(shouldRotate == true && state.hasProperty(HorizontalBlock.HORIZONTAL_FACING)) {
				return General.rotateShape(dir, state.get(HorizontalBlock.HORIZONTAL_FACING), shape);
			}else {
				return shape;
			}
		}else {
			return NO_SHAPE_CUBE;
		}
	}
	
	public static class BlockScreenTileEntity<T extends AbstractMachine<?>> extends BlockTileEntity{

		private final Class<? extends T> clazz;
		public BlockScreenTileEntity(Properties properties, String teName, VoxelShape shape, boolean shouldRotate,
				Direction defaultDirection, Class<? extends T> clazz) {
			super(properties, teName, shape, shouldRotate, defaultDirection);
			this.clazz = clazz;
		}
		
		public BlockScreenTileEntity(Properties properties, String teName, Class<T> clazz) {
			super(properties, teName);
			this.clazz = clazz;
		}

		@Override
		public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos,
				PlayerEntity player) {
			TileEntity te = world.getTileEntity(pos);
			if(clazz.isInstance(te)) {
				
				try {
					NetworkHooks.openGui((ServerPlayerEntity) player, clazz.cast(te), buf -> buf.writeBlockPos(pos));
				}catch(NullPointerException e) {}
				
			}
			return ActionResultType.CONSUME;
			
		}

		@Override
		public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos,
				PlayerEntity player) {
			return ActionResultType.CONSUME;
		}
		
		@Override
		public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
			if(state.getBlock() != newState.getBlock()) {
				TileEntity te = worldIn.getTileEntity(pos);
				if(clazz.isInstance(te)) {
					T v = clazz.cast(te);
					InventoryHelper.dropItems(worldIn, pos, v.getItems());
					worldIn.removeTileEntity(pos);
				}
			}
		}
		
	}
}
