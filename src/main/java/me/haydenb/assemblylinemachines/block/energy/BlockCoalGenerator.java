package me.haydenb.assemblylinemachines.block.energy;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.Utils;
import me.haydenb.assemblylinemachines.registry.Utils.Formatting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

public class BlockCoalGenerator extends BlockScreenBlockEntity<BlockCoalGenerator.TECoalGenerator>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 9, 3, 2, 16, 13),
			Block.box(14, 9, 3, 16, 16, 13),
			Block.box(3, 9, 14, 13, 16, 16),
			Block.box(0, 0, 0, 16, 9, 16),
			Block.box(2, 9, 2, 14, 16, 14)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();
	
	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	
	public BlockCoalGenerator() {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).sound(SoundType.METAL), 
				"coal_generator", null, true, Direction.NORTH, TECoalGenerator.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
		if(d == Direction.WEST) {
			return SHAPE_W;
		}else if(d == Direction.SOUTH) {
			return SHAPE_S;
		}else if(d == Direction.EAST) {
			return SHAPE_E;
		}else {
			return SHAPE_N;
		}
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
	}
	
	
	public static class TECoalGenerator extends EnergyMachine<ContainerCoalGenerator> implements ALMTicker<TECoalGenerator>, TOPProvider{

		
		private int genper = 0;
		private int timeremaining = 0;
		private int timer = 0;
		private boolean naphthaActive = false;
		
		public TECoalGenerator(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 1, new TranslatableComponent(Registry.getBlock("coal_generator").getDescriptionId()), Registry.getContainerId("coal_generator"), ContainerCoalGenerator.class, new EnergyProperties(false, true, 20000), pos, state);
		}
		
		public TECoalGenerator(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("coal_generator"), pos, state);
		}

		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {
			if(genper == 0) {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new TextComponent("§cIdle")).text(new TextComponent("0 FE/t"));
			}else {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new TextComponent("§aGenerating...")).text(new TextComponent("§a+" + Math.round((float)genper / 2f) + " FE/t"));
			}
			
			
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			genper = compound.getInt("assemblylinemachines:initgen");
			timeremaining = compound.getInt("assemblylinemachines:remgen");
			naphthaActive = compound.getBoolean("assemblylinemachines:naphtha");
		}
		
		@Override
		public void saveAdditional(CompoundTag compound) {
			
			compound.putInt("assemblylinemachines:initgen", genper);
			compound.putInt("assemblylinemachines:remgen", timeremaining);
			compound.putBoolean("assemblylinemachines:naphtha", naphthaActive);
			super.saveAdditional(compound);
		}
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) != 0) {
				return true;
			}
			return false;
		}

		@Override
		public void tick() {
			if(timer++ == 2) {
				timer = 0;
				if(amount < properties.getCapacity()) {
					boolean sendUpdates = false;
					if(timeremaining <= 0) {
						
						if(contents.get(0) != ItemStack.EMPTY) {
							
							
							if(getLevel().getBlockState(getBlockPos().above()).getBlock() == Registry.getBlock("naphtha_turbine") && getLevel().getBlockState(getBlockPos().relative(Direction.UP, 2)).getBlock() == Registry.getBlock("naphtha_fire")) {
								getLevel().removeBlock(getBlockPos().relative(Direction.UP, 2), false);
								getLevel().playSound(null, getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25f, 1f);
								naphthaActive = true;
							}else {
								naphthaActive = false;
							}
							int burnTime = Math.round((float) ForgeHooks.getBurnTime(contents.get(0), RecipeType.SMELTING) * 2f);
							if(burnTime != 0) {
								contents.get(0).shrink(1);
								genper = Math.round((float)(burnTime * 3f) / 90f);
								if(naphthaActive) {
									timeremaining = 60 * 4;
								}else {
									timeremaining = 60;
								}
								
								sendUpdates = true;
								
							}
						}
					}
					
					if(timeremaining > 0) {
						
						timeremaining--;
						amount += genper;
						if(amount > properties.getCapacity()) {
							amount = properties.getCapacity();
						}
						if(timeremaining == 0) {
							genper = 0;
						}
						sendUpdates = true;
					}
					
					if(sendUpdates) {
						sendUpdates();
					}
					
				}
			}
		}
		
	}
	
	public static class ContainerCoalGenerator extends ContainerALMBase<TECoalGenerator>{

		private static final Pair<Integer, Integer> UPGRADE_POS = new Pair<>(75, 34);
		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);
		
		public ContainerCoalGenerator(final int windowId, final Inventory playerInventory, final TECoalGenerator tileEntity) {
			super(Registry.getContainerType("coal_generator"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, UPGRADE_POS.getFirst(), UPGRADE_POS.getSecond(), tileEntity));
		}
		
		
		public ContainerCoalGenerator(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TECoalGenerator.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenCoalGenerator extends ScreenALMEnergyBased<ContainerCoalGenerator>{
		
		TECoalGenerator tsfm;
		
		public ScreenCoalGenerator(ContainerCoalGenerator screenContainer, Inventory inv,
				Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "coal_generator", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void renderTooltip(PoseStack mx, ItemStack stack, int mouseX, int mouseY) {
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			if(mouseX >= x+74 && mouseY >= y+33 && mouseX <= x+91 && mouseY <= y+50) {
				List<Component> tt = getTooltipFromItem(stack);
				
				int burnTime = Math.round((float) ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) * 2f);
				float mul;
				if(tsfm.naphthaActive) {
					mul = 240f;
				}else {
					mul = 60f;
				}
				tt.add(1, new TextComponent("Approx. " + Formatting.GENERAL_FORMAT.format((((float)burnTime * 3f) / 90f) * mul) + " FE Total").withStyle(ChatFormatting.YELLOW));
				tt.add(1, new TextComponent(Formatting.GENERAL_FORMAT.format(Math.round((float)(burnTime * 3) / 180f)) + " FE/t").withStyle(ChatFormatting.GREEN));
				super.renderComponentTooltip(mx, tt, mouseX, mouseY);
				return;
			}
			super.renderTooltip(mx, stack, mouseX, mouseY);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(tsfm.timeremaining != 0) {
				int prog2;
				if(tsfm.naphthaActive) {
					prog2 = Math.round(((float) tsfm.timeremaining / 240f) * 12F);
					super.blit(x+77, y+19 + (12 - prog2), 189, 52 + (12 - prog2), 13, prog2);
				}else {
					prog2 = Math.round(((float) tsfm.timeremaining / 60f) * 12F);
					super.blit(x+77, y+19 + (12 - prog2), 176, 52 + (12 - prog2), 13, prog2);
				}
				
			}
			
			if(tsfm.naphthaActive) {
				super.blit(x+75, y+52, 176, 64, 16, 16);
			}
			
			if(tsfm.genper == 0) {
				this.drawCenteredString(this.font, "0/t", x+111, y+38, 0xffffff);
			}else {
				this.drawCenteredString(this.font, "+" + Math.round((float)tsfm.genper / 2f) + "/t", x+111, y+38, 0x76f597);
			}
			
			
			
		}
		
		
	}
}
