package net.alshanex.illusionist_grimoire.registry;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.effect.DisguisedEffect;
import net.alshanex.illusionist_grimoire.effect.FearedEffect;
import net.alshanex.illusionist_grimoire.effect.ParanoiaMobEffect;
import net.alshanex.illusionist_grimoire.effect.SquishEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IGEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, IllusionistGrimoireMod.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final DeferredHolder<MobEffect, MobEffect> DEPTH_TRICK = MOB_EFFECT_DEFERRED_REGISTER.register("depth_trick", () -> new MagicMobEffect(MobEffectCategory.HARMFUL, 4800826));
    public static final DeferredHolder<MobEffect, MobEffect> DISGUISED = MOB_EFFECT_DEFERRED_REGISTER.register("disguised", () -> new DisguisedEffect(MobEffectCategory.BENEFICIAL, 4800826));
    public static final DeferredHolder<MobEffect, MobEffect> PARANOIA = MOB_EFFECT_DEFERRED_REGISTER.register("paranoia", () -> new ParanoiaMobEffect(MobEffectCategory.HARMFUL, 4800826));
    public static final DeferredHolder<MobEffect, MobEffect> EMPTINESS = MOB_EFFECT_DEFERRED_REGISTER.register("emptiness", () -> new MagicMobEffect(MobEffectCategory.HARMFUL, 4800826));
    public static final DeferredHolder<MobEffect, MobEffect> FEARED = MOB_EFFECT_DEFERRED_REGISTER.register("feared", () -> new FearedEffect(MobEffectCategory.HARMFUL, 4800826));
    public static final DeferredHolder<MobEffect, MobEffect> SQUISH = MOB_EFFECT_DEFERRED_REGISTER.register("squish", () -> new SquishEffect(MobEffectCategory.NEUTRAL, 0xFF9933));
}
