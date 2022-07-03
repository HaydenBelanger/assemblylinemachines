package me.haydenb.assemblylinemachines.item;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.item.ItemTiers.ToolDefaults.Stats;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge.PowerToolType;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public enum ItemTiers {

	TITANIUM(ToolDefaults.TITANIUM, "titanium", List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE), false, ()->{return Ingredient.of(Registry.getItem("titanium_ingot"));}),
	STEEL(ToolDefaults.STEEL, "steel", List.of(Tiers.IRON), List.of(Tiers.DIAMOND), false, ()->{return Ingredient.of(Registry.getItem("steel_ingot"));}),
	CRANK(ToolDefaults.CRANK, null, List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE), false, ()->{return Ingredient.of(TagKey.create(Keys.ITEMS, new ResourceLocation("assemblylinemachines", "precious_gears")));}),
	MYSTIUM(ToolDefaults.MYSTIUM, "mystium", List.of(Tiers.NETHERITE), List.of(), true, ()->{return Ingredient.of(Registry.getItem("mystium_ingot"));}),
	NOVASTEEL(ToolDefaults.NOVASTEEL, null, List.of(ItemTiers.MYSTIUM.getItemTier()), List.of(), true, ()->{return Ingredient.of(Registry.getItem("novasteel_ingot"));}),
	CRG(ToolDefaults.CRG, "crg", null, null, false, () -> Ingredient.EMPTY);

	private final Tier itemTier;
	private final ArmorMaterial armorTier;
	private PowerToolType powerToolType = null;
	private final ToolDefaults toolDefaults;
	private final Cache<Stats, Number> statCache = CacheBuilder.newBuilder().build();

	ItemTiers(ToolDefaults toolDefaults, String armorSetName, List<Object> tiersAfter, List<Object> tiersBefore, boolean hasTag, Supplier<Ingredient> ingredient) {

		class ItemTier implements Tier{

			private final TagKey<Block> blockTag;

			public ItemTier() {
				this.blockTag = hasTag ? TagKey.create(Keys.BLOCKS, new ResourceLocation(AssemblyLineMachines.MODID, "needs_" + ItemTiers.this.toString().toLowerCase() + "_tool")) : null;
				if(tiersAfter != null && tiersBefore != null) TierSortingRegistry.registerTier(this, new ResourceLocation(AssemblyLineMachines.MODID, ItemTiers.this.toString().toLowerCase()), tiersAfter, tiersBefore);
			}

			@Override
			public int getUses() {
				return getStat(Stats.DURABILITY).intValue();
			}

			@Override
			public float getSpeed() {
				return getStat(Stats.HRV_SPEED).floatValue();
			}

			@Override
			public float getAttackDamageBonus() {
				return getStat(Stats.ATTACK).floatValue();
			}

			@Override
			public int getLevel() {
				return 0;
			}

			@Override
			public int getEnchantmentValue() {
				return getStat(Stats.ENCHANT).intValue();
			}

			@Override
			public Ingredient getRepairIngredient() {
				return ingredient.get();
			}

			@Override
			public TagKey<Block> getTag() {
				return blockTag;
			}
		}

		class ArmorTier implements ArmorMaterial{

			@Override
			public int getDurabilityForSlot(EquipmentSlot pSlot) {
				return Math.round(getStat(Stats.DURABILITY).intValue() / switch(pSlot) {
				case HEAD -> 3f;
				case CHEST -> 2f;
				case LEGS -> 2.5f;
				case FEET -> 3.25f;
				default -> throw new UnsupportedOperationException("Unsupported case value: " + pSlot.toString());
				});
			}

			@Override
			public int getDefenseForSlot(EquipmentSlot pSlot) {
				return Math.round(getStat(Stats.D_REDUCTION).intValue() * switch(pSlot) {
				case HEAD -> 1f;
				case CHEST -> 1.5f;
				case LEGS -> 1.25f;
				case FEET -> 0.8f;
				default -> throw new UnsupportedOperationException("Unsupported case value: " + pSlot.toString());
				});
			}

			@Override
			public int getEnchantmentValue() {
				return getStat(Stats.ENCHANT).intValue();
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
				return getStat(Stats.TOUGH).floatValue();
			}

			@Override
			public float getKnockbackResistance() {
				return getStat(Stats.KB_RES).floatValue();
			}

		}

		this.toolDefaults = toolDefaults;
		this.armorTier = new ArmorTier();
		this.itemTier = new ItemTier();
		try {
			this.powerToolType = PowerToolType.valueOf(this.toString());
		}catch(IllegalArgumentException e) {}
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

	private Number getStat(Stats stat) {
		try {
			return statCache.get(stat, () -> toolDefaults.get(stat));
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static enum ToolDefaults{

		TITANIUM(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 5, 7, 8, 1150, 0, 4, 0),
		STEEL(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 7, 9, 6, 1800, 0.1, 4, 2.5),
		CRANK(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.SP_ENERGY), 8, 11, 16, 75, 750),
		MYSTIUM(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH, Stats.SP_ENERGY, Stats.SP_ENH_ENERGY), 9, 19, 28, 150, 0.15, 7, 5, 1000000, 10000000),
		NOVASTEEL(List.of(Stats.ATTACK, Stats.HRV_SPEED, Stats.ENCHANT, Stats.DURABILITY, Stats.SP_ENERGY), 10.5, 23, 37, 300, 20000000),
		CRG(List.of(Stats.ENCHANT, Stats.DURABILITY, Stats.KB_RES, Stats.D_REDUCTION, Stats.TOUGH), 3, 750, 0, 5, 0);

		private final HashMap<Stats, Number> stats = new HashMap<>();

		ToolDefaults(List<Stats> statTypes, Number... data){
			if(statTypes.size() != data.length) throw new IllegalArgumentException("Stat types does not equal data length.");
			for(int i = 0; i < statTypes.size(); i++) stats.put(statTypes.get(i), data[i]);
		}

		public Number get(Stats stat) {
			return stats.get(stat);
		}
		
		public static enum Stats{
			ATTACK, HRV_SPEED, ENCHANT, DURABILITY, KB_RES, D_REDUCTION, TOUGH, SP_ENERGY, SP_ENH_ENERGY;
		}
	}
}