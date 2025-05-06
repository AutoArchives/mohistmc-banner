package com.mohistmc.banner.mixin.world.entity;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.injection.world.entity.InjectionEntity;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.BlockUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.PositionImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftPortalEvent;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Banner - TODO fix patches
@Mixin(Entity.class)
public abstract class MixinEntity implements Nameable, EntityAccess, CommandSource, InjectionEntity {

    @Shadow
    @Final
    private static AtomicInteger ENTITY_COUNTER;
    @Shadow
    private Level level;
    @Shadow
    @Final
    public static int TOTAL_AIR_SUPPLY;
    @Shadow
    private float yRot;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getZ();

    @Shadow
    protected abstract void handleNetherPortal();

    @Shadow
    public abstract void setSecondsOnFire(int seconds);

    @Shadow
    protected abstract SoundEvent getSwimSound();

    @Shadow
    protected abstract SoundEvent getSwimSplashSound();

    @Shadow
    protected abstract SoundEvent getSwimHighSpeedSplashSound();

    @Shadow
    public abstract boolean isPushable();

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract String getScoreboardName();

    @Shadow
    private float xRot;
    @Shadow
    private int remainingFireTicks;
    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract float getXRot();

    @Shadow
    public int tickCount;

    @Shadow
    public abstract int getMaxAirSupply();

    @Shadow
    public abstract void setInvisible(boolean invisible);

    @Shadow
    @Nullable
    private Entity vehicle;

    @Shadow
    public abstract void gameEvent(net.minecraft.world.level.gameevent.GameEvent event, @Nullable Entity entity);

    @Shadow
    public ImmutableList<Entity> passengers;

    @Shadow
    @Final
    private static EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID;

    @Shadow
    public abstract SynchedEntityData getEntityData();

    @Shadow
    public abstract int getAirSupply();

    @Shadow
    @Final
    protected SynchedEntityData entityData;

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract boolean fireImmune();

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    public abstract DamageSources damageSources();

    @Shadow
    protected abstract ListTag newDoubleList(double... ds);

    @Shadow
    public abstract boolean teleportTo(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot);

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract boolean isRemoved();

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract void moveTo(Vec3 vec);

    @Shadow
    public abstract void moveTo(double x, double y, double z, float yRot, float xRot);

    @Shadow
    @Nullable
    public abstract Entity changeDimension(ServerLevel destination);

    @Shadow
    public abstract boolean getSharedFlag(int p_20292_);

    @Shadow
    private AABB bb;

    @Unique
    private CraftEntity bukkitEntity;
    @Unique
    public final org.spigotmc.ActivationRange.ActivationType activationType =
            org.spigotmc.ActivationRange.initializeEntityActivationType((Entity) (Object) this);
    @Unique
    public boolean defaultActivationState;
    @Unique
    public long activatedTick = Integer.MIN_VALUE;
    @Unique
    public boolean generation;
    @Unique
    public boolean persist = true;
    @Unique
    public boolean visibleByDefault = true;
    @Unique
    public boolean valid;
    @Unique
    public int maxAirTicks = getDefaultMaxAirSupply(); // CraftBukkit - SPIGOT-6907: re-implement LivingEntity#setMaximumAir()
    @Unique
    public org.bukkit.projectiles.ProjectileSource projectileSource; // For projectiles only
    @Unique
    public boolean lastDamageCancelled; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
    @Unique
    public boolean persistentInvisibility = false;
    @Unique
    public BlockPos lastLavaContact;
    @Unique
    private static final int CURRENT_LEVEL = 2;
    @Unique
    @javax.annotation.Nullable
    private org.bukkit.util.Vector origin;
    @Unique
    @javax.annotation.Nullable
    private UUID originWorld;

    @Override
    public void setOrigin(@NotNull Location location) {
        this.origin = location.toVector();
        this.originWorld = location.getWorld().getUID();
    }

    @Nullable
    @Override
    public Vector getOriginVector() {
        return this.origin != null ? this.origin.clone() : null;
    }

