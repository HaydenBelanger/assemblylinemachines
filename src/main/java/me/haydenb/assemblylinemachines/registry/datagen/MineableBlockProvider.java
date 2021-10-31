package me.haydenb.assemblylinemachines.registry.datagen;

import java.util.ArrayList;
import java.util.HashMap;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class MineableBlockProvider extends BlockTagsProvider {

	public static final Named<Block> NEEDS_NETHERITE_TOOL = ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, new ResourceLocation("forge", "needs_netherite_tool"));
	private static final HashMap<Material, Named<Block>> MATERIAL_TAGS = new HashMap<>();
	static {
		MATERIAL_TAGS.put(Material.AMETHYST, BlockTags.MINEABLE_WITH_PICKAXE);
		MATERIAL_TAGS.put(Material.DIRT, BlockTags.MINEABLE_WITH_SHOVEL);
		MATERIAL_TAGS.put(Material.GRASS, BlockTags.MINEABLE_WITH_SHOVEL);
		MATERIAL_TAGS.put(Material.HEAVY_METAL, BlockTags.MINEABLE_WITH_PICKAXE);
		MATERIAL_TAGS.put(Material.METAL, BlockTags.MINEABLE_WITH_PICKAXE);
		MATERIAL_TAGS.put(Material.NETHER_WOOD, BlockTags.MINEABLE_WITH_AXE);
		MATERIAL_TAGS.put(Material.SAND, BlockTags.MINEABLE_WITH_SHOVEL);
		MATERIAL_TAGS.put(Material.SNOW, BlockTags.MINEABLE_WITH_SHOVEL);
		MATERIAL_TAGS.put(Material.STONE, BlockTags.MINEABLE_WITH_PICKAXE);
		MATERIAL_TAGS.put(Material.WOOD, BlockTags.MINEABLE_WITH_AXE);
		MATERIAL_TAGS.put(Material.CLAY, BlockTags.MINEABLE_WITH_SHOVEL);
	}
	
	private static final ArrayList<ResourceLocation> OK_NO_TAGS = new ArrayList<>();
	static {
		OK_NO_TAGS.add(new ResourceLocation(AssemblyLineMachines.MODID, "naphtha_fire"));
	}
	
	public MineableBlockProvider(DataGenerator pGenerator, ExistingFileHelper helper) {
		super(pGenerator, AssemblyLineMachines.MODID, helper);
	}

	
	@Override
	protected void addTags() {
		for(Block b : Registry.blockRegistry.values()) {
			if(b instanceof IBlockWithHarvestableTags) {
				IBlockWithHarvestableTags harvestable = (IBlockWithHarvestableTags) b;
				if(harvestable.getToolLevel() != null) {
					this.tag(harvestable.getToolLevel()).add(b);
				}
				
				if(harvestable.getToolType() != null) {
					this.tag(harvestable.getToolType()).add(b);
				}
			}else if(!(b instanceof LiquidBlock)) {
				Named<Block> nb = MATERIAL_TAGS.get(b.defaultBlockState().getMaterial());
				if(nb != null) {
					this.tag(nb).add(b);
				}else {
					if(!OK_NO_TAGS.contains(b.getRegistryName())) {
						AssemblyLineMachines.LOGGER.warn("No method of tag auto-generation present for " + b.getRegistryName().toString() + ".");
					}
					
				}
			}
		}
	}
}
