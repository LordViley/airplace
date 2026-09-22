package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlaceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds every placeable spot in the configured box around the player, then works out which
 * one the look-ray enters first.
 * <p>
 * The box always stays anchored to the player - it's a local building aid, not a long-range
 * targeting reticle - but the box can be made as large as you like via
 * {@code horizontalRadius}/{@code verticalRadius} in the config, and looking around picks out
 * a specific cell within it, corners and diagonals included.
 * <p>
 * Finding the exact target cell can't use {@code Minecraft#hitResult}: vanilla raytracing
 * skips air entirely, which is exactly what we need to hit. Instead each candidate gets a
 * phantom unit cube and we keep the nearest intersection.
 */
public final class GhostTargeting {

    private static final List<AABB> UNIT_CUBE = List.of(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));

    public record Result(List<BlockPos> candidates, @Nullable BlockHitResult target) {
        public boolean hasTarget() {
            return target != null;
        }
    }

    public static Result compute(Player player, Level level, float partialTick) {
        int hr = AirPlaceConfig.HORIZONTAL_RADIUS.get();
        int vr = AirPlaceConfig.VERTICAL_RADIUS.get();
        boolean strictAir = AirPlaceConfig.STRICT_AIR_ONLY.get();

        BlockPos anchor = player.blockPosition();
        AABB playerBox = player.getBoundingBox().deflate(1.0E-4D);

        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -hr; dx <= hr; dx++) {
            for (int dy = -vr; dy <= vr; dy++) {
                for (int dz = -hr; dz <= hr; dz++) {
                    BlockPos pos = anchor.offset(dx, dy, dz);
                    if (level.isOutsideBuildHeight(pos)) continue;

                    var state = level.getBlockState(pos);
                    if (strictAir ? !state.isAir() : !state.canBeReplaced()) continue;

                    // Skip anything we are standing in, so you can't wall yourself up.
                    if (playerBox.intersects(new AABB(pos))) continue;

                    candidates.add(pos);
                }
            }
        }

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 end = eye.add(player.getViewVector(partialTick).scale(AirPlaceConfig.REACH.get()));

        BlockHitResult best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos pos : candidates) {
            // Static AABB.clip offsets the boxes by pos and hands back the entry face for free,
            // which is what lets slabs and stairs orient correctly.
            BlockHitResult hit = AABB.clip(UNIT_CUBE, eye, end, pos);
            if (hit == null) continue;

            double distSq = eye.distanceToSqr(hit.getLocation());
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = hit;
            }
        }

        return new Result(candidates, best);
    }

    private GhostTargeting() {}
}
