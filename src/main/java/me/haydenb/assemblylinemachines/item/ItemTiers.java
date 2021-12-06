package me.haydenb.assemblylinemachines.item;

import java.util.List;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;

public enum ItemTiers {
	
	TITANIUM(ConfigHolder.COMMON.titaniumToolAttack.get(), ConfigHolder.COMMON.titaniumToolHarvestSpeed.get(), ConfigHolder.COMMON.titaniumEnchantability.get(), ConfigHolder.COMMON.titaniumDurability.get(), 
			ConfigHolder.COMMON.titaniumArmorKnockbackResistance.get(), ConfigHolder.COMMON.titaniumArmorDamageReduction.get(), "titanium", ConfigHolder.COMMON.titaniumArmorToughness.get(), ()->{return Ingredient.of(Registry.getItem("titanium_ingot"));}),
	STEEL(ConfigHolder.COMMON.steelToolAttack.get(), ConfigHolder.COMMON.steelToolHarvestSpeed.get(), ConfigHolder.COMMON.steelEnchantability.get(), ConfigHolder.COMMON.steelDurability.get(), 
			ConfigHolder.COMMON.steelArmorKnockbackResistance.get(), ConfigHolder.COMMON.steelArmorDamageReduction.get(), "steel", ConfigHolder.COMMON.steelArmorToughness.get(), ()->{return Ingredient.of(Registry.getItem("steel_ingot"));}),
	CRANK(ConfigHolder.COMMON.crankToolAttack.get(), ConfigHolder.COMMON.crankToolDurability.get(), ConfigHolder.COMMON.crankToolEnchantability.get(), ConfigHolder.COMMON.crankToolDurability.get(), 
			()->{return Ingredient.of(ItemTags.getAllTags().getTag(new ResourceLocation("assemblylinemachines", "crafting/gears/precious")));}),
	MYSTIUM(ConfigHolder.COMMON.mystiumToolAttack.get(), ConfigHolder.COMMON.mystiumToolDurability.get(), ConfigHolder.COMMON.mystiumToolEnchantability.get(), ConfigHolder.COMMON.mystiumToolDurability.get(), ()->{return Ingredient.of(Registry.getItem("mystium_ingot"));}),
	NOVASTEEL(ConfigHolder.COMMON.novasteelToolAttack.get(), ConfigHolder.COMMON.novasteelToolDurability.get(), ConfigHolder.COMMON.novasteelToolEnchantability.get(), ConfigHolder.COMMON.novasteelToolDurability.get(), ()->{return Ingredient.of(Registry.getItem("novasteel_ingot"));}),
	CRG(750, 5, 0d, 3, "crg", 0d, () -> Ingredient.EMPTY);
	
	private float attack;
	private float efficiency;
	private int enchantability;
	private int durability;
	private int damageReduction;
	private String armorSetName;
	private float toughness;
	private float armorKnockbackResistance;
	private Supplier<Ingredient> ingredient;	
	
	ItemTiers(double attack, double efficiency, int enchantability, int durability, double armorKnockbackResistance, int armorDamageReduction, String armorSetName, double toughness, Supplier<Ingredient> ingredient) {
		
		
		this.armorKnockbackResistance = (float) armorKnockbackResistance;
		this.attack = (float) attack;
		this.efficiency = (float) efficiency;
		this.enchantability = enchantability;
		this.durability = durability;
		this.damageReduction = armorDamageReduction;
		this.armorSetName = armorSetName;
		this.toughness = (float) toughness;
		this.ingredient = ingredient;
	}
	
	ItemTiers(double attack, double efficiency, int enchantability, int durability, Supplier<Ingredient> ingredient){
		this.attack = (float) attack;
		this.efficiency = (float) efficiency;
		this.enchantability = enchantability;
		this.durability = durability;
		this.ingredient = ingredient;
	}
	
