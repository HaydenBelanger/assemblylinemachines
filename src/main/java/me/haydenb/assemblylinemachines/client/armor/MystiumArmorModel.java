package me.haydenb.assemblylinemachines.client.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MystiumArmorModel {

	@SuppressWarnings("unused")
	public static LayerDefinition outerLayerEnhanced() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(47, 49).addBox(-2.5F, 7.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(76, 69).addBox(-3.0F, 8.5F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, 7.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(62, 75).addBox(-1.0F, 8.5F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 30).addBox(-4.5F, 2.0F, -2.5F, 9.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(22, 44).addBox(-1.0F, 3.0F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 75).addBox(-5.0F, 0.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(82, 3).addBox(-5.0F, 6.0F, -3.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 47).addBox(-3.0F, 8.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 27).addBox(-2.0F, 1.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 67).addBox(-2.0F, 1.0F, 2.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 0).addBox(-5.0F, 6.0F, -2.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(37, 20).addBox(-3.0F, 8.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 27).addBox(2.0F, 0.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(63, 27).addBox(-3.0F, 2.0F, 4.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-20.0F, 1.0F, 1.0F, 22.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(67, 59).addBox(-11.0F, -1.0F, 0.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.0F, 5.0F, 0.0F, 0.1745F, 0.2618F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 9).addBox(-2.0F, 1.0F, 1.0F, 22.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(69, 0).addBox(-1.0F, -1.0F, 0.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -4.0F, 5.0F, 0.0F, -0.1745F, -0.2618F));

		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(88, 88).addBox(-1.0F, -1.0F, 0.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 3.0F, 5.0F, 0.0F, 0.0F, -1.2217F));

		PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(88, 85).addBox(-7.0F, -1.0F, 0.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.0F, 5.0F, 0.0F, 0.0F, 1.2217F));

		PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(28, 75).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 3.0F, 5.0F, -0.0873F, 0.9599F, 0.0F));

		PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(77, 22).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.0F, 5.0F, -0.0873F, -0.9599F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(62, 62).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(13, 87).addBox(1.5F, 5.5F, -2.5F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(72, 39).addBox(2.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(79, 39).addBox(-1.0F, 0.0F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(63, 30).addBox(1.0F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 62).addBox(1.0F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(83, 46).addBox(2.0F, 4.0F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(78, 85).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(21, 90).addBox(0.0F, -3.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(26, 62).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(29, 86).addBox(-3.5F, 5.5F, -2.5F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(63, 34).addBox(-4.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(76, 78).addBox(-4.0F, 0.0F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 49).addBox(-4.0F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 23).addBox(-4.0F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 83).addBox(-4.0F, 4.0F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(62, 84).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(37, 89).addBox(-1.0F, -3.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(92, 12).addBox(-3.5F, -5.5F, -4.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(11, 53).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(52, 29).addBox(3.5F, -5.5F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(86, 6).addBox(-3.5F, -5.5F, 2.5F, 7.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 22).addBox(-1.0F, -6.0F, -5.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(52, 43).addBox(-3.0F, -7.0F, -5.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(27, 22).addBox(-3.0F, -9.0F, -5.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(2.0F, -9.0F, -5.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(34, 76).addBox(-1.0F, -9.0F, -2.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(90, 67).addBox(-5.0F, -9.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 31).addBox(1.0F, -9.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(77, 62).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 44).addBox(-1.0F, -9.0F, -5.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(27, 13).addBox(-10.5F, -1.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(27, 15).addBox(0.5F, -1.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(44, 7).addBox(-10.5F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, 0.8727F, 0.0F, 0.0F));

		PartDefinition cube_r8 = head.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(52, 20).addBox(0.0F, -1.0F, -4.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@SuppressWarnings("unused")
	public static LayerDefinition outerLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(47, 49).addBox(-2.5F, 7.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(76, 69).addBox(-3.0F, 8.5F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, 7.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(62, 75).addBox(-1.0F, 8.5F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 30).addBox(-4.5F, 2.0F, -2.5F, 9.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(22, 44).addBox(-1.0F, 3.0F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 75).addBox(-5.0F, 0.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(82, 3).addBox(-5.0F, 6.0F, -3.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 47).addBox(-3.0F, 8.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 27).addBox(-2.0F, 1.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 67).addBox(-2.0F, 1.0F, 2.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 0).addBox(-5.0F, 6.0F, -2.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(37, 20).addBox(-3.0F, 8.0F, 2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 27).addBox(2.0F, 0.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(62, 62).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(13, 87).addBox(1.5F, 5.5F, -2.5F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(72, 39).addBox(2.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(79, 39).addBox(-1.0F, 0.0F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(63, 30).addBox(1.0F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 62).addBox(1.0F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(83, 46).addBox(2.0F, 4.0F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(78, 85).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(21, 90).addBox(0.0F, -3.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(26, 62).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(29, 86).addBox(-3.5F, 5.5F, -2.5F, 2.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(63, 34).addBox(-4.0F, -3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(76, 78).addBox(-4.0F, 0.0F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(20, 49).addBox(-4.0F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 23).addBox(-4.0F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 83).addBox(-4.0F, 4.0F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(62, 84).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(37, 89).addBox(-1.0F, -3.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(92, 12).addBox(-3.5F, -5.5F, -4.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(11, 53).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(52, 29).addBox(3.5F, -5.5F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(86, 6).addBox(-3.5F, -5.5F, 2.5F, 7.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 22).addBox(-1.0F, -6.0F, -5.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(52, 43).addBox(-3.0F, -7.0F, -5.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(27, 22).addBox(-3.0F, -9.0F, -5.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(2.0F, -9.0F, -5.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(34, 76).addBox(-1.0F, -9.0F, -2.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(90, 67).addBox(-5.0F, -9.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 31).addBox(1.0F, -9.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(77, 62).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(15, 44).addBox(-1.0F, -9.0F, -5.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(27, 13).addBox(-10.5F, -1.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(27, 15).addBox(0.5F, -1.0F, 1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(44, 7).addBox(-10.5F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, 0.8727F, 0.0F, 0.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(52, 20).addBox(0.0F, -1.0F, -4.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@SuppressWarnings("unused")
	public static LayerDefinition innerLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(47, 49).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 83).addBox(-3.0F, 3.0F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(80, 13).addBox(0.0F, 3.0F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(38, 12).addBox(-5.0F, 10.0F, -3.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(22, 54).addBox(-3.0F, 12.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
}
