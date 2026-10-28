package com.ditsjohann.shadowofisraphel;

import com.ditsjohann.shadowofisraphel.registry.ModBlocks;
import com.ditsjohann.shadowofisraphel.registry.ModEntities;
import com.ditsjohann.shadowofisraphel.registry.ModItemGroups;
import com.ditsjohann.shadowofisraphel.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShadowOfIsraphel implements ModInitializer {
	public static final String MOD_ID = "shadowofisraphel";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModItems.register();
		ModEntities.register();
		ModItemGroups.register();
		LOGGER.info("The shadow of Israphel falls across the land...");
	}
}
