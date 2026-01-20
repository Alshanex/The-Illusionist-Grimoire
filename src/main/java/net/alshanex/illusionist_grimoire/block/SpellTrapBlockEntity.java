package net.alshanex.illusionist_grimoire.block;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.alshanex.illusionist_grimoire.registry.IGBlockEntityRegistry;
import net.alshanex.illusionist_grimoire.registry.IGEntityRegistry;
import net.alshanex.illusionist_grimoire.util.ModTags;
import net.alshanex.illusionist_grimoire.util.PlayerSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SpellTrapBlockEntity extends BlockEntity implements GeoBlockEntity {
    private ResourceLocation spellId = null;
    private int spellLevel = 1;
    private int cooldownTicks = 0;
    private int maxDetectionRange = 10;

    // Player snapshot for recreating dummy entity with player stats
    private PlayerSnapshot playerSnapshot = null;
    private UUID ownerUuid = null;

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
            level.sendBlockUpdated(pos, state, state, 3);
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
        Vec3 start = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5));
        Vec3 directionVec = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 end = start.add(directionVec.scale(maxDetectionRange));

        // Create a thin AABB along the entire detection line
        AABB detectionBox = new AABB(start, end).inflate(0.4);

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                detectionBox,
                this::isValidTarget
        );

        if (entities.isEmpty()) {
            return null;
        }

        // Find the closest valid entity that has line of sight (no blocks in the way)
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            double dist = start.distanceTo(entityPos);

            if (dist < closestDist && hasLineOfSight(level, start, entityPos, facing)) {
                closest = entity;
                closestDist = dist;
            }
        }

        return closest;
    }

    private boolean hasLineOfSight(Level level, Vec3 start, Vec3 target, Direction facing) {
        Vec3 direction = Vec3.atLowerCornerOf(facing.getNormal());
        double distance = start.distanceTo(target);

        // Check blocks along the path
        for (double d = 0; d < distance; d += 0.5) {
            Vec3 checkPos = start.add(direction.scale(d));
            BlockPos blockPos = BlockPos.containing(checkPos);
            BlockState state = level.getBlockState(blockPos);

            if (!state.isAir() && state.isSolidRender(level, blockPos)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOnCooldown() {
        return cooldownTicks > 0;
    }

    public boolean hasSpell() {
        return spellId != null;
    }

    private boolean isValidTarget(LivingEntity entity) {
        // Entity must be alive
        if (!entity.isAlive()) {
            return false;
        }

        // If no owner is set, only target enemies/monsters
        if (ownerUuid == null) {
            return entity instanceof Enemy;
        }

        // Get the owner player (if online)
        if (level instanceof ServerLevel serverLevel) {
            Player owner = serverLevel.getPlayerByUUID(ownerUuid);

            if (owner != null) {
                AbstractSpell spell = SpellRegistry.getSpell(spellId);
                if(ModTags.isSpellInTag(spell, ModTags.IS_SUPPORT_SPELL)){
                    // Target the owner
                    if (entity.getUUID().equals(ownerUuid)) {
                        return true;
                    }

                    // Target entities allied with the owner
                    if (entity.isAlliedTo(owner)) {
                        return true;
                    }

                    // Target other players on the same team
                    if (entity instanceof Player targetPlayer) {
                        if (owner.isAlliedTo(targetPlayer)) {
                            return true;
                        }
                    }

                    // Target the owner's pets/tamed entities
                    if (entity instanceof net.minecraft.world.entity.OwnableEntity ownable) {
                        if (ownerUuid.equals(ownable.getOwnerUUID())) {
                            return true;
                        }
                    }

                    return false;
                } else {
                    // Don't target the owner
                    if (entity.getUUID().equals(ownerUuid)) {
                        return false;
                    }

                    // Don't target entities allied with the owner
                    if (entity.isAlliedTo(owner)) {
                        return false;
                    }

                    // Don't target other players on the same team
                    if (entity instanceof Player targetPlayer) {
                        if (owner.isAlliedTo(targetPlayer)) {
                            return false;
                        }
                    }

                    // Don't target the owner's pets/tamed entities
                    if (entity instanceof net.minecraft.world.entity.OwnableEntity ownable) {
                        if (ownerUuid.equals(ownable.getOwnerUUID())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void castSpell(Level level, BlockPos pos, LivingEntity target, Direction facing) {
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) return;

        // Create dummy entity with player snapshot characteristics
        SpellTrapDummyEntity dummyEntity = createDummyEntity(level, pos, target, facing);
        if (dummyEntity == null) return;

        int lifeTicks = spell.getCastTime(spellLevel);
        dummyEntity.setLifeTicks(lifeTicks);

        dummyEntity.setTarget(target);

        level.addFreshEntity(dummyEntity);

        LivingEntity owner = getOwner(level);
        if (owner != null) {
            SummonedEntitiesCastData summonData = new SummonedEntitiesCastData();
            int summonDuration = spell.getSpellCooldown() * 20; // Use spell cooldown as summon duration
            SummonManager.initSummon(owner, dummyEntity, summonDuration, summonData);
        }

        // Get the MagicData from the AbstractSpellCastingMob
        MagicData magicData = dummyEntity.getMagicData();

        magicData.setAdditionalCastData(new TargetEntityCastData(target));

        // Cast the spell using CastSource.NONE (no mana consumption, no cooldown checks)
        spell.onCast(level, spellLevel, dummyEntity, CastSource.NONE, magicData);

        // Set cooldown based on spell's cooldown
        this.cooldownTicks = spell.getSpellCooldown() * 5;

        setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }

    @Nullable
    private SpellTrapDummyEntity createDummyEntity(Level level, BlockPos pos, LivingEntity target, Direction facing) {
        SpellTrapDummyEntity dummy = new SpellTrapDummyEntity(
                IGEntityRegistry.SPELL_TRAP_DUMMY.get(),
                level
        );

        Vec3 spawnPos = Vec3.atCenterOf(pos);
        dummy.setPos(spawnPos);

        Vec3 lookPos = target.position().add(0, target.getEyeHeight() / 2, 0);
        Vec3 direction = lookPos.subtract(spawnPos).normalize();

        double xRot = -Math.asin(direction.y) * 180.0 / Math.PI;
        double yRot = Math.atan2(direction.z, direction.x) * 180.0 / Math.PI - 90.0;

        dummy.setXRot((float) xRot);
        dummy.setYRot((float) yRot);
        dummy.xRotO = dummy.getXRot();
        dummy.yRotO = dummy.getYRot();

        // Apply player snapshot to dummy entity (attributes only, no equipment)
        if (playerSnapshot != null) {
            playerSnapshot.applyToEntity(dummy);
        }

        // Set the owner so the dummy is allied to them
        if (ownerUuid != null) {
            dummy.setOwnerUuid(ownerUuid);
        }

        return dummy;
    }

    @Nullable
    public LivingEntity getOwner(Level level) {
        if (this.ownerUuid != null && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(this.ownerUuid);
            if (player != null) {
                return player;
            }
        }
        return null;
    }

    public boolean isOwner(UUID entityUUID){
        if(this.ownerUuid == null){
            return false;
        }
        return this.ownerUuid.equals(entityUUID);
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

    public void setOwner(Player player) {
        this.ownerUuid = player.getUUID();
        setChanged();
    }

    public void setOwner(UUID uuid, String name) {
        this.ownerUuid = uuid;
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        handleUpdateTag(pkt.getTag(), registries);
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

        if (ownerUuid != null) {
            tag.putUUID("OwnerUUID", ownerUuid);
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

        if (tag.contains("OwnerUUID")) {
            this.ownerUuid = tag.getUUID("OwnerUUID");
        }
    }

    public void drops() {

    }

    public ResourceLocation getSpellId(){
        return this.spellId;
    }

    public Vector3f getSpellColor() {
        if (spellId == null) {
            return new Vector3f(1.0f, 1.0f, 1.0f); // White
        }

        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) {
            return new Vector3f(1.0f, 1.0f, 1.0f); // White
        }

        return spell.getSchoolType().getTargetingColor();
    }

    // Geckolib animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected final RawAnimation idle = RawAnimation.begin().thenLoop("idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(this, "idle", 0, this::idlePredicate));
    }

    protected PlayState idlePredicate(AnimationState event) {
        event.getController().setAnimation(idle);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}