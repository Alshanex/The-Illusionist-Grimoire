package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.data.IGClientSquishData;
import net.alshanex.illusionist_grimoire.data.SquishData;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SquishDimensionsMixin {

    @Inject(method = "getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",
            at = @At("RETURN"),
            cancellable = true)
    private void modifySquishDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        SquishData squishData;
        if (entity.level().isClientSide()) {
            // Client side - use the synced data from packets
            squishData = IGClientSquishData.getSquishData(entity);
        } else {
            // Server side - use the attachment data
            squishData = SquishData.getSquishData(entity);
        }
        if (squishData == null || !squishData.isSquished()) {
            return;
        }

        // Get current dimensions
        EntityDimensions originalDimensions = cir.getReturnValue();
        float originalWidth = originalDimensions.width();
        float originalHeight = originalDimensions.height();

        // Get the scale for each axis
        float[] scales = squishData.getScales();

        // Calculate new dimensions based on squish
        float newWidth = originalWidth;
        float newHeight = originalHeight;

        int squishAxis = squishData.getSquishAxis();

        // Modify dimensions based on which axis is squished
        if (squishAxis == 1) { // Y-axis (height)
            newHeight = originalHeight * scales[1];
        } else if (squishAxis == 0 || squishAxis == 2) { // X or Z axis (width)
            // For horizontal squishing, reduce width
            newWidth = originalWidth * Math.min(scales[0], scales[2]);
        }

        // Ensure minimum dimensions (can't be smaller than 2 pixels)
        newWidth = Math.max(SquishData.MIN_THICKNESS, newWidth);
        newHeight = Math.max(SquishData.MIN_THICKNESS, newHeight);

        // Set the new dimensions
        EntityDimensions newDimensions = EntityDimensions.scalable(newWidth, newHeight);
        cir.setReturnValue(newDimensions);
    }
}
