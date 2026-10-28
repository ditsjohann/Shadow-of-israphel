package com.ditsjohann.shadowofisraphel.registry;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import com.ditsjohann.shadowofisraphel.item.CursedIdolItem;
import com.ditsjohann.shadowofisraphel.item.HatItem;
import com.ditsjohann.shadowofisraphel.item.MeadItem;
import com.ditsjohann.shadowofisraphel.item.ModToolMaterials;
import com.ditsjohann.shadowofisraphel.item.ShadowBladeItem;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item JAFFA_CAKE = register("jaffa_cake", new Item(new Item.Settings()
			.food(new FoodComponent.Builder().nutrition(3).saturationModifier(0.4f).snack().build())));

	public static final Item HONEYDEWS_MEAD = register("honeydews_mead", new MeadItem(new Item.Settings()
			.maxCount(16)
			.food(new FoodComponent.Builder()
					.nutrition(2)
					.saturationModifier(0.2f)
					.alwaysEdible()
					.statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 60, 0), 1.0f)
					.statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * 8, 0), 0.6f)
					.build())));

	public static final Item CURSED_SAND = register("cursed_sand", new Item(new Item.Settings()));

	public static final Item SHADOW_SHARD = register("shadow_shard", new Item(new Item.Settings()
			.rarity(Rarity.RARE)
			.fireproof()));

	public static final Item CURSED_IDOL = register("cursed_idol", new CursedIdolItem(new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.UNCOMMON)));

	public static final Item SHADOW_BLADE = register("shadow_blade", new ShadowBladeItem(ModToolMaterials.SHADOW, new Item.Settings()
			.rarity(Rarity.EPIC)
			.fireproof()
			.attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.SHADOW, 3, -2.4f))));

	public static final Item HAT_MAN_HAT = register("hat_man_hat", new HatItem(new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.UNCOMMON)));

	private ModItems() {
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, ShadowOfIsraphel.id(name), item);
	}

	public static void register() {
		// Static initialisation registers the items.
	}
}
