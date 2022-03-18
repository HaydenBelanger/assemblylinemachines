package me.haydenb.assemblylinemachines.registry;

import java.util.HashMap;
import java.util.Set;
import java.util.function.*;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.energy.BlockBatteryCell.TEBatteryCell;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.MachineBlockEntityBuilder.IMachineDataBridge;
import me.haydenb.assemblylinemachines.block.machines.*;
import me.haydenb.assemblylinemachines.block.machines.BlockOmnivoid.TEOmnivoid;
import me.haydenb.assemblylinemachines.block.pipes.PipeConnectorTileEntity;
import me.haydenb.assemblylinemachines.item.ItemSpores;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;
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
		
		//SERVER -> CLIENT
		PACKET_TARGETS.put("vacuum_hopper_particles", (pd, world) -> BlockVacuumHopper.spawnTeleparticles(pd));
		PACKET_TARGETS.put("experience_hopper_particles", (pd, world) -> BlockExperienceHopper.spawnTeleparticles(pd));
		PACKET_TARGETS.put("spores_growth", (pd, world) -> ItemSpores.spawnGrowParticles(pd));
	}
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AssemblyLineMachines.MODID, "primary"), () -> "1", "1"::equals, "1"::equals);
	public static int ID = 0;
	
	
	
	public static class PacketData{
		
		private final HashMap<String, Pair<Integer, Object>> map;
		private final String title;
		
		public PacketData(String title) {
			this.map = new HashMap<>();
			this.title = title;
		}
		
		public void writeUtf(String key, String value) {
			map.put(key, new Pair<>(0, value));
		}
		
		public void writeInteger(String key, Integer value) {
			map.put(key, new Pair<>(1, value));
		}
		
		public void writeBoolean(String key, Boolean value) {
			map.put(key, new Pair<>(2, value));
		}
		
		public void writeFloat(String key, Float value) {
			map.put(key, new Pair<>(3, value));
		}
		
		public void writeBlockPos(String key, BlockPos value) {
			map.put(key, new Pair<>(4, value));
		}
		
		public void writeItemStack(String key, ItemStack value) {
			map.put(key, new Pair<>(5, value));
		}
		
		public void writeResourceLocation(String key, ResourceLocation value) {
			map.put(key, new Pair<>(6, value));
		}
		
		public void writeDouble(String key, Double value) {
			map.put(key, new Pair<>(7, value));
		}
		
		public Pair<Integer, Object> get(String key) {
			return map.get(key);
		}
		
		public <T> T get(String key, Class<T> clazz) {
			return clazz.cast(map.get(key).getSecond());
		}
		
		public int getMapSize() {
			return map.size();
		}
		public String getCategory() {
			return title;
		}
		
		public Set<String> getKeySet() {
			return map.keySet();
		}
	}
	public static class DecoderConsumer implements Function<FriendlyByteBuf, PacketData>{

		@Override
		public PacketData apply(FriendlyByteBuf t) {
			
			PacketData pd = new PacketData(t.readUtf(32767));
			
			int max = t.readInt();
			for(int i = 0; i < max; i++) {
				
				String key = t.readUtf(32767);
				int id = t.readInt();
				
				if(id == 0) {
					pd.writeUtf(key, t.readUtf(32767));
				}else if(id == 1) {
					pd.writeInteger(key, t.readInt());
				}else if(id == 2) {
					pd.writeBoolean(key, t.readBoolean());
				}else if(id == 3) {
					pd.writeFloat(key, t.readFloat());
				}else if(id == 4) {
					pd.writeBlockPos(key, t.readBlockPos());
				}else if(id == 5) {
					pd.writeItemStack(key, t.readItem());
				}else if(id == 6) {
					pd.writeResourceLocation(key, t.readResourceLocation());
				}else if(id == 7) {
					pd.writeDouble(key, t.readDouble());
				}
			}
			return pd;
		}
		
	}
	public static class EncoderConsumer implements BiConsumer<PacketData, FriendlyByteBuf>{

		@Override
		public void accept(PacketData t, FriendlyByteBuf u) {
			
			u.writeUtf(t.getCategory());
			u.writeInt(t.getMapSize());
			
			for(String k : t.getKeySet()) {
				u.writeUtf(k);
				Pair<Integer, Object> v = t.get(k);
				Integer id = v.getFirst();
				Object data = v.getSecond();
				u.writeInt(id);
				
				if(id == 0) {
					u.writeUtf((String) data);
				}else if(id == 1) {
					u.writeInt((Integer) data);
				}else if(id == 2) {
					u.writeBoolean((Boolean) data);
				}else if(id == 3) {
					u.writeFloat((Float) data);
				}else if(id == 4) {
					u.writeBlockPos((BlockPos) data);
				}else if(id == 5) {
					u.writeItem((ItemStack) data);
				}else if(id == 6) {
					u.writeResourceLocation((ResourceLocation) data);
				}else if(id == 7) {
					u.writeDouble((Double) data);
				}
				
			}
			
		}
		
	}
	
	public static class MessageHandler implements BiConsumer<PacketData, Supplier<NetworkEvent.Context>>{

		@Override
		public void accept(PacketData t, Supplier<Context> u) {
			u.get().enqueueWork(new Runnable() {
				
				@Override
				public void run() {
					
					BiConsumer<PacketData, Level> x = PACKET_TARGETS.get(t.title);
					if(x != null) {
						if(u.get().getSender() != null) {
							x.accept(t, u.get().getSender().getCommandSenderWorld());
						}else {
							x.accept(t, null);
						}
						
					}else {
						AssemblyLineMachines.LOGGER.warn("Received packet with no method target: " + t.title + ". Look out for injection possibilities.");
					}
					
					
				}
			});
			u.get().setPacketHandled(true);
			
		}
		
	}
}
