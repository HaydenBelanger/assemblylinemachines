package me.haydenb.assemblylinemachines.block.fluid;

import java.util.Iterator;
import java.util.Random;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidCondensedVoid extends ForgeFlowingFluid {

	private static final FluidAttributes.Builder ATTRIBUTES = Registry.getFluidAttributes("condensed_void").temperature(-200);
	private static final ForgeFlowingFluid.Properties PROPERTIES = Registry.getFluidProperties("condensed_void",
			ATTRIBUTES);
	private final boolean source;
	private static final Random RAND = new Random();

	public FluidCondensedVoid(boolean source) {
		super(PROPERTIES);
		this.source = source;
		if (!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, IFluidState> builder) {
		super.fillStateContainer(builder);

		if (!source) {
			builder.add(LEVEL_1_8);
		}
	}

	@Override
	public boolean isSource(IFluidState state) {
		return source;
	}
	

	@Override
	protected int getLevelDecreasePerBlock(IWorldReader worldIn) {
		return 2;
	}
	
	@Override
	protected void randomTick(World world, BlockPos pos, IFluidState state, Random random) {
		Iterator<BlockPos> iter = BlockPos.getAllInBox(pos.up().north().east(), pos.down().south().west()).iterator();
		while(iter.hasNext()) {
			BlockPos cor = iter.next();
			if(random.nextInt(2) == 0) {
				Block block = world.getBlockState(cor).getBlock();
				if(block.getTags().contains(new ResourceLocation("minecraft", "leaves")) || block.getTags().contains(new ResourceLocation("minecraft", "logs"))
						|| block.getTags().contains(new ResourceLocation("minecraft", "flowers")) || block.getTags().contains(new ResourceLocation("minecraft", "planks"))
						|| block.getTags().contains(new ResourceLocation("minecraft", "wool"))
						|| block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.DEAD_BUSH || block == Blocks.FERN || block == Blocks.COARSE_DIRT
						|| block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.GRASS_PATH) {
					world.destroyBlock(cor, false);
				}else if(block.getDefaultState().getMaterial() == Material.ROCK) {
					world.setBlockState(cor, Blocks.GRAVEL.getDefaultState());
				}else if(block == Blocks.WATER) {
					world.setBlockState(cor, Blocks.PACKED_ICE.getDefaultState());
				}else if(block == Blocks.LAVA) {
					world.setBlockState(cor, Blocks.OBSIDIAN.getDefaultState());
				}
			}
				
		}
		
		super.randomTick(world, pos, state, random);
	}
	
	@Override
	protected boolean ticksRandomly() {
		return true;
	}

	@Override
	public int getLevel(IFluidState state) {
		if (!source) {
			return state.get(LEVEL_1_8);
		} else {
			return 8;
		}
	}

	@Override
	public int getTickRate(IWorldReader world) {
		return 5;
	}

	public static class FluidCondensedVoidBlock extends FlowingFluidBlock {

		public FluidCondensedVoidBlock() {
			super(() -> (FlowingFluid) Registry.getFluid("condensed_void"),
					Block.Properties.create(Material.WATER).hardnessAndResistance(100f).noDrops());
		}

		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			if(entity instanceof ItemEntity) {
				
				ItemEntity itemEntity = (ItemEntity) entity;
				ItemStack stack = itemEntity.getItem();
				if(stack.getItem() != Registry.getItem("corrupted_shard")) {
					
					CompoundNBT main = new CompoundNBT();
					CompoundNBT sub = new CompoundNBT();
					new ItemStack(stack.getItem(), 1).write(sub);
					main.put("assemblylinemachines:internalitem", sub);
					
					ItemStack is = new ItemStack(Registry.getItem("corrupted_shard"), stack.getCount());
					is.setTag(main);
					itemEntity.setItem(is);
				}else {
					itemEntity.setPositionAndUpdate(itemEntity.lastTickPosX + ((RAND.nextDouble() * 2D) - 1D), itemEntity.lastTickPosY + ((RAND.nextDouble() * 4D) - 2D), itemEntity.lastTickPosZ + ((RAND.nextDouble() * 2D) - 1D));
				}
			}else if (entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100));
				player.addPotionEffect(new EffectInstance(Effects.WITHER, 40, 6));
				
				entity.setMotionMultiplier(state, new Vec3d(0.02D, 0.02D, 0.02D));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
	}

}
