package net.alshanex.illusionist_grimoire.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record PictureBookData(int selectedSlot, Map<Integer, SlotData> slots) {

    public static final PictureBookData DEFAULT = new PictureBookData(0, new HashMap<>());

    // Helper codec that converts between Integer keys and String keys for serialization
    private static final Codec<Map<Integer, SlotData>> SLOTS_CODEC =
            Codec.unboundedMap(
                    Codec.STRING.xmap(Integer::parseInt, String::valueOf),
                    SlotData.CODEC
            );

    // Codec for serialization/deserialization
    public static final Codec<PictureBookData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("selectedSlot").forGetter(PictureBookData::selectedSlot),
                    SLOTS_CODEC.fieldOf("slots").forGetter(PictureBookData::slots)
            ).apply(instance, PictureBookData::new)
    );

    // Stream codec for network sync
    public static final StreamCodec<RegistryFriendlyByteBuf, PictureBookData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PictureBookData::selectedSlot,
                    ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.INT,
                            SlotData.STREAM_CODEC
                    ),
                    PictureBookData::slots,
                    PictureBookData::new
            );

    public PictureBookData withSelectedSlot(int slot) {
        return new PictureBookData(slot, this.slots);
    }

    public PictureBookData withSlot(int slot, SlotData data) {
        Map<Integer, SlotData> newSlots = new HashMap<>(this.slots);
        newSlots.put(slot, data);
        return new PictureBookData(this.selectedSlot, newSlots);
    }

    public PictureBookData withoutSlot(int slot) {
        Map<Integer, SlotData> newSlots = new HashMap<>(this.slots);
        newSlots.remove(slot);
        return new PictureBookData(this.selectedSlot, newSlots);
    }

    public record SlotData(ResourceLocation entityType, CompoundTag nbt, @Nullable UUID playerUUID, @Nullable String playerName, @Nullable String skinTexture, @Nullable String skinSignature) {

        // Constructor for mob disguises
        public SlotData(ResourceLocation entityType, CompoundTag nbt) {
            this(entityType, nbt, null, null, null, null);
        }

        // Constructor for player disguises with skin data
        public SlotData(UUID playerUUID, String playerName, @Nullable String skinTexture, @Nullable String skinSignature) {
            this(ResourceLocation.fromNamespaceAndPath("minecraft", "player"), new CompoundTag(), playerUUID, playerName, skinTexture, skinSignature);
        }

        public boolean isPlayerDisguise() {
            return playerUUID != null;
        }

        public static final Codec<SlotData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("entityType").forGetter(SlotData::entityType),
                        CompoundTag.CODEC.fieldOf("nbt").forGetter(SlotData::nbt),
                        UUIDUtil.CODEC.optionalFieldOf("playerUUID").forGetter(data -> Optional.ofNullable(data.playerUUID)),
                        Codec.STRING.optionalFieldOf("playerName").forGetter(data -> Optional.ofNullable(data.playerName)),
                        Codec.STRING.optionalFieldOf("skinTexture").forGetter(data -> Optional.ofNullable(data.skinTexture)),
                        Codec.STRING.optionalFieldOf("skinSignature").forGetter(data -> Optional.ofNullable(data.skinSignature))
                ).apply(instance, (type, nbt, uuid, name, texture, signature) ->
                        new SlotData(type, nbt, uuid.orElse(null), name.orElse(null), texture.orElse(null), signature.orElse(null)))
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SlotData> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC,
                        SlotData::entityType,
                        ByteBufCodecs.COMPOUND_TAG,
                        SlotData::nbt,
                        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
                        data -> Optional.ofNullable(data.playerUUID),
                        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                        data -> Optional.ofNullable(data.playerName),
                        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                        data -> Optional.ofNullable(data.skinTexture),
                        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                        data -> Optional.ofNullable(data.skinSignature),
                        (type, nbt, uuid, name, texture, signature) ->
                                new SlotData(type, nbt, uuid.orElse(null), name.orElse(null), texture.orElse(null), signature.orElse(null))
                );
    }
}