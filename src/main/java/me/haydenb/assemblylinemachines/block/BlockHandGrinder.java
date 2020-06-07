package me.haydenb.assemblylinemachines.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.Utils;
import me.haydenb.assemblylinemachines.util.Utils.Triplet;
import me.haydenb.assemblylinemachines.util.machines.ALMTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockHandGrinder extends Block {
	
	private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 0, 16, 6, 16), Block.makeCuboidShape(5, 6, 7, 11, 9, 9), IBooleanFunction.OR);
	private static final VoxelShape SHAPE_NO_BLADE = Block.makeCuboidShape(0, 0, 0, 16, 6, 16);
	private static final EnumProperty<Blades> BLADE_PROPERTY = EnumProperty.create("blade_type", Blades.class);
	public BlockHandGrinder() {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(4f, 15f).harvestLevel(0).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE));
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(BLADE_PROPERTY, Blades.none));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(BLADE_PROPERTY, Blades.none);
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		
		builder.add(HorizontalBlock.HORIZONTAL_FACING).add(BLADE_PROPERTY);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		if(state.getBlock() == this) {
			return true;
		}
		return false;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		
		if(state.get(BLADE_PROPERTY) == Blades.none) {
			return Utils.rotateShape(Direction.NORTH, state.get(HorizontalBlock.HORIZONTAL_FACING), SHAPE_NO_BLADE);
		}else {
			return Utils.rotateShape(Direction.NORTH, state.get(HorizontalBlock.HORIZONTAL_FACING), SHAPE);
		}
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registry.getTileEntity("hand_grinder").create();
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getTileEntity(pos) instanceof TEHandGrinder) {
				TEHandGrinder tehg = (TEHandGrinder) worldIn.getTileEntity(pos);
				if(tehg.blade != null) {
					Utils.spawnItem(tehg.blade, pos, worldIn);
				}
				worldIn.removeTileEntity(pos);
			}
		}
	}
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if(!world.isRemote) {
			if(player.getActiveHand() == Hand.MAIN_HAND) {
				
				if(world.getTileEntity(pos) instanceof TEHandGrinder) {
					TEHandGrinder entity = (TEHandGrinder) world.getTileEntity(pos);
					
					if(player.isSneaking()) {
						
						if(entity.blade == null || entity.blade == ItemStack.EMPTY) {
							player.sendStatusMessage(new StringTextComponent("There is no blade installed."), true);
						}else {
							if(entity.blade.getItem() instanceof ItemGrindingBlade) {
								ItemGrindingBlade igb = (ItemGrindingBlade) entity.blade.getItem();
								if(entity.blade.getMaxDamage() - entity.blade.getDamage() == 1) {
									player.sendStatusMessage(new StringTextComponent("You have a " + igb.blade.friendlyname + " Blade with 1 use left."), true);
								}else {
									player.sendStatusMessage(new StringTextComponent("You have a " + igb.blade.friendlyname + " Blade with " + (entity.blade.getMaxDamage() - entity.blade.getDamage()) + " uses left."), true);
								}
							}
							
							
						}
						
						
					}else {
						ItemStack held = player.getHeldItemMainhand();
						if(entity.blade == null || entity.blade == ItemStack.EMPTY) {
							if(held.getItem() instanceof ItemGrindingBlade) {
								ItemGrindingBlade igb = (ItemGrindingBlade) held.getItem();
								ItemStack is = new ItemStack(igb);
								is.setDamage(held.getDamage());
								held.shrink(1);
								world.setBlockState(pos, state.with(BLADE_PROPERTY, igb.blade));
								entity.blade = is;
								entity.sendUpdates();
								player.sendStatusMessage(new StringTextComponent("Installed " + igb.blade.friendlyname + " Blade."), true);
							}else {
								player.sendStatusMessage(new StringTextComponent("There is no blade installed."), true);
							}
							
						}else {
							boolean sendupdates = true;
							boolean bladeBroke = false;
							Triplet<ItemStack, Integer, ItemStack> prog = entity.progress.get(player.getUniqueID().toString());
							if(prog != null && prog.x == held) {
								prog.y--;
								bladeBroke = ItemGrindingBlade.damageBlade(entity.blade);
								if(prog.y == 0) {
									held.shrink(1);
									ItemHandlerHelper.giveItemToPlayer(player, prog.z);
									entity.progress.remove(player.getUniqueID().toString());
									
								}
							}else {
								GrinderCrafting recipe = world.getRecipeManager().getRecipe(GrinderCrafting.GRINDER_RECIPE, player.inventory, world).orElse(null);
								if(recipe != null) {
									if(entity.blade.getItem() instanceof ItemGrindingBlade) {
										ItemGrindingBlade igb = (ItemGrindingBlade) entity.blade.getItem();
										if(recipe.getBlade().tier <= igb.blade.tier) {
											
											bladeBroke = ItemGrindingBlade.damageBlade(entity.blade);
											entity.progress.put(player.getUniqueID().toString(), new Triplet<ItemStack, Integer, ItemStack>(held, recipe.getGrinds() - 1, recipe.getRecipeOutput().copy()));
										}else {
											player.sendStatusMessage(new StringTextComponent("You need a better blade to use this recipe."), true);
											sendupdates = false;
										}
									}
									
								}else {
									sendupdates = false;
								}
							}
							
							if(entity.blade == null || entity.blade == ItemStack.EMPTY || bladeBroke) {
								world.setBlockState(pos, state.with(BLADE_PROPERTY, Blades.none));
								player.sendStatusMessage(new StringTextComponent("The blade broke!"), true);
								entity.blade = ItemStack.EMPTY;
							}
							
							if(sendupdates) entity.sendUpdates();
							
							
						}
					}
				}
			}
			
			
			
		}
		
		return ActionResultType.CONSUME;
		
	}
	
	public static class TEHandGrinder extends ALMTileEntity {

		private ItemStack blade = null;
		HashMap<String, Triplet<ItemStack, Integer, ItemStack>> progress = new HashMap<>();
		
		public TEHandGrinder(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}
		
		public TEHandGrinder() {
			this(Registry.getTileEntity("hand_grinder"));
		}
		
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			
			if(compound.contains("assemblylinemachines:blade")) {
				blade = ItemStack.read(compound.getCompound("assemblylinemachines:blade"));
			}
		}
		
		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			
			if(blade != null) {
				CompoundNBT sub = new CompoundNBT();
				blade.write(sub);
				compound.put("assemblylinemachines:blade", sub);
			}
			
			return compound;
		}
		
		
		
	}
	
	public static enum Blades implements IStringSerializable{
		titanium(0, 110, "Titanium", "titanium_blade"), puregold(1, 170, "Pure Gold", "pure_gold_blade"), none(-999, -999, null, null);

		public int tier;
		public int uses;
		public String friendlyname;
		public String iconItemName;
		
		Blades(int tier, int uses, String friendlyname, String iconItemName){
			this.tier = tier;
			this.uses = uses;
			this.friendlyname = friendlyname;
			this.iconItemName = iconItemName;
		}
		
		@Override
		public String getName() {
			return this.toString();
		}
		
		public static Ingredient getAllBladesAtMinTier(int minTier) {
			List<Item> items = new ArrayList<>();
			for(Blades b : values()) {
				if(b.tier >= minTier) {
					items.add(Registry.getItem(b.iconItemName));
				}
			}
			
			return Ingredient.fromItems(items.toArray(new Item[items.size()]));
		}
	}
	
}
