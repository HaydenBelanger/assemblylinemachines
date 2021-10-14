package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.registry.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockElectricFluidMixer extends BlockScreenBlockEntity<BlockElectricFluidMixer.TEElectricFluidMixer>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 14, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 2, 16),
			Block.box(0, 2, 1, 16, 14, 16),
			Block.box(0, 5, 0, 16, 14, 1),
			Block.box(0, 2, 0, 3, 5, 1),
			Block.box(13, 2, 0, 16, 5, 1),
			Block.box(9, 2, 0, 12, 5, 1),
			Block.box(4, 2, 0, 7, 5, 1),
			Block.box(7, 4, 0, 9, 5, 1)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	private static final Random RAND = new Random();
	
	public BlockElectricFluidMixer() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "electric_fluid_mixer", BlockElectricFluidMixer.TEElectricFluidMixer.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.FLUID, BathCraftingFluids.NONE).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.FLUID).add(HorizontalDirectionalBlock.FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public InteractionResult blockRightClickServer(BlockState state, Level world, BlockPos pos, Player player) {
		ItemStack stack = player.getMainHandItem();
		if(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null) != null && world.getBlockEntity(pos) instanceof TEElectricFluidMixer) {
			
			IFluidHandler handler = ((TEElectricFluidMixer) world.getBlockEntity(pos)).fluids;
			FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
			if(far.isSuccess()) {
				if(stack.getCount() == 1) {
					player.getInventory().removeItemNoUpdate(player.getInventory().selected);
				}else {
					stack.shrink(1);
				}
				ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
				return InteractionResult.CONSUME;
			}
			
			
		}
		return super.blockRightClickServer(state, world, pos, player);
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
	
	public static class TEElectricFluidMixer extends ManagedSidedMachine<ContainerElectricFluidMixer> implements ALMTicker<TEElectricFluidMixer>{
		
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private BathCraftingFluids inProgress = BathCraftingFluids.NONE;
		private FluidStack fluid = FluidStack.EMPTY;
		private boolean externalTank = false;
		private IFluidHandler extHandler = null;
		
		protected IFluidHandler fluids = new MixerHandler();
		
		protected LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> fluids);
		
		public TEElectricFluidMixer(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 6, new TranslatableComponent(Registry.getBlock("electric_fluid_mixer").getDescriptionId()), Registry.getContainerId("electric_fluid_mixer"), ContainerElectricFluidMixer.class, new EnergyProperties(true, false, 20000), pos, state);
		}
		
		public TEElectricFluidMixer(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("electric_fluid_mixer"), pos, state);
		}
		
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return handler.cast();
			}
			return super.getCapability(cap, side);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				return handler.cast();
			}
			return super.getCapability(cap);
		}
		
		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == nTimer) {
					
					boolean sendUpdates = false;
					timer = 0;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					int cost = 60;
					switch(upcount){
					case 3:
						nTimer = 2;
						cost = 460;
						break;
					case 2:
						nTimer = 4;
						cost = 200;
						break;
					case 1:
						nTimer = 8;
						cost = 140;
						break;
					default:
						nTimer = 16;
					}
					
					
					if(output == null || output.isEmpty()) {
						
						boolean doShutoff = true;
						Optional<BathCrafting> rOpt = this.getLevel().getRecipeManager().getRecipeFor(BathCrafting.BATH_RECIPE, this, this.getLevel());
						BathCrafting recipe = rOpt.orElse(null);
						
						if(recipe != null) {
							boolean execute = false;
							if(externalTank == false) {
								if(fluid.getAmount() >= recipe.getPercentage().getMB()) {
									if(BathCraftingFluids.getAssocFluids(fluid.getFluid()) == recipe.getFluid()) {
										execute = true;
									}
								}
							}else {
								if(extHandler == null) {
									extHandler = General.getCapabilityFromDirection(this, "extHandler", Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
								}
								
								if(extHandler != null) {
									if(recipe.getFluid().getAssocFluid() == extHandler.getFluidInTank(0).getFluid() && extHandler.drain(recipe.getPercentage().getMB(), FluidAction.SIMULATE).getAmount() == recipe.getPercentage().getMB()) {
										execute = true;
									}
								}
							}
							
							if(execute) {
								output = recipe.getResultItem().copy();
								cycles = ((float) recipe.getStirs() * 3.6F);
								
								contents.get(1).shrink(1);
								contents.get(2).shrink(1);
								
								int rand = RAND.nextInt(9) * getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
								int cons = recipe.getPercentage().getMB();
								if(rand > 21) {
									cons = 0;
								}else if(rand > 15) {
									cons = (int) Math.round((double) cons * 0.25d);
								}else if(rand > 10) {
									cons = (int) Math.round((double) cons * 0.5d);
								}else if(rand > 5) {
									cons = (int) Math.round((double) cons * 0.75d);
								}
								if(externalTank == true && extHandler != null) {
									extHandler.drain(cons, FluidAction.EXECUTE);
								}else {
									fluid.shrink(cons);
								}
								inProgress = recipe.getFluid();
								doShutoff = false;
								
								sendUpdates = true;
								if(getBlockState().getValue(StateProperties.FLUID) != recipe.getFluid()) {
									this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.FLUID, recipe.getFluid()));
								}
							}
							
							
							
						}
						if(doShutoff && getBlockState().getValue(StateProperties.FLUID) != BathCraftingFluids.NONE) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.FLUID, BathCraftingFluids.NONE));
							sendUpdates = true;
						}
						
							
					}
					
					if(output != null && !output.isEmpty()) {
						
						if(amount - cost >= 0) {
							if(progress >= cycles) {
								if(contents.get(0).isEmpty() || (ItemHandlerHelper.canItemStacksStack(contents.get(0), output) && contents.get(0).getCount() + output.getCount() <= contents.get(0).getMaxStackSize())){
									if(contents.get(0).isEmpty()) {
										contents.set(0, output);
									}else {
										contents.get(0).grow(output.getCount());
									}
									output = null;
									inProgress = BathCraftingFluids.NONE;
									cycles = 0;
									progress = 0;
									sendUpdates = true;
								}
							}else {
								
								amount -= cost;
								fept = (float) cost / (float) nTimer;
								progress++;
								sendUpdates = true;
							}
							
						}
						
					}
					
					
					if(sendUpdates) {
						sendUpdates();
					}
					
				}
			}
			
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}
			if(compound.contains("assemblylinemachines:fluid")) {
				fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:fluid"));
			}
			if(compound.contains("assemblylinemachines:ntimer")) {
				nTimer = compound.getInt("assemblylinemachines:ntimer");
			}
			if(compound.contains("assemblylinemachines:inprogress")) {
				inProgress = BathCraftingFluids.valueOf(compound.getString("assemblylinemachines:inprogress"));
			}
			cycles = compound.getFloat("assemblylinemachines:cycles");
			progress = compound.getFloat("assemblylinemachines:progress");
			externalTank = compound.getBoolean("assemblylinemachines:externaltank");
			
		}
		
		@Override
		public CompoundTag save(CompoundTag compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putString("assemblylinemachines:inprogress", inProgress.toString());
			compound.putBoolean("assemblylinemachines:externaltank", externalTank);
			if(output != null) {
				CompoundTag sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			
			if(fluid != null) {
				CompoundTag sub = new CompoundTag();
				fluid.writeToNBT(sub);
				compound.put("assemblylinemachines:fluid", sub);
			}
			return super.save(compound);
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 2) {
				if(stack.getItem() instanceof ItemUpgrade) {
					return true;
				}
				return false;
			}
			return super.isAllowedInSlot(slot, stack);
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 3; i < 6; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
		
		private class MixerHandler implements IFluidHandler{

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(stack.getFluid());
				
				return ff != BathCraftingFluids.NONE;
			}
			
			@Override
			public int getTanks() {
				return 1;
			}
			
			@Override
			public int getTankCapacity(int tank) {
				return 4000;
			}
			
			@Override
			public FluidStack getFluidInTank(int tank) {
				return fluid;
			}
			
			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if (!fluid.isEmpty()) {
					if (resource.getFluid() != fluid.getFluid()) {
						return 0;
					}
				}
				
				BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(resource.getFluid());
				if(ff == BathCraftingFluids.NONE) {
					return 0;
				}
				
				int attemptedInsert = resource.getAmount();
				int rmCapacity = getTankCapacity(0) - fluid.getAmount();
				if (rmCapacity < attemptedInsert) {
					attemptedInsert = rmCapacity;
				}

				if (action != FluidAction.SIMULATE) {
					if (fluid.isEmpty()) {
						fluid = resource;
					} else {
						fluid.setAmount(fluid.getAmount() + attemptedInsert);
					}
				}
				sendUpdates();
				return attemptedInsert;
			}
			
			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				return FluidStack.EMPTY;
			}
			
			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
			
		}
		
	}
	
	
	
	public static class ContainerElectricFluidMixer extends ContainerALMBase<TEElectricFluidMixer>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerElectricFluidMixer(final int windowId, final Inventory playerInventory, final TEElectricFluidMixer tileEntity) {
			super(Registry.getContainerType("electric_fluid_mixer"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 54, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 75, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 57, tileEntity));
		}
		
		public ContainerElectricFluidMixer(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, General.getBlockEntity(playerInventory, data, TEElectricFluidMixer.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenElectricFluidMixer extends ScreenALMEnergyBased<ContainerElectricFluidMixer>{
		
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TEElectricFluidMixer tsfm;
		
		public ScreenElectricFluidMixer(ContainerElectricFluidMixer screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "electric_fluid_mixer", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void init() {
			super.init();
			
			int x = leftPos;
			int y = topPos;
			
			this.addRenderableWidget(new TrueFalseButton(x+129, y+57, 192, 41, 11, 11, new TrueFalseButtonSupplier("Draw From External Tank", "Draw From Internal Tank", () -> tsfm.externalTank), (b) -> sendModeChange(tsfm.getBlockPos())));
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(!tsfm.fluid.isEmpty() && tsfm.fluid.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(tsfm.fluid.getFluid());
				if(tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(tsfm.fluid.getFluid().getAttributes().getStillTexture());
					spriteMap.put(tsfm.fluid.getFluid(), tas);
				}
				
				if(tsfm.fluid.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				}
				
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
				
				super.blit(x+41, y+23, 37, 37, 37, tas);
			}
			
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			
			int fprog = Math.round(((float)tsfm.fluid.getAmount()/(float)tsfm.fluids.getTankCapacity(0)) * 37f);
			super.blit(x+41, y+23, 176, 84, 8, 37 - fprog);
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 15f);
			
			
			if(tsfm.inProgress != null && tsfm.inProgress.getElectricBlitPiece() != null) {
				super.blit(x+95, y+34, tsfm.inProgress.getElectricBlitPiece().getFirst(), tsfm.inProgress.getElectricBlitPiece().getSecond(), prog, 16);
			}
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if (mouseX >= x + 41 && mouseY >= y + 23 && mouseX <= x + 48 && mouseY <= y + 59) {
				ArrayList<String> str = new ArrayList<>();
				BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(tsfm.fluid.getFluid());
				if(ff == BathCraftingFluids.NONE) {
					this.renderComponentTooltip("None",
							mouseX - x, mouseY - y);
				}else {
					str.add(ff.getFriendlyName());
					if(Screen.hasShiftDown()) {

						str.add(Formatting.FEPT_FORMAT.format(tsfm.fluid.getAmount()) + " mB");
						
					}else {
						str.add(Formatting.FEPT_FORMAT.format((double) tsfm.fluid.getAmount() / 1000D) + " B");
					}
					
					this.renderComponentTooltip(str,
							mouseX - x, mouseY - y);
				}
				
				
			}
			
		}
		
		private static void sendModeChange(BlockPos pos) {
			PacketData pd = new PacketData("efm_gui");
			pd.writeBlockPos("pos", pos);

			HashPacketImpl.INSTANCE.sendToServer(pd);
		}
	}
	
	public static void updateDataFromPacket(PacketData pd, Level world) {
		
		if(pd.getCategory().equals("efm_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
			if(tex instanceof TEElectricFluidMixer) {
				TEElectricFluidMixer te = (TEElectricFluidMixer) tex;
				te.externalTank = !te.externalTank;
				
				te.sendUpdates();
			}
		}
	}
	
}
