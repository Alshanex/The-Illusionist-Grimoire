package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.block.IllusionBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IGBlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IllusionistGrimoireMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IllusionBlockEntity>> ILLUSION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("illusion_block_entity", () ->
                    BlockEntityType.Builder.of(IllusionBlockEntity::new, IGBlockRegistry.ILLUSION_BLOCK.get())
                            .build(null)
            );
}
