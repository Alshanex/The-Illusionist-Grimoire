package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.entity.animal.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Squid.class)
public interface SquidAccessor {

    @Accessor("xBodyRot")
    float getXBodyRot();

    @Accessor("xBodyRot")
    void setXBodyRot(float xBodyRot);

    @Accessor("xBodyRotO")
    float getXBodyRotO();

    @Accessor("xBodyRotO")
    void setXBodyRotO(float xBodyRotO);

    @Accessor("zBodyRot")
    float getZBodyRot();

    @Accessor("zBodyRot")
    void setZBodyRot(float zBodyRot);

    @Accessor("zBodyRotO")
    float getZBodyRotO();

    @Accessor("zBodyRotO")
    void setZBodyRotO(float zBodyRotO);

    @Accessor("tentacleMovement")
    float getTentacleMovement();

    @Accessor("tentacleMovement")
    void setTentacleMovement(float tentacleMovement);

    @Accessor("oldTentacleMovement")
    float getOldTentacleMovement();

    @Accessor("oldTentacleMovement")
    void setOldTentacleMovement(float oldTentacleMovement);

    @Accessor("tentacleAngle")
    float getTentacleAngle();

    @Accessor("tentacleAngle")
    void setTentacleAngle(float tentacleAngle);

    @Accessor("oldTentacleAngle")
    float getOldTentacleAngle();

    @Accessor("oldTentacleAngle")
    void setOldTentacleAngle(float oldTentacleAngle);

    @Accessor("speed")
    float getSpeed();

    @Accessor("speed")
    void setSpeed(float speed);

    @Accessor("tentacleSpeed")
    float getTentacleSpeed();

    @Accessor("tentacleSpeed")
    void setTentacleSpeed(float tentacleSpeed);

    @Accessor("rotateSpeed")
    float getRotateSpeed();

    @Accessor("rotateSpeed")
    void setRotateSpeed(float rotateSpeed);
}
