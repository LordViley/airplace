package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlaceConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AirPlaceNetwork {

    /** Called from the main mod initializer - registers the payload type on both sides. */
    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(PlaceInAirPayload.TYPE, PlaceInAirPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlaceInAirPayload.TYPE, (payload, context) ->
                context.server().execute(() -> place(context.player(), payload)));
    }

    /**
     * Re-checks everything the client claimed. Never trust the packet: a modified client
     * could otherwise place blocks across the map or through claim protection.
     */
    private static void place(ServerPlayer player, PlaceInAirPayload payload) {
        BlockPos pos = payload.pos();
        ServerLevel level = player.level();

        if (player.isSpectator() || !player.mayBuild()) return;
        if (!level.isLoaded(pos)) return;
        if (level.isOutsideBuildHeight(pos)) return;

        AirPlaceConfig.Data cfg = AirPlaceConfig.CURRENT;

        double maxDist = cfg.reach + 1.5D;
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > maxDist * maxDist) return;

        BlockPos anchor = player.blockPosition();
        int hr = cfg.horizontalRadius;
        int vr = cfg.verticalRadius;
        if (Math.abs(pos.getX() - anchor.getX()) > hr) return;
        if (Math.abs(pos.getZ() - anchor.getZ()) > hr) return;
        if (Math.abs(pos.getY() - anchor.getY()) > vr) return;

        if (!player.mayInteract(level, pos)) return;

        if (cfg.strictAirOnly) {
            if (!level.getBlockState(pos).isAir()) return;
        } else if (!level.getBlockState(pos).canBeReplaced()) {
            return;
        }

        // Don't let anyone seal themselves inside a block.
        if (player.getBoundingBox().intersects(new AABB(pos))) return;

        InteractionHand hand = payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;

        Direction face = payload.direction();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 hitVec = Vec3.atCenterOf(pos).add(normal.scale(0.5D));

        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hit));

        blockItem.place(context);
    }

    private AirPlaceNetwork() {}
}
