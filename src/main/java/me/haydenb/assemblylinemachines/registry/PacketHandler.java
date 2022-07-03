package me.haydenb.assemblylinemachines.registry;

import java.util.*;
import java.util.function.BiConsumer;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.TEBatteryCell;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.*;
import me.haydenb.assemblylinemachines.block.machines.BlockOmnivoid.TEOmnivoid;
import me.haydenb.assemblylinemachines.block.pipes.PipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.item.ItemSpores;
import me.haydenb.assemblylinemachines.world.CapabilityBooks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

	public static final HashMap<String, BiConsumer<PacketData, Level>> PACKET_TARGETS = new HashMap<>();
	static {
		//CLIENT -> SERVER
		PACKET_TARGETS.put("item_pipe_gui", (pd, world) -> PipeConnectorTileEntity.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("battery_cell_gui", (pd, world) -> TEBatteryCell.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("autocrafting_gui", (pd, world) -> BlockAutocraftingTable.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("refinery_gui", (pd, world) -> BlockRefinery.dumpFluid(pd, world));
		PACKET_TARGETS.put("fluid_router_gui", (pd, world) -> BlockFluidRouter.setFilter(pd, world));
		PACKET_TARGETS.put("interactor_gui", (pd, world) -> BlockInteractor.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("exp_mill_gui", (pd, world) -> BlockExperienceMill.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("quarry_gui", (pd, world) -> BlockQuarry.updateDataFromPacket(pd, world));
		PACKET_TARGETS.put("quantum_link_gui", (pd, world) -> BlockQuantumLink.receiveFromServer(pd, world));
		PACKET_TARGETS.put("machine_builder_gui", (pd, world) -> ((IMachineDataBridge) world.getBlockEntity(pd.get("pos", BlockPos.class))).receiveButtonPacket(pd));
		PACKET_TARGETS.put("omnivoid_gui", (pd, world) -> ((TEOmnivoid) world.getBlockEntity(pd.get("location", BlockPos.class))).toggleSettings(pd.get("settingtoggle", Integer.class)));
		PACKET_TARGETS.put("request_book", (pd, world) -> CapabilityBooks.guideBookServerRequestHandler(pd.get("uuid", UUID.class)));

		//SERVER -> CLIENT
		PACKET_TARGETS.put("vacuum_hopper_particles", (pd, world) -> BlockVacuumHopper.spawnTeleparticles(pd));
		PACKET_TARGETS.put("experience_hopper_particles", (pd, world) -> BlockExperienceHopper.spawnTeleparticles(pd));
		PACKET_TARGETS.put("spores_growth", (pd, world) -> ItemSpores.spawnGrowParticles(pd));
	}

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AssemblyLineMachines.MODID, "primary"), () -> "1", "1"::equals, "1"::equals);
	public static int simpleId = 0;

	public static class PacketData{

		private final Map<String, Object> map;
		private final String title;

		public PacketData(String title) {
			this(title, new HashMap<>());
		}

		private PacketData(String title, Map<String, Object> map) {
			this.map = map;
			this.title = title;
		}

		public void writeString(String key, String value) {
			map.put(key, value);
		}

		public void writeInteger(String key, Integer value) {
			map.put(key, value);
		}

		public void writeBoolean(String key, Boolean value) {
			map.put(key, value);
		}

		public void writeBlockPos(String key, BlockPos value) {
			map.put(key, value);
		}

		public void writeDouble(String key, Double value) {
			map.put(key, value);
		}

		public void writeUUID(String key, UUID value) {
			map.put(key, value);
		}

		public void writeByteArray(String key, byte[] value) {
			map.put(key, value);
		}

		public <T> T get(String key, Class<T> clazz) {
			return clazz.cast(map.get(key));
		}

		public String getCategory() {
			return this.title;
		}
	}

	public static void register() {
		INSTANCE.registerMessage(PacketHandler.simpleId++, PacketData.class, (t, u) -> {
			//Encoder
			u.writeUtf(t.title);
			u.writeMap(t.map, (buf, key) -> buf.writeUtf(key), (buf, val) -> {
				if(val instanceof String string) {
					buf.writeUtf("String");
					buf.writeUtf(string);
				}else if(val instanceof Integer integer) {
					buf.writeUtf("Integer");
					buf.writeInt(integer);
				}else if(val instanceof Boolean bool) {
					buf.writeUtf("Boolean");
					buf.writeBoolean(bool);
				}else if(val instanceof BlockPos pos) {
					buf.writeUtf("BlockPos");
					buf.writeBlockPos(pos);
				}else if(val instanceof Double d) {
					buf.writeUtf("Double");
					buf.writeDouble(d);
				}else if(val instanceof UUID uuid) {
					buf.writeUtf("UUID");
					buf.writeUUID(uuid);
				}else if(val instanceof byte[] byteArray){
					buf.writeUtf("ByteArray");
					buf.writeByteArray(byteArray);
				}else throw new IllegalArgumentException("Illegal member in PacketData.");
			});
		}, (t) -> {
			//Decoder
			return new PacketData(t.readUtf(), t.readMap((buf) -> buf.readUtf(), (buf) -> {
				return switch(buf.readUtf()) {
				case "String" -> buf.readUtf();
				case "Integer" -> buf.readInt();
				case "Boolean" -> buf.readBoolean();
				case "BlockPos" -> buf.readBlockPos();
				case "Double" -> buf.readDouble();
				case "UUID" -> buf.readUUID();
				case "ByteArray" -> buf.readByteArray();
				default -> throw new IllegalArgumentException("Unexpected result from packet.");
				};
			}));
		}, (t, u) -> u.get().enqueueWork(() -> {
			//Handler
			var cons = PACKET_TARGETS.get(t.title);
			if(cons != null) cons.accept(t, u.get().getSender() != null ? u.get().getSender().getCommandSenderWorld() : null);
			u.get().setPacketHandled(true);
		}));
	}
}
