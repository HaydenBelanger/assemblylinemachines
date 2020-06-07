package me.haydenb.assemblylinemachines.packets;

import java.util.HashMap;
import java.util.function.BiConsumer;

import me.haydenb.assemblylinemachines.block.energy.BlockBasicBatteryCell.ScreenBasicBatteryCell;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import net.minecraft.world.World;

public class PacketTargetMethods {

	public static final HashMap<String, BiConsumer<PacketData, World>> PACKET_TARGETS = new HashMap<>();
	
	static {
		PACKET_TARGETS.put("item_pipe_gui", (pd, world) -> ItemPipeConnectorTileEntity.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("battery_cell_gui", (pd, world) -> ScreenBasicBatteryCell.updateDataFromPacket(pd, world));
	}
}
