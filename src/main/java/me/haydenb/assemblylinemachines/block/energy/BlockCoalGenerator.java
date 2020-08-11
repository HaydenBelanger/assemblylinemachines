package me.haydenb.assemblylinemachines.block.energy;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.helpers.AbstractMachine;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.ContainerALMBase;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine;
import me.haydenb.assemblylinemachines.helpers.EnergyMachine.ScreenALMEnergyBased;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Formatting;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.*;
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

		
		private int genper = 0;
		private int timeremaining = 0;
		private int timer = 0;
		private boolean naphthaActive = false;
		
		public TECoalGenerator(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 1, new TranslationTextComponent(Registry.getBlock("coal_generator").getTranslationKey()), Registry.getContainerId("coal_generator"), ContainerCoalGenerator.class, new EnergyProperties(false, true, 20000));
		}
		
		public TECoalGenerator() {
			this(Registry.getTileEntity("coal_generator"));
		}

		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			genper = compound.getInt("assemblylinemachines:initgen");
			timeremaining = compound.getInt("assemblylinemachines:remgen");
			naphthaActive = compound.getBoolean("assemblylinemachines:naphtha");
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			
			compound.putInt("assemblylinemachines:initgen", genper);
			compound.putInt("assemblylinemachines:remgen", timeremaining);
			compound.putBoolean("assemblylinemachines:naphtha", naphthaActive);
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
					if(timeremaining <= 0) {
						
						if(contents.get(0) != ItemStack.EMPTY) {
							
							
							if(world.getBlockState(pos.up()).getBlock() == Registry.getBlock("naphtha_turbine") && world.getBlockState(pos.offset(Direction.UP, 2)).getBlock() == Registry.getBlock("naphtha_fire")) {
								world.removeBlock(pos.offset(Direction.UP, 2), false);
								world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.25f, 1f);
								naphthaActive = true;
							}else {
								naphthaActive = false;
							}
							int burnTime = Math.round((float) ForgeHooks.getBurnTime(contents.get(0)) * 2f);
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
		
		public ContainerCoalGenerator(final int windowId, final PlayerInventory playerInventory, final TECoalGenerator tileEntity) {
			super(Registry.getContainerType("coal_generator"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0);
			
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
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "coal_generator", false, new Pair<>(14, 17), screenContainer.tileEntity, false);
			tsfm = screenContainer.tileEntity;
		}
		
		
		@Override
		protected void func_230457_a_(MatrixStack mx, ItemStack stack, int mouseX, int mouseY) {
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			if(mouseX >= x+74 && mouseY >= y+33 && mouseX <= x+91 && mouseY <= y+50) {
				List<ITextComponent> tt = func_231151_a_(stack);
				
				int burnTime = Math.round((float) ForgeHooks.getBurnTime(stack) * 2f);
				float mul;
				if(tsfm.naphthaActive) {
					mul = 240f;
				}else {
					mul = 60f;
				}
				tt.add(1, new StringTextComponent("Approx. " + Formatting.GENERAL_FORMAT.format((((float)burnTime * 3f) / 90f) * mul) + " FE Total").func_230532_e_().func_240699_a_(TextFormatting.YELLOW));
				tt.add(1, new StringTextComponent(Formatting.GENERAL_FORMAT.format(Math.round((float)(burnTime * 3) / 180f)) + " FE/t").func_230532_e_().func_240699_a_(TextFormatting.GREEN));
				super.func_238654_b_(mx, tt, mouseX, mouseY);
				return;
			}
			super.func_230457_a_(mx, stack, mouseX, mouseY);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			int x = (this.width - this.xSize) / 2;
			int y = (this.height - this.ySize) / 2;
			
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
