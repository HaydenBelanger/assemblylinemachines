package me.haydenb.assemblylinemachines.block.machines.mob;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockPoweredSpawner extends BlockScreenTileEntity<BlockPoweredSpawner.TEPoweredSpawner>{

	public BlockPoweredSpawner() {
		super(Block.Properties.create(Material.IRON).notSolid().variableOpacity().hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "powered_spawner", BlockPoweredSpawner.TEPoweredSpawner.class);
	}
	
	public static class TEPoweredSpawner extends ManagedSidedMachine<ContainerPoweredSpawner> implements ITickableTileEntity{
		
		
		private EntityType<?> spawnType = null;
		private int timer = 0;
		private int nTimer = 160;
	
		private Entity client_entity = null;
		public float client_renderRot = 0f;
		
		public TEPoweredSpawner(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 4, new TranslationTextComponent(Registry.getBlock("powered_spawner").getTranslationKey()), Registry.getContainerId("powered_spawner"), ContainerPoweredSpawner.class, new EnergyProperties(true, false, 100000));
		}
		
		public TEPoweredSpawner() {
			this(Registry.getTileEntity("powered_spawner"));
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:spawntype")) {
				spawnType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(compound.getString("assemblylinemachines:spawntype")));
			}
			nTimer = compound.getInt("assemblylinemachines:ntimer");
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			
			if(spawnType != null) {
				compound.putString("assemblylinemachines:spawntype", spawnType.getRegistryName().toString());
			}
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			return super.write(compound);
		}
		
		@Override
		public boolean canExtractFromSide(int slot, Direction direction) {
			return false;
		}
		

		@Override
		public void tick() {


			if(!world.isRemote) {
				boolean sendUpdates = false;
				if(spawnType == null) {
					
					ItemStack crystal = contents.get(0);
					
					if(crystal.getItem() == Registry.getItem("mob_crystal") && crystal.hasTag() && crystal.getTag().contains("assemblylinemachines:mob")) {
						
						spawnType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(crystal.getTag().getString("assemblylinemachines:mob")));
						sendUpdates = true;
					}
				}
				
				if(spawnType != null && amount >= 500) {
					if(timer++ == nTimer) {
						timer = 0;
						
						int ug = getUpgradeAmount(Upgrades.MACHINE_EXTRA);
						int rand = General.RAND.nextInt((ug * 2) + 3) + ug;
						int cost = 500;
						switch(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED)) {
						case 3:
							nTimer = 20;
							cost = 375;
							break;
						case 2:
							nTimer = 40;
							cost = 375;
							break;
						case 1:
							nTimer = 80;
							cost = 400;
							break;
						default:
							nTimer = 160;
						}
						
						int spamt = 0;
						for(int i = 0; i < rand; i++) {
							
							if(cost * (spamt + 1) > amount) {
								break;
							}
							int size = 5;
							int max = 5;
							double d0 = (double)pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) size + 0.5D;
							double d1 = (double)(pos.getY() + world.rand.nextInt(3) - 1);
							double d2 = (double)pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) size + 0.5D;
							
							for(int j = 0; j < 10; j++) {
								if(world.hasNoCollisions(spawnType.func_220328_a(d0, d1, d2)) && EntitySpawnPlacementRegistry.func_223515_a(spawnType, world, SpawnReason.SPAWNER, new BlockPos(d0, d1, d2), world.getRandom())) {
									Entity entity = spawnType.create(world);
									entity.setLocationAndAngles(d0, d1, d2, General.RAND.nextFloat() * 360f, 0f);
									
									if(world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(pos).grow(size)).size() >= max) {
										break;
									}
									
									world.addEntity(entity);
									world.playEvent(2004, pos, 0);
									if(entity instanceof MobEntity) {
										((MobEntity) entity).spawnExplosionParticle();
									}
									
									spamt++;
									break;
									
									
								}
							}
							
						}
						
						
						int txc = cost * spamt;
						
						amount -= txc;
						
						fept = (float) txc / (float) nTimer;
						sendUpdates = true;
						
					}
				}
				
				if(sendUpdates) {
					sendUpdates();
				}
			}else {
				if(spawnType != null && client_entity == null) {
					client_entity = spawnType.create(world);
				}else if(spawnType == null && client_entity != null){
					client_entity = null;
				}
			}
			
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot != 0) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}else {
				if(stack.getItem() == Registry.getItem("mob_crystal") && stack.hasTag() && stack.getTag().contains("assemblylinemachines:mob")) {
					return true;
				}
				
				return false;
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public Entity getEntityForRender(){
			return client_entity;
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 1; i < 4; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
		
	}
	
	
	public static class ContainerPoweredSpawner extends ContainerALMBase<TEPoweredSpawner>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 91);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 149);
		
		public ContainerPoweredSpawner(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEPoweredSpawner.class));
		}
		
		public ContainerPoweredSpawner(final int windowId, final PlayerInventory playerInventory, final TEPoweredSpawner tileEntity) {
			super(Registry.getContainerType("powered_spawner"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 4, 0);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 68, 66, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 92, 30, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 92, 48, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 92, 66, tileEntity));
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			ItemStack is = super.slotClick(slotId, dragType, clickTypeIn, player);
			
			tileEntity.spawnType = null;
			tileEntity.sendUpdates();
			return is;
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			ItemStack is = super.transferStackInSlot(playerIn, index);
			
			tileEntity.spawnType = null;
			tileEntity.sendUpdates();
			return is;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenPoweredSpawner extends ScreenALMEnergyBased<ContainerPoweredSpawner>{
		TEPoweredSpawner tsfm;
		
		public ScreenPoweredSpawner(ContainerPoweredSpawner screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 173), null, new Pair<>(11, 81), "powered_spawner", false, new Pair<>(68, 6), screenContainer.tileEntity, true);
			
			renderTitleText = false;
			tsfm = screenContainer.tileEntity;
		}
		
	}
}
