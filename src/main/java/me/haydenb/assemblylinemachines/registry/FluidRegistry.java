package me.haydenb.assemblylinemachines.registry;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.block.fluid.*;
import me.haydenb.assemblylinemachines.util.General;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidRegistry {

	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, AssemblyLineMachines.MODID);
	
	
	public static final RegistryObject<ForgeFlowingFluid> OIL = FLUIDS.register("oil", () -> new FluidOil(true));
	public static final RegistryObject<ForgeFlowingFluid> OIL_FLOWING = FLUIDS.register("oil_flowing", () -> new FluidOil(false));
	
	public static final RegistryObject<ForgeFlowingFluid> CONDENSED_VOID = FLUIDS.register("condensed_void", () -> new FluidCondensedVoid(true));
	public static final RegistryObject<ForgeFlowingFluid> CONDENSED_VOID_FLOWING = FLUIDS.register("condensed_void_flowing", () -> new FluidCondensedVoid(false));
	
	public static final RegistryObject<ForgeFlowingFluid> NAPHTHA = FLUIDS.register("naphtha", () -> new FluidNaphtha(true));
	public static final RegistryObject<ForgeFlowingFluid> NAPHTHA_FLOWING = FLUIDS.register("naphtha_flowing", () -> new FluidNaphtha(false));
	
	public static final RegistryObject<ForgeFlowingFluid> GASOLINE = FLUIDS.register("gasoline", () -> new FluidOilProduct("gasoline", true));
	public static final RegistryObject<ForgeFlowingFluid> GASOLINE_FLOWING = FLUIDS.register("gasoline_flowing", () -> new FluidOilProduct("gasoline", false));
	
	public static final RegistryObject<ForgeFlowingFluid> DIESEL = FLUIDS.register("diesel", () -> new FluidOilProduct("diesel", true));
	public static final RegistryObject<ForgeFlowingFluid> DIESEL_FLOWING = FLUIDS.register("diesel_flowing", () -> new FluidOilProduct("diesel", false));
	
	public static final RegistryObject<ForgeFlowingFluid> LIQUID_EXPERIENCE = FLUIDS.register("liquid_experience", () -> buildBasicFluid("liquid_experience", 35, false, true, true, true));
	public static final RegistryObject<ForgeFlowingFluid> LIQUID_EXPERIENCE_FLOWING = FLUIDS.register("liquid_experience_flowing", () -> buildBasicFluid("liquid_experience", 35, false, true, true, false));
	
	public static final RegistryObject<ForgeFlowingFluid> ETHANE = FLUIDS.register("ethane", () -> new GaseousFluid("ethane", true, 0x135f0f));
	public static final RegistryObject<ForgeFlowingFluid> ETHYLENE = FLUIDS.register("ethylene", () -> new GaseousFluid("ethylene", true, 0xb3aac3));
	public static final RegistryObject<ForgeFlowingFluid> PROPANE = FLUIDS.register("propane", () -> new GaseousFluid("propane", true, 0x5f0f1c));
	public static final RegistryObject<ForgeFlowingFluid> PROPYLENE = FLUIDS.register("propylene", () -> new GaseousFluid("propylene", true, 0x290d55));
	
	static void registerBuckets() {
		
		//See registerFluidBlocks. Bugged patches.
		Registry.createItem("oil_bucket", new BucketItem(() -> OIL.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
		Registry.createItem("condensed_void_bucket", new BucketItem(() -> CONDENSED_VOID.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
		Registry.createItem("naphtha_bucket", new BucketItem(() -> NAPHTHA.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
		Registry.createItem("gasoline_bucket", new BucketItem(() -> GASOLINE.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
		Registry.createItem("diesel_bucket", new BucketItem(() -> DIESEL.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
		Registry.createItem("liquid_experience_bucket", new BucketItem(() -> LIQUID_EXPERIENCE.get(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(Registry.creativeTab)));
	}	
	
	static void registerFluidBlocks() {
		
		Registry.createBlock("oil_block", new FluidOil.FluidOilBlock(() -> OIL.get()), false);
		Registry.createBlock("condensed_void_block", new FluidCondensedVoid.FluidCondensedVoidBlock(() -> CONDENSED_VOID.get()), false);
		Registry.createBlock("naphtha_block", new FluidNaphtha.FluidNaphthaBlock(() -> NAPHTHA.get()), false);
		Registry.createBlock("gasoline_block", new FluidOilProduct.FluidOilProductBlock(() -> GASOLINE.get()), false);
		Registry.createBlock("diesel_block", new FluidOilProduct.FluidOilProductBlock(() -> DIESEL.get()), false);
		Registry.createBlock("liquid_experience_block", new ALMFluid.ALMFluidBlock(() -> LIQUID_EXPERIENCE.get(), ALMFluid.LIQUID_EXPERIENCE, Material.WATER), false);
		
	}
	
	public static void updateInternalStoredFluids() throws Exception{
		
		AssemblyLineMachines.LOGGER.info("Patching LiquidBlocks from Assembly Line Machines...");
		
		//Patched AT due to issue when compiled build version.
		General.setFluidField(Registry.getBlock("oil_block"), OIL.get());
		General.setFluidField(Registry.getBlock("condensed_void_block"), CONDENSED_VOID.get());
		General.setFluidField(Registry.getBlock("naphtha_block"), NAPHTHA.get());
		General.setFluidField(Registry.getBlock("gasoline_block"), GASOLINE.get());
		General.setFluidField(Registry.getBlock("diesel_block"), DIESEL.get());
		General.setFluidField(Registry.getBlock("liquid_experience_block"), LIQUID_EXPERIENCE.get());
	}
	
	private static ALMFluid buildBasicFluid(String name, int temperature, boolean gaseous, boolean bucket, boolean block, boolean source){
		
		return new ALMFluid(buildProperties(name, temperature, gaseous, bucket, block), source);
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
			properties.block(() -> (LiquidBlock) Registry.getBlock(name + "_block"));
		}
		
		
		if(bucket) {
			properties.bucket(() -> Registry.getItem(name + "_bucket"));
		}
		
		
		return properties;
	}
}
