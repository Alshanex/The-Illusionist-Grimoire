package net.alshanex.illusionist_grimoire.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.block.IllusionBlockEntity;
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
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "illusion_wall");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        String size = getHeight(spellLevel, caster) + "x" + getHeight(spellLevel, caster);
        return List.of(
                Component.translatable("ui.illusionist_grimoire.wall_size", size)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(IGSchoolRegistry.ILLUSIONISM_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(10)
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
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 20, 0, false, false, true));

        double spellPower = entity.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER);
        placePhaseWall(entity, level, getWidth(spellLevel, entity), getHeight(spellLevel, entity), spellPower);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static void placePhaseWall(LivingEntity entity, Level level, int width, int height, double ownerSpellPower) {
        // Get entity's facing direction (only cardinal directions)
        Direction facing = entity.getDirection();
        BlockPos entityPos = entity.blockPosition();

        // Get starting position (2 blocks in front of entity)
        BlockPos startPos = entityPos.relative(facing, 2);

        // Determine perpendicular direction for width
        Direction perpendicular = getPerpendicularDirection(facing);

        // Place blocks in a grid pattern
        for (int h = 0; h < height; h++) {
            for (int w = -width; w <= width; w++) {
                BlockPos placePos = startPos
                        .relative(perpendicular, w)
                        .above(h);

                // Only replace air blocks
                if (level.getBlockState(placePos).getBlock() == Blocks.AIR) {
                    level.setBlock(placePos, IGBlockRegistry.ILLUSION_BLOCK.get().defaultBlockState(), 3);

                    // Set the owner spell power in the block entity
                    if (level.getBlockEntity(placePos) instanceof IllusionBlockEntity illusionBlockEntity) {
                        illusionBlockEntity.setOwnerSpellPower(ownerSpellPower);
                        illusionBlockEntity.setSummoner(entity);
                        illusionBlockEntity.setChanged(); // Mark as changed to ensure it saves
                    }
                }
            }
        }
    }

    private static Direction getPerpendicularDirection(Direction facing) {
        return switch (facing) {
            case NORTH, SOUTH -> Direction.EAST;
            case EAST, WEST -> Direction.NORTH;
            default -> Direction.EAST; // Fallback
        };
    }

    private int getHeight(int spellLevel, LivingEntity caster) {
        return 1 + getSpellPowerBonus(spellLevel, caster) + spellLevel;
    }

    private int getWidth(int spellLevel, LivingEntity caster) {
        return (int) getHeight(spellLevel, caster) / 2;
    }

    private int getSpellPowerBonus(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * .2f);
    }
}
