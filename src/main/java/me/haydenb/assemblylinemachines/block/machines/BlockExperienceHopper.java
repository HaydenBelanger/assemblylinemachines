package me.haydenb.assemblylinemachines.block.machines;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.math.Vector3f;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.network.PacketDistributor;

public class BlockExperienceHopper extends BlockTileEntity {

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

	public BlockExperienceHopper() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "experience_hopper");
		this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING_HOPPER, Direction.DOWN));
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
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING_HOPPER);
	}

	@Override
	public BlockEntity bteExtendBlockEntity(BlockPos pPos, BlockState pState) {
		return bteDefaultReturnBlockEntity(pPos, pState);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> bteExtendTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return bteDefaultReturnTicker(level, state, blockEntityType);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction = context.getClickedFace().getOpposite();
		return this.defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction);
	}

	@Override
	public InteractionResult blockRightClickClient(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		return InteractionResult.SUCCESS;
	}
	
	public static class TEExperienceHopper extends BasicTileEntity implements ALMTicker<TEExperienceHopper>{
		
		public IFluidHandler output;
		private int internalStoredXp;
		private int timer = 0;
		private int subTimer = 0;
		private AABB bb;
		
		public TEExperienceHopper(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}

		public TEExperienceHopper(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("experience_hopper"), pos, state);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			internalStoredXp = compound.getInt("assemblylinemachines:internalxp");
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {

			compound.putInt("assemblylinemachines:internalxp", internalStoredXp);
			super.saveAdditional(compound);
		}
		
		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == 40) {
					boolean sendUpdates = false;
					timer = 0;
					
					if(output == null) {
						Direction dir = this.getBlockState().getValue(BlockStateProperties.FACING_HOPPER);
						BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(dir));
						if(te != null) {
							LazyOptional<IFluidHandler> cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
							output = cap.orElse(null);
							if(output != null) {
								cap.addListener((h) -> output = null);
							}
						}
					}
					if(output != null) {
						if(internalStoredXp != 0 && subTimer++ == 5) {
							subTimer = 0;
							
							int max = internalStoredXp;
							if(max > 500) {
								max = 500;
							}
							
							internalStoredXp -= output.fill(new FluidStack(Registry.getFluid("liquid_experience"), max), FluidAction.EXECUTE);
							sendUpdates = true;
							
						}
					}
					
					if(internalStoredXp <= 1000) {
						if (bb == null) {
							bb = new AABB(this.getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING_HOPPER).getOpposite(), 3)).inflate(2);
						}
						
						List<ExperienceOrb> el = this.getLevel().getEntitiesOfClass(ExperienceOrb.class, bb);
						
						for(ExperienceOrb entity : el) {
							internalStoredXp += (entity.getValue() * 15);
							spawnTeleparticles(entity.getX(), entity.getY(), entity.getZ(), this.getLevel().getChunkAt(entity.blockPosition()));
							sendUpdates = true;
							entity.setRemoved(RemovalReason.DISCARDED);
							if(internalStoredXp > 1000) {
								break;
							}
						}
					}
					if(sendUpdates) {
						sendUpdates();
					}
				}
			}
			
		}
		
		private static void spawnTeleparticles(double x, double y, double z, LevelChunk ch) {
			PacketData pd = new PacketData("experience_hopper_particles");
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
			mc.player.getCommandSenderWorld().addParticle(new DustParticleOptions(new Vector3f(0.643f, 0.960f, 0.258f), 1f), true, pd.get("x", Double.class), pd.get("y", Double.class) + 0.1, pd.get("z", Double.class), 0, 0, 0);
		}
	}

}
