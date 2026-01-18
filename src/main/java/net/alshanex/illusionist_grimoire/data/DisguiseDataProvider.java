package net.alshanex.illusionist_grimoire.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nullable;

public class DisguiseDataProvider implements IAttachmentSerializer<CompoundTag, DisguiseData> {
    @Override
    public DisguiseData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        var magicData = holder instanceof ServerPlayer serverPlayer ? new DisguiseData(serverPlayer) : new DisguiseData(true);
        magicData.loadNBTData(tag, provider);
        return magicData;
    }

    @Override
    public @Nullable CompoundTag write(DisguiseData disguiseData, HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        disguiseData.saveNBTData(tag, provider);
        return tag;
    }
}
