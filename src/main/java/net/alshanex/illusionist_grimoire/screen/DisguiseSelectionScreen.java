package net.alshanex.illusionist_grimoire.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.alshanex.illusionist_grimoire.IllusionistGrimoireMod;
import net.alshanex.illusionist_grimoire.data.PictureBookData;
import net.alshanex.illusionist_grimoire.item.PictureBookItem;
import net.alshanex.illusionist_grimoire.network.ClearDisguiseSlotPacket;
import net.alshanex.illusionist_grimoire.network.SelectDisguiseSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.UUID;

public class DisguiseSelectionScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IllusionistGrimoireMod.MODID, "textures/gui/picture_book_gui.png");

    private final ItemStack pictureBookStack;
    private int selectedSlot;

    // GUI texture dimensions
    private static final int GUI_WIDTH = 255;
    private static final int GUI_HEIGHT = 152;

    // Slot dimensions
    private static final int SLOT_WIDTH = 26;
    private static final int SLOT_HEIGHT = 29;

    // Slot positions
    private static final SlotPosition[] SLOT_POSITIONS = {
            new SlotPosition(31, 40),   // Slot 1
            new SlotPosition(80, 40),   // Slot 2
            new SlotPosition(31, 95),   // Slot 3
            new SlotPosition(80, 95),   // Slot 4
            new SlotPosition(149, 40),  // Slot 5
            new SlotPosition(198, 40),  // Slot 6
            new SlotPosition(149, 95),  // Slot 7
            new SlotPosition(198, 95)   // Slot 8
    };

    private int guiLeft;
    private int guiTop;

    public DisguiseSelectionScreen(ItemStack pictureBookStack) {
        super(Component.translatable("gui.illusionist_grimoire.disguise_selection"));
        this.pictureBookStack = pictureBookStack;
        this.selectedSlot = PictureBookItem.getSelectedSlot(pictureBookStack);
    }

    @Override
    protected void init() {
        super.init();

        // Center the GUI
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render the GUI texture
        graphics.blit(GUI_TEXTURE, guiLeft, guiTop, 0, 0, 255, 152, 255, 255);

        // Render entities in slots
        for (int i = 0; i < PictureBookItem.getMaxSlots(); i++) {
            renderSlot(graphics, i, mouseX, mouseY, partialTick);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSlot(GuiGraphics graphics, int slot, int mouseX, int mouseY, float partialTick) {
        SlotPosition pos = SLOT_POSITIONS[slot];
        int slotX = guiLeft + pos.x;
        int slotY = guiTop + pos.y;

        boolean isHovered = mouseX >= slotX && mouseX < slotX + SLOT_WIDTH &&
                mouseY >= slotY && mouseY < slotY + SLOT_HEIGHT;
        boolean isSelected = slot == selectedSlot;

        // Render selection highlight
        if (isSelected) {
            // Green border for selected slot (1px wide)
            graphics.fill(slotX - 1, slotY - 1, slotX + SLOT_WIDTH + 1, slotY, 0xFF00FF00);
            graphics.fill(slotX - 1, slotY + SLOT_HEIGHT, slotX + SLOT_WIDTH + 1, slotY + SLOT_HEIGHT + 1, 0xFF00FF00);
            graphics.fill(slotX - 1, slotY, slotX, slotY + SLOT_HEIGHT, 0xFF00FF00);
            graphics.fill(slotX + SLOT_WIDTH, slotY, slotX + SLOT_WIDTH + 1, slotY + SLOT_HEIGHT, 0xFF00FF00);
        } else if (isHovered) {
            // White highlight for hover
            graphics.fill(slotX - 1, slotY - 1, slotX + SLOT_WIDTH + 1, slotY, 0x80FFFFFF);
            graphics.fill(slotX - 1, slotY + SLOT_HEIGHT, slotX + SLOT_WIDTH + 1, slotY + SLOT_HEIGHT + 1, 0x80FFFFFF);
            graphics.fill(slotX - 1, slotY, slotX, slotY + SLOT_HEIGHT, 0x80FFFFFF);
            graphics.fill(slotX + SLOT_WIDTH, slotY, slotX + SLOT_WIDTH + 1, slotY + SLOT_HEIGHT, 0x80FFFFFF);
        }

        // Render entity if present
        PictureBookData.SlotData slotData = PictureBookItem.getSlotData(pictureBookStack, slot);
        if (slotData != null) {
            if (slotData.isPlayerDisguise()) {
                // Render player model with skin
                renderPlayerModel(graphics, slotX + SLOT_WIDTH / 2, slotY + SLOT_HEIGHT - 3,
                        slotData.playerUUID(), slotData.playerName(),
                        slotData.skinTexture(), slotData.skinSignature());
            } else {
                // Render mob entity
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
                if (entityType != null) {
                    LivingEntity entity = (LivingEntity) entityType.create(Minecraft.getInstance().level);
                    if (entity != null) {
                        entity.load(slotData.nbt());
                        renderEntity(graphics, slotX + SLOT_WIDTH / 2, slotY + SLOT_HEIGHT / 2 + 3, entity);
                    }
                }
            }
        }

        // Tooltip on hover
        if (isHovered && slotData != null) {
            if (slotData.isPlayerDisguise()) {
                graphics.renderTooltip(this.font, Component.literal(slotData.playerName()), mouseX, mouseY);
            } else {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
                if (entityType != null) {
                    graphics.renderTooltip(this.font, entityType.getDescription(), mouseX, mouseY);
                }
            }
        }
    }

    private void renderPlayerModel(GuiGraphics graphics, int x, int y, UUID playerUUID, String playerName, @Nullable String skinTexture, @Nullable String skinSignature) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Create GameProfile with skin properties
        GameProfile profile = new GameProfile(playerUUID, playerName);

        if (skinTexture != null) {
            profile.getProperties().put("textures",
                    new com.mojang.authlib.properties.Property("textures", skinTexture, skinSignature));
        }

        // Create a fake AbstractClientPlayer for rendering
        AbstractClientPlayer fakePlayer = new AbstractClientPlayer(mc.level, profile) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        // Get player renderer
        PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(fakePlayer);

        float scale = 12.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 50);
        graphics.pose().scale(scale, scale, scale);

        // Rotate player model
        Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternion2 = new Quaternionf().rotateX(10.0f * (float) Math.PI / 180.0f);
        quaternion.mul(quaternion2);
        graphics.pose().mulPose(quaternion);

        // Set player orientation
        fakePlayer.yBodyRot = 0.0f;
        fakePlayer.setYRot(0.0f);
        fakePlayer.yHeadRot = 0.0f;
        fakePlayer.yHeadRotO = 0.0f;
        fakePlayer.setXRot(0.0f);
        fakePlayer.xRotO = 0.0f;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        quaternion2.conjugate();
        dispatcher.overrideCameraOrientation(quaternion2);
        dispatcher.setRenderShadow(false);

        RenderSystem.runAsFancy(() -> {
            playerRenderer.render(fakePlayer, 0.0f, 1.0f, graphics.pose(), graphics.bufferSource(), 15728880);
        });

        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        graphics.flush();
    }

    private void renderEntity(GuiGraphics graphics, int x, int y, LivingEntity entity) {
        // Calculate scale based on entity size
        float entityHeight = entity.getBbHeight();
        float entityWidth = entity.getBbWidth();

        // Available space: 26x29 slot, use ~24px to leave margin
        float targetSize = 24.0f;

        // Scale to fit within available space
        float scaleVertical = targetSize / entityHeight;
        float scaleHorizontal = targetSize / entityWidth;
        float scale = Math.min(scaleVertical, scaleHorizontal);
        scale = Math.min(scale, 15.0f); // Cap maximum scale

        graphics.pose().pushPose();

        // Move to slot center
        graphics.pose().translate(x, y, 50);
        graphics.pose().scale(scale, scale, scale);

        // Rotate entity
        Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternion2 = new Quaternionf().rotateX(10.0f * (float) Math.PI / 180.0f);
        quaternion.mul(quaternion2);
        graphics.pose().mulPose(quaternion);

        // Make entity look straight ahead
        entity.yBodyRot = 0f;
        entity.setYRot(0f);
        entity.yHeadRot = 0f;
        entity.yHeadRotO = 0f;
        entity.setXRot(0.0f);
        entity.xRotO = 0.0f;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        dispatcher.overrideCameraOrientation(quaternion2);
        dispatcher.setRenderShadow(false);

        // Center entity vertically
        double yOffset = entityHeight / 2.0;

        RenderSystem.runAsFancy(() -> {
            dispatcher.render(entity, 0, -yOffset, 0, 0.0f, 1.0f, graphics.pose(), graphics.bufferSource(), 15728880);
        });

        dispatcher.setRenderShadow(true);
        graphics.pose().popPose();
        graphics.flush();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicked on any slot
        for (int i = 0; i < PictureBookItem.getMaxSlots(); i++) {
            SlotPosition pos = SLOT_POSITIONS[i];
            int slotX = guiLeft + pos.x;
            int slotY = guiTop + pos.y;

            if (mouseX >= slotX && mouseX < slotX + SLOT_WIDTH &&
                    mouseY >= slotY && mouseY < slotY + SLOT_HEIGHT) {

                // Left click to select
                if (button == 0) {
                    selectSlot(i);
                    return true;
                }
                // Right click to clear
                else if (button == 1) {
                    clearSlot(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectSlot(int slot) {
        this.selectedSlot = slot;
        PictureBookItem.setSelectedSlot(pictureBookStack, slot);

        // Send packet to server
        PacketDistributor.sendToServer(new SelectDisguiseSlotPacket(slot));

        // Play sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
        }

        // Close screen
        this.onClose();
    }

    private void clearSlot(int slot) {
        PictureBookItem.clearSlot(pictureBookStack, slot);

        // Send packet to server to clear the slot
        PacketDistributor.sendToServer(new ClearDisguiseSlotPacket(slot));

        // Play sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 0.8f);
        }

        // Refresh display
        this.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    // Helper record for slot positions
    private record SlotPosition(int x, int y) {}
}
