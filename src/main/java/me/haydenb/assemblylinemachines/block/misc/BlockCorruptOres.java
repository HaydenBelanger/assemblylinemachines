package me.haydenb.assemblylinemachines.block.misc;

import java.util.function.Supplier;

import me.haydenb.assemblylinemachines.block.misc.CorruptBlock.CorruptBlockWithAxis;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public enum BlockCorruptOres{
	COAL(() -> Items.COAL, 2, 2), COPPER(() -> Items.RAW_COPPER, 4, 10),
	DIAMOND(() -> Items.DIAMOND, 2, 2), EMERALD(() -> Items.EMERALD, 2, 2),
	GOLD(() -> Items.RAW_GOLD, 2, 2), IRON(() -> Items.RAW_IRON, 2, 2),
	LAPIS(() -> Items.LAPIS_LAZULI, 8, 18), REDSTONE(() -> Items.REDSTONE, 8, 10),
	TITANIUM(() -> Registry.getItem("raw_titanium"), 2, 2);

	public final Supplier<Item> itemDrop;
	public final float minDrop;
	public final float maxDrop;

	BlockCorruptOres(Supplier<Item> drop, float minDrop, float maxDrop){
		this.itemDrop = drop;
		this.minDrop = minDrop;
		this.maxDrop = maxDrop;
	}

	public static void createCorruptOres(){
		for(BlockCorruptOres ores : BlockCorruptOres.values()) {
			String ore = ores.toString().toLowerCase();
			Registry.createBlock("corrupt_" + ore + "_ore", new CorruptBlock(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3f, 9f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL), true);
			Registry.createBlock("corrupt_basalt_" + ore + "_ore", new CorruptBlockWithAxis(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(5f, 15f).requiresCorrectToolForDrops(), BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_DIAMOND_TOOL, false, true), true);
		}
	}
}