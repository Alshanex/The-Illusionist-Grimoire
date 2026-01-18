package net.alshanex.illusionist_grimoire.registry;

import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.block.IllusionBlock;
import net.alshanex.illusionist_grimoire.block.SpellTrapBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IGBlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(IllusionistGrimoireMod.MODID);

    public static final DeferredBlock<Block> ILLUSION_BLOCK = BLOCKS.register("illusion_block",
            () -> new IllusionBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, entityType) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
    );

    public static final Supplier<Block> SPELL_TRAP = BLOCKS.register("spell_trap",
            () -> new SpellTrapBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(state -> 3)));
}
