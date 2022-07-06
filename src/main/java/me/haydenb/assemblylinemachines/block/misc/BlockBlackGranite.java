package me.haydenb.assemblylinemachines.block.misc;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.PluginTOPRegistry.TOPProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

public class BlockBlackGranite extends Block implements TOPProvider{

	public static final BooleanProperty NATURAL_GRANITE = BooleanProperty.create("natural");
	public BlockBlackGranite() {
		super(Block.Properties.of(Material.STONE).strength(3f, 9f));
		this.registerDefaultState(this.stateDefinition.any().setValue(NATURAL_GRANITE, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(NATURAL_GRANITE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn,
			BlockPos pos) {
		if(canToolMine(player, state)) {
			return super.getDestroyProgress(state, player, worldIn, pos);
		}

		return super.getDestroyProgress(state, player, worldIn, pos) * 0.05F;
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {
		if(state.getValue(NATURAL_GRANITE)) {
			if(canToolMine(player, state)) {
				probeInfo.horizontal().text(Component.literal("Block will drop when mined.").withStyle(ChatFormatting.GREEN));
			}else {
				probeInfo.horizontal().text(Component.literal("Block will not drop when mined.").withStyle(ChatFormatting.RED));
			}
		}


	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if(!player.isCreative() && !canToolMine(player, state)) {
			player.displayClientMessage(Component.literal("A Pickaxe with mechanical power is needed to extract the Black Granite.").withStyle(ChatFormatting.RED), true);
		}
		return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	private static boolean canToolMine(Player player, BlockState state) {
		if(state.getValue(NATURAL_GRANITE)) {
			ItemStack item = player.getMainHandItem();
			if(item != ItemStack.EMPTY && item.hasTag()) {
				CompoundTag compound = item.getTag();
				if(compound.getBoolean("assemblylinemachines:canbreakblackgranite")) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
