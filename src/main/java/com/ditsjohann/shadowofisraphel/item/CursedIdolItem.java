package com.ditsjohann.shadowofisraphel.item;

import com.ditsjohann.shadowofisraphel.entity.IsraphelEntity;
import com.ditsjohann.shadowofisraphel.registry.ModEntities;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Placed on the ground at night, the idol calls Israphel into the world.
 */
public class CursedIdolItem extends Item {
	private static final double ONE_ISRAPHEL_RADIUS = 64.0;

	public CursedIdolItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		if (world.getRegistryKey() == World.OVERWORLD && world.isDay()) {
			if (player != null) {
				player.sendMessage(Text.translatable("item.shadowofisraphel.cursed_idol.daytime").formatted(Formatting.GRAY), true);
			}
			return ActionResult.FAIL;
		}

		BlockPos pos = context.getBlockPos().offset(context.getSide());
		Box searchBox = new Box(pos).expand(ONE_ISRAPHEL_RADIUS);
		if (!serverWorld.getEntitiesByClass(IsraphelEntity.class, searchBox, IsraphelEntity::isAlive).isEmpty()) {
			if (player != null) {
				player.sendMessage(Text.translatable("item.shadowofisraphel.cursed_idol.already_here").formatted(Formatting.DARK_RED), true);
			}
			return ActionResult.FAIL;
		}

		IsraphelEntity israphel = ModEntities.ISRAPHEL.create(serverWorld);
		if (israphel == null) {
			return ActionResult.FAIL;
		}

		israphel.refreshPositionAndAngles(pos, player != null ? player.getYaw() + 180.0f : 0.0f, 0.0f);
		israphel.initialize(serverWorld, serverWorld.getLocalDifficulty(pos), SpawnReason.MOB_SUMMONED, null);
		serverWorld.spawnEntity(israphel);

		LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
		if (lightning != null) {
			lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
			lightning.setCosmetic(true);
			serverWorld.spawnEntity(lightning);
		}
		serverWorld.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 0.6f);

		for (ServerPlayerEntity nearby : serverWorld.getPlayers()) {
			if (nearby.squaredDistanceTo(Vec3d.ofCenter(pos)) < 128.0 * 128.0) {
				nearby.sendMessage(Text.translatable("entity.shadowofisraphel.israphel.summoned").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
			}
		}

		if (player == null || !player.getAbilities().creativeMode) {
			context.getStack().decrement(1);
		}
		return ActionResult.CONSUME;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.shadowofisraphel.cursed_idol.tooltip").formatted(Formatting.DARK_RED, Formatting.ITALIC));
	}
}
