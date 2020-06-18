package me.haydenb.assemblylinemachines.block.energy;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
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
import net.minecraft.nbt.CompoundNBT;
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

public class BlockCoalGenerator extends BlockScreenTileEntity<BlockCoalGenerator.TECoalGenerator>{

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 9, 3, 2, 16, 13),
			Block.makeCuboidShape(14, 9, 3, 16, 16, 13),
			Block.makeCuboidShape(3, 9, 14, 13, 16, 16),
			Block.makeCuboidShape(0, 0, 0, 16, 9, 16),
			Block.makeCuboidShape(2, 9, 2, 14, 16, 14)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();
	
	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	
	public BlockCoalGenerator() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), 
				"coal_generator", null, true, Direction.NORTH, TECoalGenerator.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		
		Direction d = state.get(HorizontalBlock.HORIZONTAL_FACING);
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
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HorizontalBlock.HORIZONTAL_FACING);
	}
	
	
	public static class TECoalGenerator extends EnergyMachine<ContainerCoalGenerator> implements ITickableTileEntity{

		
		private int initGen = 0;
		private int remGen = 0;
		private int timer = 0;
		
		public TECoalGenerator(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 1, (TranslationTextComponent) Registry.getBlock("coal_generator").getNameTextComponent(), Registry.getContainerId("coal_generator"), ContainerCoalGenerator.class, new EnergyProperties(false, true, 20000));
		}
		
		public TECoalGenerator() {
			this(Registry.getTileEntity("coal_generator"));
		}

		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:initgen")) {
				initGen = compound.getInt("assemblylinemachines:initgen");
			}
			if(compound.contains("assemblylinemachines:remgen")) {
				remGen = compound.getInt("assemblylinemachines:remgen");
			}
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			
			compound.putInt("assemblylinemachines:initgen", initGen);
			compound.putInt("assemblylinemachines:remgen", remGen);
			return super.write(compound);
		}
		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			if(ForgeHooks.getBurnTime(stack) != 0) {
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
					if(remGen <= 0) {
						
						if(contents.get(0) != ItemStack.EMPTY) {
							
							
							int burnTime = ForgeHooks.getBurnTime(contents.get(0));
							if(burnTime != 0) {
								contents.get(0).shrink(1);
								initGen = burnTime * 10;
								remGen = burnTime * 10;
								sendUpdates = true;
								
							}
						}
					}
					
					if(remGen != 0) {
						
						int max = 30;
						if(remGen < 30) {
							max = remGen;
						}
						
						remGen -= max;
						amount += max;
						if(amount > properties.getCapacity()) {
							amount = properties.getCapacity();
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
		
		public ContainerCoalGenerator(final int windowId, final PlayerInventory playerInventory, final TECoalGenerator tileEntity) {
			super(Registry.getContainerType("coal_generator"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS);
			
			this.addSlot(new AbstractMachine.SlotWithRestrictions(this.tileEntity, 0, UPGRADE_POS.getFirst(), UPGRADE_POS.getSecond(), tileEntity));
		}
		
		
		public ContainerCoalGenerator(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TECoalGenerator.class));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ScreenCoalGenerator extends ScreenALMEnergyBased<ContainerCoalGenerator>{
		
		TECoalGenerator tsfm;
		
		public ScreenCoalGenerator(ContainerCoalGenerator screenContainer, PlayerInventory inv,
				ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "coal_generator", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}
		
		@Override
		protected void renderTooltip(ItemStack stack, int mouseX, int mouseY) {
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if(mouseX >= x+74 && mouseY >= y+33 && mouseX <= x+91 && mouseY <= y+50) {
				List<String> tt = getTooltipFromItem(stack);
				tt.add(1, "§e" + Formatting.GENERAL_FORMAT.format(ForgeHooks.getBurnTime(stack) * 10) + " FE Total");
				tt.add(1, "§a15 FE/t");
				this.renderTooltip(tt, mouseX, mouseY);
				return;
			}
			
			super.renderTooltip(stack, mouseX, mouseY);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
			if(tsfm.initGen != 0) {
				int prog2 = Math.round(((float) tsfm.remGen / (float)tsfm.initGen) * 12F);
				super.blit(x+77, y+19 + (12 - prog2), 176, 52 + (12 - prog2), 13, prog2);
			}
			
			if(tsfm.remGen == 0) {
				this.drawCenteredString(this.font, "0/t", x+111, y+38, 0xffffff);
			}else {
				this.drawCenteredString(this.font, "+15/t", x+111, y+38, 0x76f597);
			}
			
			
			
		}
		
		
	}
}
