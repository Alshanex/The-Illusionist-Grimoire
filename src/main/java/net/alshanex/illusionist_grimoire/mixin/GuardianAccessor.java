package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Guardian.class)
public interface GuardianAccessor {

    @Accessor("clientSideTailAnimation")
    float getClientSideTailAnimation();

    @Accessor("clientSideTailAnimation")
    void setClientSideTailAnimation(float value);

    @Accessor("clientSideTailAnimationO")
    float getClientSideTailAnimationO();

    @Accessor("clientSideTailAnimationO")
    void setClientSideTailAnimationO(float value);

    @Accessor("clientSideTailAnimationSpeed")
    float getClientSideTailAnimationSpeed();

    @Accessor("clientSideTailAnimationSpeed")
    void setClientSideTailAnimationSpeed(float value);

    @Accessor("clientSideSpikesAnimation")
    float getClientSideSpikesAnimation();

    @Accessor("clientSideSpikesAnimation")
    void setClientSideSpikesAnimation(float value);

    @Accessor("clientSideSpikesAnimationO")
    float getClientSideSpikesAnimationO();

    @Accessor("clientSideSpikesAnimationO")
    void setClientSideSpikesAnimationO(float value);

    @Invoker("setMoving")
    void invokeSetMoving(boolean moving);
}
