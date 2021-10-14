package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockQuarryAddon;
import me.haydenb.assemblylinemachines.item.items.ItemMobCrystal;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forgespi.language.IModInfo;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID)
public class Events {
	
	private static final Direction[] dirs = new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if(!event.getWorld().isClientSide()) {
			if(event.getState().getBlock() == Registry.getBlock("crank")) {
				boolean b = false;
				for(Direction d : dirs) {
					BlockState state = event.getWorld().getBlockState(event.getPos().relative(d));
					Block block = state.getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						if(((ICrankableBlock) block).validSide(state, d.getOpposite()) && !((ICrankableBlock) block).needsGearbox()) {
							b = true;
							event.getWorld().setBlock(event.getPos(), event.getState().setValue(HorizontalDirectionalBlock.FACING, d.getOpposite()), 2);
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
					BlockState state = event.getWorld().getBlockState(event.getPos().relative(d));
					Block block = state.getBlock();
					if(block != null && block instanceof ICrankableBlock) {
						if(((ICrankableBlock) block).validSide(state, d.getOpposite())) {
							b = true;
							event.getWorld().setBlock(event.getPos(), event.getState().setValue(HorizontalDirectionalBlock.FACING, d), 2);
							break;
						}
						
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
			
			else if(event.getState().getBlock() instanceof BlockQuarryAddon) {
				boolean b = false;
				for(Direction d : Direction.values()) {
					BlockState state = event.getWorld().getBlockState(event.getPos().relative(d));
					Block block = state.getBlock();
					if(block == Registry.getBlock("quarry")) {
						b = true;
						event.getWorld().setBlock(event.getPos(), event.getState().setValue(BlockStateProperties.FACING, d).setValue(BlockQuarryAddon.getAddonProperty(d), true), 2);
						break;
					}
				}
				if(b == false) {
					event.setCanceled(true);
				}
			}
		}
		
	}
	
	@SubscribeEvent
	public static void kill(LivingDeathEvent event) {
		if(event.getSource().getEntity() instanceof ServerPlayer) {
			
			ServerPlayer spe = (ServerPlayer) event.getSource().getEntity();
			ItemStack stack = spe.getMainHandItem();
			
			if (stack.getItem() == Registry.getItem("mystium_sword") && General.RAND.nextInt(10) == 0 && ItemMobCrystal.MOB_COLORS.get(event.getEntity().getType()) != null 
					&& stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {
				ItemStack inert = null;
				for(int i = 0; i < spe.getInventory().getContainerSize(); i++) {
					ItemStack sis = spe.getInventory().getItem(i);
					if(sis.getItem() == Registry.getItem("mob_crystal") && !sis.hasTag()) {
						inert = sis;
						break;
					}
				}
				if(spe.isCreative() || inert != null) {
					ItemStack crystal = new ItemStack(Registry.getItem("mob_crystal"), 1);
					CompoundTag tag = new CompoundTag();
					tag.putString("assemblylinemachines:mob", event.getEntity().getType().getRegistryName().toString());
					crystal.setTag(tag);
					event.getEntity().spawnAtLocation(crystal);
					stack.hurtAndBreak(20, spe, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
					if(!spe.isCreative() && inert != null) {
						inert.shrink(1);
					}
				}
				
			}
		}
	}
	
	@SubscribeEvent
	public static void chunkLoad(ChunkDataEvent.Load event) {
		
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.readData(event.getChunk().getPos(), event.getWorld(), event.getData());
		}
	}
	
	@SubscribeEvent
	public static void chunkSave(ChunkDataEvent.Save event) {
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.writeData(event.getChunk(), event.getWorld(), event.getData().getCompound("Level"));
		}
	}
	
	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		if(event.getWorld() != null && !event.getWorld().isClientSide()) {
			FluidLevelManager.clearData(event.getWorld(), event.getChunk().getPos());
		}
	}
	
	@SubscribeEvent
	public static void extinguishFire(PlayerInteractEvent.LeftClickBlock event) {

		Level world = event.getWorld();
		if(event.getFace() == Direction.UP) {
			BlockPos up = event.getPos().above();
			Block block = world.getBlockState(up).getBlock();
			
			if(block == Registry.getBlock("naphtha_fire")) {
				if(event.getPlayer().isCreative()) {
					event.setCanceled(true);
				}
				world.levelEvent(event.getPlayer(), 1009, up, 0);
				world.removeBlock(up, false);
			}
		}
			
	}
	
	@SubscribeEvent
	public static void join(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if(!player.getCommandSenderWorld().isClientSide() && ConfigHolder.COMMON.updateChecker.get()) {
			ModContainer mc = AssemblyLineMachines.getModContainer();
			if(mc != null) {
				IModInfo imi = mc.getModInfo();
				CheckResult result = VersionChecker.getResult(imi);
				
				if(result.status() == Status.BETA_OUTDATED || result.status() == Status.OUTDATED) {
					TextComponent tc = new TextComponent("[§aAssemblyLineMachines§f] Update available, version §e" + result.target().getCanonical() + ",§f you're using §e" + imi.getVersion().toString() + ". §2Click to Update!");
					tc.withStyle(tc.getStyle().withClickEvent(new ClickEvent(Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/assembly-line-machines")));
					player.sendMessage(tc, null);
				}
			}
		}
		
		
		
		
		CompoundTag nbt = event.getPlayer().getPersistentData();
		if(!nbt.contains("assemblylinemachines:book") || nbt.getBoolean("assemblylinemachines:book") == false) {
			
			/*
			if(PluginPatchouli.get().isPatchouliInstalled()) {
				nbt.putBoolean("assemblylinemachines:book", true);
				event.getPlayer().addItem(new ItemStack(Registry.getItem("guidebook")));
			}
			*/
			
		}
	}
}
