package me.haydenb.assemblylinemachines.block.machines.mob;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import me.haydenb.assemblylinemachines.packets.HashPacketImpl;
import me.haydenb.assemblylinemachines.packets.HashPacketImpl.PacketData;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.PacketDistributor;

public class BlockVacuumHopper extends HopperBlock {

	private static final VoxelShape SHAPE_UD = Stream.of(Block.makeCuboidShape(5, 0, 5, 11, 4, 11), Block.makeCuboidShape(4, 4, 4, 12, 8, 12),
			Block.makeCuboidShape(3, 8, 3, 13, 12, 13), Block.makeCuboidShape(2, 10, 7, 3, 13, 9), Block.makeCuboidShape(13, 10, 7, 14, 13, 9),
			Block.makeCuboidShape(3, 12, 7, 4, 13, 9), Block.makeCuboidShape(12, 12, 7, 13, 13, 9), Block.makeCuboidShape(7, 10, 13, 9, 13, 14),
			Block.makeCuboidShape(7, 12, 12, 9, 13, 13), Block.makeCuboidShape(7, 10, 2, 9, 13, 3), Block.makeCuboidShape(7, 12, 3, 9, 13, 4)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_E = Stream.of(Block.makeCuboidShape(12, 5, 5, 16, 11, 11), Block.makeCuboidShape(8, 4, 4, 12, 12, 12),
			Block.makeCuboidShape(4, 3, 3, 8, 13, 13), Block.makeCuboidShape(3, 2, 7, 6, 3, 9), Block.makeCuboidShape(3, 13, 7, 6, 14, 9), Block.makeCuboidShape(3, 3, 7, 4, 4, 9),
			Block.makeCuboidShape(3, 12, 7, 4, 13, 9), Block.makeCuboidShape(3, 7, 13, 6, 9, 14), Block.makeCuboidShape(3, 7, 12, 4, 9, 13),
			Block.makeCuboidShape(3, 7, 2, 6, 9, 3), Block.makeCuboidShape(3, 7, 3, 4, 9, 4)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	private static final VoxelShape SHAPE_S = General.rotateShape(Direction.EAST, Direction.SOUTH, SHAPE_E);
	private static final VoxelShape SHAPE_W = General.rotateShape(Direction.EAST, Direction.WEST, SHAPE_E);
	private static final VoxelShape SHAPE_N = General.rotateShape(Direction.EAST, Direction.NORTH, SHAPE_E);

	public BlockVacuumHopper() {
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return Registry.getTileEntity("vacuum_hopper").create();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction d = state.get(BlockStateProperties.FACING_EXCEPT_UP);
		if (d == Direction.WEST) {
			return SHAPE_W;
		} else if (d == Direction.SOUTH) {
			return SHAPE_S;
		} else if (d == Direction.EAST) {
			return SHAPE_E;
		} else if (d == Direction.NORTH) {
			return SHAPE_N;
		} else {
			return SHAPE_UD;
		}
	}

	public static class TEVacuumHopper extends HopperTileEntity {

		private int timer = 0;
		private int sTimer = 0;
		private AxisAlignedBB bb = null;

		@Override
		public void tick() {
			if (!world.isRemote) {
				if (timer++ == 10) {
					timer = 0;

					if (bb == null) {
						bb = new AxisAlignedBB(pos.offset(getBlockState().get(BlockStateProperties.FACING_EXCEPT_UP).getOpposite(), 2)).grow(1);
					}

					List<ItemEntity> el = world.getEntitiesWithinAABB(ItemEntity.class, bb);
					for (ItemEntity entity : el) {

						if (entity.getAge() > 30) {
							double x = entity.getPosX();
							double y = entity.getPosY();
							double z = entity.getPosZ();
							if (captureItem(this, entity)) {
								markDirty();
								spawnTeleparticles(x, y, z, world.getChunkAt(entity.func_233580_cy_()));
								break;
							}
						}

					}

				}
				if(sTimer++ == 4) {
					sTimer = 0;
					if (transferItemsOut()) {
						markDirty();
					}
				}
				
			}

		}

		
		//Below four methods copied from HopperTileEntity.class
		private boolean transferItemsOut() {
			if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(this))
				return true;
			IInventory iinventory = this.getInventoryForHopperTransfer();
			if (iinventory == null) {
				return false;
			} else {
				Direction direction = this.getBlockState().get(HopperBlock.FACING).getOpposite();
				if (this.isInventoryFull(iinventory, direction)) {
					return false;
				} else {
					for (int i = 0; i < this.getSizeInventory(); ++i) {
						if (!this.getStackInSlot(i).isEmpty()) {
							ItemStack itemstack = this.getStackInSlot(i).copy();
							ItemStack itemstack1 = putStackInInventoryAllSlots(this, iinventory, this.decrStackSize(i, 1), direction);
							if (itemstack1.isEmpty()) {
								iinventory.markDirty();
								return true;
							}

							this.setInventorySlotContents(i, itemstack);
						}
					}

					return false;
				}
			}
		}

		private IInventory getInventoryForHopperTransfer() {
			Direction direction = this.getBlockState().get(HopperBlock.FACING);
			return getInventoryAtPosition(this.getWorld(), this.pos.offset(direction));
		}

		private static IntStream func_213972_a(IInventory p_213972_0_, Direction p_213972_1_) {
			return p_213972_0_ instanceof ISidedInventory ? IntStream.of(((ISidedInventory) p_213972_0_).getSlotsForFace(p_213972_1_))
					: IntStream.range(0, p_213972_0_.getSizeInventory());
		}

		private boolean isInventoryFull(IInventory inventoryIn, Direction side) {
			return func_213972_a(inventoryIn, side).allMatch((p_213970_1_) -> {
				ItemStack itemstack = inventoryIn.getStackInSlot(p_213970_1_);
				return itemstack.getCount() >= itemstack.getMaxStackSize();
			});
		}

		@Override
		public TileEntityType<?> getType() {
			return Registry.getTileEntity("vacuum_hopper");
		}

		@Override
		public ITextComponent getName() {
			return new TranslationTextComponent(Registry.getBlock("vacuum_hopper").getTranslationKey());
		}

		private static void spawnTeleparticles(double x, double y, double z, Chunk ch) {
			PacketData pd = new PacketData("vacuum_hopper_particles");
			pd.writeDouble("x", x);
			pd.writeDouble("y", y);
			pd.writeDouble("z", z);
			HashPacketImpl.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), pd);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static final RedstoneParticleData ENDER_PARTICLE = new RedstoneParticleData(0.8f, 0.223f, 0.792f, 1f);

	@OnlyIn(Dist.CLIENT)
	public static void spawnTeleparticles(PacketData pd) {
		Minecraft mc = Minecraft.getInstance();
		for (int i = 0; i < 4; i++) {
			mc.player.getEntityWorld().addParticle(ENDER_PARTICLE, true, pd.get("x", Double.class), pd.get("y", Double.class) + 0.1, pd.get("z", Double.class), 0, 0, 0);
		}
	}

}
