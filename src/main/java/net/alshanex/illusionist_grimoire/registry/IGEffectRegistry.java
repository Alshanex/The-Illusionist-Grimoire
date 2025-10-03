package net.alshanex.illusionist_grimoire.registry;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
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

    //public static final DeferredHolder<MobEffect, MobEffect> TRUE_VISION = MOB_EFFECT_DEFERRED_REGISTER.register("antigravity", () -> new MagicMobEffect(MobEffectCategory.NEUTRAL, 0x6c42f5));

}
