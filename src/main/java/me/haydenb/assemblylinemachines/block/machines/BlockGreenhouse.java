package me.haydenb.assemblylinemachines.block.machines;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.SlotWithRestrictions;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.block.helpers.ManagedSidedMachine;
import me.haydenb.assemblylinemachines.block.machines.BlockGreenhouse.ContainerGreenhouse.BlitSlot;
import me.haydenb.assemblylinemachines.block.machines.BlockGreenhouse.TEGreenhouse;
import me.haydenb.assemblylinemachines.crafting.GreenhouseCrafting;
import me.haydenb.assemblylinemachines.crafting.GreenhouseFertilizerCrafting;
import me.haydenb.assemblylinemachines.item.ItemUpgrade;
import me.haydenb.assemblylinemachines.item.ItemUpgrade.Upgrades;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.*;
import net.minecraft.ChatFormatting;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class BlockGreenhouse extends BlockScreenBlockEntity<TEGreenhouse> {
	
	private static final HashMap<Item, Pair<Integer, Integer>> SPROUT_TINT = new HashMap<>();
	static {
		SPROUT_TINT.put(Items.ACACIA_SAPLING, Pair.of(0xa39f8c, 0x99b888));
		SPROUT_TINT.put(Items.BIRCH_SAPLING, Pair.of(0xdedede, 0x5f7d4f));
		SPROUT_TINT.put(Items.CRIMSON_FUNGUS, Pair.of(0x8f3c3c, 0xd42020));
		SPROUT_TINT.put(Items.DARK_OAK_SAPLING, Pair.of(0x42221e, 0x0d7516));
		SPROUT_TINT.put(Items.JUNGLE_SAPLING, Pair.of(0x2c4a2a, 0x35872f));
		SPROUT_TINT.put(Items.OAK_SAPLING, Pair.of(0x66553f, 0x3a6e2b));
		SPROUT_TINT.put(Items.SPRUCE_SAPLING, Pair.of(0x362c24, 0x28472b));
		SPROUT_TINT.put(Items.WARPED_FUNGUS, Pair.of(0x215157, 0x2593a1));
		
		SPROUT_TINT.put(Items.PEONY, Pair.of(0xc8a0db, 0));
		SPROUT_TINT.put(Items.ROSE_BUSH, Pair.of(0xc40000, 0));
		SPROUT_TINT.put(Items.LILAC, Pair.of(0xde9bf2, 0));
		SPROUT_TINT.put(Items.SUNFLOWER, Pair.of(0xffea00, 0));
		SPROUT_TINT.put(Items.WITHER_ROSE, Pair.of(0x4d4d4d, 0));
		SPROUT_TINT.put(Items.LILY_OF_THE_VALLEY, Pair.of(0xdbdbdb, 0));
		SPROUT_TINT.put(Items.CORNFLOWER, Pair.of(0x8791ff, 0));
		SPROUT_TINT.put(Items.OXEYE_DAISY, Pair.of(0xd9d9d9, 0));
		SPROUT_TINT.put(Items.ORANGE_TULIP, Pair.of(0xdb742a, 0));
		SPROUT_TINT.put(Items.PINK_TULIP, Pair.of(0xc7b09f, 0));
		SPROUT_TINT.put(Items.RED_TULIP, Pair.of(0xf20a0a, 0));
		SPROUT_TINT.put(Items.WHITE_TULIP, Pair.of(0xffffff, 0));
		SPROUT_TINT.put(Items.AZURE_BLUET, Pair.of(0xd9d9d9, 0));
		SPROUT_TINT.put(Items.ALLIUM, Pair.of(0xae91c7, 0));
		SPROUT_TINT.put(Items.BLUE_ORCHID, Pair.of(0xbabbff, 0));
		SPROUT_TINT.put(Items.POPPY, Pair.of(0xc20404, 0));
		SPROUT_TINT.put(Items.DANDELION, Pair.of(0xffff00, 0));
		SPROUT_TINT.put(Registry.getItem("prism_rose"), Pair.of(0xa2f0fa, 0));
		SPROUT_TINT.put(Registry.getItem("mandelbloom"), Pair.of(0x000000, 0));
		
		SPROUT_TINT.put(Items.TUBE_CORAL, Pair.of(0x7985d4, 0));
		SPROUT_TINT.put(Items.BRAIN_CORAL, Pair.of(0xcc9b9b, 0));
		SPROUT_TINT.put(Items.BUBBLE_CORAL, Pair.of(0x632678, 0));
		SPROUT_TINT.put(Items.FIRE_CORAL, Pair.of(0xeb1010, 0));
		SPROUT_TINT.put(Items.HORN_CORAL, Pair.of(0xd7f20a, 0));
	}
	
	public static final EnumProperty<Sprout> SPROUT = EnumProperty.create("sprout", Sprout.class);
	public static final EnumProperty<Soil> SOIL = EnumProperty.create("soil", Soil.class);
	public static final BooleanProperty BLACKOUT = BooleanProperty.create("blackout");
	public static final BooleanProperty LAMP = BooleanProperty.create("lamp");
	
	private static final VoxelShape SHAPE = Stream.of(
			Block.box(0, 0, 0, 16, 2, 16),Block.box(2, 2, 1, 14, 8, 2),Block.box(1, 2, 2, 2, 8, 14),Block.box(2, 2, 14, 14, 8, 15),
			Block.box(14, 2, 2, 15, 8, 14),Block.box(0, 2, 0, 2, 9, 2),Block.box(0, 2, 14, 2, 9, 16),Block.box(14, 2, 14, 16, 9, 16),
			Block.box(14, 2, 0, 16, 9, 2),Block.box(2, 8, 0, 3, 9, 2),Block.box(0, 8, 2, 2, 9, 3),Block.box(0, 8, 13, 2, 9, 14),
			Block.box(2, 8, 14, 3, 9, 16),Block.box(13, 8, 0, 14, 9, 2),Block.box(14, 8, 2, 16, 9, 3),Block.box(14, 8, 13, 16, 9, 14),
			Block.box(13, 8, 14, 14, 9, 16),Block.box(3, 3, 0, 13, 13, 1),Block.box(0, 3, 3, 1, 13, 13),Block.box(3, 3, 15, 13, 13, 16),
			Block.box(15, 3, 3, 16, 13, 13),Block.box(3, 9, 1, 13, 9, 15),Block.box(1, 9, 3, 2, 9, 13),Block.box(2, 9, 2, 3, 9, 14),
			Block.box(13, 9, 2, 14, 9, 14),Block.box(14, 9, 3, 15, 9, 13),Block.box(0, 9, 0, 16, 16, 16),Block.box(0, 9, 0, 3, 16, 0),
			Block.box(13, 9, 0, 16, 16, 0),Block.box(3, 13, 0, 13, 16, 0),Block.box(0, 9, 0, 0, 16, 3),Block.box(0, 13, 3, 0, 16, 13),
			Block.box(0, 9, 13, 0, 16, 16),Block.box(13, 9, 16, 16, 16, 16),Block.box(0, 9, 16, 3, 16, 16),Block.box(3, 13, 16, 13, 16, 16),
			Block.box(16, 9, 13, 16, 16, 16),Block.box(16, 13, 3, 16, 16, 13),Block.box(16, 9, 0, 16, 16, 3),Block.box(0, 16, 0, 16, 16, 16)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	
	public BlockGreenhouse() {
		super(Properties.of(Material.METAL).strength(4f, 15f).noOcclusion().dynamicShape().lightLevel((bs) -> bs.getValue(LAMP) ? 15 : 0).sound(SoundType.METAL), "greenhouse", TEGreenhouse.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(StateProperties.MACHINE_ACTIVE, false).setValue(SOIL, Soil.EMPTY).setValue(SPROUT, Sprout.EMPTY).setValue(LAMP, false).setValue(BLACKOUT, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.MACHINE_ACTIVE).add(SOIL).add(SPROUT).add(BLACKOUT).add(LAMP);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	public static enum Sprout implements StringRepresentable{
		EMPTY, CACTUS((s) -> true), CORRUPT_SPROUT((s) -> false), NETHER_SPROUT((s) -> false), SAPLING((s) -> s != Soil.SOUL_SAND, Upgrades.GREENHOUSE_ARBORIST, LazyOptional.of(() -> SPROUT_TINT.get(Items.OAK_SAPLING))), 
		MUSHROOM((s) -> false), SPROUT((s) -> true), SUGAR_CANE((s) -> true), CHORUS((s) -> false), BRAIN_CACTUS((s) -> false), CHAOSBARK_SAPLING((s) -> false, Upgrades.GREENHOUSE_ARBORIST),
		FLOWER((s) -> s != Soil.CORRUPT, Upgrades.GREENHOUSE_FLORIST, LazyOptional.of(() -> SPROUT_TINT.get(Items.DANDELION))),
		CORAL((s) -> true, null, LazyOptional.of(() -> SPROUT_TINT.get(Items.TUBE_CORAL)));
		
		public final LazyOptional<Pair<Integer, Integer>> tintDefault;
		public final Predicate<Soil> sunlightReq;
		public final Upgrades requiredSpecialization;
		
		Sprout(){
			this(null);
		}
		
		Sprout(Predicate<Soil> sunlightReq){
			this(sunlightReq, null);
		}
		
		Sprout(Predicate<Soil> sunlightReq, Upgrades requiredSpecialization){
			this(sunlightReq, requiredSpecialization, LazyOptional.empty());
		}
		
		Sprout(Predicate<Soil> sunlightReq, Upgrades requiredSpecialization, LazyOptional<Pair<Integer, Integer>> tintDefault){
			this.tintDefault = tintDefault;
			this.sunlightReq = sunlightReq;
			this.requiredSpecialization = requiredSpecialization;
		}
		
		@Override
		public String getSerializedName() {
			return this.toString().toLowerCase();
		}
		
		public int getTint(Item i, int tintIndex) {
			Pair<Integer, Integer> mapped = tintDefault.map((def) -> {
				return SPROUT_TINT.getOrDefault(i, def);
			}).orElse(Pair.of(0, 0));
			return tintIndex == 0 ? mapped.getFirst() : mapped.getSecond();
		}
		
	}
	
	public static enum Soil implements StringRepresentable{
		EMPTY(Lazy.of(() -> null)), DIRT(Lazy.of(() -> Ingredient.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT))), 
		MYCELIUM(Lazy.of(() -> Ingredient.of(Blocks.MYCELIUM))), SAND(Lazy.of(() -> Ingredient.of(TagKey.create(Keys.ITEMS, new ResourceLocation("minecraft", "sand"))))),
		SOUL_SAND(Lazy.of(() -> Ingredient.of(TagKey.create(Keys.ITEMS, new ResourceLocation("minecraft", "soul_fire_base_blocks")))), Upgrades.GREENHOUSE_INTERDIM), 
		CORRUPT(Lazy.of(() -> Ingredient.of(Registry.getBlock("corrupt_dirt"), Registry.getBlock("corrupt_grass"))), Upgrades.GREENHOUSE_INTERDIM), END_STONE(Lazy.of(() -> Ingredient.of(Blocks.END_STONE)), Upgrades.GREENHOUSE_INTERDIM);
		
		public final Lazy<Ingredient> soil;
		public final Upgrades requiredSpecialization;
		
		Soil(Lazy<Ingredient> soil){
			this(soil, null);
		}
		
		Soil(Lazy<Ingredient> soil, Upgrades requiredSpecialization){
			this.soil = soil;
			this.requiredSpecialization = requiredSpecialization;
		}
		
		public String getSerializedName() {
			return this.toString().toLowerCase();
		}
	}
	
	public static class TEGreenhouse extends ManagedSidedMachine<ContainerGreenhouse> implements ALMTicker<TEGreenhouse>{
		
		public FluidStack tank = FluidStack.EMPTY;
		public int currentFertilizerRemaining = 0;
		public int currentFertilizerMax = 0;
		public int currentFertilizerMultiplier = 0;
		private int timer = 0;
		private int nTimer = 20;
		private ItemStack output = ItemStack.EMPTY;
		public float progress = 0f;
		public float cycles = 0f;
		private LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> IFluidHandlerBypass.getSimpleOneTankHandler((fs) -> fs.getFluid().equals(Fluids.WATER), 4000, (oFs) -> {
			if(oFs.isPresent()) tank = oFs.get();
			return tank;
		}, (v) -> this.sendUpdates(), false));
		
		public TEGreenhouse(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 7, new TranslatableComponent(Registry.getBlock("greenhouse").getDescriptionId()), Registry.getContainerId("greenhouse"), ContainerGreenhouse.class, new EnergyProperties(true, false, 100000), pos, state);
		}
		
		public TEGreenhouse(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("greenhouse"), pos, state);
		}

		@Override
		public void tick() {
			if(!level.isClientSide) {
				if(timer++ >= nTimer) {
					timer = 0;
					boolean sendUpdates = false;
					int baseCost = switch(getUpgradeAmount(Upgrades.UNIVERSAL_SPEED)) {
					case 3:
						nTimer = 2;
						yield 320;
					case 2:
						nTimer = 4;
						yield 280;
					case 1:
						nTimer = 8;
						yield 260;
					default:
						nTimer = 16;
						yield 240;
					};
					
					if(this.getUpgradeAmount(Upgrades.GREENHOUSE_LAMP) != 0 || this.getUpgradeAmount(Upgrades.GREENHOUSE_BLACKOUT) != 0) {
						baseCost *= 2.5;
						nTimer = Math.round((int) nTimer * 0.75f);
					}
					
					if(currentFertilizerRemaining <= 0) {
						if(currentFertilizerMax != 0 || currentFertilizerMultiplier != 0) {
							currentFertilizerMax = 0;
							currentFertilizerMultiplier = 0;
							sendUpdates = true;
						}
						
						GreenhouseFertilizerCrafting fertilizer = this.getLevel().getRecipeManager().getRecipeFor(GreenhouseFertilizerCrafting.FERTILIZER_RECIPE, this, this.getLevel()).orElse(null);
						if(fertilizer != null) {
							currentFertilizerRemaining = fertilizer.usesPerItem;
							currentFertilizerMax = fertilizer.usesPerItem;
							currentFertilizerMultiplier = fertilizer.multiplication;
							this.getItem(2).shrink(1);
							sendUpdates = true;
						}
					}
					
					if(currentFertilizerRemaining > 0 && output.isEmpty()) {
						GreenhouseCrafting recipe = this.getLevel().getRecipeManager().getRecipeFor(GreenhouseCrafting.GREENHOUSE_RECIPE, this, this.getLevel()).orElse(null);
						if(recipe != null) {
							output = recipe.assemble(this);
							if(!output.isEmpty()) {
								sendUpdates = true;
							}
						}
					}
					
					if(!output.isEmpty()) {
						if(amount - baseCost >= 0) {
							if(progress >= cycles) {
								if(ScreenMath.doFit(this.getItem(0), output, (is) -> this.setItem(0, is), (i) -> this.getItem(0).grow(i))) {
									output = ItemStack.EMPTY;
									progress = cycles = 0;
									sendUpdates = true;
								}
							}else {
								amount -= baseCost;
								fept = (float) baseCost / (float) nTimer;
								progress++;
								sendUpdates = true;
							}
						}
					}else {
						if(this.getBlockState().getValue(SOIL) != Soil.EMPTY || this.getBlockState().getValue(SPROUT) != Sprout.EMPTY || this.getBlockState().getValue(StateProperties.MACHINE_ACTIVE) == true) {
							this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(SOIL, Soil.EMPTY).setValue(SPROUT, Sprout.EMPTY).setValue(StateProperties.MACHINE_ACTIVE, false));
							sendUpdates = true;
						}
					}
					
					boolean blackout = this.getUpgradeAmount(Upgrades.GREENHOUSE_BLACKOUT) != 0;
					if(this.getBlockState().getValue(BLACKOUT) != blackout) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BLACKOUT, blackout));
						sendUpdates = true;
					}
					
					boolean lamp = this.getUpgradeAmount(Upgrades.GREENHOUSE_LAMP) != 0;
					if(this.getBlockState().getValue(LAMP) != lamp) {
						this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(LAMP, lamp));
						sendUpdates = true;
					}
					
					
					if(sendUpdates) {
						sendUpdates();
					}
				}
			}
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(slot > 3) {
				if(stack.getItem() instanceof ItemUpgrade) {
					Upgrades upgrade = Upgrades.match(stack);
					if(upgrade == Upgrades.GREENHOUSE_BLACKOUT && this.getUpgradeAmount(Upgrades.GREENHOUSE_LAMP) != 0) {
						return false;
					}else if(upgrade == Upgrades.GREENHOUSE_LAMP && this.getUpgradeAmount(Upgrades.GREENHOUSE_BLACKOUT) != 0) {
						return false;
					}else {
						return true;
					}
				}
				return false;
			}
			return super.isAllowedInSlot(slot, stack);
		}
		
		public int getUpgradeAmount(Upgrades upgrade) {
			int ii = 0;
			for (int i = 4; i < 7; i++) {
				if (Upgrades.match(contents.get(i)) == upgrade) {
					ii++;
				}
			}
			return ii;
		}
		
		public int getEffectiveLight() {
			if(this.getUpgradeAmount(Upgrades.GREENHOUSE_LAMP) != 0) return 15;
			return this.getLevel().getBrightness(LightLayer.SKY, this.getBlockPos()) - this.getLevel().getSkyDarken();
		}
		
		public int getEffectiveDarkness() {
			if(this.getUpgradeAmount(Upgrades.GREENHOUSE_BLACKOUT) != 0) return 0;
			return Math.max(this.getLevel().getBrightness(LightLayer.SKY, this.getBlockPos()) - this.getLevel().getSkyDarken(), 
					this.getLevel().getBrightness(LightLayer.BLOCK, this.getBlockPos()));
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap) {
			return this.getCapability(cap, null);
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return handler.cast();
			return super.getCapability(cap, side);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			tank = FluidStack.loadFluidStackFromNBT(compound.getCompound("assemblylinemachines:tank"));
			currentFertilizerRemaining = compound.getInt("assemblylinemachines:fertilizerremaining");
			currentFertilizerMax = compound.getInt("assemblylinemachines:fertilizermax");
			currentFertilizerMultiplier = compound.getInt("assemblylinemachines:fertilizermultiplier");
			output = ItemStack.of(compound.getCompound("assemblylinemachines:output"));
			progress = compound.getFloat("assemblylinemachines:progress");
			cycles = compound.getFloat("assemblylinemachines:cycles");
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			compound.put("assemblylinemachines:tank", tank.writeToNBT(new CompoundTag()));
			compound.putInt("assemblylinemachines:fertilizerremaining", currentFertilizerRemaining);
			compound.putInt("assemblylinemachines:fertilizermax", currentFertilizerMax);
			compound.putInt("assemblylinemachines:fertilizermultiplier", currentFertilizerMultiplier);
			compound.put("assemblylinemachines:output", output.save(new CompoundTag()));
			compound.putFloat("assemblylinemachines:progress", progress);
			compound.putFloat("assemblylinemachines:cycles", cycles);
			
			super.saveAdditional(compound);
		}
	}
	
	public static class ContainerGreenhouse extends ContainerALMBase<TEGreenhouse>{
		
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		private final HashMap<Integer, BlitSlot> blitSlots = new HashMap<>();
		
		public ContainerGreenhouse(int windowId, Inventory playerInventory, TEGreenhouse tileEntity) {
			super(Registry.getContainerType("greenhouse"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 1, 3);
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 0, 119, 34, tileEntity, true));
			this.addSlot(new BlitSlot(1, 75, 34, 176, 104));
			this.addSlot(new BlitSlot(2, 54, 34, 176, 120));
			this.addSlot(new BlitSlot(3, 75, 55, 176, 136));
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 4, 149, 21, tileEntity));
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 5, 149, 39, tileEntity));
			this.addSlot(new SlotWithRestrictions(this.tileEntity, 6, 149, 57, tileEntity));
		
		}
		
		public ContainerGreenhouse(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEGreenhouse.class));
		}
		
		public class BlitSlot extends SlotWithRestrictions{

			public final int blitX;
			public final int blitY;
			public final int rendX;
			public final int rendY;
			
			public BlitSlot(int index, int xPosition, int yPosition, int blitX, int blitY) {
				super(ContainerGreenhouse.this.tileEntity, index, xPosition, yPosition, ContainerGreenhouse.this.tileEntity);
				this.blitX = blitX;
				this.blitY = blitY;
				this.rendX = xPosition;
				this.rendY = yPosition;
				
				blitSlots.put(index, this);
			}
			
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenGreenhouse extends ScreenALMEnergyBased<ContainerGreenhouse>{
		
		private HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		private TEGreenhouse tsfm;
		private ContainerGreenhouse container;
		
		public ScreenGreenhouse(ContainerGreenhouse screenContainer, Inventory inv, Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "greenhouse", false, new Pair<>(14, 17), screenContainer.tileEntity, true);
			this.tsfm = screenContainer.tileEntity;
			this.container = screenContainer;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(!tsfm.tank.isEmpty()) {
				TextureAtlasSprite tas = spriteMap.computeIfAbsent(tsfm.tank.getFluid(), (fluid) -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture()));
				if(tsfm.tank.getFluid().equals(Fluids.WATER)) {
					RenderSystem.setShaderColor(0.2470f, 0.4627f, 0.8941f, 1f);
				}else {
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				}
				super.blit(x+41, y+23, 37, 37, 37, tas);
			}
			
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
			int fprog = Math.round(((float) tsfm.tank.getAmount() / 4000) * 37f);
			super.blit(x+41, y+23, 176, 67, 8, 37 - fprog);
			
			int prog = Math.round((tsfm.progress/tsfm.cycles) * 18f);
			super.blit(x+94, y+36, 176, 52, prog, 12);
			
			if(tsfm.currentFertilizerRemaining > 0 && tsfm.currentFertilizerMax > 0) {
				int uProg = Math.round(((float)tsfm.currentFertilizerRemaining / (float)tsfm.currentFertilizerMax) * 16f);
				super.blit(x+54, y+28, 176, 64, uProg, 3);
			}
			
			for(Entry<Integer, BlitSlot> bl : container.blitSlots.entrySet()) {
				if(tsfm.getItem(bl.getKey()).isEmpty()) {
					super.blit(x+bl.getValue().rendX, y+bl.getValue().rendY, bl.getValue().blitX, bl.getValue().blitY, 16, 16);
				}
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
			
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if(ScreenMath.isMouseBetween(x, y, mouseX, mouseY, 41, 23, 48, 59)) {
				List<String> tooltip;
				if(!tsfm.tank.isEmpty()) {
					String name = tsfm.tank.getDisplayName().getString();
					if(Screen.hasShiftDown()) {
						tooltip = List.of(name, FormattingHelper.FEPT_FORMAT.format(tsfm.tank.getAmount()) + " mB");
					}else {
						tooltip = List.of(name, FormattingHelper.FEPT_FORMAT.format((double) tsfm.tank.getAmount() / 1000d) + " B");
					}
				}else {
					tooltip = List.of("Empty");
				}
				this.renderComponentTooltip(tooltip, mouseX - x, mouseY - y);
			}
			
			if(ScreenMath.isMouseBetween(x, y, mouseX, mouseY, 54, 28, 69, 30)) {
				List<Component> fertilizer = getFertilizerInformation();
				if(!fertilizer.isEmpty()) this.renderComponentTooltip(mx, fertilizer, mouseX - x, mouseY - y);
			}
		}
		
		private List<Component> getFertilizerInformation(){
			if(tsfm.currentFertilizerRemaining <= 0) return List.of();
			List<Component> list = new ArrayList<>();
			if(tsfm.currentFertilizerMultiplier != 1) list.add(new TextComponent("Fertilizer Power: ").withStyle(ChatFormatting.BLUE).append(new TextComponent(tsfm.currentFertilizerMultiplier + "x Yield").withStyle(ChatFormatting.AQUA)));
			list.add(new TextComponent("Uses Remaining: ").withStyle(ChatFormatting.DARK_GREEN).append(new TextComponent(tsfm.currentFertilizerRemaining + "/" + tsfm.currentFertilizerMax).withStyle(ChatFormatting.GREEN)));
			return list;
		}
	}
}
