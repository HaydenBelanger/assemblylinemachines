package me.haydenb.assemblylinemachines.block.machines.mob;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.helpers.ALMTicker;
import me.haydenb.assemblylinemachines.block.helpers.AbstractMachine.*;
import me.haydenb.assemblylinemachines.block.helpers.BlockTileEntity.BlockScreenBlockEntity;
import me.haydenb.assemblylinemachines.block.helpers.SimpleMachine;
import me.haydenb.assemblylinemachines.registry.*;
import me.haydenb.assemblylinemachines.registry.BathCraftingFluid.BathCraftingFluids;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.DebugOptions;
import me.haydenb.assemblylinemachines.registry.PacketHandler.PacketData;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton;
import me.haydenb.assemblylinemachines.registry.Utils.TrueFalseButton.TrueFalseButtonSupplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BlockInteractor extends BlockScreenBlockEntity<BlockInteractor.TEInteractor> {

	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(0, 0, 0, 16, 3, 16),
			Block.box(0, 13, 0, 16, 16, 16),
			Block.box(0, 3, 0, 16, 13, 3),
			Block.box(0, 3, 13, 16, 13, 16),
			Block.box(2, 3, 3, 14, 13, 13)
			).reduce((v1, v2) -> {return Shapes.join(v1, v2, BooleanOp.OR);}).get();

	private static final VoxelShape SHAPE_S = Utils.rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_N);
	private static final VoxelShape SHAPE_W = Utils.rotateShape(Direction.NORTH, Direction.WEST, SHAPE_N);
	private static final VoxelShape SHAPE_E = Utils.rotateShape(Direction.NORTH, Direction.EAST, SHAPE_N);
	
	public BlockInteractor() {
		super(Block.Properties.of(Material.METAL).strength(4f, 15f).sound(SoundType.METAL), "interactor",
				BlockInteractor.TEInteractor.class);
		this.registerDefaultState(this.stateDefinition.any().setValue(BathCraftingFluid.FLUID, BathCraftingFluids.NONE).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}
	
	

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BathCraftingFluid.FLUID).add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
		if(facing == state.getValue(HorizontalDirectionalBlock.FACING)) {
			if(!world.isClientSide() && world.getBlockEntity(currentPos) instanceof TEInteractor) {
				TEInteractor te = (TEInteractor) world.getBlockEntity(currentPos);
				if(te.bbProg != 0f) {
					te.bbProg = 0f;
					te.getLevel().destroyBlockProgress(-1, te.getBlockPos(), -1);
				}
				
			}
		}
		return state;
	}
	
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {

		if (world.getBlockEntity(pos) instanceof TEInteractor) {
			TEInteractor te = (TEInteractor) world.getBlockEntity(pos);
			te.origPlayerUUID = placer.getUUID();
			te.sendUpdates();

		}
		super.setPlacedBy(world, pos, state, placer, stack);
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

	public static class TEInteractor extends SimpleMachine<ContainerInteractor> implements ALMTicker<TEInteractor> {
		
		private static boolean hasSentInteractorMessage = false;
		
		private int timer = 0;
		private int nTimer = 0;
		private int mode = 0;
		private UUID origPlayerUUID = null;
		private WeakReference<FakePlayer> fpRef = null;
		private boolean ordered = false;
		
		private float bbProg = 0f;
		private Boolean checkInteractMode = null;
		
		private static final List<Integer> UNORDERED_VALS = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8});

		public TEInteractor(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, 9, new TranslatableComponent(Registry.getBlock("interactor").getDescriptionId()), Registry.getContainerId("interactor"),
					ContainerInteractor.class, pos, state);
		}

		public TEInteractor(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("interactor"), pos, state);
		}

		
		@Override
		public void tick() {
			
			//Mode 0 - Place Mode
			//Mode 1 - Interact Mode
			//Mode 2 - Break Mode
			//Mode 3 - Attack Mode
			
			if(timer++ == 20) {
				timer = 0;
				
				if(!level.isClientSide) {
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
					
					if(fpRef.get() == null) {
						if(origPlayerUUID != null) {
							fpRef = new WeakReference<>(FakePlayerFactory.get(this.getLevel().getServer().getLevel(this.getLevel().dimension()), this.getLevel().getServer().getProfileCache().get(origPlayerUUID).orElse(null)));
						}
					}
					if(fpRef.get() != null && stack != null) {
						FakePlayer fp = fpRef.get();
						BlockPos offsetPos = this.getBlockPos().relative(getBlockState().getValue(HorizontalDirectionalBlock.FACING));
						BlockState bs = this.getLevel().getBlockState(offsetPos);
						
						if(mode == 0) {
							
							if(!stack.isEmpty()) {
								if(bs.getBlock() == Blocks.AIR) {
									Block block = Block.byItem(stack.getItem());
									
									if(block != Blocks.AIR) {
										SoundType st = block.defaultBlockState().getSoundType(this.getLevel(), offsetPos, fp);
										this.getLevel().setBlockAndUpdate(offsetPos, block.defaultBlockState());
										this.getLevel().playSound(null, offsetPos, st.getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
										stack.shrink(1);
									}
									
								}
							}
							
							
							
						}else {
							if((stack.isEmpty() && !fp.getMainHandItem().isEmpty()) || (fp.getMainHandItem() != stack)) {
								fp.getInventory().setItem(fp.getInventory().selected, stack);
							}
							
							if(mode == 1) {
								
								if(checkInteractMode == null) {
									checkInteractMode = ConfigHolder.COMMON.interactorInteractMode.get();
								}
								if(checkInteractMode) {
									try {
										fp.gameMode.useItemOn(fp, this.getLevel(), stack, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(0.5d, 0.5d, 0.5d), getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite(), offsetPos, false));
									}catch(Exception e) {
										
										DebugOptions db = ConfigHolder.COMMON.interactorInteractDebug.get();
										if(db == DebugOptions.BASIC) {
											AssemblyLineMachines.LOGGER.warn("Interactor set to Interact Mode @ " + this.getBlockPos().getX() + ", " + this.getBlockPos().getY() + ", " + this.getBlockPos().getZ() + " triggered exception: " + e.getMessage() + ".");
										}else if(db == DebugOptions.COMPLETE) {
											AssemblyLineMachines.LOGGER.warn("Interactor set to Interact Mode @ " + this.getBlockPos().getX() + ", " + this.getBlockPos().getY() + ", " + this.getBlockPos().getZ() + " triggered stack trace: ");
											e.printStackTrace();
										}
										
										if(db != DebugOptions.NONE && hasSentInteractorMessage == false) {
											hasSentInteractorMessage = true;
											AssemblyLineMachines.LOGGER.info("Tip: Interactor Interact Mode logging can be enabled or disabled in the config, as well as the feature disabled completely.");
										}
										
									}
									
									
									for(int i = 0; i < fp.getInventory().getContainerSize(); i++) {
										ItemStack nStack = fp.getInventory().getItem(i);
										if(!nStack.isEmpty()) {
											if(nStack != stack) {
												Utils.spawnItem(nStack, offsetPos.above(), this.getLevel());
											}
											fp.getInventory().removeItemNoUpdate(i);
										}
									}
								}
								
							}else if(mode == 2) {
								if(bs.getBlock() != Blocks.AIR) {
									
									bbProg = bbProg + (bs.getDestroyProgress(fp, this.getLevel(), offsetPos) * 4f);
									if(bbProg >= 1f) {
										this.getLevel().destroyBlock(offsetPos, true, fp);
										this.getLevel().destroyBlockProgress(-1, offsetPos, -1);
										stack.hurtAndBreak(1, fp, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
										bbProg = 0f;
									}else {
										this.getLevel().destroyBlockProgress(-1, offsetPos, (int) Math.floor(bbProg * 10f));
									}
								}
							}else {
								
								List<LivingEntity> list = this.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(offsetPos.relative(getBlockState().getValue(HorizontalDirectionalBlock.FACING))).inflate(1));
								for(LivingEntity le : list) {
									boolean cont = true;
									if(le instanceof Player) {
										
										Player pe = (Player) le;
										if(pe.getUUID().equals(fp.getUUID())) {
											cont = false;
										}
									}
									
									if(cont){
										
										
										float amt = 0.5f;
										
										if(!stack.isEmpty()) {
											for(AttributeModifier am : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
												amt += am.getAmount();
											}
										}
										le.hurt(new EntityDamageSource("interactor", fp), amt);
										stack.hurtAndBreak(1, fp, (p_220038_0_) -> {p_220038_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);});
									}
									
								}
								
							}
						}
					}
				}
			}
			

		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);

			nTimer = compound.getInt("assemblylinemachines:ntimer");
			mode = compound.getInt("assemblylinemachines:mode");

			if (compound.contains("assemblylinemachines:uuid")) {
				origPlayerUUID = compound.getUUID("assemblylinemachines:uuid");
			}

			ordered = compound.getBoolean("assemblylinemachines:ordered");
		}

		@Override
		public CompoundTag save(CompoundTag compound) {
			compound.putInt("assemblylinemachines:ntimer", nTimer);
			compound.putInt("assemblylinemachines:mode", mode);

			if (origPlayerUUID != null) {
				compound.putUUID("assemblylinemachines:uuid", origPlayerUUID);
			}

			compound.putBoolean("assemblylinemachines:ordered", ordered);
			return super.save(compound);
		}

		@Override
		public boolean isAllowedInSlot(int slot, ItemStack stack) {
			return true;
		}
	}

	public static class ContainerInteractor extends ContainerALMBase<TEInteractor> {

		private static final Pair<Integer, Integer> PLAYER_INV_POS = new Pair<>(8, 84);
		private static final Pair<Integer, Integer> PLAYER_HOTBAR_POS = new Pair<>(8, 142);

		public ContainerInteractor(final int windowId, final Inventory playerInventory, final TEInteractor tileEntity) {
			super(Registry.getContainerType("interactor"), windowId, tileEntity, playerInventory, PLAYER_INV_POS, PLAYER_HOTBAR_POS, 0, 0);

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 3; ++col) {
					this.addSlot(new SlotWithRestrictions(tileEntity, (row * 3) + col, 62 + (18 * col), 12 + (18 * row), tileEntity));
				}
			}
		}

		public ContainerInteractor(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
			this(windowId, playerInventory, Utils.getBlockEntity(playerInventory, data, TEInteractor.class));
		}

	}

	@OnlyIn(Dist.CLIENT)
	public static class ScreenInteractor extends ScreenALMBase<ContainerInteractor> {

		HashMap<Fluid, TextureAtlasSprite> spriteMap = new HashMap<>();
		TEInteractor tsfm;
		private TrueFalseButton modeB;

		public ScreenInteractor(ContainerInteractor screenContainer, Inventory inv, Component titleIn) {
			super(screenContainer, inv, titleIn, new Pair<>(176, 166), new Pair<>(11, 6), new Pair<>(11, 73), "interactor", false);
			tsfm = screenContainer.tileEntity;
		}

		@Override
		protected void init() {
			super.init();

			int x = leftPos;
			int y = topPos;

			
			this.addRenderableWidget(new TrueFalseButton(x+48, y+20, 176, 33, 11, 11, new TrueFalseButtonSupplier("Top-Left to Bottom-Right", "Random Selection", () -> tsfm.ordered), (b) -> sendSelChange(tsfm.getBlockPos())));
			modeB = this.addRenderableWidget(new TrueFalseButton(x+48, y+33, 11, 11, null, (b) -> sendModeChange(tsfm.getBlockPos())));
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
			
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
				super.blit(modeB.x, modeB.y, 176, 0 + add, modeB.getWidth(), modeB.getHeight());
			}
		};
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			if (modeB.isHovered()) {
				
				int x = (this.width - this.imageWidth) / 2;
				int y = (this.height - this.imageHeight) / 2;
				
				switch(tsfm.mode) {
				case 3:
					this.renderComponentTooltip("Attack Mode", mouseX - x, mouseY - y);
					break;
				case 2:
					this.renderComponentTooltip("Break Mode", mouseX - x, mouseY - y);
					break;
				case 1:
					this.renderComponentTooltip("Interact Mode", mouseX - x, mouseY - y);
					break;
				default:
					this.renderComponentTooltip("Place Mode", mouseX - x, mouseY - y);
					break;
				}
			}
		}
	}

	private static void sendSelChange(BlockPos pos) {
		PacketData pd = new PacketData("interactor_gui");
		pd.writeBlockPos("pos", pos);
		pd.writeUtf("button", "dir");

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	private static void sendModeChange(BlockPos pos) {
		PacketData pd = new PacketData("interactor_gui");
		pd.writeBlockPos("pos", pos);
		pd.writeUtf("button", "mode");

		PacketHandler.INSTANCE.sendToServer(pd);
	}

	public static void updateDataFromPacket(PacketData pd, Level world) {

		if (pd.getCategory().equals("interactor_gui")) {
			BlockPos pos = pd.get("pos", BlockPos.class);
			BlockEntity tex = world.getBlockEntity(pos);
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
				} else if (b.equals("dir")) {
					te.ordered = !te.ordered;
				}

				te.sendUpdates();
			}
		}
	}
}