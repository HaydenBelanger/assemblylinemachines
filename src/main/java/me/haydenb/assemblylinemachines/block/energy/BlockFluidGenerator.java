package me.haydenb.assemblylinemachines.block.energy;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.energy.BlockFluidGenerator.TEFluidGenerator;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine.ManagedDirection;
import me.haydenb.assemblylinemachines.crafting.GeneratorFluidCrafting;
import me.haydenb.assemblylinemachines.crafting.GeneratorFluidCrafting.GeneratorFluidTypes;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.PluginTOPRegistry.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import me.haydenb.assemblylinemachines.registry.utils.StateProperties.BathCraftingFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockFluidGenerator extends BlockScreenBlockEntity<TEFluidGenerator> {

	private FluidGeneratorTypes type;

	public BlockFluidGenerator(FluidGeneratorTypes type) {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).noOcclusion().dynamicShape().sound(SoundType.METAL), "fluid_generator", TEFluidGenerator.class);

		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(StateProperties.MACHINE_ACTIVE, false));
		this.type = type;
	}



	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
				context.getHorizontalDirection().getOpposite());
	}

	public FluidGeneratorTypes getType() {
		return type;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING, StateProperties.MACHINE_ACTIVE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
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

	public static class TEFluidGenerator extends EnergyMachine<ContainerFluidGenerator> implements ALMTicker<TEFluidGenerator>, TOPProvider{

		private int burnTimeLeft = 0;
		private float increasedCost = 1f;
		private FluidStack burnTank = FluidStack.EMPTY;
		private FluidStack coolTank = FluidStack.EMPTY;
		public Lazy<FluidGeneratorTypes> type = Lazy.of(() -> ((BlockFluidGenerator)this.getLevel().getBlockState(this.getBlockPos()).getBlock()).getType());
		private MutableComponent name = null;

		private IFluidHandler fluids = new IFluidHandler() {

			@Override
			public boolean isFluidValid(int tank, FluidStack stack) {
				if(tank >= 2) return false;
				Predicate<GeneratorFluidTypes> typePred = tank == 0 ? (type) -> TEFluidGenerator.this.type.get() == type.equivalentGenerator : (type) -> type == GeneratorFluidTypes.COOLANT;
				return level.getRecipeManager().getAllRecipesFor(GeneratorFluidCrafting.GENFLUID_RECIPE).stream().anyMatch((recipe) -> typePred.test(recipe.fluidType) && recipe.fluid.equals(stack.getFluid()) && (recipe.powerPerUnit != 0 || recipe.coolantStrength != 0));
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

		public TEFluidGenerator(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 3, null, Registry.getContainerId("fluid_generator"), ContainerFluidGenerator.class, new EnergyProperties(false, true, 75000), pos, state);
		}

		public TEFluidGenerator(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("fluid_generator"), pos, state);
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {

			if(fept == 0) {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(Component.literal("Idle").withStyle(ChatFormatting.RED)).text(Component.literal("0 FE/t"));
			}else {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(Component.literal("Generating...").withStyle(ChatFormatting.GREEN)).text(Component.literal("+" + FormattingHelper.FEPT_FORMAT.format(fept) + " FE/t").withStyle(ChatFormatting.GREEN));
			}

		}

		@Override
		public Component getDefaultName() {
			if(name == null) {
				try {
					name = Component.translatable(this.getLevel().getBlockState(this.getBlockPos()).getBlock().getDescriptionId());
					return name;
				}catch(NullPointerException e) {
					return Component.literal("Generator");
				}
			}

			return name;

		}

		@Override
		public void tick() {

			if(!level.isClientSide) {

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
							List<GeneratorFluidCrafting> recipes = this.getLevel().getRecipeManager().getAllRecipesFor(GeneratorFluidCrafting.GENFLUID_RECIPE);
							Optional<Integer> burnAmt = recipes.stream().filter((recipe) -> recipe.matches(this, this.getLevel()) && recipe.fluid.equals(burnTank.getFluid())).map((recipe) -> recipe.powerPerUnit).findAny();
							if(burnAmt.isPresent()) {
								float cMult;

								boolean shrinkCoolant = false;

								if(getUpgradeAmount(Upgrades.GENERATOR_COOLANT) != 0 && coolTank.getAmount() >= 1000) {
									shrinkCoolant = true;
									cMult = recipes.stream().filter((recipe) -> recipe.fluidType == GeneratorFluidTypes.COOLANT && recipe.fluid.equals(coolTank.getFluid())).map((recipe) -> recipe.coolantStrength).findAny().orElse(0f);
								}else {
									cMult = 1;
								}
								if(cMult != 0) {
									burnTimeLeft = Math.round((float)burnAmt.get() * cMult);
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

						int burn = Math.round(type.get().basefept * 20 * powerOutMult);

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

						if(!getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, true));
						}
					}else {

						if(getBlockState().getValue(StateProperties.MACHINE_ACTIVE)) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), getBlockState().setValue(StateProperties.MACHINE_ACTIVE, false));
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
					if(type.get().outputSide == null) {
						return super.getCapability(cap);
					}else {
						if(side == type.get().outputSide.getDirection(getBlockState().getValue(HorizontalDirectionalBlock.FACING))) {
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
					if(type.get().inputSide == null) {
						return fhandler.cast();
					}else {
						if(side == type.get().inputSide.getDirection(getBlockState().getValue(HorizontalDirectionalBlock.FACING))) {
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
		public void load(CompoundTag compound) {
			super.load(compound);

			burnTank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:burntank"));
			coolTank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:cooltank"));
			increasedCost = compound.getFloat("assemblylinemachines:cost");
			burnTimeLeft = compound.getInt("assemblylinemachines:burntimeleft");
		}

		@Override
		public void saveAdditional(CompoundTag compound) {
			CompoundTag sub = new CompoundTag();
			burnTank.writeToNBT(sub);
			compound.put("assemblylinemachines:burntank", sub);
			CompoundTag sub2 = new CompoundTag();
			coolTank.writeToNBT(sub2);
			compound.put("assemblylinemachines:cooltank", sub2);

			compound.putInt("assemblylinemachines:burntimeleft", burnTimeLeft);
			compound.putFloat("assemblylinemachines:cost", increasedCost);
			super.saveAdditional(compound);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(stack.getItem() instanceof ItemUpgrade) {
				return true;
			}
			return false;
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

		public ContainerFluidGenerator(final int windowId, final Inventory playerInventory, final TEFluidGenerator tileEntity) {
			super(Registry.getContainerType("fluid_generator"), windowId, tileEntity, playerInventory, com.mojang.datafixers.util.Pair.of(8, 84), com.mojang.datafixers.util.Pair.of(8, 142), 0);

			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, 149, 21, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 1, 149, 39, tileEntity));
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 2, 149, 57, tileEntity));
		}


		public ContainerFluidGenerator(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEFluidGenerator.class));
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

		public ScreenFluidGenerator(ContainerFluidGenerator screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new com.mojang.datafixers.util.Pair<>(176, 166), new com.mojang.datafixers.util.Pair<>(11, 6), new com.mojang.datafixers.util.Pair<>(11, 73), "", false, new com.mojang.datafixers.util.Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}


		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			renderFluid(tsfm.burnTank, x + 49, y + 23);
			renderFluid(tsfm.coolTank, x + 62, y + 23);

			ResourceLocation rl = FG_BACKGROUNDS.get(tsfm.type.get());

			if(rl != null) {
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, rl);
				this.blit(x, y, 0, 0, this.imageWidth, this.imageHeight);
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

				if(tsfm.type.get() == FluidGeneratorTypes.GEOTHERMAL) {
					blit(x+82, y+26, 176, 89, 18, 6);
					blit(x+82, y+52, 176, 89, 18, 6);
				}else if(tsfm.type.get() == FluidGeneratorTypes.COMBUSTION) {
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

			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;

			renderFluidTooltip(tsfm.burnTank, mouseX, mouseY, x + 49, y + 23, x, y);
			if(hasSuffUpgrades) {
				renderFluidTooltip(tsfm.coolTank, mouseX, mouseY, x + 62, y + 23, x, y);
			}
		}



		private void renderFluid(FluidStack fs, int xblit, int yblit) {
			if (!fs.isEmpty() && fs.getAmount() != 0) {
				TextureAtlasSprite tas = spriteMap.get(fs.getFluid());
				if (tas == null) {
					tas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(RenderProperties.get(fs.getFluid()).getStillTexture());
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
			int fprog = Math.round((fs.getAmount() / capacity) * 37f);
			super.blit(xblit, yblit, 176, 52, 8, 37 - fprog);
		}

		private void renderFluidTooltip(FluidStack fs, int mouseX, int mouseY, int mminx, int mminy, int bx, int by) {

			if (mouseX >= mminx && mouseY >= mminy && mouseX <= mminx + 7 && mouseY <= mminy + 36) {
				if (!fs.isEmpty()) {
					ArrayList<String> str = new ArrayList<>();

					str.add(fs.getDisplayName().getString());
					if (Screen.hasShiftDown()) {

						str.add(FormattingHelper.FEPT_FORMAT.format(fs.getAmount()) + " mB");

					} else {
						str.add(FormattingHelper.FEPT_FORMAT.format(fs.getAmount() / 1000D) + " B");
					}

					this.renderComponentTooltip(str, mouseX - bx, mouseY - by);
				} else {
					this.renderComponentTooltip("Empty", mouseX - bx, mouseY - by);
				}
			}
		}
	}

	public static enum FluidGeneratorTypes{


		COMBUSTION(Stream.of(
				Block.box(3, 3, 3, 13, 7, 13),Block.box(0, 0, 0, 16, 3, 16),
				Block.box(0, 7, 0, 16, 10, 16),Block.box(0, 3, 0, 16, 7, 3),
				Block.box(0, 3, 13, 3, 7, 16),Block.box(13, 3, 13, 16, 7, 16),
				Block.box(4, 10, 4, 12, 14, 12),Block.box(3, 14, 3, 13, 16, 13),
				Block.box(4, 3, 15, 5, 5, 16),Block.box(6, 3, 15, 7, 5, 16),
				Block.box(9, 3, 15, 10, 5, 16),Block.box(11, 3, 15, 12, 5, 16),
				Block.box(4, 5, 13, 5, 6, 16),Block.box(6, 5, 13, 7, 6, 16),
				Block.box(9, 5, 13, 10, 6, 16),Block.box(11, 5, 13, 12, 6, 16),
				Block.box(13, 3, 11, 16, 7, 12),Block.box(13, 3, 9, 16, 7, 10),
				Block.box(13, 3, 6, 16, 7, 7),Block.box(13, 3, 4, 16, 7, 5),
				Block.box(0, 3, 4, 3, 7, 5),Block.box(0, 3, 6, 3, 7, 7),
				Block.box(0, 3, 9, 3, 7, 10),Block.box(0, 3, 11, 3, 7, 12),
				Block.box(13, 4, 3, 15, 6, 13),Block.box(1, 4, 3, 3, 6, 13)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true, null, ManagedDirection.TOP, 350),
		GEOTHERMAL(Stream.of(
				Block.box(0, 0, 0, 16, 2, 16),Block.box(5, 6, 0, 11, 16, 16),Block.box(2, 2, 1, 14, 5, 16),Block.box(5, 5, 1, 11, 6, 16),
				Block.box(14, 2, 0, 16, 5, 16),Block.box(14, 5, 0, 16, 6, 2),Block.box(0, 5, 0, 2, 6, 2),Block.box(11, 5, 1, 14, 6, 2),
				Block.box(2, 5, 1, 5, 6, 2),Block.box(11, 6, 0, 16, 11, 2),Block.box(0, 6, 0, 5, 11, 2),Block.box(11, 5, 11, 16, 16, 16),
				Block.box(0, 5, 11, 5, 16, 16),Block.box(11, 14, 0, 16, 16, 11),Block.box(0, 14, 0, 5, 16, 11),Block.box(11, 11, 0, 16, 14, 5),
				Block.box(0, 11, 0, 5, 14, 5),Block.box(0, 2, 0, 2, 5, 16),Block.box(1, 10, 6, 5, 14, 10),Block.box(11, 10, 6, 15, 14, 10),
				Block.box(1, 6, 2, 5, 10, 10),Block.box(11, 6, 2, 15, 10, 10),Block.box(0, 5, 3, 5, 11, 4),Block.box(11, 5, 3, 16, 11, 4),
				Block.box(0, 5, 5, 5, 11, 6),Block.box(11, 5, 5, 16, 11, 6),Block.box(0, 10, 6, 5, 11, 11),Block.box(11, 10, 6, 16, 11, 11),
				Block.box(0, 12, 5, 5, 13, 11),Block.box(11, 12, 5, 16, 13, 11),Block.box(2, 2, 1, 14, 6, 1),Block.box(11, 2, 0, 12, 6, 1),
				Block.box(9, 2, 0, 10, 6, 1),Block.box(6, 2, 0, 7, 6, 1),Block.box(4, 2, 0, 5, 6, 1)
				).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get(), true, null, null, 200);

		private final VoxelShape shapeN;
		private final VoxelShape shapeS;
		private final VoxelShape shapeW;
		private final VoxelShape shapeE;
		private final int basefept;
		public final ManagedDirection inputSide;
		public final ManagedDirection outputSide;
		FluidGeneratorTypes(VoxelShape NShape, boolean supportsCoolant, ManagedDirection inputSide, ManagedDirection outputSide, int basefept){
			shapeN = NShape;
			shapeS = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, shapeN);
			shapeW = Utils.rotateShape(Direction.NORTH, Direction.WEST, shapeN);
			shapeE = Utils.rotateShape(Direction.NORTH, Direction.EAST, shapeN);
			this.inputSide = inputSide;
			this.outputSide = outputSide;
			this.basefept = basefept;
		}

	}
}
