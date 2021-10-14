package me.haydenb.assemblylinemachines.item.items;

@Deprecated
public class ItemGuidebook /*extends Item*/ {
	//DISABLED AS PATCHOULI IS NOT YET UPDATED PLUGIN
	/*
	public ItemGuidebook() {
		super(new Item.Properties().group(Registry.creativeTab).maxStackSize(1));
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(Level world, Player player, Hand handIn) {
		
		if(!world.isClientSide) {
			PluginPatchouli.get().openBook((ServerPlayer) player, world);
		}
		
		return super.onItemRightClick(world, player, handIn);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		
		onItemRightClick(context.getLevel(), context.getPlayer(), context.getHand());
		
		return InteractionResult.CONSUME;
	}
	*/
}
