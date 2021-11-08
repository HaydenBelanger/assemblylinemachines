package me.haydenb.assemblylinemachines.block.machines.mob;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockPoweredSpawner extends BlockScreenBlockEntity<BlockPoweredSpawner.TEPoweredSpawner>{

	public BlockPoweredSpawner() {
		super(Block.Properties.of(Material.METAL).noOcclusion().dynamicShape().strength(4f, 15f).sound(SoundType.METAL), "powered_spawner", BlockPoweredSpawner.TEPoweredSpawner.class);
	}
	
	
	
	public static class TEPoweredSpawner extends ManagedSidedMachine<ContainerPoweredSpawner> implements ALMTicker<TEPoweredSpawner>{
		
		
		private EntityType<?> spawnType = null;
		private int timer = 0;
		private int nTimer = 160;
		private ServerLevel sw = null;
	
		private Entity client_entity = null;
		public float client_renderRot = 0f;
		
		public TEPoweredSpawner(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 4, new TranslatableComponent(Registry.getBlock("powered_spawner").getDescriptionId()), Registry.getContainerId("powered_spawner"), ContainerPoweredSpawner.class, new EnergyProperties(true, false, 100000), pos, state);
		}
		
		public TEPoweredSpawner(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("powered_spawner"), pos, state);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:spawntype")) {
				spawnType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(compound.getString("assemblylinemachines:spawntype")));
			}
			nTimer = compound.getInt("assemblylinemachines:ntimer");
		}
		
		@Override
		public CompoundTag save(CompoundTag compound) {
			
			if(spawnType != null) {
				compound.putString("assemblylinemachines:spawntype", spawnType.getRegistryName().toString());
			}
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			return super.save(compound);
		}
		
		@Override
		public boolean canExtractFromSide(int slot, Direction direction) {
			return false;
		}
		

		@Override
		public void tick() {


			if(!level.isClientSide) {
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
						int rand = Utils.RAND.nextInt((ug * 2) + 3) + ug;
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
						
						if(sw == null) {
							sw = this.getLevel().getServer().getLevel(this.getLevel().dimension());
						}
						for(int i = 0; i < rand; i++) {
							
							if(cost * (spamt + 1) > amount) {
								break;
							}
							int size = 5;
							int max = 5;
							double d0 = (double)this.getBlockPos().getX() + (level.random.nextDouble() - level.random.nextDouble()) * (double) size + 0.5D;
							double d1 = (double)(this.getBlockPos().getY() + level.random.nextInt(3) - 1);
							double d2 = (double)this.getBlockPos().getZ() + (level.random.nextDouble() - level.random.nextDouble()) * (double) size + 0.5D;
							
							
							for(int j = 0; j < 10; j++) {
								
								if(this.getLevel().noCollision(spawnType.getAABB(d0, d1, d2)) && SpawnPlacements.checkSpawnRules(spawnType, sw, MobSpawnType.SPAWNER, new BlockPos(d0, d1, d2), this.getLevel().getRandom())) {
									Entity entity = spawnType.create(this.getLevel());
									entity.moveTo(d0, d1, d2, Utils.RAND.nextFloat() * 360f, 0f);
									
									if(this.getLevel().getEntitiesOfClass(entity.getClass(), new AABB(this.getBlockPos()).inflate(size)).size() >= max) {
										break;
									}
									
									this.getLevel().addFreshEntity(entity);
									this.getLevel().levelEvent(2004, this.getBlockPos(), 0);
									if(entity instanceof Mob) {
										((Mob) entity).spawnAnim();
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
					client_entity = spawnType.create(this.getLevel());
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
		
		public ContainerPoweredSpawner(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEPoweredSpawner.class));
		}
		
		public ContainerPoweredSpawner(final int windowId, final Inventory playerInventory, final TEPoweredSpawner tileEntity) {
			super(Registry.getContainerType("powered_spawner"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 4, 0);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 68, 66, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 92, 30, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 92, 48, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 92, 66, tileEntity));
		}
		
		@Override
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			super.clicked(slotId, dragType, clickTypeIn, player);
			
			tileEntity.spawnType = null;
			tileEntity.sendUpdates();
		}
		
		@Override
		public ItemStack quickMoveStack(Player playerIn, int index) {
			ItemStack is = super.quickMoveStack(playerIn, index);
			
			tileEntity.spawnType = null;
			tileEntity.sendUpdates();
			return is;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenPoweredSpawner extends ScreenALMEnergyBased<ContainerPoweredSpawner>{
		TEPoweredSpawner tsfm;
		
		public ScreenPoweredSpawner(ContainerPoweredSpawner screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 173), null, new Pair<>(11, 81), "powered_spawner", false, new Pair<>(68, 6), screenContainer.tileEntity, true);
			
			renderTitleText = false;
			tsfm = screenContainer.tileEntity;
		}
		
	}
}
