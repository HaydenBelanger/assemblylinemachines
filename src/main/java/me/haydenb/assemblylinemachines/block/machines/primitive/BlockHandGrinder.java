package me.haydenb.assemblylinemachines.block.machines.primitive;

import java.util.ArrayList;
import java.util.List;

import me.haydenb.assemblylinemachines.block.helpers.BasicTileEntity;
import me.haydenb.assemblylinemachines.crafting.GrinderCrafting;
import me.haydenb.assemblylinemachines.item.categories.ItemGrindingBlade;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockHandGrinder extends Block implements EntityBlock {
	
	private static final VoxelShape SHAPE = Shapes.join(Block.box(0, 0, 0, 16, 6, 16), Block.box(5, 6, 7, 11, 9, 9), BooleanOp.OR);
	private static final VoxelShape SHAPE_NO_BLADE = Block.box(0, 0, 0, 16, 6, 16);
	private static final EnumProperty<Blades> BLADE_PROPERTY = EnumProperty.create("blade_type", Blades.class);
	public BlockHandGrinder() {
		super(Block.Properties.of(Material.STONE).strength(4f, 15f).sound(SoundType.STONE));
		this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(BLADE_PROPERTY, Blades.NONE));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite()).setValue(BLADE_PROPERTY, Blades.NONE);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		
		builder.add(HorizontalDirectionalBlock.FACING).add(BLADE_PROPERTY);
	}
	
	@Override
	public String getDescriptionId() {
		if(ConfigHolder.COMMON.coolDudeMode.get()) {
			return super.getDescriptionId() + ".cool";
		}
		return super.getDescriptionId();
	}
	
	@Override
	public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
		if(ConfigHolder.COMMON.coolDudeMode.get()) {
			tooltip.add(new TextComponent("§8XL+ Definitive Edition"));
			tooltip.add(new TextComponent("§8With Sonic & Knuckles"));
			tooltip.add(new TextComponent("§8Season Pass"));
		}
		super.appendHoverText(stack, level, tooltip, flag);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		
		if(state.getValue(BLADE_PROPERTY) == Blades.NONE) {
			return General.rotateShape(Direction.NORTH, state.getValue(HorizontalDirectionalBlock.FACING), SHAPE_NO_BLADE);
		}else {
			return General.rotateShape(Direction.NORTH, state.getValue(HorizontalDirectionalBlock.FACING), SHAPE);
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return Registry.getBlockEntity("hand_grinder").create(pPos, pState);
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(worldIn.getBlockEntity(pos) instanceof TEHandGrinder) {
				TEHandGrinder tehg = (TEHandGrinder) worldIn.getBlockEntity(pos);
				if(tehg.blade != null) {
					General.spawnItem(tehg.blade, pos, worldIn);
				}
				worldIn.removeBlockEntity(pos);
			}
		}
	}
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if(!world.isClientSide) {
			if(handIn.equals(InteractionHand.MAIN_HAND)) {
				
				if(world.getBlockEntity(pos) instanceof TEHandGrinder) {
					TEHandGrinder entity = (TEHandGrinder) world.getBlockEntity(pos);
					
					if(player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
						
						
						if(entity.blade == null || entity.blade == ItemStack.EMPTY) {
							player.displayClientMessage(new TextComponent("There is no blade installed."), true);
						}else {
							if(entity.blade.getItem() instanceof ItemGrindingBlade) {
								player.displayClientMessage(new TextComponent("Uninstalled blade."), true);
								ItemHandlerHelper.giveItemToPlayer(player, entity.blade);
								entity.blade = ItemStack.EMPTY;
								world.setBlockAndUpdate(pos, state.setValue(BLADE_PROPERTY, Blades.NONE));
								entity.sendUpdates();
							}
							
							
						}
						
						
					}else {
						ItemStack held = player.getMainHandItem();
						if(entity.blade == null || entity.blade == ItemStack.EMPTY) {
							if(held.getItem() instanceof ItemGrindingBlade) {
								ItemGrindingBlade igb = (ItemGrindingBlade) held.getItem();
								ItemStack is = new ItemStack(igb);
								is.setDamageValue(held.getDamageValue());
								held.shrink(1);
								world.setBlockAndUpdate(pos, state.setValue(BLADE_PROPERTY, igb.blade));
								entity.blade = is;
								entity.sendUpdates();
								player.displayClientMessage(new TextComponent("Installed " + igb.blade.friendlyname + " Blade."), true);
							}else {
								player.displayClientMessage(new TextComponent("There is no blade installed."), true);
							}
							
						}else {
							boolean sendupdates = true;
							boolean bladeBroke = false;
							if(entity.input != null && entity.input.sameItem(held)) {
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
								GrinderCrafting recipe = world.getRecipeManager().getRecipeFor(GrinderCrafting.GRINDER_RECIPE, player.getInventory(), world).orElse(null);
								if(recipe != null) {
									if(entity.blade.getItem() instanceof ItemGrindingBlade) {
										ItemGrindingBlade igb = (ItemGrindingBlade) entity.blade.getItem();
										if(recipe.getBlade().tier <= igb.blade.tier) {
											
											bladeBroke = ItemGrindingBlade.damageBlade(entity.blade);
											entity.input = held.copy();
											entity.value = recipe.getGrinds() - 1;
											entity.output = recipe.getResultItem().copy();
										}else {
											player.displayClientMessage(new TextComponent("You need a better blade to use this recipe."), true);
											sendupdates = false;
										}
									}
									
								}else {
									sendupdates = false;
								}
							}
							
							if(entity.blade == null || entity.blade == ItemStack.EMPTY || bladeBroke) {
								world.setBlockAndUpdate(pos, state.setValue(BLADE_PROPERTY, Blades.NONE));
								player.displayClientMessage(new TextComponent("The blade broke!"), true);
								entity.blade = ItemStack.EMPTY;
							}
							
							if(sendupdates) entity.sendUpdates();
							
							
						}
					}
				}
			}
			
			
			
		}
		
		return InteractionResult.CONSUME;
		
	}
	
	public static class TEHandGrinder extends BasicTileEntity {

		private ItemStack blade = null;
		private ItemStack input;
		private Integer value;
		private ItemStack output;
		
		public TEHandGrinder(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
		}
		
		public TEHandGrinder(BlockPos pos, BlockState state) {
			this(Registry.getBlockEntity("hand_grinder"), pos, state);
		}
		
		@Override
		public void load(CompoundTag compound) {
			super.load(compound);
			
			if(compound.contains("assemblylinemachines:blade")) {
				blade = ItemStack.of(compound.getCompound("assemblylinemachines:blade"));
			}
		}
		
		@Override
		public CompoundTag save(CompoundTag compound) {
			super.save(compound);
			
			if(blade != null) {
				CompoundTag sub = new CompoundTag();
				blade.save(sub);
				compound.put("assemblylinemachines:blade", sub);
			}
			
			return compound;
		}
		
		
		
	}
	
	public static enum Blades implements StringRepresentable{
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
			
			return Ingredient.of(items.toArray(new Item[items.size()]));
		}

		@Override
		public String getSerializedName() {
			return toString().toLowerCase();
		}
	}
	
}
