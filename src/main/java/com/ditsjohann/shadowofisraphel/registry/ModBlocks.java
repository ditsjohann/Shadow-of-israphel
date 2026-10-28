package com.ditsjohann.shadowofisraphel.registry;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {
	public static final Block CURSED_SANDSTONE = register("cursed_sandstone",
			new Block(AbstractBlock.Settings.copy(Blocks.SANDSTONE).mapColor(MapColor.DARK_RED).strength(1.2f).requiresTool()));

	private ModBlocks() {
	}

	private static Block register(String name, Block block) {
		Registry.register(Registries.ITEM, ShadowOfIsraphel.id(name), new BlockItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, ShadowOfIsraphel.id(name), block);
	}

	public static void register() {
		// Static initialisation registers the blocks.
	}
}
