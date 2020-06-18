package me.haydenb.assemblylinemachines.events;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.fluid.FluidLevelManager;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class Events {
	
	private static final Direction[] dirs = new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if(!event.getWorld().isRemote()) {
			if(event.getState().getBlock() == Registry.getBlock("crank")) {
				boolean b = false;
				for(Direction d : dirs) {
					BlockState state = event.getWorld().getBlockState(event.getPos().offset(d));
					Block block = state.getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						if(((ICrankableBlock) block).validSide(state, d.getOpposite()) && !((ICrankableBlock) block).needsGearbox()) {
							b = true;
							event.getWorld().setBlockState(event.getPos(), event.getState().with(HorizontalBlock.HORIZONTAL_FACING, d.getOpposite()), 2);
							break;
						}
						
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
			
			else if(event.getState().getBlock() == Registry.getBlock("gearbox")) {
				boolean b = false;
				for(Direction d : dirs) {
					BlockState state = event.getWorld().getBlockState(event.getPos().offset(d));
					Block block = state.getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						if(((ICrankableBlock) block).validSide(state, d.getOpposite())) {
							b = true;
							event.getWorld().setBlockState(event.getPos(), event.getState().with(HorizontalBlock.HORIZONTAL_FACING, d), 2);
							break;
						}
						
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
		}
		
	}
	
	@SubscribeEvent
	public static void chunkLoad(ChunkDataEvent.Load event) {
		
		if(event.getWorld() != null && !event.getWorld().isRemote()) {
			FluidLevelManager.readData(event.getChunk().getPos(), event.getWorld(), event.getData());
		}
	}
	
	@SubscribeEvent
	public static void chunkSave(ChunkDataEvent.Save event) {
		if(event.getWorld() != null && !event.getWorld().isRemote()) {
			FluidLevelManager.writeData(event.getChunk(), event.getWorld(), event.getData().getCompound("Level"));
		}
	}
	
	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		if(event.getWorld() != null && !event.getWorld().isRemote()) {
			FluidLevelManager.clearData(event.getWorld(), event.getChunk().getPos());
		}
	}
	
	@SubscribeEvent
	public static void extinguishFire(PlayerInteractEvent.LeftClickBlock event) {

		World world = event.getWorld();
		if(event.getFace() == Direction.UP) {
			BlockPos up = event.getPos().up();
			Block block = world.getBlockState(up).getBlock();
			
			if(block == Registry.getBlock("naphtha_fire")) {
				if(event.getPlayer().isCreative()) {
					event.setCanceled(true);
				}
				world.playEvent(event.getPlayer(), 1009, up, 0);
				world.removeBlock(up, false);
			}
		}
			
	}
}
