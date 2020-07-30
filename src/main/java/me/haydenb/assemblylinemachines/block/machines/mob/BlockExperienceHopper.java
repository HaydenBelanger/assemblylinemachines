package me.haydenb.assemblylinemachines.block.machines.mob;

import java.util.List;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.network.PacketDistributor;

public class BlockExperienceHopper extends BlockTileEntity {

	private static final VoxelShape SHAPE_UD = Stream.of(Block.makeCuboidShape(5, 0, 5, 11, 4, 11), Block.makeCuboidShape(4, 4, 4, 12, 8, 12),
			Block.makeCuboidShape(3, 8, 3, 13, 12, 13), Block.makeCuboidShape(2, 10, 7, 3, 13, 9), Block.makeCuboidShape(13, 10, 7, 14, 13, 9),
			Block.makeCuboidShape(3, 12, 7, 4, 13, 9), Block.makeCuboidShape(12, 12, 7, 13, 13, 9), Block.makeCuboidShape(7, 10, 13, 9, 13, 14),
			Block.makeCuboidShape(7, 12, 12, 9, 13, 13), Block.makeCuboidShape(7, 10, 2, 9, 13, 3), Block.makeCuboidShape(7, 12, 3, 9, 13, 4)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_E = Stream.of(Block.makeCuboidShape(12, 5, 5, 16, 11, 11), Block.makeCuboidShape(8, 4, 4, 12, 12, 12),
			Block.makeCuboidShape(4, 3, 3, 8, 13, 13), Block.makeCuboidShape(3, 2, 7, 6, 3, 9), Block.makeCuboidShape(3, 13, 7, 6, 14, 9), Block.makeCuboidShape(3, 3, 7, 4, 4, 9),
			Block.makeCuboidShape(3, 12, 7, 4, 13, 9), Block.makeCuboidShape(3, 7, 13, 6, 9, 14), Block.makeCuboidShape(3, 7, 12, 4, 9, 13),
			Block.makeCuboidShape(3, 7, 2, 6, 9, 3), Block.makeCuboidShape(3, 7, 3, 4, 9, 4)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.EAST, Direction.SOUTH, SHAPE_E);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.EAST, Direction.WEST, SHAPE_E);
	private static final VoxelShape SHAPE_N = General.rotateShape(Direction.EAST, Direction.NORTH, SHAPE_E);

	public BlockExperienceHopper() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "experience_hopper");
		this.setDefaultState(this.stateContainer.getBaseState().with(BlockStateProperties.FACING_EXCEPT_UP, Direction.DOWN));
	}
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction d = state.get(BlockStateProperties.FACING_EXCEPT_UP);
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
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING_EXCEPT_UP);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction direction = context.getFace().getOpposite();
		return this.getDefaultState().with(BlockStateProperties.FACING_EXCEPT_UP, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction);
	}

	@Override
	public ActionResultType blockRightClickClient(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		return ActionResultType.SUCCESS;
	}
	
	public static class TEExperienceHopper extends BasicTileEntity implements ITickableTileEntity{
		
		private IFluidHandler output;
		private int internalStoredXp;
		private int timer = 0;
		private int subTimer = 0;
		private AxisAlignedBB bb;
		
		public TEExperienceHopper(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		public TEExperienceHopper() {
			this(Registry.getTileEntity("experience_hopper"));
		}
		
		@Override
		public void func_230337_a_(BlockState p_230337_1_, CompoundNBT compound) {
			super.func_230337_a_(p_230337_1_, compound);
			
			internalStoredXp = compound.getInt("assemblylinemachines:internalxp");
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);

			compound.putInt("assemblylinemachines:internalxp", internalStoredXp);
			
			return compound;
		}
		
		@Override
		public void tick() {
			if(!world.isRemote) {
				if(timer++ == 40) {
					boolean sendUpdates = false;
					timer = 0;
					if(connectToOutput()) {
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
							bb = new AxisAlignedBB(pos.offset(getBlockState().get(BlockStateProperties.FACING_EXCEPT_UP).getOpposite(), 2)).grow(1);
						}
						
						List<ExperienceOrbEntity> el = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, bb);
						
						for(ExperienceOrbEntity entity : el) {
							internalStoredXp += (entity.getXpValue() * 15);
							spawnTeleparticles(entity.getPosX(), entity.getPosY(), entity.getPosZ(), world.getChunkAt(entity.func_233580_cy_()));
							sendUpdates = true;
							entity.remove();
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
		
		private static void spawnTeleparticles(double x, double y, double z, Chunk ch) {
			PacketData pd = new PacketData("experience_hopper_particles");
			pd.writeDouble("x", x);
			pd.writeDouble("y", y);
			pd.writeDouble("z", z);
			HashPacketImpl.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), pd);
		}
		
		private boolean connectToOutput() {
			
			if(output != null) {
				return true;
			}
			
			Direction d = getBlockState().get(BlockStateProperties.FACING_EXCEPT_UP);
			
			TileEntity te = world.getTileEntity(pos.offset(d));
			if (te != null) {
				LazyOptional<IFluidHandler> cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
						d.getOpposite());
				IFluidHandler output = cap.orElse(null);
				if (output != null) {
					TEExperienceHopper ipcte = this;
					cap.addListener(new NonNullConsumer<LazyOptional<IFluidHandler>>() {

						@Override
						public void accept(LazyOptional<IFluidHandler> t) {
							if (ipcte != null) {
								ipcte.output = null;
							}
						}
					});

					this.output = output;
					return true;
				}
			}

			return false;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private static final RedstoneParticleData XP_PARTICLE = new RedstoneParticleData(0.643f, 0.960f, 0.258f, 1f);

	@OnlyIn(Dist.CLIENT)
	public static void spawnTeleparticles(PacketData pd) {
		Minecraft mc = Minecraft.getInstance();
		for (int i = 0; i < 4; i++) {
			mc.player.getEntityWorld().addParticle(XP_PARTICLE, true, pd.get("x", Double.class), pd.get("y", Double.class) + 0.1, pd.get("z", Double.class), 0, 0, 0);
		}
	}

}
