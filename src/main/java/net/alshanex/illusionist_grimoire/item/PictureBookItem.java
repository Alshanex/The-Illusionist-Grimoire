package net.alshanex.illusionist_grimoire.item;

import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import net.alshanex.illusionist_grimoire.data.DisguiseData;
import net.alshanex.illusionist_grimoire.data.PictureBookData;
import net.alshanex.illusionist_grimoire.network.IGSyncPlayerDataPacket;
import net.alshanex.illusionist_grimoire.registry.IGDataAttachments;
import net.alshanex.illusionist_grimoire.registry.IGDataComponents;
import net.alshanex.illusionist_grimoire.registry.IGEffectRegistry;
import net.alshanex.illusionist_grimoire.registry.IGSpellRegistry;
import net.alshanex.illusionist_grimoire.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PictureBookItem extends Item {
    private static final int MAX_SLOTS = 8;
    private static final int DISGUISE_DURATION = 1200; // 1 minute (20 ticks/sec * 60 sec)

    public PictureBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ItemStack offhand = player.getOffhandItem();
        int slot = getSelectedSlot(stack);

        // binding mode
        if (isBindingItem(offhand)) {
            // Raycast to find entity
            LivingEntity targetEntity = getTargetEntity(player, 10.0);

            if (targetEntity == null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(
                                    Component.translatable("message.illusionist_grimoire.picture_book.no_entity_targeted")
                                            .withStyle(ChatFormatting.RED)
                            )
                    );
                }
                return InteractionResultHolder.fail(stack);
            }

            // Check if entity is blacklisted
            if (targetEntity.getType().is(ModTags.DISGUISE_BLACKLIST)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(
                                    Component.translatable("message.illusionist_grimoire.disguise.blacklisted_mob")
                                            .withStyle(ChatFormatting.RED)
                            )
                    );
                }
                return InteractionResultHolder.fail(stack);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                double neededMana = 60;
                if (!(serverPlayer.isCreative() && !ServerConfigs.CREATIVE_MANA_COST.get())
                        && magicData.getMana() < neededMana) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("message.illusionist_grimoire.disguise.no_mana")
                                    .withStyle(style -> style.withColor(0xFF5555))));
                    return InteractionResultHolder.fail(stack);
                }
            }

            // Check if target is a player
            if (targetEntity instanceof Player targetPlayer) {
                String skinTexture = null;
                String skinSignature = null;

                GameProfile profile = targetPlayer.getGameProfile();
                if (profile.getProperties().containsKey("textures")) {
                    var textureProperty = profile.getProperties().get("textures").iterator().next();
                    skinTexture = textureProperty.value();
                    skinSignature = textureProperty.signature();
                }

                // Store player-specific data with skin
                setPlayerSlotData(stack, slot, targetPlayer.getUUID(), targetPlayer.getName().getString(), skinTexture, skinSignature);

                // Success feedback
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(
                                    Component.translatable("message.illusionist_grimoire.picture_book.entity_bound",
                                                    slot + 1, targetPlayer.getDisplayName())
                                            .withStyle(ChatFormatting.GREEN)
                            )
                    );

                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    double neededMana = 60;

                    float newMana = (float) Math.max(magicData.getMana() - neededMana, 0);
                    magicData.setMana(newMana);
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
                }
            } else {
                // Save entity data to selected slot
                CompoundTag entityNBT = new CompoundTag();
                targetEntity.saveWithoutId(entityNBT);
                ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(targetEntity.getType());

                setSlotData(stack, slot, entityType, entityNBT);

                // Success feedback
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetActionBarTextPacket(
                                    Component.translatable("message.illusionist_grimoire.picture_book.entity_bound",
                                                    slot + 1, targetEntity.getDisplayName())
                                            .withStyle(ChatFormatting.GREEN)
                            )
                    );

                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    double neededMana = 60;

                    float newMana = (float) Math.max(magicData.getMana() - neededMana, 0);
                    magicData.setMana(newMana);
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
                }
            }

            // Consume
            if (!player.isCreative()) {
                offhand.shrink(1);
            }

            return InteractionResultHolder.success(stack);
        }

        // Check if slot has disguise
        if (!hasDisguiseInSlot(stack, slot)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(
                        new ClientboundSetActionBarTextPacket(
                                Component.translatable("message.illusionist_grimoire.picture_book.no_entity_in_slot")
                                        .withStyle(ChatFormatting.RED)
                        )
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        // Get slot data
        PictureBookData.SlotData slotData = getSlotData(stack, slot);
        if (slotData == null) {
            return InteractionResultHolder.fail(stack);
        }

        DisguiseData disguiseData = DisguiseData.getDisguiseData(player);

        // Check if it's a player disguise
        if (slotData.isPlayerDisguise()) {
            // Apply player disguise with skin properties
            disguiseData.setDisguisedPlayer(
                    slotData.playerUUID(),
                    slotData.playerName(),
                    slotData.skinTexture(),
                    slotData.skinSignature()
            );

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new IGSyncPlayerDataPacket(disguiseData));
            player.addEffect(new MobEffectInstance(IGEffectRegistry.DISGUISED, DISGUISE_DURATION, 0, false, false));

            if(player instanceof ServerPlayer serverPlayer){
                MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                double neededMana = 60;

                float newMana = (float) Math.max(magicData.getMana() - neededMana, 0);
                magicData.setMana(newMana);
                PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
            }

            return InteractionResultHolder.success(stack);
        } else {
            disguiseData.disguisedPlayerUUID = null;
            disguiseData.disguisedPlayerName = null;
            disguiseData.disguisedPlayerProfile = null;

            // Create mob entity
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
            if (type == null) {
                return InteractionResultHolder.fail(stack);
            }

            LivingEntity mobEntity = (LivingEntity) type.create(level);
            if (mobEntity != null) {
                mobEntity.load(slotData.nbt());

                // Apply disguise
                disguiseData.setMobDisguiseEntity(mobEntity);

                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new IGSyncPlayerDataPacket(disguiseData));

                // Apply effect
                player.addEffect(new MobEffectInstance(IGEffectRegistry.DISGUISED, DISGUISE_DURATION, 0, false, false));

                if(player instanceof ServerPlayer serverPlayer){
                    MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
                    double neededMana = 60;

                    float newMana = (float) Math.max(magicData.getMana() - neededMana, 0);
                    magicData.setMana(newMana);
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magicData));
                }

                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    public static void setPlayerSlotData(ItemStack stack, int slot, UUID playerUUID, String playerName, @Nullable String skinTexture, @Nullable String skinSignature) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        PictureBookData.SlotData slotData = new PictureBookData.SlotData(playerUUID, playerName, skinTexture, skinSignature);
        stack.set(IGDataComponents.PICTURE_BOOK_DATA, data.withSlot(slot, slotData));
    }

    private boolean isBindingItem(ItemStack item){
        if(item.getItem() instanceof Scroll) {
            ISpellContainer container = ISpellContainer.get(item);
            if(!container.isEmpty()){
                AbstractSpell boundSpell = container.getSpellAtIndex(0).getSpell();
                if(boundSpell == IGSpellRegistry.DISGUISE.get()){
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    private LivingEntity getTargetEntity(Player player, double range) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        // Create AABB for the raycast area
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0);

        // Find all living entities in the search area
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        LivingEntity closestEntity = null;
        double closestDistance = range;

        for (LivingEntity entity : entities) {
            // Get the entity's bounding box
            AABB entityBox = entity.getBoundingBox().inflate(0.3); // Slightly inflate for easier targeting

            // Check if the ray intersects with the entity's bounding box
            Optional<Vec3> hitResult = entityBox.clip(eyePos, endPos);

            if (hitResult.isPresent()) {
                double distance = eyePos.distanceTo(hitResult.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        return closestEntity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int selectedSlot = getSelectedSlot(stack);
        tooltipComponents.add(Component.translatable("tooltip.illusionist_grimoire.picture_book.selected_slot", selectedSlot + 1));

        PictureBookData.SlotData slotData = getSlotData(stack, selectedSlot);
        if (slotData != null) {
            if (slotData.isPlayerDisguise()) {
                tooltipComponents.add(Component.translatable("tooltip.illusionist_grimoire.picture_book.bound", slotData.playerName()));
            } else {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
                if (type != null) {
                    tooltipComponents.add(Component.translatable("tooltip.illusionist_grimoire.picture_book.bound", type.getDescription()));
                }
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.illusionist_grimoire.picture_book.empty"));
        }
    }

    // === Data Component Helper Methods ===

    public static int getSelectedSlot(ItemStack stack) {
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        return Math.max(0, Math.min(MAX_SLOTS - 1, data.selectedSlot()));
    }

    public static void setSelectedSlot(ItemStack stack, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        stack.set(IGDataComponents.PICTURE_BOOK_DATA, data.withSelectedSlot(slot));
    }

    @Nullable
    public static PictureBookData.SlotData getSlotData(ItemStack stack, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return null;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        return data.slots().get(slot);
    }

    public static void setSlotData(ItemStack stack, int slot, ResourceLocation entityType, CompoundTag entityNBT) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        PictureBookData.SlotData slotData = new PictureBookData.SlotData(entityType, entityNBT);
        stack.set(IGDataComponents.PICTURE_BOOK_DATA, data.withSlot(slot, slotData));
    }

    public static boolean hasDisguiseInSlot(ItemStack stack, int slot) {
        return getSlotData(stack, slot) != null;
    }

    public static void clearSlot(ItemStack stack, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        stack.set(IGDataComponents.PICTURE_BOOK_DATA, data.withoutSlot(slot));
    }

    public static int getMaxSlots() {
        return MAX_SLOTS;
    }
}
