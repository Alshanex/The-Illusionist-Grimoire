package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.util.GuardianAnimationTracker;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Guardian.class)
public class GuardianTickTrackerMixin implements GuardianAnimationTracker {

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
