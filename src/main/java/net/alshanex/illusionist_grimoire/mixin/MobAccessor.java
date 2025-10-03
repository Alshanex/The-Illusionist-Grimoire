package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.world.entity.Mob.class)
public interface MobAccessor {
    @Invoker("getAttackBoundingBox")
    AABB invokeGetAttackBoundingBox();
}
