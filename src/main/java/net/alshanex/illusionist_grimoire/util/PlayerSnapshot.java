package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.alshanex.illusionist_grimoire.entity.SpellTrapDummyEntity;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores a snapshot of a player's state at the time they placed a spell trap.
 * This includes attributes, equipment, and other relevant data for spell casting.
 */
public class PlayerSnapshot {
    private UUID playerUuid;
    private String playerName;
    private Map<String, Double> attributeValues; // Store as string keys for NBT compatibility

    public PlayerSnapshot() {
        this.attributeValues = new HashMap<>();
    }

    public static PlayerSnapshot fromPlayer(Player player) {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.playerUuid = player.getUUID();
        snapshot.playerName = player.getName().getString();

        // Capture spell power attributes from Iron Spells
        snapshot.captureAttribute(player, AttributeRegistry.SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.COOLDOWN_REDUCTION);
        snapshot.captureAttribute(player, AttributeRegistry.CAST_TIME_REDUCTION);

        // Capture school-specific spell powers
        snapshot.captureAttribute(player, AttributeRegistry.FIRE_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.ICE_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.LIGHTNING_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.HOLY_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.ENDER_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.BLOOD_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.EVOCATION_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.NATURE_SPELL_POWER);
        snapshot.captureAttribute(player, AttributeRegistry.ELDRITCH_SPELL_POWER);

        // Capture summon damage for summoning spells
        snapshot.captureAttribute(player, AttributeRegistry.SUMMON_DAMAGE);

        return snapshot;
    }

    private void captureAttribute(Player player, Holder<Attribute> attributeHolder) {
        AttributeInstance instance = player.getAttribute(attributeHolder);
        if (instance != null) {
            // Use the attribute's description ID as the key
            String key = attributeHolder.value().getDescriptionId();
            attributeValues.put(key, instance.getValue());
        }
    }

    public void applyToEntity(SpellTrapDummyEntity entity) {
        // Apply spell power attributes
        applyHolderAttribute(entity, AttributeRegistry.SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.COOLDOWN_REDUCTION);
        applyHolderAttribute(entity, AttributeRegistry.CAST_TIME_REDUCTION);

        // Apply school-specific powers
        applyHolderAttribute(entity, AttributeRegistry.FIRE_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.ICE_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.LIGHTNING_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.HOLY_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.ENDER_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.BLOOD_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.EVOCATION_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.NATURE_SPELL_POWER);
        applyHolderAttribute(entity, AttributeRegistry.ELDRITCH_SPELL_POWER);

        applyHolderAttribute(entity, AttributeRegistry.SUMMON_DAMAGE);
    }

    private void applyHolderAttribute(SpellTrapDummyEntity entity, Holder<Attribute> attributeHolder) {
        String key = attributeHolder.value().getDescriptionId();
        Double value = attributeValues.get(key);
        if (value != null) {
            entity.applyAttributeValue(attributeHolder, value);
        }
    }

    public void save(CompoundTag tag) {
        if (playerUuid != null) {
            tag.putUUID("PlayerUUID", playerUuid);
        }
        if (playerName != null) {
            tag.putString("PlayerName", playerName);
        }

        // Save attributes as simple key-value pairs
        CompoundTag attributesTag = new CompoundTag();
        for (Map.Entry<String, Double> entry : attributeValues.entrySet()) {
            attributesTag.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put("Attributes", attributesTag);
    }

    public static PlayerSnapshot load(CompoundTag tag) {
        PlayerSnapshot snapshot = new PlayerSnapshot();

        if (tag.contains("PlayerUUID")) {
            snapshot.playerUuid = tag.getUUID("PlayerUUID");
        }
        if (tag.contains("PlayerName")) {
            snapshot.playerName = tag.getString("PlayerName");
        }

        // Load attributes
        if (tag.contains("Attributes")) {
            CompoundTag attributesTag = tag.getCompound("Attributes");
            for (String key : attributesTag.getAllKeys()) {
                snapshot.attributeValues.put(key, attributesTag.getDouble(key));
            }
        }

        return snapshot;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }
}
