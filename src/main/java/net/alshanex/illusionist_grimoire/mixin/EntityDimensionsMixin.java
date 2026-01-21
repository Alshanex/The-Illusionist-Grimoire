package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.util.DimensionsRefresher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDimensionsMixin implements DimensionsRefresher {

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    public abstract Pose getPose();

    @Shadow
    protected abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AABB boundingBox);

    @Shadow
    public abstract float getEyeHeight();

    @Shadow
    private float eyeHeight;

    @Shadow
    protected boolean firstTick;

    @Inject(method = "getBbWidth", at = @At("HEAD"), cancellable = true)
    private void getWidth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
            var disguiseData = DisguiseData.getDisguiseData(player);
            LivingEntity mob = disguiseData.getMobDisguiseEntity();
            if (mob != null && !disguiseData.isDisguisedAsPlayer()) {
                cir.setReturnValue(mob.getBbWidth());
            }
        }
    }

    @Inject(method = "getBbHeight", at = @At("HEAD"), cancellable = true)
    private void getHeight(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
            var disguiseData = DisguiseData.getDisguiseData(player);
            LivingEntity mob = disguiseData.getMobDisguiseEntity();
            if (mob != null && !disguiseData.isDisguisedAsPlayer()) {
                cir.setReturnValue(mob.getBbHeight());
            }
        }
    }

    @Inject(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;)F", at = @At("HEAD"), cancellable = true)
    private void getStandingEyeHeight(Pose pose, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
            var disguiseData = DisguiseData.getDisguiseData(player);
            LivingEntity mob = disguiseData.getMobDisguiseEntity();
            if (mob != null && !disguiseData.isDisguisedAsPlayer()) {
                cir.setReturnValue(mob.getEyeHeight(pose));
            }
        }
    }

    @Override
    public void illusionist_refreshDimensions() {
        EntityDimensions currentDimensions = this.dimensions;
        Pose entityPose = this.getPose();
        EntityDimensions newDimensions = this.getDimensions(entityPose);

        this.dimensions = newDimensions;
        this.eyeHeight = this.getEyeHeight();

        AABB box = this.getBoundingBox();
        double width = newDimensions.width();
        double height = newDimensions.height();

        // Recalculate bounding box centered on entity position
        double halfWidth = width / 2.0D;
        this.setBoundingBox(new AABB(
                box.minX,
                box.minY,
                box.minZ,
                box.minX + width,
                box.minY + height,
                box.minZ + width
        ));

        // Adjust position if not first tick to prevent clipping
        if (!this.firstTick && currentDimensions != null) {
            float widthDiff = currentDimensions.width() - newDimensions.width();
            if (widthDiff != 0) {
                ((Entity)(Object)this).setPos(
                        ((Entity)(Object)this).getX(),
                        ((Entity)(Object)this).getY(),
                        ((Entity)(Object)this).getZ()
                );
            }
        }
    }

    @Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
    private void checkFireImmunity(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && player.hasEffect(IGEffectRegistry.DISGUISED)) {
            var disguiseData = DisguiseData.getDisguiseData(player);
            LivingEntity mob = disguiseData.getMobDisguiseEntity();
            if (mob != null && !disguiseData.isDisguisedAsPlayer()) {
                cir.setReturnValue(mob.fireImmune());
            }
        }
    }
}
