package me.haydenb.assemblylinemachines.block.energy;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.EnergyMachine;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.helpers.ICrankableMachine.ICrankableBlock;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class BlockCrankmill extends BlockScreenTileEntity<BlockCrankmill.TECrankmill> implements ICrankableBlock {

	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(7, 7, 2, 9, 9, 6),
			Block.makeCuboidShape(5, 5, 6, 11, 11, 8), Block.makeCuboidShape(0, 0, 0, 16, 16, 2),
			Block.makeCuboidShape(0, 0, 14, 16, 16, 16), Block.makeCuboidShape(0, 13, 2, 3, 16, 14),
			Block.makeCuboidShape(13, 13, 2, 16, 16, 14), Block.makeCuboidShape(0, 0, 2, 16, 3, 14),
			Block.makeCuboidShape(5, 5, 10, 11, 11, 14), Block.makeCuboidShape(2, 3, 6, 3, 13, 7),
			Block.makeCuboidShape(2, 3, 10, 3, 13, 11), Block.makeCuboidShape(13, 3, 6, 14, 13, 7),
			Block.makeCuboidShape(13, 3, 10, 14, 13, 11), Block.makeCuboidShape(3, 10, 6, 5, 11, 7),
			Block.makeCuboidShape(11, 10, 6, 13, 11, 7), Block.makeCuboidShape(11, 10, 10, 13, 11, 11),
			Block.makeCuboidShape(3, 10, 10, 5, 11, 11), Block.makeCuboidShape(4, 4, 8, 12, 12, 10))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	

	public BlockCrankmill() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0)
				.harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "crankmill", null, true, Direction.NORTH,
				TECrankmill.class);
		this.setDefaultState(
				this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING,
				context.getPlacementHorizontalFacing().getOpposite());
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
	
	@Override
	public boolean validSide(BlockState state, Direction dir) {
		if(state.get(HorizontalBlock.HORIZONTAL_FACING) == dir) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean needsGearbox() {
		return false;
	}
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}

	public static class TECrankmill extends EnergyMachine<ContainerCrankmill>
			implements ICrankableMachine, ITickableTileEntity {

		
		public int rfDif = 0;
		public int prevAmount = 0;
		private int timer = 0;
		
		public TECrankmill(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 0, (TranslationTextComponent) Registry.getBlock("crankmill").getNameTextComponent(), Registry.getContainerId("crankmill"), ContainerCrankmill.class,
					new EnergyProperties(false, true, 12000));
		}

		public TECrankmill() {
			this(Registry.getTileEntity("crankmill"));
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if (ForgeHooks.getBurnTime(stack) != 0) {
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
		public void tick() {
			
			if(timer++ == 120) {
				timer = 0;
				if(fept != 0) {
					fept = 0;
					sendUpdates();
				}
			}
			
		}
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING) == side.getOpposite()) {
				return super.getCapability(cap, side);
			}
			
			return LazyOptional.empty();
		}

	}

	public static class ContainerCrankmill extends ContainerALMBase<TECrankmill> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerCrankmill(final int windowId, final PlayerInventory playerInventory,
				final TECrankmill tileEntity) {
			super(Registry.getContainerType("crankmill"), windowId, tileEntity, playerInventory, PLAYER_INV_POS,
					PLAYER_HOTBAR_POS);
		}

		public ContainerCrankmill(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TECrankmill.class));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenCrankmill extends ScreenALMEnergyBased<ContainerCrankmill> {

		TECrankmill tsfm;

		public ScreenCrankmill(ContainerCrankmill screenContainer, PlayerInventory inv, ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73),
					"crankmill", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if(tsfm.fept == 0) {
				this.drawCenteredString(this.font, "0/t", x+114, y+38, 0xffffff);
			}else {
				super.blit(x+74, y+33, 176, 52, 18, 18);
				this.drawCenteredString(this.font, "+" + Formatting.FEPT_FORMAT.format(tsfm.fept) + "/t", x+114, y+38, 0x76f597);
			}
			

		}

	}
}
