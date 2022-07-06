package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityCorruptShell extends Zombie{

	public EntityCorruptShell(EntityType<? extends EntityCorruptShell> type, Level worldIn) {
		super(type, worldIn);
	}

	public static AttributeSupplier.Builder registerAttributeMap(){
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32d).add(Attributes.FOLLOW_RANGE, 40d).add(Attributes.MOVEMENT_SPEED, 0.38d).add(Attributes.ATTACK_DAMAGE, 4.5d).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 1d);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return Registry.CORRUPT_SHELL_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return Registry.CORRUPT_SHELL_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return Registry.CORRUPT_SHELL_DEATH.get();
	}

	@Override
	protected SoundEvent getStepSound() {
		return Registry.CORRUPT_SHELL_STEP.get();
	}

	@Override
	protected boolean isSunSensitive() {
		return false;
	}

	@Override
	public void playerTouch(Player entity) {
		super.playerTouch(entity);

		if(!entity.isCreative()) {
			entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140));
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140));
			entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140));
		}
	}

	public static class EntityCorruptShellRender extends LivingEntityRenderer<EntityCorruptShell, EntityCorruptShellModel>{


		public EntityCorruptShellRender(Context rendererManager, EntityCorruptShellModel entityModelIn, float shadowSizeIn) {
			super(rendererManager, entityModelIn, shadowSizeIn);
		}

		@Override
		public ResourceLocation getTextureLocation(EntityCorruptShell entity) {
			return new ResourceLocation(AssemblyLineMachines.MODID, "textures/entity/corrupt_shell.png");
		}

		@Override
		protected boolean shouldShowName(EntityCorruptShell pEntity) {
			return false;
		}

	}

	public static class EntityCorruptShellRenderFactory implements EntityRendererProvider<EntityCorruptShell>{

		@Override
		public EntityRenderer<EntityCorruptShell> create(Context context) {
			return new EntityCorruptShellRender(context, new EntityCorruptShellModel(context), 0.1f);
		}

	}

	public static class EntityCorruptShellModel extends ZombieModel<EntityCorruptShell>{


		protected EntityCorruptShellModel(Context context) {
			super(context.bakeLayer(ModelLayers.ZOMBIE));

			//this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
		}

		@Override
		public boolean isAggressive(EntityCorruptShell pEntity) {
			return pEntity.isAggressive();
		}

	}
}
