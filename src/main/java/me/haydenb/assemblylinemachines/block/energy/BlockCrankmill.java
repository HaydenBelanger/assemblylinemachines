package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import mcjty.theoneprobe.api.*;
import me.haydenb.assemblylinemachines.block.helpers.*;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.plugins.PluginTOP.PluginTOPRegistry.TOPProvider;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
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

public class BlockCrankmill extends BlockScreenBlockEntity<BlockCrankmill.TECrankmill> {

	private static final VoxelShape SHAPE_N = Stream.of(Block.box(7, 7, 2, 9, 9, 6),
			Block.box(5, 5, 6, 11, 11, 8), Block.box(0, 0, 0, 16, 16, 2),
			Block.box(0, 0, 14, 16, 16, 16), Block.box(0, 13, 2, 3, 16, 14),
			Block.box(13, 13, 2, 16, 16, 14), Block.box(0, 0, 2, 16, 3, 14),
			Block.box(5, 5, 10, 11, 11, 14), Block.box(2, 3, 6, 3, 13, 7),
			Block.box(2, 3, 10, 3, 13, 11), Block.box(13, 3, 6, 14, 13, 7),
			Block.box(13, 3, 10, 14, 13, 11), Block.box(3, 10, 6, 5, 11, 7),
			Block.box(11, 10, 6, 13, 11, 7), Block.box(11, 10, 10, 13, 11, 11),
			Block.box(3, 10, 10, 5, 11, 11), Block.box(4, 4, 8, 12, 12, 10))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	

	public BlockCrankmill() {
		super(Block.Properties.of(Material.METAL).strength(3f, 15f).sound(SoundType.METAL), "crankmill", null, true, Direction.NORTH,
				TECrankmill.class);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
				context.getHorizontalDirection().getOpposite());
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
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
	}

	public static class TECrankmill extends EnergyMachine<ContainerCrankmill>
			implements ICrankableMachine, ALMTicker<TECrankmill>, TOPProvider {

		
		public int rfDif = 0;
		public int prevAmount = 0;
		private int timer = 0;
		
		public TECrankmill(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 0, new TranslatableComponent(Registry.getBlock("crankmill").getDescriptionId()), Registry.getContainerId("crankmill"), ContainerCrankmill.class,
					new EnergyProperties(false, true, 12000), pos, state);
		}

		public TECrankmill(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("crankmill"), pos, state);
		}
		
		@Override
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data) {
			
			if(fept == 0) {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new TextComponent("Idle").withStyle(ChatFormatting.RED)).text(new TextComponent("0 FE/t"));
			}else {
				probeInfo.horizontal().item(new ItemStack(Items.REDSTONE)).vertical().text(new TextComponent("Generating...").withStyle(ChatFormatting.GREEN)).text(new TextComponent("+" + FormattingHelper.FEPT_FORMAT.format(fept) + " FE/t").withStyle(ChatFormatting.GREEN));
			}
			
		}
		
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) != 0) {
				return true;
			}
			return false;
		}

		@Override
		public boolean perform() {
			int max = 750;
			if (amount + max > properties.getCapacity()) {
				max = properties.getCapacity() - amount;
			}

			if (max == 0) {
				timer = 0;
				if(fept != 0) {
					fept = 0;
					sendUpdates();
				}
				
				return false;
			}

			amount += max;
			
			fept  = (float) 700 / (float) timer;
			timer = 0;
			sendUpdates();
			return true;
		}
		
		@Override
		public boolean validFrom(Direction dir) {
			return this.getBlockState().getValue(HorizontalDirectionalBlock.FACING) == dir;
		}
		
		@Override
		public boolean requiresGearbox() {
			return false;
		}

		@Override
		public void tick() {
			
			if(timer++ == 120) {
				timer = 0;
				if(fept != 0) {
					fept = 0;
					sendUpdates();
				}
			}
			
		}

	}

	public static class ContainerCrankmill extends ContainerALMBase<TECrankmill> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerCrankmill(final int windowId, final Inventory playerInventory,
				final TECrankmill tileEntity) {
			super(Registry.getContainerType("crankmill"), windowId, tileEntity, playerInventory, PLAYER_INV_POS,
					PLAYER_HOTBAR_POS, 0);
		}

		public ContainerCrankmill(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TECrankmill.class));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenCrankmill extends ScreenALMEnergyBased<ContainerCrankmill> {

		TECrankmill tsfm;

		public ScreenCrankmill(ContainerCrankmill screenContainer, Inventory inv, Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73),
					"crankmill", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.imageWidth) / 2;
			int y = (this.height - this.imageHeight) / 2;
			
			if(tsfm.fept == 0) {
				this.drawCenteredString(this.font, "0/t", x+114, y+38, 0xffffff);
			}else {
				super.blit(x+74, y+33, 176, 52, 18, 18);
				this.drawCenteredString(this.font, "+" + FormattingHelper.FEPT_FORMAT.format(tsfm.fept) + "/t", x+114, y+38, 0x76f597);
			}
			

		}

	}
}
