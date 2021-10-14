package me.haydenb.assemblylinemachines.registry.datagen;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class MineableBlockProvider extends BlockTagsProvider {

	public static final Named<Block> NEEDS_NETHERITE_TOOL = ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCKS, new ResourceLocation("forge", "needs_netherite_tool"));
	
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
				//Default as long as the block isn't a LiquidBlock
				this.tag(BlockTags.NEEDS_IRON_TOOL).add(b);
				this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b);
			}
		}
	}
}
