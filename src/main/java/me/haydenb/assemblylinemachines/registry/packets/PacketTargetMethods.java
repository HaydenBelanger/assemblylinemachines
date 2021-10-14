package me.haydenb.assemblylinemachines.registry.packets;

import java.util.HashMap;
import java.util.function.BiConsumer;

import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.TEBatteryCell;
import me.haydenb.assemblylinemachines.block.machines.electric.*;
import me.haydenb.assemblylinemachines.block.machines.mob.*;
import me.haydenb.assemblylinemachines.block.machines.oil.BlockRefinery;
import me.haydenb.assemblylinemachines.block.pipe.ItemPipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.block.utility.BlockFluidRouter;
import me.haydenb.assemblylinemachines.block.utility.BlockQuantumLink;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.PacketData;
import net.minecraft.world.level.Level;

public class PacketTargetMethods {

	public static final HashMap<String, BiConsumer<PacketData, Level>> PACKET_TARGETS = new HashMap<>();
	
	static {
		
		//CLIENT -> SERVER
		PACKET_TARGETS.put("item_pipe_gui", (pd, world) -> ItemPipeConnectorTileEntity.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("battery_cell_gui", (pd, world) -> TEBatteryCell.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("autocrafting_gui", (pd, world) -> BlockAutocraftingTable.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("refinery_gui", (pd, world) -> BlockRefinery.dumpFluid(pd, world));
		PACKET_TARGETS.put("fluid_router_gui", (pd, world) -> BlockFluidRouter.setFilter(pd, world));
		PACKET_TARGETS.put("interactor_gui", (pd, world) -> BlockInteractor.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("exp_mill_gui", (pd, world) -> BlockExperienceMill.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("quarry_gui", (pd, world) -> BlockQuarry.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("quantum_link_gui", (pd, world) -> BlockQuantumLink.receiveFromServer(pd, world));
		PACKET_TARGETS.put("efm_gui", (pd, world) -> BlockElectricFluidMixer.updateDataFromPacket(pd, world));
		
		//SERVER -> CLIENT
		PACKET_TARGETS.put("vacuum_hopper_particles", (pd, world) -> BlockVacuumHopper.spawnTeleparticles(pd));
		PACKET_TARGETS.put("experience_hopper_particles", (pd, world) -> BlockExperienceHopper.spawnTeleparticles(pd));
	}
}
