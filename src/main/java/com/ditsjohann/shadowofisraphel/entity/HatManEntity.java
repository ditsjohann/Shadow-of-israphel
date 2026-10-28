package com.ditsjohann.shadowofisraphel.entity;

import com.ditsjohann.shadowofisraphel.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Hat Man: a harmless wanderer with a great many opinions about hats.
 * Talk to him and he will share some wisdom; the first time, he also hands
 * over his spare hat.
 */
public class HatManEntity extends PathAwareEntity {
	private static final int LINE_COUNT = 8;

	private boolean hasSpareHat = true;
	private int talkCooldown;

	public HatManEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
		this.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.HAT_MAN_HAT));
		this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
	}

	public static DefaultAttributeContainer.Builder createHatManAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.3));
		this.goalSelector.add(2, new FleeEntityGoal<>(this, SandMonsterEntity.class, 10.0f, 0.8, 1.1));
		this.goalSelector.add(2, new FleeEntityGoal<>(this, IsraphelEntity.class, 16.0f, 0.8, 1.2));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}

		if (!this.getWorld().isClient && this.talkCooldown <= 0) {
			this.talkCooldown = 20;
			this.getLookControl().lookAt(player, 30.0f, 30.0f);

			if (this.hasSpareHat) {
				this.hasSpareHat = false;
				player.sendMessage(prefixed(Text.translatable("entity.shadowofisraphel.hat_man.gift")), false);
				ItemStack hat = new ItemStack(ModItems.HAT_MAN_HAT);
				if (!player.getInventory().insertStack(hat)) {
					player.dropItem(hat, false);
				}
			} else {
				int line = this.random.nextInt(LINE_COUNT);
				player.sendMessage(prefixed(Text.translatable("entity.shadowofisraphel.hat_man.line" + line)), false);
			}
			this.playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0f, 0.8f);
		}
		return ActionResult.success(this.getWorld().isClient);
	}

	private Text prefixed(Text line) {
		return Text.empty()
				.append(Text.literal("<").formatted(Formatting.GRAY))
				.append(this.getDisplayName())
				.append(Text.literal("> ").formatted(Formatting.GRAY))
				.append(line);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (this.talkCooldown > 0) {
			this.talkCooldown--;
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("HasSpareHat", this.hasSpareHat);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("HasSpareHat")) {
			this.hasSpareHat = nbt.getBoolean("HasSpareHat");
		}
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_VILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_VILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_VILLAGER_DEATH;
	}
}
