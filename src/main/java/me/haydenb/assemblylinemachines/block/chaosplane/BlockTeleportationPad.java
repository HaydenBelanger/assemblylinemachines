package me.haydenb.assemblylinemachines.block.chaosplane;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.world.DimensionChaosPlane;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.common.util.ITeleporter;

public class BlockTeleportationPad extends Block {

	private static final ChaosPlaneTeleporter TELEPORTER = new ChaosPlaneTeleporter();
	
	private static final VoxelShape SHAPE = Stream.of(
			Block.box(1, 3, 1, 2, 15, 2),
			Block.box(1, 3, 14, 2, 15, 15),
			Block.box(14, 3, 14, 15, 15, 15),
			Block.box(14, 3, 1, 15, 15, 2),
			Block.box(0, 0, 0, 16, 3, 16)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	
	public BlockTeleportationPad() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any());
	}
	
	@Override
	public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
		pTooltip.add(new TextComponent("§8§oTransports the user to the Chaos Plane."));
		super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
	}
	
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if(!pState.getBlock().equals(pNewState.getBlock())) {
			pLevel.removeBlockEntity(pPos);
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!level.isClientSide && hand.equals(InteractionHand.MAIN_HAND)) {
			if(player.getBlockX() != pos.getX() || player.getBlockZ() != pos.getZ()) {
				player.displayClientMessage(new TextComponent("§cYou must be on the Teleportation Pad to use it."), true);
			}else {
				boolean genPlatform = false;
				ResourceLocation currentDim = level.dimension().location();
				ServerLevel targetDim = null;
				if(currentDim.equals(DimensionType.OVERWORLD_LOCATION.location())) {
					targetDim = level.getServer().getLevel(DimensionChaosPlane.CHAOS_PLANE);
					genPlatform = true;
				}else if(currentDim.equals(DimensionChaosPlane.CHAOS_PLANE_LOCATION.location())) {
					targetDim = level.getServer().getLevel(Level.OVERWORLD);
				}
				
				if(targetDim == null) {
					player.displayClientMessage(new TextComponent("§cYou cannot use the Teleportation Pad in this dimension."), true);
				}else {
					Player newPlayer = (Player) player.changeDimension(targetDim, TELEPORTER);
					if(newPlayer != null) TELEPORTER.postTeleport(newPlayer, genPlatform);
				}
				
				
			}
			
		}
		return InteractionResult.PASS;
	}
	
	public static class ChaosPlaneTeleporter implements ITeleporter{
		@Override
		public PortalInfo getPortalInfo(Entity entity, ServerLevel target, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
			
			return new PortalInfo(new Vec3(entity.getBlockX() + 0.5d, entity.getBlockY(), entity.getBlockZ() + 0.5d), Vec3.ZERO, entity.getYRot(), entity.getXRot());
		}
		
		private void postTeleport(Player player, boolean genPlatform) {
			Level level = player.getCommandSenderWorld();
			double[] tpX = null;
			for(int i = level.getMaxBuildHeight(); i >= level.getMinBuildHeight(); i--) {
				BlockPos curPos = new BlockPos(player.getBlockX(), i, player.getBlockZ());
				if(level.getBlockState(curPos).is(Registry.getBlock("teleportation_pad"))) {
					tpX = new double[] {curPos.getX() + 0.5d, curPos.getY() + (3d/16d), curPos.getZ() + 0.5d};
					break;
				}
			}
			if(tpX == null) {
				BlockPos setPos = new BlockPos(player.getBlockX(), level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, player.getBlockX(), player.getBlockZ()), player.getBlockZ());
				level.setBlockAndUpdate(setPos, Registry.getBlock("teleportation_pad").defaultBlockState());
				FluidState belowFluid = level.getFluidState(setPos.below());
				if(genPlatform && belowFluid != null && !belowFluid.isEmpty()) {
					
					ArrayList<BlockPos> glowStone = new ArrayList<>();
					for(Direction d : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
						glowStone.add(setPos.below().relative(d));
					}
					Iterator<BlockPos> platform = BlockPos.betweenClosed(setPos.below().east(2).north(2), setPos.below().west(2).south(2)).iterator();
					while(platform.hasNext()) {
						BlockPos nextPos = platform.next();
						level.setBlockAndUpdate(nextPos, glowStone.contains(nextPos) ? Blocks.GLOWSTONE.defaultBlockState() : Registry.getBlock("smooth_black_granite").defaultBlockState());
					}
					
					
				}
				tpX = new double[] {setPos.getX() + 0.5d, setPos.getY() + (3d/16d), setPos.getZ() + 0.5d};
			}else {
			}
			player.teleportToWithTicket(tpX[0], tpX[1], tpX[2]);
		}
	}
}
