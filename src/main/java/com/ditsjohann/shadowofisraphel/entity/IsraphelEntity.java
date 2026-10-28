package com.ditsjohann.shadowofisraphel.entity;

import com.ditsjohann.shadowofisraphel.registry.ModEntities;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Israphel, the shadow himself. A boss that stalks the player, strikes with
 * bolts of shadow, blinks through the dark and raises sand monsters to fight
 * for him. Below half health he grows desperate and everything speeds up.
 */
public class IsraphelEntity extends HostileEntity {
	private static final int SHADOW_BOLT_CHARGE_TICKS = 20;
	private static final int MAX_MINIONS = 6;

	private final ServerBossBar bossBar = (ServerBossBar) new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10)
			.setDarkenSky(true)
			.setThickenFog(true);

	private int shadowBoltCooldown = 60;
	private int shadowBoltCharge = -1;
	private int teleportCooldown = 100;
	private int summonCooldown = 200;
	private int darknessCooldown = 120;
	private boolean enraged;

	public IsraphelEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 150;
	}

	public static DefaultAttributeContainer.Builder createIsraphelAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_ARMOR, 8.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));

		this.targetSelector.add(1, new RevengeGoal(this, SandMonsterEntity.class));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

		if (!this.enraged && this.getHealth() < this.getMaxHealth() / 2.0f) {
			this.enrage();
		}

		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) {
			this.shadowBoltCharge = -1;
			return;
		}

		this.tickShadowBolt(target);
		this.tickTeleport(target);
		this.tickSummon(target);
		this.tickDarkness();
	}

	private int cooldown(int base) {
		return this.enraged ? base / 2 : base;
	}

	private void enrage() {
		this.enraged = true;
		this.bossBar.setColor(BossBar.Color.PURPLE);
		this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE, 0, false, false));
		this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 0, false, false));
		this.taunt("entity.shadowofisraphel.israphel.enraged");
	}

	private void tickShadowBolt(LivingEntity target) {
		ServerWorld world = (ServerWorld) this.getWorld();

		if (this.shadowBoltCharge >= 0) {
			this.shadowBoltCharge++;
			world.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getBodyY(0.6), this.getZ(), 6, 0.4, 0.6, 0.4, 0.05);
			if (this.shadowBoltCharge >= SHADOW_BOLT_CHARGE_TICKS) {
				this.shadowBoltCharge = -1;
				this.shadowBoltCooldown = this.cooldown(80);
				if (this.canSee(target)) {
					this.fireShadowBolt(world, target);
				}
			}
			return;
		}

		if (--this.shadowBoltCooldown <= 0 && this.squaredDistanceTo(target) > 9.0 && this.canSee(target)) {
			this.shadowBoltCharge = 0;
			this.playSound(SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK, 1.5f, 0.6f);
		}
	}

	private void fireShadowBolt(ServerWorld world, LivingEntity target) {
		Vec3d from = this.getEyePos();
		Vec3d to = target.getPos().add(0.0, target.getHeight() * 0.5, 0.0);
		Vec3d step = to.subtract(from);
		int points = MathHelper.ceil(step.length() * 3.0);
		for (int i = 0; i <= points; i++) {
			Vec3d p = from.add(step.multiply(i / (double) points));
			world.spawnParticles(ParticleTypes.SQUID_INK, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
			world.spawnParticles(ParticleTypes.CRIMSON_SPORE, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
		}

		this.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1.5f, 0.5f);
		if (target.damage(this.getDamageSources().indirectMagic(this, this), this.enraged ? 9.0f : 6.0f)) {
			target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, this.enraged ? 1 : 0), this);
		}
	}

	private void tickTeleport(LivingEntity target) {
		if (--this.teleportCooldown > 0) {
			return;
		}
		this.teleportCooldown = this.cooldown(160) + this.random.nextInt(60);

		// Step out of the shadows right behind the target.
		Vec3d behind = target.getPos().subtract(target.getRotationVector().multiply(1.0, 0.0, 1.0).normalize().multiply(2.5));
		if (!this.teleportTo(behind.x, target.getY(), behind.z)) {
			this.teleportRandomly();
		}
	}

	private void tickSummon(LivingEntity target) {
		if (--this.summonCooldown > 0) {
			return;
		}
		this.summonCooldown = this.cooldown(400);

		ServerWorld world = (ServerWorld) this.getWorld();
		List<SandMonsterEntity> minions = world.getEntitiesByClass(SandMonsterEntity.class, this.getBoundingBox().expand(24.0), SandMonsterEntity::isAlive);
		int toSummon = Math.min(this.enraged ? 3 : 2, MAX_MINIONS - minions.size());
		if (toSummon <= 0) {
			return;
		}

		this.playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 0.7f);
		for (int i = 0; i < toSummon; i++) {
			SandMonsterEntity minion = ModEntities.SAND_MONSTER.create(world);
			if (minion == null) {
				continue;
			}
			BlockPos pos = this.getBlockPos().add(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3);
			minion.refreshPositionAndAngles(pos, this.random.nextFloat() * 360.0f, 0.0f);
			minion.initialize(world, world.getLocalDifficulty(pos), SpawnReason.MOB_SUMMONED, null);
			minion.setTarget(target);
			world.spawnEntity(minion);
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, minion.getX(), minion.getY() + 1.0, minion.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
		}
	}

	private void tickDarkness() {
		if (--this.darknessCooldown > 0) {
			return;
		}
		this.darknessCooldown = this.cooldown(240);

		for (PlayerEntity player : this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(24.0), p -> !p.isSpectator() && !p.isCreative())) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 160, 0), this);
		}
	}

	private boolean teleportRandomly() {
		double x = this.getX() + (this.random.nextDouble() - 0.5) * 24.0;
		double y = this.getY() + (this.random.nextInt(16) - 8);
		double z = this.getZ() + (this.random.nextDouble() - 0.5) * 24.0;
		return this.teleportTo(x, y, z);
	}

	private boolean teleportTo(double x, double y, double z) {
		World world = this.getWorld();
		BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);
		while (mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).blocksMovement()) {
			mutable.move(Direction.DOWN);
		}

		BlockState ground = world.getBlockState(mutable);
		if (!ground.blocksMovement() || !ground.getFluidState().isEmpty()) {
			return false;
		}

		double oldX = this.getX();
		double oldY = this.getY();
		double oldZ = this.getZ();
		boolean teleported = this.teleport(x, mutable.getY() + 1.0, z, true);
		if (teleported) {
			if (world instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, oldX, oldY + 1.0, oldZ, 30, 0.3, 0.8, 0.3, 0.02);
			}
			world.playSound(null, oldX, oldY, oldZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0f, 0.5f);
			this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
		}
		return teleported;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		}

		// Arrows and other projectiles often pass straight through his shadow.
		if (source.isIn(DamageTypeTags.IS_PROJECTILE) && !this.getWorld().isClient && this.random.nextFloat() < 0.4f) {
			this.teleportRandomly();
			return false;
		}

		boolean damaged = super.damage(source, amount);
		if (damaged && !this.getWorld().isClient && this.random.nextFloat() < 0.15f) {
			this.teleportRandomly();
		}
		return damaged;
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (this.getWorld().isClient) {
			for (int i = 0; i < 2; i++) {
				this.getWorld().addParticle(ParticleTypes.SMOKE,
						this.getParticleX(0.5), this.getRandomBodyY() - 0.25, this.getParticleZ(0.5),
						0.0, 0.02, 0.0);
			}
			if (this.random.nextInt(4) == 0) {
				this.getWorld().addParticle(ParticleTypes.PORTAL,
						this.getParticleX(0.5), this.getRandomBodyY() - 0.25, this.getParticleZ(0.5),
						(this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
			}
		}
	}

	private void taunt(String key) {
		Text message = Text.translatable(key).formatted(Formatting.DARK_RED, Formatting.ITALIC);
		for (PlayerEntity player : this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(48.0), p -> true)) {
			player.sendMessage(message, false);
		}
	}

	@Override
	public void onDeath(DamageSource damageSource) {
		super.onDeath(damageSource);
		if (!this.getWorld().isClient) {
			this.taunt("entity.shadowofisraphel.israphel.defeated");
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		this.bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		this.bossBar.removePlayer(player);
	}

	@Override
	public void setCustomName(Text name) {
		super.setCustomName(name);
		this.bossBar.setName(this.getDisplayName());
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("Enraged", this.enraged);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.enraged = nbt.getBoolean("Enraged");
		if (this.enraged) {
			this.bossBar.setColor(BossBar.Color.PURPLE);
		}
		if (this.hasCustomName()) {
			this.bossBar.setName(this.getDisplayName());
		}
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean canUsePortals(boolean allowVehicles) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_EVOKER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_EVOKER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WITHER_DEATH;
	}

	@Override
	public float getSoundPitch() {
		return 0.6f;
	}
}
