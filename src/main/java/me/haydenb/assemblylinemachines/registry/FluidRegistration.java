package me.haydenb.assemblylinemachines.registry;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.fluid.ALMFluid;
import me.haydenb.assemblylinemachines.block.fluid.ALMFluid.ALMFluidBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidRegistration {

	public static void buildAndRegister(String name, ForgeFlowingFluid source, ForgeFlowingFluid flowing, BucketItem bucket, FlowingFluidBlock block) {
		
		source.setRegistryName(name);
		Registry.fluidRegistry.put(name, source);
		
		if(flowing != null) {
			flowing.setRegistryName(name + "_flowing");
			Registry.fluidRegistry.put(name + "_flowing", flowing);
		}
		
		
		if(block != null) {
			Registry.blockRegistry.put(name + "_block", block.setRegistryName(name + "_block"));
		}
		
		if(bucket != null) {
			Registry.itemRegistry.put(name + "_item", bucket.setRegistryName(name + "_bucket"));
		}
	}
	
	public static void buildAndRegister(String name, ForgeFlowingFluid source, ForgeFlowingFluid flowing, boolean bucket, FlowingFluidBlock block) {
		
		BucketItem bucketitem = null;
		
		if(bucket == true) {
			bucketitem = new BucketItem(() -> Registry.fluidRegistry.get(name), new Item.Properties().maxStackSize(1).containerItem(Items.BUCKET).group(Registry.creativeTab));
		}
		
		buildAndRegister(name, source, flowing, bucketitem, block);
	}
	
	public static void buildAndRegister(String name, ForgeFlowingFluid source, ForgeFlowingFluid flowing, boolean bucket, Tag<Fluid> tag, Material material) {
		
		FlowingFluidBlock block = null;
		if(tag != null && material != null) {
			block = new ALMFluidBlock(name, tag, material);
		}	
		
		buildAndRegister(name, source, flowing, bucket, block);
	}
	
	public static void buildAndRegister(String name, int temperature, boolean gaseous, boolean bucket, Tag<Fluid> tag, Material material) {
		
		boolean block = false;
		
		if(tag != null && material != null) {
			block = true;
		}
		ALMFluid source = build(name, temperature, gaseous, bucket, block, true);
		ALMFluid flowing = build(name, temperature, gaseous, bucket, block, false);
		
		buildAndRegister(name, source, flowing, bucket, tag, material);		
	}
	
	public static ALMFluid build(String name, int temperature, boolean gaseous, boolean bucket, boolean block, boolean source) {
		ForgeFlowingFluid.Properties properties = buildProperties(name, temperature, gaseous, bucket, block);
		
		return new ALMFluid(properties, source);
	}
	
	public static ForgeFlowingFluid.Properties buildProperties(String name, int temperature, boolean gaseous, boolean bucket, boolean block){
		
		FluidAttributes.Builder attributes;
		if(gaseous) {
			attributes = FluidAttributes.builder(new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name), new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name)).temperature(temperature).gaseous();
		}else {
			attributes = FluidAttributes.builder(new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name), new ResourceLocation(AssemblyLineMachines.MODID, "fluid/" + name + "_flowing")).temperature(temperature);
		}
		
		ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(() -> Registry.getFluid(name), () -> Registry.getFluid(name + "_flowing"), attributes);
		if(block) {
			properties.block(() -> (FlowingFluidBlock) Registry.getBlock(name + "_block"));
		}
		
		if(bucket) {
			properties.bucket(() -> Registry.getItem(name + "_bucket"));
		}
		
		return properties;
	}
}
