package net.alshanex.illusionist_grimoire.item;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.Scroll;
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

public class PictureBookItem extends Item {
    private static final String TAG_PICTURE_BOOK = "PictureBook";
    private static final String TAG_SELECTED_SLOT = "SelectedSlot";
    private static final String TAG_SLOTS = "Slots";
    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_ENTITY_NBT = "EntityNBT";
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

        // Check if player has emerald in offhand - binding mode
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

            // Save entity data to selected slot
            CompoundTag entityNBT = new CompoundTag();
            targetEntity.saveWithoutId(entityNBT);
            ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(targetEntity.getType());

            setSlotData(stack, slot, entityType, entityNBT);

            // Consume emerald
            if (!player.isCreative()) {
                offhand.shrink(1);
            }

            // Success feedback
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(
                        new ClientboundSetActionBarTextPacket(
                                Component.translatable("message.illusionist_grimoire.picture_book.entity_bound",
                                                slot + 1, targetEntity.getDisplayName())
                                        .withStyle(ChatFormatting.GREEN)
                        )
                );
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
        SlotData slotData = getSlotData(stack, slot);
        if (slotData == null) {
            return InteractionResultHolder.fail(stack);
        }

        // Create mob entity
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
        if (type == null) {
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity mobEntity = (LivingEntity) type.create(level);
        if (mobEntity != null) {
            mobEntity.load(slotData.nbt());

            // Apply disguise
            DisguiseData disguiseData = DisguiseData.getDisguiseData(player);
            disguiseData.setMobDisguiseEntity(mobEntity);

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new IGSyncPlayerDataPacket(disguiseData));

            // Apply effect
            player.addEffect(new MobEffectInstance(IGEffectRegistry.DISGUISED, DISGUISE_DURATION, 0));

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
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

        SlotData slotData = getSlotData(stack, selectedSlot);
        if (slotData != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
            if (type != null) {
                tooltipComponents.add(Component.translatable("tooltip.illusionist_grimoire.picture_book.bound", type.getDescription()));
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
    public static SlotData getSlotData(ItemStack stack, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) {
            return null;
        }
        PictureBookData data = stack.getOrDefault(IGDataComponents.PICTURE_BOOK_DATA, PictureBookData.DEFAULT);
        PictureBookData.SlotData componentSlot = data.slots().get(slot);

        if (componentSlot == null) {
            return null;
        }

        return new SlotData(componentSlot.entityType(), componentSlot.nbt());
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

    // === Helper Record ===

    public record SlotData(ResourceLocation entityType, CompoundTag nbt) {}
}