	ItemTiers(int durability, int enchantability, double armorKnockbackResistance, int armorDamageReduction, String armorSetName, double toughness, Supplier<Ingredient> ingredient){
	this.durability = durability;
	this.armorKnockbackResistance = (float) armorKnockbackResistance;
	this.damageReduction = armorDamageReduction;
	this.armorSetName = armorSetName;
	this.toughness = (float) toughness;
	this.ingredient = ingredient;
	this.enchantability = enchantability;
	}
	
	public static enum ArmorTiers implements ArmorMaterial{
		TITANIUM(ItemTiers.TITANIUM), STEEL(ItemTiers.STEEL), CRG(ItemTiers.CRG);
		
		private final ItemTiers baseTier;
		ArmorTiers(ItemTiers base){
			this.baseTier = base;
		}

		@Override
		public String getName() {
			return AssemblyLineMachines.MODID + ":" + baseTier.armorSetName;
		}

		@Override
		public float getToughness() {
			return baseTier.toughness;
		}

		@Override
		public int getDurabilityForSlot(EquipmentSlot pSlot) {
			switch(pSlot) {
			case HEAD:
				return Math.round(baseTier.durability / 3f);
			case CHEST:
				return Math.round(baseTier.durability / 2f);
			case LEGS:
				return Math.round(baseTier.durability / 2.5f);
			case FEET:
				return Math.round(baseTier.durability / 3.25f);
			default:
				return 0;
			}
		}

		@Override
		public int getDefenseForSlot(EquipmentSlot pSlot) {
			switch(pSlot) {
			case HEAD:
				return baseTier.damageReduction;
			case CHEST:
				return Math.round(baseTier.damageReduction * 1.5f);
			case LEGS:
				return Math.round(baseTier.damageReduction * 1.25f);
			case FEET:
				return Math.round(baseTier.damageReduction * 0.8f);
			default:
				return 0;
			}
		}

		@Override
		public int getEnchantmentValue() {
			return baseTier.enchantability;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ARMOR_EQUIP_DIAMOND;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return baseTier.ingredient.get();
		}

		@Override
		public float getKnockbackResistance() {
			return baseTier.armorKnockbackResistance;
		}
	}
	
	public static enum ToolTiers implements Tier{
		
		TITANIUM(ItemTiers.TITANIUM, List.of(Tiers.IRON), List.of(Tiers.DIAMOND)), STEEL(ItemTiers.STEEL, List.of(Tiers.IRON), List.of(Tiers.DIAMOND)),
		CRANK(ItemTiers.CRANK, List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE)),
		MYSTIUM(ItemTiers.MYSTIUM, List.of(Tiers.NETHERITE), List.of(), new ResourceLocation(AssemblyLineMachines.MODID, "needs_mystium_tool")),
		NOVASTEEL(ItemTiers.NOVASTEEL, List.of(ToolTiers.MYSTIUM), List.of(), new ResourceLocation(AssemblyLineMachines.MODID, "needs_novasteel_tool"));
		
		
		
		private final ItemTiers baseTier;
		private final Tag.Named<Block> tierTag;
		
		ToolTiers(ItemTiers tier, List<Object> after, List<Object> before){
			this(tier, after, before, null);
		}
		
		ToolTiers(ItemTiers tier, List<Object> after, List<Object> before, ResourceLocation tierTag){
			this.baseTier = tier;
			
			this.tierTag = tierTag != null ? BlockTags.createOptional(tierTag) : null;	
			TierSortingRegistry.registerTier(this, new ResourceLocation(AssemblyLineMachines.MODID, this.name().toLowerCase()), after, before);
			
		}

		@Override
		public int getUses() {
			return baseTier.durability;
		}

		@Override
		public float getSpeed() {
			return baseTier.efficiency;
		}

		@Override
		public float getAttackDamageBonus() {
			return baseTier.attack;
		}

		@Override
		public int getLevel() {
			return 0;
		}

		@Override
		public int getEnchantmentValue() {
			return baseTier.enchantability;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return baseTier.ingredient.get();
		}
		
		@Override
		public Tag<Block> getTag() {
			return tierTag;
		}
		
	}

}
