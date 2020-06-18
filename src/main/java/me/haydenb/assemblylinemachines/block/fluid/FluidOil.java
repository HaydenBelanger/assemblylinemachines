package me.haydenb.assemblylinemachines.block.fluid;

import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidOil extends ForgeFlowingFluid {

	private static final FluidAttributes.Builder ATTRIBUTES = Registry.getFluidAttributes("oil").temperature(100);
	private static final ForgeFlowingFluid.Properties PROPERTIES = Registry.getFluidProperties("oil", ATTRIBUTES);
	private final boolean source;
	
	public FluidOil(boolean source) {
		super(PROPERTIES);
		this.source = source;
		if(!source) {
			setDefaultState(getStateContainer().getBaseState().with(LEVEL_1_8, 4));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Fluid, IFluidState> builder) {
		super.fillStateContainer(builder);
		
		if(!source) {
			builder.add(LEVEL_1_8);
		}
	}
	@Override
	public boolean isSource(IFluidState state) {
		return source;
	}

	@Override
	public int getLevel(IFluidState state) {
		if(!source) {
			return state.get(LEVEL_1_8);
		}else {
			return 5;
		}
	}
	
	@Override
	public int getTickRate(IWorldReader world) {
		return 25;
	}
	
	public static class FluidOilBlock extends FlowingFluidBlock{

		public FluidOilBlock() {
			super(() -> (FlowingFluid) Registry.getFluid("oil"), Block.Properties.create(Material.WATER).hardnessAndResistance(100f).noDrops());
		}
		
		@Override
		public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entity) {
			if(entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 60));
				player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 40, 3));
			}
			super.onEntityCollision(state, worldIn, pos, entity);
		}
		
	}

}
