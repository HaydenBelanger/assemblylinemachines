package me.haydenb.assemblylinemachines.events;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.machines.electric.BlockQuarryAddon;
import me.haydenb.assemblylinemachines.fluid.FluidLevelManager;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.item.items.ItemMobCrystal;
import me.haydenb.assemblylinemachines.plugins.other.PluginPatchouli;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.world.*;
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
			
			else if(event.getState().getBlock() instanceof BlockQuarryAddon) {
				boolean b = false;
				for(Direction d : Direction.values()) {
					BlockState state = event.getWorld().getBlockState(event.getPos().offset(d));
					Block block = state.getBlock();
					if(block == Registry.getBlock("quarry")) {
						b = true;
						event.getWorld().setBlockState(event.getPos(), event.getState().with(BlockStateProperties.FACING, d).with(BlockQuarryAddon.getAddonProperty(d), true), 2);
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
		if(event.getSource().getTrueSource() instanceof ServerPlayerEntity) {
			
			ServerPlayerEntity spe = (ServerPlayerEntity) event.getSource().getTrueSource();
			
			ItemStack stack = spe.getHeldItemMainhand();
			
			if (stack.getItem() == Registry.getItem("mystium_sword") && General.RAND.nextInt(10) == 0 && ItemMobCrystal.MOB_COLORS.get(event.getEntity().getType()) != null 
					&& stack.hasTag() && stack.getTag().contains("assemblylinemachines:fe") && stack.getTag().contains("assemblylinemachines:secondarystyle")) {
				ItemStack inert = null;
				for(int i = 0; i < spe.inventory.getSizeInventory(); i++) {
					ItemStack sis = spe.inventory.getStackInSlot(i);
					if(sis.getItem() == Registry.getItem("mob_crystal") && !sis.hasTag()) {
						inert = sis;
						break;
					}
				}
				if(spe.isCreative() || inert != null) {
					ItemStack crystal = new ItemStack(Registry.getItem("mob_crystal"), 1);
					CompoundNBT tag = new CompoundNBT();
					tag.putString("assemblylinemachines:mob", event.getEntity().getType().getRegistryName().toString());
					crystal.setTag(tag);
					event.getEntity().entityDropItem(crystal);
					stack.damageItem(20, spe, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
					if(!spe.isCreative() && inert != null) {
						inert.shrink(1);
					}
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
	
	@SubscribeEvent
	public static void joinGiveBook(PlayerLoggedInEvent event) {
		
		
		CompoundNBT nbt = event.getPlayer().getPersistentData();
		if(!nbt.contains("assemblylinemachines:book") || nbt.getBoolean("assemblylinemachines:book") == false) {
			
			if(PluginPatchouli.get().isPatchouliInstalled()) {
				nbt.putBoolean("assemblylinemachines:book", true);
				event.getPlayer().addItemStackToInventory(new ItemStack(Registry.getItem("guidebook")));
			}
			
		}
	}
}
