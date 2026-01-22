package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Rabbit.class)
public interface RabbitAccessor {

    @Accessor("jumpTicks")
    int getJumpTicks();

    @Accessor("jumpTicks")
    void setJumpTicks(int value);

    @Accessor("jumpDuration")
    int getJumpDuration();

    @Accessor("jumpDuration")
    void setJumpDuration(int value);

    @Accessor("wasOnGround")
    boolean getWasOnGround();

    @Accessor("wasOnGround")
    void setWasOnGround(boolean value);
}