    @Nullable
    @Override
    public UUID getOriginWorld() {
        return this.originWorld;
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = CraftEntity.getEntity(level.getCraftServer(), ((Entity) (Object) this));
        }
        return bukkitEntity;
    }

    @Override
    public int getDefaultMaxAirSupply() {
        return TOTAL_AIR_SUPPLY;
    }

    @Override
    public float getBukkitYaw() {
        return this.yRot;
    }

    @Override
    public boolean isChunkLoaded() {
        return level.hasChunk((int) Math.floor(this.getX()) >> 4, (int) Math.floor(this.getZ()) >> 4);
    }

    @Override
    public void postTick() {
        // No clean way to break out of ticking once the entity has been copied to a new world, so instead we move the portalling later in the tick cycle
        if (!(((Entity) (Object) this) instanceof ServerPlayer)) {
            this.handleNetherPortal();
        }
    }

    @Inject(method = "handleNetherPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"))
    public void banner$changeDimension(CallbackInfo ci) {
        if ((Entity)(Object)this instanceof ServerPlayer serverPlayer) {
            serverPlayer.pushChangeDimensionCause(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
        }
    }

    public AtomicBoolean callEntityCombustEvent = new AtomicBoolean(true);

    @Override
    public void setSecondsOnFire(int i, boolean callEvent) {
        this.callEntityCombustEvent.set(callEvent);
        setSecondsOnFire(i);
    }

    @Inject(method = "setSecondsOnFire", at = @At("HEAD"))
    private void banner$setSecondsOnFire(int seconds, CallbackInfo ci) {
        if (this.callEntityCombustEvent.getAndSet(true)) {
            EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), seconds);
            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            seconds = event.getDuration();
        }
    }

    @Shadow
    public abstract void remove(RemovalReason removalReason);

    @Override
    public SoundEvent getSwimSound0() {
        return getSwimSound();
    }

    @Override
    public SoundEvent getSwimSplashSound0() {
        return getSwimSplashSound();
    }

    @Override
    public SoundEvent getSwimHighSpeedSplashSound0() {
        return getSwimHighSpeedSplashSound();
    }

    @Override
    public boolean canCollideWithBukkit(Entity entity) {
        return isPushable();
    }

    @Inject(method = "getMaxAirSupply", cancellable = true, at = @At("RETURN"))
    private void banner$useBukkitMaxAir(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.maxAirTicks);
    }

    @Inject(method = "setPose", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V"))
    public void banner$setPose$EntityPoseChangeEvent(Pose pose, CallbackInfo ci) {
        if (pose == this.getPose()) {
            ci.cancel();
            return;
        }
        EntityPoseChangeEvent event = new EntityPoseChangeEvent(this.getBukkitEntity(), org.bukkit.entity.Pose.values()[pose.ordinal()]);
        if (this.valid) {
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;handleNetherPortal()V"))
    public void banner$baseTick$moveToPostTick(Entity entity) {
        if (entity instanceof ServerPlayer) this.handleNetherPortal();// CraftBukkit - // Moved up to postTick
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D", shift = At.Shift.AFTER))
    private void banner$setLava(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir, @Local BlockPos.MutableBlockPos mutableBlockPos) {
        if (fluidTag == FluidTags.LAVA) {
            lastLavaContact = mutableBlockPos.immutable();
        }
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/Entity;isInLava()Z"))
    private boolean banner$resetLava(Entity instance) {
        var ret = instance.isInLava();
        if (!ret) {
            this.lastLavaContact = null;
        }
        return ret;
    }

    @Redirect(method = "lavaHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    public void banner$setOnFireFromLava$bukkitEvent(Entity entity, int seconds) {
        var damager = (lastLavaContact == null) ? null : CraftBlock.at(level, lastLavaContact);
        CraftEventFactory.blockDamage = damager;
        if (entity instanceof LivingEntity && remainingFireTicks <= 0) {
            var damagee = this.getBukkitEntity();
            EntityCombustEvent combustEvent = new EntityCombustByBlockEvent(damager, damagee, 15);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled()) {
                this.setSecondsOnFire(combustEvent.getDuration());
            }
        } else {
            // This will be called every single tick the entity is in lava, so don't throw an event
            this.setSecondsOnFire(15);
        }
    }

    @Redirect(method = "lavaHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;lava()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource banner$resetBlockDamage(DamageSources instance) {
        var damager = (lastLavaContact == null) ? null : CraftBlock.at(level(), lastLavaContact);
        return instance.lava().bridge$directBlock(damager);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/nbt/CompoundTag;putUUID(Ljava/lang/String;Ljava/util/UUID;)V"))
    public void banner$writeWithoutTypeId$CraftBukkitNBT(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        compound.putLong("WorldUUIDLeast", this.level.getWorld().getUID().getLeastSignificantBits());
        compound.putLong("WorldUUIDMost", this.level.getWorld().getUID().getMostSignificantBits());
        compound.putInt("Bukkit.updateLevel", CURRENT_LEVEL);
        compound.putInt("Spigot.ticksLived", this.tickCount);
        if (!this.persist) {
            compound.putBoolean("Bukkit.persist", this.persist);
        }
        if (!this.visibleByDefault) {
            compound.putBoolean("Bukkit.visibleByDefault", this.visibleByDefault);
        }
        if (this.persistentInvisibility) {
            compound.putBoolean("Bukkit.invisible", this.persistentInvisibility);
        }
        if (maxAirTicks != getDefaultMaxAirSupply()) {
            compound.putInt("Bukkit.MaxAirSupply", getMaxAirSupply());
        }
    }

    @Unique
    private static boolean isLevelAtLeast(CompoundTag tag, int level) {
        return tag.contains("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
    }

    @Inject(method = "setInvisible", cancellable = true, at = @At("HEAD"))
    private void banner$preventVisible(boolean invisible, CallbackInfo ci) {
        if (this.persistentInvisibility) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void banner$entityDropItem(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir, ItemEntity itemEntity) {
        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) (itemEntity).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "setSwimming", cancellable = true, at = @At(value = "HEAD"))
    public void banner$setSwimming$EntityToggleSwimEvent(boolean flag, CallbackInfo ci) {
        // CraftBukkit start
        if (this.valid && this.isSwimming() != flag && (Object) this instanceof LivingEntity) {
            if (CraftEventFactory.callToggleSwimEvent((LivingEntity) (Object) this, flag).isCancelled()) {
                ci.cancel();
            }
        }
        // CraftBukkit end
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    public void banner$onStruckByLightning$EntityCombustByEntityEvent0(Entity entity, int seconds) {
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = entity.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        // CraftBukkit start - Call a combust event when lightning strikes
        EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
        pluginManager.callEvent(entityCombustEvent);
        if (!entityCombustEvent.isCancelled()) {
            this.setSecondsOnFire(entityCombustEvent.getDuration());
        }
        // CraftBukkit end
    }

    @Inject(method = "setAirSupply", cancellable = true, at = @At(value = "HEAD"))
    public void banner$setAir$EntityAirChangeEvent(int air, CallbackInfo ci) {
        // CraftBukkit start
        EntityAirChangeEvent event = new EntityAirChangeEvent(this.getBukkitEntity(), air);
        // Suppress during worldgen
        if (this.valid) {
            event.getEntity().getServer().getPluginManager().callEvent(event);
        }
        if (event.isCancelled() && this.getAirSupply() != -1) {
            ci.cancel();
            this.getEntityData().markDirty(DATA_AIR_SUPPLY_ID);
            return;
        }
        this.entityData.set(DATA_AIR_SUPPLY_ID, event.getAmount());
        // CraftBukkit end
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean banner$onStruckByLightning$EntityCombustByEntityEvent1(Entity instance, DamageSource source, float amount) {
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = instance.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (thisBukkitEntity instanceof Hanging) {
            HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging) thisBukkitEntity, stormBukkitEntity);
            pluginManager.callEvent(hangingEvent);

            if (hangingEvent.isCancelled()) {
                return false;
            }
        }

        if (this.fireImmune()) {
            return false;
        }
        if (!this.hurt((this.damageSources().lightningBolt()).bridge$customCausingEntity(instance), amount)) {
            return false;
        }
        return true;
    }

    @Inject(method = "setSharedFlag", at = @At("HEAD"),
            cancellable = true)
    private void banner$forwardHandle(int flag, boolean set, CallbackInfo ci) {
        if (BukkitSnapshotCaptures.banner$stopGlide()) {
            if (!(getSharedFlag(flag) && !CraftEventFactory.callToggleGlideEvent((LivingEntity) (Object) this, false).isCancelled())) {
                BukkitSnapshotCaptures.capturebanner$stopGlide(false);
                ci.cancel();
            }
        }
    }

    @Override
    public CraftPortalEvent callPortalEvent(Entity entity, ServerLevel exitWorldServer, PositionImpl exitPosition, PlayerTeleportEvent.TeleportCause cause, int searchRadius, int creationRadius) {
        CraftEntity bukkitEntity = entity.getBukkitEntity();
        Location enter = bukkitEntity.getLocation();
        Location exit = CraftLocation.toBukkit(exitPosition, exitWorldServer.getWorld());
        EntityPortalEvent event = new EntityPortalEvent(bukkitEntity, enter, exit, searchRadius);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null || !entity.isAlive()) {
            return null;
        }
        return new CraftPortalEvent(event);
    }

    @Unique
    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel serverWorld, BlockPos pos, boolean flag, WorldBorder worldborder, int searchRadius, boolean canCreatePortal, int createRadius) {
        return serverWorld.getPortalForcer().findPortalAround(pos, worldborder, searchRadius);
    }

    @Override
    public ActivationRange.ActivationType bridge$activationType() {
        return activationType;
    }

    @Override
    public long bridge$activatedTick() {
        return activatedTick;
    }

    @Override
    public void banner$setActivatedTick(long activatedTick) {
        this.activatedTick = activatedTick;
    }

    @Override
    public boolean bridge$defaultActivationState() {
        return defaultActivationState;
    }

    @Override
    public void banner$setDefaultActivationState(boolean state) {
        defaultActivationState = state;
    }

    @Override
    public boolean bridge$generation() {
        return generation;
    }

    @Override
    public void banner$setGeneration(boolean gen) {
        this.generation = gen;
    }

    @Override
    public boolean bridge$persist() {
        return persist;
    }

    @Override
    public void banner$setPersist(boolean persist) {
        this.persist = persist;
    }

    @Override
    public boolean bridge$visibleByDefault() {
        return visibleByDefault;
    }

    @Override
    public void banner$setVisibleByDefault(boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }

    @Override
    public boolean bridge$valid() {
        return valid;
    }

    @Override
    public void banner$setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int bridge$maxAirTicks() {
        return maxAirTicks;
    }

    @Override
    public void banner$setMaxAirTicks(int maxAirTicks) {
        this.maxAirTicks = maxAirTicks;
    }

    @Override
    public ProjectileSource bridge$projectileSource() {
        return projectileSource;
    }

    @Override
    public void banner$setProjectileSource(ProjectileSource projectileSource) {
        this.projectileSource = projectileSource;
    }

    @Override
    public boolean bridge$lastDamageCancelled() {
        return lastDamageCancelled;
    }

    @Override
    public void banner$setLastDamageCancelled(boolean lastDamageCancelled) {
        this.lastDamageCancelled = lastDamageCancelled;
    }

    @Override
    public boolean bridge$persistentInvisibility() {
        return persistentInvisibility;
    }

    @Override
    public void banner$setPersistentInvisibility(boolean persistentInvisibility) {
        this.persistentInvisibility = persistentInvisibility;
    }

    @Override
    public BlockPos bridge$lastLavaContact() {
        return lastLavaContact;
    }

    @Override
    public void banner$setLastLavaContact(BlockPos lastLavaContact) {
        this.lastLavaContact = lastLavaContact;
    }

    @Override
    public CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitEntity();
    }

    @Override
    public void banner$setBukkitEntity(CraftEntity bukkitEntity) {
        this.bukkitEntity = bukkitEntity;
    }

    @Unique
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static int nextEntityId() {
        return ENTITY_COUNTER.incrementAndGet();
    }
}
