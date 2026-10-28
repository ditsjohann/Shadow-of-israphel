package com.ditsjohann.shadowofisraphel.client.render;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import com.ditsjohann.shadowofisraphel.entity.SandMonsterEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

public class SandMonsterRenderer extends BipedEntityRenderer<SandMonsterEntity, ZombieEntityModel<SandMonsterEntity>> {
	private static final Identifier TEXTURE = ShadowOfIsraphel.id("textures/entity/sand_monster.png");

	public SandMonsterRenderer(EntityRendererFactory.Context context) {
		super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
		this.addFeature(new ArmorFeatureRenderer<>(this,
				new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
				new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
				context.getModelManager()));
	}

	@Override
	public Identifier getTexture(SandMonsterEntity entity) {
		return TEXTURE;
	}
}
