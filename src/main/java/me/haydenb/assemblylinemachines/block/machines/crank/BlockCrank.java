package me.haydenb.assemblylinemachines.block.machines.crank;

import java.util.Random;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.helpers.ICrankableMachine;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraftforge.common.ToolType;

public class BlockCrank extends Block {

	private static final Random RAND = new Random();
	private static final VoxelShape SHAPE = Stream
			.of(Block.makeCuboidShape(0, 0, 0, 4, 16, 16), Block.makeCuboidShape(4, 7, 7, 8, 9, 9),
					Block.makeCuboidShape(8, 7, 7, 10, 16, 9), Block.makeCuboidShape(10, 14, 7, 14, 16, 9))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockCrank() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(1f, 2f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {

		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		return General.rotateShape(Direction.EAST, state.get(HorizontalBlock.HORIZONTAL_FACING), SHAPE);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if(!world.isRemote) {
			if(handIn.equals(Hand.MAIN_HAND)) {
				TileEntity te = world.getTileEntity(pos.offset(state.get(HorizontalBlock.HORIZONTAL_FACING).getOpposite()));
				if(te != null && te instanceof ICrankableMachine) {
					ICrankableMachine crankable = (ICrankableMachine) te;
					if(crankable.perform()) {
						world.playSound(null, pos, SoundEvents.BLOCK_WOOD_STEP, SoundCategory.BLOCKS, 0.7f, 1f + getPitchNext());
					}else {
						int chance = ConfigHolder.COMMON.crankSnapChance.get();
						if(chance != -1 && RAND.nextInt(chance) == 0) {
							world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.BLOCKS, 1f, 1f);
							world.setBlockState(pos, Blocks.AIR.getDefaultState());
							General.spawnItem(new ItemStack(Items.STICK, 5), pos, world);
							General.spawnItem(new ItemStack(Registry.getItem("steel_nugget"), 27), pos, world);
						}else {
							world.playSound(null, pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.7f, 1f);
						}
					}
				}
			}
		}
		
		if(world.isRemote) {	
			world.addParticle(ParticleTypes.LARGE_SMOKE, true, pos.getX() + getPartNext(), pos.getY() + getPartNext(), pos.getZ() + getPartNext(), 0, 0, 0);
		}
		return ActionResultType.CONSUME;

	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!worldIn.isRemote()) {
			if (facing == stateIn.get(HorizontalBlock.HORIZONTAL_FACING).getOpposite()) {
				if (worldIn.getBlockState(currentPos.offset(facing)).getBlock() == Blocks.AIR) {
					return Blocks.AIR.getDefaultState();
				}
			}
		}

		return stateIn;
	}
	
	private static double getPartNext() {
		double d = RAND.nextDouble();
		if(d < 0.2 || d > 0.8) {
			d = 0.5;
		}
		return d;
	}
	
	private static float getPitchNext() {
		float f = RAND.nextFloat();
		
		if(f < 0.6f) {
			f = 0f;
		}
		
		if(f > 0.3f) {
			f = f * -1f;
		}
		
		return f;
		
	}

}
