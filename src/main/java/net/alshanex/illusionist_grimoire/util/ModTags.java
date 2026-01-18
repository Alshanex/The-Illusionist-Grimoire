package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;

public class ModTags {
    public static final TagKey<EntityType<?>> ILLUSION_IMMUNE_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "illusion_immune"));

    public static TagKey<AbstractSpell> TRAP_SPELL_BLACKLIST = create(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "trap_spell_blacklist"));
    public static TagKey<AbstractSpell> IS_SUPPORT_SPELL = create(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "is_support_spell"));

    public static TagKey<AbstractSpell> create(ResourceLocation name) {
        return new TagKey<AbstractSpell>(SpellRegistry.SPELL_REGISTRY_KEY, name);
    }

    public static boolean isSpellInTag(AbstractSpell spell, TagKey<AbstractSpell> tag) {
        ArrayList<AbstractSpell> list = new ArrayList();
        SpellRegistry.REGISTRY.getHolder(spell.getSpellResource()).ifPresent((a) -> {
            if (a.is(tag)) {
                list.add(spell);
            }

        });
        return !list.isEmpty();
    }
}
