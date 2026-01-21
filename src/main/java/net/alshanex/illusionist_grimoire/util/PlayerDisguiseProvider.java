package net.alshanex.illusionist_grimoire.util;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface PlayerDisguiseProvider {
    @Nullable
    LivingEntity illusionistGrimoire$getDisguiseEntity();

    void illusionistGrimoire$setDisguiseEntity(@Nullable LivingEntity entity);
}
