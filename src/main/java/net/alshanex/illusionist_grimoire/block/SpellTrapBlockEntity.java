package net.alshanex.illusionist_grimoire.block;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.alshanex.illusionist_grimoire.registry.IGBlockEntityRegistry;
import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.alshanex.illusionist_grimoire.util.PlayerSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class SpellTrapBlockEntity extends BlockEntity {
    private ResourceLocation spellId = null;
    private int spellLevel = 1;
    private int cooldownTicks = 0;
    private int maxDetectionRange = 16;

    // Player snapshot for recreating dummy entity with player stats
    private PlayerSnapshot playerSnapshot = null;

    public SpellTrapBlockEntity(BlockPos pos, BlockState state) {
        super(IGBlockEntityRegistry.SPELL_TRAP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpellTrapBlockEntity blockEntity) {
        blockEntity.tick(level, pos, state);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Handle cooldown
        if (cooldownTicks > 0) {
            cooldownTicks--;
            setChanged();
            return;
        }

        // Check if we have a spell configured
        if (spellId == null || playerSnapshot == null) {
            return;
        }

        // Get facing direction
        Direction facing = state.getValue(SpellTrapBlock.FACING);

        // Find target in line
        LivingEntity target = findTargetInLine(level, pos, facing);

        // Cast spell if target found
        if (target != null) {
            castSpell(level, pos, target, facing);
        }
    }

    @Nullable
    private LivingEntity findTargetInLine(Level level, BlockPos pos, Direction facing) {
        BlockPos currentPos = pos.relative(facing); // Start one block away from trap

        for (int i = 0; i < maxDetectionRange; i++) {
            BlockState blockState = level.getBlockState(currentPos);

            // Check if we hit a solid block - stop raycast
            if (!blockState.isAir() && blockState.isSolidRender(level, currentPos)) {
                break;
            }

            // Check for entities in this block position
            AABB searchBox = new AABB(currentPos);
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    searchBox,
                    this::isValidTarget
            );

            if (!entities.isEmpty()) {
                return entities.get(0); // Return first found entity
            }

            currentPos = currentPos.relative(facing); // Move to next block
        }

        return null;
    }

    private boolean isValidTarget(LivingEntity entity) {
        // Only target enemies/monsters
        return entity instanceof Enemy && entity.isAlive();
    }

    private void castSpell(Level level, BlockPos pos, LivingEntity target, Direction facing) {
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) return;

        // Create dummy entity with player snapshot characteristics
        SpellTrapDummyEntity dummyEntity = createDummyEntity(level, pos, target, facing);
        if (dummyEntity == null) return;

        try {
            // Get the MagicData from the AbstractSpellCastingMob
            MagicData magicData = dummyEntity.getMagicData();

            // Cast the spell using CastSource.NONE (no mana consumption, no cooldown checks)
            spell.onCast(level, spellLevel, dummyEntity, CastSource.NONE, magicData);

            // Set cooldown based on spell's cooldown
            int spellCooldownSeconds = spell.getSpellCooldown();
            this.cooldownTicks = spellCooldownSeconds * 20; // Convert to ticks

            setChanged();
        } finally {
            dummyEntity.discard();
        }
    }

    @Nullable
    private SpellTrapDummyEntity createDummyEntity(Level level, BlockPos pos, LivingEntity target, Direction facing) {
        SpellTrapDummyEntity dummy = new SpellTrapDummyEntity(
                IGEntityRegistry.SPELL_TRAP_DUMMY.get(),
                level
        );

        // Position at the trap block's face
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(
                Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5)
        );
        dummy.setPos(spawnPos);

        // Make it look at the target
        Vec3 lookPos = target.position().add(0, target.getEyeHeight() / 2, 0);
        Vec3 direction = lookPos.subtract(spawnPos).normalize();

        double xRot = -Math.asin(direction.y) * 180.0 / Math.PI;
        double yRot = Math.atan2(direction.z, direction.x) * 180.0 / Math.PI - 90.0;

        dummy.setXRot((float) xRot);
        dummy.setYRot((float) yRot);
        dummy.xRotO = dummy.getXRot();
        dummy.yRotO = dummy.getYRot();

        // Apply player snapshot to dummy entity
        if (playerSnapshot != null) {
            playerSnapshot.applyToEntity(dummy);
        }

        return dummy;
    }

    // Setters for configuration
    public void setSpell(ResourceLocation spellId, int spellLevel) {
        this.spellId = spellId;
        this.spellLevel = spellLevel;
        setChanged();
    }

    public void setPlayerSnapshot(PlayerSnapshot snapshot) {
        this.playerSnapshot = snapshot;
        setChanged();
    }

    public void setMaxDetectionRange(int range) {
        this.maxDetectionRange = range;
        setChanged();
    }

    // NBT Serialization
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (spellId != null) {
            tag.putString("SpellId", spellId.toString());
        }
        tag.putInt("SpellLevel", spellLevel);
        tag.putInt("CooldownTicks", cooldownTicks);
        tag.putInt("MaxDetectionRange", maxDetectionRange);

        if (playerSnapshot != null) {
            CompoundTag snapshotTag = new CompoundTag();
            playerSnapshot.save(snapshotTag);
            tag.put("PlayerSnapshot", snapshotTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("SpellId")) {
            this.spellId = ResourceLocation.parse(tag.getString("SpellId"));
        }
        this.spellLevel = tag.getInt("SpellLevel");
        this.cooldownTicks = tag.getInt("CooldownTicks");
        this.maxDetectionRange = tag.getInt("MaxDetectionRange");

        if (tag.contains("PlayerSnapshot")) {
            this.playerSnapshot = PlayerSnapshot.load(tag.getCompound("PlayerSnapshot"));
        }
    }

    public void drops() {

    }
}