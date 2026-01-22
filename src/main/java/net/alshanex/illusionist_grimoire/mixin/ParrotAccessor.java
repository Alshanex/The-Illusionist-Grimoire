package net.alshanex.illusionist_grimoire.mixin;

import net.minecraft.world.entity.animal.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Parrot.class)
public interface ParrotAccessor {

    @Accessor("flapping")
    float getFlapping();

    @Accessor("flapping")
    void setFlapping(float flapping);

    @Accessor("nextFlap")
    float getNextFlap();

    @Accessor("nextFlap")
    void setNextFlap(float nextFlap);
}
