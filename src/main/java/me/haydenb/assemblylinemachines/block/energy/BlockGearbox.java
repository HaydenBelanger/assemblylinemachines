package me.haydenb.assemblylinemachines.block.energy;

import java.util.Random;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.item.IGearboxFuel;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.StateProperties;
import me.haydenb.assemblylinemachines.registry.Utils;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

public class BlockGearbox extends BlockScreenBlockEntity<BlockGearbox.TEGearbox> {
	
	public BlockGearbox() {
		super(Block.Properties.of(Material.METAL).strength(1f, 2f).sound(SoundType.METAL), "gearbox", TEGearbox.class);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(StateProperties.MACHINE_ACTIVE, false));
	}
	
	

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(HorizontalDirectionalBlock.FACING).add(StateProperties.MACHINE_ACTIVE);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (!worldIn.isClientSide()) {
			if (facing == stateIn.getValue(HorizontalDirectionalBlock.FACING)) {
				if (worldIn.getBlockState(currentPos.relative(facing)).getBlock() == Blocks.AIR) {
					return Blocks.AIR.defaultBlockState();
				}
			}
		}

		return stateIn;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		for(Direction d : Utils.CARDINAL_DIRS) {
			BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(d));
			if(state.getBlock() instanceof ICrankableBlock) {
				if(((ICrankableBlock) state.getBlock()).validSide(state, d.getOpposite())) {
					return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, d);
				}
			}
		}
		return null;
	}
	
	public static class TEGearbox extends SimpleMachine<ContainerGearbox> implements ALMTicker<TEGearbox>{
		
		public int timer = 0;
		public float maxBurnTime = 0;
		public float burnTime = 0;
		public int nTimer = 20;
		public ICrankableMachine mch;
		public TEGearbox(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 2, new TranslatableComponent(Registry.getBlock("gearbox").getDescriptionId()), Registry.getContainerId("gearbox"), ContainerGearbox.class, pos, state);
		}
		
		public TEGearbox(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("gearbox"), pos, state);
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			compound.putFloat("assemblylinemachines:maxburntime", maxBurnTime);
			compound.putFloat("assemblylinemachines:burntime", burnTime);
			compound.putInt("assemblylinemachines:prevtimer", nTimer);
			super.saveAdditional(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			if(compound.contains("assemblylinemachines:maxburntime")) {
				maxBurnTime = compound.getFloat("assemblylinemachines:maxburntime");
			}
			if(compound.contains("assemblylinemachines:burntime")) {
				burnTime = compound.getFloat("assemblylinemachines:burntime");
			}
			if(compound.contains("assemblylinemachines:prevtimer")) {
				nTimer = compound.getInt("assemblylinemachines:prevtimer");
			}
		}
		@Override
		public void tick() {
			
			if(!level.isClientSide) {
				if(timer++ == nTimer) {
					
					boolean machineValid = true;
					if(mch == null) {
						BlockEntity te = getLevel().getBlockEntity(this.getBlockPos().relative(getBlockState().getValue(HorizontalDirectionalBlock.FACING)));
						if(te != null && te instanceof ICrankableMachine) {
							mch = (ICrankableMachine) te;
						}else {
							machineValid = false;
						}
					}
					
					if(machineValid == true) {
						boolean sendUpdate = false;
						timer = 0;
						
						Upgrades u = Upgrades.match(contents.get(0));
						int mul;
						switch (u) {
						case UNIVERSAL_SPEED:
							nTimer = 10;
							mul = 40;
							break;
						case GB_EFFICIENCY:
							nTimer = 40;
							mul = 10;
							break;
						default:
							nTimer = 20;
							mul = 20;
							break;
						}
						
						if(burnTime != 0) {
							
							
							if(mch.perform()) {
								burnTime = burnTime - mul;
								sendUpdate = true;
								getLevel().playSound(null, this.getBlockPos(), SoundEvents.WOOD_STEP, SoundSource.BLOCKS, 0.4f, 1f + getPitchNext(getLevel().getRandom()));
								
							}else {
								if(u != Upgrades.GB_LIMITER) {
									burnTime = burnTime - mul;
									sendUpdate = true;
									getLevel().playSound(null, this.getBlockPos(), SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.4f, 1f);
								}
							}
							
							
							
						}
						
						if(burnTime <= 0) {
							
							if(contents.get(1) != ItemStack.EMPTY) {
								burnTime = ForgeHooks.getBurnTime(contents.get(1), RecipeType.SMELTING);
								maxBurnTime = burnTime;
								contents.get(1).shrink(1);
								sendUpdate = true;
							}else {
								if(maxBurnTime != 0) {
									burnTime = 0;
									maxBurnTime = 0;
									sendUpdate = true;
								}
							}
						}
						
						
						if(burnTime == 0 && getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
							sendUpdate = true;
						}else if(burnTime != 0 && !getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
							sendUpdate = true;
						}
						if(sendUpdate) {
							sendUpdates();
						}
					}
					
				}
			}
			
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot == 0) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
			}else {
				if(Upgrades.match(contents.get(0)) == Upgrades.GB_COMPATABILITY) {
					if(ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) != 0) {
						return true;
					}
				}else {
					if(stack.getItem() instanceof IGearboxFuel) {
						return true;
					}
				}
				
			}
			return false;
		}
		
		
		@Override
		public NonNullList<ItemStack> getItems() {
			if(contents.get(1) != ItemStack.EMPTY && contents.get(1).getItem() != Items.AIR && !(contents.get(1).getItem() instanceof IGearboxFuel)) {
				if(Upgrades.match(contents.get(0)) != Upgrades.GB_COMPATABILITY) {
					Utils.spawnItem(contents.get(1), this.getBlockPos().above(), level);
					contents.set(1, ItemStack.EMPTY);
					burnTime = 0;
					sendUpdates();
				}
			}
			return super.getItems();
		}
	}
	
	public static class ContainerGearbox extends ContainerALMBase<TEGearbox>{
		
		private static final Pair<Integer, Integer> UPGRADE_POS = new Pair<>(55, 34);
		private static final Pair<Integer, Integer> INPUT_POS = new Pair<>(75, 34);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerGearbox(final int windowId, final Inventory playerInventory, final TEGearbox tileEntity) {
			super(Registry.getContainerType("gearbox"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, UPGRADE_POS.getFirst(), UPGRADE_POS.getSecond(), tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, INPUT_POS.getFirst(), INPUT_POS.getSecond(), tileEntity));
		}
		
		
		public ContainerGearbox(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEGearbox.class));
		}
		
		
		
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenGearbox extends ScreenALMBase<ContainerGearbox>{
		
		TEGearbox tsfm;
		
		public ScreenGearbox(ContainerGearbox screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "gearbox", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			if(tsfm.maxBurnTime > 0) {
				int x = (this.width - this.imageWidth) / 2;
				int y = (this.height - this.imageHeight) / 2;
				super.blit(x+29, y+33, 176, 0, 18, 18);
				super.blit(x+119, y+33, 176, 0, 18, 18);
				
				int prog = Math.round((tsfm.burnTime/tsfm.maxBurnTime) * 13f);
				super.blit(x+77, y+18 + (13 - prog), 176, 18 + (13 - prog), 13, prog);
			}
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		}
	}
	
	private static float getPitchNext(Random rand) {
		float f = rand.nextFloat();
		
		if(f < 0.6f) {
			f = 0f;
		}
		
		if(f > 0.3f) {
			f = f * -1f;
		}
		
		return f;
		
	}

}
