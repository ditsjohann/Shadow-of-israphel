package com.ditsjohann.shadowofisraphel.registry;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
	public static final ItemGroup SHADOW_OF_ISRAPHEL = Registry.register(Registries.ITEM_GROUP, ShadowOfIsraphel.id("main"),
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(ModItems.CURSED_IDOL))
					.displayName(Text.translatable("itemGroup.shadowofisraphel.main"))
					.entries((context, entries) -> {
						entries.add(ModItems.CURSED_IDOL);
						entries.add(ModItems.SHADOW_SHARD);
						entries.add(ModItems.SHADOW_BLADE);
						entries.add(ModItems.HAT_MAN_HAT);
						entries.add(ModItems.CURSED_SAND);
						entries.add(ModBlocks.CURSED_SANDSTONE);
						entries.add(ModItems.JAFFA_CAKE);
						entries.add(ModItems.HONEYDEWS_MEAD);
						entries.add(ModEntities.ISRAPHEL_SPAWN_EGG);
						entries.add(ModEntities.SAND_MONSTER_SPAWN_EGG);
						entries.add(ModEntities.HAT_MAN_SPAWN_EGG);
					})
					.build());

	private ModItemGroups() {
	}

	public static void register() {
		// Static initialisation registers the item group.
	}
}
