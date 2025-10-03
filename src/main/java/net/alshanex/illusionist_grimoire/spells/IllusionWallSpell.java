package net.alshanex.illusionist_grimoire.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.registry.IGBlockRegistry;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.registry.IGSchoolRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class IllusionWallSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "schrodinger_box");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.illusionist_grimoire.wall_size", getRadius(spellLevel))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(IGSchoolRegistry.ILLUSIONISM_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public IllusionWallSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 35;
        this.castTime = 0;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(IGEffectRegistry.TRUE_VISION, 600, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 100, 0, false, false, true));

        createPhaseBox(entity, level, getRadius(spellLevel));

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static void createPhaseBox(Entity entity, Level level, int radius) {
        BlockPos center = entity.blockPosition();

        createWalls(level, center, radius);

        createRoof(level, center, radius);

        createFloor(level, center, radius);
    }

    private static void createWalls(Level level, BlockPos center, int radius) {
        // North wall (negative Z)
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                BlockPos pos = center.offset(x, y, -radius);
                replaceWithPhaseBlock(level, pos);
            }
        }

        // South wall (positive Z)
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                BlockPos pos = center.offset(x, y, radius);
                replaceWithPhaseBlock(level, pos);
            }
        }

        // East wall (positive X)
        for (int z = -radius + 1; z < radius; z++) { // -1 and +1 to avoid corner duplicates
            for (int y = 0; y <= radius; y++) {
                BlockPos pos = center.offset(radius, y, z);
                replaceWithPhaseBlock(level, pos);
            }
        }

        // West wall (negative X)
        for (int z = -radius + 1; z < radius; z++) { // -1 and +1 to avoid corner duplicates
            for (int y = 0; y <= radius; y++) {
                BlockPos pos = center.offset(-radius, y, z);
                replaceWithPhaseBlock(level, pos);
            }
        }
    }

    private static void createRoof(Level level, BlockPos center, int radius) {
        // Roof at height = radius
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = center.offset(x, radius, z);
                replaceWithPhaseBlock(level, pos);
            }
        }
    }

    private static void createFloor(Level level, BlockPos center, int radius) {
        // Floor at entity's feet level
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = center.offset(x, 0, z);
                replaceWithPhaseBlock(level, pos);
            }
        }
    }

    private static void replaceWithPhaseBlock(Level level, BlockPos pos) {
        // Only replace air blocks
        if (level.getBlockState(pos).getBlock() == Blocks.AIR) {
            level.setBlock(pos, IGBlockRegistry.ILLUSION_BLOCK.get().defaultBlockState(), 3);
        }
    }

    private int getRadius(int spellLevel) {
        return 2 + spellLevel;
    }
}
