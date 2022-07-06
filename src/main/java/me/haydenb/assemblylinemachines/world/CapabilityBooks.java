package me.haydenb.assemblylinemachines.world;

import java.util.UUID;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.plugins.PluginPatchouli;
import me.haydenb.assemblylinemachines.registry.PacketHandler;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.config.ALMConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CapabilityBooks {

	public static final Capability<IBookDistroCapability> BOOK_DISTRO_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	//Registered using listeners, as some events in this class are client-only.
	public static void registerAllEvents() {

		IEventBus bus = MinecraftForge.EVENT_BUS;
		bus.addListener((RegisterCapabilitiesEvent event) -> event.register(IBookDistroCapability.class));
		bus.addGenericListener(Entity.class, (AttachCapabilitiesEvent<Entity> event) -> {
			if(event.getObject() instanceof Player player) {
				BookDistroCapability bookDistro = new BookDistroCapability(player);
				LazyOptional<IBookDistroCapability> lazyBookDistro = LazyOptional.of(() -> bookDistro);
				ICapabilitySerializable<CompoundTag> serializableProvider = new ICapabilitySerializable<>() {

					@Override
					public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
						if(cap == CapabilityBooks.BOOK_DISTRO_CAPABILITY) {
							return lazyBookDistro.cast();
						}
						return LazyOptional.empty();
					}

					@Override
					public CompoundTag serializeNBT() {
						return bookDistro.save();
					}

					@Override
					public void deserializeNBT(CompoundTag nbt) {
						bookDistro.load(nbt);
					}
				};
				event.addCapability(new ResourceLocation(AssemblyLineMachines.MODID, "book_distro"), serializableProvider);
			}
		});

		if(FMLLoader.getDist() == Dist.CLIENT) bus.addListener((LoggedInEvent event) -> {
			if(ALMConfig.getClientConfig().receiveGuideBook().get()) {
				AssemblyLineMachines.LOGGER.debug("Sending request for guide to server from player " + event.getPlayer().getDisplayName().getString() + ".");
				PacketData pd = new PacketData("request_book");
				pd.writeUUID("uuid", event.getPlayer().getUUID());
				PacketHandler.INSTANCE.sendToServer(pd);
				return;
			}
		});
	}

	//Receives packet request from server to give book to player by UUID.
	public static void guideBookServerRequestHandler(UUID uuid) {
		if(ALMConfig.getServerConfig().distributeGuideBook().get()) {
			ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
			if(player == null) throw new NullPointerException("UUID-based player lookup failed.");
			AssemblyLineMachines.LOGGER.debug("Received request to dispense book for player " + player.getDisplayName().getString() + ".");
			player.getCapability(CapabilityBooks.BOOK_DISTRO_CAPABILITY).ifPresent((cap) -> cap.giveBook());
			return;
		}
		AssemblyLineMachines.LOGGER.debug("Consuming request to dispense guide from " + uuid.toString() + ".");
	}

	public static interface IBookDistroCapability{

		/**
		 * Gives a copy of "Assembly Lines & You" to the Player as long as it is enabled in the config to do so.
		 * @return Whether or not a book was successfully given to the Player.
		 */
		public boolean giveBook();
		public boolean receivedBook();
	}

	public static class BookDistroCapability implements IBookDistroCapability{

		boolean givenBook = false;
		private final Player player;

		public BookDistroCapability(Player player) {
			this.player = player;
		}

		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();

			tag.putBoolean("assemblylinemachines:given_book", givenBook);
			return tag;
		}

		public void load(CompoundTag tag) {
			givenBook = tag.getBoolean("assemblylinemachines:given_book");
		}

		@Override
		public boolean giveBook() {
			if(!givenBook) {
				ItemStack book = PluginPatchouli.INTERFACE.get().getBookItem();
				if(!book.isEmpty()) {
					givenBook = true;
					player.addItem(book);
					player.shouldBeSaved();
					AssemblyLineMachines.LOGGER.debug("Guide dispensed successfully.");
					return true;
				}

				AssemblyLineMachines.LOGGER.debug("Patchouli is not installed on the server.");
				return false;
			}
			AssemblyLineMachines.LOGGER.debug("Guide has already been dispensed to this player.");
			return false;
		}

		@Override
		public boolean receivedBook() {
			return givenBook;
		}
	}
}
