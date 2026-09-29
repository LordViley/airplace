package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlaceConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;

/**
 * Draws the red/green highlight using vanilla's own debug-drawing API (the same one F3+B
 * hitboxes use).
 * <p>
 * Minecraft 26.3 replaced its whole GPU layer (blaze3d -> renderpearl, with separate OpenGL and
 * Vulkan backends), so hand-rolled draw calls are a moving target. Gizmos sit above all of that,
 * so this keeps working however the renderer underneath changes.
 * <p>
 * Per Fabric's own event index, gizmos are collected during the extraction phase via
 * {@code LevelExtractionEvents.END_EXTRACTION} - "called after all render states have been
 * extracted, before any are drawn" - not a separate debug-render draw event.
 */
public final class GhostRenderer {

    private static final double CANDIDATE_INSET = 0.30D; // small cube
    private static final double TARGET_INSET = 0.02D;    // near-full cube

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> draw());
    }

    private static void draw() {
        GhostTargeting.Result result = AirPlaceClient.getState();
        if (!AirPlaceClient.isActive() || result == null || result.candidates().isEmpty()) return;

        AirPlaceConfig.Data cfg = AirPlaceConfig.CURRENT;
        int alpha = (int) Math.round(Math.max(0.05D, Math.min(1.0D, cfg.outlineAlpha)) * 255.0D);
        int candidateArgb = (alpha << 24) | AirPlaceConfig.rgb(cfg.candidateColor, 0xFF3B30);
        int targetArgb = (0xFF << 24) | AirPlaceConfig.rgb(cfg.targetColor, 0x34C759);

        BlockPos targetPos = result.hasTarget() ? result.target().getBlockPos() : null;

        for (BlockPos pos : result.candidates()) {
            if (pos.equals(targetPos)) continue; // drawn last so it sits on top
            Gizmos.cuboid(box(pos, CANDIDATE_INSET), GizmoStyle.stroke(candidateArgb));
        }
        if (targetPos != null) {
            Gizmos.cuboid(box(targetPos, TARGET_INSET), GizmoStyle.stroke(targetArgb));
        }
    }

    private static AABB box(BlockPos pos, double inset) {
        return new AABB(
                pos.getX() + inset, pos.getY() + inset, pos.getZ() + inset,
                pos.getX() + 1 - inset, pos.getY() + 1 - inset, pos.getZ() + 1 - inset);
    }

    private GhostRenderer() {}
}
