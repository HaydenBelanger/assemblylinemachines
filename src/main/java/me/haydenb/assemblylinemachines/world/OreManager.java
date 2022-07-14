package me.haydenb.assemblylinemachines.world;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Supplier;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.misc.CorruptBlock;
import me.haydenb.assemblylinemachines.block.misc.CorruptBlock.CorruptBlockWithAxis;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE, modid = AssemblyLineMachines.MODID)
public class OreManager {
	
	public static final ArrayList<Holder<PlacedFeature>> PLACED_FEATURES = new ArrayList<>();
	
	@SubscribeEvent
	public static void placeOres(BiomeLoadingEvent event) {
		for(var placedFeature : PLACED_FEATURES) event.getGeneration().addFeature(Decoration.UNDERGROUND_ORES, Holder.direct(placedFeature.value()));
	}
	
	public static void registerOres() {
		try {
			Files.find(Paths.get(Thread.currentThread().getContextClassLoader().getResource("data/assemblylinemachines/worldgen/placed_feature_raw").toURI()), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile()).forEach((p) -> {
				try {
					PlacedFeature.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(new BufferedInputStream(Files.newInputStream(p)))))
						.resultOrPartial((s) -> AssemblyLineMachines.LOGGER.fatal("In relation to " + p.getFileName().toString() + ": " + s)).ifPresent((cf) -> PLACED_FEATURES.add(cf));
				}catch(Exception e) {
					AssemblyLineMachines.LOGGER.fatal("Error while codec-deserializing " + p.getFileName().toString() + ": " + e.getMessage());
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static enum CorruptOres{
		COAL(() -> Items.COAL, 2, 2), COPPER(() -> Items.RAW_COPPER, 4, 10), 
		DIAMOND(() -> Items.DIAMOND, 2, 2), EMERALD(() -> Items.EMERALD, 2, 2), 
		GOLD(() -> Items.RAW_GOLD, 2, 2), IRON(() -> Items.RAW_IRON, 2, 2), 
		LAPIS(() -> Items.LAPIS_LAZULI, 8, 18), REDSTONE(() -> Items.REDSTONE, 8, 10), 
		TITANIUM(() -> Registry.getItem("raw_titanium"), 2, 2);
		
		public final Supplier<Item> itemDrop;
		public final float minDrop;
		public final float maxDrop;
		
		CorruptOres(Supplier<Item> drop, float minDrop, float maxDrop){
			this.itemDrop = drop;
			this.minDrop = minDrop;
			this.maxDrop = maxDrop;
		}
		
		public static void createCorruptOres(){
			for(CorruptOres ores : CorruptOres.values()) {
				String ore = ores.toString().toLowerCase();
				Registry.createBlock("corrupt_" + ore + "_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 9f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
				Registry.createBlock("corrupt_basalt_" + ore + "_ore", new CorruptBlockWithAxis(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 15f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, false, true), true);
			}
		}
	}
}
