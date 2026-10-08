package dev.mikey.airplace.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mikey.airplace.AirPlaceConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GhostRenderer {

    private static final double CANDIDATE_INSET = 0.22D; // small marker cube
    private static final double TARGET_INSET = 0.002D;   // near-full outline

    public static void register() {
        // After translucent terrain, so the lines depth-sort sensibly against water/glass.
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            GhostTargeting.Result result = AirPlaceClient.getState();
            if (!AirPlaceClient.isActive() || result == null || result.candidates().isEmpty()) return;

            PoseStack pose = context.matrixStack();
            if (pose == null) return;

            AirPlaceConfig.Data cfg = AirPlaceConfig.CURRENT;
            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            VertexConsumer lines = buffers.getBuffer(RenderType.lines());

            int candidateRgb = AirPlaceConfig.rgb(cfg.candidateColor, 0xFF3B30);
            int targetRgb = AirPlaceConfig.rgb(cfg.targetColor, 0x34C759);
            float alpha = (float) cfg.outlineAlpha;

            BlockPos targetPos = result.hasTarget() ? result.target().getBlockPos() : null;
            Vec3 cam = context.camera().getPosition();

            pose.pushPose();
            // The pose stack starts at the world origin; shift into camera-relative coordinates.
            pose.translate(-cam.x, -cam.y, -cam.z);

            for (BlockPos pos : result.candidates()) {
                boolean isTarget = pos.equals(targetPos);
                int rgb = isTarget ? targetRgb : candidateRgb;
                AABB box = new AABB(pos).deflate(isTarget ? TARGET_INSET : CANDIDATE_INSET);

                LevelRenderer.renderLineBox(
                        pose, lines, box,
                        ((rgb >> 16) & 0xFF) / 255.0F,
                        ((rgb >> 8) & 0xFF) / 255.0F,
                        (rgb & 0xFF) / 255.0F,
                        isTarget ? alpha : alpha * 0.55F);
            }

            pose.popPose();

            // Flush now; RenderType.lines() is not auto-drawn at this point.
            buffers.endBatch(RenderType.lines());
        });
    }

    private GhostRenderer() {}
}
