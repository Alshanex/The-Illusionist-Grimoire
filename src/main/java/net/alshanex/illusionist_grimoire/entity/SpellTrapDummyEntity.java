package net.alshanex.illusionist_grimoire.entity;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpellTrapDummyEntity extends AbstractSpellCastingMob {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID ownerUuid;

    private int lifeTicks = 20;

    public SpellTrapDummyEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setNoGravity(true);
        this.setSilent(true);
        this.setInvisible(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSpellCastingMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // No animations needed for dummy entity
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount > this.lifeTicks) {
            this.discard();
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void registerGoals() {

    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.ownerUuid = uuid;
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.ownerUuid != null && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(this.ownerUuid);
            if (player != null) {
                return player;
            }
        }
        return null;
    }

    // Alliance checking - make this entity allied to its owner
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity == null) {
            return false;
        }

        // Allied to the owner
        if (this.ownerUuid != null && entity.getUUID().equals(this.ownerUuid)) {
            return true;
        }

        // Allied to anyone the owner is allied to
        LivingEntity owner = this.getOwner();
        if (owner != null && entity instanceof LivingEntity livingEntity) {
            return owner.isAlliedTo(livingEntity);
        }

        return super.isAlliedTo(entity);
    }

    public void setLifeTicks(int lifeTicks){
        this.lifeTicks = lifeTicks;
    }

    @Override
    public boolean isNoAi() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean shouldDropLoot() {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // Apply attribute from holder
    public void applyAttributeValue(Holder<Attribute> attributeHolder, double value) {
        var attributeInstance = this.getAttribute(attributeHolder);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(value);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        }
        tag.putInt("lifeTicks", this.lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            this.ownerUuid = tag.getUUID("Owner");
        }
        if(tag.contains("lifeTicks")){
            this.tickCount = tag.getInt("lifeTicks");
        }
    }
}
