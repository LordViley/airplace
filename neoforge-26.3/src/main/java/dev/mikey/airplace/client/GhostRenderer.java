package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlace;
import dev.mikey.airplace.AirPlaceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Draws the red/green highlight using vanilla's own debug-drawing API (Gizmos - the same one
 * F3+B hitboxes use), the same approach that ended up working for the Fabric 26.3 port.
 * <p>
 * NOTE (highest-risk file in this port): Minecraft 26.x rewrote its whole GPU layer
 * (blaze3d -> renderpearl, split OpenGL/Vulkan backends) and reorganized rendering into a
 * separate "extraction" phase and "drawing" phase. That rewrite is in vanilla itself, so it
 * broke the old RenderType.lines()/renderLineBox/MultiBufferSource path on Fabric regardless of
 * loader - which is why this skips straight to Gizmos rather than repeating that mistake here.
 * <p>
 * NOTE: confirmed via real NeoForge javadoc for this generation - RenderLevelStageEvent no
 * longer uses a Stage enum with getStage(); it's split into separate nested static event
 * classes (AfterSky, AfterOpaqueBlocks, AfterEntities, AfterLevel, etc.), each independently
 * dispatched. This subscribes to RenderLevelStageEvent.AfterLevel (fires latest, after
 * everything else), same reasoning as the original AFTER_LEVEL guess - still unverified
 * whether it overlaps the gizmo-collector window, hence the same try/catch safety net.
 */
@EventBusSubscriber(modid = AirPlace.MODID, value = Dist.CLIENT)
public final class GhostRenderer {

    private static final double CANDIDATE_INSET = 0.30D;
    private static final double TARGET_INSET = 0.02D;

    private static boolean warned;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
        draw();
    }

    private static void draw() {
        GhostTargeting.Result result = AirPlaceClient.getState();
        if (!AirPlaceClient.isActive() || result == null || result.candidates().isEmpty()) return;

        int alpha = (int) Math.round(Math.max(0.05D, Math.min(1.0D, AirPlaceConfig.OUTLINE_ALPHA.get())) * 255.0D);
        int candidateArgb = (alpha << 24) | AirPlaceConfig.rgb(AirPlaceConfig.COLOR_CANDIDATE.get(), 0xFF3B30);
        int targetArgb = (0xFF << 24) | AirPlaceConfig.rgb(AirPlaceConfig.COLOR_TARGET.get(), 0x34C759);

        BlockPos targetPos = result.hasTarget() ? result.target().getBlockPos() : null;

        try {
            for (BlockPos pos : result.candidates()) {
                if (pos.equals(targetPos)) continue;
                Gizmos.cuboid(box(pos, CANDIDATE_INSET), GizmoStyle.stroke(candidateArgb));
            }
            if (targetPos != null) {
                Gizmos.cuboid(box(targetPos, TARGET_INSET), GizmoStyle.stroke(targetArgb));
            }
        } catch (IllegalStateException e) {
            if (!warned) {
                warned = true;
                AirPlace.LOGGER.warn("AirPlace: gizmos could not be drawn from RenderLevelStageEvent.AFTER_LEVEL "
                        + "- the collector likely isn't active at this stage on NeoForge 26.3", e);
            }
        }
    }

    private static AABB box(BlockPos pos, double inset) {
        return new AABB(
                pos.getX() + inset, pos.getY() + inset, pos.getZ() + inset,
                pos.getX() + 1 - inset, pos.getY() + 1 - inset, pos.getZ() + 1 - inset);
    }

    private GhostRenderer() {}
}
