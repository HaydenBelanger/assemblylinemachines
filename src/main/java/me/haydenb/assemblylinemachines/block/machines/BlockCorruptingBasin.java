package me.haydenb.assemblylinemachines.block.machines;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.*;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.crafting.WorldCorruptionCrafting;
import me.haydenb.assemblylinemachines.item.ItemCorruptedShard;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties.BathCraftingFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockCorruptingBasin extends BlockScreenBlockEntity<BlockCorruptingBasin.TECorruptingBasin>{

	
	private static final VoxelShape SHAPE = Stream.of(Block.box(1, 0, 1, 15, 16, 2),
			Block.box(1, 0, 14, 15, 16, 15), Block.box(1, 0, 2, 2, 16, 14),
			Block.box(14, 0, 2, 15, 16, 14), Block.box(2, 0, 2, 14, 1, 14)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();
	
	public BlockCorruptingBasin() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "corrupting_basin", BlockCorruptingBasin.TECorruptingBasin.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	
	public static class TECorruptingBasin extends SimpleMachine<ContainerCorruptingBasin> implements ALMTicker<TECorruptingBasin>{
		
		public int timer;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		
		public FluidStack tank = FluidStack.EMPTY;
		
		
		public IFluidHandler handler = IFluidHandlerBypass.getSimpleOneTankHandler((fs) -> fs.getFluid().equals(Registry.getFluid("condensed_void")), 4000, (oFs) -> {
			if(oFs.isPresent()) tank = oFs.get();
			return tank;
		}, (v) -> this.sendUpdates(), false);
		
		protected LazyOptional<IFluidHandler> lazy = LazyOptional.of(() -> handler);
		
		public TECorruptingBasin(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 3, new TranslatableComponent(Registry.getBlock("corrupting_basin").getDescriptionId()), Registry.getContainerId("corrupting_basin"), ContainerCorruptingBasin.class, true, pos, state);
		}
		
		public TECorruptingBasin(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("corrupting_basin"), pos, state);
		}
		
		@Override
		public void tick() {
			if(timer++ == 10) {
				timer = 0;
				if(!level.isClientSide) {
					boolean sendUpdates = false;
					
					if((output == null || output.isEmpty()) && !contents.get(2).isEmpty() && tank.getAmount() >= 50) {
						Optional<WorldCorruptionCrafting> rOpt = this.getLevel().getRecipeManager().getRecipeFor(WorldCorruptionCrafting.WORLD_CORRUPTION_RECIPE, this, this.getLevel());
						if(rOpt.isPresent() && tank.getAmount() >= 100) {
							WorldCorruptionCrafting wcc = rOpt.get();
							output = wcc.getRandom(this.getLevel().getRandom()).asItem().getDefaultInstance().copy();
							cycles = 16;
							tank.shrink(100);
							contents.get(2).shrink(1);
							sendUpdates = true;
						}else if(!contents.get(2).getItem().equals(Registry.getItem("corrupted_shard")) && tank.getAmount() >= (contents.get(2).getCount() * 50)){
							output = ItemCorruptedShard.corruptItem(contents.get(2));
							cycles = 8 * output.getCount();
							tank.shrink(50 * output.getCount());
							contents.get(2).shrink(output.getCount());
							sendUpdates = true;
						}
					}else if(output != null && !output.isEmpty()) {
						if(progress++ >= cycles) {
							int targetOutput = output.getItem().equals(Registry.getItem("corrupted_shard")) ? 1 : 0;
							ItemStack target = contents.get(targetOutput);
							if(target.isEmpty() || (ItemHandlerHelper.canItemStacksStack(target, output) && target.getCount() + output.getCount() <= target.getMaxStackSize())){
								if(target.isEmpty()) {
									contents.set(targetOutput, output);
								}else {
									contents.get(targetOutput).grow(output.getCount());
								}
								output = null;
								progress = cycles = 0;
							}
						}
						sendUpdates = true;
					}
					if(!this.getBlockState().getValue(StateProperties.MACHINE_ACTIVE).equals(tank.getFluid().equals(Registry.getFluid("condensed_void")))) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(StateProperties.MACHINE_ACTIVE, tank.getFluid().equals(Registry.getFluid("condensed_void"))));
						sendUpdates = true;
					}
					
					if(sendUpdates) {
						sendUpdates();
					}
					
					
				}
			}
			
		}
		
		@Override
		public boolean canBeExtracted(ItemStack stack, int slot) {
			return slot < 2;
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			CompoundTag tank = new CompoundTag();
			this.tank.writeToNBT(tank);
			compound.put("assemblylinemachines:tank", tank);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			if(output != null) {
				CompoundTag output = new CompoundTag();
				this.output.save(output);
				compound.put("assemblylinemachines:output", output);
			}
			super.saveAdditional(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:tank")) tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tank"));
			if(compound.contains("assemblylinemachines:output")) output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			progress = compound.getFloat("assemblylinemachines:progress");
			cycles = compound.getFloat("assemblylinemachines:cycles");
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return slot == 2;
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return lazy.cast();
			}
			return super.getCapability(cap, side);
		}
	}
	
	public static class ContainerCorruptingBasin extends ContainerALMBase<TECorruptingBasin>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerCorruptingBasin(final int windowId, final Inventory playerInventory, final TECorruptingBasin tileEntity) {
			super(Registry.getContainerType("corrupting_basin"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 2);
			
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 0, 113, 35, tileEntity, true));
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 1, 134, 35, tileEntity, true));
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 2, 65, 35, tileEntity));
			
			
		}
		
		public ContainerCorruptingBasin(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TECorruptingBasin.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenCorruptingBasin extends ScreenALMBase<ContainerCorruptingBasin>{
		
		TECorruptingBasin tsfm;
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		
		public ScreenCorruptingBasin(ContainerCorruptingBasin screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "corrupting_basin", false);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			renderFluid(tsfm.tank, x+52, y+24);
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			renderFluidOverlayBar(tsfm.tank, tsfm.handler.getTankCapacity(0), x+52, y+24);
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
			super.blit(x+85, y+34, 176, 37, prog, 18);
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			renderFluidTooltip(tsfm.tank, mouseX, mouseY, x+52, y+24, x, y);
		}
		
		
		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				}

				super.blit(xblit, yblit, 37, 37, 37, tas);
			}
		}
		
		private void renderFluidOverlayBar(FluidStack fs, float capacity, int xblit, int yblit) {
			int fprog = Math.round(((float) fs.getAmount() / capacity) * 37f);
			super.blit(xblit, yblit, 176, 0, 8, 37 - fprog);
		}
		
		private void renderFluidTooltip(FluidStack fs, int mouseX, int mouseY, int mminx, int mminy, int bx, int by) {

			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 7 && mouseY <= mminy + 36) {
				if (!fs.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(fs.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(FormattingHelper.FEPT_FORMAT.format(fs.getAmount()) + " mB");
					} else {
						str.add(FormattingHelper.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderComponentTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderComponentTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
	}
}
