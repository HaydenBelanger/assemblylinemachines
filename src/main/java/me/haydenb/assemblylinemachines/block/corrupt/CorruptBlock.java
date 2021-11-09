package me.haydenb.assemblylinemachines.block.corrupt;

import java.util.*;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.TagMaster;
import me.haydenb.assemblylinemachines.registry.TagMaster.IMiningLevelDataGenProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;

public class CorruptBlock extends Block implements TagMaster.IMiningLevelDataGenProvider {

	private final Named<Block> type;
	private final Named<Block> level;
	private final boolean isGrass;
	private final boolean shouldBePoisonous;
	
	public static final HashMap<Block, String> CORRUPT_VARIANTS = new HashMap<>();
	static{
		CORRUPT_VARIANTS.put(Blocks.DIRT, "corrupt_dirt");
		CORRUPT_VARIANTS.put(Blocks.GRASS, "corrupt_grass");
		CORRUPT_VARIANTS.put(Blocks.SAND, "corrupt_sand");
		CORRUPT_VARIANTS.put(Blocks.STONE, "corrupt_stone");
		CORRUPT_VARIANTS.put(Blocks.GRAVEL, "corrupt_gravel");
	}
	
	public CorruptBlock(BlockBehaviour.Properties properties, Named<Block> type, Named<Block> level, boolean isGrass, boolean shouldBePoisonous) {
		super(properties.strength(3f, 9f));
		this.type = type;
		this.level = level;
		this.isGrass = isGrass;
		this.shouldBePoisonous = shouldBePoisonous;
	}
	
	public CorruptBlock(BlockBehaviour.Properties properties, Named<Block> type, Named<Block> level) {
		super(properties.strength(3f, 9f));
		this.type = type;
		this.level = level;
		this.isGrass = false;
		this.shouldBePoisonous = true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
		if(shouldBePoisonous) {
			poisonAll(world, pos);
		}
		
		if(isGrass) {
			if(!SpreadingSnowyDirtBlock.canBeGrass(state, world, pos)) {
				if(world.isAreaLoaded(pos, 3)) {
					world.setBlockAndUpdate(pos, Registry.getBlock("corrupt_dirt").defaultBlockState());
				}

			}
		}

	}
	
	//Used for Chaosbark/Stripped Chaosbark Logs.
	public static class CorruptBlockWithAxis extends CorruptBlock{

		public CorruptBlockWithAxis(Properties properties, Named<Block> type, Named<Block> level, boolean isGrass, boolean shouldBePoisonous) {
			super(properties, type, level, isGrass, shouldBePoisonous);
			this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Axis.Y));
		}
		
		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
			pBuilder.add(RotatedPillarBlock.AXIS);
		}
		
		@Override
		public BlockState rotate(BlockState pState, Rotation pRotation) {
			return RotatedPillarBlock.rotatePillar(pState, pRotation);
		}
		
		@Override
		public BlockState getStateForPlacement(BlockPlaceContext pContext) {
			return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, pContext.getClickedFace().getAxis());
			
		}
		
	}
	
	public static class CorruptLeavesBlock extends LeavesBlock implements IMiningLevelDataGenProvider{

		public CorruptLeavesBlock() {
			super(Properties.of(Material.LEAVES).sound(SoundType.GRASS).randomTicks().noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
		}

		@Override
		public Named<Block> getToolType() {
			return BlockTags.MINEABLE_WITH_PICKAXE;
		}

		@Override
		public Named<Block> getToolLevel() {
			return BlockTags.NEEDS_DIAMOND_TOOL;
		}
		
		
		
	}

	@Override
	public Named<Block> getToolType() {
		return type;
	}


	@Override
	public Named<Block> getToolLevel() {
		return level;
	}
	
	@Override
	public boolean isRandomlyTicking(BlockState p_49921_) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if(shouldBePoisonous) {
			animate(stateIn, worldIn, pos, rand);
		}
		
	}

	//Provides gentle 'corrupt' particles to area of block.
	@OnlyIn(Dist.CLIENT)
	public static void animate(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (rand.nextInt(10) == 0) {
			for(int i = 0; i < 5; i++) {
				worldIn.addParticle(ParticleTypes.MYCELIUM, (double)pos.getX() + rand.nextDouble(), (double)pos.getY() + 1.1D, (double)pos.getZ() + rand.nextDouble(), 0.1D, 0.1D, 0.1D);
			}
			
		}

	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		if(plantable.getPlantType(world, pos) == CorruptTallGrassBlock.CORRUPT_GRASS) {
			Block b = state.getBlock();
			return (b.equals(Registry.getBlock("corrupt_grass")) || b.equals(Registry.getBlock("corrupt_dirt")));
		}
		return false;
	}

	//Applies Entropy Poisoning to all entities in area.
	public static void poisonAll(ServerLevel world, BlockPos pos) {

		BlockPos pos1 = pos.above();
		BlockPos pos2 = pos.above(2);
		List<Player> players = world.getEntitiesOfClass(Player.class, new AABB(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5, pos2.getX() + 1.5, pos2.getY() + 1.5, pos2.getZ() + 1.5));

		for(Player p : players) {
			p.addEffect(new MobEffectInstance(Registry.getEffect("entropy_poisoning"), 100));
		}
	}
}
