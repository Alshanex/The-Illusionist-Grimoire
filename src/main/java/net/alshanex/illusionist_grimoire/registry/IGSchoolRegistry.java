package net.alshanex.illusionist_grimoire.registry;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IGSchoolRegistry {
    public static final ResourceLocation ILLUSIONISM_RESOURCE = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "illusion");
    public static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, IllusionistGrimoireMod.MODID);

    public static final Supplier<SchoolType> ILLUSIONISM = registerSchool(new SchoolType(
            ILLUSIONISM_RESOURCE,
            ModTags.EVOCATION_FOCUS,
            Component.translatable("school.illusionist_grimoire.illusion").withColor(0xd3d3d3),
            AttributeRegistry.EVOCATION_SPELL_POWER,
            AttributeRegistry.EVOCATION_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.EVOCATION_MAGIC
    ));

    private static Supplier<SchoolType> registerSchool(SchoolType schoolType) {
        return SCHOOLS.register(schoolType.getId().getPath(), () -> schoolType);
    }
}
