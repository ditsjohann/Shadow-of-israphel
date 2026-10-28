package com.ditsjohann.shadowofisraphel.client.render;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import com.ditsjohann.shadowofisraphel.entity.IsraphelEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class IsraphelRenderer extends BipedEntityRenderer<IsraphelEntity, PlayerEntityModel<IsraphelEntity>> {
	private static final Identifier TEXTURE = ShadowOfIsraphel.id("textures/entity/israphel.png");
	private static final RenderLayer EYES = RenderLayer.getEyes(ShadowOfIsraphel.id("textures/entity/israphel_eyes.png"));

	public IsraphelRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
		this.addFeature(new Eyes(this));
	}

	@Override
	protected void scale(IsraphelEntity entity, MatrixStack matrices, float amount) {
		// Israphel stands a little taller than any mortal.
		matrices.scale(1.1f, 1.1f, 1.1f);
	}

	@Override
	public Identifier getTexture(IsraphelEntity entity) {
		return TEXTURE;
	}

	private static class Eyes extends EyesFeatureRenderer<IsraphelEntity, PlayerEntityModel<IsraphelEntity>> {
		Eyes(FeatureRendererContext<IsraphelEntity, PlayerEntityModel<IsraphelEntity>> context) {
			super(context);
		}

		@Override
		public RenderLayer getEyesTexture() {
			return EYES;
		}
	}
}
