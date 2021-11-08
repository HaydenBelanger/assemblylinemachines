package me.haydenb.assemblylinemachines.block.machines.crank;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ScreenALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.crafting.BathCrafting;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.BathCraftingFluid.BathCraftingFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BlockSimpleFluidMixer extends BlockScreenBlockEntity<BlockSimpleFluidMixer.TESimpleFluidMixer> implements ICrankableBlock{
	

	public BlockSimpleFluidMixer() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "simple_fluid_mixer", BlockSimpleFluidMixer.TESimpleFluidMixer.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(BathCraftingFluid.FLUID, BathCraftingFluids.NONE).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {

		builder.add(BathCraftingFluid.FLUID).add(HorizontalDirectionalBlock.FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean validSide(BlockState state, Direction dir) {
		return true;
	}
	
	@Override
	public boolean needsGearbox() {
		return false;
	}
	
	public static class TESimpleFluidMixer extends SimpleMachine<ContainerSimpleFluidMixer> implements ALMTicker<TESimpleFluidMixer>, ICrankableMachine{
		
		public IFluidHandler handler;
		public int timer;
		public ItemStack output = null;
		public boolean check;
		public float progress = 0;
		public float cycles = 0;
		public ItemStack isa = null;
		public ItemStack isb = null;
		public BathCraftingFluids f = null;
		public boolean pendingOutput = false;
		public int cranks;
		
		public TESimpleFluidMixer(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 2, new TranslatableComponent(Registry.getBlock("simple_fluid_mixer").getDescriptionId()), Registry.getContainerId("simple_fluid_mixer"), ContainerSimpleFluidMixer.class, pos, state);
		}
		
		public TESimpleFluidMixer(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("simple_fluid_mixer"), pos, state);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
		
		@Override
		public boolean perform() {
			if(output == null || pendingOutput == true) {
				return false;
			}
			cranks++;
			return true;
			
		}
		
		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ == 20) {
					timer = 0;
					if(handler == null) {
						handler = Utils.getCapabilityFromDirection(this, (lo) -> {if(this != null) handler = null;}, Direction.DOWN, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
					}
					if(output != null) {
						boolean sendupdate = false;
						boolean end = false;
						if(pendingOutput) {
							Direction opdr = getBlockState().getValue(HorizontalDirectionalBlock.FACING).getCounterClockWise();
							BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().relative(opdr));
							if(te != null) {
								LazyOptional<IItemHandler> h = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opdr.getOpposite());
								IItemHandler handler = h.orElse(null);
								if(handler != null) {
									for(int i = 0; i < handler.getSlots(); i++) {
										if(handler.insertItem(i, output, true) == ItemStack.EMPTY) {
											handler.insertItem(i, output, false);
											output = null;
											progress = 0;
											cycles = 0;
											f = null;
											pendingOutput = false;
											sendupdate = true;
											end = true;
											this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BathCraftingFluid.FLUID, BathCraftingFluids.NONE));
											break;
										}
									}
								}
							}
							
						}else {
							
							if(cranks >= 1) {
								cranks = 0;
								if(progress == cycles) {
									pendingOutput = true;
								}
								progress++;
								sendupdate = true;
							}
							
						}
						
						if(getBlockState().getValue(BathCraftingFluid.FLUID) != f && end == false) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(BathCraftingFluid.FLUID, f));
							sendupdate = true;
						}
						
						if(sendupdate) {
							sendUpdates();
						}
						
					}else {
						if(handler != null) {
							if(contents.get(0) != isa || contents.get(1) != isb) {
								
								check = true;
							}
							if(check == true) {
								isa = contents.get(0);
								isb = contents.get(1);
								check = false;
								
								BathCrafting crafting = this.getLevel().getRecipeManager().getRecipeFor(BathCrafting.BATH_RECIPE, this, this.getLevel()).orElse(null);
								if(crafting != null && crafting.getFluid().isElectricMixerOnly() == false) {
									if(crafting.getFluid().getAssocFluid() == handler.getFluidInTank(0).getFluid() && handler.drain(crafting.getPercentage().getMB(), FluidAction.SIMULATE).getAmount() == crafting.getPercentage().getMB()) {
										handler.drain(crafting.getPercentage().getMB(), FluidAction.EXECUTE);
										isa.shrink(1);
										isb.shrink(1);
										isa = null;
										isb = null;
										output = crafting.getResultItem().copy();
										cycles = crafting.getStirs();
										f = crafting.getFluid();
										pendingOutput = false;
										sendUpdates();
										
									}else {
										check = true;
									}
								}
							}
						}
					}
				}
				
			}
			
		}
		
		@Override
		public CompoundTag save(CompoundTag compound) {
			compound.putFloat("assemblylinemachines:cycles", cycles);
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putBoolean("assemblylinemachines:pendingoutput", pendingOutput);
			if(f != null) {
				compound.putString("assemblylinemachines:fluid", f.toString());
			}
			if(output != null) {
				CompoundTag sub = new CompoundTag();
				output.save(sub);
				compound.put("assemblylinemachines:output", sub);
			}
			return super.save(compound);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			if(compound.contains("assemblylinemachines:cycles")) {
				cycles = compound.getFloat("assemblylinemachines:cycles");
			}
			if(compound.contains("assemblylinemachines:progress")) {
				progress = compound.getFloat("assemblylinemachines:progress");
			}
			if(compound.contains("assemblylinemachines:pendingoutput")) {
				pendingOutput = compound.getBoolean("assemblylinemachines:pendingoutput");
			}
			if(compound.contains("assemblylinemachines:output")) {
				output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			}
			if(compound.contains("assemblylinemachines:fluid")) {
				f = BathCraftingFluids.valueOf(compound.getString("assemblylinemachines:fluid"));
			}
		}

	}
	
	public static class ContainerSimpleFluidMixer extends ContainerALMBase<TESimpleFluidMixer>{
		
		private static final Pair<Integer, Integer> INPUT_A_POS = new Pair<>(63, 48);
		private static final Pair<Integer, Integer> INPUT_B_POS = new Pair<>(88, 48);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerSimpleFluidMixer(final int windowId, final Inventory playerInventory, final TESimpleFluidMixer tileEntity) {
			super(Registry.getContainerType("simple_fluid_mixer"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
			this.addSlot(new Slot(this.tileEntity, 0, INPUT_A_POS.getFirst(), INPUT_A_POS.getSecond()));
			this.addSlot(new Slot(this.tileEntity, 1, INPUT_B_POS.getFirst(), INPUT_B_POS.getSecond()));
		}
		
		public ContainerSimpleFluidMixer(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TESimpleFluidMixer.class));
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenSimpleFluidMixer extends ScreenALMBase<ContainerSimpleFluidMixer>{
		
		TESimpleFluidMixer tsfm;
		
		public ScreenSimpleFluidMixer(ContainerSimpleFluidMixer screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "simple_fluid_mixer", true);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if(tsfm.f != null && tsfm.cycles != 0) {
				int prog = Math.round((tsfm.progress/tsfm.cycles) * 24f);
				super.blit(x+71, y+19 + (24 - prog), tsfm.f.getSimpleBlitPiece().getFirst(), tsfm.f.getSimpleBlitPiece().getSecond() + (24 - prog), 24, prog);
			}
		}
	}

}
