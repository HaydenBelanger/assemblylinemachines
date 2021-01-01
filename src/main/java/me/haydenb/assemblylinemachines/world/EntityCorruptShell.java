package me.haydenb.assemblylinemachines.world;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.AbstractZombieModel;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCorruptShell extends ZombieEntity{

	public static final EntityType<EntityCorruptShell> CORRUPT_SHELL = EntityType.Builder.create(EntityCorruptShell::new, EntityClassification.MONSTER).build(new ResourceLocation(AssemblyLineMachines.MODID, "corrupt_shell").toString());
	
	public EntityCorruptShell(EntityType<? extends EntityCorruptShell> type, World worldIn) {
		super(type, worldIn);
	}

	public static AttributeModifierMap.MutableAttribute registerAttributeMap(){
		return MonsterEntity.func_234295_eP_().func_233815_a_(Attributes.field_233818_a_, 32d).func_233815_a_(Attributes.field_233819_b_, 40d).func_233815_a_(Attributes.field_233821_d_, 0.38d).func_233815_a_(Attributes.field_233823_f_, 4.5d).func_233815_a_(Attributes.field_233829_l_, 1d);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_HUSK_AMBIENT;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_HUSK_HURT;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_HUSK_DEATH;
	}
	
	@Override
	protected boolean shouldBurnInDay() {
		return false;
	}
	
	@Override
	public void onCollideWithPlayer(PlayerEntity entity) {
		super.onCollideWithPlayer(entity);
		
		if(!entity.isCreative()) {
			entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 140));
			entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 140));
			entity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 140));
		}
		
	}
	
	public static class EntityCorruptShellRender extends LivingRenderer<EntityCorruptShell, EntityCorruptShellModel>{

		public EntityCorruptShellRender(EntityRendererManager rendererManager, EntityCorruptShellModel entityModelIn, float shadowSizeIn) {
			super(rendererManager, entityModelIn, shadowSizeIn);
		}

		@Override
		public ResourceLocation getEntityTexture(EntityCorruptShell entity) {
			return new ResourceLocation(AssemblyLineMachines.MODID, "textures/entity/corrupt_shell.png");
		}

		public static class EntityCorruptShellRenderFactory implements IRenderFactory<EntityCorruptShell>{

			@Override
			public EntityRenderer<? super EntityCorruptShell> createRenderFor(EntityRendererManager manager) {
				return new EntityCorruptShellRender(manager, new EntityCorruptShellModel(), 0.1f);
			}
			
		}
		
		@Override
		protected boolean canRenderName(EntityCorruptShell entity) {
			return false;
		}
		
	}
	
	public static class EntityCorruptShellModel extends AbstractZombieModel<EntityCorruptShell>{

		protected EntityCorruptShellModel() {
			
			super(1f, 0f, 64, 64);
		}

		@Override
		public boolean isAggressive(EntityCorruptShell entity) {
			return entity.isAggressive();
		}

		
	}
}
