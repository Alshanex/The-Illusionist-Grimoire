package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.util.WitherHeadRotation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherBoss.class)
public interface WitherAccessor extends WitherHeadRotation {

    @Accessor("xRotHeads")
    float[] illusionist_grimoire$getXRotHeadsArray();

    @Accessor("yRotHeads")
    float[] illusionist_grimoire$getYRotHeadsArray();

    @Accessor("xRotOHeads")
    float[] illusionist_grimoire$getXRotOHeadsArray();

    @Accessor("yRotOHeads")
    float[] illusionist_grimoire$getYRotOHeadsArray();
}
