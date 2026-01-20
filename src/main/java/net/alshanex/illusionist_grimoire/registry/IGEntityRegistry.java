package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.entity.FearProjectile;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IGEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, IllusionistGrimoireMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpellTrapDummyEntity>> SPELL_TRAP_DUMMY =
            ENTITY_TYPES.register("spell_trap_dummy", () -> EntityType.Builder.of(
                            SpellTrapDummyEntity::new,
                            MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(0)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "spell_trap_dummy").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FearProjectile>> FEAR_BOLT =
            ENTITY_TYPES.register("fear_bolt", () -> EntityType.Builder.<FearProjectile>of(FearProjectile::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "fear_bolt").toString()));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
