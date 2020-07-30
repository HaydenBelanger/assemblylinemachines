package me.haydenb.assemblylinemachines.item.items;

import java.util.HashMap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemMobCrystal extends Item{

	
	public static final HashMap<EntityType<?>, Integer> MOB_COLORS = new HashMap<>();
	
	static {
		MOB_COLORS.put(EntityType.BAT, 0x402400);
		MOB_COLORS.put(EntityType.CAT, 0x7d4600);
		MOB_COLORS.put(EntityType.CHICKEN, 0xffe0e0);
		MOB_COLORS.put(EntityType.COD, 0x806c44);
		MOB_COLORS.put(EntityType.COW, 0x454036);
		MOB_COLORS.put(EntityType.DONKEY, 0x6e6960);
		MOB_COLORS.put(EntityType.FOX, 0xbd8624);
		MOB_COLORS.put(EntityType.HORSE, 0x69480e);
		MOB_COLORS.put(EntityType.MOOSHROOM, 0x941f1f);
		MOB_COLORS.put(EntityType.MULE, 0xffffff);
		MOB_COLORS.put(EntityType.OCELOT, 0xfca103);
		MOB_COLORS.put(EntityType.PARROT, 0xff0000);
		MOB_COLORS.put(EntityType.PIG, 0xff668a);
		MOB_COLORS.put(EntityType.PUFFERFISH, 0xd4bb00);
		MOB_COLORS.put(EntityType.RABBIT, 0x827865);
		MOB_COLORS.put(EntityType.SALMON, 0x8f6e6e);
		MOB_COLORS.put(EntityType.SHEEP, 0xebebeb);
		MOB_COLORS.put(EntityType.SKELETON_HORSE, 0xbfbfbf);
		MOB_COLORS.put(EntityType.SNOW_GOLEM, 0xffb114);
		MOB_COLORS.put(EntityType.SQUID, 0x555361);
		//STRIDER
		MOB_COLORS.put(EntityType.field_233589_aE_, 0x5c2525);
		MOB_COLORS.put(EntityType.TROPICAL_FISH, 0x00c6c9);
		MOB_COLORS.put(EntityType.TURTLE, 0x0b6b1a);
		MOB_COLORS.put(EntityType.VILLAGER, 0xa37c5f);
		MOB_COLORS.put(EntityType.WANDERING_TRADER, 0x63687d);
		MOB_COLORS.put(EntityType.BEE, 0xfbff00);
		MOB_COLORS.put(EntityType.DOLPHIN, 0xc7d6d9);
		MOB_COLORS.put(EntityType.IRON_GOLEM, 0xb8aeae);
		MOB_COLORS.put(EntityType.LLAMA, 0xd1d0be);
		MOB_COLORS.put(EntityType.PANDA, 0x595959);
		MOB_COLORS.put(EntityType.POLAR_BEAR, 0xb1bdbc);
		MOB_COLORS.put(EntityType.WOLF, 0xb5b5b5);
		MOB_COLORS.put(EntityType.CAVE_SPIDER, 0x8583a8);
		MOB_COLORS.put(EntityType.ENDERMAN, 0x000000);
		//PIGLIN
		MOB_COLORS.put(EntityType.field_233591_ai_, 0x755769);
		MOB_COLORS.put(EntityType.SPIDER, 0x524948);
		//ZOMBIFIED PIGLIN
		MOB_COLORS.put(EntityType.field_233592_ba_, 0x5d695f);
		MOB_COLORS.put(EntityType.BLAZE, 0xd9a352);
		MOB_COLORS.put(EntityType.CREEPER, 0x37ff00);
		MOB_COLORS.put(EntityType.DROWNED, 0x768a94);
		MOB_COLORS.put(EntityType.ELDER_GUARDIAN, 0xa69e94);
		MOB_COLORS.put(EntityType.ENDERMITE, 0x642685);
		MOB_COLORS.put(EntityType.EVOKER, 0x666666);
		MOB_COLORS.put(EntityType.GHAST, 0xd9d9d9);
		MOB_COLORS.put(EntityType.GUARDIAN, 0x8ca398);
		//HOGLIN
		MOB_COLORS.put(EntityType.field_233588_G_, 0x9e8373);
		MOB_COLORS.put(EntityType.HUSK, 0x9e928b);
		MOB_COLORS.put(EntityType.MAGMA_CUBE, 0x574545);
		MOB_COLORS.put(EntityType.PHANTOM, 0x34526e);
		MOB_COLORS.put(EntityType.PILLAGER, 0x666666);
		MOB_COLORS.put(EntityType.RAVAGER, 0x3e4042);
		MOB_COLORS.put(EntityType.SHULKER, 0x9900ff);
		MOB_COLORS.put(EntityType.SILVERFISH, 0x8a8a8a);
		MOB_COLORS.put(EntityType.SKELETON, 0xffffff);
		MOB_COLORS.put(EntityType.SLIME, 0x289917);
		MOB_COLORS.put(EntityType.STRAY, 0x9db1cc);
		MOB_COLORS.put(EntityType.VEX, 0xb0cdf5);
		MOB_COLORS.put(EntityType.VINDICATOR, 0x666666);
		MOB_COLORS.put(EntityType.WITCH, 0x4f3861);
		MOB_COLORS.put(EntityType.WITHER_SKELETON, 0x3b3b3b);
		//ZOGLIN
		MOB_COLORS.put(EntityType.field_233590_aW_, 0x444a40);
		MOB_COLORS.put(EntityType.ZOMBIE, 0x5b7848);
		MOB_COLORS.put(EntityType.ZOMBIE_VILLAGER, 0x5b7848);
		MOB_COLORS.put(EntityType.ENDER_DRAGON, 0x9500b3);
		MOB_COLORS.put(EntityType.WITHER, 0x282529);
		
	}
	
	private static final String TRANSLATION_TAG = Util.makeTranslationKey("item", new ResourceLocation(AssemblyLineMachines.MODID, "mob_crystal_tuned"));
	public ItemMobCrystal() {
		super(new Item.Properties().group(Registry.creativeTab));
		
		
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		
		if(stack.hasTag()) {
			
			EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(stack.getTag().getString("assemblylinemachines:mob")));
			if(entity != null) {
				return new TranslationTextComponent(TRANSLATION_TAG, entity.getName().func_230532_e_().func_240699_a_(TextFormatting.AQUA));
			}
			
		}
		return super.getDisplayName(stack);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		if(stack.hasTag()) {
			return true;
		}
		return super.hasEffect(stack);
	}
}
