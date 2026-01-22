package net.alshanex.illusionist_grimoire.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alshanex.illusionist_grimoire.item.PictureBookItem;
import net.alshanex.illusionist_grimoire.network.SelectDisguiseSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

public class DisguiseSelectionScreen extends Screen {
    private final ItemStack amuletStack;
    private int selectedSlot;

    private static final int SLOT_SIZE = 40;
    private static final int SLOT_PADDING = 8;
    private static final int SLOTS_PER_ROW = 4;
    private static final int TITLE_HEIGHT = 20;

    private int gridStartX;
    private int gridStartY;

    public DisguiseSelectionScreen(ItemStack amuletStack) {
        super(Component.translatable("gui.illusionist_grimoire.disguise_selection"));
        this.amuletStack = amuletStack;
        this.selectedSlot = PictureBookItem.getSelectedSlot(amuletStack);
    }

    @Override
    protected void init() {
        super.init();

        // Calculate grid position (centered)
        int totalWidth = SLOTS_PER_ROW * SLOT_SIZE + (SLOTS_PER_ROW - 1) * SLOT_PADDING;
        int totalHeight = 2 * SLOT_SIZE + SLOT_PADDING;

        this.gridStartX = (this.width - totalWidth) / 2;
        this.gridStartY = (this.height - totalHeight) / 2 + TITLE_HEIGHT;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Semi-transparent background
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.gridStartY - TITLE_HEIGHT, 0xFFFFFF);

        // Render slots
        for (int i = 0; i < PictureBookItem.getMaxSlots(); i++) {
            renderSlot(graphics, i, mouseX, mouseY, partialTick);
        }

        // Instructions
        Component instructions = Component.translatable("gui.illusionist_grimoire.disguise_selection.instructions");
        graphics.drawCenteredString(this.font, instructions, this.width / 2, this.gridStartY + 2 * SLOT_SIZE + SLOT_PADDING + 20, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSlot(GuiGraphics graphics, int slot, int mouseX, int mouseY, float partialTick) {
        int row = slot / SLOTS_PER_ROW;
        int col = slot % SLOTS_PER_ROW;

        int x = gridStartX + col * (SLOT_SIZE + SLOT_PADDING);
        int y = gridStartY + row * (SLOT_SIZE + SLOT_PADDING);

        boolean isHovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
        boolean isSelected = slot == selectedSlot;

        // Slot background
        int backgroundColor = isSelected ? 0xFF4A4A4A : (isHovered ? 0xFF3A3A3A : 0xFF2A2A2A);
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, backgroundColor);

        // Border
        int borderColor = isSelected ? 0xFF00FF00 : (isHovered ? 0xFFFFFFFF : 0xFF8B8B8B);
        // Top
        graphics.fill(x, y, x + SLOT_SIZE, y + 1, borderColor);
        // Bottom
        graphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, borderColor);
        // Left
        graphics.fill(x, y, x + 1, y + SLOT_SIZE, borderColor);
        // Right
        graphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, borderColor);

        // Slot number
        String slotNumber = String.valueOf(slot + 1);
        graphics.drawString(this.font, slotNumber, x + 2, y + 2, 0xFFFFFF);

        // Render entity if present
        PictureBookItem.SlotData slotData = PictureBookItem.getSlotData(amuletStack, slot);
        if (slotData != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
            if (entityType != null) {
                LivingEntity entity = (LivingEntity) entityType.create(Minecraft.getInstance().level);
                if (entity != null) {
                    entity.load(slotData.nbt());
                    int numberSpace = 12; // Space occupied by slot number at top
                    int availableHeight = SLOT_SIZE - numberSpace;
                    int entityCenterY = y + numberSpace + (availableHeight / 2);
                    renderEntity(graphics, x + SLOT_SIZE / 2, entityCenterY, entity);
                }
            }
        } else {
            // Empty slot indicator
            Component emptyText = Component.literal("?");
            int textX = x + (SLOT_SIZE - this.font.width(emptyText)) / 2;
            int textY = y + (SLOT_SIZE - this.font.lineHeight) / 2;
            graphics.drawString(this.font, emptyText, textX, textY, 0x666666);
        }

        // Tooltip on hover
        if (isHovered && slotData != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(slotData.entityType());
            if (entityType != null) {
                graphics.renderTooltip(this.font, entityType.getDescription(), mouseX, mouseY);
            }
        }
    }

    private void renderEntity(GuiGraphics graphics, int x, int y, LivingEntity entity) {
        // Calculate scale based on entity size
        float entityHeight = entity.getBbHeight();
        float entityWidth = entity.getBbWidth();

        // Account for slot number space at top (12px)
        // Available space in slot: 40x40, minus 12px for number = ~28px vertically, 40px horizontally
        // Use 26px to leave some margin
        float targetVerticalSize = 26.0f;
        float targetHorizontalSize = 30.0f;

        // Scale to fit within available space
        float scaleVertical = targetVerticalSize / entityHeight;
        float scaleHorizontal = targetHorizontalSize / entityWidth;
        float scale = Math.min(scaleVertical, scaleHorizontal);
        scale = Math.min(scale, 20.0f); // Cap maximum scale to avoid huge tiny entities

        graphics.pose().pushPose();

        // Move to slot center first
        graphics.pose().translate(x, y, 50);
        graphics.pose().scale(scale, scale, scale);

        // Rotate entity - flip upside down and slightly tilt for better view
        Quaternionf quaternion = new Quaternionf().rotateZ((float) Math.PI); // 180° flip
        Quaternionf quaternion2 = new Quaternionf().rotateX(10.0f * (float) Math.PI / 180.0f); // Reduced to 10° tilt
        quaternion.mul(quaternion2);
        graphics.pose().mulPose(quaternion);

        // Make entity look straight ahead (toward camera)
        entity.yBodyRot = 0f;
        entity.setYRot(0f);
        entity.yHeadRot = 0f;
        entity.yHeadRotO = 0f;
        entity.setXRot(0f);
        entity.xRotO = 0f;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        dispatcher.overrideCameraOrientation(quaternion2);
        dispatcher.setRenderShadow(false);

        // Calculate vertical offset to center the entity
        // Entities render from feet (y=0), so we offset by half height to center them
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
        // Left click to select slot
        if (button == 0) {
            for (int i = 0; i < PictureBookItem.getMaxSlots(); i++) {
                int row = i / SLOTS_PER_ROW;
                int col = i % SLOTS_PER_ROW;

                int x = gridStartX + col * (SLOT_SIZE + SLOT_PADDING);
                int y = gridStartY + row * (SLOT_SIZE + SLOT_PADDING);

                if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                    selectSlot(i);
                    return true;
                }
            }
        }
        // Right click to clear slot
        else if (button == 1) {
            for (int i = 0; i < PictureBookItem.getMaxSlots(); i++) {
                int row = i / SLOTS_PER_ROW;
                int col = i % SLOTS_PER_ROW;

                int x = gridStartX + col * (SLOT_SIZE + SLOT_PADDING);
                int y = gridStartY + row * (SLOT_SIZE + SLOT_PADDING);

                if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                    clearSlot(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectSlot(int slot) {
        this.selectedSlot = slot;
        PictureBookItem.setSelectedSlot(amuletStack, slot);

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
        PictureBookItem.clearSlot(amuletStack, slot);

        // Send packet to server
        PacketDistributor.sendToServer(new SelectDisguiseSlotPacket(slot));

        // Play sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.3f, 0.8f);
        }

        // Refresh display
        this.init();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
