package net.alshanex.illusionist_grimoire.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.registry.IGSchoolRegistry;
import net.alshanex.illusionist_grimoire.util.IGUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class EmptinessSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "emptiness");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(IGSchoolRegistry.ILLUSIONISM_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(45)
            .build();

    public EmptinessSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 50;
        this.baseManaCost = 60;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                boolean canBypassIllusion;
                if(targetEntity instanceof Player player) {
                    canBypassIllusion = IGUtils.canBypassIllusions(player, entity.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER));
                } else {
                    canBypassIllusion = IGUtils.canMobBypassIllusions(targetEntity);
                }

                if(!canBypassIllusion) {
                    targetEntity.addEffect(new MobEffectInstance(IGEffectRegistry.EMPTINESS, getDuration(spellLevel, entity), 0));
                    if(!(targetEntity instanceof Player)){
                        targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, getDuration(spellLevel, entity), 5));
                    }
                }
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20 * 30);
    }
}
