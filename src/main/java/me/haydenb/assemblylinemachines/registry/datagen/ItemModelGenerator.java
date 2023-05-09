package me.haydenb.assemblylinemachines.registry.datagen;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.compress.utils.FileNameUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.item.ItemDowsingRod;
import me.haydenb.assemblylinemachines.item.ItemHammer;
import me.haydenb.assemblylinemachines.item.powertools.IToolWithCharge;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.BushBlock;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemModelGenerator extends ItemModelProvider {

	private final PrintWriter writer;
	private final Collection<Path> inputs;

	public ItemModelGenerator(GatherDataEvent event, PrintWriter writer, Collection<Path> inputs) {
		super(event.getGenerator().getPackOutput(), AssemblyLineMachines.MODID, event.getExistingFileHelper());
		this.writer = writer;
		this.inputs = inputs;
		
		event.getGenerator().addProvider(true, this);
	}
	@Override
	protected void registerModels() {

		writer.println("[ITEM MODELS - INFO]: Starting item model generation...");
		List<File> files = new ArrayList<>();
		for(Path p : inputs) {
			File f = new File(p.toString() + "/assets/" + AssemblyLineMachines.MODID + "/models/item/");
			files.addAll(Lists.newArrayList(f.listFiles()));
		}
		List<String> exclude = Lists.transform(files, (f) -> FileNameUtils.getBaseName(f.getPath()));

		//BLOCK, HANDHELD, OTHER
		int[] stats = new int[8];

		for(Item item : Registry.getAllItems()) {
			String name = ForgeRegistries.ITEMS.getKey(item).getPath();
			if(exclude.contains(name)) {
				stats[6]++;
			}else {
				if(item instanceof BlockItem blockItem) {
					if(blockItem.getBlock() instanceof BushBlock) {
						this.blockTextureGenerated(name);
						stats[7]++;

					}else {
						this.blockParent(name);
						stats[1]++;

					}
				}else if(item instanceof IToolWithCharge chargeTool && chargeTool.getPowerToolType().needsActiveModel(item)) {
					this.toolWithAbility(name);
					stats[5]++;

				}else if(item instanceof SwordItem || item instanceof AxeItem || item instanceof PickaxeItem || item instanceof ShovelItem
						|| item instanceof HoeItem || item instanceof ItemHammer || item instanceof ItemDowsingRod) {
					this.simple(name, "item/handheld");
					stats[2]++;

				}else if(item instanceof BucketItem bucket){
					this.bucket(name, ForgeRegistries.FLUIDS.getKey(bucket.getFluid()));
					stats[3]++;

				}else if(item instanceof SpawnEggItem){
					super.withExistingParent(name, "item/template_spawn_egg");
					stats[4]++;

				}else {
					this.simple(name, "item/generated");
					stats[0]++;

				}
			}
		}

		writer.println("[ITEM MODELS - INFO]: Generated " + stats[0] + " Item Model(s) for general item models.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[1] + " Block Model(s) with a parent of the corresponding block model.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[2] + " Handheld Model(s) for tools.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[3] + " Bucket Model(s) for mod fluid buckets.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[4] + " Spawn Egg Model(s) for mod mob Spawn Eggs.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[5] + " Power Tool Model(s) with active models for tools with secondary abilities.");
		writer.println("[ITEM MODELS - INFO]: Generated " + stats[7] + " Bush Model(s) for mod saplings, flowers, bushes, and other plants.");
		writer.println("[ITEM MODELS - INFO]: Skipped " + stats[6] + " model(s) which had an existing model file in an input directory.");
	}

	private void blockParent(String name) {
		super.withExistingParent(name, new ResourceLocation(AssemblyLineMachines.MODID, "block/" + name));
	}

	private void blockTextureGenerated(String name) {
		super.withExistingParent(name, "item/generated").texture("layer0", new ResourceLocation(AssemblyLineMachines.MODID, "block/" + name));
	}

	private void simple(String name, String parent) {
		super.withExistingParent(name, parent).texture("layer0", new ResourceLocation(AssemblyLineMachines.MODID, "item/" + name));
	}

	private void bucket(String bucketItem, ResourceLocation fluid) {
		class BucketLoader extends CustomLoaderBuilder<ItemModelBuilder>{

			private static final ResourceLocation BUCKET_LOADER = new ResourceLocation("forge", "fluid_container");

			protected BucketLoader(ItemModelBuilder parent,
					ExistingFileHelper existingFileHelper) {
				super(BUCKET_LOADER, parent, existingFileHelper);
			}

			@Override
			public JsonObject toJson(JsonObject json) {
				super.toJson(json);

				json.addProperty("fluid", fluid.toString());

				return json;
			}

		}

		super.withExistingParent(bucketItem, new ResourceLocation("forge", "item/default"))
		.texture("base", new ResourceLocation(AssemblyLineMachines.MODID, "item/bucket/base"))
		.texture("fluid", new ResourceLocation(AssemblyLineMachines.MODID, "item/bucket/fluid")).customLoader((mb, fh) -> new BucketLoader(mb, fh));
	}

	private void toolWithAbility(String name) {
		super.withExistingParent(name + "_active", "item/handheld").texture("layer0", new ResourceLocation(AssemblyLineMachines.MODID, "item/" + name + "_active"));

		super.withExistingParent(name, "item/handheld").texture("layer0", new ResourceLocation(AssemblyLineMachines.MODID, "item/" + name))
		.override().predicate(new ResourceLocation(AssemblyLineMachines.MODID, "active"), 1)
		.model(new ModelFile.UncheckedModelFile(new ResourceLocation(AssemblyLineMachines.MODID, "item/" + name + "_active")));
	}
}
