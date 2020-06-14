package me.haydenb.assemblylinemachines.item;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;


public enum ItemTiers implements IItemTier, IArmorMaterial {
	
	
	
	TITANIUM(6f, 7f, 20, 3, 2200, 153, new int[] {8, 11, 10, 7}, "titanium", 1.7f, ()->{return Ingredient.fromItems(Registry.getItem("titanium_ingot"));}),
	STEEL(8f, 15f, 0, 3, 1100, 98, new int[] {11, 15, 12, 9}, "steel", 2.4f, ()->{return Ingredient.fromItems(Registry.getItem("steel_ingot"));}),
	CRANK(10f, 22f, 35, 3, 50, 0, new int[] {0, 0, 0, 0}, "", 0f,  ()->{return Ingredient.fromTag(ItemTags.getCollection().get(new ResourceLocation("assemblylinemachines", "crafting/gears/precious")));});
	
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
	ItemTiers(float attack, float efficiency, int enchantability, int harvestLevel, int durability, int armorDurabilityBase, int[] damageReduction, String armorSetName, float toughness, Supplier<Ingredient> ingredient) {
		
		
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
	
	

	@Override
	public float getAttackDamage() {
		return attack;
	}

	@Override
	public float getEfficiency() {
		return efficiency;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public int getHarvestLevel() {
		return harvestLevel;
	}

	@Override
	public int getMaxUses() {
		return durability;
	}

	@Override
	public Ingredient getRepairMaterial() {
		return ingredient.getValue();
	}

	@Override
	public int getDamageReductionAmount(EquipmentSlotType est) {
		return damageReduction[est.getIndex()];
	}



	@Override
	public int getDurability(EquipmentSlotType est) {
		return ARMOR_DURABILITY_OFFSET[est.getIndex()] * armorDurabilityBase;
	}



	@Override
	public String getName() {
		return AssemblyLineMachines.MODID + ":" + armorSetName;
	}



	@Override
	public SoundEvent getSoundEvent() {
		return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
	}



	@Override
	public float getToughness() {
		return toughness;
	}

}
