package net.alshanex.illusionist_grimoire.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.registry.IGSchoolRegistry;
import net.alshanex.illusionist_grimoire.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class SquishSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "squish");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(IGSchoolRegistry.ILLUSIONISM_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(70)
            .build();

    public SquishSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 8;
        this.castTime = 40;
        this.baseManaCost = 70;
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
        if (Utils.preCastTargetHelper(level, entity, playerMagicData, this, 10, .15f)) {
            var target = ((TargetEntityCastData) playerMagicData.getAdditionalCastData()).getTarget((ServerLevel) level);
            if (target == null) {
                return false;
            }
            if (target.getType().is(ModTags.DISGUISE_BLACKLIST)){
                if(entity instanceof ServerPlayer serverPlayer){
                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(Component.translatable("message.illusionist_grimoire.disguise.blacklisted_mob")
                                    .withStyle(ChatFormatting.RED)));
                }
                return false;
            }
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(target));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        LivingEntity finalTarget = null;

        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData target && level instanceof ServerLevel server && target.getTarget(server)!=null) {
            LivingEntity targetEntity = target.getTarget(server);
            if (targetEntity != null){
                finalTarget = targetEntity;
            }
        } else {
            finalTarget = entity;
        }

        if(finalTarget != null){
            finalTarget.addEffect(new MobEffectInstance(IGEffectRegistry.SQUISH, getDuration(spellLevel, entity), 0, false, false, true));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }
}
