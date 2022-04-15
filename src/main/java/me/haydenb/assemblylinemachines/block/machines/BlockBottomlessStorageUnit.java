package me.haydenb.assemblylinemachines.block.machines;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockBottomlessStorageUnit extends BlockScreenBlockEntity<BlockBottomlessStorageUnit.TEBottomlessStorageUnit> {

	public BlockBottomlessStorageUnit() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "bottomless_storage_unit",
				BlockBottomlessStorageUnit.TEBottomlessStorageUnit.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		
		if(stack.hasTag()) {
			
			CompoundTag nbt = stack.getTag();
			
			if(world.getBlockEntity(pos) instanceof TEBottomlessStorageUnit && nbt.contains("assemblylinemachines:storeditem") && nbt.contains("assemblylinemachines:stored") && nbt.contains("assemblylinemachines:storedprettyname")) {
				
				TEBottomlessStorageUnit te = (TEBottomlessStorageUnit) world.getBlockEntity(pos);
				te.internalStored = nbt.getLong("assemblylinemachines:stored");
				te.storedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("assemblylinemachines:storeditem")));
				te.storedItemPrettyName = nbt.getString("assemblylinemachines:storedprettyname");
				te.sendUpdates();
			}
		}
		super.setPlacedBy(world, pos, state, placer, stack);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if(stack.hasTag()) {
			
			CompoundTag nbt = stack.getTag();
			if(nbt.contains("assemblylinemachines:stored") && nbt.contains("assemblylinemachines:storeditem") && nbt.contains("assemblylinemachines:storedprettyname")) {
				tooltip.add(1, new TextComponent("This BSU has " + FormattingHelper.formatToSuffix(nbt.getLong("assemblylinemachines:stored")) + " of " + nbt.getString("assemblylinemachines:storedprettyname") + " stored.").withStyle(ChatFormatting.GREEN));
			}
			
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
	
	public static class TEBottomlessStorageUnit extends AbstractMachine<ContainerBottomlessStorageUnit> implements ALMTicker<TEBottomlessStorageUnit> {

		public TEBottomlessStorageUnit(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 2, new TranslatableComponent(Registry.getBlock("bottomless_storage_unit").getDescriptionId()),
					Registry.getContainerId("bottomless_storage_unit"), ContainerBottomlessStorageUnit.class, pos, state);
		}

		public TEBottomlessStorageUnit(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("bottomless_storage_unit"), pos, state);
		}

		private long internalStored = 0;
		private Item storedItem = null;
		private String storedItemPrettyName = null;

		protected InternalStoredExtractHandler items = new InternalStoredExtractHandler(this);
		protected LazyOptional<InternalStoredExtractHandler> itemHandler = LazyOptional.of(() -> items);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return itemHandler.cast();
			}

			return LazyOptional.empty();
		}

		@Override
		public void setRemoved() {
			super.setRemoved();
			if (itemHandler != null) {
				itemHandler.invalidate();
			}
		}

		private class InternalStoredExtractHandler extends InvWrapper {

			InternalStoredExtractHandler(Container inv) {
				super(inv);
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				
				if (slot == 0) {
					if (internalStored != 0 && storedItem != null) {
						int max = storedItem.getDefaultInstance().getMaxStackSize();
						if (max > internalStored) {
							max = (int) internalStored;
						}

						if (max > amount) {
							max = amount;
						}
						ItemStack itemstack = new ItemStack(storedItem, max);

						if (simulate == false) {
							internalStored -= max;

							if (internalStored <= 0) {
								storedItem = null;
								storedItemPrettyName = null;
							}

							sendUpdates();
						}

						return itemstack;
					}
				}

				return ItemStack.EMPTY;
			}
			
			@Override
			public ItemStack getStackInSlot(int slot) {
				if(slot == 0) {
					if(internalStored != 0 && storedItem != null) {
					
						int amt = storedItem.getDefaultInstance().getMaxStackSize();
						if(amt >= internalStored) {
							amt = (int) internalStored;
						}
						
						return new ItemStack(storedItem, amt);
					}
				}
				return super.getStackInSlot(slot);
			}
		}

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return this.getCapability(cap);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if (slot == 1) {
				if (!stack.isDamaged() && !stack.hasTag()) {

					if (storedItem == null || storedItem == stack.getItem()) {
						return true;
					}

				}
			}
			return false;
		}

		@Override
		public void tick() {
			if (!level.isClientSide) {
				boolean sendUpdates = false;

				if (internalStored == 0) {
					storedItem = null;
					storedItemPrettyName = null;
					sendUpdates = true;
				}

				if (!contents.get(1).isEmpty()) {
					try {
						internalStored = Math.addExact(internalStored, contents.get(1).getCount());
						if (storedItem == null) {
							storedItem = contents.get(1).getItem();
							storedItemPrettyName = storedItem.getDefaultInstance().getHoverName().getString();
						}
						contents.set(1, ItemStack.EMPTY);
						sendUpdates = true;
					} catch (ArithmeticException e) {
					}
					;
				}

				if (sendUpdates) {
					sendUpdates();
				}

			}
		}

		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			if (compound.contains("assemblylinemachines:storeditem") && compound.contains("assemblylinemachines:stored") && compound.contains("assemblylinemachines:storedprettyname")) {
				storedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("assemblylinemachines:storeditem")));
				internalStored = compound.getLong("assemblylinemachines:stored");
				storedItemPrettyName = compound.getString("assemblylinemachines:storedprettyname");
			} else {
				storedItem = null;
				internalStored = 0l;
			}

			
		}

		@Override
		public void saveAdditional(CompoundTag compound) {

			if (storedItem != null && internalStored != 0 && storedItemPrettyName != null) {
				compound.putString("assemblylinemachines:storeditem", storedItem.getRegistryName().toString());
				compound.putLong("assemblylinemachines:stored", internalStored);
				compound.putString("assemblylinemachines:storedprettyname", storedItemPrettyName);
			}

			
			super.saveAdditional(compound);
		}
	}

	public static class ContainerBottomlessStorageUnit extends ContainerALMBase<TEBottomlessStorageUnit> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerBottomlessStorageUnit(final int windowId, final Inventory playerInventory, final TEBottomlessStorageUnit tileEntity) {
			super(Registry.getContainerType("bottomless_storage_unit"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 17, 60, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 17, 10, tileEntity));
		}

		public ContainerBottomlessStorageUnit(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEBottomlessStorageUnit.class));
		}

		@Override
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (slotId == 36) {

				if (tileEntity.internalStored != 0 && tileEntity.storedItem != null) {
					if (clickTypeIn == ClickType.PICKUP && this.getCarried().isEmpty()) {
						int max = tileEntity.storedItem.getDefaultInstance().getMaxStackSize();
						if (max > tileEntity.internalStored) {
							max = (int) tileEntity.internalStored;
						}
						if (dragType == 0) {

							this.setCarried(new ItemStack(tileEntity.storedItem, max));
							reduceInternal(max);

						} else if (dragType == 1) {

							max = Math.round((float) max / 2f);

							this.setCarried(new ItemStack(tileEntity.storedItem, max));
							reduceInternal(max);
						}
					} else if (clickTypeIn == ClickType.QUICK_MOVE && dragType == 0) {

						int max = tileEntity.storedItem.getDefaultInstance().getMaxStackSize();
						if (max > tileEntity.internalStored) {
							max = (int) tileEntity.internalStored;
						}

						ItemStack itemstack = new ItemStack(tileEntity.storedItem, max);
						if (this.moveItemStackTo(itemstack, 0, 36, false)) {
							max -= itemstack.getCount();
							reduceInternal(max);
						}
					}

				}
			}
			
			super.clicked(slotId, dragType, clickTypeIn, player);
		}

		private void reduceInternal(int amt) {

			tileEntity.internalStored -= amt;

			if (tileEntity.internalStored <= 0) {
				tileEntity.storedItem = null;
				tileEntity.storedItemPrettyName = null;
			}

			tileEntity.sendUpdates();
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenBottomlessStorageUnit extends ScreenALMBase<ContainerBottomlessStorageUnit> {

		TEBottomlessStorageUnit tsfm;
		

		public ScreenBottomlessStorageUnit(ContainerBottomlessStorageUnit screenContainer, Inventory inv, Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "bottomless_storage_unit", false);
			renderTitleText = false;
			renderInventoryText = false;
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			if(tsfm.storedItem != null){
				String n = tsfm.storedItem.getDescription().getString();
				
				if(n.length() > 30) {
					n = n.substring(0, 30) + "...";
				}
				float wsc = 110f / (float) this.font.width(n);
				if(wsc > 3f) wsc = 3f;
				ScreenMath.renderScaledText(this.font, 52, 13, wsc, n, false, 0xd4d4d4);
				
				n = FormattingHelper.GENERAL_FORMAT.format(tsfm.internalStored);
				wsc = 110f / (float) this.font.width(n);
				if(wsc > 2f) wsc = 2f;
				ScreenMath.renderScaledText(this.font, 52, 54, wsc, n, false, 0xd4d4d4);
			}

		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if (tsfm.storedItem != null) {

				this.itemRenderer.renderGuiItem(tsfm.storedItem.getDefaultInstance(), (x + 17), (y + 60));
				if (tsfm.internalStored < 10000) {
					
					ScreenMath.renderItemSlotBoundScaledText(this.font, x+25, y+68, 0.5f, FormattingHelper.GENERAL_FORMAT.format(tsfm.internalStored));
				} else {
					ScreenMath.renderItemSlotBoundScaledText(this.font, x+25, y+68, 0.5f, FormattingHelper.formatToSuffix(tsfm.internalStored));
				}
			}

		}
		
		
	}
}
