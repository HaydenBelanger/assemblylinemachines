package me.haydenb.assemblylinemachines.block.machines.electric;

import java.util.*;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockElectricFluidMixer extends BlockScreenTileEntity<BlockElectricFluidMixer.TEElectricFluidMixer>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 14, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 0, 0, 16, 2, 16),
			Block.makeCuboidShape(0, 2, 1, 16, 14, 16),
			Block.makeCuboidShape(0, 5, 0, 16, 14, 1),
			Block.makeCuboidShape(0, 2, 0, 3, 5, 1),
			Block.makeCuboidShape(13, 2, 0, 16, 5, 1),
			Block.makeCuboidShape(9, 2, 0, 12, 5, 1),
			Block.makeCuboidShape(4, 2, 0, 7, 5, 1),
			Block.makeCuboidShape(7, 4, 0, 9, 5, 1)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	private static final Random RAND = new Random();
	
	public BlockElectricFluidMixer() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "electric_fluid_mixer", BlockElectricFluidMixer.TEElectricFluidMixer.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.FLUID, BathCraftingFluids.NONE).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.FLUID).add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public ActionResultType blockRightClickServer(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null) != null && world.getTileEntity(pos) instanceof TEElectricFluidMixer) {
			
			IFluidHandler handler = ((TEElectricFluidMixer) world.getTileEntity(pos)).fluids;
			FluidActionResult far = FluidUtil.tryEmptyContainer(stack, handler, 1000, player, true);
			if(far.isSuccess()) {
				if(stack.getCount() == 1) {
					player.inventory.removeStackFromSlot(player.inventory.currentItem);
				}else {
					stack.shrink(1);
				}
				ItemHandlerHelper.giveItemToPlayer(player, far.getResult());
				return ActionResultType.CONSUME;
			}
			
			
		}
		return super.blockRightClickServer(state, world, pos, player);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
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
	
	public static class TEElectricFluidMixer extends ManagedSidedMachine<ContainerElectricFluidMixer> implements ITickableTileEntity{
		
		
		private int timer = 0;
		private int nTimer = 20;
		private float progress = 0;
		private float cycles = 0;
		private ItemStack output = null;
		private BathCraftingFluids inProgress = BathCraftingFluids.NONE;
		private FluidStack fluid = FluidStack.EMPTY;
		
		protected IFluidHandler fluids = new MixerHandler();
		
		protected LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> fluids);
		
		public TEElectricFluidMixer(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 6, new TranslationTextComponent(Registry.getBlock("electric_fluid_mixer").getTranslationKey()), Registry.getContainerId("electric_fluid_mixer"), ContainerElectricFluidMixer.class, new EnergyProperties(true, false, 20000));
		}
		
		public TEElectricFluidMixer() {
			this(Registry.getTileEntity("electric_fluid_mixer"));
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
			if(!world.isRemote) {
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
						
						Optional<BathCrafting> rOpt = world.getRecipeManager().getRecipe(BathCrafting.BATH_RECIPE, this, world);
						BathCrafting recipe = rOpt.orElse(null);
						if(recipe != null) {
							if(fluid.getAmount() >= 1000) {
								BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(fluid.getFluid());
								if(ff == recipe.getFluid()) {
									output = recipe.getRecipeOutput().copy();
									cycles = ((float) recipe.getStirs() * 3.6F);
									
									contents.get(1).shrink(1);
									contents.get(2).shrink(1);
									
									int rand = RAND.nextInt(9) * getUpgradeAmount(Upgrades.MACHINE_CONSERVATION);
									int cons = 1000;
									if(rand > 21) {
										cons = 0;
									}else if(rand > 15) {
										cons = 250;
									}else if(rand > 10) {
										cons = 500;
									}else if(rand > 5) {
										cons = 750;
									}
									fluid.shrink(cons);
									inProgress = ff;
									
									sendUpdates = true;
									if(getBlockState().get(StateProperties.FLUID) != ff) {
										world.setBlockState(pos, getBlockState().with(StateProperties.FLUID, ff));
									}
								}
							}
							
							
						}else {
							if(getBlockState().get(StateProperties.FLUID) != BathCraftingFluids.NONE) {
								world.setBlockState(pos, getBlockState().with(StateProperties.FLUID, BathCraftingFluids.NONE));
								sendUpdates = true;
							}
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
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.read(compound.getCompound("assemblylinemachines:output"));
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
			
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putString("assemblylinemachines:inprogress", inProgress.toString());
			if(output != null) {
				CompoundNBT sub = new CompoundNBT();
				output.write(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			
			if(fluid != null) {
				CompoundNBT sub = new CompoundNBT();
				fluid.writeToNBT(sub);
				compound.put("assemblylinemachines:fluid", sub);
			}
			return super.write(compound);
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
		
		public ContainerElectricFluidMixer(final int windowId, final PlayerInventory playerInventory, final TEElectricFluidMixer tileEntity) {
			super(Registry.getContainerType("electric_fluid_mixer"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 54, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 75, 34, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 3, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 4, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 5, 149, 57, tileEntity));
		}
		
		public ContainerElectricFluidMixer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEElectricFluidMixer.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenElectricFluidMixer extends ScreenALMEnergyBased<ContainerElectricFluidMixer>{
		
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TEElectricFluidMixer tsfm;
		
		public ScreenElectricFluidMixer(ContainerElectricFluidMixer screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "electric_fluid_mixer", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if(!tsfm.fluid.isEmpty() && tsfm.fluid.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(tsfm.fluid.getFluid());
				if(tas == null) {
					tas = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(tsfm.fluid.getFluid().getAttributes().getStillTexture());
					spriteMap.put(tsfm.fluid.getFluid(), tas);
				}
				
				if(tsfm.fluid.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					GL11.glColor4f(0.2470f, 0.4627f, 0.8941f, 1f);
				}
				
				field_230706_i_.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
				
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
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if (mouseX >= x + 41 && mouseY >= y + 23 && mouseX <= x + 48 && mouseY <= y + 59) {
				ArrayList<String> str = new ArrayList<>();
				BathCraftingFluids ff = BathCraftingFluids.getAssocFluids(tsfm.fluid.getFluid());
				if(ff == BathCraftingFluids.NONE) {
					this.renderTooltip("None",
							mouseX - x, mouseY - y);
				}else {
					str.add(ff.getFriendlyName());
					if(Screen.func_231173_s_()) {

						str.add(Formatting.FEPT_FORMAT.format(tsfm.fluid.getAmount()) + " mB");
						
					}else {
						str.add(Formatting.FEPT_FORMAT.format((double) tsfm.fluid.getAmount() / 1000D) + " B");
					}
					
					this.renderTooltip(str,
							mouseX - x, mouseY - y);
				}
				
				
			}
			
		}
	}
	
}
