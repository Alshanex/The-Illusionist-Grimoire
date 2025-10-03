package net.alshanex.illusionist_grimoire.registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.spells.DepthTrickSpell;
import net.alshanex.illusionist_grimoire.spells.IllusionWallSpell;
import net.alshanex.illusionist_grimoire.spells.ParanoiaSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY;

public class IGSpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELL_REGISTRY_KEY, IllusionistGrimoireMod.MODID);
    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    private static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> ILLUSION_WALL = registerSpell(new IllusionWallSpell());
    public static final Supplier<AbstractSpell> DEPTH_TRICK = registerSpell(new DepthTrickSpell());
    public static final Supplier<AbstractSpell> PARANOIA = registerSpell(new ParanoiaSpell());
}
