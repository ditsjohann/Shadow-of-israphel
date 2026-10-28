package com.ditsjohann.shadowofisraphel.registry;

import com.ditsjohann.shadowofisraphel.ShadowOfIsraphel;
import com.ditsjohann.shadowofisraphel.entity.HatManEntity;
import com.ditsjohann.shadowofisraphel.entity.IsraphelEntity;
import com.ditsjohann.shadowofisraphel.entity.SandMonsterEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
	public static final EntityType<IsraphelEntity> ISRAPHEL = register("israphel",
			EntityType.Builder.create(IsraphelEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6f, 1.95f)
					.eyeHeight(1.62f)
					.makeFireImmune()
					.maxTrackingRange(10));

	public static final EntityType<SandMonsterEntity> SAND_MONSTER = register("sand_monster",
			EntityType.Builder.create(SandMonsterEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6f, 1.95f)
					.eyeHeight(1.74f)
					.maxTrackingRange(8));

	public static final EntityType<HatManEntity> HAT_MAN = register("hat_man",
			EntityType.Builder.create(HatManEntity::new, SpawnGroup.CREATURE)
					.dimensions(0.6f, 1.95f)
					.eyeHeight(1.62f)
					.maxTrackingRange(10));

	public static final Item ISRAPHEL_SPAWN_EGG = registerEgg("israphel_spawn_egg", ISRAPHEL, 0x1A1022, 0xC4161C);
	public static final Item SAND_MONSTER_SPAWN_EGG = registerEgg("sand_monster_spawn_egg", SAND_MONSTER, 0xDBCB8C, 0x6E5A2E);
	public static final Item HAT_MAN_SPAWN_EGG = registerEgg("hat_man_spawn_egg", HAT_MAN, 0x2B2B33, 0x8E1B1B);

	private ModEntities() {
	}

	private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		return Registry.register(Registries.ENTITY_TYPE, ShadowOfIsraphel.id(name), builder.build(name));
	}

	private static Item registerEgg(String name, EntityType<? extends MobEntity> type, int primary, int secondary) {
		return Registry.register(Registries.ITEM, ShadowOfIsraphel.id(name), new SpawnEggItem(type, primary, secondary, new Item.Settings()));
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(ISRAPHEL, IsraphelEntity.createIsraphelAttributes());
		FabricDefaultAttributeRegistry.register(SAND_MONSTER, SandMonsterEntity.createSandMonsterAttributes());
		FabricDefaultAttributeRegistry.register(HAT_MAN, HatManEntity.createHatManAttributes());

		SpawnRestriction.register(SAND_MONSTER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(HAT_MAN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);

		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.ERODED_BADLANDS, BiomeKeys.WOODED_BADLANDS),
				SpawnGroup.MONSTER, SAND_MONSTER, 80, 1, 3);
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.MEADOW, BiomeKeys.FOREST),
				SpawnGroup.CREATURE, HAT_MAN, 1, 1, 1);
	}
}
