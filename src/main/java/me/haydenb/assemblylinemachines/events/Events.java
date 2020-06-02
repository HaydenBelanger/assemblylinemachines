package me.haydenb.assemblylinemachines.events;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.ICrankableMachine.ICrankableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.Direction;
import net.minecraftforge.event.world.BlockEvent;
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
					Block block = event.getWorld().getBlockState(event.getPos().offset(d)).getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						b = true;
						event.getWorld().setBlockState(event.getPos(), event.getState().with(HorizontalBlock.HORIZONTAL_FACING, d.getOpposite()), 2);
						break;
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
			
			else if(event.getState().getBlock() == Registry.getBlock("gearbox")) {
				boolean b = false;
				for(Direction d : dirs) {
					Block block = event.getWorld().getBlockState(event.getPos().offset(d)).getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						b = true;
						event.getWorld().setBlockState(event.getPos(), event.getState().with(HorizontalBlock.HORIZONTAL_FACING, d), 2);
						break;
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
		}
		
	}
}
