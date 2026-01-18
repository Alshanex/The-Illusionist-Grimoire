package net.alshanex.illusionist_grimoire.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpellTrapBlockEntityRenderer implements BlockEntityRenderer<SpellTrapBlockEntity> {
    private static final float CIRCLE_RADIUS = 0.5f;

    public SpellTrapBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SpellTrapBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // Only render when not on cooldown and has a spell
        if (blockEntity.isOnCooldown()) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) return;

        Direction facing = blockEntity.getBlockState().getValue(SpellTrapBlock.FACING);

        // Get the center position of the trap (slightly offset from the wall)
        Vec3 center = Vec3.atCenterOf(blockEntity.getBlockPos()).add(
                Vec3.atLowerCornerOf(facing.getNormal()).scale(0.56)
        );

        // Animation time
        long gameTime = level.getGameTime();
        float time = gameTime + partialTick;

        // Rotation speeds
        float outerRotation = (time * 2.0f) % 360.0f;
        float middleRotation = (-time * 2.5f) % 360.0f;
        float innerRotation = (time * 3.0f) % 360.0f;

        // Spawn outer circle particles
        int outerParticles = 16;
        for (int i = 0; i < outerParticles; i++) {
            float angle = (float) Math.toRadians((360.0f / outerParticles * i) + outerRotation);
            Vec3 particlePos = getCirclePosition(center, angle, CIRCLE_RADIUS, facing);

            if (level.random.nextFloat() < 0.4f) {
                level.addParticle(ParticleTypes.ENCHANT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
        }

        // Spawn middle circle particles (counter-rotating)
        int middleParticles = 12;
        for (int i = 0; i < middleParticles; i++) {
            float angle = (float) Math.toRadians((360.0f / middleParticles * i) + middleRotation);
            Vec3 particlePos = getCirclePosition(center, angle, CIRCLE_RADIUS * 0.7f, facing);

            if (level.random.nextFloat() < 0.4f) {
                level.addParticle(ParticleTypes.ENCHANT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
        }

        // Spawn inner circle particles
        int innerParticles = 8;
        for (int i = 0; i < innerParticles; i++) {
            float angle = (float) Math.toRadians((360.0f / innerParticles * i) + innerRotation);
            Vec3 particlePos = getCirclePosition(center, angle, CIRCLE_RADIUS * 0.4f, facing);

            if (level.random.nextFloat() < 0.4f) {
                level.addParticle(ParticleTypes.ENCHANT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
        }

        // Spawn runic symbols with pulsing effect
        int numRunes = 6;
        float runeRadius = CIRCLE_RADIUS * 0.85f;
        for (int i = 0; i < numRunes; i++) {
            float runeAngle = (float) Math.toRadians(60.0f * i + outerRotation * 0.5f);
            Vec3 runePos = getCirclePosition(center, runeAngle, runeRadius, facing);

            // Pulsing effect
            float pulse = (Mth.sin(time * 0.1f + i) + 1.0f) / 2.0f;

            if (level.random.nextFloat() < 0.2f * pulse) {
                level.addParticle(ParticleTypes.GLOW,
                        runePos.x, runePos.y, runePos.z,
                        0, 0, 0);
            }
        }

        // Center glow (occasional)
        if (gameTime % 20 == 0 && level.random.nextFloat() < 0.5f) {
            level.addParticle(ParticleTypes.END_ROD,
                    center.x, center.y, center.z,
                    0, 0, 0);
        }
    }

    private Vec3 getCirclePosition(Vec3 center, float angle, float radius, Direction facing) {
        float x = Mth.cos(angle) * radius;
        float y = Mth.sin(angle) * radius;

        // Rotate the circle to be perpendicular to the facing direction
        return switch (facing) {
            case UP, DOWN ->
                // Circle on XZ plane (horizontal)
                    new Vec3(center.x + x, center.y, center.z + y);
            case NORTH, SOUTH ->
                // Circle on XY plane (vertical, north-south facing)
                    new Vec3(center.x + x, center.y + y, center.z);
            case EAST, WEST ->
                // Circle on YZ plane (vertical, east-west facing)
                    new Vec3(center.x, center.y + y, center.z + x);
        };
    }

    @Override
    public boolean shouldRenderOffScreen(SpellTrapBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
