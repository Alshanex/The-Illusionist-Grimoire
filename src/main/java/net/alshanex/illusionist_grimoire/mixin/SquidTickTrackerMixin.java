package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.util.SquidAnimationTracker;
import net.minecraft.world.entity.animal.Squid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Squid.class)
public class SquidTickTrackerMixin implements SquidAnimationTracker {

    @Unique
    private int illusionist_grimoire$lastAnimationTick = -1;

    @Override
    public int illusionist_grimoire$getLastAnimationTick() {
        return this.illusionist_grimoire$lastAnimationTick;
    }

    @Override
    public void illusionist_grimoire$setLastAnimationTick(int tick) {
        this.illusionist_grimoire$lastAnimationTick = tick;
    }
}
