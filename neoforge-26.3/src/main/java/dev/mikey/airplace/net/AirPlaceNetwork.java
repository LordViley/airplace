package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlaceConfig;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AirPlaceNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PlaceInAirPayload.TYPE, PlaceInAirPayload.CODEC, AirPlaceNetwork::handle);
    }

    private static void handle(PlaceInAirPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                place(player, payload);
            }
        });
    }

    private static void place(ServerPlayer player, PlaceInAirPayload payload) {
        BlockPos pos = payload.pos();
        // NOTE: ServerPlayer#serverLevel() was renamed - level() now returns ServerLevel
        // directly via a covariant override (confirmed against real 26.1/26.3 vanilla docs).
        ServerLevel level = player.level();

        if (player.isSpectator() || !player.mayBuild()) return;
        if (!level.isLoaded(pos)) return;
        if (level.isOutsideBuildHeight(pos)) return;

        double maxDist = AirPlaceConfig.REACH.get() + 1.5D;
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > maxDist * maxDist) return;

        BlockPos anchor = player.blockPosition();
        int hr = AirPlaceConfig.HORIZONTAL_RADIUS.get();
        int vr = AirPlaceConfig.VERTICAL_RADIUS.get();
        if (Math.abs(pos.getX() - anchor.getX()) > hr) return;
        if (Math.abs(pos.getZ() - anchor.getZ()) > hr) return;
        if (Math.abs(pos.getY() - anchor.getY()) > vr) return;

        // NOTE: mayInteract moved from Level to ServerPlayer - it's now player.mayInteract(level, pos).
        if (!player.mayInteract(level, pos)) return;

        if (AirPlaceConfig.STRICT_AIR_ONLY.get()) {
            if (!level.getBlockState(pos).isAir()) return;
        } else if (!level.getBlockState(pos).canBeReplaced()) {
            return;
        }

        if (player.getBoundingBox().intersects(new AABB(pos))) return;

        InteractionHand hand = payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;

        Direction face = payload.direction();
        // NOTE: Direction#getNormal() was reorganized in 26.x - build the vector from the
        // long-stable getStepX/Y/Z() accessors instead (confirmed via real vanilla docs).
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 hitVec = Vec3.atCenterOf(pos).add(normal.scale(0.5D));

        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hit));

        blockItem.place(context);
    }

    private AirPlaceNetwork() {}
}
