package me.haydenb.assemblylinemachines.client.armor;

import java.util.*;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.client.model.geom.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AssemblyLineMachines.MODID, bus = Bus.MOD)
public class ArmorData {

	private static final HashMap<String, Map<EquipmentSlot, ArmorModel>> BAKED_MODELS = new HashMap<>();
	
	private static final ModelLayerLocation MYSTIUM_OUTER_LAYER = get("mystium", "outer");
	private static final ModelLayerLocation MYSTIUM_INNER_LAYER = get("mystium", "inner");
	
	private static final ModelLayerLocation ENHANCED_MYSTIUM_OUTER_LAYER = get("enhanced_mystium", "outer");
	
	@SubscribeEvent
	public static void registerLayers(RegisterLayerDefinitions event) {
		event.registerLayerDefinition(MYSTIUM_OUTER_LAYER, () -> MystiumArmorModel.outerLayer());
		event.registerLayerDefinition(MYSTIUM_INNER_LAYER, () -> MystiumArmorModel.innerLayer());
		
		event.registerLayerDefinition(ENHANCED_MYSTIUM_OUTER_LAYER, () -> MystiumArmorModel.outerLayerEnhanced());
	}
	
	@SubscribeEvent
	public static void addLayers(AddLayers event) {
		EntityModelSet ems = event.getEntityModels();
		
		BAKED_MODELS.put("mystium", bake(ems, MYSTIUM_OUTER_LAYER, MYSTIUM_INNER_LAYER));
		BAKED_MODELS.put("enhanced_mystium", bake(ems, ENHANCED_MYSTIUM_OUTER_LAYER, MYSTIUM_INNER_LAYER));
	}
	
	private static Map<EquipmentSlot, ArmorModel> bake(EntityModelSet ems, ModelLayerLocation outer, ModelLayerLocation inner){
		Map<EquipmentSlot, ArmorModel> map = new EnumMap<>(EquipmentSlot.class);
		for(EquipmentSlot slot : EquipmentSlot.values()) {
			ModelPart model = ems.bakeLayer(slot == EquipmentSlot.LEGS ? inner : outer);
			map.put(slot, new ArmorModel(model, slot));
		}
		return map;
	}
	
	private static ModelLayerLocation get(String setName, String part) {
		return new ModelLayerLocation(new ResourceLocation(AssemblyLineMachines.MODID, setName + "_" + part), part + "_armor");
	}
	
	public static Optional<ArmorModel> get(String cat, EquipmentSlot slot) {
		return Optional.ofNullable(BAKED_MODELS.getOrDefault(cat, Collections.emptyMap()).get(slot));
	}
}
