package com.ditsjohann.shadowofisraphel.client;

import com.ditsjohann.shadowofisraphel.client.render.HatManRenderer;
import com.ditsjohann.shadowofisraphel.client.render.IsraphelRenderer;
import com.ditsjohann.shadowofisraphel.client.render.SandMonsterRenderer;
import com.ditsjohann.shadowofisraphel.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ShadowOfIsraphelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.ISRAPHEL, IsraphelRenderer::new);
		EntityRendererRegistry.register(ModEntities.SAND_MONSTER, SandMonsterRenderer::new);
		EntityRendererRegistry.register(ModEntities.HAT_MAN, HatManRenderer::new);
	}
}
