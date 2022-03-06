package me.haydenb.assemblylinemachines.block.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine.ManagedDirection;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.StateProperties.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockFluidRouter extends BlockScreenBlockEntity<BlockFluidRouter.TEFluidRouter>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 13, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 3, 0, 3, 13, 16),
			Block.box(13, 3, 0, 16, 13, 16),
			Block.box(3, 3, 8, 13, 13, 16),
			Block.box(3, 4, 4, 13, 12, 8)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockFluidRouter() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "fluid_router", BlockFluidRouter.TEFluidRouter.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
		if (d == Direction.WEST) {
			return SHAPE_W;
		} else if (d == Direction.SOUTH) {
			return SHAPE_S;
		} else if (d == Direction.EAST) {
			return SHAPE_E;
		} else {
			return SHAPE_N;
		}
	}
	
	public static class TEFluidRouter extends AbstractMachine<ContainerFluidRouter>{
		
		private Fluid fluidL = Fluids.EMPTY;
		private Fluid fluidR = Fluids.EMPTY;
		private FluidStack tank = FluidStack.EMPTY;
		private IFluidHandler handlerL = new FluidRouterHandler(ManagedDirection.LEFT);
		private IFluidHandler handlerR = new FluidRouterHandler(ManagedDirection.RIGHT);
		private IFluidHandler handlerX = new FluidRouterHandler(null);
		private LazyOptional<IFluidHandler> lazyl = LazyOptional.of(() -> handlerL);
		private LazyOptional<IFluidHandler> lazyr = LazyOptional.of(() -> handlerR);
		private LazyOptional<IFluidHandler> lazyx = LazyOptional.of(() -> handlerX);
		
		
		public TEFluidRouter(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 2, new TranslatableComponent(Registry.getBlock("fluid_router").getDescriptionId()), Registry.getContainerId("fluid_router"), ContainerFluidRouter.class, pos, state);
		}
		
		public TEFluidRouter(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("fluid_router"), pos, state);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				Direction facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
				if(side == facing.getClockWise()) {
					return lazyr.cast();
				}else if(side == facing.getCounterClockWise()) {
					return lazyl.cast();
				}else if(side == facing.getOpposite()) {
					return lazyx.cast();
				}
			}
			
			return LazyOptional.empty();
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:fluidl")) {
				fluidL = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(compound.getString("assemblylinemachines:fluidl")));
			}
			if(compound.contains("assemblylinemachines:fluidr")) {
				fluidR = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(compound.getString("assemblylinemachines:fluidr")));
			}
			
			if(compound.contains("assemblylinemachines:tank")) {
				tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tank"));
			}
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			compound.putString("assemblylinemachines:fluidl", fluidL.getRegistryName().toString());
			compound.putString("assemblylinemachines:fluidr", fluidR.getRegistryName().toString());
			CompoundTag sub = new CompoundTag();
			tank.writeToNBT(sub);
			compound.put("assemblylinemachines:tank", sub);
			super.saveAdditional(compound);
		}
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return LazyOptional.empty();
		}
		
		private class FluidRouterHandler implements IFluidHandler{
			
			private final ManagedDirection d;
			FluidRouterHandler(ManagedDirection d){
				this.d = d;
			}
			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				return true;
			}
			
			@Override
			public int getTanks() {
				return 1;
			}
			
			@Override
			public int getTankCapacity(int tank) {
				return 6000;
			}
			
			@Override
			public FluidStack getFluidInTank(int tank) {
				return TEFluidRouter.this.tank;
			}
			
			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if(d != null) {
					return 0;
				}
				if (!tank.isEmpty()) {
					if (resource.getFluid() != tank.getFluid()) {
						return 0;
					}
				}
				
				int attemptedInsert = resource.getAmount();
				int rmCapacity = getTankCapacity(0) - tank.getAmount();
				if (rmCapacity < attemptedInsert) {
					attemptedInsert = rmCapacity;
				}

				if (action != FluidAction.SIMULATE) {
					if (tank.isEmpty()) {
						tank = resource;
					} else {
						tank.setAmount(tank.getAmount() + attemptedInsert);
					}
				}

				sendUpdates();
				return attemptedInsert;
			}

			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {

				if(d == null) {
					return FluidStack.EMPTY;
				}else if(d == ManagedDirection.LEFT && !tank.getFluid().equals(fluidL)) {
					return FluidStack.EMPTY;
				}else if(d == ManagedDirection.RIGHT && !tank.getFluid().equals(fluidR)) {
					return FluidStack.EMPTY;
				}
				
				if (tank.getAmount() < maxDrain) {
					maxDrain = tank.getAmount();
				}

				Fluid f = tank.getFluid();
				if (action != FluidAction.SIMULATE) {
					tank.setAmount(tank.getAmount() - maxDrain);
				}

				if (tank.getAmount() <= 0) {
					tank = FluidStack.EMPTY;

				}

				sendUpdates();
				return new FluidStack(f, maxDrain);
			}

			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return false;
		}
	}
	
	public static class ContainerFluidRouter extends ContainerALMBase<TEFluidRouter>{
		
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 84);
		
		public ContainerFluidRouter(final int windowId, final Inventory playerInventory, final TEFluidRouter tileEntity) {
			super(Registry.getContainerType("fluid_router"), windowId, tileEntity, playerInventory, null, PLAYER_HOTBAR_POS, 2, 0);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 62, 35, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 98, 35, tileEntity));
		}
		
		public ContainerFluidRouter(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEFluidRouter.class));
		}
		
		@Override
		public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
			
			if(slot == 9 || slot == 10) {
				ItemStack stack = this.getCarried();
				
				if(stack.isEmpty() || stack.getItem() == Items.AIR) {
					if(slot == 9) {
						tileEntity.fluidL = Fluids.EMPTY;
					}else {
						tileEntity.fluidR = Fluids.EMPTY;
					}
					
				}else {
					FluidStack fs = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
					if(!fs.isEmpty()) {
						if(slot == 9) {
							tileEntity.fluidL = fs.getFluid();
						}else {
							tileEntity.fluidR = fs.getFluid();
						}
					}
				}
			}
			
			super.clicked(slot, dragType, clickTypeIn, player);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenFluidRouter extends ScreenALMBase<ContainerFluidRouter>{
		
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TEFluidRouter tsfm;
		
		public ScreenFluidRouter(ContainerFluidRouter screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 108), new Pair<>(11, 6), new Pair<>(11, 73), "fluid_router", false);
			
			renderInventoryText = false;
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(!tsfm.tank.isEmpty() && tsfm.tank.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(tsfm.tank.getFluid());
				if(tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tsfm.tank.getFluid().getAttributes().getStillTexture());
				}
				
				if(tsfm.tank.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				}
				
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
				
				super.blit(x+84, y+15, 57, 57, 57, tas);
			}
			
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			
			int fprog = Math.round(((float)tsfm.tank.getAmount()/(float)tsfm.handlerX.getTankCapacity(0)) * 57f);
			super.blit(x+84, y+15, 176, 0, 8, 57 - fprog);
			
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			renderFluidIcon(x+62, y+35, tsfm.fluidL);
			renderFluidIcon(x+98, y+35, tsfm.fluidR);
		}
		
		@Override
		protected void init() {
			super.init();
			
			int x = leftPos;
			int y = topPos;
			
			this.addRenderableWidget(new TrueFalseButton(x+84, y+15, 8, 57, null, (b) ->{
				if(Screen.hasShiftDown()) {
					sendSetFilter(tsfm.getBlockPos(), false);
				}else {
					sendSetFilter(tsfm.getBlockPos(), true);
				}
			}));
		}
		
		private void renderFluidIcon(int x, int y, Fluid fluid) {
			if(fluid != Fluids.EMPTY) {
				TextureAtlasSprite tas = spriteMap.get(fluid);
				if(tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture());
					spriteMap.put(fluid, tas);
				}
				
				if(fluid == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				}
				
				super.blit(x, y, 16, 16, 16, tas);
			}
			
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			this.font.draw(mx, new TextComponent("Hotbar"), invTextLoc.getFirst(),
					invTextLoc.getSecond(), 4210752);
			
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if (mouseX >= x+84 && mouseY >= y+15 && mouseX <= x+91 && mouseY <= y+71) {
				if (!tsfm.tank.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(tsfm.tank.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(Formatting.FEPT_FORMAT.format(tsfm.tank.getAmount()) + " mB");
					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) tsfm.tank.getAmount() / 1000D) + " B");
					}

					str.add("Left-Click to set as LEFT filter.");
					str.add("Shift Left-Click to set as RIGHT filter.");
					this.renderComponentTooltip(str, mouseX - x, mouseY - y);
				} else {
					this.renderComponentTooltip("Empty", mouseX - x, mouseY - y);
				}
			}
			
			renderFluidTooltip(tsfm.fluidL, mouseX, mouseY, x + 61, y + 34, x, y);
			renderFluidTooltip(tsfm.fluidR, mouseX, mouseY, x + 97, y + 34, x, y);
			
			
		}
		
		private void renderFluidTooltip(Fluid fluid, int mouseX, int mouseY, int mminx, int mminy, int x, int y) {
			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 17 && mouseY <= mminy + 17) {
				
				if(fluid != Fluids.EMPTY) {
					this.renderComponentTooltip(fluid.getAttributes().getDisplayName(FluidStack.EMPTY).getString(), mouseX - x, mouseY - y);
				}
			}
		}
	}
	
	
	
	private static void sendSetFilter(BlockPos pos, boolean left) {
		PacketData pd = new PacketData("fluid_router_gui");
		pd.writeBlockPos("pos", pos);
		pd.writeBoolean("left", left);
		PacketHandler.INSTANCE.sendToServer(pd);
	}
	
	public static void setFilter(PacketData pd, Level world) {
		if (world.getBlockEntity(pd.get("pos", BlockPos.class)) instanceof TEFluidRouter) {
			TEFluidRouter tef = (TEFluidRouter) world.getBlockEntity(pd.get("pos", BlockPos.class));
			
			
			if(!tef.tank.isEmpty()) {
				if(pd.get("left", Boolean.class)) {
					tef.fluidL = tef.tank.getFluid();
				}else {
					tef.fluidR = tef.tank.getFluid();
				}
				tef.sendUpdates();
			}
			
		}
	}
}
