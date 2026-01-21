package net.alshanex.illusionist_grimoire.mixin;

import net.alshanex.illusionist_grimoire.util.PlayerDisguiseProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerEntityMixin implements PlayerDisguiseProvider {

    @Unique
    private @Nullable LivingEntity illusionistGrimoire$disguiseEntity = null;

    @Override
    public @Nullable LivingEntity illusionistGrimoire$getDisguiseEntity() {
        return illusionistGrimoire$disguiseEntity;
    }

    @Override
    public void illusionistGrimoire$setDisguiseEntity(@Nullable LivingEntity entity) {
        this.illusionistGrimoire$disguiseEntity = entity;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void illusionistGrimoire$readDisguiseNbt(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("IllusionistGrimoireDisguise")) {
            CompoundTag disguiseTag = compound.getCompound("IllusionistGrimoireDisguise");

            Optional<EntityType<?>> typeOpt = EntityType.by(disguiseTag);
            if (typeOpt.isPresent()) {
                Player player = (Player) (Object) this;
                this.illusionistGrimoire$disguiseEntity = (LivingEntity) typeOpt.get().create(player.level());
                if (this.illusionistGrimoire$disguiseEntity != null) {
                    this.illusionistGrimoire$disguiseEntity.load(disguiseTag);
                }
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void illusionistGrimoire$writeDisguiseNbt(CompoundTag compound, CallbackInfo ci) {
        if (this.illusionistGrimoire$disguiseEntity != null) {
            CompoundTag disguiseTag = new CompoundTag();
            this.illusionistGrimoire$disguiseEntity.saveWithoutId(disguiseTag);
            compound.put("IllusionistGrimoireDisguise", disguiseTag);
        }
    }
}
