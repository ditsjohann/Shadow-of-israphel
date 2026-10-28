package com.ditsjohann.shadowofisraphel.client.render;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import com.ditsjohann.shadowofisraphel.entity.HatManEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class HatManRenderer extends BipedEntityRenderer<HatManEntity, PlayerEntityModel<HatManEntity>> {
	private static final Identifier TEXTURE = ShadowOfIsraphel.id("textures/entity/hat_man.png");

	public HatManRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
	}

	@Override
	public Identifier getTexture(HatManEntity entity) {
		return TEXTURE;
	}
}
