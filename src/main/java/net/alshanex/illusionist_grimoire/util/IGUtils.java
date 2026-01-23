package net.alshanex.illusionist_grimoire.util;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.alshanex.illusionist_grimoire.item.PictureBookItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IGUtils {
    public static boolean canMobBypassIllusions(LivingEntity entity) {
        boolean resistEvocation = entity.getAttributeValue(AttributeRegistry.SPELL_RESIST) > 1.2;
        return resistEvocation || entity.getType().is(ModTags.ILLUSION_IMMUNE_ENTITIES);
    }

    public static boolean canBypassIllusions(LivingEntity entity, double illusionistSpellPower) {
        double resist = (1 - entity.getAttributeValue(AttributeRegistry.SPELL_RESIST));
        return resist > (Math.max(0, (illusionistSpellPower - 1)) / 1.5);
    }

    public static void handleSlotSelection(Player player, int slot){
        if (player != null) {
            InteractionHand hand = InteractionHand.MAIN_HAND;
            ItemStack stack = player.getItemInHand(hand);

            if (stack.getItem() instanceof PictureBookItem) {
                PictureBookItem.setSelectedSlot(stack, slot);
            }
        }
    }

    public static void handleSlotClear(Player player, int slot){
        if (player != null) {
            InteractionHand hand = InteractionHand.MAIN_HAND;
            ItemStack stack = player.getItemInHand(hand);

            if (stack.getItem() instanceof PictureBookItem) {
                PictureBookItem.clearSlot(stack, slot);
            }
        }
    }
}
