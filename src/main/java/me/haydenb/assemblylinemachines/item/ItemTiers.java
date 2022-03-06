package me.haydenb.assemblylinemachines.item;

import java.util.List;
import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.item.ItemPowerTool.PowerToolType;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ALMCommonConfig;
import me.haydenb.assemblylinemachines.registry.ConfigHandler.ConfigHolder;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;

public enum ItemTiers {
	
	TITANIUM(cfg().titaniumToolAttack.get(), cfg().titaniumToolHarvestSpeed.get(), cfg().titaniumEnchantability.get(), cfg().titaniumDurability.get(), 
			cfg().titaniumArmorKnockbackResistance.get(), cfg().titaniumArmorDamageReduction.get(), "titanium", cfg().titaniumArmorToughness.get(), List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE), false, ()->{return Ingredient.of(Registry.getItem("titanium_ingot"));}),
	
	STEEL(cfg().steelToolAttack.get(), cfg().steelToolHarvestSpeed.get(), cfg().steelEnchantability.get(), cfg().steelDurability.get(), 
			cfg().steelArmorKnockbackResistance.get(), cfg().steelArmorDamageReduction.get(), "steel", cfg().steelArmorToughness.get(), List.of(Tiers.IRON), List.of(Tiers.DIAMOND), false, ()->{return Ingredient.of(Registry.getItem("steel_ingot"));}),
	
	CRANK(cfg().crankToolAttack.get(), cfg().crankToolDurability.get(), cfg().crankToolEnchantability.get(), cfg().crankToolDurability.get(), 0d, 0, null, 0d, List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE), false,
			()->{return Ingredient.of(ItemTags.getAllTags().getTag(new ResourceLocation("assemblylinemachines", "crafting/gears/precious")));}),
	
	MYSTIUM(cfg().mystiumToolAttack.get(), cfg().mystiumToolHarvestSpeed.get(), cfg().mystiumEnchantability.get(), cfg().mystiumDurability.get(), cfg().mystiumArmorKnockbackResistance.get(), cfg().mystiumArmorDamageReduction.get(), "mystium", cfg().mystiumArmorToughness.get(),
			List.of(Tiers.NETHERITE), List.of(), true, ()->{return Ingredient.of(Registry.getItem("mystium_ingot"));}),
	
	NOVASTEEL(cfg().novasteelToolAttack.get(), cfg().novasteelToolHarvestSpeed.get(), cfg().novasteelToolEnchantability.get(), cfg().novasteelToolDurability.get(), 0d, 0, null, 0d, List.of(ItemTiers.MYSTIUM.getItemTier()), List.of(), true, ()->{return Ingredient.of(Registry.getItem("novasteel_ingot"));}),
	
	CRG(0d, 0d, 3, 750, 0d, 5, "crg", 0d, null, null, false, () -> Ingredient.EMPTY);
	
	private final Tier itemTier;
	private final ArmorMaterial armorTier;
	private PowerToolType powerToolType = null;
	
	ItemTiers(double attack, double efficiency, int enchantability, int durability, double armorKnockbackResistance, int armorDamageReduction, String armorSetName, double toughness, List<Object> tiersAfter, List<Object> tiersBefore, boolean hasTag, Supplier<Ingredient> ingredient) {

		class ItemTier implements Tier{

			private final Named<Block> blockTag;
			
			public ItemTier() {
				this.blockTag = hasTag ? BlockTags.createOptional(new ResourceLocation(AssemblyLineMachines.MODID, "needs_" + ItemTiers.this.toString().toLowerCase() + "_tool")) : null;
				if(tiersAfter != null && tiersBefore != null) TierSortingRegistry.registerTier(this, new ResourceLocation(AssemblyLineMachines.MODID, ItemTiers.this.toString().toLowerCase()), tiersAfter, tiersBefore);
			}
			
			@Override
			public int getUses() {
				return durability;
			}

			@Override
			public float getSpeed() {
				return (float) efficiency;
			}

			@Override
			public float getAttackDamageBonus() {
				return (float) attack;
			}

			@Override
			public int getLevel() {
				return 0;
			}

			@Override
			public int getEnchantmentValue() {
				return enchantability;
			}

			@Override
			public Ingredient getRepairIngredient() {
				return ingredient.get();
			}
			
			@Override
			public Tag<Block> getTag() {
				return blockTag;
			}
		}
		
		class ArmorTier implements ArmorMaterial{

			@Override
			public int getDurabilityForSlot(EquipmentSlot pSlot) {
				return Math.round(durability / switch(pSlot) {
				case HEAD -> 3f;
				case CHEST -> 2f;
				case LEGS -> 2.5f;
				case FEET -> 3.25f;
				default -> throw new UnsupportedOperationException("Unsupported case value: " + pSlot.toString());
				});
			}

			@Override
			public int getDefenseForSlot(EquipmentSlot pSlot) {
				return Math.round(armorDamageReduction * switch(pSlot) {
				case HEAD -> 1f;
				case CHEST -> 1.5f;
				case LEGS -> 1.25f;
				case FEET -> 0.8f;
				default -> throw new UnsupportedOperationException("Unsupported case value: " + pSlot.toString());
				});
			}

			@Override
			public int getEnchantmentValue() {
				return enchantability;
			}

			@Override
			public SoundEvent getEquipSound() {
				return SoundEvents.ARMOR_EQUIP_DIAMOND;
			}

			@Override
			public Ingredient getRepairIngredient() {
				return ingredient.get();
			}

			@Override
			public String getName() {
				return AssemblyLineMachines.MODID + ":" + armorSetName;
			}

			@Override
			public float getToughness() {
				return (float) toughness;
			}

			@Override
			public float getKnockbackResistance() {
				return (float) armorKnockbackResistance;
			}
			
		}
		
		this.armorTier = new ArmorTier();
		this.itemTier = new ItemTier();
		try {
			this.powerToolType = PowerToolType.valueOf(this.toString());
		}catch(IllegalArgumentException e) {}
	}
	
	private static ALMCommonConfig cfg() {
		return ConfigHolder.getCommonConfig();
	}
	
	public Tier getItemTier() {
		return itemTier;
	}
	
	public ArmorMaterial getArmorTier() {
		return armorTier;
	}
	
	public PowerToolType getPowerToolType() {
		return powerToolType;
	}
	
	public static ItemTiers getTier(Tier tier) {
		for(ItemTiers vals : ItemTiers.values()) {
			if(vals.getItemTier().equals(tier)) {
				return vals;
			}
		}
		return null;
	}
	
	public static ItemTiers getTier(ArmorMaterial tier) {
		for(ItemTiers vals : ItemTiers.values()) {
			if(vals.getArmorTier().equals(tier)) {
				return vals;
			}
		}
		return null;
	}
}