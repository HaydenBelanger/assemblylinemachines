package me.haydenb.assemblylinemachines.block.machines.primitive;

import java.util.ArrayList;
import java.util.List;

import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.item.categories.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
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
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(BLADE_PROPERTY, Blades.NONE));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(BLADE_PROPERTY, Blades.NONE);
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
		
		if(state.get(BLADE_PROPERTY) == Blades.NONE) {
			return General.rotateShape(Direction.NORTH, state.get(HorizontalBlock.HORIZONTAL_FACING), SHAPE_NO_BLADE);
		}else {
			return General.rotateShape(Direction.NORTH, state.get(HorizontalBlock.HORIZONTAL_FACING), SHAPE);
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
					General.spawnItem(tehg.blade, pos, worldIn);
				}
				worldIn.removeTileEntity(pos);
			}
		}
	}
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if(!world.isRemote) {
			if(handIn.equals(Hand.MAIN_HAND)) {
				
				if(world.getTileEntity(pos) instanceof TEHandGrinder) {
					TEHandGrinder entity = (TEHandGrinder) world.getTileEntity(pos);
					
					if(player.isSneaking() && player.getHeldItemMainhand().isEmpty()) {
						
						
						if(entity.blade == null || entity.blade == ItemStack.EMPTY) {
							player.sendStatusMessage(new StringTextComponent("There is no blade installed."), true);
						}else {
							if(entity.blade.getItem() instanceof ItemGrindingBlade) {
								player.sendStatusMessage(new StringTextComponent("Uninstalled blade."), true);
								ItemHandlerHelper.giveItemToPlayer(player, entity.blade);
								entity.blade = ItemStack.EMPTY;
								world.setBlockState(pos, state.with(BLADE_PROPERTY, Blades.NONE));
								entity.sendUpdates();
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
							if(entity.input != null && entity.input.isItemEqual(held)) {
								entity.value--;
								bladeBroke = ItemGrindingBlade.damageBlade(entity.blade);
								if(entity.value == 0) {
									held.shrink(1);
									ItemHandlerHelper.giveItemToPlayer(player, entity.output);
									entity.input = null;
									entity.value = null;
									entity.output = null;
									
								}
							}else {
								GrinderCrafting recipe = world.getRecipeManager().getRecipe(GrinderCrafting.GRINDER_RECIPE, player.inventory, world).orElse(null);
								if(recipe != null) {
									if(entity.blade.getItem() instanceof ItemGrindingBlade) {
										ItemGrindingBlade igb = (ItemGrindingBlade) entity.blade.getItem();
										if(recipe.getBlade().tier <= igb.blade.tier) {
											
											bladeBroke = ItemGrindingBlade.damageBlade(entity.blade);
											entity.input = held.copy();
											entity.value = recipe.getGrinds() - 1;
											entity.output = recipe.getRecipeOutput().copy();
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
								world.setBlockState(pos, state.with(BLADE_PROPERTY, Blades.NONE));
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
	
	public static class TEHandGrinder extends BasicTileEntity {

		private ItemStack blade = null;
		private ItemStack input;
		private Integer value;
		private ItemStack output;
		
		public TEHandGrinder(final TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}
		
		public TEHandGrinder() {
			this(Registry.getTileEntity("hand_grinder"));
		}
		
		@Override
		public void read(BlockState p_230337_1_, CompoundNBT compound) {
			super.read(p_230337_1_, compound);
			
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
		TITANIUM(0, 360, "Titanium", "titanium_blade"), PUREGOLD(1, 590, "Pure Gold", "pure_gold_blade"), STEEL(2, 1000, "Steel", "steel_blade"), NONE(-999, -999, null, null);

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
		
		public static Ingredient getAllBladesAtMinTier(int minTier) {
			List<Item> items = new ArrayList<>();
			for(Blades b : values()) {
				if(b.tier >= minTier) {
					items.add(Registry.getItem(b.iconItemName));
				}
			}
			
			return Ingredient.fromItems(items.toArray(new Item[items.size()]));
		}

		@Override
		public String getString() {
			return toString().toLowerCase();
		}
	}
	
}
