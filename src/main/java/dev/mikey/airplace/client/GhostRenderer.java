package dev.mikey.airplace.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mikey.airplace.AirPlace;
import dev.mikey.airplace.AirPlaceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = AirPlace.MODID, value = Dist.CLIENT)
public final class GhostRenderer {

    private static final double CANDIDATE_INSET = 0.22D; // small marker cube
    private static final double TARGET_INSET = 0.002D;   // near-full outline

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // AFTER_PARTICLES runs after translucent terrain, so line depth-sorts sensibly
        // against water and glass.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        GhostTargeting.Result result = AirPlaceClient.getState();
        if (!AirPlaceClient.isActive() || result == null || result.candidates().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        int candidateRgb = AirPlaceConfig.rgb(AirPlaceConfig.COLOR_CANDIDATE.get(), 0xFF3B30);
        int targetRgb = AirPlaceConfig.rgb(AirPlaceConfig.COLOR_TARGET.get(), 0x34C759);
        float alpha = AirPlaceConfig.OUTLINE_ALPHA.get().floatValue();

        BlockPos targetPos = result.hasTarget() ? result.target().getBlockPos() : null;

        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();

        pose.pushPose();
        // The pose stack starts at camera space; shift into world coordinates.
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

        // Flush now; RenderType.lines() is not auto-drawn at this stage.
        buffers.endBatch(RenderType.lines());
    }

    private GhostRenderer() {}
}
