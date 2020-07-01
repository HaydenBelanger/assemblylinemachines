package me.haydenb.assemblylinemachines.block;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.helpers.AbstractMachine.*;
import me.haydenb.assemblylinemachines.helpers.BlockTileEntity.BlockScreenTileEntity;
import me.haydenb.assemblylinemachines.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.DebugOptions;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.*;
import me.haydenb.assemblylinemachines.util.StateProperties.BathCraftingFluids;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BlockInteractor extends BlockScreenTileEntity<BlockInteractor.TEInteractor> {

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.makeCuboidShape(0, 0, 0, 16, 3, 16),
			Block.makeCuboidShape(0, 13, 0, 16, 16, 16),
			Block.makeCuboidShape(0, 3, 0, 16, 13, 3),
			Block.makeCuboidShape(0, 3, 13, 16, 13, 16),
			Block.makeCuboidShape(2, 3, 3, 14, 13, 13)
			).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = General.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockInteractor() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL), "interactor",
				BlockInteractor.TEInteractor.class);
		this.setDefaultState(this.stateContainer.getBaseState().with(StateProperties.FLUID, BathCraftingFluids.NONE).with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(StateProperties.FLUID).add(HorizontalBlock.HORIZONTAL_FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
		if(facing == state.get(HorizontalBlock.HORIZONTAL_FACING)) {
			if(!world.isRemote() && world.getTileEntity(currentPos) instanceof TEInteractor) {
				TEInteractor te = (TEInteractor) world.getTileEntity(currentPos);
				if(te.bbProg != 0f) {
					te.bbProg = 0f;
					te.getWorld().sendBlockBreakProgress(-1, te.getPos(), -1);
				}
				
			}
		}
		return state;
	}
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

		if (world.getTileEntity(pos) instanceof TEInteractor) {
			TEInteractor te = (TEInteractor) world.getTileEntity(pos);
			te.origPlayerUUID = placer.getUniqueID();
			te.sendUpdates();

		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
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

	public static class TEInteractor extends SimpleMachine<ContainerInteractor> implements ITickableTileEntity {
		
		private static boolean hasSentInteractorMessage = false;
		
		private int timer = 0;
		private int nTimer = 0;
		private int mode = 0;
		private UUID origPlayerUUID = null;
		private FakePlayer fp = null;
		private boolean ordered = false;
		
		private float bbProg = 0f;
		private Boolean checkInteractMode = null;
		
		private static final List<Integer> UNORDERED_VALS = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8});

		public TEInteractor(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn, 9, new TranslationTextComponent(Registry.getBlock("interactor").getTranslationKey()), Registry.getContainerId("interactor"),
					ContainerInteractor.class);
		}

		public TEInteractor() {
			this(Registry.getTileEntity("interactor"));
		}

		
		@Override
		public void tick() {
			
			//Mode 0 - Place Mode
			//Mode 1 - Interact Mode
			//Mode 2 - Break Mode
			//Mode 3 - Attack Mode
			
			if(timer++ == 20) {
				timer = 0;
				
				if(!world.isRemote) {
					ItemStack stack = ItemStack.EMPTY;
					
					if(ordered) {
						for(int i = 0; i < 9; i++) {
							if(!contents.get(i).isEmpty()) {
								stack = contents.get(i);
								break;
							}
						}
					}else {
						Collections.shuffle(UNORDERED_VALS);
						for(Integer i : UNORDERED_VALS) {
							if(!contents.get(i).isEmpty()) {
								stack = contents.get(i);
								break;
							}
						}
					}
					
					if(fp == null) {
						if(origPlayerUUID != null) {
							fp = FakePlayerFactory.get(world.getServer().getWorld(world.func_234923_W_()), world.getServer().getPlayerProfileCache().getProfileByUUID(origPlayerUUID));
						}
					}
					if(fp != null && stack != null) {
						
						BlockPos offsetPos = pos.offset(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING));
						BlockState bs = world.getBlockState(offsetPos);
						
						if(mode == 0 && !stack.isEmpty()) {
							if(bs.getBlock() == Blocks.AIR) {
								Block block = Block.getBlockFromItem(stack.getItem());
								
								if(block != Blocks.AIR) {
									SoundType st = block.getDefaultState().getSoundType(world, offsetPos, fp);
									world.setBlockState(offsetPos, block.getDefaultState());
									world.playSound(null, offsetPos, st.getPlaceSound(), SoundCategory.BLOCKS, 1f, 1f);
									stack.shrink(1);
								}
								
							}
							
							
						}else {
							if((stack.isEmpty() && !fp.getHeldItemMainhand().isEmpty()) || (fp.getHeldItemMainhand() != stack)) {
								fp.inventory.setInventorySlotContents(fp.inventory.currentItem, stack);
							}
							
							if(mode == 1) {
								
								if(checkInteractMode == null) {
									checkInteractMode = ConfigHolder.COMMON.interactorInteractMode.get();
								}
								if(checkInteractMode) {
									try {
										fp.interactionManager.func_219441_a(fp, world, stack, Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5d, 0.5d, 0.5d), getBlockState().get(HorizontalBlock.HORIZONTAL_FACING).getOpposite(), offsetPos, false));
									}catch(Exception e) {
										
										DebugOptions db = ConfigHolder.COMMON.interactorInteractDebug.get();
										if(db == DebugOptions.BASIC) {
											AssemblyLineMachines.LOGGER.warn("Interactor set to Interact Mode @ " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " triggered exception: " + e.getMessage() + ".");
										}else if(db == DebugOptions.COMPLETE) {
											AssemblyLineMachines.LOGGER.warn("Interactor set to Interact Mode @ " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " triggered stack trace: ");
											e.printStackTrace();
										}
										
										if(db != DebugOptions.NONE && hasSentInteractorMessage == false) {
											hasSentInteractorMessage = true;
											AssemblyLineMachines.LOGGER.info("Tip: Interactor Interact Mode logging can be enabled or disabled in the config, as well as the feature disabled completely.");
										}
										
									}
									
									
									for(int i = 0; i < fp.inventory.getSizeInventory(); i++) {
										ItemStack nStack = fp.inventory.getStackInSlot(i);
										if(!nStack.isEmpty()) {
											if(nStack != stack) {
												General.spawnItem(nStack, offsetPos.up(), world);
											}
											fp.inventory.removeStackFromSlot(i);
										}
									}
								}
								
							}else if(mode == 2) {
								if(bs.getBlock() != Blocks.AIR) {
									
									bbProg = bbProg + (bs.getPlayerRelativeBlockHardness(fp, world, offsetPos) * 4f);
									if(bbProg >= 1f) {
										world.destroyBlock(offsetPos, true, fp);
										world.sendBlockBreakProgress(-1, offsetPos, -1);
										stack.damageItem(1, fp, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
										bbProg = 0f;
									}else {
										world.sendBlockBreakProgress(-1, offsetPos, (int) Math.floor(bbProg * 10f));
									}
								}
							}else {
								
								List<LivingEntity> list = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(offsetPos.offset(getBlockState().get(HorizontalBlock.HORIZONTAL_FACING))).grow(1));
								for(LivingEntity le : list) {
									boolean cont = true;
									if(le instanceof PlayerEntity) {
										
										PlayerEntity pe = (PlayerEntity) le;
										if(pe.getUniqueID().equals(fp.getUniqueID())) {
											cont = false;
										}
									}
									
									if(cont){
										
										
										float amt = 0.5f;
										
										if(!stack.isEmpty()) {
											for(AttributeModifier am : stack.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.field_233823_f_)) {
												amt += am.getAmount();
											}
										}
										le.attackEntityFrom(new EntityDamageSource("interactor", fp), amt);
										stack.damageItem(1, fp, (p_220038_0_) -> {p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);});
									}
									
								}
								
							}
						}
					}
				}
			}
			

		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);

			nTimer = compound.getInt("assemblylinemachines:ntimer");
			mode = compound.getInt("assemblylinemachines:mode");

			if (compound.contains("assemblylinemachines:uuidMost") && compound.contains("assemblylinemachines:uuidLeast")) {
				origPlayerUUID = compound.getUniqueId("assemblylinemachines:uuid");
			}

			ordered = compound.getBoolean("assemblylinemachines:ordered");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putInt("assemblylinemachines:mode", mode);

			if (origPlayerUUID != null) {
				compound.putUniqueId("assemblylinemachines:uuid", origPlayerUUID);
			}

			compound.putBoolean("assemblylinemachines:ordered", ordered);
			return super.write(compound);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
	}

	public static class ContainerInteractor extends ContainerALMBase<TEInteractor> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerInteractor(final int windowId, final PlayerInventory playerInventory, final TEInteractor tileEntity) {
			super(Registry.getContainerType("interactor"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new SlotWithRestrictions(tileEntity, (row * 3) + col, 62 + (18 * col), 12 + (18 * row), tileEntity));
				}
			}
		}

		public ContainerInteractor(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
			this(windowId, playerInventory, General.getTileEntity(playerInventory, data, TEInteractor.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenInteractor extends ScreenALMBase<ContainerInteractor> {

		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TEInteractor tsfm;
		private SimpleButton dirB;
		private SupplierWrapper dirW;
		private SimpleButton modeB;

		public ScreenInteractor(ContainerInteractor screenContainer, PlayerInventory inv, ITextComponent titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(175, 165), new Pair<>(11, 6), new Pair<>(11, 73), "interactor", false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void init() {
			super.init();

			int x = guiLeft;
			int y = guiTop;

			dirB = new SimpleButton(x + 48, y + 20, 176, 33, 11, 11, "", (button) -> {
				sendSelChange(tsfm.getPos());
			});

			dirW = new SupplierWrapper("Top-Left to Bottom-Right", "Random Selection", new Supplier<Boolean>() {

				@Override
				public Boolean get() {
					return tsfm.ordered;
				}
			});

			modeB = new SimpleButton(x + 48, y + 33, 176, 0, 11, 11, "", (button) -> {
				sendModeChange(tsfm.getPos());
			});
			
			addButton(dirB);
			addButton(modeB);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			if(dirW.supplier.get()) {
				super.blit(dirB.getX(), dirB.getY(), dirB.blitx, dirB.blity, dirB.sizex, dirB.sizey);
			}
			
			int add = -1;
			switch(tsfm.mode) {
			case 3:
				add = 22;
				break;
			case 2:
				add = 11;
				break;
			case 1:
				add = 0;
				break;
			}
			
			if(add != -1) {
				super.blit(modeB.getX(), modeB.getY(), modeB.blitx, modeB.blity + add, modeB.sizex, modeB.sizey);
			}
		};
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			if (mouseX >= modeB.getX() && mouseX <= modeB.getX() + modeB.sizex && mouseY >= modeB.getY() && mouseY <= modeB.getY() + modeB.sizey) {
				
				int x = (this.width - this.xSize) / 2;
				int y = (this.height - this.ySize) / 2;
				
				switch(tsfm.mode) {
				case 3:
					this.renderTooltip("Attack Mode", mouseX - x, mouseY - y);
					break;
				case 2:
					this.renderTooltip("Break Mode", mouseX - x, mouseY - y);
					break;
				case 1:
					this.renderTooltip("Interact Mode", mouseX - x, mouseY - y);
					break;
				default:
					this.renderTooltip("Place Mode", mouseX - x, mouseY - y);
					break;
				}
			}
			
			if (mouseX >= dirB.getX() && mouseX <= dirB.getX() + dirB.sizex && mouseY >= dirB.getY() && mouseY <= dirB.getY() + dirB.sizey) {
				
				int x = (this.width - this.xSize) / 2;
				int y = (this.height - this.ySize) / 2;
				
				this.renderTooltip(dirW.getTextFromSupplier(), mouseX - x, mouseY - y);
			}
		}
	}

	private static void sendSelChange(BlockPos pos) {
		PacketData pd = new PacketData("interactor_gui");
		pd.writeBlockPos("pos", pos);
		pd.writeString("button", "dir");

		HashPacketImpl.INSTANCE.sendToServer(pd);
	}

	private static void sendModeChange(BlockPos pos) {
		PacketData pd = new PacketData("interactor_gui");
		pd.writeBlockPos("pos", pos);
		pd.writeString("button", "mode");

		HashPacketImpl.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, World world) {

		if (pd.getCategory().equals("interactor_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			TileEntity tex = world.getTileEntity(pos);
			if (tex instanceof TEInteractor) {
				TEInteractor te = (TEInteractor) tex;
				String b = pd.get("button", String.class);
				if (b.equals("mode")) {
					if (te.mode == 3) {
						te.mode = 0;
					} else if(te.mode == 0) {
						if(ConfigHolder.COMMON.interactorInteractMode.get() == true) {
							te.mode++;
						}else {
							te.mode = 2;
						}
					}else {
						te.mode++;
					}
					te.sendUpdates();
				} else if (b.equals("dir")) {
					te.ordered = !te.ordered;
				}

				te.sendUpdates();
			}
		}
	}
}