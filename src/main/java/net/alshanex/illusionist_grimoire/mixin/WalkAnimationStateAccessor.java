package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("position")
    float getPosition();

    @Accessor("position")
    void setPosition(float position);

    @Accessor("speedOld")
    float getSpeedOld();

    @Accessor("speedOld")
    void setSpeedOld(float speedOld);
}
