package me.haydenb.assemblylinemachines.block.storage;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockBottomlessStorageUnit extends BlockScreenTileEntity<BlockBottomlessStorageUnit.TEBottomlessStorageUnit> {

	public BlockBottomlessStorageUnit() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "bottomless_storage_unit",
				BlockBottomlessStorageUnit.TEBottomlessStorageUnit.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		
		if(stack.hasTag()) {
			
			CompoundNBT nbt = stack.getTag();
			
			if(world.getTileEntity(pos) instanceof TEBottomlessStorageUnit && nbt.contains("assemblylinemachines:storeditem") && nbt.contains("assemblylinemachines:stored")) {
				
				TEBottomlessStorageUnit te = (TEBottomlessStorageUnit) world.getTileEntity(pos);
				te.internalStored = nbt.getLong("assemblylinemachines:stored");
				te.storedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("assemblylinemachines:storeditem")));
				te.sendUpdates();
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}
	
	public static class BlockItemBottomlessStorageUnit extends BlockItem{

		public BlockItemBottomlessStorageUnit(Block blockIn) {
			super(blockIn, new Item.Properties().group(Registry.creativeTab));
		}
		
		@Override
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			if(stack.hasTag()) {
				
				CompoundNBT nbt = stack.getTag();
				if(nbt.contains("assemblylinemachines:stored") && nbt.contains("assemblylinemachines:storeditem")) {
					tooltip.add(1, new StringTextComponent("This BSU has items stored inside!").func_230532_e_().func_240699_a_(TextFormatting.GREEN));
				}
				
			}
			super.addInformation(stack, worldIn, tooltip, flagIn);
		}
		
	}
	
	public static class TEBottomlessStorageUnit extends AbstractMachine<ContainerBottomlessStorageUnit> implements ITickableTileEntity {

		public TEBottomlessStorageUnit(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 2, new TranslationTextComponent(Registry.getBlock("bottomless_storage_unit").getTranslationKey()),
					Registry.getContainerId("bottomless_storage_unit"), ContainerBottomlessStorageUnit.class);
		}

		public TEBottomlessStorageUnit() {
			this(Registry.getTileEntity("bottomless_storage_unit"));
		}

		private long internalStored = 0;
		private Item storedItem = null;

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
		public void remove() {
			super.remove();
			if (itemHandler != null) {
				itemHandler.invalidate();
			}
		}

		private class InternalStoredExtractHandler extends InvWrapper {

			InternalStoredExtractHandler(IInventory inv) {
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
							}

							sendUpdates();
						}

						return itemstack;
					}
				}

				return ItemStack.EMPTY;
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
			if (!world.isRemote) {
				boolean sendUpdates = false;

				if (internalStored == 0) {
					storedItem = null;
					sendUpdates = true;
				}

				if (!contents.get(1).isEmpty()) {
					try {
						internalStored = Math.addExact(internalStored, contents.get(1).getCount());
						if (storedItem == null) {
							storedItem = contents.get(1).getItem();
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
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (compound.contains("assemblylinemachines:storeditem") && compound.contains("assemblylinemachines:stored")) {
				storedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("assemblylinemachines:storeditem")));
				internalStored = compound.getLong("assemblylinemachines:stored");
			} else {
				storedItem = null;
				internalStored = 0l;
			}

			
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {

			if (storedItem != null && internalStored != 0) {
				compound.putString("assemblylinemachines:storeditem", storedItem.getRegistryName().toString());
				compound.putLong("assemblylinemachines:stored", internalStored);
			}

			
			return super.write(compound);
		}
	}

	public static class ContainerBottomlessStorageUnit extends ContainerALMBase<TEBottomlessStorageUnit> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerBottomlessStorageUnit(final int windowId, final PlayerInventory playerInventory, final TEBottomlessStorageUnit tileEntity) {
			super(Registry.getContainerType("bottomless_storage_unit"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 17, 60, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 17, 10, tileEntity));
		}

		public ContainerBottomlessStorageUnit(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEBottomlessStorageUnit.class));
		}

		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId == 36) {

				if (tileEntity.internalStored != 0 && tileEntity.storedItem != null) {
					if (clickTypeIn == ClickType.PICKUP && player.inventory.getItemStack().isEmpty()) {
						int max = tileEntity.storedItem.getDefaultInstance().getMaxStackSize();
						if (max > tileEntity.internalStored) {
							max = (int) tileEntity.internalStored;
						}
						if (dragType == 0) {

							player.inventory.setItemStack(new ItemStack(tileEntity.storedItem, max));
							reduceInternal(max);

						} else if (dragType == 1) {

							max = Math.round((float) max / 2f);

							player.inventory.setItemStack(new ItemStack(tileEntity.storedItem, max));
							reduceInternal(max);
						}
					} else if (clickTypeIn == ClickType.QUICK_MOVE && dragType == 0) {

						int max = tileEntity.storedItem.getDefaultInstance().getMaxStackSize();
						if (max > tileEntity.internalStored) {
							max = (int) tileEntity.internalStored;
						}

						ItemStack itemstack = new ItemStack(tileEntity.storedItem, max);
						if (this.mergeItemStack(itemstack, 0, 36, false)) {
							max -= itemstack.getCount();
							reduceInternal(max);
						}
					}

				}
				return ItemStack.EMPTY;
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}

		private void reduceInternal(int amt) {

			tileEntity.internalStored -= amt;

			if (tileEntity.internalStored <= 0) {
				tileEntity.storedItem = null;
			}

			tileEntity.sendUpdates();
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenBottomlessStorageUnit extends ScreenALMBase<ContainerBottomlessStorageUnit> {

		TEBottomlessStorageUnit tsfm;
		

		public ScreenBottomlessStorageUnit(ContainerBottomlessStorageUnit screenContainer, PlayerInventory inv, ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "bottomless_storage_unit", false);
			renderTitleText = false;
			renderInventoryText = false;
			tsfm = screenContainer.tileEntity;

		}

		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);

			if(tsfm.storedItem != null){
				String n = tsfm.storedItem.getName().func_230532_e_().getString();
				
				if(n.length() > 30) {
					n = n.substring(0, 30) + "...";
				}
				float wsc = 110f / (float) this.font.getStringWidth(n);
				if(wsc > 3f) wsc = 3f;
				MathHelper.renderScaledText(this.font, 52, 13, wsc, n, false, 0xd4d4d4);
				
				n = Formatting.GENERAL_FORMAT.format(tsfm.internalStored);
				wsc = 110f / (float) this.font.getStringWidth(n);
				if(wsc > 2f) wsc = 2f;
				MathHelper.renderScaledText(this.font, 52, 54, wsc, n, false, 0xd4d4d4);
			}

		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if (tsfm.storedItem != null) {

				this.field_230707_j_.renderItemIntoGUI(tsfm.storedItem.getDefaultInstance(), (x + 17), (y + 60));
				if (tsfm.internalStored < 10000) {
					
					MathHelper.renderItemSlotBoundScaledText(this.font, x+25, y+68, 0.5f, Formatting.GENERAL_FORMAT.format(tsfm.internalStored));
				} else {
					MathHelper.renderItemSlotBoundScaledText(this.font, x+25, y+68, 0.5f, Formatting.formatToSuffix(tsfm.internalStored));
				}
			}

		}
		
		
	}
}
