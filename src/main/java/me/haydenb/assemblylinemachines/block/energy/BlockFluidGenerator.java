package me.haydenb.assemblylinemachines.block.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.TEFluidGenerator;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ManagedSidedMachine.ManagedDirection;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.categories.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockFluidGenerator extends BlockScreenTileEntity<TEFluidGenerator> {
	
	private FluidGeneratorTypes type;
	
	public BlockFluidGenerator(FluidGeneratorTypes type) {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).notSolid().variableOpacity().harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "fluid_generator", TEFluidGenerator.class);
		
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(StateProperties.MACHINE_ACTIVE, false));
		this.type = type;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING,
				context.getPlacementHorizontalFacing().getOpposite());
	}
	
	public FluidGeneratorTypes getType() {
		return type;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING, StateProperties.MACHINE_ACTIVE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
		if (d == Direction.WEST) {
			return type.shapeW;
		} else if (d == Direction.SOUTH) {
			return type.shapeS;
		} else if (d == Direction.EAST) {
			return type.shapeE;
		} else {
			return type.shapeN;
		}
	}

	public static class TEFluidGenerator extends EnergyMachine<ContainerFluidGenerator> implements ITickableTileEntity{
		
		private int burnTimeLeft = 0;
		private float increasedCost = 1f;
		private FluidStack burnTank = FluidStack.EMPTY;
		private FluidStack coolTank = FluidStack.EMPTY;
		private FluidGeneratorTypes type = null;
		private TranslationTextComponent name = null;
		
		private IFluidHandler fluids = new IFluidHandler() {
			
			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				if(tank == 0) {
					
					if(type != null) {
						
						for(Pair<Fluid, Integer> v : type.supplier.get()) {
							if(v.getLeft().equals(stack.getFluid())) {
								return true;
							}
						}
					}
				}else if(tank == 1) {
					
					if(getUpgradeAmount(Upgrades.GENERATOR_COOLANT) != 0) {
						for(Pair<Fluid, Integer> v : ConfigHolder.COMMON.coolantFluids) {
							if(v.getLeft().equals(stack.getFluid())) {
								return true;
							}
						}
					}
					
				}
				
				return false;
			}
			
			@Override
			public int getTanks() {
				return 2;
			}
			
			@Override
			public int getTankCapacity(int tank) {
				return 4000;
			}
			
			@Override
			public FluidStack getFluidInTank(int tank) {
				if(tank == 0) {
					return burnTank;
				}else {
					return coolTank;
				}
			}
			
			@Override
			public int fill(FluidStack resource, FluidAction action) {
				if(isFluidValid(0, resource)) {
					if (!burnTank.isEmpty()) {
						if (resource.getFluid() != burnTank.getFluid()) {
							return 0;
						}
					}

					int attemptedInsert = resource.getAmount();
					int rmCapacity = getTankCapacity(0) - burnTank.getAmount();
					if (rmCapacity < attemptedInsert) {
						attemptedInsert = rmCapacity;
					}

					if (action != FluidAction.SIMULATE) {
						if (burnTank.isEmpty()) {
							burnTank = resource;
						} else {
							burnTank.setAmount(burnTank.getAmount() + attemptedInsert);
						}
					}
					sendUpdates();
					return attemptedInsert;
				}else if(isFluidValid(1, resource)) {
					if (!coolTank.isEmpty()) {
						if (resource.getFluid() != coolTank.getFluid()) {
							return 0;
						}
					}

					int attemptedInsert = resource.getAmount();
					int rmCapacity = getTankCapacity(0) - coolTank.getAmount();
					if (rmCapacity < attemptedInsert) {
						attemptedInsert = rmCapacity;
					}

					if (action != FluidAction.SIMULATE) {
						if (coolTank.isEmpty()) {
							coolTank = resource;
						} else {
							coolTank.setAmount(coolTank.getAmount() + attemptedInsert);
						}
					}
					sendUpdates();
					return attemptedInsert;
				}else {
					return 0;
				}
			}
			
			@Override
			public FluidStack drain(int maxDrain, FluidAction action) {
				return FluidStack.EMPTY;
			}
			
			@Override
			public FluidStack drain(FluidStack resource, FluidAction action) {
				return drain(resource.getAmount(), action);
			}
		};
		protected LazyOptional<IFluidHandler> fhandler = LazyOptional.of(() -> fluids);
		private int timer = 0;
		
		public TEFluidGenerator(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 3, null, Registry.getContainerId("fluid_generator"), ContainerFluidGenerator.class, new EnergyProperties(false, true, 75000));
		}
		
		public TEFluidGenerator() {
			this(Registry.getTileEntity("fluid_generator"));
		}
		
		@Override
		public ITextComponent getDefaultName() {
			if(name == null) {
				try {
					name = new TranslationTextComponent(world.getBlockState(pos).getBlock().getTranslationKey());
					return name;
				}catch(NullPointerException e) {
					return ITextComponent.func_244388_a("Generator");
				}
			}
			
			return name;
			
		}
		
		@Override
		public void tick() {
			
			if(checkGeneratorType() && !world.isRemote) {
				
				if(timer++ == 20) {
					timer = 0;
					boolean sendUpdates = false;
					int upcount = getUpgradeAmount(Upgrades.UNIVERSAL_SPEED);
					float powerOutMult = 1f;
					switch(upcount) {
					case 3:
						increasedCost = 1.6f;
						powerOutMult = 2.2f;
						break;
					case 2:
						increasedCost = 1.4f;
						powerOutMult = 1.8f;
						break;
					case 1:
						increasedCost = 1.2f;
						powerOutMult = 1.4f;
						break;
					default:
						increasedCost = 1f;
					}
					
					if(burnTimeLeft == 0) {
						if(burnTank.getAmount() >= 1000) {
							int burnAmt = 0;
							for(Pair<Fluid, Integer> v : type.supplier.get()) {
								if(v.getLeft().equals(burnTank.getFluid())) {
									burnAmt = v.getRight();
									break;
								}
							}
							if(burnAmt != 0) {
								int cMult;
								
								boolean shrinkCoolant = false;
								
								if(getUpgradeAmount(Upgrades.GENERATOR_COOLANT) != 0) {
									cMult = 0;
									shrinkCoolant = true;
									if(coolTank.getAmount() >= 1000) {
										for(Pair<Fluid, Integer> v : ConfigHolder.COMMON.coolantFluids) {
											if(v.getLeft().equals(coolTank.getFluid())) {
												cMult = v.getRight();
												break;
											}
										}
									}
								}else {
									cMult = 1;
								}
								if(cMult != 0) {
									burnTimeLeft = burnAmt * cMult;
									burnTank.shrink(1000);
									if(shrinkCoolant) {
										coolTank.shrink(1000);
									}
									sendUpdates = true;
								}
								
								
							}
						}
					}
					
					if(burnTimeLeft != 0) {
						
						int burn = Math.round(type.basefept * 20 * powerOutMult);
						
						int cX = Math.round(burn * increasedCost);
						
						
						if(cX > burnTimeLeft) {
							cX = burnTimeLeft;
							burn = Math.round(cX / increasedCost);
						}
						
						if(burn + amount <= properties.getCapacity()) {
							amount += burn;
							burnTimeLeft -= cX;
							fept = (float) burn / (float) 20;
							sendUpdates = true;
						}
						
						if(!getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, true));
						}
					}else {
						
						if(getBlockState().get(StateProperties.MACHINE_ACTIVE)) {
							world.setBlockState(pos, getBlockState().with(StateProperties.MACHINE_ACTIVE, false));
						}
						fept = 0f;
					}
					
					if(sendUpdates) {
						sendUpdates();
					}
				}
			}
			
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityEnergy.ENERGY) {
				
				if(type != null) {
					if(type.outputSide == null) {
						return super.getCapability(cap);
					}else {
						if(side == type.outputSide.getDirection(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING))) {
							return super.getCapability(cap);
						}else {
							return LazyOptional.empty();
						}
					}
				}else {
					return super.getCapability(cap);
				}
			}else if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
				if(type != null) {
					if(type.inputSide == null) {
						return fhandler.cast();
					}else {
						if(side == type.inputSide.getDirection(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING))) {
							return fhandler.cast();
						}else {
							return LazyOptional.empty();
						}
					}
				}else {
					return fhandler.cast();
				}
			}else {
				return LazyOptional.empty();
			}
			
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			burnTank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:burntank"));
			coolTank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:cooltank"));
			increasedCost = compound.getFloat("assemblylinemachines:cost");
			burnTimeLeft = compound.getInt("assemblylinemachines:burntimeleft");
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			CompoundNBT sub = new CompoundNBT();
			burnTank.writeToNBT(sub);
			compound.put("assemblylinemachines:burntank", sub);
			CompoundNBT sub2 = new CompoundNBT();
			coolTank.writeToNBT(sub2);
			compound.put("assemblylinemachines:cooltank", sub2);
			
			compound.putInt("assemblylinemachines:burntimeleft", burnTimeLeft);
			compound.putFloat("assemblylinemachines:cost", increasedCost);
			return super.write(compound);
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(stack.getItem() instanceof ItemUpgrade) {
				return true;
			}
			return false;
		}
		
		public boolean checkGeneratorType() {
			
			if(type != null) {
				return true;
			}else {
				
				Block bl = world.getBlockState(pos).getBlock();
				if(bl instanceof BlockFluidGenerator) {
					type = ((BlockFluidGenerator) bl).getType();
					return true;
				}
				return false;
			}
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 0; i < 3; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}

			return ii;
		}
	}
	
	public static class ContainerFluidGenerator extends ContainerALMBase<TEFluidGenerator>{
		
		public ContainerFluidGenerator(final int windowId, final PlayerInventory playerInventory, final TEFluidGenerator tileEntity) {
			super(Registry.getContainerType("fluid_generator"), windowId, tileEntity, playerInventory, com.mojang.datafixers.util.Pair.of(8, 84), com.mojang.datafixers.util.Pair.of(8, 142), 0);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 57, tileEntity));
		}
		
		
		public ContainerFluidGenerator(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEFluidGenerator.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenFluidGenerator extends ScreenALMEnergyBased<ContainerFluidGenerator>{
		TEFluidGenerator tsfm;
		private boolean hasSuffUpgrades;
		
		private static final HashMap<FluidGeneratorTypes, ResourceLocation> FG_BACKGROUNDS = new HashMap<>();
		static {
			FG_BACKGROUNDS.put(FluidGeneratorTypes.COMBUSTION, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/combustion_generator.png"));
			FG_BACKGROUNDS.put(FluidGeneratorTypes.GEOTHERMAL, new ResourceLocation(AssemblyLineMachines.MODID, "textures/gui/geothermal_generator.png"));
		}
		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		
		public ScreenFluidGenerator(ContainerFluidGenerator screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new com.mojang.datafixers.util.Pair<>(176, 166), new com.mojang.datafixers.util.Pair<>(11, 6), new com.mojang.datafixers.util.Pair<>(11, 73), "", false, new com.mojang.datafixers.util.Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}
		
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			field_230706_i_.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			renderFluid(tsfm.burnTank, x + 49, y + 23);
			renderFluid(tsfm.coolTank, x + 62, y + 23);
			
			ResourceLocation rl = FG_BACKGROUNDS.get(tsfm.type);
			
			if(rl != null) {
				GL11.glColor4f(1f, 1f, 1f, 1f);
				this.field_230706_i_.getTextureManager().bindTexture(rl);
				this.blit(x, y, 0, 0, this.xSize, this.ySize);
			}
			
			int prog = Math.round(((float) machine.amount / (float) machine.properties.getCapacity()) * 52F);
			super.blit(x + energyMeterLoc.getFirst(), y + energyMeterLoc.getSecond() + (52 - prog), startx, (52 - prog), 16, prog);
			
			renderFluidOverlayBar(tsfm.burnTank, 4000, x + 49, y + 23);
			renderFluidOverlayBar(tsfm.coolTank, 4000, x + 62, y + 23);
			
			hasSuffUpgrades = tsfm.getUpgradeAmount(Upgrades.GENERATOR_COOLANT) != 0;
			if(!hasSuffUpgrades) {
				this.blit(x+61, y+22, 123, 6, 10, 39);
			}
			
			if(tsfm.burnTimeLeft != 0) {
				
				if(tsfm.type == FluidGeneratorTypes.GEOTHERMAL) {
					blit(x+82, y+26, 176, 89, 18, 6);
					blit(x+82, y+52, 176, 89, 18, 6);
				}else if(tsfm.type == FluidGeneratorTypes.COMBUSTION) {
					blit(x+84, y+51, 176, 89, 13, 12);
				}
			}
			
			if(tsfm.fept == 0) {
				this.drawCenteredString(this.font, "0/t", x+90, y+38, 0xffffff);
			}else {
				this.drawCenteredString(this.font, Math.round(tsfm.fept) + "/t", x+90, y+38, 0x76f597);
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			renderFluidTooltip(tsfm.burnTank, mouseX, mouseY, x + 49, y + 23, x, y);
			if(hasSuffUpgrades) {
				renderFluidTooltip(tsfm.coolTank, mouseX, mouseY, x + 62, y + 23, x, y);
			}
		}
		
		
		
		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(fs.getFluid().getAttributes().getStillTexture());
					spriteMap.put(fs.getFluid(), tas);
				}

				if (fs.getFluid() == BathCraftingFluids.WATER.getAssocFluid()) {
					GL11.glColor4f(0.2470f, 0.4627f, 0.8941f, 1f);
				} else {
					GL11.glColor4f(1f, 1f, 1f, 1f);
				}

				super.blit(xblit, yblit, 37, 37, 37, tas);
			}
		}

		private void renderFluidOverlayBar(FluidStack fs, float capacity, int xblit, int yblit) {
			int fprog = Math.round(((float) fs.getAmount() / capacity) * 37f);
			super.blit(xblit, yblit, 176, 52, 8, 37 - fprog);
		}

		private void renderFluidTooltip(FluidStack fs, int mouseX, int mouseY, int mminx, int mminy, int bx, int by) {

			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 7 && mouseY <= mminy + 36) {
				if (!fs.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(fs.getDisplayName().func_230532_e_().getString());
					if (Screen.func_231173_s_()) {

						str.add(Formatting.FEPT_FORMAT.format(fs.getAmount()) + " mB");

					} else {
						str.add(Formatting.FEPT_FORMAT.format((double) fs.getAmount() / 1000D) + " B");
					}

					this.renderTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
	}
	
	public static enum FluidGeneratorTypes{
		
		
		COMBUSTION(Stream.of(
				Block.makeCuboidShape(3, 3, 3, 13, 7, 13),Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
				Block.makeCuboidShape(0, 7, 0, 16, 10, 16),Block.makeCuboidShape(0, 3, 0, 16, 7, 3),
				Block.makeCuboidShape(0, 3, 13, 3, 7, 16),Block.makeCuboidShape(13, 3, 13, 16, 7, 16),
				Block.makeCuboidShape(4, 10, 4, 12, 14, 12),Block.makeCuboidShape(3, 14, 3, 13, 16, 13),
				Block.makeCuboidShape(4, 3, 15, 5, 5, 16),Block.makeCuboidShape(6, 3, 15, 7, 5, 16),
				Block.makeCuboidShape(9, 3, 15, 10, 5, 16),Block.makeCuboidShape(11, 3, 15, 12, 5, 16),
				Block.makeCuboidShape(4, 5, 13, 5, 6, 16),Block.makeCuboidShape(6, 5, 13, 7, 6, 16),
				Block.makeCuboidShape(9, 5, 13, 10, 6, 16),Block.makeCuboidShape(11, 5, 13, 12, 6, 16),
				Block.makeCuboidShape(13, 3, 11, 16, 7, 12),Block.makeCuboidShape(13, 3, 9, 16, 7, 10),
				Block.makeCuboidShape(13, 3, 6, 16, 7, 7),Block.makeCuboidShape(13, 3, 4, 16, 7, 5),
				Block.makeCuboidShape(0, 3, 4, 3, 7, 5),Block.makeCuboidShape(0, 3, 6, 3, 7, 7),
				Block.makeCuboidShape(0, 3, 9, 3, 7, 10),Block.makeCuboidShape(0, 3, 11, 3, 7, 12),
				Block.makeCuboidShape(13, 4, 3, 15, 6, 13),Block.makeCuboidShape(1, 4, 3, 3, 6, 13)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), true, null, ManagedDirection.TOP, () -> ConfigHolder.COMMON.combustionFluids, 350),
		GEOTHERMAL(Stream.of(
				Block.makeCuboidShape(0, 0, 0, 16, 2, 16),Block.makeCuboidShape(5, 6, 0, 11, 16, 16),Block.makeCuboidShape(2, 2, 1, 14, 5, 16),Block.makeCuboidShape(5, 5, 1, 11, 6, 16),
				Block.makeCuboidShape(14, 2, 0, 16, 5, 16),Block.makeCuboidShape(14, 5, 0, 16, 6, 2),Block.makeCuboidShape(0, 5, 0, 2, 6, 2),Block.makeCuboidShape(11, 5, 1, 14, 6, 2),
				Block.makeCuboidShape(2, 5, 1, 5, 6, 2),Block.makeCuboidShape(11, 6, 0, 16, 11, 2),Block.makeCuboidShape(0, 6, 0, 5, 11, 2),Block.makeCuboidShape(11, 5, 11, 16, 16, 16),
				Block.makeCuboidShape(0, 5, 11, 5, 16, 16),Block.makeCuboidShape(11, 14, 0, 16, 16, 11),Block.makeCuboidShape(0, 14, 0, 5, 16, 11),Block.makeCuboidShape(11, 11, 0, 16, 14, 5),
				Block.makeCuboidShape(0, 11, 0, 5, 14, 5),Block.makeCuboidShape(0, 2, 0, 2, 5, 16),Block.makeCuboidShape(1, 10, 6, 5, 14, 10),Block.makeCuboidShape(11, 10, 6, 15, 14, 10),
				Block.makeCuboidShape(1, 6, 2, 5, 10, 10),Block.makeCuboidShape(11, 6, 2, 15, 10, 10),Block.makeCuboidShape(0, 5, 3, 5, 11, 4),Block.makeCuboidShape(11, 5, 3, 16, 11, 4),
				Block.makeCuboidShape(0, 5, 5, 5, 11, 6),Block.makeCuboidShape(11, 5, 5, 16, 11, 6),Block.makeCuboidShape(0, 10, 6, 5, 11, 11),Block.makeCuboidShape(11, 10, 6, 16, 11, 11),
				Block.makeCuboidShape(0, 12, 5, 5, 13, 11),Block.makeCuboidShape(11, 12, 5, 16, 13, 11),Block.makeCuboidShape(2, 2, 1, 14, 6, 1),Block.makeCuboidShape(11, 2, 0, 12, 6, 1),
				Block.makeCuboidShape(9, 2, 0, 10, 6, 1),Block.makeCuboidShape(6, 2, 0, 7, 6, 1),Block.makeCuboidShape(4, 2, 0, 5, 6, 1)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get(), true, null, null, () -> ConfigHolder.COMMON.geothermalFluids, 200);
		
		private final VoxelShape shapeN;
		private final VoxelShape shapeS;
		private final VoxelShape shapeW;
		private final VoxelShape shapeE;
		private final int basefept;
		public final ManagedDirection inputSide;
		public final ManagedDirection outputSide;
		public final Supplier<ArrayList<Pair<Fluid, Integer>>> supplier;
		FluidGeneratorTypes(VoxelShape NShape, boolean supportsCoolant, ManagedDirection inputSide, ManagedDirection outputSide, Supplier<ArrayList<Pair<Fluid, Integer>>> validFluids, int basefept){
			shapeN = NShape;
			shapeS = General.rotateShape(Direction.NORTH, Direction.SOUTH, shapeN);
			shapeW = General.rotateShape(Direction.NORTH, Direction.WEST, shapeN);
			shapeE = General.rotateShape(Direction.NORTH, Direction.EAST, shapeN);
			supplier = validFluids;
			this.inputSide = inputSide;
			this.outputSide = outputSide;
			this.basefept = basefept;
		}
		
	}
}
