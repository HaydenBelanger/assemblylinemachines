package me.haydenb.assemblylinemachines.item;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;


public enum ItemTiers {
	
	TITANIUM(6f, 7f, 20, 3, 2200, 153, 0.05f, new int[] {8, 11, 10, 7}, "titanium", 1.7f, ()->{return Ingredient.fromItems(Registry.getItem("titanium_ingot"));}),
	STEEL(8f, 15f, 2, 3, 1100, 98, 0.15f, new int[] {11, 15, 12, 9}, "steel", 2.4f, ()->{return Ingredient.fromItems(Registry.getItem("steel_ingot"));}),
	CRANK(10f, 22f, 35, 3, 50, 0, 0f, new int[] {0, 0, 0, 0}, "", 0f,  ()->{return Ingredient.fromTag(ItemTags.getCollection().get(new ResourceLocation("assemblylinemachines", "crafting/gears/precious")));}),
	MYSTIUM(14f, 24f, 60, 3, 125, 0, 0f, new int[] {0, 0, 0, 0}, "", 0f,  ()->{return Ingredient.fromItems(Registry.getItem("mystium_ingot"));});
	
	private static final int[] ARMOR_DURABILITY_OFFSET = new int[] {4, 8, 7, 5};
	private float attack;
	private float efficiency;
	private int enchantability;
	private int harvestLevel;
	private int durability;
	private int armorDurabilityBase;
	private int[] damageReduction;
	private String armorSetName;
	private float toughness;
	private LazyValue<Ingredient> ingredient;	
	ItemTiers(float attack, float efficiency, int enchantability, int harvestLevel, int durability, int armorDurabilityBase, float armorKnockbackResistance, int[] damageReduction, String armorSetName, float toughness, Supplier<Ingredient> ingredient) {
		
		
		this.attack = attack;
		this.efficiency = efficiency;
		this.enchantability = enchantability;
		this.harvestLevel = harvestLevel;
		this.durability = durability;
		this.armorDurabilityBase = armorDurabilityBase;
		this.damageReduction = damageReduction;
		this.armorSetName = armorSetName;
		this.toughness = toughness;
		this.ingredient = new LazyValue<>(ingredient);
	}
	
	public static enum ArmorTiers implements IArmorMaterial{
		TITANIUM(ItemTiers.TITANIUM), STEEL(ItemTiers.STEEL);
		
		private final ItemTiers baseTier;
		ArmorTiers(ItemTiers base){
			this.baseTier = base;
		}

		@Override
		public int getDamageReductionAmount(EquipmentSlotType est) {
			
			switch(est) {
			case HEAD:
				return baseTier.damageReduction[0];
			case CHEST:
				return baseTier.damageReduction[1];
			case LEGS:
				return baseTier.damageReduction[2];
			case FEET:
				return baseTier.damageReduction[3];
			default:
				return 0;
			}
		}

		@Override
		public int getDurability(EquipmentSlotType est) {
			
			switch(est) {
			case HEAD:
				return ARMOR_DURABILITY_OFFSET[0] * baseTier.armorDurabilityBase;
			case CHEST:
				return ARMOR_DURABILITY_OFFSET[1] * baseTier.armorDurabilityBase;
			case LEGS:
				return ARMOR_DURABILITY_OFFSET[2] * baseTier.armorDurabilityBase;
			case FEET:
				return ARMOR_DURABILITY_OFFSET[3] * baseTier.armorDurabilityBase;
			default:
				return 0;
			}
		}

		@Override
		public int getEnchantability() {
			return baseTier.enchantability;
		}

		@Override
		public String getName() {
			return AssemblyLineMachines.MODID + ":" + baseTier.armorSetName;
		}

		@Override
		public Ingredient getRepairMaterial() {
			return baseTier.ingredient.getValue();
		}

		@Override
		public SoundEvent getSoundEvent() {
			return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
		}

		@Override
		public float getToughness() {
			return baseTier.toughness;
		}
	}
	
	public static enum ToolTiers implements IItemTier{
		TITANIUM(ItemTiers.TITANIUM), STEEL(ItemTiers.STEEL), CRANK(ItemTiers.CRANK), MYSTIUM(ItemTiers.MYSTIUM);
		
		private final ItemTiers baseTier;
		ToolTiers(ItemTiers tier){
			this.baseTier = tier;
		}

		@Override
		public float getAttackDamage() {
			return baseTier.attack;
		}

		@Override
		public float getEfficiency() {
			return baseTier.efficiency;
		}

		@Override
		public int getEnchantability() {
			return baseTier.enchantability;
		}

		@Override
		public int getHarvestLevel() {
			return baseTier.harvestLevel;
		}

		@Override
		public int getMaxUses() {
			return baseTier.durability;
		}

		@Override
		public Ingredient getRepairMaterial() {
			return baseTier.ingredient.getValue();
		}
		
	}

}
